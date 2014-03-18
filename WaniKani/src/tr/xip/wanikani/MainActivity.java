package tr.xip.wanikani;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.animation.AnimationUtils;

import com.cocosw.undobar.UndoBarController;
import com.cocosw.undobar.UndoBarStyle;

import tr.xip.wanikani.managers.PrefManager;

public class MainActivity extends ActionBarActivity
        implements NavigationDrawerFragment.NavigationDrawerCallbacks, UndoBarController.UndoListener {

    private NavigationDrawerFragment mNavigationDrawerFragment;
    private CharSequence mTitle;

    public String TAG = "WANIKANI";

    PrefManager prefMan;

    private BroadcastReceiver mRetrofitConnectionTimeoutErrorReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            showConnectionError("timeout");
        }
    };

    private BroadcastReceiver mRetrofitConnectionErorReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            showConnectionError("connection");
        }
    };

    private BroadcastReceiver mRetrofitUnknownErrorReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            showConnectionError("unknown");
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        registerReceivers();
    }

    @Override
    public void onPause() {
        unregisterReceivers();
        super.onPause();
    }

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefMan = new PrefManager(getApplicationContext());

        if(prefMan.isFirstLaunch()) {
            startActivity(new Intent(this, FirstTimeActivity.class));
            finish();
        }

        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);

        mTitle = getTitle();

        mNavigationDrawerFragment.setUp(
                R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment fragment = null;

        switch (position) {
            case 0: {
                fragment = new DashboardFragment();
                getSupportActionBar().setTitle(R.string.title_dashboard);
                break;
            }
            case 1: {
                fragment = new ProfileFragment();
                getSupportActionBar().setTitle(R.string.title_profile);
                break;
            }
        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, fragment)
                .commit();
    }

    private void registerReceivers() {
        LocalBroadcastManager.getInstance(this).registerReceiver(mRetrofitConnectionTimeoutErrorReceiver,
                new IntentFilter(BroadcastIntents.RETROFIT_ERROR_TIMEOUT()));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRetrofitConnectionErorReceiver,
                new IntentFilter(BroadcastIntents.RETROFIT_ERROR_CONNECTION()));
        LocalBroadcastManager.getInstance(this).registerReceiver(mRetrofitUnknownErrorReceiver,
                new IntentFilter(BroadcastIntents.RETROFIT_ERROR_UNKNOWN()));
    }

    private void unregisterReceivers() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRetrofitConnectionTimeoutErrorReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRetrofitConnectionErorReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRetrofitUnknownErrorReceiver);
    }

    public void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle(mTitle);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            getMenuInflater().inflate(R.menu.global, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_logout:
                prefMan.logout();
                startActivity(new Intent(this, FirstTimeActivity.class));
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void showConnectionError(String error) {
        if (error.equals("timeout")) {
            UndoBarController.show(this, getString(R.string.error_connection_timeout),
                    this, UndoBarController.RETRYSTYLE);
        }

        if (error.equals("connection")) {
            UndoBarController.show(this, getString(R.string.error_connection_error),
                    this, UndoBarController.RETRYSTYLE);
        }

        if (error.equals("unknown")) {
            UndoBarController.show(this, getString(R.string.error_connection_error),
                    this, UndoBarController.RETRYSTYLE);
        }
    }

    @Override
    public void onUndo(Parcelable parcelable) {
        Intent intent = new Intent(BroadcastIntents.SYNC());
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
}
