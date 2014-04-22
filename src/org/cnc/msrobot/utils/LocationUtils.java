package org.cnc.msrobot.utils;

import org.cnc.msrobot.R;
import org.cnc.msrobot.activity.BaseActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

public class LocationUtils {
	/*
	 * Constants for location update parameters
	 */
	// min distance for request update location (m)
	public static final int UPDATE_MIN_DISTANCE_MET = 10;

	// min time for request update location (second)
	public static final int UPDATE_MIN_TIME_SECOND = 10000;

	// Create an empty string for initializing strings
	public static final String EMPTY_STRING = new String();
	private static final int TWO_MINUTES = 1000 * 60 * 2;

	public interface LocationUtilsListener {
		public void locationSettingCancel();

		public void locationChanged(Location location);
	}

	private LocationUtilsListener callBack;
	private LocationManager lm;
	private boolean gps_enabled = false;
	private boolean network_enabled = false;

	private static final String TAG = LocationUtils.class.getSimpleName();

	/**
	 * request update new current location and return last current location
	 * 
	 * @param activity
	 * @param callBack
	 * @return
	 */
	public Location getLocation(Activity activity, LocationUtilsListener callBack) {
		return getLocation(activity, callBack, true);
	}

	public Location getLocation(final Activity activity, LocationUtilsListener callBack, boolean checkLastRequestSetting) {
		this.callBack = callBack;
		// I use LocationResult callback class to pass location value from
		// MyLocation to user code.
		if (lm == null) lm = (LocationManager) activity.getSystemService(Context.LOCATION_SERVICE);

		// exceptions will be thrown if provider is not permitted.
		try {
			gps_enabled = lm.isProviderEnabled(LocationManager.GPS_PROVIDER);
		} catch (Exception ex) {
			Logger.warn(TAG, "error while check GPS enabled");
		}
		try {
			network_enabled = lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
		} catch (Exception ex) {
			Logger.warn(TAG, "error while check network enabled");
		}
		// don't start listeners if no provider is enabled
		if (!gps_enabled && !network_enabled && callBack != null) {
			AlertDialog.Builder builder = new AlertDialog.Builder(activity);
			builder.setMessage(R.string.msg_warn_location_cannot_determine).setCancelable(false)
					.setPositiveButton(R.string.common_yes, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dlg, int sumthin) {
							// go to setting location service
							try {
								Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
								activity.startActivityForResult(intent, Consts.REQUEST_CODE_LOCATION);
							} catch (Exception ex) {
								((BaseActivity) activity).showCenterToast(R.string.msg_err_cannot_open_setting);
							}
						}
					}).setNegativeButton(R.string.common_no, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							// if this button is clicked, just close the dialog box and set view v is unselect
							dialog.cancel();
							if (LocationUtils.this.callBack != null) {
								LocationUtils.this.callBack.locationSettingCancel();
							}
						}
					});
			AlertDialog alert = builder.create();
			alert.show();
			return null;
		}

		// call location update in background for future request
		if (gps_enabled) lm.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_MIN_TIME_SECOND,
				UPDATE_MIN_DISTANCE_MET, locationListenerGps);

		if (network_enabled) lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_MIN_TIME_SECOND,
				UPDATE_MIN_DISTANCE_MET, locationListenerNetwork);

		return getLastLocation();
	}

	LocationListener locationListenerGps = new LocationListener() {
		public void onLocationChanged(Location location) {
			lm.removeUpdates(this);
			lm.removeUpdates(locationListenerNetwork);
			callBack.locationChanged(location);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	LocationListener locationListenerNetwork = new LocationListener() {
		public void onLocationChanged(Location location) {
			lm.removeUpdates(this);
			lm.removeUpdates(locationListenerGps);
			callBack.locationChanged(location);
		}

		public void onProviderDisabled(String provider) {
		}

		public void onProviderEnabled(String provider) {
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
		}
	};

	private Location getLastLocation() {
		Location net_loc = null, gps_loc = null;
		if (gps_enabled) gps_loc = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		if (network_enabled) net_loc = lm.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		// if there are both values use the latest one
		if (gps_loc != null && net_loc != null) {
			if (gps_loc.getTime() > net_loc.getTime()) return gps_loc;
			else
				return net_loc;
		}

		if (gps_loc != null) { return gps_loc; }
		if (net_loc != null) { return net_loc; }

		return null;
	}

	/**
	 * Determines whether one Location reading is better than the current Location fix
	 * 
	 * @param location
	 *            The new Location that you want to evaluate
	 * @param currentBestLocation
	 *            The current Location fix, to which you want to compare the new one
	 */
	public boolean isBetterLocation(Location location, Location currentBestLocation) {
		if (currentBestLocation == null) {
			// A new location is always better than no location
			return true;
		}

		// Check whether the new location fix is newer or older
		long timeDelta = location.getTime() - currentBestLocation.getTime();
		boolean isSignificantlyNewer = timeDelta > TWO_MINUTES;
		boolean isSignificantlyOlder = timeDelta < -TWO_MINUTES;
		boolean isNewer = timeDelta > 0;

		// If it's been more than two minutes since the current location, use the new location
		// because the user has likely moved
		if (isSignificantlyNewer) {
			return true;
			// If the new location is more than two minutes older, it must be worse
		} else if (isSignificantlyOlder) { return false; }

		// Check whether the new location fix is more or less accurate
		int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
		boolean isLessAccurate = accuracyDelta > 0;
		boolean isMoreAccurate = accuracyDelta < 0;
		boolean isSignificantlyLessAccurate = accuracyDelta > 200;

		// Check if the old and new location are from the same provider
		boolean isFromSameProvider = isSameProvider(location.getProvider(), currentBestLocation.getProvider());

		// Determine location quality using a combination of timeliness and accuracy
		if (isMoreAccurate) {
			return true;
		} else if (isNewer && !isLessAccurate) {
			return true;
		} else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) { return true; }
		return false;
	}

	/** Checks whether two providers are the same */
	private boolean isSameProvider(String provider1, String provider2) {
		if (provider1 == null) { return provider2 == null; }
		return provider1.equals(provider2);
	}
}
