package net.stkaddons.viewer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Addon {
	
	private Context mContext;
	private static List<String> mAddonList = null;
	
	private String mAddonId;
	private String mType;
	private String mName;
	private int mRevision;
	private int mDownloaded;
	private String mDescription;
	private String mDesigner;
	private String mRemotePath;
	private String mImagePath;
	private String mIconPath;
	private String mRemoteIconPath;
	private String mLocalPath;
	private String mImageListPath;
	private float mRating;
	private int mStatus;
	
	// Define status flags
	public static final int F_APPROVED = 1;
	public static final int F_ALPHA = 2;
	public static final int F_BETA = 4;
	public static final int F_RC = 8;
	public static final int F_INVISIBLE = 16;
	public static final int F_DFSG = 64;
	public static final int F_FEATURED = 128;
	public static final int F_LATEST = 256;
	public static final int F_TEX_NOT_POT = 512;
	
	public static final float MAX_RATING = 3.0f;
	
	public Addon(Context context) {
		mContext = context;
		
		clear();
	}
	
    public class AddonItem {

        public String id;
        public String name;
        public int status;
        public String type;
        public String icon;

        public AddonItem(String id, String name, int status, String type, String icon) {
            this.id = id;
            this.name = name;
            this.status = status;
            this.type = type;
            this.icon = icon;
        }

        @Override
        public String toString() {
            return name;
        }
    }
    
    public ArrayList<AddonItem> getListOfType(String type) {
		SQLiteDatabase db = null;		
		try {
			// Open database connection
	    	SQLiteOpenHelper dbHelper = new Database(mContext);
	    	db = dbHelper.getReadableDatabase();
	    	
	    	// Query database
	    	String[] columns = {"addonId", "name", "status", "type", "iconPath", "remoteIconPath"};
	    	String[] selectionArgs = {type};
	    	Cursor ref = db.query("addons", columns, "type = ?", selectionArgs, null, null, null);
	    	
	    	// Put entries into list
	    	ArrayList<AddonItem> list = new ArrayList<AddonItem>();
	    	for (int i = 0; i < ref.getCount(); i++) {
	    		ref.move(1);
	    		
	    		String name = ref.getString(ref.getColumnIndex("name"));
	    		String iconPath = ref.getString(ref.getColumnIndex("iconPath"));
	    		
	    		// Make sure cached image still exists
	    		if (iconPath == null) {
	    			Log.i("AddonList", "No icon for: " + name);
	    		} else if (!iconPath.startsWith("http")) {
		    		File iconFile = new File(iconPath);
		    		if(!iconFile.exists()) {
			    		Log.w("AddonItem", "Icon not found: " + iconPath);
		    			iconPath = ref.getString(ref.getColumnIndex("remoteIconPath"));
		    		}
	    		}
	    		
	    		list.add(new AddonItem(ref.getString(ref.getColumnIndex("addonId")),
	    				name,
	    				ref.getInt(ref.getColumnIndex("status")),
	    				type,
	    				iconPath));
	    	}
	    	
	    	return list;
		} finally {
			// Close database connection
			if (db != null)
				db.close();
		}
    }
	
	public boolean add(String addonId, String type, String name, int revision,
			String description, String designer, String remotePath,
			String imagePath, String iconPath, int status, String imageList, float rating) {
		// Load the addon list if it hasn't been loaded yet
		if (Addon.mAddonList == null)
			getAddonList();
		
		SQLiteDatabase db = null;
		try {
			// Create database connection
			SQLiteOpenHelper dbHelper = new Database(mContext);
			db = dbHelper.getWritableDatabase();
			
			// Create list of parameters to set
			ContentValues values = new ContentValues();
			values.put("name", name);
			values.put("revision", revision);
			values.put("remotePath", remotePath);
			values.put("imagePath", imagePath);
			values.put("iconPath", iconPath);
			values.put("remoteIconPath", iconPath);
			values.put("status", status);
			values.put("description", description);
			values.put("designer", designer);
			values.put("imageListPath", imageList);
			values.put("rating", rating);
			
			// Check if this addon already exists
			if (Addon.mAddonList.contains(addonId)) {
				if (!getAddon(addonId)) {
					Log.wtf("Addon", "Could not load existing addon: " + addonId);
					return false;
				}
				
				if (mRevision >= revision) {
					clear();
					return true;
				} else {
					// Update existing record
					String[] whereArgs = {addonId};
					int rowsUpdated = db.update("addons", values, "addonId = ?", whereArgs);
					if (rowsUpdated == 0)
						return false;;
				}
			} else {
				// Create new record
				values.put("addonId", addonId);
				values.put("type", type);
				long newRow = db.insert("addons", null, values);
				if (newRow == -1)
					return false;
				
				// Reload addon list
				Addon.mAddonList = null;
				getAddonList();
			}
		} finally {
			if (db != null) {
				db.close();
			}
		}
		
		return true;
	}
	
	/**
	 * Initialise class variables
	 */
	private void clear() {
		mAddonId = null;
		mType = null;
		mName = null;
		mRevision = -1;
		mDownloaded = -1;
		mDescription = null;
		mDesigner = null;
		mRemotePath = null;
		mImagePath = null;
		mIconPath = null;
		mRemoteIconPath = null;
		mLocalPath = null;
		mStatus = -1;
		mImageListPath = null;
		mRating = 0.0f;
	}
	
	/**
	 * Get the list of known addons
	 * @return
	 */
	public List<String> getAddonList() {
		// Return addon list if we already have it
		if (Addon.mAddonList != null)
			return Addon.mAddonList;
		
		SQLiteDatabase db = null;		
		try {
			// Open database connection
	    	SQLiteOpenHelper dbHelper = new Database(mContext);
	    	db = dbHelper.getReadableDatabase();
	    	
	    	// Query database
	    	String[] columns = {"addonId"};
	    	Cursor ref = db.query("addons", columns, null, null, null, null, null);
	    	
	    	// Put entries into list
	    	List<String> result = new ArrayList<String>();
	    	for (int i = 0; i < ref.getCount(); i++) {
	    		ref.move(1);
	    		result.add(ref.getString(0));
	    	}
	    	
	    	Addon.mAddonList = result;
			return result;
		} finally {
			// Close database connection
			if (db != null)
				db.close();
		}
	}
	
	/**
	 * Load an addon record from the database
	 * @param addonId
	 * @return
	 */
	public boolean getAddon(String addonId) {
		// Reset the instance
		if (mAddonId != null) {
			clear();
		}
		
		// Load the addon list if it hasn't been loaded yet
		if (Addon.mAddonList == null)
			getAddonList();
		
		// Check if the desired addon is in the list
		if (!Addon.mAddonList.contains(addonId)) {
			Log.e("Addon", "Could not find requested addon: " + addonId);
			return false;
		}
		
		SQLiteDatabase db = null;
		try {
			// Open database connection
	    	SQLiteOpenHelper dbHelper = new Database(mContext);
	    	db = dbHelper.getReadableDatabase();

	    	// Query database
	    	String[] columns = {"type", "name", "revision", "description", "designer", "downloaded", "remotePath",
	    			"imagePath", "iconPath", "remoteIconPath", "localPath", "status", "imageListPath", "rating"};
	    	String[] selectionArgs = {addonId};
	    	Cursor ref = db.query("addons", columns, "addonId = ?", selectionArgs, null, null, null);

	    	// Save results
	    	if (ref.getCount() == 0) {
	    		Log.wtf("Addon", "Could not find addon which was in the addon list: " + addonId);
	    		return false;
	    	}
	    	ref.moveToFirst();
	    	mType = ref.getString(ref.getColumnIndex("type"));
	    	mName = ref.getString(ref.getColumnIndex("name"));
	    	mRevision = ref.getInt(ref.getColumnIndex("revision"));
	    	mDescription = ref.getString(ref.getColumnIndex("description"));
	    	mDesigner = ref.getString(ref.getColumnIndex("designer"));
	    	mDownloaded = ref.getInt(ref.getColumnIndex("downloaded"));
	    	mRemotePath = ref.getString(ref.getColumnIndex("remotePath"));
	    	mImagePath = ref.getString(ref.getColumnIndex("imagePath"));
	    	mIconPath = ref.getString(ref.getColumnIndex("iconPath"));
	    	mRemoteIconPath = ref.getString(ref.getColumnIndex("remoteIconPath"));
	    	mLocalPath = ref.getString(ref.getColumnIndex("localPath"));
	    	mStatus = ref.getInt(ref.getColumnIndex("status"));
	    	mAddonId = addonId;
	    	mImageListPath = ref.getString(ref.getColumnIndex("imageListPath"));
	    	mRating = ref.getFloat(ref.getColumnIndex("rating"));
	    	
		} finally {
			if (db != null)
				db.close();
		}

		return true;
	}
	
	public String getImageList() {
		return mImageListPath;
	}
	
	/**
	 * Get the loaded addon's name
	 * @return
	 */
	public String getName() {
		if (mAddonId == null) {
			Log.e("Addon", "Addon has not been loaded yet.");
			return null;
		}
		
		return mName;
	}
	
	/**
	 * Get the loaded addon's type
	 * @return
	 */
	public String getType() {
		if (mAddonId == null) {
			Log.e("Addon", "Addon has not been loaded yet.");
			return null;
		}
		
		return mType;
	}
	
	/**
	 * Get the loaded addon's revision
	 * @return
	 */
	public int getRevision() {
		if (mAddonId == null) {
			Log.e("Addon", "Addon has not been loaded yet.");
			return -1;
		}
		
		return mRevision;
	}
	
	/**
	 * Get the loaded addon's status
	 * @return
	 */
	public int getStatus() {
		if (mAddonId == null) {
			Log.e("Addon", "Addon has not been loaded yet.");
			return -1;
		}
		
		return mStatus;
	}
	
	/**
	 * Get the loaded addon's description
	 * @return
	 */
	public String getDescription() {
		if (mAddonId == null) {
			Log.e("Addon", "Addon has not been loaded yet.");
			return null;
		}
		
		return mDescription;
	}
	
	/**
	 * Get the loaded addon's designer
	 * @return
	 */
	public String getDesigner() {
		if (mAddonId == null) {
			Log.e("Addon", "Addon has not been loaded yet.");
			return null;
		}
		
		return mDesigner;
	}
	
	/**
	 * Get the loaded addon's download state
	 * @return
	 */
	public int getDownloaded() {
		if (mAddonId == null) {
			Log.e("Addon", "Addon has not been loaded yet.");
			return -1;
		}
		
		return mDownloaded;
	}
	
	/**
	 * Get the loaded addon's rating
	 * @return
	 */
	public float getRating() {
		if (mAddonId == null) {
			Log.e("Addon", "Addon has not been loaded yet.");
			return 0.0f;
		}
		
		return mRating;
	}
	
	private boolean set(String field, String value) {
		if (mAddonId == null) {
			return false;
		}
		
		SQLiteDatabase db = null;
		try {
			// Open database connection
	    	SQLiteOpenHelper dbHelper = new Database(mContext);
	    	db = dbHelper.getWritableDatabase();

	    	// Write
	    	String[] searchArgs = {mAddonId};
	    	ContentValues update = new ContentValues();
	    	update.put(field, value);
	    	int affected = db.update("addons", update, "addonId = ?", searchArgs);
	    	if (affected == 0) return false;

	    	// Save results
	    	if (field.equals("iconPath"))
	    		mIconPath = value;
	    	
		} finally {
			if (db != null)
				db.close();
		}

		return true;
	}
	
	public boolean setIcon(String path) {
		return set("iconPath", path);
	}
}
