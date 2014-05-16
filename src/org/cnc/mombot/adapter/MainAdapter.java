package org.cnc.mombot.adapter;

import java.util.ArrayList;

import org.cnc.mombot.R;
import org.cnc.mombot.activity.BaseActivity;
import org.cnc.mombot.activity.CalendarActivity;
import org.cnc.mombot.activity.EmailSetupActivity;
import org.cnc.mombot.activity.ReadEmailSmsActivity;
import org.cnc.mombot.activity.RssActivity;
import org.cnc.mombot.activity.WeatherActivity;
import org.cnc.mombot.module.Module;
import org.cnc.mombot.module.ModuleManager;
import org.cnc.mombot.requestmanager.RequestManager;
import org.cnc.mombot.resource.ItemListFunction;
import org.cnc.mombot.utils.Actions;
import org.cnc.mombot.utils.AppUtils;
import org.cnc.mombot.utils.Consts;
import org.cnc.mombot.utils.DateTimeFormater;
import org.cnc.mombot.utils.ListenToPhoneState;
import org.cnc.mombot.utils.SharePrefs;
import org.cnc.mombot.utils.Consts.RequestCode;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

/**
 * @author cnc Adapter for list main
 */
public class MainAdapter extends ArrayAdapter<ItemListFunction> implements OnClickListener {
	private LayoutInflater mInfalter;
	private BaseActivity activity;
	private OnFunctionDoListener listener;
	private ImageLoader mImageLoader = ImageLoader.getInstance();
	private static final DisplayImageOptions imageDisplayOptions = new DisplayImageOptions.Builder()
			.bitmapConfig(Bitmap.Config.RGB_565).imageScaleType(ImageScaleType.IN_SAMPLE_INT)
			.resetViewBeforeLoading(true).cacheInMemory(true).cacheOnDisc(true).build();

	public MainAdapter(BaseActivity activity, ArrayList<ItemListFunction> list, OnFunctionDoListener listener) {
		super(activity, 0, list);
		this.listener = listener;
		this.activity = activity;
		mInfalter = LayoutInflater.from(activity);
	}

	@Override
	public int getViewTypeCount() {
		return ItemListFunction.TYPE_COUNT;
	}

	@Override
	public int getItemViewType(int position) {
		return getItem(position).type;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ItemListFunction item = getItem(position);
		ViewHolder holder;
		if (convertView == null) {
			holder = new ViewHolder();
			if (getItemViewType(position) == ItemListFunction.TYPE_FUNCTION) {
				convertView = mInfalter.inflate(R.layout.item_list_main_function, parent, false);
				holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
				holder.tvNotifyCount = (TextView) convertView.findViewById(R.id.tvNotifyCount);
				holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
			} else if (getItemViewType(position) == ItemListFunction.TYPE_FUNCTION_CLASSIC) {
				convertView = mInfalter.inflate(R.layout.item_list_main_function_classic, parent, false);
				holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
				holder.tvNotifyCount = (TextView) convertView.findViewById(R.id.tvNotifyCount);
				holder.imgIcon = (ImageView) convertView.findViewById(R.id.imgIcon);
			} else {
				convertView = mInfalter.inflate(R.layout.item_list_main_event, parent, false);
				holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
				holder.tvDate = (TextView) convertView.findViewById(R.id.tvDate);
			}
			convertView.setOnClickListener(this);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		holder.item = item;
		if (getItemViewType(position) == ItemListFunction.TYPE_FUNCTION
				|| getItemViewType(position) == ItemListFunction.TYPE_FUNCTION_CLASSIC) {
			if (item.colorResId != 0) {
				holder.tvDesc.setTextColor(getContext().getResources().getColor(item.colorResId));
			}
			if (item.iconResId != 0) {
				holder.imgIcon.setImageResource(item.iconResId);
				holder.imgIcon.setVisibility(View.VISIBLE);
			} else {
				holder.imgIcon.setVisibility(View.GONE);
			}
			if (!TextUtils.isEmpty(item.iconUrl)) {
				mImageLoader.displayImage(item.iconUrl, holder.imgIcon, imageDisplayOptions);
				holder.imgIcon.setVisibility(View.VISIBLE);
			}
			if (item.notifyCount > 0) {
				holder.tvNotifyCount.setText(item.notifyCount + "");
				holder.tvNotifyCount.setVisibility(View.VISIBLE);
			} else {
				holder.tvNotifyCount.setVisibility(View.GONE);
			}
			holder.tvDesc.setText(item.desc);
		} else {
			String date = DateTimeFormater.dateFormater.format(item.times.get(0).start);
			holder.tvDate.setText(date);
			String strTime = "";
			String time = "";
			for (int i = 0; i < item.times.size(); i++) {
				time = DateTimeFormater.time24hFormater.format(item.times.get(i).start);
				strTime += "\n" + time;
				time = DateTimeFormater.time24hFormater.format(item.times.get(i).end);
				strTime += " - " + time + ": " + item.times.get(i).desc;

			}
			// remove character new line
			if (strTime.length() > 0) {
				strTime = strTime.substring(2);
			}
			holder.tvDesc.setText(strTime);
		}
		convertView.setTag(holder);
		return convertView;
	}

	public class ViewHolder {
		TextView tvDesc, tvNotifyCount, tvDate;
		ImageView imgIcon;
		ItemListFunction item;
	}

	@Override
	public void onClick(View v) {
		// first, stop speech
		ViewHolder holder = (ViewHolder) v.getTag();
		switch (holder.item.itemClickId) {
			case ItemListFunction.FUNCTION_READ_SMS: {
				Intent intent = new Intent(activity, ReadEmailSmsActivity.class);
				intent.putExtra(ReadEmailSmsActivity.EXTRA_TYPE, ReadEmailSmsActivity.TYPE_SENT_SMS);
				activity.startActivity(intent);
				break;
			}
			case ItemListFunction.FUNCTION_READ_EMAIL: {
				Intent intent = new Intent(activity, ReadEmailSmsActivity.class);
				intent.putExtra(ReadEmailSmsActivity.EXTRA_TYPE, ReadEmailSmsActivity.TYPE_SENT_EMAIL);
				activity.startActivity(intent);
				break;
			}
			case ItemListFunction.FUNCTION_SENT_MESSAGE: {
				ModuleManager.getInstance().runModule(Module.MODULE_SEND_MESSAGE);
				break;
			}
			case ItemListFunction.FUNCTION_CHECK_WEATHER:
				activity.startActivity(new Intent(getContext(), WeatherActivity.class));
				break;
			case ItemListFunction.FUNCTION_CHECK_NEWS:
				activity.startActivity(new Intent(getContext(), RssActivity.class));
				break;
			case ItemListFunction.FUNCTION_SEARCH:
				ModuleManager.getInstance().runModule(Module.MODULE_SEARCH);
				break;
			case ItemListFunction.FUNCTION_SPEAK_TIME:
				activity.getTextToSpeech().speak(AppUtils.getCurrentTimeForSpeech(getContext()),
						TextToSpeech.QUEUE_FLUSH);
				break;
			case ItemListFunction.FUNCTION_CHECK_MY_CALENDAR:
				activity.startActivity(new Intent(getContext(), CalendarActivity.class));
				break;
			case ItemListFunction.FUNCTION_SETUP_EMAIL_ACCOUNT:
				activity.startActivityForResult(new Intent(getContext(), EmailSetupActivity.class),
						RequestCode.REQUEST_EMAIL_SETUP);
				break;
			case ItemListFunction.FUNCTION_GROUP_EMERGENCY:
				startCallAndRecording();
				break;
		}
		if (listener != null) {
			listener.doFunction(holder.item.itemClickId);
		}
	}

	private void startCallAndRecording() {
		try {
			// get current location
			Location location = SharePrefs.getInstance().getCurrentLocation();
			// set long, lat in bundle
			Bundle bundle = new Bundle();
			if (location != null) {
				bundle.putString(Consts.PARAMS_MESSAGE_LAT, location.getLatitude() + "");
				bundle.putString(Consts.PARAMS_MESSAGE_LON, location.getLongitude() + "");
			}
			// request create emergency
			RequestManager.getInstance().request(Actions.ACTION_CREATE_EMERGENCY, bundle, null, null);

			// start dial emergency number
			Intent intent = new Intent(Intent.ACTION_CALL);
			intent.setData(Uri.parse("tel:911"));
			activity.startActivity(intent);

			TelephonyManager tManager = (TelephonyManager) activity.getSystemService(Context.TELEPHONY_SERVICE);
			ListenToPhoneState listener = new ListenToPhoneState(activity);
			tManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
		} catch (Exception activityException) {
			activityException.printStackTrace();
			activity.showCenterToast("Can't make phone call");
		}
	}

	public interface OnFunctionDoListener {
		void doFunction(int id);
	}
}
