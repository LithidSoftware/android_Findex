package com.lithidsw.findex.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.MimeTypeMap;

import com.lithidsw.findex.db.DBUtils;
import com.lithidsw.findex.info.FileInfo;

import java.io.File;

public class FileWalker {

    private SharedPreferences mPrefs;
    private DBUtils dbUtils;
    MimeTypeMap map;
    Context mContext;

    public FileWalker(Context context) {
        mContext = context;
        dbUtils = new DBUtils(mContext);
        mPrefs = mContext.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
    }

    public void walk(File root, String folderName, String storage) {
        File[] list = root.listFiles();
        if (list != null && !isNoMedia(list)) {
            for (File f : list) {
                if (f.isDirectory() && !f.isHidden() && !isNoMediaFolder(f, storage)) {
                    walk(f, f.getName(), storage);
                } else {
                    if (!f.isHidden() && !f.isDirectory()) {
                        if (!dbUtils.isFileExist(f.getAbsolutePath())) {
                            String name = f.getName();
                            map = MimeTypeMap.getSingleton();
                            String ext = MimeTypeMap.getFileExtensionFromUrl(
                                    name.toLowerCase().replace(" ", "")
                            );
                            String type = map.getMimeTypeFromExtension(ext);

                            if (type == null) {
                                type = "*/*";
                            }

                            if (type.contains("video/")) {
                                type = C.TAG_VIDEO;
                            } else if (type.contains("image/")) {
                                type = C.TAG_PICTURES;
                            } else if (type.contains("audio/")) {
                                type = C.TAG_SOUNDS;
                            } else if (type.contains("application/ogg")) {
                                type = C.TAG_SOUNDS;
                            } else {
                                type = C.TAG_DOCUMENTS;
                            }

                            FileInfo fileInfo = new FileInfo();
                            fileInfo.folder = folderName;
                            fileInfo.path = f.getAbsolutePath();
                            fileInfo.name = name;
                            fileInfo.type = type;
                            fileInfo.modified = f.lastModified();
                            fileInfo.size = f.length();
                            fileInfo.storage = storage;
                            dbUtils.write(fileInfo, false);
                        }
                    }
                }
            }
        }
    }

    private boolean isNoMedia(File[] files) {
        for (File file : files) {
            if (file.getName().equalsIgnoreCase(".nomedia")) {
                return true;
            }
        }

        return false;
    }

    private boolean isNoMediaFolder(File file, String storage) {
        boolean isNoMedia = false;
        String[] folders = mPrefs.getString(C.PREF_EXCLUDE_FOLDERS, "").split("::");
        for (String folder : folders) {
            if (folder.length() > 0) {
                isNoMedia = file.getAbsolutePath().contains(folder);
            }

            if (isNoMedia) {
                return true;
            }
        }

        isNoMedia = file.getAbsolutePath().contains(storage + "/Android") ||
                file.getAbsolutePath().contains(storage + "/LOST.DIR") ||
                file.getAbsolutePath().contains(storage + "/data");

        return isNoMedia;

    }

    public void main() {
        StorageOptions.determineStorageOptions(mContext);
        for (String path: StorageOptions.paths) {
            File file = new File(path);
            walk(file, file.getName(), path);
        }
    }
}
