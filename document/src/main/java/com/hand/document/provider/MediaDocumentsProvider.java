/*
 * Copyright (C) 2014 Hari Krishna Dulipudi
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.hand.document.provider;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.ParcelFileDescriptor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio;
import android.provider.MediaStore.Audio.*;
import android.provider.MediaStore.Files.FileColumns;
import android.provider.MediaStore.Images;
import android.provider.MediaStore.Images.ImageColumns;
import android.provider.MediaStore.Video;
import android.provider.MediaStore.Video.VideoColumns;
import android.text.TextUtils;
import android.text.format.DateUtils;
import androidx.annotation.Nullable;
import com.hand.document.BuildConfig;
import com.hand.document.R;
import com.hand.document.io.IoUtils;
import com.hand.document.io.Volume;
import com.hand.document.provider.DocumentsContract.Document;
import com.hand.document.provider.DocumentsContract.Root;

import java.io.FileNotFoundException;
import java.util.List;

import static com.hand.document.provider.Providers.DOCUMENTS_URI;

//import dev.dworks.apps.anexplorer.BuildConfig;
//import dev.dworks.apps.anexplorer.R;
//import dev.dworks.apps.anexplorer.cursor.MatrixCursor;
//import dev.dworks.apps.anexplorer.cursor.MatrixCursor.RowBuilder;
//import dev.dworks.apps.anexplorer.libcore.io.IoUtils;
//import dev.dworks.apps.anexplorer.model.DocumentsContract;
//import dev.dworks.apps.anexplorer.model.DocumentsContract.Document;
//import dev.dworks.apps.anexplorer.model.DocumentsContract.Root;

/**
 * Presents a {@link DocumentsContract} view of {MediaProvider} external
 * contents.
 */
public class MediaDocumentsProvider extends StorageProvider {
    @SuppressWarnings("unused")
    private static final String TAG = "MediaDocumentsProvider";

    public static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".media.documents";
    // docId format: root:id

    private static final String[] DEFAULT_ROOT_PROJECTION = new String[]{
            Root.COLUMN_ROOT_ID, Root.COLUMN_FLAGS, Root.COLUMN_ICON,
            Root.COLUMN_TITLE, Root.COLUMN_DOCUMENT_ID, Root.COLUMN_MIME_TYPES
    };

    private static final String[] DEFAULT_DOCUMENT_PROJECTION = new String[]{
            Document.COLUMN_DOCUMENT_ID, Document.COLUMN_MIME_TYPE, Document.COLUMN_PATH, Document.COLUMN_DISPLAY_NAME,
            Document.COLUMN_LAST_MODIFIED, Document.COLUMN_FLAGS, Document.COLUMN_SIZE, Document.COLUMN_ICON, Document.COLUMN_SUMMARY,
    };

    private static final String IMAGE_MIME_TYPES = joinNewline("image/*");

    private static final String VIDEO_MIME_TYPES = joinNewline("video/*");

    private static final String AUDIO_MIME_TYPES = joinNewline(
            "audio/*", "application/ogg", "application/x-flac");

    private static final String[] DOCUMENT_MIMES =
            new String[]{
                    "application/pdf",
                    "application/epub+zip",
                    "application/msword",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
                    "application/vnd.openxmlformats-officedocument.wordprocessingml.template",
                    "application/vnd.ms-excel",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                    "application/vnd.openxmlformats-officedocument.spreadsheetml.template",
                    "application/vnd.ms-powerpoint",
                    "application/vnd.openxmlformats-officedocument.presentationml.presentation",
                    "application/vnd.openxmlformats-officedocument.presentationml.template",
                    "application/vnd.openxmlformats-officedocument.presentationml.slideshow",
                    "application/vnd.oasis.opendocument.text",
                    "application/vnd.oasis.opendocument.text-master",
                    "application/vnd.oasis.opendocument.text-template",
                    "application/vnd.oasis.opendocument.text-web",
                    "application/vnd.stardivision.writer",
                    "application/vnd.stardivision.writer-global",
                    "application/vnd.sun.xml.writer",
                    "application/vnd.sun.xml.writer.global",
                    "application/vnd.sun.xml.writer.template",
                    "application/x-abiword",
                    "application/x-kword",
            };

    private static final String[] ARCHIVE_MIMES =
            new String[]{
                    "application/mac-binhex40",
                    "application/rar",
                    "application/zip",
                    "application/x-apple-diskimage",
                    "application/x-debian-package",
                    "application/x-gtar",
                    "application/x-iso9660-image",
                    "application/x-lha",
                    "application/x-lzh",
                    "application/x-lzx",
                    "application/x-stuffit",
                    "application/x-tar",
                    "application/x-webarchive",
                    "application/x-webarchive-xml",
                    "application/gzip",
                    "application/x-7z-compressed",
                    "application/x-deb",
                    "application/x-rar-compressed"
            };

    private static final String[] APK_MIMES =
            new String[]{
                    "application/vnd.android.package-archive"
            };

    private static final String DOCUMENT_MIME_TYPES =
            joinNewline(DOCUMENT_MIMES);

    private static final String ARCHIVE_MIME_TYPES =
            joinNewline(ARCHIVE_MIMES);

    private static final String APK_MIME_TYPES =
            joinNewline(APK_MIMES);

    public static final String TYPE_IMAGES_ROOT = Providers.ROOT_IMAGE;
    public static final String TYPE_IMAGES_BUCKET = "images_bucket";
    public static final String TYPE_IMAGE = "image";

    public static final String TYPE_VIDEOS_ROOT = Providers.ROOT_VIDEO;
    public static final String TYPE_VIDEOS_BUCKET = "videos_bucket";
    public static final String TYPE_VIDEO = "video";

    public static final String TYPE_AUDIO_ROOT = Providers.ROOT_AUDIO;
    public static final String TYPE_AUDIO = "audio";
    public static final String TYPE_ARTIST = "artist";
    public static final String TYPE_ALBUM = "album";

    public static final String TYPE_DOCUMENTS_ROOT = Providers.ROOT_DOCUMENTS;
    public static final String TYPE_DOCUMENTS = "documents";

    private static boolean sReturnedImagesEmpty = false;
    private static boolean sReturnedVideosEmpty = false;
    private static boolean sReturnedAudioEmpty = false;

    private static String joinNewline(String... args) {
        return TextUtils.join("\n", args);
    }

    private void copyNotificationUri(MatrixCursor result, Uri uri) {
        result.setNotificationUri(getContext().getContentResolver(), uri);//cursor.getNotificationUri());
    }

    public static void notifyRootsChanged(Context context) {
        context.getContentResolver()
                .notifyChange(DocumentsContract.buildRootsUri(AUTHORITY), null, false);
    }

    /**
     * When inserting the first item of each type, we need to trigger a roots
     * refresh to clear a previously reported {@link Root#FLAG_EMPTY}.
     */
    static void onMediaStoreInsert(Context context, String volumeName, int type, long id) {
        if (!"external".equals(volumeName)) return;

        if (type == FileColumns.MEDIA_TYPE_IMAGE && sReturnedImagesEmpty) {
            sReturnedImagesEmpty = false;
            notifyRootsChanged(context);
        } else if (type == FileColumns.MEDIA_TYPE_VIDEO && sReturnedVideosEmpty) {
            sReturnedVideosEmpty = false;
            notifyRootsChanged(context);
        } else if (type == FileColumns.MEDIA_TYPE_AUDIO && sReturnedAudioEmpty) {
            sReturnedAudioEmpty = false;
            notifyRootsChanged(context);
        }
    }

    /**
     * When deleting an item, we need to revoke any outstanding Uri grants.
     */
    static void onMediaStoreDelete(Context context, String volumeName, int type, long id) {
        if (!"external".equals(volumeName)) return;

        if (type == FileColumns.MEDIA_TYPE_IMAGE) {
            final Uri uri = DocumentsContract.buildDocumentUri(
                    AUTHORITY, getDocIdForIdent(TYPE_IMAGE, id));
            context.revokeUriPermission(uri, ~0);
        } else if (type == FileColumns.MEDIA_TYPE_VIDEO) {
            final Uri uri = DocumentsContract.buildDocumentUri(
                    AUTHORITY, getDocIdForIdent(TYPE_VIDEO, id));
            context.revokeUriPermission(uri, ~0);
        } else if (type == FileColumns.MEDIA_TYPE_AUDIO) {
            final Uri uri = DocumentsContract.buildDocumentUri(
                    AUTHORITY, getDocIdForIdent(TYPE_AUDIO, id));
            context.revokeUriPermission(uri, ~0);
        }
    }

    @Override
    public void onStorageVolumeChanged(List<Volume> newVolumes) {

    }

    @Override
    public boolean onCreate() {
        return true;
    }

    private static class Ident {
        public String type;
        public long id;
    }

    private static Ident getIdentForDocId(String docId) {
        final Ident ident = new Ident();
        final int split = docId.indexOf(':');
        if (split == -1) {
            ident.type = docId;
            ident.id = -1;
        } else {
            ident.type = docId.substring(0, split);
            ident.id = Long.parseLong(docId.substring(split + 1));
        }
        return ident;
    }

    public static Uri getMediaUriForDocumentId(String docId) {
        final Ident ident = getIdentForDocId(docId);
        if (TYPE_IMAGE.equals(ident.type) && ident.id != -1) {
            return ContentUris.withAppendedId(
                    Images.Media.EXTERNAL_CONTENT_URI, ident.id);
        } else if (TYPE_VIDEO.equals(ident.type) && ident.id != -1) {
            return ContentUris.withAppendedId(
                    Video.Media.EXTERNAL_CONTENT_URI, ident.id);
        } else if (TYPE_AUDIO.equals(ident.type) && ident.id != -1) {
            return ContentUris.withAppendedId(
                    Audio.Media.EXTERNAL_CONTENT_URI, ident.id);
        }
        return null;
    }

    private static String getDocIdForIdent(String type, long id) {
        return type + ":" + id;
    }

    private static String[] resolveRootProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_ROOT_PROJECTION;
    }

    private static String[] resolveDocumentProjection(String[] projection) {
        return projection != null ? projection : DEFAULT_DOCUMENT_PROJECTION;
    }


    private void includeDocumentsRoot(MatrixCursor result) {
        int flags = Root.FLAG_LOCAL_ONLY;
        if (isEmpty(DOCUMENTS_URI)) {
            flags |= Root.FLAG_EMPTY;
            sReturnedAudioEmpty = true;
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, TYPE_DOCUMENTS_ROOT);
        row.add(Root.COLUMN_FLAGS, flags);
        row.add(Root.COLUMN_TITLE, getContext().getString(R.string.root_documents));
        row.add(Root.COLUMN_DOCUMENT_ID, TYPE_DOCUMENTS_ROOT);
        row.add(Root.COLUMN_MIME_TYPES, DOCUMENT_MIME_TYPES);
    }

    private void includeDocumentsRootDocument(MatrixCursor result) {
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, TYPE_DOCUMENTS_ROOT);
        row.add(Document.COLUMN_DISPLAY_NAME, getContext().getString(R.string.root_documents));
        int flags = Document.FLAG_DIR_PREFERS_LAST_MODIFIED | Document.FLAG_SUPPORTS_DELETE;
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
    }
    private void includeFileRootDocument(MatrixCursor result, String root_type, int name_id) {
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, root_type);
        row.add(Document.COLUMN_DISPLAY_NAME, getContext().getString(name_id));
        int flags = Document.FLAG_DIR_PREFERS_LAST_MODIFIED | Document.FLAG_SUPPORTS_DELETE;
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
    }
    private void includeFileRoot(MatrixCursor result, String root_type, int name_id,
                                 String mime_types, boolean supports_recent) {
        int flags = Root.FLAG_LOCAL_ONLY;
        if (isEmpty(DOCUMENTS_URI, root_type)) {
            flags |= Root.FLAG_EMPTY;
        }

        if(supports_recent){
            flags |= Root.FLAG_SUPPORTS_RECENTS;
        }
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, root_type);
        row.add(Root.COLUMN_FLAGS, flags);
        row.add(Root.COLUMN_TITLE, getContext().getString(name_id));
        row.add(Root.COLUMN_DOCUMENT_ID, root_type);
        row.add(Root.COLUMN_MIME_TYPES, mime_types);
    }

    private interface FileQuery {
        String[] PROJECTION = new String[] {
                FileColumns._ID,
                FileColumns.TITLE,
                FileColumns.MIME_TYPE,
                FileColumns.SIZE,
                FileColumns.DATA,
                FileColumns.DATE_MODIFIED };

        int _ID = 0;
        int TITLE = 1;
        int MIME_TYPE = 2;
        int SIZE = 3;
        int DATA = 4;
        int DATE_MODIFIED = 5;
    }

    private void queryLikeFile(ContentResolver resolver, Cursor cursor, MatrixCursor result, String[] mimeType, String like) {
        // single file
        cursor = resolver.query(DOCUMENTS_URI,
                FileQuery.PROJECTION,
                FileColumns.MIME_TYPE + " IN "+ "("+toString(mimeType)+")"
                        + " OR " + FileColumns.MIME_TYPE + " LIKE "+ "'"+like+"%'",
                null,
                null);
        copyNotificationUri(result, DOCUMENTS_URI);
        while (cursor.moveToNext()) {
            includeFile(result, cursor, TYPE_DOCUMENTS);
        }
    }

    private void includeFile(MatrixCursor result, Cursor cursor, String type) {
        final long id = cursor.getLong(FileQuery._ID);
        final String docId = getDocIdForIdent(type, id);

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, cursor.getString(FileQuery.TITLE));
        row.add(Document.COLUMN_SIZE, cursor.getLong(FileQuery.SIZE));
        row.add(Document.COLUMN_MIME_TYPE, cursor.getString(FileQuery.MIME_TYPE));
        row.add(Document.COLUMN_PATH, cursor.getString(FileQuery.DATA));
        row.add(Document.COLUMN_LAST_MODIFIED,
                cursor.getLong(FileQuery.DATE_MODIFIED) * DateUtils.SECOND_IN_MILLIS);
        row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_THUMBNAIL | Document.FLAG_SUPPORTS_DELETE);
    }

    @Override
    public Cursor queryRoots(String[] projection) throws FileNotFoundException {
        final MatrixCursor result = new MatrixCursor(resolveRootProjection(projection));
        includeImagesRoot(result);
        includeVideosRoot(result);
        includeAudioRoot(result);
        includeFileRoot(result,TYPE_DOCUMENTS_ROOT, R.string.root_documents, DOCUMENT_MIME_TYPES, true);
        return result;
    }

    @Override
    public Cursor queryDocument(String docId, String[] projection) throws FileNotFoundException {
        final ContentResolver resolver = getContext().getContentResolver();
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        final Ident ident = getIdentForDocId(docId);

        final long token = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            if (TYPE_DOCUMENTS_ROOT.equals(ident.type)) {
                includeFileRootDocument(result, TYPE_DOCUMENTS_ROOT, R.string.root_documents);
            } else if (TYPE_DOCUMENTS.equals(ident.type)) {
                queryLikeFile(resolver, cursor, result, DOCUMENT_MIMES, "text");
            } else if (TYPE_IMAGES_ROOT.equals(ident.type)) {
                // single root
                includeImagesRootDocument(result);
            } else if (TYPE_IMAGES_BUCKET.equals(ident.type)) {
                // single bucket
                cursor = resolver.query(Images.Media.EXTERNAL_CONTENT_URI,
                        ImagesBucketQuery.PROJECTION, ImageColumns.BUCKET_ID + "=" + ident.id,
                        null, ImagesBucketQuery.SORT_ORDER);
                copyNotificationUri(result, Images.Media.EXTERNAL_CONTENT_URI);
                if (cursor.moveToFirst()) {
                    includeImagesBucket(result, cursor);
                }
            } else if (TYPE_IMAGE.equals(ident.type)) {
                // single image
                cursor = resolver.query(Images.Media.EXTERNAL_CONTENT_URI,
                        ImageQuery.PROJECTION, BaseColumns._ID + "=" + ident.id, null,
                        null);
                copyNotificationUri(result, Images.Media.EXTERNAL_CONTENT_URI);
                if (cursor.moveToFirst()) {
                    includeImage(result, cursor);
                }
            } else if (TYPE_VIDEOS_ROOT.equals(ident.type)) {
                // single root
                includeVideosRootDocument(result);
            } else if (TYPE_VIDEOS_BUCKET.equals(ident.type)) {
                // single bucket
                cursor = resolver.query(Video.Media.EXTERNAL_CONTENT_URI,
                        VideosBucketQuery.PROJECTION, VideoColumns.BUCKET_ID + "=" + ident.id,
                        null, VideosBucketQuery.SORT_ORDER);
                copyNotificationUri(result, Images.Media.EXTERNAL_CONTENT_URI);
                if (cursor.moveToFirst()) {
                    includeVideosBucket(result, cursor);
                }
            } else if (TYPE_VIDEO.equals(ident.type)) {
                // single video
                cursor = resolver.query(Video.Media.EXTERNAL_CONTENT_URI,
                        VideoQuery.PROJECTION, BaseColumns._ID + "=" + ident.id, null,
                        null);
                copyNotificationUri(result, Video.Media.EXTERNAL_CONTENT_URI);
                if (cursor.moveToFirst()) {
                    includeVideo(result, cursor);
                }
            } else if (TYPE_AUDIO_ROOT.equals(ident.type)) {
                // single root
                includeAudioRootDocument(result);
            } else if (TYPE_ARTIST.equals(ident.type)) {
                // single artist
                cursor = resolver.query(Artists.EXTERNAL_CONTENT_URI,
                        ArtistQuery.PROJECTION, BaseColumns._ID + "=" + ident.id, null,
                        null);
                copyNotificationUri(result, Artists.EXTERNAL_CONTENT_URI);
                if (cursor.moveToFirst()) {
                    includeArtist(result, cursor);
                }
            } else if (TYPE_ALBUM.equals(ident.type)) {
                // single album
                cursor = resolver.query(Albums.EXTERNAL_CONTENT_URI,
                        AlbumQuery.PROJECTION, BaseColumns._ID + "=" + ident.id, null,
                        null);
                copyNotificationUri(result, Albums.EXTERNAL_CONTENT_URI);
                if (cursor.moveToFirst()) {
                    includeAlbum(result, cursor);
                }
            } else if (TYPE_AUDIO.equals(ident.type)) {
                // single song
                cursor = resolver.query(Audio.Media.EXTERNAL_CONTENT_URI,
                        SongQuery.PROJECTION, BaseColumns._ID + "=" + ident.id, null,
                        null);
                copyNotificationUri(result, Audio.Media.EXTERNAL_CONTENT_URI);
                if (cursor.moveToFirst()) {
                    includeAudio(result, cursor);
                }
            } else {
                throw new UnsupportedOperationException("Unsupported document " + docId);
            }
        } finally {
            IoUtils.closeQuietly(cursor);
            Binder.restoreCallingIdentity(token);
        }
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String docId, String[] projection, String sortOrder)
            throws FileNotFoundException {
        final ContentResolver resolver = getContext().getContentResolver();
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));
        final Ident ident = getIdentForDocId(docId);

        final long token = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            if (TYPE_DOCUMENTS_ROOT.equals(ident.type)) {
                queryLikeFile(resolver, cursor, result, DOCUMENT_MIMES, "text");
            } else if (TYPE_IMAGES_ROOT.equals(ident.type)) {
                // include all unique buckets
                cursor = resolver.query(Images.Media.EXTERNAL_CONTENT_URI,
                        ImagesBucketQuery.PROJECTION, " 1=1) group by (" + MediaStore.Images.Media.BUCKET_DISPLAY_NAME, null, ImagesBucketQuery.SORT_ORDER);
                // multiple orders
                copyNotificationUri(result, Images.Media.EXTERNAL_CONTENT_URI);
                long lastId = Long.MIN_VALUE;
                while (cursor.moveToNext()) {
                    final long id = cursor.getLong(ImagesBucketQuery.BUCKET_ID);
                    if (lastId != id) {
                        includeImagesBucket(result, cursor);
                        lastId = id;
                    }
                }
            } else if (TYPE_IMAGES_BUCKET.equals(ident.type)) {
                // include images under bucket
                cursor = resolver.query(Images.Media.EXTERNAL_CONTENT_URI,
                        ImageQuery.PROJECTION, ImageColumns.BUCKET_ID + "=" + ident.id,
                        null, null);
                copyNotificationUri(result, Images.Media.EXTERNAL_CONTENT_URI);
                while (cursor.moveToNext()) {
                    includeImage(result, cursor);
                }
            } else if (TYPE_VIDEOS_ROOT.equals(ident.type)) {
                // include all unique buckets
                cursor = resolver.query(Video.Media.EXTERNAL_CONTENT_URI,
                        VideosBucketQuery.PROJECTION, null, null, VideosBucketQuery.SORT_ORDER);
                copyNotificationUri(result, Video.Media.EXTERNAL_CONTENT_URI);
                long lastId = Long.MIN_VALUE;
                while (cursor.moveToNext()) {
                    final long id = cursor.getLong(VideosBucketQuery.BUCKET_ID);
                    if (lastId != id) {
                        includeVideosBucket(result, cursor);
                        lastId = id;
                    }
                }
            } else if (TYPE_VIDEOS_BUCKET.equals(ident.type)) {
                // include videos under bucket
                cursor = resolver.query(Video.Media.EXTERNAL_CONTENT_URI,
                        VideoQuery.PROJECTION, VideoColumns.BUCKET_ID + "=" + ident.id,
                        null, null);
                copyNotificationUri(result, Video.Media.EXTERNAL_CONTENT_URI);
                while (cursor.moveToNext()) {
                    includeVideo(result, cursor);
                }
            } else if (TYPE_AUDIO_ROOT.equals(ident.type)) {
/*                // include all artists
                cursor = resolver.query(Audio.Artists.EXTERNAL_CONTENT_URI,
                        ArtistQuery.PROJECTION, null, null, null);
                copyNotificationUri(result, Artists.EXTERNAL_CONTENT_URI);
                while (cursor.moveToNext()) {
                    includeArtist(result, cursor);
                }*/
                // include all albums under artist
                cursor = resolver.query(Albums.EXTERNAL_CONTENT_URI,
                        AlbumQuery.PROJECTION, null, null, null);
                copyNotificationUri(result, Albums.EXTERNAL_CONTENT_URI);
                while (cursor.moveToNext()) {
                    includeAlbum(result, cursor);
                }
            } else if (TYPE_ARTIST.equals(ident.type)) {
                // include all albums under artist
                cursor = resolver.query(Artists.Albums.getContentUri("external", ident.id),
                        AlbumQuery.PROJECTION, null, null, null);
                copyNotificationUri(result, Artists.Albums.getContentUri("external", ident.id));
                while (cursor.moveToNext()) {
                    includeAlbum(result, cursor);
                }
            } else if (TYPE_ALBUM.equals(ident.type)) {
                // include all songs under album
                cursor = resolver.query(Audio.Media.EXTERNAL_CONTENT_URI,
                        SongQuery.PROJECTION, AudioColumns.ALBUM_ID + "=" + ident.id,
                        null, null);
                copyNotificationUri(result, Audio.Media.EXTERNAL_CONTENT_URI);
                while (cursor.moveToNext()) {
                    includeAudio(result, cursor);
                }
            } else {
                throw new UnsupportedOperationException("Unsupported document " + docId);
            }
        } finally {
            IoUtils.closeQuietly(cursor);
            Binder.restoreCallingIdentity(token);
        }
        return result;
    }

    @Override
    public Cursor queryChildDocuments(String parentDocumentId, @Nullable String[] projection, @Nullable Bundle queryArgs) throws FileNotFoundException {
        return null;
    }

    @Override
    public Cursor queryRecentDocuments(String rootId, String[] projection)
            throws FileNotFoundException {
        final ContentResolver resolver = getContext().getContentResolver();
        final MatrixCursor result = new MatrixCursor(resolveDocumentProjection(projection));

        final long token = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            if (TYPE_IMAGES_ROOT.equals(rootId)) {
                // include all unique buckets
                cursor = resolver.query(Images.Media.EXTERNAL_CONTENT_URI,
                        ImageQuery.PROJECTION, null, null, ImageColumns.DATE_MODIFIED + " DESC");
                copyNotificationUri(result, Images.Media.EXTERNAL_CONTENT_URI);
                while (cursor.moveToNext() && result.getCount() < 64) {
                    includeImage(result, cursor);
                }
            } else if (TYPE_VIDEOS_ROOT.equals(rootId)) {
                // include all unique buckets
                cursor = resolver.query(Video.Media.EXTERNAL_CONTENT_URI,
                        VideoQuery.PROJECTION, null, null, VideoColumns.DATE_MODIFIED + " DESC");
                copyNotificationUri(result, Video.Media.EXTERNAL_CONTENT_URI);
                while (cursor.moveToNext() && result.getCount() < 64) {
                    includeVideo(result, cursor);
                }
            } else {
                throw new UnsupportedOperationException("Unsupported root " + rootId);
            }
        } finally {
            IoUtils.closeQuietly(cursor);
            Binder.restoreCallingIdentity(token);
        }
        return result;
    }

    @Override
    public ParcelFileDescriptor openDocument(String docId, String mode, CancellationSignal signal)
            throws FileNotFoundException {

        if (!"r".equals(mode)) {
            throw new IllegalArgumentException("Media is read-only");
        }

        final Uri target = getUriForDocumentId(docId);

        // Delegate to real provider
        final long token = Binder.clearCallingIdentity();
        try {
            return getContext().getContentResolver().openFileDescriptor(target, mode);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    private Uri getUriForDocumentId(String docId) {
        final Ident ident = getIdentForDocId(docId);
        if (TYPE_IMAGE.equals(ident.type) && ident.id != -1) {
            return ContentUris.withAppendedId(
                    Images.Media.EXTERNAL_CONTENT_URI, ident.id);
        } else if (TYPE_VIDEO.equals(ident.type) && ident.id != -1) {
            return ContentUris.withAppendedId(
                    Video.Media.EXTERNAL_CONTENT_URI, ident.id);
        } else if (TYPE_AUDIO.equals(ident.type) && ident.id != -1) {
            return ContentUris.withAppendedId(
                    Audio.Media.EXTERNAL_CONTENT_URI, ident.id);
        } else {
            throw new UnsupportedOperationException("Unsupported document " + docId);
        }
    }

    @Override
    public String renameDocument(String documentId, String displayName) throws FileNotFoundException {
        return super.renameDocument(documentId, displayName);
    }

    @Override
    public void deleteDocument(String docId) throws FileNotFoundException {
        final Ident ident = getIdentForDocId(docId);

        final Uri target = getUriForDocumentId(docId);

        // Delegate to real provider
        final long token = Binder.clearCallingIdentity();
        try {
            getContext().getContentResolver().delete(target, null, null);
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }

    protected interface ImagesBucketThumbnailQuery {
        String[] PROJECTION = new String[]{
                ImageColumns._ID,
                ImageColumns.BUCKET_ID,
                ImageColumns.DATE_MODIFIED};

        int _ID = 0;
        int BUCKET_ID = 1;
        int DATE_MODIFIED = 2;
    }


    protected long getImageForBucketCleared(long bucketId) throws FileNotFoundException {
        final ContentResolver resolver = getContext().getContentResolver();
        Cursor cursor = null;
        try {
            cursor = resolver.query(Images.Media.EXTERNAL_CONTENT_URI,
                    ImagesBucketThumbnailQuery.PROJECTION, ImageColumns.BUCKET_ID + "=" + bucketId,
                    null, ImageColumns.DATE_MODIFIED + " DESC");
            if (cursor.moveToFirst()) {
                return cursor.getLong(ImagesBucketThumbnailQuery._ID);
            }
        } finally {
            IoUtils.closeQuietly(cursor);
        }
        throw new FileNotFoundException("No video found for bucket");
    }

    /*@Override
    public AssetFileDescriptor openDocumentThumbnail(
            String docId, Point sizeHint, CancellationSignal signal) throws FileNotFoundException {
//        final ContentResolver resolver = getContext().getContentResolver();
        final Ident ident = getIdentForDocId(docId);

        final long token = Binder.clearCallingIdentity();
        try {
            if (TYPE_IMAGES_BUCKET.equals(ident.type)) {
                final long id = getImageForBucketCleared(ident.id);
                return openOrCreateImageThumbnailCleared(id, signal);
            } else if (TYPE_IMAGE.equals(ident.type)) {
                return openOrCreateImageThumbnailCleared(ident.id, signal);
            } else if (TYPE_VIDEOS_BUCKET.equals(ident.type)) {
                final long id = getVideoForBucketCleared(ident.id);
                return openOrCreateVideoThumbnailCleared(id, signal);
            } else if (TYPE_VIDEO.equals(ident.type)) {
                return openOrCreateVideoThumbnailCleared(ident.id, signal);
            } else if (TYPE_ALBUM.equals(ident.type)) {
                return openOrCreateAudioThumbnailCleared(ident.id, signal);
            } else if (TYPE_AUDIO.equals(ident.type)) {
                final long id = getAlbumForAudioCleared(ident.id);
                return openOrCreateAudioThumbnailCleared(id, signal);
            } else {
                throw new UnsupportedOperationException("Unsupported document " + docId);
            }
        } finally {
            Binder.restoreCallingIdentity(token);
        }
    }*/

    private boolean isEmpty(Uri uri) {
        final ContentResolver resolver = getContext().getContentResolver();
        final long token = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            cursor = resolver.query(uri, new String[]{
                    BaseColumns._ID}, null, null, null);
            return (cursor == null) || (cursor.getCount() == 0);
        } finally {
            IoUtils.closeQuietly(cursor);
            Binder.restoreCallingIdentity(token);
        }
    }
    private boolean isEmpty(Uri uri, String type) {
        final ContentResolver resolver = getContext().getContentResolver();
        final long token = Binder.clearCallingIdentity();
        Cursor cursor = null;
        try {
            String[] mimeType;
            if (TYPE_DOCUMENTS_ROOT.equals(type)) {
                mimeType = DOCUMENT_MIMES;
            } else {
                return true;
            }
            cursor = resolver.query(DOCUMENTS_URI,
                    FileQuery.PROJECTION,
                    FileColumns.MIME_TYPE + " IN "+ "("+toString(mimeType)+")" , null, null);
            return (cursor == null) || (cursor.getCount() == 0);
        } finally {
            IoUtils.closeQuietly(cursor);
            Binder.restoreCallingIdentity(token);
        }
    }

    private void includeImagesRoot(MatrixCursor result) {
        int flags = Root.FLAG_LOCAL_ONLY | Root.FLAG_SUPPORTS_RECENTS;
        if (isEmpty(Images.Media.EXTERNAL_CONTENT_URI)) {
            flags |= Root.FLAG_EMPTY;
            sReturnedImagesEmpty = true;
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, TYPE_IMAGES_ROOT);
        row.add(Root.COLUMN_FLAGS, flags);
        row.add(Root.COLUMN_TITLE, getContext().getString(R.string.root_image));
        row.add(Root.COLUMN_DOCUMENT_ID, TYPE_IMAGES_ROOT);
        row.add(Root.COLUMN_MIME_TYPES, IMAGE_MIME_TYPES);
    }

    private void includeVideosRoot(MatrixCursor result) {
        int flags = Root.FLAG_LOCAL_ONLY | Root.FLAG_SUPPORTS_RECENTS;
        if (isEmpty(Video.Media.EXTERNAL_CONTENT_URI)) {
            flags |= Root.FLAG_EMPTY;
            sReturnedVideosEmpty = true;
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, TYPE_VIDEOS_ROOT);
        row.add(Root.COLUMN_FLAGS, flags);
        row.add(Root.COLUMN_TITLE, getContext().getString(R.string.root_video));
        row.add(Root.COLUMN_DOCUMENT_ID, TYPE_VIDEOS_ROOT);
        row.add(Root.COLUMN_MIME_TYPES, VIDEO_MIME_TYPES);
    }

    private void includeAudioRoot(MatrixCursor result) {
        int flags = Root.FLAG_LOCAL_ONLY;
        if (isEmpty(Audio.Media.EXTERNAL_CONTENT_URI)) {
            flags |= Root.FLAG_EMPTY;
            sReturnedAudioEmpty = true;
        }

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Root.COLUMN_ROOT_ID, TYPE_AUDIO_ROOT);
        row.add(Root.COLUMN_FLAGS, flags);
        row.add(Root.COLUMN_TITLE, getContext().getString(R.string.root_audio));
        row.add(Root.COLUMN_DOCUMENT_ID, TYPE_AUDIO_ROOT);
        row.add(Root.COLUMN_MIME_TYPES, AUDIO_MIME_TYPES);
    }

    private void includeImagesRootDocument(MatrixCursor result) {
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, TYPE_IMAGES_ROOT);
        row.add(Document.COLUMN_DISPLAY_NAME, getContext().getString(R.string.root_image));
        int flags = Document.FLAG_DIR_PREFERS_LAST_MODIFIED | Document.FLAG_SUPPORTS_DELETE;
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
    }

    private void includeVideosRootDocument(MatrixCursor result) {
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, TYPE_VIDEOS_ROOT);
        row.add(Document.COLUMN_DISPLAY_NAME, getContext().getString(R.string.root_video));

        int flags = Document.FLAG_DIR_PREFERS_LAST_MODIFIED | Document.FLAG_SUPPORTS_DELETE;
        row.add(Document.COLUMN_FLAGS, flags);
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
    }

    private void includeAudioRootDocument(MatrixCursor result) {
        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, TYPE_AUDIO_ROOT);
        row.add(Document.COLUMN_DISPLAY_NAME, getContext().getString(R.string.root_audio));
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);

        int flags = Document.FLAG_DIR_PREFERS_LAST_MODIFIED | Document.FLAG_SUPPORTS_DELETE;
        row.add(Document.COLUMN_FLAGS, flags);
    }

    public static String[] PROJECTION_BUCKET = new String[]{
            MediaStore.MediaColumns._ID,
            MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
            MediaStore.Images.Media.DATA,
            MediaStore.Images.Media.BUCKET_ID,
            MediaStore.Images.Media.DATE_MODIFIED,
            "count(" + MediaStore.Images.Media._ID + ")"//统计当前文件夹下共有多少张图片
    };


    private interface ImagesBucketQuery {
        String[] PROJECTION = new String[]{
                ImageColumns.BUCKET_ID,
                ImageColumns.BUCKET_DISPLAY_NAME,
                ImageColumns.DATE_MODIFIED,
                MediaStore.Images.Media.DATA,
                "count(" + MediaStore.Images.Media._ID + ")"//统计当前文件夹下共有多少张图片
        };
        String SORT_ORDER = /*ImageColumns.BUCKET_ID + ", " + */ImageColumns.DATE_MODIFIED
                + " DESC";

        int BUCKET_ID = 0;
        int BUCKET_DISPLAY_NAME = 1;
        int DATE_MODIFIED = 2;
        int BUCKET_DISPLAY_ICON = 3;
        int BUCKET_count = 4;
    }

    private void includeImagesBucket(MatrixCursor result, Cursor cursor) {
        final long id = cursor.getLong(ImagesBucketQuery.BUCKET_ID);
        final String docId = getDocIdForIdent(TYPE_IMAGES_BUCKET, id);

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
        row.add(Document.COLUMN_LAST_MODIFIED,
                cursor.getLong(ImagesBucketQuery.DATE_MODIFIED) * DateUtils.SECOND_IN_MILLIS);
        row.add(Document.COLUMN_ICON, cursor.getString(ImagesBucketQuery.BUCKET_DISPLAY_ICON));
        row.add(Document.COLUMN_SUMMARY, getContext().getResources().getString(R.string.directory_counts, String.valueOf(cursor.getInt(ImagesBucketQuery.BUCKET_count))));
        row.add(Document.COLUMN_DISPLAY_NAME,
                cursor.getString(ImagesBucketQuery.BUCKET_DISPLAY_NAME));
        int flags = Document.FLAG_SUPPORTS_THUMBNAIL | Document.FLAG_DIR_PREFERS_LAST_MODIFIED
                | Document.FLAG_SUPPORTS_DELETE;
        row.add(Document.COLUMN_FLAGS, flags);
    }

    private interface ImageQuery {
        String[] PROJECTION = new String[]{
                ImageColumns._ID,
                ImageColumns.DISPLAY_NAME,
                ImageColumns.MIME_TYPE,
                ImageColumns.SIZE,
                ImageColumns.DATA,
                ImageColumns.DATE_MODIFIED};

        int _ID = 0;
        int DISPLAY_NAME = 1;
        int MIME_TYPE = 2;
        int SIZE = 3;
        int DATA = 4;
        int DATE_MODIFIED = 5;
    }

    private void includeImage(MatrixCursor result, Cursor cursor) {
        final long id = cursor.getLong(ImageQuery._ID);
        final String docId = getDocIdForIdent(TYPE_IMAGE, id);

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, cursor.getString(ImageQuery.DISPLAY_NAME));
        row.add(Document.COLUMN_SIZE, cursor.getLong(ImageQuery.SIZE));
        row.add(Document.COLUMN_MIME_TYPE, cursor.getString(ImageQuery.MIME_TYPE));
        row.add(Document.COLUMN_PATH, cursor.getString(ImageQuery.DATA));
        row.add(Document.COLUMN_LAST_MODIFIED,
                cursor.getLong(ImageQuery.DATE_MODIFIED) * DateUtils.SECOND_IN_MILLIS);
        row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_THUMBNAIL | Document.FLAG_SUPPORTS_DELETE);
    }

    private interface VideosBucketQuery {
        String[] PROJECTION = new String[]{
                VideoColumns.BUCKET_ID,
                VideoColumns.BUCKET_DISPLAY_NAME,
                VideoColumns.DATE_MODIFIED};
        String SORT_ORDER = VideoColumns.BUCKET_ID + ", " + VideoColumns.DATE_MODIFIED
                + " DESC";

        int BUCKET_ID = 0;
        int BUCKET_DISPLAY_NAME = 1;
        int DATE_MODIFIED = 2;
    }

    private void includeVideosBucket(MatrixCursor result, Cursor cursor) {
        final long id = cursor.getLong(VideosBucketQuery.BUCKET_ID);
        final String docId = getDocIdForIdent(TYPE_VIDEOS_BUCKET, id);

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME,
                cursor.getString(VideosBucketQuery.BUCKET_DISPLAY_NAME));
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
        row.add(Document.COLUMN_LAST_MODIFIED,
                cursor.getLong(VideosBucketQuery.DATE_MODIFIED) * DateUtils.SECOND_IN_MILLIS);

        int flags = Document.FLAG_SUPPORTS_THUMBNAIL | Document.FLAG_DIR_PREFERS_LAST_MODIFIED
                | Document.FLAG_SUPPORTS_DELETE;
        row.add(Document.COLUMN_FLAGS, flags);
    }

    private interface VideoQuery {
        String[] PROJECTION = new String[]{
                VideoColumns._ID,
                VideoColumns.DISPLAY_NAME,
                VideoColumns.MIME_TYPE,
                VideoColumns.SIZE,
                VideoColumns.DATA,
                VideoColumns.DATE_MODIFIED};

        int _ID = 0;
        int DISPLAY_NAME = 1;
        int MIME_TYPE = 2;
        int SIZE = 3;
        int DATA = 4;
        int DATE_MODIFIED = 5;
    }

    private void includeVideo(MatrixCursor result, Cursor cursor) {
        final long id = cursor.getLong(VideoQuery._ID);
        final String docId = getDocIdForIdent(TYPE_VIDEO, id);

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, cursor.getString(VideoQuery.DISPLAY_NAME));
        row.add(Document.COLUMN_SIZE, cursor.getLong(VideoQuery.SIZE));
        row.add(Document.COLUMN_MIME_TYPE, cursor.getString(VideoQuery.MIME_TYPE));
        row.add(Document.COLUMN_PATH, cursor.getString(VideoQuery.DATA));
        row.add(Document.COLUMN_LAST_MODIFIED,
                cursor.getLong(VideoQuery.DATE_MODIFIED) * DateUtils.SECOND_IN_MILLIS);
        row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_THUMBNAIL | Document.FLAG_SUPPORTS_DELETE);
    }

    private interface ArtistQuery {
        String[] PROJECTION = new String[]{
                BaseColumns._ID,
                ArtistColumns.ARTIST};

        int _ID = 0;
        int ARTIST = 1;
    }

    private void includeArtist(MatrixCursor result, Cursor cursor) {
        final long id = cursor.getLong(ArtistQuery._ID);
        final String docId = getDocIdForIdent(TYPE_ARTIST, id);

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, cleanUpMediaDisplayName(cursor.getString(ArtistQuery.ARTIST)));
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
    }

    private interface AlbumQuery {
        String[] PROJECTION = new String[]{
                BaseColumns._ID,
                AlbumColumns.ALBUM};

        int _ID = 0;
        int ALBUM = 1;
    }

    private void includeAlbum(MatrixCursor result, Cursor cursor) {
        final long id = cursor.getLong(AlbumQuery._ID);
        final String docId = getDocIdForIdent(TYPE_ALBUM, id);

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME,
                cleanUpMediaDisplayName(cursor.getString(AlbumQuery.ALBUM)));
        row.add(Document.COLUMN_MIME_TYPE, Document.MIME_TYPE_DIR);
        int flags = Document.FLAG_SUPPORTS_THUMBNAIL | Document.FLAG_DIR_PREFERS_LAST_MODIFIED | Document.FLAG_SUPPORTS_DELETE;
        row.add(Document.COLUMN_FLAGS, flags);
    }

    private interface SongQuery {
        String[] PROJECTION = new String[]{
                AudioColumns._ID,
                AudioColumns.TITLE,
                AudioColumns.MIME_TYPE,
                AudioColumns.SIZE,
                AudioColumns.DATA,
                AudioColumns.DATE_MODIFIED};

        int _ID = 0;
        int TITLE = 1;
        int MIME_TYPE = 2;
        int SIZE = 3;
        int DATA = 4;
        int DATE_MODIFIED = 5;
    }

    private void includeAudio(MatrixCursor result, Cursor cursor) {
        final long id = cursor.getLong(SongQuery._ID);
        final String docId = getDocIdForIdent(TYPE_AUDIO, id);

        final MatrixCursor.RowBuilder row = result.newRow();
        row.add(Document.COLUMN_DOCUMENT_ID, docId);
        row.add(Document.COLUMN_DISPLAY_NAME, cursor.getString(SongQuery.TITLE));
        row.add(Document.COLUMN_SIZE, cursor.getLong(SongQuery.SIZE));
        String mimeType = cursor.getString(SongQuery.MIME_TYPE);
        if (!mimeType.startsWith("audio")) {
            mimeType = "audio/" + mimeType;
        }
        row.add(Document.COLUMN_MIME_TYPE, mimeType);
        row.add(Document.COLUMN_PATH, cursor.getString(SongQuery.DATA));
        row.add(Document.COLUMN_LAST_MODIFIED,
                cursor.getLong(SongQuery.DATE_MODIFIED) * DateUtils.SECOND_IN_MILLIS);
        row.add(Document.COLUMN_FLAGS, Document.FLAG_SUPPORTS_THUMBNAIL | Document.FLAG_SUPPORTS_DELETE);
    }

    private String cleanUpMediaDisplayName(String displayName) {
        if (!MediaStore.UNKNOWN_STRING.equals(displayName)) {
            return displayName;
        }
        return displayName;//getContext().getResources().getString(com.android.internal.R.string.unknownName);
    }
    public static String toString(String[] list) {
        if (list == null) {
            return "";
        }
        if (list.length == 0) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("'"+list[0]+"'");
        for (int i = 1; i < list.length; i++) {
            sb.append(", ");
            sb.append("'"+list[i]+"'");
        }
        return sb.toString();
    }
}
