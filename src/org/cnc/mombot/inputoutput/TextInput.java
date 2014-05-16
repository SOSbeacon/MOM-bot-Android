package org.cnc.mombot.inputoutput;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Handler;
import android.widget.EditText;

public class TextInput implements Input {
	private InputReceiveCallback callback;
	private Context mContext;
	private Handler handler = new Handler();

	public TextInput(Context context) {
		this.mContext = context;
	}

	@Override
	public void show(final String id) {
		handler.post(new Runnable() {

			@Override
			public void run() {

				AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
				builder.setTitle("Your answer");

				// Use an EditText view to get user input.
				final EditText input = new EditText(mContext);
				input.setLines(3);
				builder.setView(input);

				builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int whichButton) {
						String value = input.getText().toString();
						if (callback != null) {
							callback.onReceive(value, id);
						}
					}
				});

				builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						if (callback != null) {
							callback.onReceive(null, id);
						}
					}
				});

				AlertDialog dialog = builder.create();
				dialog.setCanceledOnTouchOutside(false);
				dialog.show();
			}
		});

	}

	@Override
	public void setReceiveCallback(InputReceiveCallback callback) {
		this.callback = callback;
	}

}