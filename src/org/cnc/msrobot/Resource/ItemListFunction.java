package org.cnc.msrobot.resource;

import android.content.Context;

public final class ItemListFunction {
	public static final int FUNCTION_READ_SMS = 0;
	public static final int FUNCTION_READ_EMAIL = 1;
	public static final int FUNCTION_READ_REMINDER = 2;
	public static final int FUNCTION_SENT_TEXT_SMS = 3;
	public static final int FUNCTION_SENT_PICTURE_MMS = 4;
	public static final int FUNCTION_SENT_TEXT_EMAIL = 5;
	public static final int FUNCTION_SENT_PICTURE_EMAIL = 6;
	public static final int FUNCTION_SENT_PICTURE = 7;
	public static final int FUNCTION_CHECK_WEATHER = 8;
	public static final int FUNCTION_CHECK_NEWS = 9;
	public static final int FUNCTION_SPEAK_TIME = 10;
	public static final int FUNCTION_SET_ALARM = 11;
	public static final int FUNCTION_CHECK_MY_CALENDAR = 12;
	public static final int FUNCTION_CHECK_MY_MEDICINES = 13;
	public static final int FUNCTION_SEARCH = 14;
	public int iconResId;
	public String iconUrl;
	public String desc;
	/**
	 * item id for click, using those const above
	 */
	public int itemClickId;
	/**
	 * function textview click, using thos const above
	 */
	public int function1ClickId;
	public int function2ClickId;
	public int function1TextResId;
	public int function2TextResId;
	public int notifyCount;

	public ItemListFunction(final Builder builder) {
		this.iconResId = builder.iconResId;
		this.desc = builder.context.getString(builder.descResId);
		this.itemClickId = builder.itemClickId;
		this.function1ClickId = builder.function1ClickId;
		this.function1TextResId = builder.function1TextResId;
		this.function2ClickId = builder.function2ClickId;
		this.function2TextResId = builder.function2TextResId;
		this.notifyCount = builder.notifyCount;
	}

	public ItemListFunction() {
	}

	public static class Builder {
		public Builder(Context context) {
			this.context = context;
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
			this.descResId = descResId;
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
		 * set function 1 click id, use consts above. Example FUNCTION_READ_SMS
		 * 
		 * @param function1ClickId
		 * @return
		 */
		public Builder setFunction1ClickId(int function1ClickId) {
			this.function1ClickId = function1ClickId;
			return this;
		}

		/**
		 * set function 2 click id, use consts above. Example FUNCTION_READ_SMS
		 * 
		 * @param function2ClickId
		 * @return
		 */
		public Builder setFunction2ClickId(int function2ClickId) {
			this.function2ClickId = function2ClickId;
			return this;
		}

		/**
		 * set function 1 string resource id
		 * 
		 * @param function1TextResId
		 * @return
		 */
		public Builder setFunction1TextResId(int function1TextResId) {
			this.function1TextResId = function1TextResId;
			return this;
		}

		/**
		 * set function 2 string resource id
		 * 
		 * @param function2TextResId
		 * @return
		 */
		public Builder setFunction2TextResId(int function2TextResId) {
			this.function2TextResId = function2TextResId;
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

		int iconResId;
		int descResId;
		/**
		 * item id for click, using those const above
		 */
		int itemClickId;
		/**
		 * function textview click, using thos const above
		 */
		int function1ClickId;
		int function2ClickId;
		int function1TextResId;
		int function2TextResId;
		int notifyCount;
		Context context;
	}
}
