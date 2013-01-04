package net.stkaddons.viewer;

import java.util.Locale;

import android.util.Log;

public class StringUtils {

	/**
	 * Converts a byte-magnitude file size into a string
	 * @param size File size in bytes
	 * @return Human readable file size string
	 */
	public static String sizeString(int size) {
		float value = (float) size;
		int magnitude = 0;
		
		while (value > 1000f && magnitude < 3) {
			value = value/1024f;
			magnitude++;
		}
		
		String string = String.format(Locale.getDefault(), "%.1f", value);
		switch(magnitude) {
		default:
			Log.wtf("StringUtils.sizeString", "Unreachable code path!");
			string = string.concat("B");
			break;
		case 0:
			string = string.concat("B");
			break;
		case 1:
			string = string.concat("kB");
			break;
		case 2:
			string = string.concat("MB");
			break;
		case 3:
			string = string.concat("GB");
			break;
		}
		return string;
	}
	
}
