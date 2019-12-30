package com.easyparty.invitation.ui.eventsManage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.easyparty.invitation.R;
import com.easyparty.invitation.templates.BaseActivity;
import com.easyparty.invitation.templates.MapsActivitySelectPrebuilt;
import com.easyparty.invitation.utils.network.Endpoints;
import com.easyparty.invitation.utils.network.requestMaker;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.suke.widget.SwitchButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

import static android.content.ContentValues.TAG;

public class EditEvent extends BaseActivity {
    final int MAP_BUTTON_REQUEST_CODE = 23;
    EditText childName;
    EditText theme;
    EditText street;
    EditText city;
    EditText zipCode;
    public String latitude="";
    public String longitude="";
    EditText otherAddr;
    com.suke.widget.SwitchButton guestSee;
    com.suke.widget.SwitchButton specialTheme;
    com.jaredrummler.materialspinner.MaterialSpinner date;
    com.jaredrummler.materialspinner.MaterialSpinner time_start;
    com.jaredrummler.materialspinner.MaterialSpinner time_end;
    ImageView maplogo;
    String[] times;
    String id;


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==MAP_BUTTON_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    latitude=data.getStringExtra("latitude");
                    longitude=data.getStringExtra("longitude");
                    String url = "http://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=15&size=200x200&sensor=false&key="+getString(R.string.maps_api_key);
                    //Glide.with(this).load(url).optionalFitCenter().into(maplogo);
                    Toast.makeText(this, getString(R.string.success_map), Toast.LENGTH_SHORT).show();
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(this, getString(R.string.error_map_pick), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    public void setEventFields(JSONObject jsonObject1) throws Exception {

        if (jsonObject1.getBoolean("success")) {
            JSONObject jsonObject = jsonObject1.getJSONObject("event");
            if (jsonObject.has("latitude")) {
                latitude=jsonObject.getString("latitude");
                longitude=jsonObject.getString("longitude");
            }
            childName.setText(jsonObject.getString("childName"));
            street.setText(jsonObject.getString("street"));
            city.setText(jsonObject.getString("city"));
            zipCode.setText(jsonObject.getString("zipCode"));
            otherAddr.setText(jsonObject.getString("otherAddress"));
            String dateSet=jsonObject.getString("date");
            String newDate=convertMongoDate(dateSet);
            try {
                Boolean set=false;
                List<Object> list1 = date.getItems();
                for (int i = 0; i < list1.size(); i++) {
                    if (list1.get(i).toString().equals(newDate)) {
                        date.setSelectedIndex(i);
                        set=true;
                    }
                }
                if (!set) {
                    throw new Exception("NOT SET");
                }
            } catch (Exception e) {
                date.setText(newDate);
                e.printStackTrace();
            }

            guestSee.setChecked(jsonObject.getBoolean("guestSee"));
            try {
                List<Object> list = time_start.getItems();
                String timeStartReference = jsonObject.getString("timeStart");
                String timeEndReference = jsonObject.getString("timeEnd");
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i).toString().equals(timeStartReference)) {
                        time_start.setSelectedIndex(i);
                    }
                    if (list.get(i).toString().equals(timeEndReference)) {
                        time_end.setSelectedIndex(i);
                    }
                }
            } catch (Exception e) {e.printStackTrace();}
            if (jsonObject.getBoolean("isSpecialTheme")) {
                showTheme(jsonObject.getString("theme"));
            }
        }
    }

    public void showTheme(String theme1 ) {
        specialTheme.setChecked(true);
        theme.setText(theme1);
    }

    public static String convertMongoDate(String val) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MM yyyy");
        try {
            String finalStr = outputFormat.format(inputFormat.parse(val));
            System.out.println(finalStr);
            return finalStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    HashMap<String, Object> getMap() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("childName", getEdittextString(childName));
        map.put("theme", getEdittextString(theme));
        map.put("street", getEdittextString(street));
        map.put("city", getEdittextString(city));
        map.put("zipCode", getEdittextString(zipCode));
        map.put("otherAddress", getEdittextString(otherAddr));
        map.put("isSpecialTheme", String.valueOf(specialTheme.isChecked()));
        map.put("guestSee", String.valueOf(guestSee.isChecked()));
        map.put("date",
                String.valueOf(
                        ((MyDateInterval) date.getItems().get(date.getSelectedIndex())).mDate.getTime()
                )
        );
        map.put("timeStart", time_start.getItems().get(time_start.getSelectedIndex()).toString());
        map.put("timeEnd", time_end.getItems().get(time_end.getSelectedIndex()).toString());
        map.put("eventId",id);
        return map;
    }

    void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(this, getString(R.string.event_updated), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, getString(R.string.coudlnt_update_event), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.coudlnt_update_event), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_event);
        TextView toBold=(TextView)findViewById(R.id.setBold);
        //toBold.setText(Html.fromHtml(getString(R.string.indicate_all_the_informations_regarding_the_bithday_party_of_your_child_and_send_the_invitation_n_it_doesn_t_prevent_you_to_give_a_funny_invitation_card_to_the_kids)));
        (((ScrollView)findViewById(R.id.scrollView))).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null && event.getAction() == MotionEvent.ACTION_MOVE)
                {
                    InputMethodManager imm = ((InputMethodManager) EditEvent.this.getSystemService(Context.INPUT_METHOD_SERVICE));
                    boolean isKeyboardUp = imm.isAcceptingText();
                    if (isKeyboardUp)
                    {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });
        id=getIntent().getStringExtra("eventId");
        times = getResources().getStringArray(R.array.times);
        childName = (EditText) findViewById(R.id.childName);
        theme = (EditText) findViewById(R.id.theme);
        street = (EditText) findViewById(R.id.street);
        city = (EditText) findViewById(R.id.city);
        zipCode = (EditText) findViewById(R.id.zipCode);
        otherAddr = (EditText) findViewById(R.id.other_addr);
        guestSee = (com.suke.widget.SwitchButton) findViewById(R.id.guest_see_responses);
        specialTheme = (com.suke.widget.SwitchButton) findViewById(R.id.special_theme);
        date = (com.jaredrummler.materialspinner.MaterialSpinner) findViewById(R.id.date_spinner);
        time_start = (com.jaredrummler.materialspinner.MaterialSpinner) findViewById(R.id.time_start);
        time_end = (com.jaredrummler.materialspinner.MaterialSpinner) findViewById(R.id.time_end);
        maplogo = (ImageView) findViewById(R.id.maplogo);
        theme.setVisibility(View.INVISIBLE);
        ArrayList<MyDateInterval> items = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MM yyyy");
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(System.currentTimeMillis());
        c.add(Calendar.DATE, 60);
        Date endDate = c.getTime();
        Log.e(getClass().getSimpleName(), dateFormat.format(endDate));
        Calendar todayCalendar = Calendar.getInstance();
        while (endDate.after(todayCalendar.getTime())) {
            items.add(new MyDateInterval(dateFormat.format(todayCalendar.getTime()), todayCalendar.getTime()));
            todayCalendar.add(Calendar.DAY_OF_MONTH, 1);
        }
        ArrayAdapter<MyDateInterval> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, items);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        date.setAdapter(dataAdapter);

        ArrayAdapter<String> spinnerCountShoesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, times);
        time_start.setAdapter(spinnerCountShoesArrayAdapter);
        time_end.setAdapter(spinnerCountShoesArrayAdapter);
        specialTheme.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) theme.setVisibility(View.VISIBLE);
                else theme.setVisibility(View.INVISIBLE);
            }
        });

        findViewById(R.id.update).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,Object> map = getMap();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (entry.getKey().toString().equals("otherAddress")) {continue;}
                    if (entry.getKey().equals("theme") && !(specialTheme.isChecked())) {
                        continue;
                    } else {
                        if (entry.getValue().toString().equals("")) {
                            Toast.makeText(EditEvent.this, getString(R.string.fill_all_fields_banner), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                if (!("".equals(latitude))) {
                    map.put("latitude",latitude);
                    map.put("longitude",longitude);
                }
                new updateEvent(EditEvent.this,Endpoints.UPDATE_EVENT,map).execute();
            }
        });
        baseCreate(this);
        setTitle(getString(R.string.edit_party));
        HashMap<String,Object> map = new HashMap<>();
        map.put("eventId",id);
        new getEventExisting(this, Endpoints.GET_EVENT_INFO,map).execute();
    }

    String getEdittextString(EditText t) {
        return t.getText().toString();
    }
    class MyDateInterval {
        public String mDateString;
        public Date mDate;

        public MyDateInterval(String dateString, Date date) {
            mDateString = dateString;
            mDate = date;
        }

        @Override
        public String toString() {
            return mDateString;
        }
    }


    public void onPickLocation(final View v) {
        try {
            PermissionListener dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
                    .withContext(EditEvent.this)
                    .withTitle(R.string.error_map_pick)
                    .withMessage(R.string.contact_permission_text)
                    .withButtonText(android.R.string.ok)
                    .withIcon(R.mipmap.ic_launcher)
                    .build();
            PermissionListener listener = new PermissionListener() {
                @Override
                public void onPermissionDenied(PermissionDeniedResponse response) {
                }

                @Override
                public void onPermissionGranted(PermissionGrantedResponse response) {
                    Intent intent = new Intent(EditEvent.this, MapsActivitySelectPrebuilt.class);
                    if (!("".equals(latitude))) {
                        intent.putExtra("latitude",latitude).putExtra("longitude",longitude);
                    }
                    try {
                        startActivityForResult(intent,MAP_BUTTON_REQUEST_CODE);
                    } catch (Exception e ) {
                        Toast.makeText(EditEvent.this, getString(R.string.error_map_pick), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                    token.continuePermissionRequest();
                }
            };
            Dexter.withActivity(EditEvent.this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new CompositePermissionListener(dialogPermissionListener, listener))
                    .check();
            // TODO MAP PICK
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_map_pick), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}

class getEventExisting extends AsyncTask<Object, Void, JSONObject> {
    EditEvent baseActivity;
    String url;
    HashMap<String, Object> map;
    public getEventExisting(EditEvent baseActivity, String url, HashMap<String, Object> map) {
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
            baseActivity.dialog.cancel();
            baseActivity.dialog.dismiss();
            if (jsonObject.getBoolean("success")) {
                baseActivity.setEventFields(jsonObject);
                baseActivity.maplogo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //baseActivity.onPickLocation(v);
                    }
                });
            }
            else {
                throw new Exception("ERROR_JSON");
            }
        } catch (Throwable e) {
            e.printStackTrace();
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error_fetching_invite), Toast.LENGTH_SHORT).show();
            baseActivity.finish();
        }
    }
}

class updateEvent extends AsyncTask<Object, Void, JSONObject> {
    EditEvent baseActivity;
    String url;
    HashMap<String, Object> map;
    public updateEvent(EditEvent baseActivity, String url, HashMap<String, Object> map) {
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
            baseActivity.dialog.cancel();
            baseActivity.dialog.dismiss();
            if (jsonObject.getBoolean("success")) {
                baseActivity.callback(jsonObject);
            }
            else {
                throw new Exception("ERROR_JSON");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error_updating_event), Toast.LENGTH_SHORT).show();
            baseActivity.finish();
        }
    }
}