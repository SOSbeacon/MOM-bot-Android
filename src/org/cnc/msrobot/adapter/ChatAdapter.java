package org.cnc.msrobot.adapter;

import java.util.ArrayList;

import org.cnc.msrobot.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ChatAdapter extends ArrayAdapter<String> implements OnClickListener {

	private LayoutInflater mInfalter;
	private int colorWhite, colorBlack;

	public ChatAdapter(Context context, ArrayList<String> list) {
		super(context, 0, list);
		mInfalter = LayoutInflater.from(context);
		colorWhite = context.getResources().getColor(R.color.white);
		colorBlack = context.getResources().getColor(R.color.black);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) {
			convertView = mInfalter.inflate(R.layout.item_list_chat, parent, false);
		}
		TextView tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
		tvDesc.setText(getItem(position));
		if (position == 0) {
			tvDesc.setBackgroundResource(R.drawable.img_bg_reply);
			tvDesc.setTextColor(colorWhite);
		} else {
			tvDesc.setBackgroundResource(R.drawable.img_bg_reply_me);
			tvDesc.setTextColor(colorBlack);
		}
		return convertView;
	}

	@Override
	public void onClick(View v) {
	}
}
