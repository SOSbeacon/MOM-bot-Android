package org.cnc.msrobot.resource;

import java.util.ArrayList;

import android.content.Context;

public final class ItemListFunction {
	public static final int TYPE_COUNT = 3;
	public static final int TYPE_FUNCTION = 0;
	public static final int TYPE_EVENT = 1;
	public static final int TYPE_FUNCTION_CLASSIC = 2;
	public static final int FUNCTION_READ_SMS = 1;
	public static final int FUNCTION_READ_EMAIL = 2;
	public static final int FUNCTION_READ_REMINDER = 3;
	public static final int FUNCTION_SENT_MESSAGE = 4;
	public static final int FUNCTION_CHECK_WEATHER = 9;
	public static final int FUNCTION_CHECK_NEWS = 10;
	public static final int FUNCTION_SPEAK_TIME = 11;
	public static final int FUNCTION_SET_ALARM = 12;
	public static final int FUNCTION_CHECK_MY_CALENDAR = 13;
	public static final int FUNCTION_CHECK_MY_MEDICINES = 14;
	public static final int FUNCTION_SEARCH = 15;
	public static final int FUNCTION_SETUP_EMAIL_ACCOUNT = 16;
	public static final int FUNCTION_BACK_TO_MAIN = 17;
	public static final int FUNCTION_BACK_TO_COMMAND= 18;
	public static final int FUNCTION_SET_REMINDER = 19;
	public static final int FUNCTION_GROUP_COMUNICATION = 22;
	public static final int FUNCTION_GROUP_INFORMATION = 23;
	public static final int FUNCTION_GROUP_EMERGENCY = 24;
	public static final int FUNCTION_GROUP_ADMIN = 25;
	public static final int FUNCTION_COMMAND = 26;
	public int iconResId;
	public int colorResId;
	public String iconUrl;
	public String desc;
	public ArrayList<TaskTime> times;
	public int type = TYPE_FUNCTION;
	/**
	 * item id for click, using those const above
	 */
	public int itemClickId;
	/**
	 * function textview click, using thos const above
	 */
	public int notifyCount;

	public ItemListFunction(final Builder builder) {
		this.iconResId = builder.iconResId;
		this.desc = builder.desc;
		this.itemClickId = builder.itemClickId;
		this.notifyCount = builder.notifyCount;
		this.type = builder.type;
		this.times = builder.times;
		this.colorResId = builder.colorResId;
	}

	public ItemListFunction() {
	}

	public static class Builder {
		public Builder(Context context) {
			this.context = context;
		}

		/**
		 * set item start time (use for type event)
		 * 
		 * @param title
		 * @return
		 */
		public Builder setTimes(ArrayList<TaskTime> times) {
			this.times = times;
			return this;
		}

		/**
		 * set item type, example function, event, ...
		 * 
		 * @param type
		 * @return
		 */
		public Builder setType(int type) {
			this.type = type;
			return this;
		}

		/**
		 * set icon resource id
		 * 
		 * @param iconResId
		 * @return
		 */
		public Builder setIconResId(int iconResId) {
			this.iconResId = iconResId;
			return this;
		}

		/**
		 * set description string resource id
		 * 
		 * @param descResId
		 * @return
		 */
		public Builder setDescResId(int descResId) {
			this.desc = context.getString(descResId);
			return this;
		}

		/**
		 * set description string
		 * 
		 * @param desc
		 * @return
		 */
		public Builder setDesc(String desc) {
			this.desc = desc;
			return this;
		}

		/**
		 * set item click id, use consts above. Example FUNCTION_READ_SMS
		 * 
		 * @param itemClickId
		 * @return
		 */
		public Builder setItemClickId(int itemClickId) {
			this.itemClickId = itemClickId;
			return this;
		}

		/**
		 * set notification count
		 * 
		 * @param notifyCount
		 * @return
		 */
		public Builder setNotifyCount(int notifyCount) {
			this.notifyCount = notifyCount;
			return this;
		}

		public ItemListFunction build() {
			return new ItemListFunction(this);
		}

		public Builder setColorResId(int resId) {
			this.colorResId = resId;
			return this;
		}

		int iconResId;
		String desc;
		/**
		 * item id for click, using those const above
		 */
		int itemClickId;
		/**
		 * function textview click, using thos const above
		 */
		int notifyCount;
		int type;
		int colorResId;
		ArrayList<TaskTime> times;
		Context context;
	}
}
