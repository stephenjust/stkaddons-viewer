package net.stkaddons.viewer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

public class AddonDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";
    
    Addon mAddon;
    String mAddonId;
    ProgressBar mGalleryProgress;
    LinearLayout mGallery;
    HorizontalScrollView mGalleryCnt;
    ArrayList<File> mGalleryImages = new ArrayList<File>();
    LoadGalleryTask mRunningTask;

    public AddonDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        if (getArguments().containsKey(ARG_ITEM_ID)) {
        	mAddon = new Addon(getActivity());
        	mAddonId = getArguments().getString(ARG_ITEM_ID);
        	mAddon.getAddon(mAddonId);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_addon_detail, container, false);
        if (mAddon == null) {
        	return rootView;
        }
        
        // Display basic addon info
    	getActivity().setTitle(mAddon.getName());
        ((TextView) rootView.findViewById(R.id.addon_name)).setText(mAddon.getName());
        ((TextView) rootView.findViewById(R.id.addon_description)).setText(mAddon.getDescription());
        ((TextView) rootView.findViewById(R.id.addon_rating)).setText("Rating: " + String.format("%.1f", mAddon.getRating()) + "/" + String.format("%.1f", Addon.MAX_RATING));
        String designer = mAddon.getDesigner();
        if (designer.trim().isEmpty()) {
        	designer = "Unknown";
        }
        ((TextView) rootView.findViewById(R.id.addon_designer)).setText("Designer: " + designer.trim());
        
        // Prepare image gallery loading sequence
        mGalleryProgress = (ProgressBar) rootView.findViewById(R.id.addon_image_gallery_progress);
        mGalleryCnt = (HorizontalScrollView) rootView.findViewById(R.id.addon_image_gallery_cnt);
        mGallery = (LinearLayout) rootView.findViewById(R.id.addon_image_gallery);
        mGalleryCnt.setVisibility(View.GONE);
        mGalleryProgress.setVisibility(View.VISIBLE);
        mGalleryProgress.setMax(100);
        mGalleryProgress.setProgress(0);
        
        // Get image list
        mRunningTask = new LoadGalleryTask();
        mRunningTask.execute(mAddon);
        
        return rootView;
    }
    
    public void onDestroyView() {
    	if (mRunningTask != null && mRunningTask.getStatus() == AsyncTask.Status.RUNNING) {
    		mRunningTask.cancel(false);
    	}
    	super.onDestroyView();
    }
    
    private void initGallery() {
    	mGalleryProgress.setVisibility(View.GONE);
    	if (mGalleryImages.isEmpty()) {
    		mGalleryCnt.setVisibility(View.GONE);
    		return;
    	}
    	
    	Log.d("initGallery", "Initializing gallery with " + mGalleryImages.size() + " images");
    	for (int i = 0; i < mGalleryImages.size(); i++) {
    		ImageView image = new ImageView(getActivity());
    		Drawable draw = Drawable.createFromPath(mGalleryImages.get(i).getAbsolutePath());
    		image.setImageDrawable(draw);
    		image.setPadding(2,2,2,2);
    		image.setScaleType(ImageView.ScaleType.FIT_CENTER);
    		image.setAdjustViewBounds(true);
    		image.setContentDescription(mGalleryImages.get(i).getAbsolutePath());
    		image.setOnClickListener(new GalleryOnClickListener());
    		mGallery.addView(image);
    	}
    	mGalleryCnt.setVisibility(View.VISIBLE);
    }
    
    private class LoadGalleryTask extends AsyncTask<Addon, Integer, String> {
    	String mImageListPath = null;
    	boolean mError = false;

        @Override
        protected String doInBackground(Addon... addon) {
        	Log.i("LoadGalleryTask", "Loading Json file");
        	JsonFile imageList = new JsonFile(addon[0].getImageList(), getActivity(), mAddonId, "image-list");
        	mImageListPath = imageList.getPath();
        	if (mImageListPath == null) return null;
        	publishProgress(10);
        	
        	File json_file = new File(mImageListPath);
        	try {
	        	if (!json_file.canRead()) {
	        		throw new IOException();
	        	}
        		String json_string = readFile(mImageListPath);
            	JSONArray gallery_json = new JSONArray(json_string);
            	int num_images = gallery_json.length();
            	
            	for (int i = 0; i < num_images; i++) {
            		JSONObject image_record = gallery_json.getJSONObject(i);
            		if (image_record.has("approved") && image_record.getInt("approved") == 1) {
            			if (isCancelled()) break;
            			// Load approved image
            			if (image_record.has("url")) {
            				File image_file = Network.downloadFile(getActivity(), image_record.getString("url"));
                			mGalleryImages.add(image_file);
            			}
            		}
            		publishProgress(10 + (int)((float)(i+1)/(float)num_images * 90f));
            	}
            	
        	} catch (IOException e) {
        		mError = true;
        		Log.e("LoadGalleryTask", "Json file is not readable!");
        	} catch (JSONException e) {
        		mError = true;
        		Log.e("LoadGalleryTask", "Json file is invalid!");
        		Log.e("LoadGalleryTask", e.getMessage());
			}
        	
        	return null;
        }
        
        protected void onProgressUpdate(Integer... progress) {
        	if (isCancelled()) return;
        	mGalleryProgress.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
        	mGalleryProgress.setProgress(100);
        	if (mError) {
        		mGalleryProgress.setVisibility(View.GONE);
        	}
        	initGallery();
        }
    }
    
    private class GalleryOnClickListener implements View.OnClickListener {

		@Override
		public void onClick(View v) {
			Bundle arguments = new Bundle();
            arguments.putString(ImageViewFragment.ARG_IMAGE_PATH, (String) v.getContentDescription());
            ImageViewFragment fragment = new ImageViewFragment();
            fragment.setArguments(arguments);
            fragment.setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
            fragment.show(getFragmentManager(), "dialog");
		}
    	
    }
    
    private static String readFile(String path) throws IOException {
		FileInputStream stream = new FileInputStream(new File(path));
		try {
			FileChannel fc = stream.getChannel();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0,
					fc.size());
			/* Instead of using default, pass in a decoder. */
			return Charset.defaultCharset().decode(bb).toString();
		} finally {
			stream.close();
		}
	}
}
