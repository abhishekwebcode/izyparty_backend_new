package com.easyparty.invitation.ui.home;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;
import com.easyparty.invitation.R;
import com.easyparty.invitation.event.EventContacts;
import com.easyparty.invitation.templates.BaseActivity;
import com.easyparty.invitation.templates.MapsActivitySelectPrebuilt;
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
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.LimitColumn;
import com.wafflecopter.multicontactpicker.MultiContactPicker;
import com.wafflecopter.multicontactpicker.RxContacts.PhoneNumber;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.*;

import static android.content.ContentValues.TAG;

public class AddEventPage1 extends BaseActivity {
    final int CONTACT_PICKER_REQUEST = 32;
    String latitude = "";
    String longitude = "";
    EditText childName;
    EditText theme;
    EditText street;
    EditText city;
    EditText zipCode;
    EditText otherAddr;
    com.suke.widget.SwitchButton guestSee;
    com.suke.widget.SwitchButton specialTheme;
    com.jaredrummler.materialspinner.MaterialSpinner date;
    com.jaredrummler.materialspinner.MaterialSpinner time_start;
    com.jaredrummler.materialspinner.MaterialSpinner time_end;
    ImageView maplogo;
    final int MAP_BUTTON_REQUEST_CODE = 23;
    addEvenetTask task;
    String[] times;
    List<String> times1;
    ArrayList<MyDateInterval> items;


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
        String time_start_string = time_start.getItems().get(time_start.getSelectedIndex()).toString();
        String time_end_string = time_end.getItems().get(time_end.getSelectedIndex()).toString();
        if (!getString(R.string.please_select_time_slot).equals(time_start_string)) {
            map.put("timeStart", time_start_string);
        } else {
            map.put("timeStart", "");
        }
        if (!getString(R.string.please_select_time_slot).equals(time_end_string)) {
            map.put("timeEnd", time_end_string);
        } else {
            map.put("timeEnd", "");
        }
        return map;
    }

    void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(this, getString(R.string.event_created), Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, getString(R.string.couldnt_party_create), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.couldnt_party_create), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTACT_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                List<ContactResult> results = MultiContactPicker.obtainResult(data);
                ArrayList<String> numbers = new ArrayList<>();
                ArrayList<String> emails = new ArrayList<>();
                JSONArray number2s = new JSONArray();
                for (int i = 0; i < results.size(); i++) {
                    ContactResult contactResult = results.get(i);
                    List<PhoneNumber> r = contactResult.getPhoneNumbers();
                    for (int j = 0; j < r.size(); j++) {
                        PhoneNumber phoneNumber = r.get(j);
                        JSONArray arr = new JSONArray();
                        arr.put(contactResult.getDisplayName());
                        arr.put(phoneNumber.getNumber());
                        number2s.put(arr);
                        numbers.add(phoneNumber.getNumber());
                        //Only pick 1 phone per contact to avoid duplicate numbers
                        break;
                    }
                    //emails.addAll(contactResult.getEmails());
                }
                Log.d(TAG, "onActivityResult: " + Arrays.toString(numbers.toArray()));
                if (numbers.size() == 0) {
                    Toast.makeText(this, getString(R.string.no_attendees_selected), Toast.LENGTH_SHORT).show();
                }
                HashMap<String, Object> map = getMap();
                map.put("latitude", latitude);
                map.put("longitude", longitude);
                JSONObject eventToAdd = new JSONObject(map);
                String event = eventToAdd.toString();
                HashMap<String, Object> jj = new HashMap<>();
                jj.put("event", event);
                jj.put("emails", new JSONArray().toString());
                jj.put("numbers", number2s);
                Intent intent = new Intent(this, EventContacts.class);
                intent.putExtra("newNumbersListWithNames", number2s.toString());
                intent.putExtra("hashmap", jj);
                intent.putExtra("event", event);
                startActivity(intent);
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("Cancelled....");
                Toast.makeText(this, getString(R.string.cancelled_contact_select), Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == MAP_BUTTON_REQUEST_CODE) {
            switch (resultCode) {
                case RESULT_OK:
                    latitude = data.getStringExtra("latitude");
                    longitude = data.getStringExtra("longitude");
                    String url = "http://maps.google.com/maps/api/staticmap?center=" + latitude + "," + longitude + "&zoom=15&size=200x200&sensor=false&key=" + getString(R.string.maps_api_key);
                    //Glide.with(this).load(url).optionalFitCenter().into(maplogo);
                    Toast.makeText(this, getString(R.string.success_map), Toast.LENGTH_SHORT).show();
                    break;
                case RESULT_CANCELED:
                    Toast.makeText(this, getString(R.string.error_map_pick), Toast.LENGTH_SHORT).show();
                    break;
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_add_event_page1);
        TextView toBold=(TextView)findViewById(R.id.setBold);
        //toBold.setText(Html.fromHtml(getString(R.string.indicate_all_the_informations_regarding_the_bithday_party_of_your_child_and_send_the_invitation_n_it_doesn_t_prevent_you_to_give_a_funny_invitation_card_to_the_kids)));
        setTitle(getString(R.string.add_event_title));
        (((ScrollView) findViewById(R.id.scrollview))).setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event != null && event.getAction() == MotionEvent.ACTION_MOVE) {
                    InputMethodManager imm = ((InputMethodManager) AddEventPage1.this.getSystemService(Context.INPUT_METHOD_SERVICE));
                    boolean isKeyboardUp = imm.isAcceptingText();
                    if (isKeyboardUp) {
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                    }
                }
                return false;
            }
        });
        times = getResources().getStringArray(R.array.times);
        times1 = Arrays.asList(times);
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
        maplogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //onPickLocation(v);
            }
        });
        theme.setVisibility(View.INVISIBLE);
        items = new ArrayList<>();
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
        ArrayAdapter<MyDateInterval> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        date.setItems(items);
        ArrayAdapter<String> timesAdapterOld = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_dropdown_item, times);
        time_start.setItems(times);
        time_end.setItems(times);
        specialTheme.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                if (isChecked) theme.setVisibility(View.VISIBLE);
                else theme.setVisibility(View.INVISIBLE);
            }
        });
        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = getMap();
                for (Map.Entry<String, Object> entry : map.entrySet()) {
                    if (entry.getKey().toString().equals("otherAddress")) {
                        continue;
                    }
                    if (entry.getKey().equals("theme") && !(specialTheme.isChecked())) {
                        continue;
                    } else {
                        if (entry.getValue().toString().equals("")) {
                            Toast.makeText(AddEventPage1.this, getString(R.string.fill_all_fields), Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }
                Date timeStart = items.get(time_start.getSelectedIndex()).mDate;
                Date timeEnd = items.get(time_end.getSelectedIndex()).mDate;
                if (!(timeEnd.after(timeStart))) {
                    Toast.makeText(AddEventPage1.this, getString(R.string.time_warning), Toast.LENGTH_SHORT).show();
                    return;
                }
                PermissionListener dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
                        .withContext(AddEventPage1.this)
                        .withTitle(R.string.add_attendees)
                        .withTitle(R.string.add_attendees)
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

                        HashMap<String, Object> map = getMap();
                        map.put("latitude", latitude);
                        map.put("longitude", longitude);
                        JSONObject eventToAdd = new JSONObject(map);
                        String event = eventToAdd.toString();
                        Intent intent = new Intent(AddEventPage1.this, EventContacts.class);
                        intent.putExtra("event", event);
                        startActivity(intent);
                        if (true) {
                            return;
                        }
                        new MultiContactPicker.Builder(AddEventPage1.this) //Activity/fragment context
                                //.theme(R.style.MyCustomPickerTheme) //Optional - default: MultiContactPicker.Azure
                                .hideScrollbar(false) //Optional - default: false
                                .showTrack(true) //Optional - default: true
                                .searchIconColor(Color.RED) //Option - default: White
                                .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE) //Optional - default: CHOICE_MODE_MULTIPLE
                                .handleColor(ContextCompat.getColor(AddEventPage1.this, R.color.colorAccent)) //Optional - default: Azure Blue
                                .bubbleColor(ContextCompat.getColor(AddEventPage1.this, R.color.colorPrimary)) //Optional - default: Azure Blue
                                .bubbleTextColor(Color.WHITE) //Optional - default: White
                                .setTitleText(getString(R.string.select_attendees_title)) //Optional - default: Select Contacts
                                //.setSelectedContacts("10", "5" / myList) //Optional - will pre-select contacts of your choice. String... or List<ContactResult>
                                .setLoadingType(MultiContactPicker.LOAD_SYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                                .limitToColumn(LimitColumn.NONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                                .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out,
                                        android.R.anim.fade_in,
                                        android.R.anim.fade_out) //Optional - default: No animation overrides
                                .showPickerForResult(CONTACT_PICKER_REQUEST);
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }
                };
                Dexter.withActivity(AddEventPage1.this)
                        .withPermission(Manifest.permission.READ_CONTACTS)
                        .withListener(new CompositePermissionListener(dialogPermissionListener, listener))
                        .check();
                return;
            }
        });
        baseCreate(this);

    }

    String getEdittextString(EditText t) {
        return t.getText().toString();
    }

    public void onPickLocation(final View v) {
        try {
            PermissionListener dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
                    .withContext(AddEventPage1.this)
                    .withTitle(R.string.add_attendees)
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
                    try {
                        startActivityForResult(new Intent(AddEventPage1.this, MapsActivitySelectPrebuilt.class), MAP_BUTTON_REQUEST_CODE);
                    } catch (Exception e) {
                        Toast.makeText(AddEventPage1.this, getString(R.string.error_map_pick), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                    token.continuePermissionRequest();
                }
            };
            Dexter.withActivity(AddEventPage1.this)
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


class addEvenetTask extends AsyncTask<Object, Void, JSONObject> {
    AddEventPage1 baseActivity;
    String url;
    HashMap<String, Object> map;

    public addEvenetTask(AddEventPage1 baseActivity, String url, HashMap<String, Object> map) {
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

    }

    @Override
    protected JSONObject doInBackground(Object... objects) {
        try {
            Log.d(TAG, "doInBackground: " + url);
            Log.d(TAG, "doInBackground: " + map.toString());
            return requestMaker.makeRequests(url, map, baseActivity);
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
        try {
            baseActivity.callback(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
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