package com.easyparty.invitation.templates;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import com.easyparty.invitation.R;
import com.easyparty.invitation.ui.auth.RegisterActivity;
import com.easyparty.invitation.utils.network.Endpoints;
import com.easyparty.invitation.utils.network.requestMaker;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class ChangePassword extends BaseActivity {
public EditText exisitng;
public EditText new1;
public EditText confirm;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);
        exisitng=(EditText)findViewById(R.id.name);
        new1=(EditText)findViewById(R.id.email2);
        confirm=(EditText)findViewById(R.id.email);
        baseCreate(this);
        setTitle(getString(R.string.change_password));
    }

    HashMap<String,Object> getMap() {
        HashMap<String,Object> map = new HashMap<>();
        map.put("existing",exisitng.getText().toString());
        map.put("new",new1.getText().toString());
        map.put("confirm",confirm.getText().toString());
        return map;
    }

    public void fi(View v) {finish();}

    public void update(View v) {
        HashMap<String,Object> map = getMap();
        for (String key : map.keySet()) {
            if ("".equals(map.get(key).toString())) {
                Toast.makeText(this, getString(R.string.password_fields_empty), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!RegisterActivity.checkPassword(new1.getText().toString(),this)) {
            return;
        }
        if (!RegisterActivity.checkPassword(confirm.getText().toString(),this)) {
            return;
        }
        if (!(new1.getText().toString().equals(confirm.getText().toString()))) {
            Toast.makeText(this, getString(R.string.password_dont_match), Toast.LENGTH_SHORT).show();
            return;
        }
        new ShowProfile(this, Endpoints.CHANGE_PASSWORD,map).execute();
        baseCreate(this);
    }

}


class ShowProfile extends AsyncTask<Object, Void, JSONObject> {
    ChangePassword baseActivity;
    String url;
    HashMap<String, Object> map;

    public ShowProfile(ChangePassword baseActivity, String url, HashMap<String, Object> map) {
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
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(baseActivity, baseActivity.getString(R.string.updated_password), Toast.LENGTH_SHORT).show();
                baseActivity.finish();
            }
            else {
                Toast.makeText(baseActivity, baseActivity.getString(R.string.password_wrong), Toast.LENGTH_SHORT).show();
                //switch (jsonObject.getString("message")) { }
            }

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error_updating_password), Toast.LENGTH_SHORT).show();
        }
    }
}

