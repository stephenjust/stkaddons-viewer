package net.stkaddons.viewer;

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
		public String mRemoteFile;
		public String mLocalFile;
		
		public MusicTrack(int id, String title, String artist, float gain, String remote, String local) {
			mId = id;
			mTitle = title;
			mArtist = artist;
			mGain = gain;
			mRemoteFile = remote;
			mLocalFile = local;
		}
	}

	public MusicTrack get(int id) {
		SQLiteDatabase db = null;
		try {
			// Open database connection
	    	SQLiteOpenHelper dbHelper = new Database(mContext);
	    	db = dbHelper.getReadableDatabase();

	    	// Query database
	    	String[] columns = {"title", "artist", "gain", "remoteFile", "localFile"};
	    	String[] selectionArgs = {Integer.toString(id)};
	    	Cursor ref = db.query("music", columns, "id = ?", selectionArgs, null, null, null);

	    	// Save results
	    	if (ref.getCount() == 0) {
	    		return null;
	    	}
	    	ref.moveToFirst();
	    	String title = ref.getString(ref.getColumnIndex("title"));
	    	String artist = ref.getString(ref.getColumnIndex("artist"));
	    	float gain = ref.getFloat(ref.getColumnIndex("gain"));
	    	String remote = ref.getString(ref.getColumnIndex("remoteFile"));
	    	String local = ref.getString(ref.getColumnIndex("localFile"));
	    	
	    	return new MusicTrack(id, title, artist, gain, remote, local);
		} finally {
			if (db != null)
				db.close();
		}
	}
	
	public boolean add(MusicTrack track) {
		SQLiteDatabase db = null;
		try {
			Log.i("Music.add", "Adding " + track.mTitle + " by " + track.mArtist);
			// Create database connection
			SQLiteOpenHelper dbHelper = new Database(mContext);
			db = dbHelper.getWritableDatabase();
			
			// Create list of parameters to set
			ContentValues values = new ContentValues();
			values.put("title", track.mTitle);
			values.put("artist", track.mArtist);
			values.put("gain", track.mGain);
			values.put("remoteFile", track.mRemoteFile);
			values.put("localFile", track.mLocalFile);
			
			if (this.get(track.mId) == null) {
				// Create new record
				values.put("id", track.mId);
				long newRow = db.insert("music", null, values);
				if (newRow == -1)
					return false;
			} else {
				// Update existing record
				String[] whereArgs = {Integer.toString(track.mId)};
				int rowsUpdated = db.update("music", values, "id = ?", whereArgs);
				if (rowsUpdated == 0)
					return false;
				
			}
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return true;
	}
}
