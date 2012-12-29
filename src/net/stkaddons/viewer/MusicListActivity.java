package net.stkaddons.viewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class MusicListActivity extends FragmentActivity
        implements MusicListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_music_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.music_detail_container) != null) {
            mTwoPane = true;
            ((MusicListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.music_list))
                    .setActivateOnItemClick(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onItemSelected(int id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putInt(MusicDetailFragment.ARG_ITEM_ID, id);
            MusicDetailFragment fragment = new MusicDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.music_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, MusicDetailActivity.class);
            detailIntent.putExtra(MusicDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
