package org.cnc.msrobot.activity;

import org.cnc.msrobot.MainApplication;
import org.cnc.msrobot.R;
import org.cnc.msrobot.fragment.BaseFragment;
import org.cnc.msrobot.fragment.CalendarEventFragment;
import org.cnc.msrobot.fragment.ClassicFragment;
import org.cnc.msrobot.fragment.HomeFragment;
import org.cnc.msrobot.module.Module;
import org.cnc.msrobot.provider.DbContract.TableContact;
import org.cnc.msrobot.resource.ContactResource;
import org.cnc.msrobot.resource.EmptyResource;
import org.cnc.msrobot.resource.EventResource;
import org.cnc.msrobot.resource.StaticResource;
import org.cnc.msrobot.utils.Actions;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.RequestCode;
import org.cnc.msrobot.utils.DialogUtils.OnConfirmClickListener;
import org.cnc.msrobot.utils.SpeechToText.SpeechToTextListener;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.text.TextUtils;
import android.util.SparseArray;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.android.volley.Response.ErrorListener;
import com.android.volley.Response.Listener;
import com.android.volley.VolleyError;

public class MainActivity extends BaseActivity implements LoaderCallbacks<Cursor>, OnClickListener,
		SpeechToTextListener {
	private static final int LOADER_GET_LIST_CONTACT = 1;
	private String[] mPlanetTitles;
	private DrawerLayout mDrawerLayout;
	private ActionBarDrawerToggle mDrawerToggle;
	private ListView mDrawerList;
	private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	protected BaseFragment fragment;
	SparseArray<Module> mRecModule = new SparseArray<Module>();
	private FrameLayout touchInterceptor;
	private FrameLayout rootViewGroup;

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

	Listener<EventResource[]> mRequestCreateEventListener = new Listener<EventResource[]>() {

		@Override
		public void onResponse(EventResource[] response) {
			if (response == null) {
				showCenterToast(R.string.msg_err_create_event);
			} else {
				showCenterToast(R.string.msg_info_create_event);
				// set reminder for alarm
				for (int i = 0; i < response.length; i++) {
					MainApplication.alarm.setReminder(getApplicationContext(), response[i]);
				}
			}
		}
	};

	ErrorListener mRequestCreateEventError = new ErrorListener() {
		@Override
		public void onErrorResponse(VolleyError error) {
			showCenterToast(R.string.msg_err_create_event);
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActionbar.setOnClickListener(this);
		setContentView(R.layout.activity_main);

		// set listener for Speech To Text
		getSpeechToText().setListener(this);

		touchInterceptor = new FrameLayout(this);
		touchInterceptor.setClickable(true); // otherwise clicks will fall through
		rootViewGroup = (FrameLayout) findViewById(R.id.content_frame);

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

		// init cursor loader
		initLoader();

		// get contact list
		getContactList();

		// check email config
		if (TextUtils.isEmpty(mSharePrefs.getGmailUsername())) {
			// show dialog to confirm setup email
			mDialog.showConfirmDialog(R.string.dialog_confirm_setup_email, new OnConfirmClickListener() {

				@Override
				public void onConfirmOkClick() {
					startActivityForResult(new Intent(MainActivity.this, EmailSetupActivity.class),
							RequestCode.REQUEST_EMAIL_SETUP);
				}

				@Override
				public void onConfirmCancelClick() {
					showCenterToast(R.string.msg_info_setup_email);
				}
			});
		}
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
	public boolean onMenuItemSelected(int featureId, MenuItem item) {
		// The action bar home/up action should open or close the drawer.
		// ActionBarDrawerToggle will take care of this.
		if (mDrawerToggle.onOptionsItemSelected(item)) { return true; }
		return super.onOptionsItemSelected(item);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (touchInterceptor.getParent() == null) {
			rootViewGroup.addView(touchInterceptor);
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		rootViewGroup.removeView(touchInterceptor);
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
		Bundle bundle = new Bundle();
		switch (position) {
			case 0:
				fragment = new ClassicFragment();
				break;
			case 1:
				fragment = new HomeFragment();
				break;
			case 2:
				fragment = new CalendarEventFragment();
				bundle.putInt(CalendarEventFragment.EXTRA_TYPE, CalendarEventFragment.TYPE_MONTH);
				break;
			case 3:
				fragment = new CalendarEventFragment();
				bundle.putInt(CalendarEventFragment.EXTRA_TYPE, CalendarEventFragment.TYPE_WEEK);
				break;
			case 4:
				fragment = new CalendarEventFragment();
				bundle.putInt(CalendarEventFragment.EXTRA_TYPE, CalendarEventFragment.TYPE_DAY);
				break;
			case 5:
				// setting
				startActivityForResult(new Intent(this, EmailSetupActivity.class), RequestCode.REQUEST_EMAIL_SETUP);
				return;
			case 6:
				// Logout
				showProgress();
				mRequestManager.request(Actions.ACTION_LOGOUT, null, mRequestLogoutlistener, mRequestLogoutError);
				return;
			case 7:
				// Exit
				mDialog.showConfirmDialog(R.string.dialog_confirm_exit, new OnConfirmClickListener() {

					@Override
					public void onConfirmOkClick() {
						finish();
					}

					@Override
					public void onConfirmCancelClick() {
					}
				});
				return;
			default:
				fragment = new ClassicFragment();
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

	/**
	 * Get contact list
	 */
	private void getContactList() {
		mRequestManager.request(Actions.ACTION_GET_LIST_CONTACT, null, null, null);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case RequestCode.REQUEST_ADD_OR_EDIT_EVENT:
				if (resultCode == Activity.RESULT_OK) {
					Bundle bundle = data.getExtras();
					String id = data.getStringExtra(Consts.PARAMS_ID);
					if (id == null) {
						mRequestManager.request(Actions.ACTION_CREATE_EVENT, bundle, mRequestCreateEventListener,
								mRequestCreateEventError);
					} else {
						// TODO update event
					}
				}
				break;
		// case RequestCode.REQUEST_RECOGNIZE:
		// if (resultCode == Activity.RESULT_OK) {
		// ArrayList<String> lstData = new ArrayList<String>();
		// lstData.add(data.getStringExtra(RecognizeActivity.EXTRA_TEXT));
		// Module recModule = mRecModule.get(mModuleId);
		// if (recModule == null) return;
		// recModule.getListener().onRecoginze(lstData);
		// }
		// break;
		}
	}

	@Override
	public void onBackPressed() {
		if (fragment instanceof HomeFragment) {
			if (((HomeFragment) fragment).checkBackPress()) {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		} else if (fragment instanceof ClassicFragment) {
			if (((ClassicFragment) fragment).checkBackPress()) {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		} else {
			mDrawerLayout.openDrawer(mDrawerList);
		}
	}

	private void initLoader() {
		getSupportLoaderManager().initLoader(LOADER_GET_LIST_CONTACT, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
		switch (id) {
			case LOADER_GET_LIST_CONTACT:
				return new CursorLoader(this, TableContact.CONTENT_URI, null, null, null, null);
			default:
				break;
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		switch (loader.getId()) {
			case LOADER_GET_LIST_CONTACT:
				if (cursor != null) {
					if (cursor.moveToFirst()) {
						StaticResource.listContact.clear();
						do {
							ContactResource contact = new ContactResource(cursor);
							StaticResource.listContact.add(contact);
						} while (cursor.moveToNext());
					}
				}
				break;
			default:
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}

	@Override
	public void onClick(View v) {
	}

	public void addChatListView(String text, int pos) {
		if (fragment != null && fragment instanceof HomeFragment && fragment.isAdded()) {
			((HomeFragment) fragment).addChatListView(text, pos);
		}
	}

	public void changeSmsItem(int count) {
		if (fragment != null && fragment instanceof HomeFragment && fragment.isAdded()) {
			((HomeFragment) fragment).changeSmsItem(count);
		} else if (fragment != null && fragment instanceof ClassicFragment && fragment.isAdded()) {
			((ClassicFragment) fragment).changeSmsItem(count);
		}
	}

	public void changeEmailLoading() {
		if (fragment != null && fragment instanceof HomeFragment && fragment.isAdded()) {
			((HomeFragment) fragment).changeEmailLoading();
		} else if (fragment != null && fragment instanceof ClassicFragment && fragment.isAdded()) {
			((ClassicFragment) fragment).changeEmailLoading();
		}
	}

	public void changeEmailItem(int count, boolean readFail) {
		if (fragment != null && fragment instanceof HomeFragment && fragment.isAdded()) {
			((HomeFragment) fragment).changeEmailItem(count, readFail);
		} else if (fragment != null && fragment instanceof ClassicFragment && fragment.isAdded()) {
			((ClassicFragment) fragment).changeEmailItem(count, readFail);
		}
	}

	@Override
	public void onSpeechStart() {
		getCusomActionBar().showRecAnimation();
	}

	@Override
	public void onSpeechStop() {
		getCusomActionBar().hideRecAnimation();
	}
}
