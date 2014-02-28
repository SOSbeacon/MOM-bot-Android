package org.cnc.msrobot;

import java.io.File;

import org.cnc.msrobot.requestmanager.RequestManager;
import org.cnc.msrobot.utils.AppUtils;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.LruBitmapCache;
import org.cnc.msrobot.utils.SharePrefs;

import android.app.Application;

import com.nostra13.universalimageloader.cache.disc.naming.HashCodeFileNameGenerator;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.QueueProcessingType;

public class MainApplication extends Application {
	private static final int	DISK_CACHE_SIZE	= 200 * 1024 * 1024;	// 200 MB

	@Override
	public void onCreate() {
		super.onCreate();

		// Init Request Manager
		RequestManager.getInstance().init(getApplicationContext());

		// Init Share Prefs
		SharePrefs.getInstance().init(this);

		// init image loader
		InitImageLoaderConfiguration();

		// Create temp path.
		File file = new File(Consts.TEMP_PATH);
		if (!file.exists()) {
			file.mkdirs();
		}
	}

	private void InitImageLoaderConfiguration() {
		int size[] = AppUtils.getScreenSize(this);
		// Neu do ngang lon hon do dai thi swap
		if (size[0] > size[1]) {
			int temp = size[0];
			size[0] = size[1];
			size[1] = temp;
		}
		final int maxMemory = (int) (Runtime.getRuntime().maxMemory());

		// Use 1/4th of the available memory for this memory cache.
		final int cacheSize = maxMemory / 4 / 1024;

		ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(getApplicationContext())
				.threadPoolSize(3).memoryCache(new LruBitmapCache(cacheSize)).discCacheSize(DISK_CACHE_SIZE)
				.threadPriority(Thread.NORM_PRIORITY).discCacheFileNameGenerator(new HashCodeFileNameGenerator())
				.memoryCacheExtraOptions(size[0], size[1]).tasksProcessingOrder(QueueProcessingType.LIFO).build();
		// Initialize ImageLoader with configuration.
		ImageLoader.getInstance().init(config);
		// set slow downloader for 3G or slow network
		if (AppUtils.getConnectivityStatus(getApplicationContext()) != AppUtils.TYPE_WIFI) {
			ImageLoader.getInstance().handleSlowNetwork(true);
		} else {
			ImageLoader.getInstance().handleSlowNetwork(false);
		}
	}
}
