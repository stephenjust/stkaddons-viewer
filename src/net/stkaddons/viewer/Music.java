package net.stkaddons.viewer;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class Music {
	private Context mContext;
	
	public Music(Context context) {
		mContext = context;
	}
	
	public static class MusicTrack {
		public int mId;
		public String mTitle;
		public String mArtist;
		public float mGain;
		public String mLicense;
		public int mTrackLength;
		public int mFileSize;
		public String mRemoteFile;
		public String mLocalFile;
		public String mXmlFile;
		
		public MusicTrack(int id, String title, String artist, float gain, String license, int length, int size, String remote, String local, String xml) {
			mId = id;
			mTitle = title;
			mArtist = artist;
			mGain = gain;
			mLicense = license;
			mTrackLength = length;
			mFileSize= size;
			mRemoteFile = remote;
			mLocalFile = local;
			mXmlFile = xml;
		}
	}
	
	public MusicTrack get(int id) {
		SQLiteDatabase db = null;
		try {
			// Open database connection
	    	SQLiteOpenHelper dbHelper = new Database(mContext);
	    	db = dbHelper.getReadableDatabase();
			
			return this.get(id, db);
		} finally {
			if (db != null)
				db.close();
		}
	}
	
	public MusicTrack get(int id, SQLiteDatabase db) {
		if (!db.isOpen())
			return null;
		
		Cursor ref = null;
		try {
			// Query database
			String[] columns = { "title", "artist", "gain", "license", "trackLength", "fileSize",
					"remoteFile", "localFile", "xmlFile" };
			String[] selectionArgs = { Integer.toString(id) };
			ref = db.query("music", columns, "id = ?", selectionArgs, null,
					null, null);

			// Save results
			if (ref.getCount() == 0) {
				return null;
			}
			ref.moveToFirst();
			String title = ref.getString(ref.getColumnIndex("title"));
			String artist = ref.getString(ref.getColumnIndex("artist"));
			float gain = ref.getFloat(ref.getColumnIndex("gain"));
			String license = ref.getString(ref.getColumnIndex("license"));
			int length = ref.getInt(ref.getColumnIndex("trackLength"));
			int size = ref.getInt(ref.getColumnIndex("fileSize"));
			String remote = ref.getString(ref.getColumnIndex("remoteFile"));
			String local = ref.getString(ref.getColumnIndex("localFile"));
			String xml = ref.getString(ref.getColumnIndex("xmlFile"));

			return new MusicTrack(id, title, artist, gain, license, length, size, remote, local, xml);
		} finally {
			if (ref != null)
				ref.close();
		}
	}
	
	public ArrayList<MusicTrack> getAll() {
		SQLiteDatabase db = null;
		Cursor ref = null;
		try {
			// Open database connection
	    	SQLiteOpenHelper dbHelper = new Database(mContext);
	    	db = dbHelper.getReadableDatabase();

	    	// Query database
	    	String[] columns = {"id"};
	    	ref = db.query("music", columns, null, null, null, null, "title ASC");

	    	// Save results
	    	ArrayList<MusicTrack> result = new ArrayList<MusicTrack>();
	    	if (ref.getCount() == 0) {
	    		return result;
	    	}
	    	for (int i = 0; i < ref.getCount(); i++) {
	    		ref.move(1);
	    		result.add(this.get(ref.getInt(ref.getColumnIndex("id"))));
	    	}
	    	return result;
		} finally {
			if (ref != null) {
				ref.close();
			}
			if (db != null)
				db.close();
		}
	}
	
	public boolean add(MusicTrack track) {
		SQLiteDatabase db = null;
		try {
			// Create database connection
			SQLiteOpenHelper dbHelper = new Database(mContext);
			db = dbHelper.getWritableDatabase();
			
			return this.add(track, db);
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}
	
	public boolean add(MusicTrack track, SQLiteDatabase db) {
		if (!db.isOpen()) {
			return false;
		}
		if (db.isReadOnly()) {
			Log.e("Music.add", "Database is read only!");
			return false;
		}
		Log.i("Music.add", "Adding " + track.mTitle + " by " + track.mArtist);
		
		// Create list of parameters to set
		ContentValues values = new ContentValues();
		values.put("title", track.mTitle);
		values.put("artist", track.mArtist);
		values.put("gain", track.mGain);
		values.put("license", track.mLicense);
		values.put("trackLength", track.mTrackLength);
		values.put("fileSize", track.mFileSize);
		values.put("remoteFile", track.mRemoteFile);
		values.put("xmlFile", track.mXmlFile);
		
		if (this.get(track.mId) == null) {
			// Create new record
			values.put("id", track.mId);
			values.put("localFile", (String) null);
			long newRow = db.insert("music", null, values);
			if (newRow == -1)
				return false;
		} else {
			// Update existing record
			String[] whereArgs = {Integer.toString(track.mId)};
			values.put("localFile", track.mLocalFile);
			int rowsUpdated = db.update("music", values, "id = ?", whereArgs);
			if (rowsUpdated == 0)
				return false;
			
		}
		return true;
	}
}
