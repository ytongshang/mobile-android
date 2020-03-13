package cradle.rancune.internal.core.utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.DocumentsContract;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.File;

/**
 * Created by Rancune@126.com on 2016/10/19.
 * <p>
 * Uri的基本格式：
 * [scheme:]scheme-specific-part[#fragment]
 * [scheme:][//authority][path][?query][#fragment]
 * [scheme:][//host:port][path][?query][#fragment]
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public final class UriUtils {

    /**
     * http scheme for URIs
     */
    public static final String HTTP_SCHEME = "http";
    public static final String HTTPS_SCHEME = "https";

    /**
     * Directory scheme for URIs
     */
    public static final String LOCAL_FILE_SCHEME = "file";

    /**
     * Content URI scheme for URIs
     */
    public static final String LOCAL_CONTENT_SCHEME = "content";

    /**
     * Asset scheme for URIs
     */
    public static final String LOCAL_ASSET_SCHEME = "asset";

    /**
     * Resource scheme for URIs
     */
    public static final String LOCAL_RESOURCE_SCHEME = "res";

    /**
     * Resource scheme for fully qualified resources which might have a package name that is different
     * than the application one. This has the constant value of "android.resource".
     */
    public static final String QUALIFIED_RESOURCE_SCHEME = ContentResolver.SCHEME_ANDROID_RESOURCE;

    /**
     * Data scheme for URIs
     */
    private static final String DATA_SCHEME = "data";


    private UriUtils() {
    }

    /**
     * A wrapper around {@link Uri#parse} that returns null if the input is null.
     *
     * @param uriAsString the uri as a string
     * @return the parsed Uri or null if the input was null
     */
    @Nullable
    public static Uri parseUriOrNull(@Nullable String uriAsString) {
        return uriAsString != null ? Uri.parse(uriAsString) : null;
    }

    /**
     * Returns a URI for a given file using {@link Uri#fromFile(File)}.
     *
     * @param file a file with a valid path
     * @return the URI
     */
    @Nullable
    public static Uri getUriForFile(File file) {
        return file == null ? null : Uri.fromFile(file);
    }

    private static final String FILE_URI_PREFIX = "file://";

    /**
     * Returns a file URI string for given file path.
     */
    @NonNull
    public static String getUriForLocalpath(String path) {
        if (path == null || path.isEmpty()) {
            return "";
        }
        if (!path.startsWith(FILE_URI_PREFIX)) {
            path = FILE_URI_PREFIX + path;
        }
        return path;
    }

    /**
     * Return a URI for the given resource ID.
     * The returned URI consists of a {@link #LOCAL_RESOURCE_SCHEME} scheme and
     * the resource ID as path.
     *
     * @param resourceId the resource ID to use
     * @return the URI
     */
    public static Uri getUriForResourceId(int resourceId) {
        return new Uri.Builder()
                .scheme(LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(resourceId))
                .build();
    }

    /**
     * Returns a URI for the given resource ID in the given package. Use this method only if you need
     * to specify a package name different to your application's main package.
     *
     * @param packageName a package name (e.g. com.facebook.myapp.plugin)
     * @param resourceId  to resource ID to use
     * @return the URI
     */
    public static Uri getUriForQualifiedResource(String packageName, int resourceId) {
        return new Uri.Builder()
                .scheme(QUALIFIED_RESOURCE_SCHEME)
                .authority(packageName)
                .path(String.valueOf(resourceId))
                .build();
    }

    /**
     * @param uri uri to extract scheme from, possibly null
     * @return null if uri is null, result of uri.getScheme() otherwise
     */
    @Nullable
    public static String getSchemeOrNull(@Nullable Uri uri) {
        return uri == null ? null : uri.getScheme();
    }

    /**
     * Check if uri represents network resource
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to "http" or "https"
     */
    public static boolean isNetworkUri(@Nullable Uri uri) {
        final String scheme = getSchemeOrNull(uri);
        return HTTPS_SCHEME.equals(scheme) || HTTP_SCHEME.equals(scheme);
    }

    /**
     * Check if uri represents local file
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to "file"
     */
    public static boolean isLocalFileUri(@Nullable Uri uri) {
        final String scheme = getSchemeOrNull(uri);
        return LOCAL_FILE_SCHEME.equals(scheme);
    }

    /**
     * Check if uri represents local content
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to "content"
     */
    public static boolean isLocalContentUri(@Nullable Uri uri) {
        final String scheme = getSchemeOrNull(uri);
        return LOCAL_CONTENT_SCHEME.equals(scheme);
    }

    /**
     * Checks if the given URI is for a photo from the device's local media store.
     *
     * @param uri the URI to check
     * @return true if the URI points to a media store photo
     */
    public static boolean isLocalCameraUri(Uri uri) {
        if (uri == null) {
            return false;
        }
        String uriString = uri.toString();
        return uriString.startsWith(MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString())
                || uriString.startsWith(MediaStore.Images.Media.INTERNAL_CONTENT_URI.toString());
    }

    /**
     * Check if uri represents local asset
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to "asset"
     */
    public static boolean isLocalAssetUri(@Nullable Uri uri) {
        final String scheme = getSchemeOrNull(uri);
        return LOCAL_ASSET_SCHEME.equals(scheme);
    }

    /**
     * Check if uri represents local resource
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to {@link #LOCAL_RESOURCE_SCHEME}
     */
    public static boolean isLocalResourceUri(@Nullable Uri uri) {
        final String scheme = getSchemeOrNull(uri);
        return LOCAL_RESOURCE_SCHEME.equals(scheme);
    }

    /**
     * Check if uri represents fully qualified resource URI.
     *
     * @param uri uri to check
     * @return true if uri's scheme is equal to {@link #QUALIFIED_RESOURCE_SCHEME}
     */
    public static boolean isQualifiedResourceUri(@Nullable Uri uri) {
        String scheme = getSchemeOrNull(uri);
        return "android.resource".equals(scheme);
    }

    /**
     * Check if the uri is a data uri
     */
    public static boolean isDataUri(@Nullable Uri uri) {
        return DATA_SCHEME.equals(getSchemeOrNull(uri));
    }

    /**
     * uri authority for externalstorage documents
     */
    private static final String EXTERNAL_DOCUMENTS = "com.android.externalstorage.documents";

    /**
     * uri authority for downloads documents
     */
    private static final String DOWNLOAD_DOCUMENTS = "com.android.providers.downloads.documents";

    /**
     * uri authority for media documents
     */
    private static final String MEDIA_DOCUMENTS = "com.android.providers.media.documents";

    /**
     * @param context context
     * @param uri     uri
     * @return the absolute path of the resources which this uri represents
     */
    @Nullable
    public static String getAbsolutePath(@NonNull Context context, Uri uri) {
        if (uri == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && DocumentsContract.isDocumentUri(context, uri)) {
            if (EXTERNAL_DOCUMENTS.equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

            } else if (DOWNLOAD_DOCUMENTS.equals(uri.getAuthority())) {
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));
                return getDataColumn(context, contentUri, null, null);
            } else if (MEDIA_DOCUMENTS.equals(uri.getAuthority())) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];
                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }
                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};
                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        } else if (isLocalContentUri(uri)) {
            return getDataColumn(context, uri, null, null);
        } else if (isLocalFileUri(uri)) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       context
     * @param uri           uri
     * @param selection     selection (Optional) Filter used in the query
     * @param selectionArgs selectionArgs (Optional) Selection arguments used in the query
     * @return The value of the _data column, which is typically a file path
     */
    @Nullable
    private static String getDataColumn(Context context, Uri uri, String selection, String[] selectionArgs) {
        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};
        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs, null);
            if (cursor != null && cursor.moveToFirst()) {
                int index = cursor.getColumnIndexOrThrow(column);
                if (index != -1) {
                    return cursor.getString(index);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return null;
    }

}