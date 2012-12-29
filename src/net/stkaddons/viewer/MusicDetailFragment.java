package net.stkaddons.viewer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class MusicDetailFragment extends Fragment {

    public static final String ARG_ITEM_ID = "item_id";

    private Music mMusic;
    private Music.MusicTrack mTrack;
    
    public MusicDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        if (getArguments().containsKey(ARG_ITEM_ID)) {
        	mMusic = new Music(getActivity());
        	mTrack = mMusic.get(getArguments().getInt(ARG_ITEM_ID));
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_music_detail, container, false);
        if (mTrack == null) {
        	return rootView;
        }
        
        // Display basic addon info
        ((TextView) rootView.findViewById(R.id.music_title)).setText(mTrack.mTitle);
        ((TextView) rootView.findViewById(R.id.music_artist)).setText(mTrack.mArtist);

        
        return rootView;
    }
 
}
