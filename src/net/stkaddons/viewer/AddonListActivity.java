package net.stkaddons.viewer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.NavUtils;
import android.view.MenuItem;

public class AddonListActivity extends FragmentActivity
        implements AddonListFragment.Callbacks {

    private boolean mTwoPane;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_list);
        getActionBar().setDisplayHomeAsUpEnabled(true);

        if (findViewById(R.id.addon_detail_container) != null) {
            mTwoPane = true;
            ((AddonListFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.addon_list))
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
    public void onItemSelected(String id) {
        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putString(AddonDetailFragment.ARG_ITEM_ID, id);
            AddonDetailFragment fragment = new AddonDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.addon_detail_container, fragment)
                    .commit();

        } else {
            Intent detailIntent = new Intent(this, AddonDetailActivity.class);
            detailIntent.putExtra(AddonDetailFragment.ARG_ITEM_ID, id);
            startActivity(detailIntent);
        }
    }
}
