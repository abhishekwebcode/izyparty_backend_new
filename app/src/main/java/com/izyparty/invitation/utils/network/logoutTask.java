package com.izyparty.invitation.utils.network;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class logoutTask extends AsyncTask<Object, Void, JSONObject> {

    public static class callbackLogout {
        public void onDoneCallBack() {}
    }
    callbackLogout callback;
    BaseActivity baseActivity;
    String url;
    public logoutTask(BaseActivity baseActivity, String url,callbackLogout callback) {
        super();
        Log.d(TAG, "addEvenetTask: CREATD TASK");
        this.baseActivity = baseActivity;
        this.url = url;
        this.callback=callback;
        }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        baseActivity.recycleDialog();
        baseActivity.dialog = new ProgressDialog(baseActivity);
        baseActivity.dialog.setMessage(baseActivity.getString(R.string.loading1));
        baseActivity.dialog.show();
    }

    @Override
    protected JSONObject doInBackground(Object... objects) {
        try {
            Log.d(TAG, "doInBackground: " + url);
            HashMap<String,Object> map=new HashMap<String, Object>();
            map.put("dummy","dummy");
            return requestMaker.makeRequests(url,map,baseActivity);
        } catch (Exception e) {
            e.printStackTrace();
            try {
                return new JSONObject("{\"success\":false,\"message\":\" " + e.getStackTrace().toString() + " \"}");
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
        }
    }
    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        baseActivity.dialog.cancel();
        this.callback.onDoneCallBack();
    }
}

