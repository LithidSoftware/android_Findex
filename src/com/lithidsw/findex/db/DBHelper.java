package com.lithidsw.findex.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DBHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "fearch_files.db";
    private static final int DATABASE_VERSION = 1;

    public static final String DROP_TABLE = "DROP TABLE IF EXISTS ";

    public static final String TABLE_FILES = "files";
    public static final String TABLE_TAGS = "tags";
    public static final String TABLE_TRASH = "trash";
    public static final String TABLE_WIDGETS = "widgets";

    public static final String FOLDER = "folder";
    public static final String FILE_PATH = "file_path";
    public static final String FILE_PATH_OLD = "file_path_old";
    public static final String FILE_NAME = "file_name";
    public static final String FILE_SIZE = "file_size";
    public static final String FILE_MODIFIED = "file_modified";
    public static final String FILE_TRASHED = "file_trashed";
    public static final String FILE_TAGS = "file_tags";
    public static final String FILE_STORAGE = "file_storage";

    public static final String TAGS_NAME = "tags_name";
    public static final String TAGS_TYPE = "tags_type";
    public static final String TAGS_VALUE = "tags_value";

    public static final String WIDGETS_ID = "widgets_id";
    public static final String WIDGETS_POSITION = "widgets_position";
    public static final String WIDGETS_VALUE = "widgets_value";

    public static final String[] TAGS_TABLE_ENTRIES = {
            TAGS_NAME, TAGS_TYPE, TAGS_VALUE
    };

    private static final String DATABASE_CREATE_FILES = "create table "
            + TABLE_FILES
            + "("
            + FOLDER + " TEXT NOT NULL, "
            + FILE_PATH + " TEXT NOT NULL, "
            + FILE_NAME + " TEXT NOT NULL, "
            + FILE_TAGS + " TEXT NOT NULL, "
            + FILE_SIZE + " INTEGER NOT NULL, "
            + FILE_STORAGE + " TEXT NOT NULL, "
            + FILE_MODIFIED + " INTEGER NOT NULL"
            + ");";

    private static final String DATABASE_CREATE_TRASH = "create table "
            + TABLE_TRASH
            + "("
            + FOLDER + " TEXT NOT NULL, "
            + FILE_PATH + " TEXT NOT NULL, "
            + FILE_PATH_OLD + " TEXT NOT NULL, "
            + FILE_NAME + " TEXT NOT NULL, "
            + FILE_TAGS + " TEXT NOT NULL, "
            + FILE_SIZE + " INTEGER NOT NULL, "
            + FILE_MODIFIED + " INTEGER NOT NULL, "
            + FILE_STORAGE + " TEXT NOT NULL, "
            + FILE_TRASHED + " INTEGER NOT NULL"
            + ");";

    private static final String DATABASE_CREATE_TAGS = "create table "
            + TABLE_TAGS
            + "("
            + TAGS_NAME + " TEXT NOT NULL, "
            + TAGS_TYPE + " TEXT NOT NULL, "
            + TAGS_VALUE + " TEXT NOT NULL"
            + ");";

    private static final String DATABASE_CREATE_WIDGETS = "create table "
            + TABLE_WIDGETS
            + "("
            + WIDGETS_ID + " INTEGER NOT NULL, "
            + WIDGETS_POSITION + " INTEGER, "
            + WIDGETS_VALUE + " TEXT NOT NULL"
            + ");";

    public DBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase database) {
        database.execSQL(DATABASE_CREATE_FILES);
        database.execSQL(DATABASE_CREATE_TAGS);
        database.execSQL(DATABASE_CREATE_TRASH);
        database.execSQL(DATABASE_CREATE_WIDGETS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(DROP_TABLE + TABLE_FILES);
        db.execSQL(DROP_TABLE + TABLE_TAGS);
        db.execSQL(DROP_TABLE + TABLE_TRASH);
        db.execSQL(DROP_TABLE + TABLE_WIDGETS);
        onCreate(db);
    }

}
