package org.cnc.msrobot.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.BaseActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.decode.ImageDecoder;

public class PhotoUtils {
	private static final String TAG = PhotoUtils.class.getSimpleName();

	public static String CROP_WIDTH_ID = "crop_width";
	public static String CROP_HEIGHT_ID = "crop_height";

	/**
	 * Do the Cropping image by calling activity from Android
	 * 
	 * @param uri
	 *            : the image uri
	 * @param aActivity
	 *            : main activity that call cropping
	 * @param aFragment
	 *            : fragment that call cropping, if not null, activity result will get back to fragment
	 * @param aWidth
	 *            : default width of crop (0 means no fixed ratio)
	 * @param aHeight
	 *            : default height of crop (0 means no fixed ratio)
	 * @param isScale
	 *            : the ratio of crop is scale or not
	 */
	public static void doCrop(Uri uri, final Activity aActivity, final Fragment aFragment, final int aWidth,
			final int aHeight, final boolean isScale) {
		final ArrayList<CropOption> cropOptions = new ArrayList<CropOption>();

		Intent intent = new Intent("com.android.camera.action.CROP");
		intent.setType("image/*");

		List<ResolveInfo> list = aActivity.getPackageManager().queryIntentActivities(intent, 0);

		int size = list.size();

		if (size == 0) {
			Toast.makeText(aActivity, "Can not find image crop app", Toast.LENGTH_SHORT).show();
			return;
		} else {
			intent.setData(uri);
			intent.putExtra("crop", "true");
			if (aWidth != 0 && aHeight != 0) {
				intent.putExtra("outputX", aWidth);
				intent.putExtra("outputY", aHeight);
				if (isScale) {
					intent.putExtra("aspectX", aWidth);
					intent.putExtra("aspectY", aHeight);
				}
			}
			// intent.putExtra("return-data", true);
			intent.putExtra(MediaStore.EXTRA_OUTPUT, uri);
			intent.putExtra("outputFormat", Bitmap.CompressFormat.JPEG.toString());

			if (size == 1) {
				Intent i = new Intent(intent);
				ResolveInfo res = list.get(0);

				i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

				if (aFragment == null) {
					aActivity.startActivityForResult(i, Consts.REQUEST_CROP_AFTER_CAPTURE);
				} else {
					aFragment.startActivityForResult(i, Consts.REQUEST_CROP_AFTER_CAPTURE);
				}
			} else {
				for (ResolveInfo res : list) {
					final CropOption co = new CropOption();

					co.title = aActivity.getPackageManager().getApplicationLabel(res.activityInfo.applicationInfo);
					co.icon = aActivity.getPackageManager().getApplicationIcon(res.activityInfo.applicationInfo);
					co.appIntent = new Intent(intent);

					co.appIntent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));

					cropOptions.add(co);
				}

				CropOptionAdapter adapter = new CropOptionAdapter(aActivity.getApplicationContext(), cropOptions);

				AlertDialog.Builder builder = new AlertDialog.Builder(aActivity);
				builder.setTitle("Choose Crop App");
				builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int item) {
						if (aFragment == null) {
							aActivity.startActivityForResult(cropOptions.get(item).appIntent,
									Consts.REQUEST_CROP_AFTER_CAPTURE);
						} else {
							aFragment.startActivityForResult(cropOptions.get(item).appIntent,
									Consts.REQUEST_CROP_AFTER_CAPTURE);
						}
					}
				});

				builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
					@Override
					public void onCancel(DialogInterface dialog) {
						// if (uri != null) {
						// getBaseActivity().getContentResolver().delete(uri,
						// null, null);
						// mNewImageUri = null;
						// }
					}
				});

				AlertDialog alert = builder.create();

				alert.show();
			}
		}
	}

	/**
	 * TODO add comment for this old code
	 * 
	 * @param uri
	 * @param aActivity
	 * @return
	 */
	private static Bitmap doScale(Uri uri, Activity aActivity) {

		InputStream in = null;
		try {
			// TODO: Sua lai IMAGE_MAX_SIZE 200 -> 150
			final int IMAGE_MAX_SIZE = 150 * 1000; // 0,15MB
			in = aActivity.getContentResolver().openInputStream(uri);

			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeStream(in, null, o);
			in.close();

			int scale = 1;
			while ((o.outWidth * o.outHeight) * (1 / Math.pow(scale, 2)) > IMAGE_MAX_SIZE) {
				scale++;
			}
			Logger.debug(TAG, "scale = " + scale + ", orig-width: " + o.outWidth + ",   orig-height: " + o.outHeight);

			Bitmap b = null;

			in = aActivity.getContentResolver().openInputStream(uri);

			if (scale > 1) {
				scale--;
				// scale to max possible inSampleSize that still yields an image
				// larger than target
				o = new BitmapFactory.Options();
				o.inSampleSize = scale;
				b = BitmapFactory.decodeStream(in, null, o);

				// resize to desired dimensions
				int height = b.getHeight();
				int width = b.getWidth();
				Logger.debug(TAG, "1th scale operation dimenions - width: " + width + ",height: " + height);

				double y = Math.sqrt(IMAGE_MAX_SIZE / (((double) width) / height));
				double x = (y / height) * width;

				Bitmap scaledBitmap = Bitmap.createScaledBitmap(b, (int) x, (int) y, true);
				b.recycle();
				b = scaledBitmap;

				System.gc();
			} else {
				b = BitmapFactory.decodeStream(in);
			}
			in.close();

			Logger.debug(TAG, "bitmap size - width: " + b.getWidth() + ", height: " + b.getHeight());
			return b;

		} catch (IOException e) {
			Logger.error(TAG, e.getMessage(), e);
			return null;
		}
	}

	/**
	 * Show Aviary Photo Editor TODO tam xoa Aviary
	 * 
	 * @param uri
	 *            photo uri
	 * @param aActivity
	 *            activity call this method
	 */
	/*
	 * public static void showAviary(Uri uri, Activity aActivity, Fragment aFragment) { Logger.info(TAG,
	 * uri.toString()); String apiKey = aActivity.getResources().getString(R.string.common_aviary_api_key); // Create
	 * the intent needed to start feather Intent newIntent = new Intent(aActivity, FeatherActivity.class); // set the
	 * source image uri newIntent.setData(uri); // pass the required api key ( http://developers.aviary.com/ )
	 * newIntent.putExtra("API_KEY", apiKey); // pass the uri of the destination image file (optional) // This will be
	 * the same uri you will receive in the onActivityResult newIntent.putExtra("output", uri); // format of the
	 * destination image (optional) newIntent.putExtra("output-format", Bitmap.CompressFormat.JPEG.name()); // output
	 * format quality (optional) newIntent.putExtra("output-quality", 100); // you can force feather to display only a
	 * certain tools newIntent.putExtra("tools-list", new String[] { "EFFECTS", "CROP", "FOCUS", "ENHANCE", "ADJUST",
	 * "BRIGHTNESS", "CONTRAST" }); // enable fast rendering preview newIntent.putExtra("effect-enable-fast-preview",
	 * true); newIntent.putExtra(Constants.EXTRA_TOOLS_DISABLE_VIBRATION, true); // ..and start feather if (aFragment ==
	 * null) { aActivity.startActivityForResult(newIntent, Consts.REQUEST_AVIARY); } else {
	 * aFragment.startActivityForResult(newIntent, Consts.REQUEST_AVIARY); } }
	 */

	/**
	 * @param sourceLocation
	 *            like this /mnt/sdcard/XXXX/XXXXX/15838e85-066d-4738-a243-76c461cd8b01.jpg
	 * @param destLocation
	 *            /mnt/sdcard/XXXX/XXXXX/15838e85-066d-4738-a243-76c461cd8b01.jpg
	 * @return true if successful copy file and false othrerwise
	 * 
	 *         set this permissions in your application WRITE_EXTERNAL_STORAGE ,READ_EXTERNAL_STORAGE
	 * 
	 */
	public static boolean copyFile(Context context, Uri sourceLocation, String destLocation) {
		String sSource = AppUtils.getRealPathFromURI(context, sourceLocation);
		return copyFile(context, sSource, destLocation);
	}

	public static boolean copyFile(Context context, String sourceLocation, String destLocation) {
		try {
			File sd = Environment.getExternalStorageDirectory();
			if (sd.canWrite()) {
				File source = new File(sourceLocation);
				File dest = new File(destLocation);
				if (!dest.exists()) {
					dest.createNewFile();
				}
				if (source.exists()) {
					InputStream src = new FileInputStream(source);
					OutputStream dst = new FileOutputStream(dest);
					// Copy the bits from instream to outstream
					byte[] buf = new byte[1024];
					int len;
					while ((len = src.read(buf)) > 0) {
						dst.write(buf, 0, len);
					}
					src.close();
					dst.close();
				}
			}
			return true;
		} catch (Exception ex) {
			ex.printStackTrace();
			return false;
		}
	}

	public static void saveToGallery(Context mContext, File file) {
		MediaScannerConnection.scanFile(mContext, new String[] { file.toString() }, null, null);
	}

	public static Bitmap fastblur(Context context, Bitmap sentBitmap, int radius) {
		// Stack Blur v1.0 from
		// http://www.quasimondo.com/StackBlurForCanvas/StackBlurDemo.html
		//
		// Java Author: Mario Klingemann <mario at quasimondo.com>
		// http://incubator.quasimondo.com
		// created Feburary 29, 2004
		// Android port : Yahel Bouaziz <yahel at kayenko.com>
		// http://www.kayenko.com
		// ported april 5th, 2012
		//

		// This is a compromise between Gaussian Blur and Box blur
		// It creates much better looking blurs than Box Blur, but is
		// 7x faster than my Gaussian Blur implementation.
		//
		// I called it Stack Blur because this describes best how this
		// filter works internally: it creates a kind of moving stack
		// of colors whilst scanning through the image. Thereby it
		// just has to add one new block of color to the right side
		// of the stack and remove the leftmost color. The remaining
		// colors on the topmost layer of the stack are either added on
		// or reduced by one, depending on if they are on the right or
		// on the left side of the stack.
		//
		// If you are using this algorithm in your code please add
		// the following line:
		//
		// Stack Blur Algorithm by Mario Klingemann <mario@quasimondo.com>

		Bitmap bitmap = sentBitmap.copy(sentBitmap.getConfig(), true);

		try {
			if (radius < 1) { return (null); }

			int w = bitmap.getWidth();
			int h = bitmap.getHeight();

			int[] pix = new int[w * h];
			if (bitmap.isRecycled()) return null;
			bitmap.getPixels(pix, 0, w, 0, 0, w, h);

			int wm = w - 1;
			int hm = h - 1;
			int wh = w * h;
			int div = radius + radius + 1;

			int r[] = new int[wh];
			int g[] = new int[wh];
			int b[] = new int[wh];
			int rsum, gsum, bsum, x, y, i, p, yp, yi, yw;
			int vmin[] = new int[Math.max(w, h)];

			int divsum = (div + 1) >> 1;
			divsum *= divsum;
			int dv[] = new int[256 * divsum];
			for (i = 0; i < 256 * divsum; i++) {
				dv[i] = (i / divsum);
			}

			yw = yi = 0;

			int[][] stack = new int[div][3];
			int stackpointer;
			int stackstart;
			int[] sir;
			int rbs;
			int r1 = radius + 1;
			int routsum, goutsum, boutsum;
			int rinsum, ginsum, binsum;

			for (y = 0; y < h; y++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				for (i = -radius; i <= radius; i++) {
					p = pix[yi + Math.min(wm, Math.max(i, 0))];
					sir = stack[i + radius];
					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);
					rbs = r1 - Math.abs(i);
					rsum += sir[0] * rbs;
					gsum += sir[1] * rbs;
					bsum += sir[2] * rbs;
					if (i > 0) {
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					} else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}
				}
				stackpointer = radius;

				for (x = 0; x < w; x++) {

					r[yi] = dv[rsum];
					g[yi] = dv[gsum];
					b[yi] = dv[bsum];

					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;

					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];

					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];

					if (y == 0) {
						vmin[x] = Math.min(x + radius + 1, wm);
					}
					p = pix[yw + vmin[x]];

					sir[0] = (p & 0xff0000) >> 16;
					sir[1] = (p & 0x00ff00) >> 8;
					sir[2] = (p & 0x0000ff);

					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;

					stackpointer = (stackpointer + 1) % div;
					sir = stack[(stackpointer) % div];

					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];

					yi++;
				}
				yw += w;
			}
			for (x = 0; x < w; x++) {
				rinsum = ginsum = binsum = routsum = goutsum = boutsum = rsum = gsum = bsum = 0;
				yp = -radius * w;
				for (i = -radius; i <= radius; i++) {
					yi = Math.max(0, yp) + x;

					sir = stack[i + radius];

					sir[0] = r[yi];
					sir[1] = g[yi];
					sir[2] = b[yi];

					rbs = r1 - Math.abs(i);

					rsum += r[yi] * rbs;
					gsum += g[yi] * rbs;
					bsum += b[yi] * rbs;

					if (i > 0) {
						rinsum += sir[0];
						ginsum += sir[1];
						binsum += sir[2];
					} else {
						routsum += sir[0];
						goutsum += sir[1];
						boutsum += sir[2];
					}

					if (i < hm) {
						yp += w;
					}
				}
				yi = x;
				stackpointer = radius;
				for (y = 0; y < h; y++) {
					// Preserve alpha channel: ( 0xff000000 & pix[yi] )
					pix[yi] = (0xff000000 & pix[yi]) | (dv[rsum] << 16) | (dv[gsum] << 8) | dv[bsum];

					rsum -= routsum;
					gsum -= goutsum;
					bsum -= boutsum;

					stackstart = stackpointer - radius + div;
					sir = stack[stackstart % div];

					routsum -= sir[0];
					goutsum -= sir[1];
					boutsum -= sir[2];

					if (x == 0) {
						vmin[y] = Math.min(y + r1, hm) * w;
					}
					p = x + vmin[y];

					sir[0] = r[p];
					sir[1] = g[p];
					sir[2] = b[p];

					rinsum += sir[0];
					ginsum += sir[1];
					binsum += sir[2];

					rsum += rinsum;
					gsum += ginsum;
					bsum += binsum;

					stackpointer = (stackpointer + 1) % div;
					sir = stack[stackpointer];

					routsum += sir[0];
					goutsum += sir[1];
					boutsum += sir[2];

					rinsum -= sir[0];
					ginsum -= sir[1];
					binsum -= sir[2];

					yi += w;
				}
			}

			bitmap.setPixels(pix, 0, w, 0, 0, w, h);
		} catch (Exception e) {
			e.printStackTrace();
			return bitmap;
		}
		return (bitmap);
	}
}