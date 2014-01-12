package com.lithidsw.findex.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.StatFs;

import com.lithidsw.findex.R;
import com.lithidsw.findex.db.DBUtils;
import com.lithidsw.findex.info.FileInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;

public class FileUtils {

    private static Context mContext;

    public static void putContext(Context context) {
        mContext = context;
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
    public static long[] getStorageData(String path) {
        StatFs statFs = new StatFs(path);
        long available;
        long total;
        if (C.CURRENT_SDK > C.SDK_19) {
            available = statFs.getAvailableBlocksLong();
            total = statFs.getBlockCountLong();
        } else {
            long blockSize = statFs.getBlockSize();
            total = statFs.getBlockCount() * blockSize;
            available = statFs.getAvailableBlocks() * blockSize;
        }

        return new long[]{total, (total - available)};
    }

    public static String getPrettyTag(String tag) {
        String item = "";
        String[] tags = tag.split(",");
        int count = 0;
        for (String string : tags) {
            if (count == 0) {
                item = item+string;
            } else {
                item = item + " | " + string;
            }
            count++;
        }

        return item;
    }

    public static String getTrashPath(FileInfo fileInfo) {
        boolean proceed = true;
        File file_new_dir = new File(mContext.getExternalCacheDir(), ".MrFiles_Trash");
        if (!file_new_dir.exists()) {
            proceed = file_new_dir.mkdir();
        }

        if (proceed) {
            File file_old = new File(fileInfo.path);
            File file_new = new File(file_new_dir.getAbsolutePath(), fileInfo.name);

            if (file_old.exists()) {
                if (copyFile(file_old, file_new)) {
                    file_old.delete();
                    return file_new.getAbsolutePath();
                }
            }
        }

        return null;
    }

    public static boolean copyFile(File src, File dst) {
        FileChannel inChannel;
        FileChannel outChannel;
        try {
            inChannel = new FileInputStream(src).getChannel();
            outChannel = new FileOutputStream(dst).getChannel();
            inChannel.transferTo(0, inChannel.size(), outChannel);
            if (inChannel != null) {
                inChannel.close();
            }
            if (outChannel != null) {
                outChannel.close();
            }
        } catch (IOException ignored) {}
        return dst.exists();
    }

    private static boolean restoreFile(String old_path, String path) {
        File file_new = new File(old_path);
        File file_old = new File(path);

        if (file_old.exists()) {
            if (copyFile(file_old, file_new)) {
                return file_old.delete();
            }
        }

        return false;
    }

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    public static Drawable getApkIcon(Context context, String apkPath) {
        PackageManager pm = context.getPackageManager();
        if (pm != null) {
            PackageInfo info = pm.getPackageArchiveInfo(apkPath,PackageManager.GET_ACTIVITIES);
            if (info != null) {
                ApplicationInfo appInfo = info.applicationInfo;
                if (appInfo != null) {
                    appInfo.sourceDir = apkPath;
                    appInfo.publicSourceDir = apkPath;
                    try {
                        return appInfo.loadIcon(pm);
                    } catch (OutOfMemoryError ignored) {}
                }
            }
        }
        return context.getResources().getDrawable(R.drawable.loader);
    }

    public static void restoreTrash(final Context context, final ArrayList<FileInfo> files) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBUtils dbUtils = new DBUtils(context);
                for (FileInfo item : files) {
                    dbUtils.write(item, true);
                    dbUtils.deleteTrashItem(item.path);
                    restoreFile(item.path_old, item.path);
                }

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.sendBroadcast(new Intent(C.ACTION_INDEX_COMPLETE));
                    }
                });
            }
        }).start();
    }

    public static void file2Trash(final Context context, final ArrayList<FileInfo> files) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                DBUtils dbUtils = new DBUtils(context);
                for (FileInfo item : files) {
                    dbUtils.writeTrash(item);
                }

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.sendBroadcast(new Intent(C.ACTION_INDEX_COMPLETE));
                    }
                });
            }
        }).start();
    }

    public static void deleteFiles(final Context context, final ArrayList<FileInfo> files) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean isGone = true;
                for (FileInfo item : files) {
                    File file = new File(item.path);
                    if (isGone) {
                        if (file.exists()) {
                            isGone = file.delete();
                            new DBUtils(context).deleteFile(item.path);
                        }
                    }
                }

                if (isGone) {
                    ((Activity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            context.sendBroadcast(new Intent(C.ACTION_INDEX_COMPLETE));
                        }
                    });
                }
            }
        }).start();
    }

    public static void tagFiles(final Context context, final String tag, final ArrayList<FileInfo> files) {

        new Thread(new Runnable() {
            @Override
            public void run() {
                for (FileInfo item : files) {
                    new DBUtils(context).tagFiles(tag, item.path);
                }

                ((Activity) context).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.sendBroadcast(new Intent(C.ACTION_INDEX_COMPLETE));
                    }
                });
            }
        }).start();
    }
}
