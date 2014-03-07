package org.cnc.msrobot.fragment;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.cnc.msrobot.R;
import org.cnc.msrobot.provider.DbContract.TableEvent;
import org.cnc.msrobot.resource.EventResource;
import org.cnc.msrobot.utils.DateTimeFormater;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.antonyt.infiniteviewpager.InfinitePagerAdapter;
import com.roomorama.caldroid.CaldroidFragment;
import com.roomorama.caldroid.CaldroidListener;

/**
 * Calendar event activity
 * 
 * @author ThanhLCM
 */
public final class CalendarEventFragment extends BaseFragment implements LoaderCallbacks<Cursor> {
	private static final Level LOGGING_LEVEL = Level.OFF;
	public static final String EXTRA_TYPE = "TYPE"; //$NON-NLS-1$
	private static final int LOADER_GET_LIST_EVENT = 1;

	public static final String TAG = CalendarEventFragment.class.getSimpleName();
	public static final int NUMBER_OF_PAGES = 4;
	public static final int TYPE_MONTH = 0;
	public static final int TYPE_WEEK = 1;
	public static final int TYPE_DAY = 2;

	public static final int OFFSET = 1000;
	private static final int CONTEXT_EDIT = 0;
	private static final int CONTEXT_DELETE = 1;

	public ArrayAdapter<EventResource> adapter;
	private ArrayList<EventResource> listEvent = new ArrayList<EventResource>();
	private int mType;

	private ListView listView;
	private CaldroidFragment caldroidFragment;
	private ViewPager pagerWeek;
	private ViewPager pagerDay;
	private View mLayout;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		mLayout = inflater.inflate(R.layout.fragment_calendar_event, container, false);
		listView = (ListView) mLayout.findViewById(R.id.lvEvent);
		listView.setAdapter(adapter);

		initViewPager();

		// init calendar
		initCalendar(savedInstanceState);

		// register menu for list view
		registerForContextMenu(listView);

		changeViewType(mType);
		return mLayout;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// enable logging
		Logger.getLogger("com.google.api.client").setLevel(LOGGING_LEVEL);

		mType = getArguments().getInt(EXTRA_TYPE, TYPE_MONTH);

		// init adapter
		initAdapter();

		// init cursor loader
		initCursorLoader();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		if (caldroidFragment != null) {
			caldroidFragment.saveStatesToKey(outState, "CALDROID_SAVED_STATE");
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
		this.pagerWeek = (ViewPager) mLayout.findViewById(R.id.pagerWeek);
		this.pagerDay = (ViewPager) mLayout.findViewById(R.id.pagerDay);
		weekPagerAdapter = new WeekPagerAdapter(getChildFragmentManager(), listEvent);
		// Setup InfinitePagerAdapter to wrap around WeekPagerAdapter
		final DatePageChangeListener weekListener = new DatePageChangeListener(weekPagerAdapter);
		this.pagerWeek.setOnPageChangeListener(weekListener);
		InfinitePagerAdapter infinitePagerAdapter = new InfinitePagerAdapter(weekPagerAdapter);
		this.pagerWeek.setAdapter(infinitePagerAdapter);
		pagerWeek.setCurrentItem(OFFSET);

		dayPagerAdapter = new DayPagerAdapter(getChildFragmentManager(), listEvent);
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
				mLayout.findViewById(R.id.calendarFragment).setVisibility(View.VISIBLE);
				break;
			case TYPE_WEEK: {
				pagerWeek.setVisibility(View.VISIBLE);
				pagerDay.setVisibility(View.GONE);
				listView.setVisibility(View.GONE);
				mLayout.findViewById(R.id.calendarFragment).setVisibility(View.GONE);
				break;
			}
			case TYPE_DAY: {
				pagerWeek.setVisibility(View.GONE);
				pagerDay.setVisibility(View.VISIBLE);
				listView.setVisibility(View.GONE);
				mLayout.findViewById(R.id.calendarFragment).setVisibility(View.GONE);
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
		FragmentTransaction t = getChildFragmentManager().beginTransaction();
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
		adapter = new ArrayAdapter<EventResource>(getBaseActivity(), android.R.layout.simple_list_item_2,
				android.R.id.text1, new ArrayList<EventResource>()) {

			@Override
			public View getView(int position, View convertView, ViewGroup parent) {
				View view = super.getView(position, convertView, parent);
				EventResource event = getItem(position);
				ViewHolder holder = (ViewHolder) view.getTag();
				if (holder == null) {
					holder = new ViewHolder();
					holder.text1 = (TextView) view.findViewById(android.R.id.text1);
					holder.text2 = (TextView) view.findViewById(android.R.id.text2);
				}
				holder.text1.setText(event.title);
				if (event.start() == event.end()) {
					holder.text2.setText(getString(R.string.calendar_all_day));
				} else {
					String startTime, endTime = "";
					startTime = DateTimeFormater.timeFormater.format(event.start());
					endTime = DateTimeFormater.timeFormater.format(event.end());
					holder.text2.setText(startTime + " - " + endTime);
				}
				view.setTag(holder);
				return view;
			}

			class ViewHolder {
				TextView text1, text2;
			}
		};
	}

	private void loadEvent(Date date) {
		String currentDate = DateTimeFormater.compareFormater.format(date);
		adapter.clear();
		for (int i = 0; i < listEvent.size(); i++) {
			EventResource event = listEvent.get(i);
			String eventDate;
			eventDate = DateTimeFormater.compareFormater.format(event.start());
			if (currentDate.equals(eventDate)) {
				adapter.add(event);
			}
		}
		adapter.notifyDataSetChanged();
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
			switch (item.getItemId()) {
				case CONTEXT_EDIT:
					// startAddOrEditEventActivity(event);
					return true;
				case CONTEXT_DELETE:
					return true;
			}
		}
		return super.onContextItemSelected(item);
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

	protected void initCursorLoader() {
		getLoaderManager().initLoader(LOADER_GET_LIST_EVENT, null, this);
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle arg1) {
		switch (id) {
			case LOADER_GET_LIST_EVENT:
				return new CursorLoader(getBaseActivity(), TableEvent.CONTENT_URI, null, null, null, null);
		}
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		int id = loader.getId();
		switch (id) {
			case LOADER_GET_LIST_EVENT:
				listEvent.clear();
				if (cursor != null && cursor.moveToFirst()) {
					Calendar cal = Calendar.getInstance();
					Date currentDate = cal.getTime();
					do {
						EventResource event = new EventResource(cursor);
						listEvent.add(event);
						caldroidFragment.setBackgroundResourceForDate(R.color.blue, event.start());
						caldroidFragment.setTextColorForDate(R.color.white, event.start());
						caldroidFragment.refreshView();
					} while (cursor.moveToNext());
					if (mType == TYPE_MONTH) {
						loadEvent(currentDate);
					}
				}
				if (dayPagerAdapter != null) {
					dayPagerAdapter.setListEvent(listEvent);
				}
				if (weekPagerAdapter != null) {
					weekPagerAdapter.setListEvent(listEvent);
				}
				break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
	}
}
