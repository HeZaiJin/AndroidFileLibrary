package com.hand.document.core;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import androidx.collection.ArrayMap;
import androidx.documentfile.provider.DocumentFile;
import com.hand.document.provider.DocumentsContract;
import com.hand.document.provider.ExternalStorageProvider;
import com.hand.document.util.BuildUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;

import static com.hand.document.provider.ExternalStorageProvider.ROOT_ID_SECONDARY;


public class SAFManager {

    private static final String TAG = "SAFManager";
    public static final int ADD_STORAGE_REQUEST_CODE = 4010;
    public static ArrayMap<String, Uri> sSecondaryRoots = new ArrayMap<>();

    public static DocumentFile getDocumentFile(Context context, String docId, File file)
            throws FileNotFoundException {

        DocumentFile documentFile = null;
        if (null != file && file.canWrite()) {
            documentFile = DocumentFile.fromFile(file);
            return documentFile;
        }
        if (docId.startsWith(ROOT_ID_SECONDARY) && BuildUtils.hasLollipop()) {
            String newDocId = docId.substring(ROOT_ID_SECONDARY.length());
            Uri uri = getRootUri(context, newDocId);
            if (null == uri) {
                if (null != file) {
                    documentFile = DocumentFile.fromFile(file);
                }
                return documentFile;
            }
            Uri fileUri = buildDocumentUriMaybeUsingTree(uri, newDocId);
            documentFile = DocumentFile.fromTreeUri(context, fileUri);
        }/* else if (docId.startsWith(ROOT_ID_USB)) {
            documentFile = UsbDocumentFile.fromUri(mContext, docId);
        }*/ else {
            if (null != file) {
                documentFile = DocumentFile.fromFile(file);
            } else {
                documentFile = DocumentFile.fromSingleUri(context,
                        DocumentsContract.buildDocumentUri(ExternalStorageProvider.AUTHORITY, docId));
            }
        }

        return documentFile;
    }

    public static DocumentFile getDocumentFile(Context context, Uri uri)
            throws FileNotFoundException {
        String docId = getRootUri(uri);
        return getDocumentFile(context, docId, null);
    }

    public static String getRootUri(Uri uri) {
        if (isTreeUri(uri)) {
            return DocumentsContract.getTreeDocumentId(uri);
        }
        return DocumentsContract.getDocumentId(uri);
    }

    private static Uri buildDocumentUriMaybeUsingTree(Uri uri, String docId) {
        return DocumentsContract.buildDocumentUriMaybeUsingTree(uri, docId);
    }

    private static boolean isTreeUri(Uri uri) {
        return DocumentsContract.isTreeUri(uri);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static Uri getRootUri(Context context, String docId) {
        Uri treeUri;
        final int splitIndex = docId.indexOf(':', 1);
        final String tag = docId.substring(0, splitIndex);

        //check in cache
        treeUri = sSecondaryRoots.get(tag);
        if (null != treeUri) {
            return treeUri;
        }

        //get root dynamically
        List<UriPermission> permissions = context.getContentResolver().getPersistedUriPermissions();
        for (UriPermission permission :
                permissions) {
            String treeRootId = getRootUri(permission.getUri());
            if (docId.startsWith(treeRootId)) {
                treeUri = permission.getUri();
                sSecondaryRoots.put(tag, treeUri);
                return treeUri;
            }
        }
        return treeUri;
    }

    /*@SuppressLint("NewApi")
    public static void takeCardUriPermission(final Activity activity, RootInfo root, DocumentInfo doc) {
        boolean useStorageAccess = Utils.hasNougat() && !Utils.hasOreo();
        if(useStorageAccess && null != doc.path){
            StorageManager storageManager = (StorageManager) activity.getSystemService(Context.STORAGE_SERVICE);
            StorageVolume storageVolume = storageManager.getStorageVolume(new File(doc.path));
            Intent intent = storageVolume.createAccessIntent(null);
            try {
                activity.startActivityForResult(intent, ADD_STORAGE_REQUEST_CODE);
            } catch (ActivityNotFoundException e){
                CrashReportingManager.logException(e, true);
            }
        } else if(Utils.hasLollipop()){
            DialogBuilder builder = new DialogBuilder(activity);
            Spanned message = Utils.fromHtml("Select root (outermost) folder of storage "
                    + "<b>" + root.title + "</b>"
                    + " to grant access from next screen");
            builder.setTitle("Grant accesss to External Storage")
                    .setMessage(message.toString())
                    .setPositiveButton("Give Access", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterfaceParam, int code) {
                            Intent intent = new Intent(ACTION_OPEN_DOCUMENT_TREE);
                            intent.setPackage("com.android.documentsui");
                            try {
                                activity.startActivityForResult(intent, ADD_STORAGE_REQUEST_CODE);
                            } catch (ActivityNotFoundException e){
                                CrashReportingManager.logException(e, true);
                            }
                        }
                    })
                    .setNegativeButton("Cancel", null);
            builder.showDialog();
        }
    }

    public static boolean onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        boolean accessGranted = false;
        boolean primaryStorage = false;
        if (requestCode == ADD_STORAGE_REQUEST_CODE ){
            if(resultCode == RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri uri = data.getData();
                    if (Utils.hasKitKat()) {
                        activity.getContentResolver().takePersistableUriPermission(uri,
                                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        String rootId = getRootUri(uri);
                        if(!rootId.startsWith(ExternalStorageProvider.ROOT_ID_PRIMARY_EMULATED)){
                            String secondaryRootId = ExternalStorageProvider.ROOT_ID_SECONDARY + rootId;
                            ExternalStorageProvider.notifyDocumentsChanged(activity, rootId);
                            accessGranted = true;
                        } else {
                            primaryStorage = true;
                        }
                    }
                }
            }
        }
        Utils.showSnackBar(activity, "Access"+ (accessGranted ? "" : " was not") +" granted"
                        + (primaryStorage ? ". Choose the external storage." : ""),
                Snackbar.LENGTH_SHORT, accessGranted ? "" : "ERROR", null);
        return accessGranted;
    }*/
}