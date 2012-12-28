package net.stkaddons.viewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AddonListAdapter extends ArrayAdapter<Addon.AddonItem> {
	
	private int mResource;
	private int mTextField;
	private int mIconField;
	private int mIconProgress;
	private Context mContext;
	private List<Addon.AddonItem> mListObjects; 
	private List<Boolean> mDownloading;

	public AddonListAdapter(Context context, int resource,
			int textViewResourceId, int iconViewResourceId, int iconProgressResourceId, List<Addon.AddonItem> objects) {
		super(context, resource, textViewResourceId, objects);
		
		mContext = context;
		mResource = resource;
		mTextField = textViewResourceId;
		mIconField = iconViewResourceId;
		mIconProgress = iconProgressResourceId;
		mListObjects = objects;
		mDownloading = new ArrayList<Boolean>();
		for (int i = 0; i < objects.size(); i++) {
			mDownloading.add(Boolean.valueOf(false));
		}
	}
	
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        TextView text;
        LayoutInflater inflater = (LayoutInflater)getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        if (convertView == null) {
            view = inflater.inflate(mResource, parent, false);
        } else {
            view = convertView;
        }

        try {
            text = (TextView) view.findViewById(mTextField);
        } catch (ClassCastException e) {
            Log.e("ArrayAdapter", "You must supply a resource ID for a TextView");
            throw new IllegalStateException(
                    "ArrayAdapter requires the resource ID to be a TextView", e);
        }

        Addon.AddonItem item = getItem(position);
        text.setText(item.name);
        
        ImageView iconView = (ImageView) view.findViewById(mIconField);
        ProgressBar iconProgress = (ProgressBar) view.findViewById(mIconProgress);
        if (item.type.equalsIgnoreCase("kart")) {
        	String icon = item.icon;
        	if (icon == null) {
        		Log.i("AddonListAdapter", "No icon specified for: " + item.name);
        		iconProgress.setVisibility(View.GONE);
        		iconView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.kart_icon));
        		iconView.setVisibility(View.VISIBLE);
        	} else {
        		if (icon.startsWith("http") && Network.isConnected(mContext)) {
        			// Download file
                	iconView.setVisibility(View.GONE);
                	iconProgress.setVisibility(View.VISIBLE);
	        		if (!mDownloading.get(position)) {
	        			Log.d("ArrayListAdapter", "Downloading file for position: " + position);
	        			mDownloading.set(position, Boolean.valueOf(true));
	        			new IconDownloadTask().execute(icon, position, item.id);
        			}
        		} else {
        			// Check if cached file exists
        			File cacheFile = new File(icon);
        			if (cacheFile.exists()) {
            			// Show saved image
            			iconView.setImageDrawable(Drawable.createFromPath(icon));
            			iconProgress.setVisibility(View.GONE);
            			iconView.setVisibility(View.VISIBLE);
        			} else {
        				iconProgress.setVisibility(View.GONE);
        				iconView.setImageDrawable(mContext.getResources().getDrawable(R.drawable.kart_icon));
            			iconView.setVisibility(View.VISIBLE);
        				Log.i("AddonListAdapter", "Cached icon not found for: " + item.name);
        			}
        		}
        	}
        } else {
            // If not a kart, set the icon to a generic icon
        	iconProgress.setVisibility(View.GONE);
        	iconView.setVisibility(View.VISIBLE);
        }
        
        return view;
    }
    
    private class IconDownloadTask extends AsyncTask<String, String, String> {
    	private String mAddonId;
    	private int mPosition;
    	private String mFilePath;
    	
    	public void execute(String url, int position, String addonId) {
    		mAddonId = addonId;
    		mPosition = position;
    		super.execute(url);
    	}
		@Override
		protected String doInBackground(String... url) {
			try {
				File localFile = Network.downloadFile(getContext(), url[0]);
				if (localFile == null) cancel(false);
				if (isCancelled()) {
					return null;
				}
				mFilePath = localFile.getPath();
				return mFilePath;
			}
			catch (IOException e) {
				return null;
			}
		}
		
		protected void onCancelled() {
			super.onCancelled();
			mDownloading.set(mPosition, Boolean.valueOf(false));
			notifyDataSetChanged();
		}
    	
		protected void onPostExecute(String result) {
			mDownloading.set(mPosition, Boolean.valueOf(false));
			Addon ad = new Addon(mContext);
			ad.getAddon(mAddonId);
			ad.setIcon(mFilePath);
			Addon.AddonItem item = mListObjects.get(mPosition);
			item.icon = mFilePath;
			mListObjects.set(mPosition, item);
			notifyDataSetChanged();
		}
    }

}
