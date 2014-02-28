package org.cnc.msrobot.activity;

import java.util.ArrayList;

import org.cnc.msrobot.R;
import org.cnc.msrobot.resource.ItemListFunction;
import org.cnc.msrobot.resource.WeatherResource;
import org.cnc.msrobot.task.ReadEmailTask;
import org.cnc.msrobot.task.ReadSMSTask;
import org.cnc.msrobot.utils.Actions;
import org.cnc.msrobot.utils.AppUtils;
import org.cnc.msrobot.utils.Consts;
import org.cnc.msrobot.utils.Consts.URLConsts;
import org.cnc.msrobot.utils.LocationUtils;
import org.cnc.msrobot.utils.LocationUtils.LocationUtilsListener;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.android.volley.Response.Listener;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;

public class MainActivity extends BaseActivity {
	
}
