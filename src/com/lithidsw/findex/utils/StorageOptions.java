package com.lithidsw.findex.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Environment;

import com.lithidsw.findex.R;

import java.io.File;
import java.util.ArrayList;
import java.util.Scanner;

@SuppressLint("NewApi")
public class StorageOptions {
    public static String[] labels;
    public static String[] paths;
    public static int count = 0;

    private static Context sContext;
    private static ArrayList<String> sVold = new ArrayList<String>();

    public static void determineStorageOptions(Context context) {
        sContext = context.getApplicationContext();
        readVoldFile();
        testAndCleanList();
        setProperties();
    }

    private static void readVoldFile() {
		/*
		 * Scan the /system/etc/vold.fstab file and look for lines like this:
		 * dev_mount sdcard /mnt/sdcard 1
		 * /devices/platform/s3c-sdhci.0/mmc_host/mmc0
		 *
		 * When one is found, split it into its elements and then pull out the
		 * path to the that mount point and add it to the arraylist
		 *
		 * some devices are missing the vold file entirely so we add a path here
		 * to make sure the list always includes the path to the first sdcard,
		 * whether real or emulated.
		 */

        try {
            Scanner scanner = new Scanner(new File("/system/etc/vold.fstab"));
            while (scanner.hasNext()) {
                String line = scanner.nextLine();
                if (line.startsWith("dev_mount")) {
                    String[] lineElements = line.split(" ");
                    String element = lineElements[2];

                    if (element.contains(":")) {
                        element = element.substring(0, element.indexOf(":"));
                    }

                    if (element.contains("usb")) {
                        continue;
                    }

                    if (!sVold.contains(element)) {
                        sVold.add(element);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void testAndCleanList() {
		/*
		 * Now that we have a cleaned list of mount paths, test each one to make
		 * sure it's a valid and available path. If it is not, remove it from
		 * the list.
		 */

        for (int i = 0; i < sVold.size(); i++) {
            String voldPath = sVold.get(i);
            File path = new File(voldPath);
            if (!path.exists() || !path.isDirectory() || !path.canWrite())
                sVold.remove(i--);
        }

        if (sVold.size() == 0) {
            sVold.add("/mnt/sdcard");
        }
    }

    private static void setProperties() {
		/*
		 * At this point all the paths in the list should be valid. Build the
		 * public properties.
		 */

        ArrayList<String> labelList = new ArrayList<String>();

        int j = 0;
        if (sVold.size() > 0) {
            if (!Environment.isExternalStorageRemovable() || Environment.isExternalStorageEmulated()) {
                labelList.add(sContext.getString(R.string.text_internal_storage));
            } else {
                labelList.add(sContext.getString(R.string.text_external_storage) + " 1");
                j = 1;
            }

            if (sVold.size() > 1) {
                for (int i = 1; i < sVold.size(); i++) {
                    labelList.add(sContext.getString(R.string.text_external_storage)
                            + " " + (i + j));
                }
            }
        }

        labels = new String[labelList.size()];
        labelList.toArray(labels);
        paths = new String[sVold.size()];
        sVold.toArray(paths);
        count = Math.min(labels.length, paths.length);
        sVold.clear();
    }
}
