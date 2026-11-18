package com.example.el_mozote.vehicles;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.widget.ImageView;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageStorageHelper {

    private static final String VEHICLES_DIR = "vehicle_photos";
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(3);
    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    public static File getPhotosDirectory(Context context) {
        File dir = new File(context.getFilesDir(), VEHICLES_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        return dir;
    }

    public static List<String> saveUrisToInternalStorage(Context context, List<Uri> uris) {
        List<String> savedPaths = new ArrayList<>();
        if (uris == null || uris.isEmpty()) {
            return savedPaths;
        }

        File photosDir = getPhotosDirectory(context);

        for (Uri uri : uris) {
            if (uri == null) continue;
            try {
                String fileName = "veh_" + System.currentTimeMillis() + "_"
                        + UUID.randomUUID().toString().substring(0, 8) + ".jpg";
                File destFile = new File(photosDir, fileName);

                try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
                     OutputStream outputStream = new FileOutputStream(destFile)) {

                    if (inputStream != null) {
                        byte[] buffer = new byte[8192];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }
                        outputStream.flush();
                        savedPaths.add(destFile.getAbsolutePath());
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return savedPaths;
    }

    public static void deletePhotoFiles(List<String> filePaths) {
        if (filePaths == null) return;
        for (String path : filePaths) {
            deletePhotoFile(path);
        }
    }

    public static void deletePhotoFile(String filePath) {
        if (filePath == null || filePath.isEmpty()) return;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadScaledImage(ImageView imageView, String filePath, int reqWidth, int reqHeight, int fallbackResId) {
        if (imageView == null) return;

        if (filePath == null || filePath.isEmpty()) {
            imageView.setTag(null);
            imageView.setImageResource(fallbackResId);
            return;
        }

        File file = new File(filePath);
        if (!file.exists()) {
            imageView.setTag(null);
            imageView.setImageResource(fallbackResId);
            return;
        }

        imageView.setTag(filePath);
        imageView.setImageResource(fallbackResId);

        EXECUTOR.execute(() -> {
            Bitmap bitmap = decodeSampledBitmap(filePath, reqWidth, reqHeight);
            if (bitmap != null) {
                MAIN_HANDLER.post(() -> {
                    if (filePath.equals(imageView.getTag())) {
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
    }

    private static Bitmap decodeSampledBitmap(String filePath, int reqWidth, int reqHeight) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(filePath, options);

            options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;

            return BitmapFactory.decodeFile(filePath, options);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;

        if (height > reqHeight || width > reqWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }
}
