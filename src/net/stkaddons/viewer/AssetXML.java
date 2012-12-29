package net.stkaddons.viewer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import android.content.Context;
import android.util.Log;

public class AssetXML {
	private File mFile;
	private Context mContext;
	
	private XmlPullParser xpp;
	
	public AssetXML(Context context, File xmlFile) throws FileNotFoundException {
		if (!xmlFile.exists()) {
			throw new FileNotFoundException();
		}
		mFile = xmlFile;
		mContext = context;
	}
	
	
	public void parse() throws XmlPullParserException, IOException, FileNotFoundException {    	
    	XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
    	InputStream stream = null;
    	xpp = factory.newPullParser();
    	
    	// Read file to stream and parse
    	try {
    		// Load file
    		stream = new BufferedInputStream(new FileInputStream(mFile));
        	xpp.setInput(new InputStreamReader(stream));
        	
        	// Look for assets header and check format version
        	int eventType = xpp.getEventType();
        	boolean correctVersion = false;
        	int formatVersion = -1;
        	while (eventType != XmlPullParser.END_DOCUMENT && !correctVersion) {
        		if (eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("assets")) {
        			for (int i = 0; i < xpp.getAttributeCount(); i++) {
        				String attrib = xpp.getAttributeName(i);
        				if (attrib.equalsIgnoreCase("version") && (Integer.parseInt(xpp.getAttributeValue(i)) == 2)) {
        					correctVersion = true;
        					formatVersion = Integer.parseInt(xpp.getAttributeValue(i));
        					break;
        				}
        			}
        		}
        		eventType = xpp.next();
        	}
        	if (!correctVersion) {
        		Log.e("AssetXML.parse", "Wrong XML version!");
        		return;
        	}
        	Log.i("AssetXML.parse", "Loaded asset xml version " + formatVersion);
        	
        	// Loop through each type of addon
        	while(eventType != XmlPullParser.END_DOCUMENT) {
        		if (eventType == XmlPullParser.START_TAG && isTypeTag(xpp.getName())) {
        			Log.i("AssetXML.parse", "Reading xml section for " + xpp.getName() + "s");
            		String type = xpp.getName();
            		int num_records = 0;
            		while (this.parseAddon(type)) {
            			num_records++;
            		}
            		Log.i("AssetXML.parse", "Parsed " + num_records + " records");
        		} else if (eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("music")) {
            		while (this.parseMusic()) {} // Loop through music records
        		}
        		eventType = xpp.next();
        	}
        	
        	return;
    	}
    	finally {
    		if (stream != null) {
    			stream.close();
    		}
    	}
	}
	
	private static boolean isTypeTag(String tag) {
		if (tag.equalsIgnoreCase("kart") || tag.equalsIgnoreCase("track") || tag.equalsIgnoreCase("arena")) {
			return true;
		} else {
			return false;
		}
	}
	
	private boolean parseAddon(String type) throws XmlPullParserException, IOException {
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (xpp.getName().equalsIgnoreCase("addon")) {
					// Get attributes for this addon
					String addonId = null;
					String addonName = null;
					String addonDescription = null;
					String addonDesigner = null;
					String addonImageList = null;
					float addonRating = 0.0f;
					for (int i = 0; i < xpp.getAttributeCount(); i++) {
						String attrib = xpp.getAttributeName(i);
						if (attrib.equalsIgnoreCase("id")) {
							addonId = xpp.getAttributeValue(i);
						} else if (attrib.equalsIgnoreCase("name")) {
							addonName = xpp.getAttributeValue(i);
						} else if (attrib.equalsIgnoreCase("description")) {
							addonDescription = xpp.getAttributeValue(i);
						} else if (attrib.equalsIgnoreCase("designer")) {
							addonDesigner = xpp.getAttributeValue(i);
						} else if (attrib.equalsIgnoreCase("image-list")) {
							addonImageList = xpp.getAttributeValue(i);
						} else if (attrib.equalsIgnoreCase("rating")) {
							addonRating = Float.parseFloat(xpp.getAttributeValue(i));
						}
					}
					
					// Look for revisions
					int addonStatus = -1;
					int addonRevision = -1;
					int lastRevision = -1;
					String addonRemotePath = null;
					String addonImage = null;
					String addonIcon = null;
					
					eventType = xpp.next();
					while (eventType != XmlPullParser.END_DOCUMENT &&
							!(eventType == XmlPullParser.END_TAG && xpp.getName().equalsIgnoreCase("addon"))) {
						if (eventType == XmlPullParser.START_TAG && xpp.getName().equalsIgnoreCase("revision")) {
							for (int i = 0; i < xpp.getAttributeCount(); i++) {
								String attrib = xpp.getAttributeName(i);
								if (attrib.equalsIgnoreCase("status")) {
									addonStatus = Integer.parseInt(xpp.getAttributeValue(i));
								} else if (attrib.equalsIgnoreCase("revision")) {
									addonRevision = Integer.parseInt(xpp.getAttributeValue(i));
								}
							}
							if ((addonStatus & Addon.F_APPROVED) != 0 && (addonStatus & Addon.F_INVISIBLE) == 0 && addonRevision > lastRevision) {
								lastRevision = addonRevision;

								// Get the rest of the revision attributes
								for (int i = 0; i < xpp.getAttributeCount(); i++) {
									String attrib = xpp.getAttributeName(i);
									if (attrib.equalsIgnoreCase("file")) {
			        					addonRemotePath = xpp.getAttributeValue(i);
			        				} else if (attrib.equalsIgnoreCase("image")) {
			        					addonImage = xpp.getAttributeValue(i);
			        				} else if (attrib.equalsIgnoreCase("icon")) {
			        					addonIcon = xpp.getAttributeValue(i);
			        				}
								}
							}
						}
						
						eventType = xpp.next();
					}
					addonRevision = lastRevision;
					
					// Create addon record
					Addon addon = new Addon(mContext);
        			// Check if addon exists
        			boolean createRecord = false;
        			if (!addon.getAddonList().contains(addonId)) {
        				createRecord = true;
        			} else {
        				addon.getAddon(addonId);
        				if (addon.getRevision() < addonRevision) {
	        				Log.i("AssetXML.parseAddon", "Update record for: " + addonId +
	        						" (Old: " + addon.getRevision() + "; New: " + addonRevision + ")");
	        				createRecord = true;
        				}
        			}
        			// Don't create record for hidden addons!
        			if ((addonStatus & Addon.F_APPROVED) == 0 || (addonStatus & Addon.F_INVISIBLE) != 0) {
        				createRecord = false;
        			}
        			if (createRecord) {
        				boolean addSuccess = addon.add(addonId, type, addonName, addonRevision,
        						addonDescription, addonDesigner, addonRemotePath, addonImage,
        						addonIcon, addonStatus, addonImageList, addonRating);
        				if (!addSuccess) {
        					Log.e("AssetXML.parseAddon", "Failed to add record for: " + addonId);
        				}
        			}
					return true;
				}
				break;
			// Quit at the end of the type section
			case XmlPullParser.END_TAG:
				if (isTypeTag(xpp.getName())) {
					return false;
				}
				break;
			}
			
			eventType = xpp.next();
		}
		return false;
	}
	
	private boolean parseMusic() throws XmlPullParserException, IOException {
		int eventType = xpp.next();
		while (eventType != XmlPullParser.END_DOCUMENT) {
			switch (eventType) {
			case XmlPullParser.START_TAG:
				if (xpp.getName().equalsIgnoreCase("addon")) {
					// Get attributes for this addon
					int id = 0;
					String title = null;
					String artist = null;
					float gain = 1.0f;
					String remoteFile = null;
					String localFile = null;
					for (int i = 0; i < xpp.getAttributeCount(); i++) {
						String attrib = xpp.getAttributeName(i);
						if (attrib.equalsIgnoreCase("id")) {
							id = Integer.parseInt(xpp.getAttributeValue(i));
						} else if (attrib.equalsIgnoreCase("title")) {
							title = xpp.getAttributeValue(i);
						} else if (attrib.equalsIgnoreCase("artist")) {
							artist = xpp.getAttributeValue(i);
						} else if (attrib.equalsIgnoreCase("file")) {
							remoteFile = xpp.getAttributeValue(i);
						}
					}
					
					Music music = new Music(mContext);
					Music.MusicTrack track = music.get(id);
					boolean changed = false;
					if (track == null) {
						track = new Music.MusicTrack(id, title, artist, gain, remoteFile, localFile);
						changed = true;
					} else {
						if (!title.equals(track.mTitle) || !artist.equals(track.mArtist) || !remoteFile.equals(track.mRemoteFile)) {
							changed = true;
							// Removed changed file
							if (!track.mRemoteFile.equals(remoteFile)) {
								if (track.mLocalFile != null) {
									File local = new File(track.mLocalFile);
									if (local.exists()) local.delete();
								}
								track.mLocalFile = null;
								Log.i("AssetXML.parseMusic", "Deleted old file for music track " + title);
								Log.i("AssetXML.parseMusic", "Remote file changed from " + track.mRemoteFile + " to " + remoteFile);
							}
							track.mTitle = title;
							track.mArtist = artist;
							track.mGain = gain;
							track.mRemoteFile = remoteFile;
						}
					}
					
					if (changed) {
						if (!music.add(track)) {
							Log.e("AssetXML.parseMusic", "Failed to add music track " + title);
						}
					}
					return true;
				}
				break;
			// Quit at the end of the type section
			case XmlPullParser.END_TAG:
				if (isTypeTag(xpp.getName())) {
					return false;
				}
				break;
			}
			
			eventType = xpp.next();
		}
		return false;
	}
}
