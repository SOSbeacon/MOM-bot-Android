package org.cnc.msrobot.activity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import org.cnc.msrobot.R;
import org.cnc.msrobot.fragment.BaseFragment;
import org.cnc.msrobot.fragment.CalendarEventFragment;
import org.cnc.msrobot.fragment.HomeFragment;
import org.cnc.msrobot.resource.EmptyResource;
import org.cnc.msrobot.resource.EventResource;
import org.cnc.msrobot.utils.Actions;
import org.cnc.msrobot.utils.AppUtils;
import org.cnc.msrobot.utils.Consts.RequestCode;
import org.cnc.msrobot.utils.CustomActionBar;
import org.cnc.msrobot.utils.DialogUtils;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.AudioManager;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public class MainActivity extends BaseActivity {
	public static final int REC_SMS = 0;
	public static final int REC_EMAIL = 1;
	public static final int REC_SEARCH = 2;
	public static final int REC_ALARM = 3;
	/**
	 * regconize index
	 */
	private int mRecIndex = 0;
	private String[] mPlanetTitles;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerList;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	protected DialogUtils mDialog;

	Listener<EmptyResource> mRequestLogoutlistener = new Listener<EmptyResource>() {

		@Override
		public void onResponse(EmptyResource response) {
			mSharePrefs.clear();
			startActivity(new Intent(MainActivity.this, LoginActivity.class));
			finish();
		}
	};

	ErrorListener mRequestLogoutError = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			showCenterToast(R.string.err_logout_fail);

		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		mDialog = new DialogUtils(this);

		mTitle = mDrawerTitle = getTitle();
		mPlanetTitles = getResources().getStringArray(R.array.array_list_drawer);
		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.left_drawer);

		// set a custom shadow that overlays the main content when the drawer opens
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
		// Set the adapter for the list view
		mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.item_list_drawer, mPlanetTitles));
		// Set the list's click listener
		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		// enable ActionBar app icon to behave as action to toggle nav drawer
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
		mActionbar.setType(CustomActionBar.TYPE_HOME);

		// ActionBarDrawerToggle ties together the the proper interactions
		// between the sliding drawer and the action bar app icon
		mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
		mDrawerLayout, /* DrawerLayout object */
		R.drawable.ic_drawer, /* nav drawer image to replace 'Up' caret */
		R.string.drawer_open, /* "open drawer" description for accessibility */
		R.string.drawer_close /* "close drawer" description for accessibility */
		) {
			public void onDrawerClosed(View view) {
				mActionbar.setTitle(mTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}

			public void onDrawerOpened(View drawerView) {
				mActionbar.setTitle(mDrawerTitle);
				invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			selectItem(0);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/* Called whenever we call invalidateOptionsMenu() */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// If the nav drawer is open, hide action items related to the content view
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		if (drawerOpen) {
			mActionbar.hideRightLayout();
		} else {
			mActionbar.showRightLayout();
		}
		return super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) { return true; }
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				// TODO refresh calendar
				break;
			case R.id.menu_add:
				startAddOrEditEventActivity(null);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	private class DrawerItemClickListener implements ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
			selectItem(position);
		}
	}

	/** Swaps fragments in the main content view */
	private void selectItem(int position) {

		// Create a new fragment and specify the planet to show based on position
		BaseFragment fragment;
		Bundle bundle = new Bundle();
		switch (position) {
			case 0:
				fragment = new HomeFragment();
				break;
			case 1:
				fragment = new CalendarEventFragment();
				bundle.putInt(CalendarEventFragment.EXTRA_TYPE, CalendarEventFragment.TYPE_MONTH);
				break;
			case 2:
				fragment = new CalendarEventFragment();
				bundle.putInt(CalendarEventFragment.EXTRA_TYPE, CalendarEventFragment.TYPE_WEEK);
				break;
			case 3:
				fragment = new CalendarEventFragment();
				bundle.putInt(CalendarEventFragment.EXTRA_TYPE, CalendarEventFragment.TYPE_DAY);
				break;
			case 4:
				// setting
				startActivityForResult(new Intent(this, EmailSetupActivity.class), RequestCode.REQUEST_EMAIL_SETUP);
				return;
			case 5:
				// Logout
				showProgress();
				mRequestManager.request(Actions.ACTION_LOGOUT, null, mRequestLogoutlistener, mRequestLogoutError);
				return;
			default:
				fragment = new HomeFragment();
				break;
		}
		fragment.setArguments(bundle);
		// Insert the fragment by replacing any existing fragment
		FragmentManager fragmentManager = getSupportFragmentManager();
		// add fragment with tag is position
		fragmentManager.beginTransaction().replace(R.id.content_frame, fragment, position + "").commit();

		// Highlight the selected item, update the title, and close the drawer
		mDrawerList.setItemChecked(position, true);
		setTitle(mPlanetTitles[position]);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		mActionbar.setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}

	public void listen(int id) {
		String msg = "";
		HashMap<String, String> myHashAlarm = new HashMap<String, String>();
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_STREAM, String.valueOf(AudioManager.STREAM_MUSIC));
		switch (id) {
			case REC_ALARM:
				msg = getString(R.string.recognize_alarm);
				break;
			case REC_EMAIL:
				msg = getString(R.string.recognize_email);
				break;
			case REC_SEARCH:
				msg = getString(R.string.recognize_search);
				break;
			case REC_SMS:
				msg = getString(R.string.recognize_sms);
				break;

		}
		myHashAlarm.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id + "");
		mTts.speak(msg, TextToSpeech.QUEUE_FLUSH, myHashAlarm);
	}

	@Override
	public void onUtteranceCompleted(String utteranceId) {
		try {
			mRecIndex = Integer.parseInt(utteranceId);
			// run listener for 200 ms delay
			handler.postDelayed(new Runnable() {
				@Override
				public void run() {
					listen();
				}
			}, 200);
		} catch (Exception e) {
		}
	}

	@Override
	public void onRecognize(final ArrayList<String> data) {
		if (data == null || data.size() == 0) return;
		switch (mRecIndex) {
			case REC_SMS:
				if (data.size() >= 1) {
					AppUtils.showSentSmsIntent(this, data.get(0));
				} else {
					mDialog.showSelectionDialog(R.string.dialog_choose_message_title,
							data.toArray(new String[data.size()]), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									AppUtils.showSentSmsIntent(MainActivity.this, data.get(which));
								}
							});
				}
				break;
			case REC_EMAIL:
				if (data.size() >= 1) {
					AppUtils.showSentEmailIntent(MainActivity.this, "", data.get(0));
				} else {
					mDialog.showSelectionDialog(R.string.dialog_choose_message_title,
							data.toArray(new String[data.size()]), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									AppUtils.showSentEmailIntent(MainActivity.this, "", data.get(which));
								}
							});
				}
				break;
			case REC_SEARCH:
				if (data.size() >= 1) {
					AppUtils.showGoogleSearchIntent(MainActivity.this, data.get(0));
				} else {
					mDialog.showSelectionDialog(R.string.dialog_choose_search_title,
							data.toArray(new String[data.size()]), new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog, int which) {
									AppUtils.showGoogleSearchIntent(MainActivity.this, data.get(which));
								}
							});
				}
				break;
			case REC_ALARM:
				String alarmString = data.get(0);
				// hh:45

				break;
		}
	}

	private void startAddOrEditEventActivity(EventResource event) {
		Intent intent = new Intent(this, AddOrEditEventActivity.class);
		if (event != null) {
			intent.putExtra("id", event.id);
			intent.putExtra("summary", event.title);
		}
		startActivityForResult(intent, RequestCode.REQUEST_ADD_OR_EDIT_EVENT);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case RequestCode.REQUEST_ADD_OR_EDIT_EVENT:
				if (resultCode == Activity.RESULT_OK) {
					EventResource event = new EventResource();
					event.title = data.getStringExtra(AddOrEditEventActivity.EXTRA_SUMMARY);
					event.content = data.getStringExtra(AddOrEditEventActivity.EXTRA_DESC);
					event.setStart(new Date(data.getLongExtra(AddOrEditEventActivity.EXTRA_START_TIME, 0)));
					event.setEnd(new Date(data.getLongExtra(AddOrEditEventActivity.EXTRA_END_TIME, 0)));
					String id = data.getStringExtra(AddOrEditEventActivity.EXTRA_ID);
					if (id == null) {
						// TODO add new event
					} else {
						event.id = id;
						// TODO update event
					}
				}
				break;
		}
	}
}
