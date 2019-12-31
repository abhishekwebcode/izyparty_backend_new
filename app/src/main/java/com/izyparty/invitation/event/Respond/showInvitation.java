package com.izyparty.invitation.event.Respond;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;
import com.izyparty.invitation.ui.new1.Allergy;
import com.izyparty.invitation.utils.network.Endpoints;
import com.izyparty.invitation.utils.network.requestMaker;
import com.pixplicity.easyprefs.library.Prefs;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class showInvitation extends BaseActivity {
    TextView childName;
    TextView theme;
    TextView street;
    TextView city;
    TextView zipCode;
    TextView otherAddr;
    TextView date;
    TextView time_start;
    TextView time_end;
    TextView user;
    String owner;
    String eventID;
    View themeBanner;
    View themeLine;
    LinearLayout question_bottom;
    LinearLayout respond_container;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_invitation);
        question_bottom=findViewById(R.id.hide_1);
        respond_container=findViewById(R.id.hide_2);
        eventID = getIntent().getStringExtra("event_id");
        childName = (TextView) findViewById(R.id.childName);
        theme = (TextView) findViewById(R.id.theme);
        street = (TextView) findViewById(R.id.street);
        city = (TextView) findViewById(R.id.city);
        zipCode = (TextView) findViewById(R.id.zipCode);
        otherAddr = (TextView) findViewById(R.id.other_addr);
        date = (TextView) findViewById(R.id.date_spinner);
        time_start = (TextView) findViewById(R.id.time_start);
        time_end = (TextView) findViewById(R.id.time_end);
        user = (TextView) findViewById(R.id.user);
        themeBanner = findViewById(R.id.themeBanner);
        themeLine = findViewById(R.id.themeLine);
        theme.setVisibility(View.GONE);
        themeBanner.setVisibility(View.GONE);
        themeLine.setVisibility(View.GONE);
        HashMap<String,Object> map = new HashMap<>();
        map.put("eventId",eventID);
        //new checkInviteTask(this,Endpoints.INVITE_CHECK,map,eventID).execute();
        baseCreate(this);
        HashMap<String, Object> map1 = new HashMap<>();
        map1.put("eventId", eventID);
        baseCreate(this);
        new getInviteInfo(this, Endpoints.GET_INVITE, map1).execute();
        findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accept();
            }
        });
        findViewById(R.id.reject).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                reject();
            }
        });
        if (getIntent().hasExtra("from")) {
            if (getIntent().getStringExtra("from").equals("notif")) {
                int existing= Prefs.getInt("invitation",0);
                if (existing!=0) {Prefs.putInt("invitation",existing-1);}
            }
        }
    }

    public void accept() {
        startActivity(new Intent(this, Allergy.class).putExtra("eventId", eventID).putExtra("owner", owner));
    }

    public void reject() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("eventId", eventID);
        new RejectTask(this, Endpoints.REJECT_INVITE, map).execute();
    }

    public void showTheme(String theme1) {
        theme.setVisibility(View.VISIBLE);
        themeBanner.setVisibility(View.VISIBLE);
        themeLine.setVisibility(View.VISIBLE);
        theme.setText(theme1);
    }

    public void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                JSONObject first = jsonObject;
                jsonObject = jsonObject.getJSONObject("invite");
                childName.setText(jsonObject.getString("childName"));
                street.setText(jsonObject.getString("street"));
                city.setText(jsonObject.getString("city"));
                zipCode.setText(jsonObject.getString("zipCode"));
                otherAddr.setText(jsonObject.getString("otherAddress"));
                date.setText(
                        convertMongoDate(jsonObject.getString("date"))
                );
                owner = first.getJSONObject("owner").getString("name");
                //user.setText(first.getJSONObject("owner").getString("name")+" " + getString(R.string.has_invited_you));
                user.setText(jsonObject.getString("childName")+" " + getString(R.string.has_invited_you));
                time_start.setText(jsonObject.getString("timeStart"));
                time_end.setText(jsonObject.getString("timeEnd"));
                try {
                    if (true) throw new Exception("");
                    if (!("".equals(jsonObject.getString("latitude")))) {
                        findViewById(R.id.map_header).setVisibility(View.VISIBLE);
                        ImageView map = (ImageView)findViewById(R.id.maplogo);
                        map.setVisibility(View.VISIBLE);
                        final String latitude=jsonObject.getString("latitude");
                        final String longitude=jsonObject.getString("longitude");
                        String url = "http://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=12&size=200x200&sensor=false&key="+getString(R.string.maps_api_key);
                        //Glide.with(this).load(url).fitCenter().optionalCenterInside().into(map);
                        map.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?daddr="+latitude+","+longitude));
                                startActivity(intent);
                            }
                        });
                    }
                    else {
                        findViewById(R.id.map_header_disable).setVisibility(View.VISIBLE);
                    }
                } catch (Throwable e ) {e.printStackTrace();}

                if (jsonObject.getBoolean("isSpecialTheme")) {
                    showTheme(jsonObject.getString("theme"));
                }
            } else {
                throw new Exception("errorResponse");
            }
        } catch (Exception e) {
            Log.d(TAG, "callback: COULDNT LOAD INVITE");
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.coudlnt_load_invite), Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public static String convertMongoDate(String val) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("yyyy/MM/dd");
        try {
            String finalStr = outputFormat.format(inputFormat.parse(val));
            System.out.println(finalStr);
            return finalStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

}

class getInviteInfo extends AsyncTask<Object, Void, JSONObject> {
    showInvitation baseActivity;
    String url;
    HashMap<String, Object> map;

    public getInviteInfo(showInvitation baseActivity, String url, HashMap<String, Object> map) {
        super();
        Log.d(TAG, "addEvenetTask: CREATD TASK");
        this.baseActivity = baseActivity;
        this.url = url;
        this.map = map;
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
            baseActivity.callback(jsonObject);
            baseActivity.dialog.cancel();
            Log.d(TAG, "onPostExecute: JSON OBJECT FOR CHECKING INVITE");
            Log.d(TAG, "onPostExecute: "+jsonObject.toString(2));
            final JSONObject temp=jsonObject;
            if (!jsonObject.getBoolean("sent")) {
                baseActivity.respond_container.setVisibility(View.VISIBLE);
            } else {
                baseActivity.question_bottom.setVisibility(View.VISIBLE);
                String initial = temp.getString("intention").equals("going")?baseActivity.getString(R.string.going):baseActivity.getString(R.string.notgoing);
                ((TextView)baseActivity.findViewById(R.id.question_resubmit)).setText(initial
                //        + baseActivity.getString(R.string.change_response_qustion)
                );
                ((TextView)baseActivity.findViewById(R.id.question_resubmit)).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        baseActivity.question_bottom.setVisibility(View.GONE);
                        baseActivity.respond_container.setVisibility(View.VISIBLE);
                        if (true) return;
                        AlertDialog.Builder builder;
                        final DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                switch (which){
                                    case DialogInterface.BUTTON_POSITIVE:
                                        //Yes button clicked
                                        baseActivity.question_bottom.setVisibility(View.GONE);
                                        baseActivity.respond_container.setVisibility(View.VISIBLE);
                                        break;
                                    case DialogInterface.BUTTON_NEGATIVE:
                                        //No button clicked
                                        break;
                                }
                            }
                        };
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            builder = new AlertDialog.Builder(baseActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
                        } else {
                            builder = new AlertDialog.Builder(baseActivity);
                        }
                        try {
                            /**
                             * @deprecated
                             */
                            builder.setMessage(baseActivity.getString(R.string.going) + temp.getString("intention") + baseActivity.getString(R.string.change_response_qustion))
                                    .setPositiveButton(R.string.yes, dialogClickListener)
                                    .setNegativeButton(R.string.no, dialogClickListener).show();
                        } catch (Throwable e) {e.printStackTrace();}
                    }
                });
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

class RejectTask extends AsyncTask<Object, Void, JSONObject> {
    showInvitation baseActivity;
    String url;
    HashMap<String, Object> map;

    public RejectTask(showInvitation baseActivity, String url, HashMap<String, Object> map) {
        super();
        Log.d(TAG, "addEvenetTask: CREATD TASK");
        this.baseActivity = baseActivity;
        this.url = url;
        this.map = map;
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
            baseActivity.finish();
            baseActivity.dialog.cancel();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error_rejecting_event), Toast.LENGTH_SHORT).show();
        }
    }
}

class checkInviteTask extends AsyncTask<Object, Void, JSONObject> {
    showInvitation baseActivity;
    String url;
    HashMap<String, Object> map;
    String id;

    public checkInviteTask(showInvitation baseActivity, String url, HashMap<String, Object> map,String id) {
        super();
        Log.d(TAG, "addEvenetTask: CREATD TASK");
        this.baseActivity = baseActivity;
        this.url = url;
        this.map = map;
        this.id=id;
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
            baseActivity.dialog.dismiss();
            if (jsonObject.getBoolean("success")) {

            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error_fetching_invite), Toast.LENGTH_SHORT).show();
        }
    }
}