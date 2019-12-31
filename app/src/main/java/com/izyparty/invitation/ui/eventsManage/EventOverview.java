package com.izyparty.invitation.ui.eventsManage;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;
import com.izyparty.invitation.utils.network.Endpoints;
import com.izyparty.invitation.utils.network.requestMaker;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class EventOverview extends BaseActivity {
public Boolean highlight = false;
public String eventId="";
TextView going;
TextView NotGoing;
TextView UNKNOWN;
TextView invites_total;
TextView time_start;
TextView date_show;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_event_overview);
        baseCreate(this);
        setTitle(getString(R.string.party_overview));
        going=findViewById(R.id.number_yes);
        NotGoing=findViewById(R.id.number_no);
        UNKNOWN=findViewById(R.id.number_unknown);
        invites_total=findViewById(R.id.invites_total);
        date_show=findViewById(R.id.date_show);
        time_start=findViewById(R.id.time_start);
        eventId=getIntent().getStringExtra("eventId");
        highlight=getIntent().getBooleanExtra("highlight",false);
        HashMap<String,Object> map = new HashMap<>();
        map.put("eventId",eventId);
        new getStatus(this, Endpoints.GET_EVENT_STATUS,map).execute();
    }

    public void action(View v) {
        startActivity(new Intent(this, EventsManageOptionsScreen.class).putExtra("eventId",eventId).putExtra("highlight",highlight));
    }

    public void callback(JSONObject jsonObject) {
        try {
            if (!jsonObject.getBoolean("success")) {throw new Exception("NOT_SUCCESS");}
            going.setText(jsonObject.getString("going"));
            NotGoing.setText(jsonObject.getString("notGoing"));
            int unknown  = Integer.parseInt(jsonObject.getString("totalInvited"))-(Integer.parseInt(jsonObject.getString("going"))+Integer.parseInt(jsonObject.getString("notGoing")));
            UNKNOWN.setText(String.valueOf(unknown));
            invites_total.setText(jsonObject.getString("totalInvited")+" "+getString(R.string.invites));
            time_start.setText((jsonObject.getString("time")));
            date_show.setText(toDate(jsonObject.getString("date")));
        } catch (Throwable e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

static String toDate(String date) {
    SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
    SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy");
    try {
        String finalStr = outputFormat.format(inputFormat.parse(date));
        System.out.println(finalStr);
        return finalStr;
    } catch (Exception e) {
        e.printStackTrace();
    }
    return "";
}


}


class getStatus extends AsyncTask<Object,Void, JSONObject> {
    EventOverview baseActivity;
    String url;
    HashMap<String,Object> map;
    public getStatus(EventOverview baseActivity,String url,HashMap<String,Object> map) {
        super();
        Log.d(TAG, "addEvenetTask: CREATD TASK");
        this.baseActivity=baseActivity;
        this.url=url;
        this.map=map;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        baseActivity.recycleDialog();
        baseActivity.dialog=new ProgressDialog(baseActivity);
        baseActivity.dialog.setMessage(baseActivity.getString(R.string.loading1));
        baseActivity.dialog.show();

    }

    @Override
    protected JSONObject doInBackground(Object... objects) {
        try {
            Log.d(TAG, "doInBackground: "+url);
            Log.d(TAG, "doInBackground: "+map.toString());
            return requestMaker.makeRequests(url,map,baseActivity);
        } catch (Exception e )  {
            //Toast.makeText(baseActivity, "Error", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
            try {
                return new JSONObject("{\"success\":false,\"message\":\" " + e.getStackTrace().toString()+ " \"}");
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
        }
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject)  {
        super.onPostExecute(jsonObject);
        try {
            baseActivity.callback(jsonObject);
            baseActivity.dialog.cancel();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}