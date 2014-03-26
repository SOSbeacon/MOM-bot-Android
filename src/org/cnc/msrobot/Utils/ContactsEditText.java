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

import org.cnc.msrobot.R;
import org.cnc.msrobot.provider.DbContract.TableContact;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.support.v4.widget.CursorAdapter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.MultiAutoCompleteTextView;
import android.widget.TextView;

public class ContactsEditText extends MultiAutoCompleteTextView {

	private ContactsAdapter mAdapter;
	private Bitmap mLoadingImage;
	private int mDropdownItemHeight;
	private boolean mShowPhone = false;
	private boolean mShowEmail = false;

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

	public void setShowNumber(boolean show) {
		mShowPhone = show;
	}

	public void setShowEmail(boolean show) {
		mShowEmail = show;
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

		// Separate entries by commas
		setTokenizer(new MultiAutoCompleteTextView.CommaTokenizer());

		// Pop up suggestions after 1 character is typed.
		setThreshold(1);
	}

	@Override
	protected CharSequence convertSelectionToString(Object selectedItem) {
		Contact c = ((Contact) selectedItem);
		if (mShowPhone) {
			return c.name + "(" + c.mobile + ")";
		} else if (mShowEmail) {
			return c.name + "(" + c.email + ")";
		} else {
			return c.name;
		}
	}

	/**
	 * Holder class to return results to the parent Activity.
	 */
	public class Contact {
		public String name, email, mobile;
		public long id;
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
			Contact contact = new Contact();

			contact.id = cursor.getLong(ContactsQuery.ID_COLUMN);
			contact.name = cursor.getString(ContactsQuery.NAME_COLUMN);
			contact.email = cursor.getString(ContactsQuery.EMAIL_COLUMN);
			contact.mobile = cursor.getString(ContactsQuery.MOBILE_COLUMN);

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

	private static interface ContactsQuery {

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
		public static boolean hasHoneycomb() {
			return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
		}

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

}