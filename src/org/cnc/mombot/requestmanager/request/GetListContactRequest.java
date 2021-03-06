package org.cnc.mombot.requestmanager.request;

import java.lang.reflect.Type;

import org.cnc.mombot.provider.DbContract.TableContact;
import org.cnc.mombot.provider.DbContract.TableGroupContact;
import org.cnc.mombot.requestmanager.RequestBase;
import org.cnc.mombot.resource.GroupContactResource;
import org.cnc.mombot.utils.Consts;
import org.cnc.mombot.utils.Logger;
import org.cnc.mombot.utils.SharePrefs;
import org.cnc.mombot.utils.Consts.URLConsts;

import android.content.ContentValues;
import android.content.Context;

import com.android.volley.Request.Method;

public class GetListContactRequest extends RequestBase<GroupContactResource[]> {
	public GetListContactRequest(Context context) {
		super(context);
	}

	@Override
	public void postAfterRequest(GroupContactResource[] result) {
		// delete all group and contact
		mContext.getContentResolver().delete(TableGroupContact.CONTENT_URI, null, null);
		mContext.getContentResolver().delete(TableContact.CONTENT_URI, null, null);

		// insert group contact
		ContentValues[] valueGroups, valueContacts;
		if (result != null && result.length > 0) {
			valueGroups = new ContentValues[result.length];
			for (int i = 0; i < result.length; i++) {
				valueGroups[i] = result[i].prepareContentValue();
				valueContacts = result[i].prepareContactContentValue();
				Logger.debug("GetListContactRequest", "insert group: " + result[i].name + " - "
						+ result[i].contacts.length);
				if (valueContacts != null) {
					mContext.getContentResolver().bulkInsert(TableContact.CONTENT_URI, valueContacts);
				}
			}
			mContext.getContentResolver().bulkInsert(TableGroupContact.CONTENT_URI, valueGroups);
		}
		mContext.getContentResolver().notifyChange(TableContact.CONTENT_URI, null);
		mContext.getContentResolver().notifyChange(TableGroupContact.CONTENT_URI, null);
	}

	@Override
	protected String buildRequestUrl() {
		String token = SharePrefs.getInstance().getLoginToken();
		String url = URLConsts.GET_LIST_CONTACT.replace(Consts.HOLDER_AUTH_TOKEN, token);
		Logger.debug("GetListContactRequest", "Get list contact: " + url);
		return url;
	}

	@Override
	protected Type getClassOf() {
		return GroupContactResource[].class;
	}

	@Override
	protected int getMethod() {
		return Method.GET;
	}

}
