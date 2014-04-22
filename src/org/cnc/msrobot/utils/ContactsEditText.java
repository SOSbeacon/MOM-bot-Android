/*
 * Copyright (C) 2012 Robert Li
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.cnc.msrobot.utils;

import java.io.FileDescriptor;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.cnc.msrobot.R;
import org.cnc.msrobot.provider.DbContract.TableContact;
import org.cnc.msrobot.resource.ContactResource;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class ContactsEditText extends MultiAutoCompleteTextView implements OnClickListener, OnFocusChangeListener {

	public static Pattern mPatternTagFriend = Pattern.compile("@\\{name: (.*?), email: (.*?), mobile: (.*?)\\}",
			Pattern.CASE_INSENSITIVE);
	private ContactsAdapter mAdapter;
	private Bitmap mLoadingImage;
	private int mDropdownItemHeight;

	public ContactsEditText(Context context) {
		super(context);
		init(context);
	}

	public ContactsEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		init(context);
	}

	public ContactsEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		init(context);
	}

	private void init(Context context) {
		// Set image height
		mDropdownItemHeight = 48;

		// Set default image
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture_holo_light, options);
		options.inSampleSize = Utils.calculateInSampleSize(options, mDropdownItemHeight, mDropdownItemHeight);
		options.inJustDecodeBounds = false;
		mLoadingImage = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture_holo_light,
				options);

		// Set adapter
		mAdapter = new ContactsAdapter(context);
		setAdapter(mAdapter);
		mAdapter.swapCursor(mAdapter.runQueryOnBackgroundThread(""));

		// Separate entries by commas
		setTokenizer(new Tokenizer() {

			@Override
			public CharSequence terminateToken(CharSequence text) {
				int i = text.length();

				while (i > 0 && text.charAt(i - 1) == ' ') {
					i--;
				}

				if (i > 0 && text.charAt(i - 1) == ' ') {
					return text;
				} else {
					if (text instanceof Spanned) {
						SpannableString sp = new SpannableString(text + " ");
						TextUtils.copySpansFrom((Spanned) text, 0, text.length(), Object.class, sp, 0);
						return sp;
					} else {
						return text + " ";
					}
				}
			}

			@Override
			public int findTokenStart(CharSequence text, int cursor) {
				int i = cursor;

				while (i > 0 && text.charAt(i - 1) != ' ') {
					i--;
				}
				// Check if token really started with ' ', else we don't have a valid token
				if (i < 1 || text.charAt(i - 1) != ' ') { return cursor; }

				return i;
			}

			@Override
			public int findTokenEnd(CharSequence text, int cursor) {
				int i = cursor;
				int len = text.length();

				while (i < len) {
					if (text.charAt(i) == ' ') {
						return i;
					} else {
						i++;
					}
				}

				return len;
			}
		});

		// Pop up suggestions after 1 character is typed.
		setThreshold(1);

		setOnClickListener(this);
		setOnFocusChangeListener(this);
	}

	@Override
	protected CharSequence convertSelectionToString(Object selectedItem) {
		ContactResource contact = (ContactResource) selectedItem;
		return getContactSpanned(contact);
	}

	public void addContact(ContactResource contact) {
		append(getContactSpanned(contact));
		append(" ");
	}

	public void setTextFromSpannedString(String spannedString) {
		setText(addPeopleToGroupChat(new SpannableStringBuilder(spannedString)));
	}

	private CharSequence getContactSpanned(ContactResource contact) {
		String friendName = "@{name: " + contact.name + ", email: " + contact.email + ", mobile:  " + contact.phone
				+ " }";
		return addPeopleToGroupChat(new SpannableStringBuilder(friendName));
	}

	private SpannableStringBuilder addPeopleToGroupChat(SpannableStringBuilder spannable) {
		// For tag friends.
		final Matcher matcherTag = mPatternTagFriend.matcher(spannable);
		while (matcherTag.find()) {
			// Set span. group 1 is name
			View view = createContactTextView(matcherTag.group(1));
			BitmapDrawable bd = (BitmapDrawable) convertViewToDrawable(view);
			spannable.setSpan(new ImageSpan(bd), matcherTag.start(), matcherTag.end(),
					Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
		return spannable;
	}

	private Object convertViewToDrawable(View view) {
		BitmapDrawable bd = null;
		try {
			int spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
			view.measure(spec, spec);
			view.layout(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
			Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(),
					Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(bitmap);
			canvas.translate(-view.getScrollX(), -view.getScrollY());
			view.draw(canvas);
			bd = new BitmapDrawable(getContext().getResources(), bitmap);
			bd.setBounds(0, 0, view.getMeasuredWidth(), view.getMeasuredHeight());
		} catch (Exception ex) {
		}
		return bd;
	}

	private View createContactTextView(String text) {
		TextView tv = new TextView(getContext());
		tv.setTextSize(TypedValue.COMPLEX_UNIT_PX,
				getContext().getResources().getDimension(R.dimen.common_textsize_larger));
		tv.setText(text);
		tv.setBackgroundResource(R.drawable.img_bg_notify_blue);
		tv.setTextColor(Color.WHITE);
		return tv;
	}

	/**
	 * get selected contact
	 */
	public List<ContactResource> getSelectedContact() {
		String text = getText().toString();
		return getListContact(text);
	}

	public static List<ContactResource> getListContact(String text) {
		final Matcher matcherTag = mPatternTagFriend.matcher(text);
		ArrayList<ContactResource> result = new ArrayList<ContactResource>();
		while (matcherTag.find()) {
			ContactResource c = new ContactResource();
			c.name = matcherTag.group(1);
			c.email = matcherTag.group(2);
			c.phone = matcherTag.group(3);
			result.add(c);
		}
		return result;
	}

	private class ContactsAdapter extends CursorAdapter {

		Context mContext;
		LayoutInflater mInflater;

		public ContactsAdapter(Context context) {
			super(context, null, 0);

			mContext = context;
			mInflater = LayoutInflater.from(context);
		}

		@Override
		public Object getItem(int position) {
			Cursor cursor = (Cursor) super.getItem(position);
			ContactResource contact = new ContactResource();

			contact.id = cursor.getInt(ContactsQuery.ID_COLUMN);
			contact.name = cursor.getString(ContactsQuery.NAME_COLUMN);
			contact.email = cursor.getString(ContactsQuery.EMAIL_COLUMN);
			contact.phone = cursor.getString(ContactsQuery.MOBILE_COLUMN);

			return contact;
		}

		@Override
		public Cursor runQueryOnBackgroundThread(CharSequence constraint) {
			Cursor c;
			if (constraint == null || constraint.length() == 0) {
				c = mContext.getContentResolver().query(ContactsQuery.CONTENT_URI, ContactsQuery.PROJECTION, null,
						null, ContactsQuery.SORT_ORDER);
			} else {
				c = mContext.getContentResolver()
						.query(ContactsQuery.FILTER_URI, ContactsQuery.PROJECTION,
								ContactsQuery.SELECTION.replaceAll("\\$1", (String) constraint), null,
								ContactsQuery.SORT_ORDER);
			}
			return c;
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
			final View dropdownView = mInflater.inflate(R.layout.contacts_dropdown_item, viewGroup, false);

			ViewHolder holder = new ViewHolder();
			holder.text = (TextView) dropdownView.findViewById(android.R.id.text1);
			holder.image = (ImageView) dropdownView.findViewById(android.R.id.icon);

			dropdownView.setTag(holder);

			return dropdownView;
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			final ViewHolder holder = (ViewHolder) view.getTag();

			final String displayName = cursor.getString(ContactsQuery.NAME_COLUMN);

			holder.text.setText(displayName);
			holder.image.setImageBitmap(mLoadingImage);
		}
	}

	/**
	 * Class to hold the dropdown item's views. Used as a tag to bind the child views to its parent.
	 */
	private class ViewHolder {
		public TextView text;
		public ImageView image;
	}

	public static interface ContactsQuery {

		// A content URI for the Contacts table
		final static Uri CONTENT_URI = TableContact.CONTENT_URI;

		// The search/filter query Uri
		final static Uri FILTER_URI = TableContact.CONTENT_URI;

		@SuppressLint("InlinedApi")
		final static String SELECTION = TableContact.NAME + " like '%$1%' OR " + TableContact.EMAIL
				+ " like '%$1%' OR " + TableContact.MOBILE + " like '%$1%' ";

		final static String SORT_ORDER = TableContact.NAME;

		@SuppressLint("InlinedApi")
		final static String[] PROJECTION = { TableContact._ID, TableContact.NAME, TableContact.EMAIL,
				TableContact.MOBILE };

		// The query column numbers which map to each value in the projection
		final static int ID_COLUMN = 0;
		final static int NAME_COLUMN = 1;
		final static int EMAIL_COLUMN = 2;
		final static int MOBILE_COLUMN = 3;
	}

	private static class Utils {

		// Prevents instantiation.
		private Utils() {
		}

		/**
		 * Uses static final constants to detect if the device's platform version is Honeycomb or later.
		 */
		@SuppressWarnings("unused")
		public static boolean hasHoneycomb() {
			return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
		}

		@SuppressWarnings("unused")
		public static Bitmap decodeSampledBitmapFromDescriptor(FileDescriptor fileDescriptor, int reqWidth,
				int reqHeight) {

			// First decode with inJustDecodeBounds=true to check dimensions
			final BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);

			// Calculate inSampleSize
			options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

			// Decode bitmap with inSampleSize set
			options.inJustDecodeBounds = false;
			return BitmapFactory.decodeFileDescriptor(fileDescriptor, null, options);
		}

		public static int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
			// Raw height and width of image
			final int height = options.outHeight;
			final int width = options.outWidth;
			int inSampleSize = 1;

			if (height > reqHeight || width > reqWidth) {

				// Calculate ratios of height and width to requested height and width
				final int heightRatio = Math.round((float) height / (float) reqHeight);
				final int widthRatio = Math.round((float) width / (float) reqWidth);

				// Choose the smallest ratio as inSampleSize value, this will guarantee a final image
				// with both dimensions larger than or equal to the requested height and width.
				inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;

				// This offers some additional logic in case the image has a strange
				// aspect ratio. For example, a panorama may have a much larger
				// width than height. In these cases the total pixels might still
				// end up being too large to fit comfortably in memory, so we should
				// be more aggressive with sample down the image (=larger inSampleSize).

				final float totalPixels = width * height;

				// Anything more than 2x the requested pixels we'll sample down further
				final float totalReqPixelsCap = reqWidth * reqHeight * 2;

				while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
					inSampleSize++;
				}
			}
			return inSampleSize;
		}

	}

	@Override
	public void onClick(View v) {
		showDropDown();
	}

	@Override
	public void onFocusChange(View view, boolean focus) {
		Log.d("zzz", "onFocusChange: " + focus);
		if (focus) {
			showDropDown();
		} else {
			dismissDropDown();
			AppUtils.hideKeyboard(view);
		}
	}

}