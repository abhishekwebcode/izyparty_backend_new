package com.izyparty.invitation.ui.eventsManage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.izyparty.invitation.R;
import com.izyparty.invitation.event.EventContacts;
import com.izyparty.invitation.templates.BaseActivity;
import com.izyparty.invitation.templates.showAttendees;
import com.izyparty.invitation.ui.home.EventsActivity;
import com.izyparty.invitation.utils.network.Endpoints;
import com.izyparty.invitation.utils.network.requestMaker;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.CompositePermissionListener;
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener;
import com.karumi.dexter.listener.single.PermissionListener;
import com.wafflecopter.multicontactpicker.ContactResult;
import com.wafflecopter.multicontactpicker.MultiContactPicker;
import com.wafflecopter.multicontactpicker.RxContacts.PhoneNumber;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class EventsManageOptionsScreen extends BaseActivity {
    View edit;
    View view;
    View contacts;
    String eventId;
    int CONTACT_PICKER_REQUEST = 234;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTACT_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                List<ContactResult> results = MultiContactPicker.obtainResult(data);
                ArrayList<String> numbers = new ArrayList<>();
                ArrayList<String> emails = new ArrayList<>();
                for (int i = 0; i < results.size(); i++) {
                    ContactResult contactResult = results.get(i);
                    List<PhoneNumber> r = contactResult.getPhoneNumbers();
                    for (int j = 0; j < r.size(); j++) {
                        PhoneNumber phoneNumber = r.get(j);
                        numbers.add(phoneNumber.getNumber());
                        break;
                    }
                    emails.addAll(contactResult.getEmails());
                }
                if (numbers.size() == 0 && emails.size() == 0) {
                    Toast.makeText(this, getString(R.string.no_attendees_selected), Toast.LENGTH_SHORT).show();
                }
                HashMap<String, Object> jj = new HashMap<>();
                jj.put("emails", new JSONArray(emails).toString());
                jj.put("numbers", new JSONArray(numbers).toString());
                Intent intent = new Intent(this, EventContacts.class);
                intent.putExtra("hashmap", jj);
                intent.putExtra("eventId", eventId);
                intent.putExtra("flow", "update");
                startActivity(intent);
            } else if (resultCode == RESULT_CANCELED) {
                System.out.println("Cancelled....");
                Toast.makeText(this, getString(R.string.cancelled_contact_select), Toast.LENGTH_SHORT).show();
            }
        }
    }

    public Boolean isHighlight = false;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events_manage_options_screen);
        if (getIntent().hasExtra("highlight")) {
            if (getIntent().getBooleanExtra("highlight", false)) {
                ((LinearLayout) findViewById(R.id.hightlight)).setBackgroundColor(getResources().getColor(R.color.colorPrimaryDark));
                ((TextView) findViewById(R.id.hightlightText)).setTextColor(Color.WHITE);
            }
        }
        edit = findViewById(R.id.edit);
        view = findViewById(R.id.view);
        contacts = findViewById(R.id.contacts);
        eventId = getIntent().getStringExtra("eventId");
        baseCreate(this);
        setTitle(getString(R.string.events_option_title));
    }

    public void viewAttendees(View v) {
        startActivity(new Intent(this, showAttendees.class).putExtra("id", eventId));
    }

    public void edit(View v) {
        startActivity(new Intent(this, EditEvent.class).putExtra("eventId", eventId));
    }

    public void viewit(View v) {
        startActivity(new Intent(this, seeResponsesNew.class).putExtra("eventId", eventId).putExtra("guest", false));
    }

    public void contact(View v) {
        PermissionListener dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
                .withContext(EventsManageOptionsScreen.this)
                .withTitle(getString(R.string.add_attendees))
                .withMessage(getString(R.string.contact_permission_text))
                .withButtonText(android.R.string.ok)
                .withIcon(R.mipmap.ic_launcher)
                .build();
        PermissionListener listener = new PermissionListener() {
            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {
                Toast.makeText(EventsManageOptionsScreen.this, getString(R.string.contact_permission_text), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                HashMap<String, Object> jj = new HashMap<>();
                jj.put("emails", new JSONArray().toString());
                jj.put("numbers", new JSONArray().toString());
                Intent intent = new Intent(EventsManageOptionsScreen.this, EventContacts.class);
                intent.putExtra("hashmap", jj);
                intent.putExtra("eventId", eventId);
                intent.putExtra("flow", "update");
                intent.putExtra("fromEdit",true);
                startActivity(intent);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };
        Dexter.withActivity(EventsManageOptionsScreen.this)
                .withPermission(Manifest.permission.READ_CONTACTS)
                .withListener(new CompositePermissionListener(dialogPermissionListener, listener))
                .check();
    }

    public void delete(View v) {
        new deleteTask(eventId, this).execute();
    }

    public void chase(View v) {
        new chaseTask(eventId, this).execute();
    }

}

class deleteTask extends AsyncTask<Void, Void, JSONObject> {
    String id;
    BaseActivity baseActivity;

    public deleteTask(String eventID, EventsManageOptionsScreen eventsManageOptionsScreen) {
        this.id = eventID;
        this.baseActivity = eventsManageOptionsScreen;
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
    protected JSONObject doInBackground(Void... voids) {
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("eventId", id);
            return requestMaker.makeRequests(Endpoints.DELETE_EVENT, map, baseActivity);
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
        try {
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(baseActivity, baseActivity.getString(R.string.event_deleted_succesfully), Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(baseActivity, EventsActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP).putExtra("order", "manage_events");
                baseActivity.startActivity(intent);
            } else {
                throw new Exception("NOT DONE");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error_deleteing_event), Toast.LENGTH_SHORT).show();
        }
    }
}

class chaseTask extends AsyncTask<Void, Void, JSONObject> {
    String id;
    EventsManageOptionsScreen baseActivity;

    public chaseTask(String eventID, EventsManageOptionsScreen eventsManageOptionsScreen) {
        this.id = eventID;
        this.baseActivity = eventsManageOptionsScreen;
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
    protected JSONObject doInBackground(Void... voids) {
        try {
            HashMap<String, Object> map = new HashMap<>();
            map.put("eventId", id);
            return requestMaker.makeRequests(Endpoints.CHASE, map, baseActivity);
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
            if (jsonObject.getBoolean("success")) {
                if (
                        jsonObject.has("send_sms") && (!("".equals(jsonObject.getString("send_sms_datas"))))
                ) {
                    String shareBody = baseActivity.getString(R.string.sms_start_text) + jsonObject.getString("sms_invite_link");
                    Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                    i.putExtra("address", jsonObject.getString("send_sms_datas"));
                    i.putExtra("sms_body", shareBody);
                    i.setType("vnd.android-dir/mms-sms");
                    //startActivityForResult(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)),CHOOSE_SHARE);
                    baseActivity.startActivityForResult(i, 2342);
                } else {
                    Toast.makeText(baseActivity, baseActivity.getString(R.string.NOTFI), Toast.LENGTH_SHORT).show();
                }
            } else {
                throw new Exception("NOT DONE");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error_notifying_guests), Toast.LENGTH_SHORT).show();
        }
    }
}