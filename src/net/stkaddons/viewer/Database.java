package net.stkaddons.viewer;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Database extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 2;
    private static final String DATABASE_NAME = "stkaddons";
	
	// Define database tables
    public static final String ADDON_TABLE_NAME = "addons";
    private static final String ADDON_TABLE_CREATE =
                "CREATE TABLE " + ADDON_TABLE_NAME + " (" +
                	"addonId TEXT UNIQUE, " +
                	"type TEXT NOT NULL, " +
                	"name TEXT NOT NULL, " +
                	"description TEXT, " +
                	"designer TEXT, " +
                	"revision INT(3) NOT NULL DEFAULT 0, " +
                	"downloaded INT(3) NOT NULL DEFAULT 0, " +
                	"remotePath TEXT NOT NULL, " +
                	"imagePath TEXT, " +
                	"iconPath TEXT, " +
                	"remoteIconPath TEXT, " +
                	"localPath TEXT NULL DEFAULT NULL, " +
                	"status INT NOT NULL DEFAULT 0, " +
                	"rating FLOAT NOT NULL DEFAULT 0.0, " +
                	"imageListPath TEXT NOT NULL" +
                ");";
    
    public static final String JSON_CACHE_TABLE_NAME = "json_cache";
    public static final String JSON_CACHE_TBL_CREATE =
    			"CREATE TABLE " + JSON_CACHE_TABLE_NAME + " (" +
    				"remotePath TEXT UNIQUE NOT NULL, " +
    				"localPath TEXT UNIQUE NOT NULL, " +
    				"addonId TEXT NOT NULL, " +
    				"dataType TEXT NOT NULL," +
    				"dlTime DATETIME NOT NULL" +
    			");";
    
    // Added in db version 2
    public static final String MUSIC_TABLE_NAME = "music";
    public static final String MUSIC_TBL_CREATE =
    			"CREATE TABLE " + MUSIC_TABLE_NAME + " (" +
    				"id INT UNIQUE NOT NULL, " +
    				"title TEXT NOT NULL, " +
    				"artist TEXT NOT NULL, " +
    				"license TEXT NOT NULL, " +
    				"gain FLOAT NOT NULL, " +
    				"remoteFile TEXT UNIQUE NOT NULL, " +
    				"localFile TEXT UNIQUE, " +
    				"fileSize INT NOT NULL, " +
    				"trackLength INT NOT NULL, " +
    				"xmlFile TEXT UNIQUE" +
    			");";
	
	public Database(Context context) {
		super(context, DATABASE_NAME, null /*factory*/, DATABASE_VERSION, null /*error handler*/);
	}
	
    @Override
    // Create database
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(ADDON_TABLE_CREATE);
        db.execSQL(JSON_CACHE_TBL_CREATE);
        db.execSQL(MUSIC_TBL_CREATE);
    }

	@Override
	// Upgrade database versions
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion == 1 && newVersion > 1) {
			db.execSQL(MUSIC_TBL_CREATE);
			oldVersion++;
		}
		
	}
}
