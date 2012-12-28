package net.stkaddons.viewer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;

public class Network {
	
	private static String mUserAgent = "SuperTuxKart/Addon-Viewer (Android " + Build.VERSION.RELEASE + ")";
	
	public static boolean isConnected(Context context) {
    	// Check network connection
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || networkInfo.isRoaming() || !connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected()) {
            // Off-line
        	return false;
        }
        return true;
	}
	
	public static File downloadFile(Context context, String urlString) throws IOException {
		URL url = new URL(urlString);
        File remoteFile = new File(url.toString());
        return downloadFile(context, urlString, remoteFile.getName());
	}
	
    /**
     * Download a file to cache directory
     * @param context
     * @param urlString
     * @return Reference to cache file
     * @throws IOException
     */
    public static File downloadFile(Context context, String urlString, String destFilename) throws IOException {
        InputStream stream = null;
        FileOutputStream output = null;
        HttpURLConnection conn = null;
        
        try {
            URL url = new URL(urlString);
            File cacheFile = new File(context.getCacheDir(), destFilename);
            
        	// Check for existing copy of the file
            long maxCacheAge = 2*24*3600*1000;
            if (cacheFile.exists() && (System.currentTimeMillis() - cacheFile.lastModified()) < maxCacheAge) {
            	Log.i("downloadFile", "Using cached file: " + cacheFile.getPath());
            	return cacheFile;
            }

            if (!isConnected(context))
            	return null;
            
            conn = (HttpURLConnection) url.openConnection();
            conn.setRequestProperty("User-Agent", Network.mUserAgent);
            conn.setReadTimeout(10000 /* milliseconds */);
            conn.setConnectTimeout(15000 /* milliseconds */);
            conn.setRequestMethod("GET");
            conn.setDoInput(true);
            // Starts the query
            conn.connect();
            stream = new BufferedInputStream(conn.getInputStream());
            
            // Write cache file
            cacheFile.createNewFile();
            output = new FileOutputStream(cacheFile);
            int bufferSize = 1024;
	        byte[] buffer = new byte[bufferSize];
	        int len = 0;
	        while ((len = stream.read(buffer)) != -1) {
	            output.write(buffer, 0, len);
	        }
	        
	        return cacheFile;
            
        // Makes sure the streams and connections are closed
        } finally {
            if (stream != null) {
                stream.close();
            }
            if (output != null) {
            	output.close();
            }
            if (conn != null) {
            	conn.disconnect();
            }
        }
    }
}
