package net.stkaddons.viewer;

import java.io.File;
import java.io.IOException;

import net.stkaddons.viewer.Music.MusicTrack;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnBufferingUpdateListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;

public class MediaPlayerFragment extends Fragment {

    public static final String ARG_ITEM_ID = "id";
    
    View mView;

    private int mTrackId = 0;
    private Music.MusicTrack mTrack;

	private ImageButton mPlayButton;
	private ProgressBar mProgressBar;
	
	private final Handler handler = new Handler();
	
	private boolean mIsReady = false;
	private boolean mPreparing = false;
	private boolean isPlaying;
	private MediaPlayer mPlayer;
    
    public MediaPlayerFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        if (getArguments().containsKey(ARG_ITEM_ID)) {
        	mTrackId = getArguments().getInt(ARG_ITEM_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_media_player, container, false);
        mTrack = new Music(getActivity()).get(mTrackId);
        if (mTrackId == 0 || mTrack == null) {
        	return mView;
        }

		mPlayButton = (ImageButton) mView.findViewById(R.id.button_play);
		mProgressBar = (ProgressBar) mView.findViewById(R.id.progress_bar);
        
		mPlayButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View view) {
				if (!mIsReady) {
					if (!mPreparing) {
						new StreamAudioTask().execute(mTrack);
						mPreparing = true;
					}
					return;
				}
				if (mPlayer.isPlaying()) {
					mPlayer.pause();
					//playButton.setImageResource(R.drawable.button_play);
				} else {
					mPlayer.start();
					startPlayProgressUpdater();
					//playButton.setImageResource(R.drawable.button_pause);
				}
				isPlaying = !isPlaying;
        }});
        
        // Prepare media player
        try {
	        if (mPlayer != null) {
	        	mPlayer.reset();
	        } else {
	        	mPlayer = new MediaPlayer();
	        }
	        mPlayer.setOnErrorListener(new MediaErrorListener());
	        mPlayer.setOnPreparedListener(new MediaPreparedListener());
	        mPlayer.setOnBufferingUpdateListener(new MediaBufferListener());
        } catch (IllegalStateException e) {
        	
        } catch (IllegalArgumentException e) {
        	
        }
        return mView;
    }
    
    public void startPlayProgressUpdater() {
    	float progress = ((float)mPlayer.getCurrentPosition()/mPlayer.getDuration());
    	mProgressBar.setProgress((int)(progress*100));
    	
		if (mPlayer.isPlaying()) {
			Runnable notification = new Runnable() {
		        public void run() {
		        	startPlayProgressUpdater();
				}
		    };
		    handler.postDelayed(notification,500);
    	}
    }
    
    class MediaErrorListener implements OnErrorListener {
		@Override
		public boolean onError(MediaPlayer player, int arg1, int arg2) {
			Log.e("MediaPlayerFragment.MediaErrorListener.onError", "Media player error!");

			player.reset();
			return false;
		}
    }
    
    class MediaPreparedListener implements OnPreparedListener {
		@Override
		public void onPrepared(MediaPlayer player) {
			mPlayButton.setEnabled(true);
		}
    }
    
    class MediaBufferListener implements OnBufferingUpdateListener {
		@Override
		public void onBufferingUpdate(MediaPlayer player, int progress) {
			 mProgressBar.setMax(100);
			 mProgressBar.setSecondaryProgress(progress);
		}
    }
    
    class StreamAudioTask extends AsyncTask<Music.MusicTrack, String, String> {

		@Override
		protected String doInBackground(MusicTrack... track) {
			// Check if cached file exists
			File localFile;
			if (track[0].mLocalFile != null) {
				localFile = new File(track[0].mLocalFile);
				if (!localFile.exists() || !localFile.canRead()) {
					return null;
				}
			} else {
				try {
					localFile = Network.downloadFile(getActivity(), track[0].mRemoteFile);
					if (localFile == null) {
						return null;
					}
					
					// Move cached file to SD storage space
					StorageHelper sHelper = new StorageHelper(getActivity());
					File newLocalFile = new File(getActivity().getExternalFilesDir(null), localFile.getName());
					sHelper.copyToExternalStorage(localFile, newLocalFile);
					localFile.delete();
					localFile = newLocalFile;
					
					// Save download record
					track[0].mLocalFile = localFile.getAbsolutePath();
					new Music(getActivity()).add(track[0]);
				} catch (IOException e) {
					Log.e("a", e.getMessage());
					return null;
				}
			}

	        try {
				mPlayer.setDataSource(localFile.getAbsolutePath());
		        mPlayer.prepare();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mPlayer.start();
			startPlayProgressUpdater();
			isPlaying = !isPlaying;
			mIsReady  = true;
			return null;
		}
    	
    }
}

