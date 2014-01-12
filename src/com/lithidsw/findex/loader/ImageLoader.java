package com.lithidsw.findex.loader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.widget.ImageView;

import com.lithidsw.findex.R;
import com.lithidsw.findex.widget.WidgetLoadStub;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageLoader {

    MemoryCache memoryCache = new MemoryCache();
    private Map<ImageView, String> imageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());
    private Map<WidgetLoadStub, String> widgetViews = Collections
            .synchronizedMap(new WeakHashMap<WidgetLoadStub, String>());
    ExecutorService executorService;
    Handler handler = new Handler();
    Context context;

    private File cacheDir;
    private int stub_id;

    /**
     * integer used to set the scale for the images small = 100 large = 350
     */
    private int scale = 0;

    public ImageLoader(Context c, int i, int id) {
        context = c;
        executorService = Executors.newFixedThreadPool(5);
        cacheDir = context.getCacheDir();
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        scale = i;
        stub_id = id;
    }

    public void DisplayImage(String url, ImageView imageView, WidgetLoadStub widgetLoadStub) {
        if (imageView != null) {
            imageViews.put(imageView, url);
        } else {
            widgetViews.put(widgetLoadStub, url);
        }
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            if (imageView != null) {
                imageView.setImageBitmap(bitmap);
            } else {
                widgetLoadStub.views.setImageViewBitmap(R.id.file_image, bitmap);
            }
        } else {
            queuePhoto(url, imageView, widgetLoadStub);
            if (imageView != null) {
                imageView.setImageResource(stub_id);
            } else {
                widgetLoadStub.views.setImageViewResource(R.id.file_image, stub_id);
            }
        }
    }

    private void queuePhoto(String url, ImageView imageView, WidgetLoadStub widgetLoadStub) {
        PhotoToLoad p = new PhotoToLoad(url, imageView, widgetLoadStub);
        executorService.submit(new PhotosLoader(p));
    }

    private Bitmap getBitmap(String url) {
        File f = getFile(url);
        Bitmap bitmap;

        // from cache
        bitmap = decodeFile(f.toString());
        if (bitmap != null) {
            return bitmap;
        }

        // from uri
        if (url.contains("content:")) {
            bitmap = decodeFile(url);
            return bitmap;
        }

        try {
            int res_id = Integer.parseInt(url);
            Bitmap b = decodeFile(url);
            return b;
        } catch(NumberFormatException ignored) {}

        //from video
        bitmap = null;
        bitmap = ThumbnailUtils.createVideoThumbnail(url, MediaStore.Video.Thumbnails.MINI_KIND);
        if (bitmap != null) {
            return bitmap;
        }

        // from downloaded
        File fi = new File(url);
        if (fi.exists()) {
            bitmap = null;
            bitmap = decodeFile(fi.toString());
            return bitmap;
        }

        // from web
        try {
            bitmap = null;
            URL imageUrl = new URL(url);
            HttpURLConnection conn;
            conn = (HttpURLConnection) imageUrl.openConnection();
            conn.setConnectTimeout(30000);
            conn.setReadTimeout(30000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            CopyStream(is, os);
            os.close();
            conn.disconnect();
            bitmap = decodeFile(f.toString());
            return bitmap;
        } catch (Throwable ex) {
            if (ex instanceof OutOfMemoryError) {
                memoryCache.clear();
            }
            return null;
        }
    }

    private boolean CopyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            for (; ; ) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) {
                    break;
                }
                os.write(bytes, 0, count);
            }
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    // decodes image and scales it to reduce memory consumption
    private Bitmap decodeFile(String item) {
        Uri uri = null;
        File f = null;
        boolean isUri = false;
        if (item.contains("content:")) {
            uri = Uri.parse(item);
            isUri = true;
        } else {
            f = new File(item);
        }
        try {
            // decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            if (!isUri) {
                try {
                    int res_id = Integer.parseInt(item);
                    BitmapFactory.decodeResource(context.getResources(), res_id, o);
                } catch(NumberFormatException e) {
                    FileInputStream stream = new FileInputStream(f);
                    BitmapFactory.decodeStream(stream, null, o);
                    stream.close();
                }
            } else {
                InputStream is = context.getContentResolver().openInputStream(uri);
                BitmapFactory.decodeStream(is, null, o);
                is.close();
            }

            // Find the correct scale value. It should be the power of 2.
            final int REQUIRED_SIZE = scale;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE
                        || height_tmp / 2 < REQUIRED_SIZE) {
                    break;
                }
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            // decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            Bitmap bitmap;
            if (!isUri) {
                try {
                    int res_id = Integer.parseInt(item);
                    bitmap = BitmapFactory.decodeResource(context.getResources(), res_id, o2);
                } catch(NumberFormatException e) {
                    FileInputStream stream = new FileInputStream(f);
                    bitmap = BitmapFactory.decodeStream(stream, null, o2);
                    stream.close();
                }
            } else {
                InputStream is = context.getContentResolver().openInputStream(uri);
                bitmap = BitmapFactory.decodeStream(is, null, o2);
                is.close();
            }
            return bitmap;
        } catch (FileNotFoundException ignored) {
        } catch (IOException ignored) {
        }
        return null;
    }

    private File getFile(String url) {
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;

    }

    private void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null) {
            return;
        }
        for (File f : files) {
            f.delete();
        }
    }

    // Task for the queue
    private class PhotoToLoad {
        public String url;
        public ImageView imageView;
        public WidgetLoadStub widgetLoadStub;

        public PhotoToLoad(String u, ImageView i, WidgetLoadStub w) {
            url = u;
            imageView = i;
            widgetLoadStub = w;
        }
    }

    class PhotosLoader implements Runnable {
        PhotoToLoad photoToLoad;

        PhotosLoader(PhotoToLoad photoToLoad) {
            this.photoToLoad = photoToLoad;
        }

        @Override
        public void run() {
            try {
                if (viewReused(photoToLoad)) {
                    return;
                }
                Bitmap bmp = getBitmap(photoToLoad.url);
                memoryCache.put(photoToLoad.url, bmp);
                if (viewReused(photoToLoad)) {
                    return;
                }
                BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                handler.post(bd);
            } catch (Throwable ignored) {
            }
        }
    }

    boolean viewReused(PhotoToLoad photoToLoad) {
        if (photoToLoad.imageView != null) {
            String tag = imageViews.get(photoToLoad.imageView);
            if (tag == null || !tag.equals(photoToLoad.url)) {
                return true;
            }
        } else {
            String tag = widgetViews.get(photoToLoad.widgetLoadStub);
            if (tag == null || !tag.equals(photoToLoad.url)) {
                return true;
            }
        }

        return false;
    }

    // Used to display bitmap in the UI thread
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        @Override
        public void run() {
            if (viewReused(photoToLoad)) {
                return;
            }
            if (bitmap != null) {
                if (photoToLoad.imageView != null) {
                    photoToLoad.imageView.setImageBitmap(bitmap);
                } else {
                    photoToLoad.widgetLoadStub.views.setImageViewBitmap(R.id.file_image, bitmap);
                }
            } else {
                if (photoToLoad.imageView != null) {
                    photoToLoad.imageView.setImageResource(stub_id);
                } else {
                    photoToLoad.widgetLoadStub.views.setImageViewResource(R.id.file_image, stub_id);
                }
            }
        }
    }
}