package org.cnc.msrobot.inputoutput;

import org.cnc.msrobot.resource.StaticResource;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Handler;

public class ContactListInput implements Input {
	private InputReceiveCallback callback;
	private Context mContext;
	private Handler handler = new Handler();
	private CharSequence[] contactList = null;

	public ContactListInput(Context context) {
		this.mContext = context;
	}

	@Override
	public void show(final String id) {
		handler.post(new Runnable() {

			@Override
			public void run() {
				try {
					contactList = new CharSequence[StaticResource.listContact.size()];
					for (int i = 0; i < StaticResource.listContact.size(); i++) {
						contactList[i] = StaticResource.listContact.get(i).name;
					}

					AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
					builder.setTitle("Select contact");
					builder.setPositiveButton("Cancel", new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int arg1) {
							if (callback != null) {
								callback.onReceive(null, id);
							}
						}
					});
					builder.setItems(contactList, new OnClickListener() {

						@Override
						public void onClick(DialogInterface arg0, int position) {
							if (callback != null) {
								callback.onReceive(position + "", id);
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