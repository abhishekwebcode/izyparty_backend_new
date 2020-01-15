package com.izyparty.invitation.event;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatButton;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;
import com.izyparty.invitation.ui.home.HomeActivity;
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
import com.wafflecopter.multicontactpicker.LimitColumn;
import com.wafflecopter.multicontactpicker.MultiContactPicker;
import com.wafflecopter.multicontactpicker.RxContacts.PhoneNumber;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static android.content.ContentValues.TAG;
import static android.view.View.GONE;

public class EventContacts extends BaseActivity {
    Intent intent;
    HashMap<String, Object> hashMap;
    ListView listView;
    ArrayList<NumberItem> numbers;
    JSONArray emails;
    CustomListAdapter adapter;
    String newCOntactString;
    String newCOntactName;
    public int CHOOSE_SHARE = 3247;
    addEvenetTask1 task;
    Boolean onbackhome = false;
    final public int CHOOSE_CONTACTS = 567;
    AppCompatButton create;
    Boolean fromEdit=false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_event_contacts);
        setTitle(getString(R.string.participants));
        create=findViewById(R.id.create);
        intent = getIntent();
        listView = findViewById(R.id.listview);
        if (getIntent().hasExtra("fromEdit")) {
            fromEdit=true;
        }
        if (getIntent().hasExtra("flow")) {
            ((AppCompatButton) findViewById(R.id.create)).setText(getString(R.string.update_contacts));
        }
        try {
            hashMap = (HashMap<String, Object>) intent.getExtras().get("hashmap");
            // JSONArray array  = new JSONArray(hashMap.get("numbers").toString());
            // JSONArray arrayReference  = new JSONArray(hashMap.get("numbers").toString());
            // JSONArray filter = new JSONArray();
            // for (int i = 0; i < array.length() ; i++) { }
            //numbers = new JSONArray(hashMap.get("numbers").toString());
            numbers = new ArrayList<NumberItem>();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_loading_contacts), Toast.LENGTH_SHORT).show();
        }
        baseCreate(this);
        buildList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 3247:
                startTomain();
                break;
            case CHOOSE_CONTACTS:
                switch (resultCode) {
                    case RESULT_OK:
                        List<ContactResult> results = MultiContactPicker.obtainResult(data);
                        for (int i = 0; i < results.size(); i++) {
                            ContactResult contactResult = results.get(i);
                            List<PhoneNumber> r = contactResult.getPhoneNumbers();
                            for (int j = 0; j < r.size(); j++) {
                                PhoneNumber phoneNumber = r.get(j);
                                String number = phoneNumber.getNumber();
                                number.replaceAll("[^0-9\\+]", "");
                                adapter.items.add(new NumberItem(contactResult.getDisplayName(),number,number));
                                break;
                            }
                        }
                        adapter.notifyDataSetChanged();
                        break;
                    case RESULT_CANCELED:
                        Toast.makeText(this, getString(R.string.cancelled_contact_select), Toast.LENGTH_SHORT).show();
                        break;
                }
                break;
        }
    }

    void startTomain() {
        startActivity(new Intent(this, HomeActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK));
        finish();
    }

    void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(this, getString(R.string.event_updated_to_attendees), Toast.LENGTH_SHORT).show();
                if (
                        jsonObject.has("send_sms") &&
                                (!("".equals(jsonObject.getString("send_sms_datas"))))
                ) {
                    String shareBody = getString(R.string.sms_start_text) + jsonObject.getString("sms_invite_link");
                    Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                    i.putExtra("address", jsonObject.getString("send_sms_datas"));
                    i.putExtra("sms_body", shareBody);
                    i.setType("vnd.android-dir/mms-sms");
                    //startActivityForResult(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)),CHOOSE_SHARE);
                    startActivityForResult(i, CHOOSE_SHARE);
                    onbackhome = true;
                } else {
                    startTomain();
                }
            } else {
                Toast.makeText(this, getString(R.string.couldnt_party_create), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.couldnt_party_create), Toast.LENGTH_SHORT).show();
        }
    }

    public void addContact(View v) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(getString(R.string.add_phone_email));
        View dialogView = getLayoutInflater().inflate(R.layout.new_contact_custom,null);
        builder.setView(dialogView);
        final EditText name = dialogView.findViewById(R.id.name);
        final EditText number = dialogView.findViewById(R.id.number);
        builder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Toast.makeText(EventContacts.this, getString(R.string.error), Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        final AlertDialog dialog = builder.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newCOntactString = number.getText().toString();
                newCOntactName = name.getText().toString();
                if (newCOntactString.isEmpty() || newCOntactName.isEmpty()) {
                    Toast.makeText(EventContacts.this, getString(R.string.please_custom_detail), Toast.LENGTH_SHORT).show();
                    return;
                }
                newCOntactString = number.getText().toString();
                if (newCOntactString.contains("+")) {
                    if (!(newCOntactString.substring(1).matches("[0-9]+"))) {
                        Toast.makeText(EventContacts.this, getString(R.string.celular_or_email), Toast.LENGTH_SHORT).show();
                        return;
                    }
                } else {
                    if (!(newCOntactString.matches("[0-9]+"))) {
                        Toast.makeText(EventContacts.this, getString(R.string.celular_or_email), Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                adapter.items.add(new NumberItem(newCOntactName, newCOntactString, newCOntactString));
                adapter.notifyDataSetChanged();
                //EventContacts.this.findViewById(R.id.add_attendee).setVisibility(GONE);
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
    }

    public void createEvent(View v) {
        try {
            if (getIntent().hasExtra("flow")) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("eventId", getIntent().getStringExtra("eventId"));
                map.put("data", adapter.getSerialised());
                map.put("names", adapter.getNamesSerialised());
                addEvenetTask1 task = new addEvenetTask1(EventContacts.this, Endpoints.UPDATE_EVENT_CONTATCS, map);
                task.goCallback = false;
                task.execute();
                return;
            }
            if (task != null) {
                task.cancel(true);
                task = null;
            }
            HashMap<String, Object> jj = new HashMap<>();
            jj.put("event", getIntent().getStringExtra("event"));
            jj.put("data", adapter.getSerialised());
            jj.put("names", adapter.getNamesSerialised());
            task = new addEvenetTask1(EventContacts.this, Endpoints.ADD_EVENT, jj);
            task.execute();
        } catch (Throwable e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();
        }
    }

    public void buildList() {
        adapter = new CustomListAdapter(this, numbers);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    public void deleteItem(int position) {
        adapter.items.remove(position);
        adapter.notifyDataSetChanged();

    }

    public void addNumber(View v) {
        org.michaelbel.bottomsheet.BottomSheet.Builder builder = new org.michaelbel.bottomsheet.BottomSheet.Builder(this);
        builder.setTitle(getString(R.string.add_contacts));
        builder.setMenu(R.menu.menu_contacts, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 1:
                        EventContacts.this.addContact(null);
                        break;
                    case 0:
                        EventContacts.this.showPhoneBookPIcker(null);
                        break;
                }
            }
        }).setFullWidth(true).show();
        /*
        new BottomSheet.Builder(this).title(getString(R.string.add_contacts)).sheet(R.menu.menu_contacts).listener(new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case R.id.custom:
                        addContact(null);
                        break;
                    case R.id.phonebook:
                        showPhoneBookPIcker(null);
                        break;
                }
            }
        }).show();
         */
    }

    public void showPhoneBookPIcker(View v) {
        PermissionListener dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
                .withContext(EventContacts.this)
                .withTitle(R.string.add_attendees)
                .withMessage(R.string.contact_permission_text)
                .withButtonText(android.R.string.ok)
                .withIcon(R.mipmap.ic_launcher)
                .build();
        PermissionListener listener = new PermissionListener() {
            @Override
            public void onPermissionDenied(PermissionDeniedResponse response) {}

            @Override
            public void onPermissionGranted(PermissionGrantedResponse response) {
                new MultiContactPicker.Builder(EventContacts.this) //Activity/fragment context //.theme(R.style.MyCustomPickerTheme) //Optional - default: MultiContactPicker.Azure
                        .hideScrollbar(false) //Optional - default: false
                        .showTrack(true) //Optional - default: true
                        .searchIconColor(Color.RED) //Option - default: White
                        .setChoiceMode(MultiContactPicker.CHOICE_MODE_MULTIPLE) //Optional - default: CHOICE_MODE_MULTIPLE
                        .handleColor(ContextCompat.getColor(EventContacts.this, R.color.colorAccent)) //Optional - default: Azure Blue
                        .bubbleColor(ContextCompat.getColor(EventContacts.this, R.color.colorPrimary)) //Optional - default: Azure Blue
                        .bubbleTextColor(Color.WHITE) //Optional - default: White
                        .setTitleText(getString(R.string.select_attendees_title)) //Optional - default: Select Contacts //.setSelectedContacts("10", "5" / myList) //Optional - will pre-select contacts of your choice. String... or List<ContactResult>
                        .setLoadingType(MultiContactPicker.LOAD_SYNC) //Optional - default LOAD_ASYNC (wait till all loaded vs stream results)
                        .limitToColumn(LimitColumn.PHONE) //Optional - default NONE (Include phone + email, limiting to one can improve loading time)
                        .setActivityAnimations(android.R.anim.fade_in, android.R.anim.fade_out, android.R.anim.fade_in, android.R.anim.fade_out) //Optional - default: No animation overrides
                        .showPickerForResult(CHOOSE_CONTACTS);
            }

            @Override
            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                token.continuePermissionRequest();
            }
        };
        Dexter.withActivity(EventContacts.this)
                .withPermission(Manifest.permission.READ_CONTACTS)
                .withListener(new CompositePermissionListener(dialogPermissionListener, listener))
                .check();
    }

    public void showSubmit() {
        String text;
        if (adapter.items.size()==0) {

            if (fromEdit) {
                text="";
                create.setVisibility(View.GONE);
            } else {
                create.setVisibility(View.VISIBLE);
                text = getString(R.string.add_participants_later);
            }
        } else {
            create.setVisibility(View.VISIBLE);
            text = getString(R.string.validate_the_event);
        }
        create.setText(text);
    }

}

class NumberItem {


    public String name = "";
    public String displayNumber = "";
    public String number = "";

    public NumberItem(String a, String b, String c) {
        this.name = a;
        this.displayNumber = b;
        this.number = c;
    }
}

class CustomListAdapter extends BaseAdapter {
    private EventContacts context; //context
    public ArrayList<NumberItem> items; //data source of the list adapter

    //public constructor
    public CustomListAdapter(EventContacts context, ArrayList<NumberItem> items) {
        this.context = context;
        this.items = items;
    }
    public String getNamesSerialised() throws JSONException {
        JSONObject jsonObject = new JSONObject();
        for (int i = 0; i < items.size(); i++) {
            jsonObject.put(items.get(i).name,items.get(i).number);
        }
        return  jsonObject.toString();
    }
    @Override
    public int getCount() {
        return items.size(); //returns total of items in the list
    }

    public String getSerialised() {
        JSONArray tempNumbers = new JSONArray();
        for (int i = 0; i < items.size(); i++) {
            tempNumbers.put(items.get(i).number);
        }
        return tempNumbers.toString();
    }

    @Override
    public NumberItem getItem(int position) {
        return items.get(position);
    }

    @Override
    public void notifyDataSetChanged() {
        super.notifyDataSetChanged();
        context.showSubmit();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        NumberItem item = items.get(position);
        convertView = LayoutInflater.from(context).inflate(R.layout.contact_item_row, parent, false);
        TextView data = convertView.findViewById(R.id.name);
        TextView data2 = convertView.findViewById(R.id.date_event);
        ImageView delete = convertView.findViewById(R.id.imageView3);
        data.setText(item.name);
        data2.setText(item.displayNumber);
        final int position1 = position;
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: " + String.valueOf(position1));
                context.deleteItem(position1);
            }
        });
        return convertView;
    }
}


class addEvenetTask1 extends AsyncTask<Object, Void, JSONObject> {
    EventContacts baseActivity;
    String url;
    HashMap<String, Object> map;
    public Boolean goCallback = true;

    public addEvenetTask1(EventContacts baseActivity, String url, HashMap<String, Object> map) {
        super();
        Log.d(TAG, "addEvenetTask: UPDATING TASK");
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
            return requestMaker.makeRequests(url, map, baseActivity);
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
            if (goCallback) {
                baseActivity.callback(jsonObject);
            } else {
                try {
                    if (jsonObject.getBoolean("success")) {
                        Toast.makeText(baseActivity, baseActivity.getString(R.string.successfully_updated), Toast.LENGTH_SHORT).show();
                        if (
                                jsonObject.has("send_sms") && (!("".equals(jsonObject.getString("send_sms_datas"))))
                        ) {
                            String shareBody = baseActivity.getString(R.string.sms_start_text) + jsonObject.getString("sms_invite_link");
                            Intent i = new Intent(android.content.Intent.ACTION_VIEW);
                            i.putExtra("address", jsonObject.getString("send_sms_datas"));
                            i.putExtra("sms_body", shareBody);
                            i.setType("vnd.android-dir/mms-sms");
                            //startActivityForResult(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_using)),CHOOSE_SHARE);
                            baseActivity.startActivityForResult(i, baseActivity.CHOOSE_SHARE);
                            baseActivity.onbackhome = true;
                        } else {
                            baseActivity.startTomain();
                        }
                    } else {
                        Toast.makeText(baseActivity, baseActivity.getString(R.string.updating_party), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(baseActivity, baseActivity.getString(R.string.couldnt_party_create), Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}