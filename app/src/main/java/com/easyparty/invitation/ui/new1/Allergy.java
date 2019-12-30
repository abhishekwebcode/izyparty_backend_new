package com.easyparty.invitation.ui.new1;


import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.easyparty.invitation.R;
import com.easyparty.invitation.event.Respond.GiftSelect;
import com.easyparty.invitation.event.Respond.RespondsScreen;
import com.easyparty.invitation.templates.BaseActivity;
import com.easyparty.invitation.utils.network.Endpoints;
import com.easyparty.invitation.utils.network.requestMaker;
import com.jaredrummler.materialspinner.MaterialSpinner;
import com.suke.widget.SwitchButton;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class Allergy extends BaseActivity {
String owner;
String eventId;
MaterialSpinner allerygy1;
MaterialSpinner allerygy2;
EditText allerygy3;
EditText childName;
    com.suke.widget.SwitchButton isAllergy;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.allergy_screen_new);
        owner=getIntent().getStringExtra("owner");
        ((TextView)findViewById(R.id.welcome) ).setText(owner+" "+getString(R.string.is_delighted_toInvite));
        allerygy1=((MaterialSpinner)findViewById(R.id.spinner));
        allerygy2=((MaterialSpinner)findViewById(R.id.spinner1));
        allerygy3=((EditText)findViewById(R.id.spinner2));
        allerygy3=((EditText)findViewById(R.id.childName));
        allerygy1.setItems(getResources().getStringArray(R.array.foods));
        allerygy2.setItems(getResources().getStringArray(R.array.pets));
        eventId=getIntent().getStringExtra("eventId");
        isAllergy=(com.suke.widget.SwitchButton)findViewById(R.id.switch_button);
        isAllergy.setOnCheckedChangeListener(new SwitchButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(SwitchButton view, boolean isChecked) {
                onCheckk(view);
            }
        });
        isAllergy.setChecked(false);
        setVisisble(false);
        allerygy1.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                    Allergy.this.select(position==7);
            }
        });
        allerygy2.setOnItemSelectedListener(new MaterialSpinner.OnItemSelectedListener() {
            @Override
            public void onItemSelected(MaterialSpinner view, int position, long id, Object item) {
                Allergy.this.select(position==4);
            }
        });
        baseCreate(this);
        findViewById(R.id.send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
        select(false);
        setTitle(getString(R.string.choose_allergies));
    }

    public void select(Boolean isOther) {
        Boolean show = allerygy1.getSelectedIndex()==7||allerygy2.getSelectedIndex()==5;
        allerygy3.setVisibility(show?View.VISIBLE:View.GONE);
    }

    public void submit() {
        if ("".equals(childName.getText().toString())) {
            Toast.makeText(this, getString(R.string.allergy_childName_error), Toast.LENGTH_SHORT).show();
            return;
        }
        HashMap<String,Object> map = new HashMap<>();
        map.put("eventId",eventId);
        map.put("childName",childName.getText().toString());
        map.put("isAllergy",String.valueOf(isAllergy.isChecked()));
        map.put("allergy1",allerygy1.getItems().get(allerygy1.getSelectedIndex()).toString());
        map.put("allergy2",allerygy2.getItems().get(allerygy2.getSelectedIndex()).toString());
        map.put("allergy3",allerygy3.getText().toString());
        new acceptTask(this, Endpoints.ACCEPT_INVITE,map).execute();
    }

    public void callback(final JSONObject jsonObject) {


        try {
            final String name = jsonObject.getString("eventName");
            final Allergy baseActivity = this;
            DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case DialogInterface.BUTTON_POSITIVE:
                            Intent intent = new Intent(Intent.ACTION_EDIT);
                            intent.setType("vnd.android.cursor.item/event");
                            intent.putExtra(CalendarContract.Events.TITLE, (name==null?"":name)+(baseActivity.getString(R.string.bday_title)) );
                            Allergy.this.startActivity(intent);
                            break;
                        case DialogInterface.BUTTON_NEGATIVE:
                            break;
                    }
                }
            };

            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(baseActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(baseActivity);
            }
            AlertDialog d = builder
                    .setMessage(baseActivity.getString(R.string.add_todo_using_calendar))
                    .setPositiveButton(baseActivity.getString(R.string.yes), dialogClickListener)
                    .setNegativeButton(baseActivity.getString(R.string.no), dialogClickListener)
                    .show();
            d.setCanceledOnTouchOutside(false);
            d.setCancelable(false);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this,this.getString(R.string.no_calendar_app), Toast.LENGTH_SHORT).show();
        }

        try {
            if (jsonObject.getBoolean("success")) {
                Log.d(TAG, "GIFT CHECK: " + jsonObject.toString());
                if (jsonObject.has("chooseGifts")) {
                    Toast.makeText(this, getString(R.string.response_sent), Toast.LENGTH_SHORT).show();
                    Intent intent =
                            new Intent(this, GiftSelect.class)
                            .putExtra("responseId",jsonObject.getString("response_id"))
                            .putExtra("eventId",eventId)
                            .putExtra("from","initial");
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
                else {
                    Toast.makeText(this, getString(R.string.response_sent), Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(this, RespondsScreen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    finish();
                }
            } else  {
                throw new Exception("JSON_ERROR");
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_accpeting_invite), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    public void setVisisble(Boolean b) {
        int vis = b?View.VISIBLE:View.GONE;
        allerygy1.setVisibility(vis);
        allerygy2.setVisibility(vis);
        select(false);
    }

    public void onCheckk(View v) {
        if (((com.suke.widget.SwitchButton)v).isChecked()) {
           setVisisble(true);
        }
        else {
            setVisisble(false);
        }
    }

}


class acceptTask extends AsyncTask<Object, Void, JSONObject> {
    Allergy baseActivity;
    String url;
    HashMap<String, Object> map;

    public acceptTask(Allergy baseActivity, String url, HashMap<String, Object> map) {
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
            baseActivity.callback(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error1), Toast.LENGTH_SHORT).show();
        }
    }
}