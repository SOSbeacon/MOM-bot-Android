package org.cnc.msrobot.activity;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.acra.ACRA;
import org.cnc.msrobot.R;
import org.cnc.msrobot.utils.AppUtils;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.CropOption;
import org.cnc.msrobot.utils.CropOptionAdapter;
import org.cnc.msrobot.utils.Logger;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.nostra13.universalimageloader.utils.StorageUtils;

public class CameraActivity extends BaseActivity {
	protected static final String TAG = CameraActivity.class.getSimpleName();
	public static final String IMAGE_PATH = "image_path";
	public static final String IMAGE_URI = "image_uri";
	public static final String IMAGE_NAME = "image_name";
	protected String mNewImageName;
	protected String mNewImagePath;
	protected Uri mNewImageUri;
	protected Uri mNewImageUriCamera;
	protected int mImageHeight, mImageWidth;
	protected boolean mImageRotation = false;
	protected Boolean isLoadGalleryOrCamera = false;
	protected String mDefaultTag, mDefaultLocation;
	protected String mPrivateFriendId, mPrivateFriendAvatar, mPrivateFriendName;

	@Override
	public void onCreate(final Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_camera);

		// Get Image path saved
		if (savedInstanceState != null) {
			mNewImagePath = savedInstanceState.getString(IMAGE_PATH);
			mNewImageName = savedInstanceState.getString(IMAGE_NAME);
			mNewImageUriCamera = savedInstanceState.getParcelable(IMAGE_URI);
		} else {
			showActionList();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent aIntent) {
		super.onActivityResult(requestCode, resultCode, aIntent);
		Logger.debug(TAG, "result " + aIntent + ", requestCode: " + resultCode + ", requestCode: " + requestCode);
		if (requestCode == Consts.REQUEST_GALLERY) {
			Uri galleryUri = aIntent.getData();
			if (galleryUri == null) {
				mNewImagePath = aIntent.getAction();
			} else {
				mNewImagePath = AppUtils.getRealPathFromURI(this, galleryUri);
			}
			// Start Create Talk Activity
			gotoSendMmsAndEmail();
		} else if (requestCode == Consts.REQUEST_CAPTURE) {
			if (aIntent != null) {
				mNewImageUri = aIntent.getData();
				mNewImagePath = AppUtils.getRealPathFromURI(this, mNewImageUri);
			} else {
				getContentResolver().notifyChange(mNewImageUriCamera, null);
				mNewImageUri = mNewImageUriCamera;
				mNewImagePath = AppUtils.getRealPathFromURI(this, mNewImageUri);
			}
			// Start Create Talk Activity
			mImageRotation = true;
			gotoSendMmsAndEmail();
		}
	}

	@Override
	public void onSaveInstanceState(Bundle bundle) {
		super.onSaveInstanceState(bundle);
		bundle.putString(IMAGE_PATH, mNewImagePath);
		bundle.putString(IMAGE_NAME, mNewImageName);
		bundle.putParcelable(IMAGE_URI, mNewImageUriCamera);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (isLoadGalleryOrCamera) finish();
	}

	@Override
	protected void onPause() {
		super.onPause();
	}

	protected void showActionList() {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		// get list camera action
		Intent intentCapture = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		List<ResolveInfo> listCapture = getPackageManager().queryIntentActivities(intentCapture, 0);
		if (listCapture.size() > 1) {
			for (ResolveInfo res : listCapture) {
				final CropOption co = new CropOption();

				co.title = getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
				co.icon = getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
				co.appIntent = new Intent(intentCapture);
				co.requestCode = Consts.REQUEST_CAPTURE;

				co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

				cropOptions.add(co);
			}
		} else if (listCapture.size() == 1) {
			final ResolveInfo res = listCapture.get(0);
			final CropOption co = new CropOption();

			co.title = getString(R.string.common_camera);
			co.icon = getResources().getDrawable(R.drawable.img_btn_bottom_camera);
			co.appIntent = new Intent(intentCapture);
			co.requestCode = Consts.REQUEST_CAPTURE;

			co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

			cropOptions.add(co);
		}

		// get list gallery action
		Intent intentGallery = new Intent(Intent.ACTION_GET_CONTENT);
		intentGallery.setType("image/*")
				// .putExtra("crop", "true") Using Aviary to edit photo
				.putExtra("scale", false).putExtra("scaleUpIfNeeded", true)
				.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

		List<ResolveInfo> listGallery = getPackageManager().queryIntentActivities(intentGallery,
				PackageManager.MATCH_DEFAULT_ONLY);
		if (listGallery.size() > 0) {
			final ResolveInfo res = listGallery.get(0);
			final CropOption co = new CropOption();

			co.title = getString(R.string.common_gallery);
			co.icon = getResources().getDrawable(R.drawable.img_btn_bottom_gallery);
			co.appIntent = new Intent(intentGallery);
			co.requestCode = Consts.REQUEST_GALLERY;

			co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

			cropOptions.add(co);
		}

		CropOptionAdapter adapter = new CropOptionAdapter(getApplicationContext(), cropOptions);
		// build dialog to select action
		final AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle(R.string.dialog_choose_action);
		builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int item) {
				try {
					final CropOption crop = cropOptions.get(item);
					isLoadGalleryOrCamera = true;

					generationImageTemp();

					if (crop.requestCode == Consts.REQUEST_GALLERY) {
						crop.appIntent.putExtra(MediaStore.EXTRA_OUTPUT, mNewImagePath);
					} else {
						ContentValues values = new ContentValues();
						values.put(MediaStore.Images.Media.TITLE, mNewImageName);
						// store content
						mNewImageUriCamera = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
								values);
						// set the image file name values
						crop.appIntent.putExtra(MediaStore.EXTRA_OUTPUT, mNewImageUriCamera);
					}
					startActivityForResult(crop.appIntent, crop.requestCode);
				} catch (Exception ex) {
					// ko tao duoc file t???m th?? show toast th??ng b??o ng?????i d??ng
					ACRA.getErrorReporter().handleSilentException(ex);
					showCenterToast(R.string.msg_err_camera_not_sdcard);
				}
			}
		});

		builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
			@Override
			public void onCancel(DialogInterface dialog) {
				finish();
			}
		});

		AlertDialog alert = builder.create();
		alert.show();
	}

	/**
	 * Call the dialog asking for image input
	 */
	protected void generationImageTemp() {
		File tempPath = StorageUtils.getOwnCacheDirectory(this, Consts.APP_FOLDER_CACHE);
		mNewImageName = String.valueOf(System.currentTimeMillis()) + Consts.PHOTO_JPG_EXTENSION;
		mNewImagePath = tempPath.getAbsolutePath() + mNewImageName;
		File file = new File(tempPath, mNewImageName);
		mNewImageUri = Uri.fromFile(file);
	}

	protected void gotoSendMmsAndEmail() {
		Intent intent = new Intent(CameraActivity.this, SendSmsEmailActivity.class);
		Bundle bundle = getIntent().getExtras();
		intent.putExtra(SendSmsEmailActivity.EXTRA_TYPE, bundle.getInt(SendSmsEmailActivity.EXTRA_TYPE));
		intent.putExtra(SendSmsEmailActivity.EXTRA_IMAGE, mNewImagePath);
		startActivity(intent);
		finish();
	}
}
