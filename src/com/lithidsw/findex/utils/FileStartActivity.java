package com.lithidsw.findex.utils;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.MimeTypeMap;

import java.io.File;

public class FileStartActivity {

    public static void go(Context context, MrToast mrToast, String path) {
        File file = new File(path);
        MimeTypeMap map = MimeTypeMap.getSingleton();
        String ext = MimeTypeMap.getFileExtensionFromUrl(file.getName().replace(" ", ""));
        String type = map.getMimeTypeFromExtension(ext);

        if (type == null) {
            type = "*/*";
        }

        if (type.contains("video/")) {
            type = "video/*";
        } else if (type.contains("image/")) {
            type = "image/*";
        } else if (type.contains("audio/")) {
            type = "audio/*";
        }

        Intent intent = new Intent(Intent.ACTION_VIEW);
        Uri data = Uri.fromFile(file);
        intent.setDataAndType(data, type);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            mrToast.sendShortMessage("Can't find activity for type : " + type);
        }
    }
}
