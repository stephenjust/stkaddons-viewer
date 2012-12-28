package net.stkaddons.viewer;

import java.io.File;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ImageViewFragment extends DialogFragment {

    public static final String ARG_IMAGE_PATH = "image_path";
    
    File mImage;

    public ImageViewFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        if (getArguments().containsKey(ARG_IMAGE_PATH)) {
        	mImage = new File(getArguments().getString(ARG_IMAGE_PATH));
        } else {
        	dismiss();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_image_view, container, false);
        if (mImage == null || !mImage.canRead() || !mImage.exists()) {
        	return rootView;
        }
        
        Drawable image = Drawable.createFromPath(mImage.getAbsolutePath());
        ImageView imageView = ((ImageView) rootView.findViewById(R.id.image_popup));
        imageView.setImageDrawable(image);
        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
        imageView.setAdjustViewBounds(true);
        
        return rootView;
    }
}
