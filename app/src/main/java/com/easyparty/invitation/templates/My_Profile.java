package com.easyparty.invitation.templates;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.easyparty.invitation.R;
import com.easyparty.invitation.utils.network.Endpoints;
import com.easyparty.invitation.utils.network.requestMaker;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class My_Profile extends BaseActivity {
    public TextView email;
    public TextView name;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);
        email = findViewById(R.id.email);
        name = findViewById(R.id.name);
        baseCreate(this);
        setTitle(getString(R.string.my_profile));
        new ShowProfile(this, Endpoints.GET_PROFILE, new HashMap<String, Object>()).execute();
    }

    public void onback(View v) {//onBackPressed();}

    }

    class ShowProfile extends AsyncTask<Object, Void, JSONObject> {
        My_Profile baseActivity;
        String url;
        HashMap<String, Object> map;

        public ShowProfile(My_Profile baseActivity, String url, HashMap<String, Object> map) {
            super();
            Log.d(TAG, "addEvenetTask: CREATD TASK");
            this.baseActivity = baseActivity;
            this.url = url;
            this.map = map;
            map.put("nothing", "dummy");
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            baseActivity.recycleDialog();
            baseActivity.dialog = new ProgressDialog(baseActivity);
            baseActivity.dialog.setMessage(baseActivity.getString(R.string.loading1));
            baseActivity.dialog.show();
            baseActivity.dialog.setCanceledOnTouchOutside(false);
        }

        @Override
        protected JSONObject doInBackground(Object... objects) {
            try {
                Log.d(TAG, "doInBackground: " + url);
                Log.d(TAG, "doInBackground: " + map.toString());
                return requestMaker.makeRequests(url,map,baseActivity);
            } catch (Exception e) {
                //Toast.makeText(baseActivity, "Error", Toast.LENGTH_SHORT).show();
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
            try {
                baseActivity.dialog.cancel();
                Log.d(TAG, "onPostExecute: " + jsonObject.toString(4));
                JSONObject jsonObject1 = jsonObject.getJSONObject("data");
                baseActivity.email.setText(jsonObject1.getString("email"));
                baseActivity.name.setText(jsonObject1.getString("name"));
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(baseActivity, baseActivity.getString(R.string.error_loading_profile), Toast.LENGTH_SHORT).show();
            }
        }
    }
}
