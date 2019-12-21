package one.robof.facedetector;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;


public class Utils {

    public static final int RESULT_LOAD_IMG = 1;

    public static void openGallery(final Activity activity) {
        activity.startActivityForResult(prepareGalleryIntent(), RESULT_LOAD_IMG);
    }

    private static Intent prepareGalleryIntent() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryIntent.setType("image/*");

        return galleryIntent;
    }

    public static String getRealPathFromURI(Uri contentUri, final Activity activity) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = activity.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public static String getFileExtension(String path) {
        String res = "";

        int i = path.lastIndexOf('.');
        if (i > 0) {
            res = path.substring(i+1);
        }

        return res;
    }

    public static byte[] getBytesFromData(final String path) {
        File file = new File(path);
        int size = (int) file.length();
        byte[] bytes = new byte[size];
        try {
            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
            buf.read(bytes, 0, bytes.length);
            buf.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bytes;
    }

    public static String getFileName(String path) {
        int slashIndex = path.lastIndexOf("/");
        if(slashIndex < 0) {
            return "";
        }

        return path.substring(slashIndex + 1);
    }

}
