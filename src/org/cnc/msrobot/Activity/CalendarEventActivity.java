package org.cnc.msrobot.activity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;

import org.cnc.msrobot.R;
import org.cnc.msrobot.fragment.DayPagerAdapter;
import org.cnc.msrobot.fragment.ScheduleFragment;
import org.cnc.msrobot.fragment.SchedulePagerAdapter;
import org.cnc.msrobot.fragment.WeekPagerAdapter;
import org.cnc.msrobot.task.calendar.AsyncInsertEvent;
import org.cnc.msrobot.task.calendar.AsyncLoadCalendars;
import org.cnc.msrobot.utils.Logger;

import android.accounts.AccountManager;
import android.app.ActionBar;
import android.app.ActionBar.OnNavigationListener;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.util.DateTime;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Event;
import com.google.api.services.calendar.model.EventDateTime;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

/**
 * Calendar event activity
 * 
 * @author ThanhLCM
 */
public final class CalendarEventActivity extends BaseActivity {
	// private static final Level LOGGING_LEVEL = Level.OFF;

	private static final String PREF_ACCOUNT_NAME = "accountName";
	public static final String EXTRA_TYPE = "TYPE"; //$NON-NLS-1$

	public static final String TAG = "CalendarEventActivity";
	public static final int NUMBER_OF_PAGES = 4;
	public static final int TYPE_MONTH = 0;
	public static final int TYPE_WEEK = 1;
	public static final int TYPE_DAY = 2;

	public static final int OFFSET = 1000;
	private static final int CONTEXT_EDIT = 0;
	private static final int CONTEXT_DELETE = 1;

	static final int REQUEST_GOOGLE_PLAY_SERVICES = 0;
	public static final int REQUEST_AUTHORIZATION = 1;
	static final int REQUEST_ACCOUNT_PICKER = 2;
	private final static int ADD_OR_EDIT_EVENT_REQUEST = 3;

	final HttpTransport transport = AndroidHttp.newCompatibleTransport();

	final JsonFactory jsonFactory = GsonFactory.getDefaultInstance();

	GoogleAccountCredential credential;

	public ArrayAdapter<Event> adapter;
	public ArrayList<Event> listEvent = new ArrayList<Event>();

	public com.google.api.services.calendar.Calendar client;

	public int numAsyncTasks;
	public String calendarId;
	private int mType;

	private ListView listView;
	private CaldroidFragment caldroidFragment;
	private ViewPager pagerWeek;
	private ViewPager pagerDay;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// enable logging
		// Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);

		// view and menu
		setContentView(R.layout.activity_calendar_event);
		listView = (ListView) findViewById(R.id.lvEvent);

		// init action bar
		getActionBar().setTitle("");
		getActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
		ArrayAdapter<CharSequence> list = ArrayAdapter.createFromResource(this, R.array.array_action_bar_dropdown,
				android.R.layout.simple_dropdown_item_1line);
		getActionBar().setListNavigationCallbacks(list, new OnNavigationListener() {
			@Override
			public boolean onNavigationItemSelected(int itemPosition, long itemId) {
				changeViewType(itemPosition);
				return false;
			}
		});

		initViewPager();

		// init calendar
		initCalendar(savedInstanceState);

		// init adapter
		initAdapter();

		// register menu for list view
		registerForContextMenu(listView);

		// Google Accounts
		credential = GoogleAccountCredential.usingOAuth2(this, Collections.singleton(CalendarScopes.CALENDAR));
		SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
		credential.setSelectedAccountName(settings.getString(PREF_ACCOUNT_NAME, null));
		// Calendar client
		client = new com.google.api.services.calendar.Calendar.Builder(transport, jsonFactory, credential)
				.setApplicationName("MsRobot").build();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (caldroidFragment != null) {
			caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
		}
	}

	public void showGooglePlayServicesAvailabilityErrorDialog(final int connectionStatusCode) {
		runOnUiThread(new Runnable() {
			public void run() {
				Dialog dialog = GooglePlayServicesUtil.getErrorDialog(connectionStatusCode, CalendarEventActivity.this,
						REQUEST_GOOGLE_PLAY_SERVICES);
				dialog.show();
			}
		});
	}

	private static SimpleDateFormat timeFormater = new SimpleDateFormat("HH:mm", Locale.US);
	public SimpleDateFormat dateFormater = new SimpleDateFormat("yyyyMMdd", Locale.US);

	public void refreshView() {
		if (listEvent.size() > 0) {
			Calendar cal = Calendar.getInstance();
			Date currentDate = cal.getTime();
			for (int i = 0; i < listEvent.size(); i++) {
				Event event = listEvent.get(i);
				Date date;
				if (event.getStart().getDateTime() != null) {
					date = new Date(event.getStart().getDateTime().getValue());
				} else {
					date = new Date(event.getStart().getDate().getValue());
				}
				caldroidFragment.setBackgroundResourceForDate(R.color.blue, date);
				caldroidFragment.setTextColorForDate(R.color.white, date);
				caldroidFragment.refreshView();
			}
			loadEvent(currentDate);
		}
		if (dayPagerAdapter != null) {
			dayPagerAdapter.setListEvent(listEvent);
		}
		if (weekPagerAdapter != null) {
			weekPagerAdapter.setListEvent(listEvent);
		}
	}

	public void showPrevious() {
		if (mType == TYPE_WEEK) {
			if (this.pagerWeek.getCurrentItem() > 0) {
				this.pagerWeek.setCurrentItem(this.pagerWeek.getCurrentItem() - 1, true);
			}
		} else if (mType == TYPE_DAY) {
			if (this.pagerDay.getCurrentItem() > 0) {
				this.pagerDay.setCurrentItem(this.pagerDay.getCurrentItem() - 1, true);
			}
		}
	}

	public void showNext() {
		if (mType == TYPE_WEEK) {
			if (this.pagerWeek.getCurrentItem() < Integer.MAX_VALUE) {
				this.pagerWeek.setCurrentItem(this.pagerWeek.getCurrentItem() + 1, true);
			}
		} else if (mType == TYPE_DAY) {
			if (this.pagerDay.getCurrentItem() < Integer.MAX_VALUE) {
				this.pagerDay.setCurrentItem(this.pagerDay.getCurrentItem() + 1, true);
			}
		}
	}

	WeekPagerAdapter weekPagerAdapter;
	DayPagerAdapter dayPagerAdapter;

	private void initViewPager() {
		this.pagerWeek = (ViewPager) this.findViewById(R.id.pagerWeek);
		this.pagerDay = (ViewPager) this.findViewById(R.id.pagerDay);
		weekPagerAdapter = new WeekPagerAdapter(getSupportFragmentManager(), listEvent);
		// Setup InfinitePagerAdapter to wrap around WeekPagerAdapter
		final DatePageChangeListener weekListener = new DatePageChangeListener(weekPagerAdapter);
		this.pagerWeek.setOnPageChangeListener(weekListener);
		InfinitePagerAdapter infinitePagerAdapter = new InfinitePagerAdapter(weekPagerAdapter);
		this.pagerWeek.setAdapter(infinitePagerAdapter);
		pagerWeek.setCurrentItem(OFFSET);

		dayPagerAdapter = new DayPagerAdapter(getSupportFragmentManager(), listEvent);
		// Setup InfinitePagerAdapter to wrap around DayPagerAdapter
		final DatePageChangeListener dayListener = new DatePageChangeListener(dayPagerAdapter);
		this.pagerDay.setOnPageChangeListener(dayListener);
		InfinitePagerAdapter infiniteDayAdapter = new InfinitePagerAdapter(dayPagerAdapter);
		this.pagerDay.setAdapter(infiniteDayAdapter);
		pagerDay.setCurrentItem(OFFSET);
	}

	private void changeViewType(int type) {
		mType = type;
		switch (mType) {
			case TYPE_MONTH:
				pagerWeek.setVisibility(View.GONE);
				pagerDay.setVisibility(View.GONE);
				listView.setVisibility(View.VISIBLE);
				findViewById(R.id.calendarFragment).setVisibility(View.VISIBLE);
				break;
			case TYPE_WEEK: {
				pagerWeek.setVisibility(View.VISIBLE);
				pagerDay.setVisibility(View.GONE);
				listView.setVisibility(View.GONE);
				findViewById(R.id.calendarFragment).setVisibility(View.GONE);
				break;
			}
			case TYPE_DAY: {
				pagerWeek.setVisibility(View.GONE);
				pagerDay.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
				findViewById(R.id.calendarFragment).setVisibility(View.GONE);
				break;
			}
		}
	}

	private void initCalendar(Bundle savedInstanceState) {
		// Setup caldroid fragment
		// **** If you want normal CaldroidFragment, use below line ****
		caldroidFragment = new CaldroidFragment();

		// Setup arguments
		// If Activity is created after rotation
		if (savedInstanceState != null) {
			caldroidFragment.restoreStatesFromKey(savedInstanceState, "CALDROID_SAVED_STATE");
		}
		// If activity is created from fresh
		else {
			Bundle args = new Bundle();
			Calendar cal = Calendar.getInstance();
			args.putInt(CaldroidFragment.MONTH, cal.get(Calendar.MONTH) + 1);
			args.putInt(CaldroidFragment.YEAR, cal.get(Calendar.YEAR));
			args.putBoolean(CaldroidFragment.ENABLE_SWIPE, true);
			args.putBoolean(CaldroidFragment.SIX_WEEKS_IN_CALENDAR, true);

			// Uncomment this to customize startDayOfWeek
			// args.putInt(CaldroidFragment.START_DAY_OF_WEEK,
			// CaldroidFragment.TUESDAY); // Tuesday
			caldroidFragment.setArguments(args);
		}
		// Attach to the activity
		FragmentTransaction t = getSupportFragmentManager().beginTransaction();
		t.replace(R.id.calendarFragment, caldroidFragment);
		t.commit();

		// Setup listener
		final CaldroidListener listener = new CaldroidListener() {

			@Override
			public void onSelectDate(Date date, View view) {
				loadEvent(date);
			}

			@Override
			public void onChangeMonth(int month, int year) {
			}

			@Override
			public void onLongClickDate(Date date, View view) {
			}

			@Override
			public void onCaldroidViewCreated() {
			}
		};
		// Setup Caldroid
		caldroidFragment.setCaldroidListener(listener);
	}

	private void initAdapter() {
		adapter = new ArrayAdapter<Event>(this, android.R.layout.simple_list_item_2, android.R.id.text1,
				new ArrayList<Event>()) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				Event event = getItem(position);
				ViewHolder holder = (ViewHolder) view.getTag();
				if (holder == null) {
					holder = new ViewHolder();
					holder.text1 = (TextView) view.findViewById(android.R.id.text1);
					holder.text2 = (TextView) view.findViewById(android.R.id.text2);
				}
				holder.text1.setText(event.getSummary());
				if (event.getStart().isEmpty()) {
					holder.text2.setText(getString(R.string.calendar_all_day));
				} else {
					String startTime, endTime = "";
					if (event.getStart().getDateTime() != null) {
						startTime = timeFormater.format(new Date(event.getStart().getDateTime().getValue()));
						if (event.getEnd().getDateTime() != null) {
							endTime = timeFormater.format(new Date(event.getEnd().getDateTime().getValue()));
						}
						holder.text2.setText(startTime + " - " + endTime);
					} else {
						startTime = getString(R.string.calendar_all_day);
						holder.text2.setText(startTime);
					}
				}
				view.setTag(holder);
				return view;
			}

			class ViewHolder {
				TextView text1, text2;
			}
		};
		listView.setAdapter(adapter);
	}

	private void loadEvent(Date date) {
		String currentDate = dateFormater.format(date);
		adapter.clear();
		for (int i = 0; i < listEvent.size(); i++) {
			Event event = listEvent.get(i);
			String eventDate;
			if (event.getStart().getDateTime() != null) {
				eventDate = dateFormater.format(new Date(event.getStart().getDateTime().getValue()));
			} else {
				eventDate = dateFormater.format(new Date(event.getStart().getDate().getValue()));
			}
			if (currentDate.equals(eventDate)) {
				adapter.add(event);
			}
		}
		adapter.notifyDataSetChanged();
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (checkGooglePlayServicesAvailable()) {
			haveGooglePlayServices();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
			case REQUEST_GOOGLE_PLAY_SERVICES:
				if (resultCode == Activity.RESULT_OK) {
					haveGooglePlayServices();
				} else {
					checkGooglePlayServicesAvailable();
				}
				break;
			case REQUEST_AUTHORIZATION:
				if (resultCode == Activity.RESULT_OK) {
					AsyncLoadCalendars.run(this);
				} else {
					// chooseAccount();
					// finish();
				}
				break;
			case REQUEST_ACCOUNT_PICKER:
				if (resultCode == Activity.RESULT_OK && data != null && data.getExtras() != null) {
					String accountName = data.getExtras().getString(AccountManager.KEY_ACCOUNT_NAME);
					if (accountName != null) {
						credential.setSelectedAccountName(accountName);
						SharedPreferences settings = getPreferences(Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = settings.edit();
						editor.putString(PREF_ACCOUNT_NAME, accountName);
						editor.commit();
						AsyncLoadCalendars.run(this);
					}
				}
				break;
			case ADD_OR_EDIT_EVENT_REQUEST:
				if (resultCode == Activity.RESULT_OK) {
					Event event = new Event();
					event.setSummary(data.getStringExtra("summary"));
					event.setCreated(new DateTime(Calendar.getInstance().getTime()));
					EventDateTime eStart = new EventDateTime();
					eStart.setDateTime(new DateTime(data.getLongExtra("start", 0)));
					EventDateTime eEnd = new EventDateTime();
					eEnd.setDateTime(new DateTime(data.getLongExtra("end", 0)));
					event.setStart(eStart);
					event.setEnd(eEnd);
					String id = data.getStringExtra("id");
					if (id == null) {
						new AsyncInsertEvent(this, event).execute();
					} else {
						event.setId(id);
						// new AsyncUpdateCalendar(this, id, event).execute();
					}
				}
				break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main_menu, menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case R.id.menu_refresh:
				AsyncLoadCalendars.run(this);
				break;
			case R.id.menu_accounts:
				chooseAccount();
				return true;
			case R.id.menu_add:
				startAddOrEditEventActivity(null);
				return true;
		}
		return super.onOptionsItemSelected(item);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		menu.add(0, CONTEXT_EDIT, 0, R.string.edit_event);
		menu.add(0, CONTEXT_DELETE, 0, R.string.delete);
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		int eventIndex = (int) info.id;
		if (eventIndex < adapter.getCount()) {
			final Event event = adapter.getItem(eventIndex);
			switch (item.getItemId()) {
				case CONTEXT_EDIT:
					startAddOrEditEventActivity(event);
					return true;
				case CONTEXT_DELETE:
					new AlertDialog.Builder(this).setTitle(R.string.delete_title).setMessage(event.getSummary())
							.setCancelable(false)
							.setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {

								public void onClick(DialogInterface dialog, int which) {
									// new AsyncDeleteCalendar(CalendarEventActivity.this, calendarInfo).execute();
								}
							}).setNegativeButton(R.string.no, null).create().show();
					return true;
					// case CONTEXT_BATCH_ADD:
					// List<Calendar> calendars = new ArrayList<Calendar>();
					// for (int i = 0; i < 3; i++) {
					// Calendar cal = new Calendar();
					// cal.setSummary(calendarInfo.summary + " [" + (i + 1) + "]");
					// calendars.add(cal);
					// }
					// new AsyncBatchInsertCalendars(this, calendars).execute();
					// return true;
			}
		}
		return super.onContextItemSelected(item);
	}

	/** Check that Google Play services APK is installed and up to date. */
	private boolean checkGooglePlayServicesAvailable() {
		final int connectionStatusCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
		if (GooglePlayServicesUtil.isUserRecoverableError(connectionStatusCode)) {
			showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode);
			return false;
		}
		return true;
	}

	private void haveGooglePlayServices() {
		// check if there is already an account selected
		if (credential.getSelectedAccountName() == null) {
			// ask user to choose account
			chooseAccount();
		} else {
			// load calendars
			AsyncLoadCalendars.run(this);
		}
	}

	private void chooseAccount() {
		startActivityForResult(credential.newChooseAccountIntent(), REQUEST_ACCOUNT_PICKER);
	}

	private void startAddOrEditEventActivity(Event event) {
		Intent intent = new Intent(this, AddOrEditEventActivity.class);
		if (event != null) {
			intent.putExtra("id", event.getId());
			intent.putExtra("summary", event.getSummary());
		}
		startActivityForResult(intent, ADD_OR_EDIT_EVENT_REQUEST);
	}

	public class DatePageChangeListener implements OnPageChangeListener {
		private SchedulePagerAdapter adapter;
		private int currentPos = OFFSET;
		private Date currentDate;

		public DatePageChangeListener(SchedulePagerAdapter adapter) {
			this.adapter = adapter;
		}

		@Override
		public void onPageScrollStateChanged(int position) {
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
		}

		private void updateFragment(int position, boolean saveCurrent) {
			ScheduleFragment fragment = (ScheduleFragment) adapter.getItem(position % NUMBER_OF_PAGES);
			final Calendar calendar = Calendar.getInstance();
			if (currentDate != null) {
				calendar.setTime(currentDate);
			}
			// Swipe right
			if (position > currentPos) {
				// Update current date time to next date
				calendar.add(Calendar.DATE, adapter.getInterval());
			}
			// Swipe left
			else if (position < currentPos) {
				// Update current date time to previous date
				calendar.add(Calendar.DATE, -adapter.getInterval());
			}

			// add event
			if (saveCurrent) {
				currentPos = position;
				currentDate = calendar.getTime();
			} else {
				fragment.setCurrentDate(calendar.getTime());
			}
			fragment.refreshView();
		}

		@Override
		public void onPageSelected(int position) {
			updateFragment(position, true);
			// update previous fragment
			updateFragment(position - 1, false);
			// update next fragment
			updateFragment(position + 1, false);
		}
	}
}
