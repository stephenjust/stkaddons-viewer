package net.stkaddons.viewer;

import java.util.List;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MusicListAdapter  extends ArrayAdapter<Music.MusicTrack> {
	private int mResource;
	private int mTextField;

	public MusicListAdapter(Context context, int resource,
			int textViewResourceId, List<Music.MusicTrack> objects) {
		super(context, resource, textViewResourceId, objects);

		mResource = resource;
		mTextField = textViewResourceId;
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

        Music.MusicTrack item = getItem(position);
        text.setText(item.mTitle);

        return view;
    }
}
