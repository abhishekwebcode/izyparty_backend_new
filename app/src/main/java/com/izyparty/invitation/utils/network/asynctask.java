package com.izyparty.invitation.utils.network;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import com.izyparty.invitation.templates.BaseActivity;
import com.izyparty.invitation.ui.auth.RegisterActivity;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class asynctask extends AsyncTask<Object,Void, JSONObject> {
    BaseActivity baseActivity;
    String url;
    HashMap<String,Object> map;
    public asynctask(BaseActivity baseActivity,String url,HashMap<String,Object> map) {
        super();
        this.baseActivity=baseActivity;
        this.url=url;
        this.map=map;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        baseActivity.recycleDialog();
        baseActivity.dialog=new ProgressDialog(baseActivity);
    }

    @Override
    protected JSONObject doInBackground(Object... objects) {
        try {return requestMaker.makeRequests(url,map,baseActivity);} catch (Exception e )  {
            try {
                return new JSONObject("{\"success\":false,\"error\":\"error in json\"}");
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        ((RegisterActivity)baseActivity).callback(jsonObject);
    }
}
