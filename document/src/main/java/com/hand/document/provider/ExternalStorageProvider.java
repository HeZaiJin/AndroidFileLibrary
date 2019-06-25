package com.hand.document.provider;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MatrixCursor.RowBuilder;
import android.net.Uri;
import android.os.*;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.GuardedBy;
import androidx.annotation.Nullable;
import androidx.collection.ArrayMap;
import androidx.core.os.EnvironmentCompat;
import androidx.documentfile.provider.DocumentFile;
import com.hand.document.BuildConfig;
import com.hand.document.R;
import com.hand.document.core.SAFManager;
import com.hand.document.io.Volume;
import com.hand.document.provider.DocumentsContract.Document;
import com.hand.document.provider.DocumentsContract.Root;
import com.hand.document.util.FileUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.List;

import static com.hand.document.util.FileUtils.getTypeForFile;

public class ExternalStorageProvider extends StorageProvider {
    private static final String TAG = "ExternalStorageProvider";

    private final Object mRootsLock = new Object();

    private static class RootInfo {
        public String rootId;
        public int flags;
        public String title;
        public String docId;
        public File path;
        public File visiblePath;
        public boolean reportAvailableBytes;
    }

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".externalstorage.documents";

    public static final String ROOT_ID_PRIMARY_EMULATED = "primary";
    public static final String ROOT_ID_SECONDARY = "secondary";
    public static final String ROOT_ID_PHONE = "phone";

    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID, Root.COLUMN_FLAGS, Root.COLUMN_ICON, Root.COLUMN_TITLE,
            Root.COLUMN_DOCUMENT_ID, Root.COLUMN_AVAILABLE_BYTES, Root.COLUMN_CAPACITY_BYTES, Root.COLUMN_PATH,
    };

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID, Document.COLUMN_MIME_TYPE, Document.COLUMN_PATH, Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED, Document.COLUMN_FLAGS, Document.COLUMN_SIZE, Document.COLUMN_SUMMARY,
    };

    private static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    private static String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
    }

    public static void notifyRootsChanged(Context context) {
        context.getContentResolver()
                .notifyChange(DocumentsContract.buildRootsUri(AUTHORITY), null, false);
    }

    public static void notifyDocumentsChanged(Context context, String rootId) {
        Uri uri = DocumentsContract.buildChildDocumentsUri(AUTHORITY, rootId);
        context.getContentResolver().notifyChange(uri, null, false);
    }

    @GuardedBy("mRootsLock")
    private ArrayMap<String, RootInfo> mRoots = new ArrayMap<>();

    @GuardedBy("mObservers")
    private ArrayMap<File, DirectoryObserver> mObservers = new ArrayMap<>();

    private Handler mHandler;

    @Override
    public boolean onCreate() {
        mHandler = new Handler();
        updateRoots();
        StorageVolumeProvider.get(getContext()).registerObserver(this);
        return true;
    }

    private void updateRoots() {
        synchronized (mRootsLock) {
            mRoots.clear();
            int count = 0;
            List<Volume> storageVolumes = getStorageVolumes();
            for (Volume volume : storageVolumes) {
                final File path = new File(volume.path);
                String state = EnvironmentCompat.getStorageState(path);
                final boolean mounted = Environment.MEDIA_MOUNTED.equals(state)
                        || Environment.MEDIA_MOUNTED_READ_ONLY.equals(state);
                if (!mounted) continue;

                final String rootId;
                final String title;
                if (volume.isPrimary) {
                    rootId = ROOT_ID_PRIMARY_EMULATED;
                    title = getContext().getString(R.string.root_internal_storage);
                } else if (volume.id != null) {
                    rootId = ROOT_ID_SECONDARY + volume.id;
                    String label = volume.desc;
                    title = !TextUtils.isEmpty(label) ? label
                            : getContext().getString(R.string.root_external_storage)
                            + (count > 0 ? " " + count : "");
                    count++;
                } else {
                    Log.d(TAG, "Missing UUID for " + volume.path + "; skipping");
                    continue;
                }

                if (mRoots.containsKey(rootId)) {
                    Log.w(TAG, "Duplicate UUID " + rootId + "; skipping");
                    continue;
                }

                try {
                    if (null == path.listFiles()) {
                        continue;
                    }
                    final RootInfo root = new RootInfo();
                    mRoots.put(rootId, root);

                    root.rootId = rootId;
                    root.flags = Root.FLAG_LOCAL_ONLY | Root.FLAG_ADVANCED
                            | Root.FLAG_SUPPORTS_SEARCH | Root.FLAG_SUPPORTS_IS_CHILD;
                    if (volume.state.equals(Environment.MEDIA_MOUNTED)) {
                        root.flags = Root.FLAG_SUPPORTS_CREATE | Root.FLAG_SUPPORTS_EDIT;
                    }
                    root.title = title;
                    root.path = path;
                    root.docId = getDocIdForFile(path);
                } catch (FileNotFoundException e) {
                    throw new IllegalStateException(e);
                }
            }

        }
    }

    private String getDocIdForFile(File file) throws FileNotFoundException {
        return getDocIdForFileMaybeCreate(file, false);
    }

    private String getDocIdForFileMaybeCreate(File file, boolean createNewDir)
            throws FileNotFoundException {
        String path = file.getAbsolutePath();

        // Find the most-specific root path
        boolean visiblePath = false;
        RootInfo mostSpecificRoot = getMostSpecificRootForPath(path, false);

        if (mostSpecificRoot == null) {
            // Try visible path if no internal path matches. MediaStore uses visible paths.
            visiblePath = true;
            mostSpecificRoot = getMostSpecificRootForPath(path, true);
        }

        if (mostSpecificRoot == null) {
            throw new FileNotFoundException("Failed to find root that contains " + path);
        }

        // Start at first char of path under root
        final String rootPath = visiblePath
                ? mostSpecificRoot.visiblePath.getAbsolutePath()
                : mostSpecificRoot.path.getAbsolutePath();
        if (rootPath.equals(path)) {
            path = "";
        } else if (rootPath.endsWith("/")) {
            path = path.substring(rootPath.length());
        } else {
            path = path.substring(rootPath.length() + 1);
        }

        if (!file.exists() && createNewDir) {
            Log.i(TAG, "Creating new directory " + file);
            if (!file.mkdir()) {
                Log.e(TAG, "Could not create directory " + file);
            }
        }

        return mostSpecificRoot.rootId + ':' + path;
    }

    private RootInfo getMostSpecificRootForPath(String path, boolean visible) {
        // Find the most-specific root path
        RootInfo mostSpecificRoot = null;
        String mostSpecificPath = null;
        synchronized (mRootsLock) {
            for (int i = 0; i < mRoots.size(); i++) {
                final RootInfo root = mRoots.valueAt(i);
                final File rootFile = visible ? root.visiblePath : root.path;
                if (rootFile != null) {
                    final String rootPath = rootFile.getAbsolutePath();
                    if (path.startsWith(rootPath) && (mostSpecificPath == null
                            || rootPath.length() > mostSpecificPath.length())) {
                        mostSpecificRoot = root;
                        mostSpecificPath = rootPath;
                    }
                }
            }
        }

        return mostSpecificRoot;
    }

    private void enforceRoots() {
        if (mRoots.isEmpty()) {
            updateRoots();
        }
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));
        enforceRoots();
        synchronized (mRootsLock) {
            for (RootInfo root : mRoots.values()) {
                final MatrixCursor.RowBuilder row = result.newRow();
                row.add(Root.COLUMN_ROOT_ID, root.rootId);
                row.add(Root.COLUMN_FLAGS, root.flags);
                row.add(Root.COLUMN_TITLE, root.title);
                row.add(Root.COLUMN_DOCUMENT_ID, root.docId);
                row.add(Root.COLUMN_PATH, root.path.getAbsolutePath());
                if (ROOT_ID_PRIMARY_EMULATED.equals(root.rootId)
                        || root.rootId.startsWith(ROOT_ID_SECONDARY)
                        || root.rootId.startsWith(ROOT_ID_PHONE)) {
                    final File file = root.rootId.startsWith(ROOT_ID_PHONE)
                            ? Environment.getRootDirectory() : root.path;
                    row.add(Root.COLUMN_AVAILABLE_BYTES, file.getFreeSpace());
                    row.add(Root.COLUMN_CAPACITY_BYTES, file.getTotalSpace());
                } else {
                    row.add(Root.COLUMN_AVAILABLE_BYTES, -1);
                    row.add(Root.COLUMN_CAPACITY_BYTES, -1);
                }
            }
        }
        return result;
    }

    @Override
    public Cursor queryDocument(String documentId, String[] projection) throws FileNotFoundException {
//        if (mArchiveHelper.isArchivedDocument(documentId)) {
//            return mArchiveHelper.queryDocument(documentId, projection);
//        }

        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        includeFile(result, documentId, null);
        return result;
    }

    private void includeFile(MatrixCursor result, String docId, File file)
            throws FileNotFoundException {
        if (docId == null) {
            docId = getDocIdForFile(file);
        } else {
            file = getFileForDocId(docId);
        }

        DocumentFile documentFile = SAFManager.getDocumentFile(getContext(), docId, file);

        int flags = 0;

        if (documentFile.canWrite()) {
            if (file.isDirectory()) {
                flags |= Document.FLAG_DIR_SUPPORTS_CREATE;
            } else {
                flags |= Document.FLAG_SUPPORTS_WRITE;
            }
            flags |= Document.FLAG_SUPPORTS_DELETE;
            flags |= Document.FLAG_SUPPORTS_RENAME;
            flags |= Document.FLAG_SUPPORTS_MOVE;
            flags |= Document.FLAG_SUPPORTS_COPY;
//            flags |= Document.FLAG_SUPPORTS_ARCHIVE;
//            flags |= Document.FLAG_SUPPORTS_BOOKMARK;
//            flags |= Document.FLAG_SUPPORTS_EDIT;

        }

        final String mimeType = getTypeForFile(file);
        /*if (DocumentArchiveHelper.isSupportedArchiveType(mimeType)) {
            flags |= Document.FLAG_ARCHIVE;
        }*/

        final String displayName = file.getName();
       /* if (!showFilesHidden && !TextUtils.isEmpty(displayName)) {
            if (displayName.charAt(0) == '.') {
                return;
            }
        }
        if (MimePredicate.mimeMatches(MimePredicate.VISUAL_MIMES, mimeType)) {
            flags |= Document.FLAG_SUPPORTS_THUMBNAIL;
        }*/

        final RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, displayName);
        row.add(Document.COLUMN_SIZE, file.length());
        row.add(Document.COLUMN_MIME_TYPE, mimeType);
        row.add(Document.COLUMN_PATH, file.getAbsolutePath());
        row.add(Document.COLUMN_FLAGS, flags);
        if (file.isDirectory() && null != file.list()) {
            row.add(Document.COLUMN_SUMMARY, FileUtils.formatFileCount(file.list().length));
        }

        // Only publish dates reasonably after epoch
        long lastModified = file.lastModified();
        if (lastModified > 31536000000L) {
            row.add(Document.COLUMN_LAST_MODIFIED, lastModified);
        }
    }

    protected final File getFileForDocId(String docId) throws FileNotFoundException {
        return getFileForDocId(docId, false);
    }

    protected File getFileForDocId(String docId, boolean visible) throws FileNotFoundException {
        return getFileForDocId(docId, visible, true);
    }

    private File getFileForDocId(String docId, boolean visible, boolean mustExist)
            throws FileNotFoundException {
        RootInfo root = getRootFromDocId(docId);
        return buildFile(root, docId, visible, mustExist);
    }

    private RootInfo getRootFromDocId(String docId) throws FileNotFoundException {
        final int splitIndex = docId.indexOf(':', 1);
        final String tag = docId.substring(0, splitIndex);

        RootInfo root;
        synchronized (mRootsLock) {
            root = mRoots.get(tag);
        }
        if (root == null) {
            throw new FileNotFoundException("No root for " + tag);
        }

        return root;
    }

    private File buildFile(RootInfo root, String docId, boolean visible, boolean mustExist)
            throws FileNotFoundException {
        final int splitIndex = docId.indexOf(':', 1);
        final String path = docId.substring(splitIndex + 1);

        File target = visible ? root.visiblePath : root.path;
        if (target == null) {
            return null;
        }
        if (!target.exists()) {
            target.mkdirs();
        }
        target = new File(target, path);
        if (mustExist && !target.exists()) {
            throw new FileNotFoundException("Missing file for " + docId + " at " + target);
        }
        return target;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, String[] projection, String sortOrder) throws FileNotFoundException {
        /*if (mArchiveHelper.isArchivedDocument(parentDocumentId) ||
                DocumentArchiveHelper.isSupportedArchiveType(getDocumentType(parentDocumentId))) {
            return mArchiveHelper.queryChildDocuments(parentDocumentId, projection, sortOrder);
        }*/

        final File parent = getFileForDocId(parentDocumentId);
        final MatrixCursor result = new DirectoryCursor(
                resolveDocumentProjection(projection), parentDocumentId, parent);
//        updateSettings();
        for (File file : parent.listFiles()) {
            includeFile(result, null, file);
        }
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, @Nullable String[] projection, @Nullable Bundle queryArgs) throws FileNotFoundException {
        return null;
    }

    @Override
    public ParcelFileDescriptor openDocument(String documentId, String mode, @Nullable CancellationSignal signal) throws FileNotFoundException {
//        if (mArchiveHelper.isArchivedDocument(documentId)) {
//            return mArchiveHelper.openDocument(documentId, mode, signal);
//        }

        final File file = getFileForDocId(documentId);
        final int pfdMode = ParcelFileDescriptor.parseMode(mode);
        if (pfdMode == ParcelFileDescriptor.MODE_READ_ONLY) {
            return ParcelFileDescriptor.open(file, pfdMode);
        } else {
            try {
                // When finished writing, kick off media scanner
                return ParcelFileDescriptor.open(file, pfdMode, mHandler, new ParcelFileDescriptor.OnCloseListener() {
                    @Override
                    public void onClose(IOException e) {
                        FileUtils.updateMediaStore(getContext(), file.getPath());
                    }
                });
            } catch (IOException e) {
                throw new FileNotFoundException("Failed to open for writing: " + e);
            }
        }
    }


    @Override
    public void onStorageVolumeChanged(List<Volume> newVolumes) {
        updateRoots();
        notifyRootsChanged(getContext());
    }

    private class DirectoryCursor extends MatrixCursor {
        private final File mFile;

        public DirectoryCursor(String[] columnNames, String docId, File file) {
            super(columnNames);

            final Uri notifyUri = DocumentsContract.buildChildDocumentsUri(AUTHORITY, docId);
            setNotificationUri(getContext().getContentResolver(), notifyUri);

            mFile = file;
            startObserving(mFile, notifyUri);
        }

        @Override
        public void close() {
            super.close();
            stopObserving(mFile);
        }
    }

    private void startObserving(File file, Uri notifyUri) {
        synchronized (mObservers) {
            DirectoryObserver observer = mObservers.get(file);
            if (observer == null) {
                observer = new DirectoryObserver(
                        file, getContext().getContentResolver(), notifyUri);
                observer.startWatching();
                mObservers.put(file, observer);
            }
            observer.mRefCount++;
        }
    }

    private void stopObserving(File file) {
        synchronized (mObservers) {
            DirectoryObserver observer = mObservers.get(file);
            if (observer == null) return;

            observer.mRefCount--;
            if (observer.mRefCount == 0) {
                mObservers.remove(file);
                observer.stopWatching();
            }

        }
    }

    private class DirectoryObserver extends FileObserver {
        private static final int NOTIFY_EVENTS = ATTRIB | CLOSE_WRITE | MOVED_FROM | MOVED_TO
                | CREATE | DELETE | DELETE_SELF | MOVE_SELF;

        private final File mFile;
        private final ContentResolver mResolver;
        private final Uri mNotifyUri;

        private int mRefCount = 0;

        public DirectoryObserver(File file, ContentResolver resolver, Uri notifyUri) {
            super(file.getAbsolutePath(), NOTIFY_EVENTS);
            mFile = file;
            mResolver = resolver;
            mNotifyUri = notifyUri;
        }

        @Override
        public void onEvent(int event, String path) {
            if ((event & NOTIFY_EVENTS) != 0) {
                switch ((event & NOTIFY_EVENTS)) {
                    case MOVED_FROM:
                    case MOVED_TO:
                    case CREATE:
                    case DELETE:
                        mResolver.notifyChange(mNotifyUri, null, false);
                        FileUtils.updateMediaStore(getContext(), FileUtils.makeFilePath(mFile, path));
                        break;
                }
            }
        }

        @Override
        public String toString() {
            return "DirectoryObserver{file=" + mFile.getAbsolutePath() + ", ref=" + mRefCount + "}";
        }
    }

}
