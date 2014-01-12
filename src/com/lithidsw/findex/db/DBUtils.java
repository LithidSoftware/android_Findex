package com.lithidsw.findex.db;

import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.lithidsw.findex.R;
import com.lithidsw.findex.info.FileInfo;
import com.lithidsw.findex.utils.C;
import com.lithidsw.findex.utils.FileUtils;
import com.lithidsw.findex.widget.WidgetInfo;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

public class DBUtils {

    Context mContext;
    SharedPreferences prefs;

    private SQLiteDatabase database;
    private DBHelper dbHelper;
    final private static String TABLE_FILES = DBHelper.TABLE_FILES;
    final private static String TABLE_TAGS = DBHelper.TABLE_TAGS;
    final private static String TABLE_WIDGETS = DBHelper.TABLE_WIDGETS;

    final private static long ONE_DAY = 86400000;

    public DBUtils(Context context) {
        mContext = context;
        dbHelper = new DBHelper(mContext);
        prefs = mContext.getSharedPreferences(C.PREF, Context.MODE_PRIVATE);
    }

    public boolean open() {
        try {
            database = dbHelper.getWritableDatabase();
            return true;
        } catch (SQLException ignored) {
            return false;
        }
    }

    public void close() {
        dbHelper.close();
    }

    public void write(FileInfo fileInfo, boolean isFromTrash) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.FOLDER, fileInfo.folder);
        if (isFromTrash) {
            contentValues.put(DBHelper.FILE_PATH, fileInfo.path_old);
        } else {
            contentValues.put(DBHelper.FILE_PATH, fileInfo.path);
        }
        contentValues.put(DBHelper.FILE_NAME, fileInfo.name);
        contentValues.put(DBHelper.FILE_SIZE, fileInfo.size);
        contentValues.put(DBHelper.FILE_TAGS, fileInfo.type);
        contentValues.put(DBHelper.FILE_MODIFIED, fileInfo.modified);
        contentValues.put(DBHelper.FILE_STORAGE, fileInfo.storage);

        open();
        try {
            database.insert(TABLE_FILES, null, contentValues);
        } finally {
            close();
        }
    }

    public void writeTrash(FileInfo fileInfo) {
        FileUtils.putContext(mContext);
        String path = FileUtils.getTrashPath(fileInfo);
        Log.e("", "Path: "+path);
        if (path != null) {
            ContentValues contentValues = new ContentValues();
            contentValues.put(DBHelper.FOLDER, fileInfo.folder);
            contentValues.put(DBHelper.FILE_PATH_OLD, fileInfo.path);
            contentValues.put(DBHelper.FILE_NAME, fileInfo.name);
            contentValues.put(DBHelper.FILE_SIZE, fileInfo.size);
            contentValues.put(DBHelper.FILE_TAGS, fileInfo.type);
            contentValues.put(DBHelper.FILE_STORAGE, fileInfo.storage);
            contentValues.put(DBHelper.FILE_MODIFIED, fileInfo.modified);
            contentValues.put(DBHelper.FILE_TRASHED, new Date().getTime());
            contentValues.put(DBHelper.FILE_PATH, path);

            open();
            try {
                database.insert(DBHelper.TABLE_TRASH, null, contentValues);
            } finally {
                close();
            }

            deleteFile(fileInfo.path);
        }
    }

    public void addCustomTag(String[] items) {
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.TAGS_NAME, items[0]);
        contentValues.put(DBHelper.TAGS_TYPE, items[1]);
        contentValues.put(DBHelper.TAGS_VALUE, items[2]);
        open();
        try {
            database.insert(TABLE_TAGS, null, contentValues);
        } finally {
            close();
        }
    }

    public void addWidget(WidgetInfo widgetInfo) {
        Log.e("Fearch", "Added id: " + widgetInfo.id + " Pos: " + widgetInfo.position + " Value: " + widgetInfo.value);
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBHelper.WIDGETS_ID, widgetInfo.id);
        contentValues.put(DBHelper.WIDGETS_POSITION, widgetInfo.position);
        contentValues.put(DBHelper.WIDGETS_VALUE, widgetInfo.value);
        open();
        try {
            database.insert(TABLE_WIDGETS, null, contentValues);
        } finally {
            close();
        }
    }

    public String getStorage(String path) {
        String[] whereArgs = {path};
        String item = "";
        open();
        Cursor c = database.query(TABLE_FILES, new String[]{"*"}, DBHelper.FILE_PATH + " = ? ", whereArgs, null, null, getSort());
        item = c.getString(c.getColumnIndex(DBHelper.FILE_STORAGE));
        close();
        return item;
    }

    public int getWidgetCount() {
        int r = 0;
        open();
        Cursor c = database.rawQuery(
                "select count(*) from " + TABLE_WIDGETS, null
        );
        if (c.moveToFirst()) {
            r = c.getInt(0);
        }
        close();
        return r;
    }

    public int getFileCount() {
        int r = 0;
        open();
        Cursor c = database.rawQuery(
                "select count(*) from " + TABLE_FILES, null
        );
        if (c.moveToFirst()) {
            r = c.getInt(0);
        }
        close();
        return r;
    }

    public int getDownloadCount() {
        int r = 0;
        open();
        Cursor c = database.rawQuery(
                "select count(*) from " + TABLE_FILES + " where " + DBHelper.FOLDER + " like \"%ownload%\"", null
        );
        if (c.moveToFirst()) {
            r = c.getInt(0);
        }
        close();
        return r;
    }

    public int getTrashCount() {
        int r = 0;
        open();
        Cursor c = database.rawQuery(
                "select count(*) from " + DBHelper.TABLE_TRASH, null
        );
        if (c.moveToFirst()) {
            r = c.getInt(0);
        }
        close();
        return r;
    }

    public int getTagCount() {
        int r = 0;
        open();
        try {
            Cursor c = database.query(TABLE_FILES, new String[]{"*"}, null, null, null, null, getSort());
            r = c.getCount();
        } finally {
            close();
        }

        return r;
    }

    public int getCustomTagItemCount(String tag) {
        int r = 0;
        String[] custom = getCustomTag(tag);
        open();
        try {
            Cursor c = getQuery(custom, "");
            r = c.getCount();
        } finally {
            close();
        }

        return r;
    }

    public int getMainTagItemCount(String tag) {
        String[] whereArgs = {tag};
        int r = 0;
        open();
        try {
            Cursor c = database.query(TABLE_FILES, new String[]{"*"}, DBHelper.FILE_TAGS + " = ? ", whereArgs, null, null, getSort());
            r = c.getCount();
        } finally {
            close();
        }

        return r;
    }


    public void deleteOlder30() {
        open();
        database.delete(
                DBHelper.TABLE_TRASH, DBHelper.FILE_TRASHED + " < date('now','-30 day')", null
        );
        close();
    }

    public void deleteFile(String file) {
        String[] whereArgs = {file};
        open();
        database.delete(
                TABLE_FILES, DBHelper.FILE_PATH + " = ? ", whereArgs
        );
        close();
    }

    public void deleteAllCommonPath(String file) {
        String[] whereArgs = {"%"+file+"%"};
        open();
        database.delete(
                TABLE_FILES, DBHelper.FILE_PATH + " LIKE ? ", whereArgs
        );
        close();
    }

    public void deleteTrashItem(String file) {
        String[] whereArgs = {file};
        open();
        database.delete(
                DBHelper.TABLE_TRASH, DBHelper.FILE_PATH + " = ? ", whereArgs
        );
        close();
    }

    public void deleteTag(String name) {
        String[] whereArgs = {name};
        open();
        database.delete(
                TABLE_TAGS, DBHelper.TAGS_NAME + " = ? ", whereArgs
        );
        close();
    }

    public void deleteWidget(int id) {
        String[] whereArgs = {String.valueOf(id)};
        open();
        database.delete(
                TABLE_WIDGETS, DBHelper.WIDGETS_ID + " = ? ", whereArgs
        );
        close();
    }

    public boolean isFileExist(String file) {
        String[] whereArgs = {file};
        boolean is = false;
        open();
        try {
            Cursor c = database.query(
                    TABLE_FILES, new String[]{"*"},
                    DBHelper.FILE_PATH + " = ?",
                    whereArgs, null, null, null
            );
            is = c.moveToFirst();
        } finally {
            close();
        }
        return is;
    }

    public void tagFiles(String tag, String path) {
        String cur_tag = getFileTag(path);
        if (!cur_tag.contains(tag)) {
            String[] whereArgs = {path};
            ContentValues value = new ContentValues();
            value.put(DBHelper.FILE_TAGS, getFileTag(path) + "," + tag);
            open();
            try {
                database.update(
                        TABLE_FILES, value, DBHelper.FILE_PATH + " = ? ", whereArgs
                );
            } finally {
                close();
            }
        }
    }

    public void updateFileTags(boolean[] bools, String path) {
        ArrayList<String[]> list = getCustomTags();
        String tag = getFileTag(path);
        String[] tags = tag.split(",");
        String main = tags[0];

        int count=0;
        for (String[] string : list) {
            if (bools[count]) {
                main = main + "," + string[0];
            }
            count++;
        }

        if (tag.equals(main)) {
            return;
        }
        updateTag(path ,main);
    }


    public boolean[] getIsTagged(String path) {
        ArrayList<String[]> list = getCustomTags();
        boolean[] bool = new boolean[getTagCount()];
        String tag = getFileTag(path);
        String[] tags = tag.split(",");

        int count=0;
        for (String[] string : list) {
            for (String item : tags) {
                bool[count] = string[0].equals(item);
                if (bool[count]) {
                    break;
                }
            }
            count++;
        }
        return bool;
    }

    public WidgetInfo getWidget(int id) {
        WidgetInfo widgetInfo = new WidgetInfo();
        String[] whereArgs = {String.valueOf(id)};
        open();
        try {
            Cursor c = database.query(
                    TABLE_WIDGETS, new String[]{"*"}, DBHelper.WIDGETS_ID + " = ? ", whereArgs,
                    null, null, null
            );
            if (c != null && c.moveToFirst()) {
                widgetInfo.id = c.getInt(c.getColumnIndex(DBHelper.WIDGETS_ID));
                widgetInfo.position = c.getInt(c.getColumnIndex(DBHelper.WIDGETS_POSITION));
                widgetInfo.value = c.getString(c.getColumnIndex(DBHelper.WIDGETS_VALUE));
            }
        } finally {
            close();
        }

        return widgetInfo;
    }

    private String getFileTag(String path) {
        String item = "";
        String[] whereArgs = {path};
        open();
        try {
            Cursor c = database.query(
                    TABLE_FILES, new String[]{"*"}, DBHelper.FILE_PATH + " = ? ", whereArgs,
                    null, null, null
            );

            if (c != null && c.moveToFirst()) {
                item = c.getString(c.getColumnIndex(DBHelper.FILE_TAGS));
            }
        } finally {
            close();
        }

        return item;
    }

    public void updateModified(String path, long modified) {
        String[] whereArgs = {path};
        ContentValues value = new ContentValues();
        value.put(DBHelper.FILE_MODIFIED, modified);
        open();
        try {
            database.update(
                    TABLE_FILES, value, DBHelper.FILE_PATH + " = ? ", whereArgs
            );
        } finally {
            close();
        }
    }

    public void updateTag(String path, String tag) {
        String[] whereArgs = {path};
        ContentValues value = new ContentValues();
        value.put(DBHelper.FILE_TAGS, tag);
        open();
        try {
            database.update(
                    TABLE_FILES, value, DBHelper.FILE_PATH + " = ? ", whereArgs
            );
        } finally {
            close();
        }
    }

    public void updateFileSize(String path, long size) {
        String[] whereArgs = {path};
        ContentValues value = new ContentValues();
        value.put(DBHelper.FILE_SIZE, size);
        open();
        try {
            database.update(
                    TABLE_FILES, value, DBHelper.FILE_PATH + " = ? ", whereArgs
            );
        } finally {
            close();
        }
    }

    public void updateFileName(String oldpath, String newpath, String oldname, String newname) {
        String[] whereArgs = {oldname, oldpath};
        ContentValues value = new ContentValues();
        value.put(DBHelper.FILE_NAME, newname);
        value.put(DBHelper.FILE_PATH, newpath);
        open();
        try {
            database.update(
                    TABLE_FILES,
                    value,
                    DBHelper.FILE_NAME + " = ? and " + DBHelper.FILE_PATH  + " = ? ",
                    whereArgs
            );
        } finally {
            close();
        }
    }

    public ArrayList<String> getWidgetConfigTags() {
        ArrayList<String> list = new ArrayList<String>();
        String[] mainTags = mContext.getResources().getStringArray(R.array.main_titles);
        Collections.addAll(list, mainTags);
        open();
        try {
            Cursor c = database.query(
                    TABLE_TAGS, new String[]{"*"}, null, null, null, null, null
            );
            if (c != null && c.moveToFirst()) {
                int count = c.getCount();
                for (int i=0; i < count; i++) {
                    list.add(c.getString(c.getColumnIndex(DBHelper.TAGS_NAME)));
                    c.moveToNext();
                }

            }
        } finally {
            close();
        }
        return list;
    }

    public ArrayList<String[]> getCustomTags() {
        ArrayList<String[]> list = new ArrayList<String[]>();
        open();
        try {
            Cursor c = database.query(
                    TABLE_TAGS, new String[]{"*"}, null, null, null, null, null
            );
            if (c != null && c.moveToFirst()) {
                int count = c.getCount();
                for (int i=0; i < count; i++) {
                    String[] strings = new String[3];
                    strings[0] = c.getString(c.getColumnIndex(DBHelper.TAGS_NAME));
                    strings[1] = c.getString(c.getColumnIndex(DBHelper.TAGS_TYPE));
                    strings[2] = c.getString(c.getColumnIndex(DBHelper.TAGS_VALUE));
                    list.add(strings);
                    c.moveToNext();
                }

            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        } finally {
            close();
        }

        return list;
    }

    public String[] getCustomTag(String tag) {
        String[] whereArgs = {tag};
        String[] items = new String[3];
        open();
        try {
            Cursor c = database.query(
                    TABLE_TAGS, new String[]{"*"}, DBHelper.TAGS_NAME + " = ?",
                    whereArgs, null, null, null
            );
            if (c.moveToFirst()) {
                int count = c.getCount();
                for (int i=0; i < count; i++) {
                    int str_length = DBHelper.TAGS_TABLE_ENTRIES.length;
                    for (int x=0; x < str_length; x++) {
                        items[x] = c.getString(c.getColumnIndex(DBHelper.TAGS_TABLE_ENTRIES[x])
                        );
                    }
                    c.moveToNext();
                }

            }
        } finally {
            close();
        }

        return items;
    }

    private String getSort() {
        switch (prefs.getInt("pref_sort", 0)) {
            case 0:
                return DBHelper.FILE_MODIFIED + " DESC";
            case 1:
                return DBHelper.FILE_MODIFIED + " ASC";
            case 2:
                return DBHelper.FILE_NAME + " COLLATE NOCASE ASC";
            case 3:
                return DBHelper.FILE_NAME + " COLLATE NOCASE DESC";
            case 4:
                return DBHelper.FILE_SIZE + " DESC";
            case 5:
                return DBHelper.FILE_SIZE + " ASC";
            default:
                return null;
        }
    }

    public ArrayList<FileInfo> getCustomTagFiles(String tag, String search) {
        ArrayList<FileInfo> list = new ArrayList<FileInfo>();
        String[] custom = getCustomTag(tag);
        open();
        try {
            Cursor c = getQuery(custom, search);
            if (c.moveToFirst()) {
                int count = c.getCount();
                for (int i=0; i < count; i++) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.path = c.getString(c.getColumnIndex(DBHelper.FILE_PATH));
                    File file = new File(fileInfo.path);

                    if (file.exists()) {
                        updateModified(file.getAbsolutePath(), file.lastModified());
                        updateFileSize(file.getAbsolutePath(), file.length());
                        fileInfo.folder = c.getString(c.getColumnIndex(DBHelper.FOLDER));
                        fileInfo.name = c.getString(c.getColumnIndex(DBHelper.FILE_NAME));
                        fileInfo.storage = c.getString(c.getColumnIndex(DBHelper.FILE_STORAGE));
                        fileInfo.type = c.getString(c.getColumnIndex(DBHelper.FILE_TAGS));
                        fileInfo.modified = c.getLong(c.getColumnIndex(DBHelper.FILE_MODIFIED));
                        fileInfo.size = c.getLong(c.getColumnIndex(DBHelper.FILE_SIZE));
                        list.add(fileInfo);
                    } else {
                        deleteFile(fileInfo.path);
                    }
                    c.moveToNext();
                }
            }
        } finally {
            close();
        }

        return list;
    }

    public ArrayList<FileInfo> getMainTagFiles(String tag, String search) {
        String[] whereArgs = {tag, "%"+search+"%"};
        ArrayList<FileInfo> list = new ArrayList<FileInfo>();
        open();
        try {
            Cursor c = database.query(TABLE_FILES, new String[]{"*"}, DBHelper.FILE_TAGS + " = ? and " + DBHelper.FILE_NAME + " like ?", whereArgs, null, null, getSort());
            if (c.moveToFirst()) {
                int count = c.getCount();
                for (int i=0; i < count; i++) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.folder = c.getString(c.getColumnIndex(DBHelper.FOLDER));
                    fileInfo.path = c.getString(c.getColumnIndex(DBHelper.FILE_PATH));
                    fileInfo.storage = c.getString(c.getColumnIndex(DBHelper.FILE_STORAGE));
                    fileInfo.name = c.getString(c.getColumnIndex(DBHelper.FILE_NAME));
                    fileInfo.type = c.getString(c.getColumnIndex(DBHelper.FILE_TAGS));
                    fileInfo.modified = c.getLong(c.getColumnIndex(DBHelper.FILE_MODIFIED));
                    fileInfo.size = c.getLong(c.getColumnIndex(DBHelper.FILE_SIZE));
                    File file = new File(fileInfo.path);
                    if (file.exists()) {
                        list.add(fileInfo);
                    } else {
                        deleteFile(fileInfo.path);
                    }
                    c.moveToNext();
                }
            }
        } finally {
            close();
        }

        return list;
    }

    public ArrayList<FileInfo> getAllTrashFiles(String search) {
        String[] whereArgs = {"%"+search+"%"};
        ArrayList<FileInfo> list = new ArrayList<FileInfo>();
        open();
        try {
            Cursor c = database.query(DBHelper.TABLE_TRASH, new String[]{"*"}, DBHelper.FILE_NAME + " like ?", whereArgs, null, null, getSort());
            if (c.moveToFirst()) {
                int count = c.getCount();
                for (int i=0; i < count; i++) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.folder = c.getString(c.getColumnIndex(DBHelper.FOLDER));
                    fileInfo.path = c.getString(c.getColumnIndex(DBHelper.FILE_PATH));
                    fileInfo.storage = c.getString(c.getColumnIndex(DBHelper.FILE_STORAGE));
                    fileInfo.path_old = c.getString(c.getColumnIndex(DBHelper.FILE_PATH_OLD));
                    fileInfo.name = c.getString(c.getColumnIndex(DBHelper.FILE_NAME));
                    fileInfo.type = c.getString(c.getColumnIndex(DBHelper.FILE_TAGS));
                    fileInfo.modified = c.getLong(c.getColumnIndex(DBHelper.FILE_MODIFIED));
                    fileInfo.size = c.getLong(c.getColumnIndex(DBHelper.FILE_SIZE));
                    File file = new File(fileInfo.path);
                    if (file.exists()) {
                        list.add(fileInfo);
                    } else {
                        deleteTrashItem(fileInfo.path);
                    }
                    c.moveToNext();
                }
            }
        } finally {
            close();
        }

        return list;
    }

    public ArrayList<FileInfo> getAllFiles(String search) {
        String[] whereArgs = {"%"+search+"%"};
        ArrayList<FileInfo> list = new ArrayList<FileInfo>();
        open();
        try {
            Cursor c = database.query(TABLE_FILES, new String[]{"*"}, DBHelper.FILE_NAME + " like ?", whereArgs, null, null, getSort());
            if (c.moveToFirst()) {
                int count = c.getCount();
                for (int i=0; i < count; i++) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.folder = c.getString(c.getColumnIndex(DBHelper.FOLDER));
                    fileInfo.path = c.getString(c.getColumnIndex(DBHelper.FILE_PATH));
                    fileInfo.storage = c.getString(c.getColumnIndex(DBHelper.FILE_STORAGE));
                    fileInfo.name = c.getString(c.getColumnIndex(DBHelper.FILE_NAME));
                    fileInfo.type = c.getString(c.getColumnIndex(DBHelper.FILE_TAGS));
                    fileInfo.modified = c.getLong(c.getColumnIndex(DBHelper.FILE_MODIFIED));
                    fileInfo.size = c.getLong(c.getColumnIndex(DBHelper.FILE_SIZE));
                    File file = new File(fileInfo.path);
                    if (file.exists()) {
                        list.add(fileInfo);
                    } else {
                        deleteFile(fileInfo.path);
                    }
                    c.moveToNext();
                }
            }
        } finally {
            close();
        }

        return list;
    }

    public ArrayList<FileInfo> getDownloadFiles(String search) {
        String[] whereArgs = {"%ownload%", "%"+search+"%"};
        ArrayList<FileInfo> list = new ArrayList<FileInfo>();
        open();
        try {
            Cursor c = database.query(TABLE_FILES, new String[]{"*"}, DBHelper.FOLDER + " like ? and " + DBHelper.FILE_NAME + " like ?", whereArgs, null, null, getSort());
            if (c.moveToFirst()) {
                int count = c.getCount();
                for (int i=0; i < count; i++) {
                    FileInfo fileInfo = new FileInfo();
                    fileInfo.folder = c.getString(c.getColumnIndex(DBHelper.FOLDER));
                    fileInfo.path = c.getString(c.getColumnIndex(DBHelper.FILE_PATH));
                    fileInfo.storage = c.getString(c.getColumnIndex(DBHelper.FILE_STORAGE));
                    fileInfo.name = c.getString(c.getColumnIndex(DBHelper.FILE_NAME));
                    fileInfo.type = c.getString(c.getColumnIndex(DBHelper.FILE_TAGS));
                    fileInfo.modified = c.getLong(c.getColumnIndex(DBHelper.FILE_MODIFIED));
                    fileInfo.size = c.getLong(c.getColumnIndex(DBHelper.FILE_SIZE));
                    File file = new File(fileInfo.path);
                    if (file.exists()) {
                        list.add(fileInfo);
                    } else {
                        deleteFile(fileInfo.path);
                    }
                    c.moveToNext();
                }
            }
        } finally {
            close();
        }

        return list;
    }

    private Cursor getQuery(String[] tag, String search) {
        String name = tag[0];
        String type = tag[1];
        String value = tag[2];
        String[] whereArgs = null;
        String query;
        if (type.equalsIgnoreCase("search")) {
            query = DBHelper.FILE_NAME + " like ? and (" + DBHelper.FILE_TAGS + " like ? or " + DBHelper.FILE_NAME + " like ?)";
            whereArgs = new String[3];
            whereArgs[0] = "%"+search+"%";
            whereArgs[1] = "%"+name+"%";
            whereArgs[2] = "%"+value+"%";
        } else if (type.equalsIgnoreCase("extension")) {
            query = DBHelper.FILE_NAME + " like ? and (" + DBHelper.FILE_TAGS + " like ? or " + DBHelper.FILE_NAME + " like ?)";
            whereArgs = new String[3];
            whereArgs[0] = "%"+search+"%";
            whereArgs[1] = "%"+name+"%";
            whereArgs[2] = "%"+value+"%";
        } else if (type.equalsIgnoreCase("time")) {
            Date date = new Date();
            long math;
            switch (Integer.parseInt(value)) {
                case 0:
                    math = date.getTime() - ONE_DAY;
                    break;
                case 1:
                    math = date.getTime() - (ONE_DAY * 2);
                    break;
                case 2:
                    math = date.getTime() - (ONE_DAY * 7);
                    break;
                case 3:
                    math = date.getTime() - (ONE_DAY * 30);
                    break;
                case 4:
                    math = date.getTime() - (ONE_DAY * 182);
                    break;
                case 5:
                    math = date.getTime() - (ONE_DAY * 365);
                    break;
                default:
                    math = 0;
                    break;
            }
            query = DBHelper.FILE_NAME + " like ? and (" + DBHelper.FILE_TAGS + " like ? or " + DBHelper.FILE_MODIFIED + " > ?)";
            whereArgs = new String[3];
            whereArgs[0] = "%"+search+"%";
            whereArgs[1] = "%"+name+"%";
            whereArgs[2] = "%"+math+"%";
        } else if (type.equalsIgnoreCase("size")) {
            String[] strings = value.split("::");
            String item = " = ";
            switch (Integer.parseInt(strings[0])) {
                case 0:
                    item = " < ";
                    break;
                case 1:
                    item = " > ";
                    break;
            }
            query = DBHelper.FILE_SIZE + item + strings[1];
        } else if (type.equalsIgnoreCase("none")) {
            query = DBHelper.FILE_NAME + " like ? and (" + DBHelper.FILE_TAGS + " like ? or " + DBHelper.FILE_TAGS + " like ?)";
            whereArgs = new String[3];
            whereArgs[0] = "%"+search+"%";
            whereArgs[1] = "%"+name+"%";
            whereArgs[2] = "%"+value+"%";
        } else {
            query = null;
        }

        return database.query(TABLE_FILES, new String[]{"*"}, query, whereArgs, null, null, getSort());
    }

}
