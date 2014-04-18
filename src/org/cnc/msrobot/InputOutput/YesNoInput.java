package org.cnc.msrobot.InputOutput;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;

public class YesNoInput implements Input {
	private InputReceiveCallback callback;
	private Context mContext;
	private Handler handler = new Handler();

	public YesNoInput(Context context) {
		this.mContext = context;
	}

	@Override
	public void show(final String id) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				try {
					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle("Yes or No?");
					builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int whichButton) {
							if (callback != null) {
								callback.onReceive("yes", id);
							}
						}
					});

					builder.setNegativeButton("No", new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							if (callback != null) {
								callback.onReceive("no", id);
							}
						}
					});

					AlertDialog dialog = builder.create();
					dialog.setCanceledOnTouchOutside(false);
					dialog.show();
				} catch (Exception ex) {
					ex.printStackTrace();
				}
			}
		});

	}

	@Override
	public void setReceiveCallback(InputReceiveCallback callback) {
		this.callback = callback;
	}

}