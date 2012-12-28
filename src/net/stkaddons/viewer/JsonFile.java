package net.stkaddons.viewer;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class JsonFile {
	private Context mContext;
	private String mAddonId;
	private String mDataType;
	private String mLocalPath;

	public JsonFile(String url, Context context, String addonId, String dataType) {
		mContext = context;
		mAddonId = addonId;
		mDataType = dataType;
		mLocalPath = null;
		loadFile(url);
	}
	
	private void loadFile(String url) {
    	Log.i("JsonFile.loadFile", "Requesting Json file: " + url);
    	// Check if we already have the file
    	SQLiteDatabase db = null;
    	String cachePath = null;
    	
        try {
			// Open database connection
	    	SQLiteOpenHelper dbHelper = new Database(mContext);
	    	db = dbHelper.getWritableDatabase();
	    	
	    	// Query database
	    	String[] columns = {"remotePath", "localPath", "dlTime"};
	    	String[] selectionArgs = {mAddonId, mDataType};
	    	Cursor ref = db.query("json_cache", columns, "addonId = ? AND dataType = ?", selectionArgs, null, null, null);
	    	
	    	// Check if the entry exists
	    	if (ref.getCount() != 0) {
	    		ref.move(1);
	    		
	    		// Make sure file exists
	    		File cacheFile = new File(ref.getString(ref.getColumnIndex("localPath")));
	    		if (!cacheFile.exists()) {
	    			// Cache no longer exists!
	    			String[] delArgs = {ref.getString(ref.getColumnIndex("localPath"))};
	    			int del = db.delete("json_cache", "localPath = ?", delArgs);
	    			// Sanity check
	    			if (del != 1) {
	    				Log.wtf("GetJsonTask", "Failed to delete row that exists??");
	    			}
	    		} else {
    	    		Date dlTime = new Date(ref.getLong(ref.getColumnIndex("dlTime")) * 1000);
    	    		Date expireTime = new Date(new Date().getTime() - 2 * 24 * 3600 * 1000);
    	    		if (dlTime.after(expireTime)) {
    	    			// Cache is fresh. Make sure it exists, and keep it.
    	    			cachePath = ref.getString(ref.getColumnIndex("localPath"));
    	    		} else {
    	    			// Cache is old. Delete the file and clear the record of the cache.
    	    			cacheFile.delete();
    	    			String[] delArgs = {ref.getString(ref.getColumnIndex("localPath"))};
    	    			int del = db.delete("json_cache", "localPath = ?", delArgs);
    	    			// Sanity check
    	    			if (del != 1) {
    	    				Log.wtf("GetJsonTask", "Failed to delete row that exists??");
    	    			}
    	    			Log.i("GetJsonTask", "Deleted old JSON file.");
    	    		}
	    		}
	    	}
	    	
	    	if (cachePath == null) {
	    		// Download the json file
	    		try {
	    			File jsonFile = Network.downloadFile(mContext, url, "image-list-" + new Date().getTime() + ".json");
	    			if (jsonFile == null) return;
	    			cachePath = jsonFile.getAbsolutePath();
	    			
	    			// Add new cache record
					// Create new record
					ContentValues newValues = new ContentValues();
					newValues.put("remotePath", url);
					newValues.put("localPath", cachePath);
					newValues.put("dlTime", new Date().getTime()/1000);
					newValues.put("addonId", mAddonId);
					newValues.put("dataType", mDataType);
					long newRow = db.insert("json_cache", null, newValues);
					if (newRow == -1) {
						Log.e("GetJsonTask", "Error saving cache record for JSON file!");
						return;
					}
	    		}
	    		catch (IOException e) {
	    			Log.e("GetJsonTask", "Failed to download JSON file: " + ref.getString(ref.getColumnIndex("remoteFile")));
	    			return;
	    		}
	    	}
		} finally {
			// Close database connection
			if (db != null)
				db.close();
		}
        
        Log.i("GetJsonTask", "Using JSON file: " + cachePath);
        mLocalPath = cachePath;
	}
	
	public String getPath() {
		return mLocalPath;
	}
}
