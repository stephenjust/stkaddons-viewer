package net.stkaddons.viewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

public class StorageHelper {
	private Context mContext;

	boolean mExternalStorageAvailable = false;
	boolean mExternalStorageWriteable = false;
	
	public StorageHelper(Context context) {
		mContext = context;
	}
	
	private void getExternalStorageState() {
		String state = Environment.getExternalStorageState();
		if (Environment.MEDIA_MOUNTED.equals(state)) {
		    // We can read and write the media
		    mExternalStorageAvailable = mExternalStorageWriteable = true;
		} else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
		    // We can only read the media
		    mExternalStorageAvailable = true;
		    mExternalStorageWriteable = false;
		} else {
		    // Something else is wrong. It may be one of many other states, but all we need
		    //  to know is we can neither read nor write
		    mExternalStorageAvailable = mExternalStorageWriteable = false;
		}
	}
	
	public boolean copyToExternalStorage(File source, File dest) throws IOException {
		getExternalStorageState();
		
		// Check if we have access to an external memory card
		if (!mExternalStorageAvailable || !mExternalStorageWriteable)
			throw new IOException();
		
		File extDir = mContext.getExternalFilesDir(null);
		if (extDir == null) {
			Log.wtf("a", "Couldn't find external files dir!");
			return false;
		}
		if (!extDir.exists()) {
			extDir.mkdirs();
		}
		dest.getParentFile().mkdirs();
		
		// Copy file
        InputStream is = new FileInputStream(source);
        OutputStream os = new FileOutputStream(dest);
        byte[] data = new byte[is.available()];
        is.read(data);
        os.write(data);
        is.close();
        os.close();
		
		return false;

	}
}
