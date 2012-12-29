package net.stkaddons.viewer;

import java.io.File;
import java.io.IOException;

import org.xmlpull.v1.XmlPullParserException;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends Activity {
	public static String xml_news = null;
	public static String xml_assets = null;
	public final static String SELECTED_MODE = "net.stkaddons.viewer.MenuActivity.SELECTED_MODE";
	public final static int INIT_S_OK = 1;
	public final static int INIT_S_OFFLINE = 2;
	public final static int INIT_S_DL_ERR = 3;

	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_init);
        
        int initStatus = doInit();
        switch (initStatus) {
	        case INIT_S_OK:
	        	break;
	        case INIT_S_OFFLINE:
	        	TextView menu_error = (TextView) findViewById(R.id.menu_loading_message);
	        	menu_error.setText(R.string.error_no_network);
	        	Button quit_button = (Button) findViewById(R.id.quit_button);
	        	quit_button.setVisibility(View.VISIBLE);
	        	break;
        	default:
        		// Throw an error
        		break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menu_settings:
                openSettings();
                return true;
            case R.id.menu_credits:
            	openCredits();
            	return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private int doInit() {    
    	// Check network connection
    	if (!Network.isConnected(this)) {
    		return INIT_S_OFFLINE;
    	}
 
        // Fetch XML files
        new DownloadFileTask().execute(getString(R.string.xml_news));
        new DownloadFileTask().execute(getString(R.string.xml_assets));

    	return INIT_S_OK;
    }
    
    public void openSettings() {
    	Intent settingsActivity = new Intent(this, SettingsActivity.class);
    	startActivity(settingsActivity);
    }
    
    public void openCredits() {
    	Intent creditsActivity = new Intent(this, CreditsActivity.class);
    	startActivity(creditsActivity);
    }
    
    public void menuButton(View view) {
    	Button clicked = (Button) findViewById(view.getId());
    	CharSequence mode = clicked.getHint();
    	
    	if (!mode.equals((CharSequence) "music")) {
	    	Intent browser = new Intent(this, AddonListActivity.class);
	    	browser.putExtra(SELECTED_MODE, mode);
	    	startActivity(browser);
    	} else {
    		// Go to music browser
    		Intent browser = new Intent(this, MusicListActivity.class);
    		startActivity(browser);
    	}
    }
    
    public void quitButton(View view) {
    	this.finish();
    }
    
    // Implementation of AsyncTask used to download a file
    private class DownloadFileTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... urls) {
            try {
            	File dlFile = Network.downloadFile(getBaseContext(), urls[0]);
            	String filePath = dlFile.getPath();
            	Log.i("DownloadFileTask", "Saved file: " + filePath);
            	
            	// Parse necessary files
            	if (dlFile.getName().equalsIgnoreCase("assets2.xml")) {
            		AssetXML parser = new AssetXML(getBaseContext(), dlFile);
            		parser.parse();
            	}
            	
                return dlFile.getName();
            } catch (IOException e) {
                return getResources().getString(R.string.error_no_network);
            } catch (XmlPullParserException e) {
            	Log.e("DownloadFileTask", "Exception: " + e.getMessage());
            	return e.getMessage();
            }
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equalsIgnoreCase("assets2.xml")) {
            	MainActivity.xml_assets = result;
            } else if (result.equalsIgnoreCase("news.xml")) {
            	MainActivity.xml_news = result;
            }
            
            if (MainActivity.xml_news != null && MainActivity.xml_assets != null) {
            	setContentView(R.layout.activity_menu);
            }
        }
    }
    
    
}
