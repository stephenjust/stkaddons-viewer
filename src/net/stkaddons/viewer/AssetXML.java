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
        		}
        		eventType = xpp.next();
        	}
        	
        	return;
        	
//        	Addon addon = new Addon(context);
//        	String addonId;
//        	String addonName;
//        	String addonDescription;
//        	int addonStatus;
//        	int addonRevision;
//        	String addonRemotePath;
//        	String addonImage;
//        	String addonIcon;
//        	String addonImageList;
//        	float addonRating;
//        	// Loop through the document
//        	while (eventType != XmlPullParser.END_DOCUMENT) {
//        		switch (eventType) {
//	        		case XmlPullParser.START_TAG:
//	        			if (xpp.getName().equalsIgnoreCase("assets")) {
//	        				eventType = xpp.next();
//	        				continue;
//	        			} else if (!isAllowedTag(xpp.getName())) {
//	        				Log.w("AssetXML.parse", "Unknown asset type: " + xpp.getName());
//	                		eventType = xpp.next();
//	        				continue;
//	        			}
//	                	addonId = null;
//	                	addonName = null;
//	                	addonStatus = -1;
//	                	addonRevision = -1;
//	                	addonDescription = null;
//	                	addonRemotePath = null;
//	                	addonImage = null;
//	                	addonIcon = null;
//	                	addonImageList = null;
//	                	addonRating = 0f;
//	        			// Loop through attributes
//	        			for (int i = 0; i < xpp.getAttributeCount(); i++) {
//	        				String attrib = xpp.getAttributeName(i);
//	        				
//	        				if (attrib.equalsIgnoreCase("id")) {
//	        					addonId = xpp.getAttributeValue(i);
//	        				} else if (attrib.equalsIgnoreCase("status")) {
//	        					addonStatus = Integer.parseInt(xpp.getAttributeValue(i));
//	        				} else if (attrib.equalsIgnoreCase("name")) {
//	        					addonName = xpp.getAttributeValue(i);
//	        				} else if (attrib.equalsIgnoreCase("revision")) {
//	        					addonRevision = Integer.parseInt(xpp.getAttributeValue(i));
//	        				} else if (attrib.equalsIgnoreCase("file")) {
//	        					addonRemotePath = xpp.getAttributeValue(i);
//	        				} else if (attrib.equalsIgnoreCase("image")) {
//	        					addonImage = xpp.getAttributeValue(i);
//	        				} else if (attrib.equalsIgnoreCase("description")) {
//	        					addonDescription = xpp.getAttributeValue(i);
//	        				} else if (attrib.equalsIgnoreCase("icon")) {
//	        					addonIcon = xpp.getAttributeValue(i);
//	        				} else if (attrib.equalsIgnoreCase("image-list")) {
//	        					addonImageList = xpp.getAttributeValue(i);
//	        				} else if (attrib.equalsIgnoreCase("rating")) {
//	        					addonRating = Float.parseFloat(xpp.getAttributeValue(i));
//	        				} else {
//	        					Log.w("AssetXML.parser", "Unknown attribute: " + attrib);
//	        				}
//	        			}
//	        			
//	        			// Check if addon exists
//	        			boolean createRecord = false;
//	        			if (!addon.getAddonList().contains(addonId)) {
//	        				Log.i("AddonXML.parse", "No record found for: " + addonId);
//	        				createRecord = true;
//	        			} else {
//	        				addon.getAddon(addonId);
//	        				if (addon.getRevision() < addonRevision) {
//		        				Log.i("AddonXML.parse", "Update record for: " + addonId +
//		        						" (Old: " + addon.getRevision() + "; New: " + addonRevision + ")");
//		        				createRecord = true;
//	        				}
//	        			}
//	        			if (createRecord) {
//	        				boolean addSuccess = addon.add(addonId, xpp.getName(), addonName, addonRevision,
//	        						addonDescription, addonRemotePath, addonImage, addonIcon, addonStatus, addonImageList);
//	        				if (!addSuccess) {
//	        					Log.e("AddonXML.parse", "Failed to add record for: " + addonId);
//	        				}
//	        			}
//	        			break;
//	        			
//	        		case XmlPullParser.END_TAG:
//	        			// Don't do anything
//	        			break;
//	        		case XmlPullParser.TEXT:
//	        			// Don't do anything
//	        			break;
//	        		case XmlPullParser.DOCDECL:
//	        			// Don't do anything
//	        			break;
//	        		
//	        		default:
//	        			Log.i("AddonXML.parse", "Unhandled XML event: " + eventType);
//	        			break;
//        		}
//        		eventType = xpp.next();
//        	}
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
}
