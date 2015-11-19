package it.jaschke.alexandria;

import android.app.ActivityManager;
import android.app.ActivityManager.RunningServiceInfo;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import it.jaschke.alexandria.AddBook.OnConnectionProvider;
import it.jaschke.alexandria.NavigationDrawerFragment.NavigationDrawerCallbacks;
//import it.jaschke.alexandria.PreferencesFragment.OnFragmentInteractionListener;
import it.jaschke.alexandria.api.Callback;

//import it.jaschke.alexandria.PreferencesFragment.OnFragmentInteractionListener;


public class MainActivity extends ActionBarActivity implements
        NavigationDrawerCallbacks, Callback, OnConnectionProvider
{
    public static final String MESSAGE_EVENT = "MESSAGE_EVENT";
    public static final String MESSAGE_KEY = "MESSAGE_EXTRA";
    public static final String MESSAGE_LIST_BOOKS = "LIST_BOOKS";
    public static final String POSITION_STATE = "position";

    public static boolean IS_TABLET = false;
    private int mMenuposition;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen mTitle. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private BroadcastReceiver mMessageReciever;

    @Override
    protected final void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        IS_TABLET = isTablet();
        if (IS_TABLET) {
            setContentView(R.layout.activity_main_tablet);
        } else {
            setContentView(R.layout.activity_main);
        }

        mMessageReciever = new MessageReceiver();
        IntentFilter filter = new IntentFilter(MESSAGE_EVENT);
        LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReciever, filter);

        mNavigationDrawerFragment = (NavigationDrawerFragment)
                getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer,
                (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    public final void onNavigationDrawerItemSelected(int position) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Fragment nextFragment;
        Boolean removeFragment = false;
        this.setmMenuposition(position);
        switch (position) {
            default:
            case 0:
                nextFragment = new ListOfBooks();
                setTitle(R.string.books);
                break;
            case 1:
                nextFragment = new AddBook();
                setTitle(R.string.scan);

                // Removes book detail fragment
                if (IS_TABLET) {
                    if (findViewById(R.id.right_container) != null) {
                        removeFragment = true;
                    }
                }
                break;
            case 2:
                nextFragment = new About();
                setTitle(R.string.about);
                break;

        }
        fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
// TODO: 14/11/15 Changed onbackpressed behaviour to not storage onNavigationDrawerItem events
                .commit();

        if (removeFragment) {
// TODO: 17/11/15 Remove book detail fragment
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.right_container);
            if (fragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .remove(fragment)
                        .commit();
            }
        }

    }

    public final void setTitle(int titleId) {
        mTitle = getString(titleId);
    }

    public final void restoreActionBar() {
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setTitle(mTitle);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }


    @Override
    public final boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public final boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            mTitle = getString(R.string.action_settings);
            // Display the fragment as the main content.
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, new PrefFragment())
                    .commit();

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public final void onSaveInstanceState(Bundle outState, PersistableBundle outPersistentState) {
        outState.putInt(POSITION_STATE, this.getmMenuposition());
        super.onSaveInstanceState(outState, outPersistentState);
    }

    @Override
    protected final void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReciever);
        super.onDestroy();
    }

    @Override
    public final void onItemSelected(String ean) {
        Bundle args = new Bundle();
        args.putString(BookDetail.EAN_KEY, ean);

        BookDetail fragment = new BookDetail();
        fragment.setArguments(args);

        int id = R.id.container;
        if (findViewById(R.id.right_container) != null) {
            id = R.id.right_container;
        }
        getSupportFragmentManager().beginTransaction()
                .replace(id, fragment)
                .addToBackStack("Book Detail")
                .commit();
    }

    @Override
    public final void onNotConnectionProvided() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        new ConnectionDialogFragment().show(fragmentManager, mTitle.toString());
    }

    private boolean serviceStillRunning(String serv) {
        ActivityManager manager = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        for (RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serv.equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public final void goBack(View view) {
        getSupportFragmentManager().popBackStack();
    }

    private boolean isTablet() {
        return (getApplicationContext().getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK)
                >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public final void onBackPressed() {
        if (getSupportFragmentManager().getBackStackEntryCount() < 1) {
            finish();
        }
        mTitle = getString(R.string.app_name);
        restoreActionBar();
        super.onBackPressed();
    }

    public final int getmMenuposition() {
        return mMenuposition;
    }

    public final void setmMenuposition(int mMenuposition) {
        this.mMenuposition = mMenuposition;
    }

    private class MessageReceiver extends BroadcastReceiver {
        @Override
        public final void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra(MESSAGE_KEY) != null) {
                Toast.makeText(MainActivity.this, intent.getStringExtra(MESSAGE_KEY), Toast.LENGTH_LONG).show();

            } else if (intent.getStringExtra(MESSAGE_LIST_BOOKS) != null) {
                // Replacing fragment with loaded cursor updated data
                FragmentManager fragmentManager = getSupportFragmentManager();
                Fragment nextFragment = new ListOfBooks();
                fragmentManager.beginTransaction()
                .replace(R.id.container, nextFragment)
                .commit();

                // popBackStack in case of tablet and landscape
                if (isTablet() && getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ) {
                    getSupportFragmentManager().popBackStack();
                }
            }
        }
    }

}