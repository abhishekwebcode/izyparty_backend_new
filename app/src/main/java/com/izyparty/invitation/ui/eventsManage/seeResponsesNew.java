package com.izyparty.invitation.ui.eventsManage;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;
import com.izyparty.invitation.utils.network.Endpoints;
import com.izyparty.invitation.utils.network.requestMaker;
import com.pixplicity.easyprefs.library.Prefs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class seeResponsesNew extends BaseActivity {
    public String eventId;
    public String todo_new;
    ListView listView;
    ArrayList<RESPONSE> ResponsesArray = new ArrayList<RESPONSE>();
    Boolean done = false;
    getResponses task;
    int offset = 0;
    CustomListResponse adapter1;
    public Boolean isGuest = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_activity_see_responses);
        isGuest=getIntent().getBooleanExtra("guest",true);
        eventId = getIntent().getStringExtra("eventId");
        listView = findViewById(R.id.listview);
        baseCreate(this);
        setTitle(getString(R.string.see_responses));
        populate();
        if (getIntent().hasExtra("from")) {
            if (getIntent().getStringExtra("from").equals("notif")) {
                int existing= Prefs.getInt("event",0);
                if (existing!=0) {Prefs.putInt("event",existing-1);}
            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        populate();
    }

    void populate() {
        offset=0;
        ResponsesArray.clear();
        this.adapter1 = new CustomListResponse(this, ResponsesArray);
        listView.setAdapter(adapter1);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {

            }

            @Override
            public void onScroll(AbsListView absListView, int firstItem, int visibleItemCount, final int totalItems) {
                final int lastItem = firstItem + visibleItemCount;
                if (lastItem == totalItems) {
                    if (!done) {
                        loadMore();
                    }
                }
            }
        });
        loadMore();
    }

    void loadMore() {
        if (task != null && !task.isCancelled()) {
            task.cancel(true);
            task = null;
        }
        HashMap map = new HashMap<String, Object>();
        map.put("eventId",eventId);
        map.put("isGuest",String.valueOf(isGuest));
        task = new getResponses(this, Endpoints.GET_RESPONSES, map , offset);
        task.execute();
    }

    void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                JSONArray array = jsonObject.getJSONArray("responses");
                if (array.length() != 10) {
                    done = true;
                }
                offset += array.length();
                RESPONSE.fromJSONArray(array, ResponsesArray,isGuest);
                adapter1.notifyDataSetChanged();
            } else {
                Toast.makeText(this, getString(R.string.error_loading_rsvp), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_loading_rsvp), Toast.LENGTH_SHORT).show();
        }
    }
}

class RESPONSE {
    public String id;
    public String name;
    public Boolean intention;
    public Boolean isAllergy = false;
    public String allergy1;
    public String allergy2;
    public String allergy3;
    public String date;
    public String childName="";
    public Boolean isGift = false;
    public String gift= "";

    public RESPONSE(String a,String b,Boolean c,String h) {
        this.id=a;
        this.name=b;
        this.intention=c;
        /*
        this.isAllergy=d;
        this.allergy1=e;
        this.allergy2=f;
        this.allergy3=g;
         */
        this.date=h;
    }

    static void fromJSONArray(JSONArray array, ArrayList<RESPONSE> list,Boolean isGuest) {
        int i;
        for (i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                String date = new SimpleDateFormat("d MMM, yyyy").format((new Date((jsonObject.getLong("date_created")))));
                RESPONSE item = new RESPONSE(
                        jsonObject.getString("_id"), jsonObject.getString("name"), jsonObject.getBoolean("intention"), date
                );
                if (!isGuest) {
                    if (jsonObject.has("isAllergy")) {
                        if (jsonObject.getBoolean("isAllergy")) {
                            item.isAllergy = true;
                            try {
                                item.childName=jsonObject.getString("childNameAllergy");
                            }catch (Exception e){e.printStackTrace();}
                            item.allergy1 = jsonObject.getString("allergy1");
                            item.allergy2 = jsonObject.getString("allergy2");
                            item.allergy3 = jsonObject.getString("allergy3");
                        }
                    }
                    if (jsonObject.has("isGift")) {
                        if (jsonObject.getBoolean("isGift")) {
                            item.isGift=true;
                            item.gift=jsonObject.getString("gift");
                        }
                    }
                }
                else {
                    item.isAllergy=false;
                }
                list.add(item);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}



class CustomListResponse extends ArrayAdapter<RESPONSE> {
    seeResponsesNew context;
    ArrayList<RESPONSE> list;

    public CustomListResponse(seeResponsesNew context, ArrayList<RESPONSE> list) {
        super(context, R.layout.new_row_todo, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        RESPONSE item = list.get(position);
        View rowView = inflater.inflate(R.layout.row_event_responses_list, null, true);
        ((TextView)rowView.findViewById(R.id.name)).setText(item.name);
        ((TextView)rowView.findViewById(R.id.date)).setText(item.date);
        ((TextView)rowView.findViewById(R.id.response)).setText(item.intention?context.getString(R.string.present):context.getString(R.string.Absent));
        int imageID = item.intention?R.drawable.ic_yes:R.drawable.ic_no;
        ((ImageView)rowView.findViewById(R.id.intent)).setImageDrawable(context.getResources().getDrawable(imageID));
        int V = View.VISIBLE;
        if (context.isGuest) return rowView;
        if (item.isAllergy) {
            rowView.findViewById(R.id.divider_1).setVisibility(V);
            rowView.findViewById(R.id.divider_2).setVisibility(V);
            rowView.findViewById(R.id.allergies).setVisibility(V);
            rowView.findViewById(R.id.childNameTitle).setVisibility(V);
            rowView.findViewById(R.id.childNameAllergy).setVisibility(V);
            ((TextView)rowView.findViewById(R.id.childNameAllergy)).setText(item.childName);
            Log.d(TAG, "getView: DEBUG ALLERGY + "+ String.valueOf(item.allergy2));
            Log.d(TAG, "getView: DEBUG ALLERGY + "+ String.valueOf(item.allergy2.equals("Autres")));
            if ("Others".equals(item.allergy1) || "Autres".equals(item.allergy1)) {
                rowView.findViewById(R.id.allergies3).setVisibility(V);
                ((TextView)rowView.findViewById(R.id.allergies3)).setText(item.allergy3);
            } else {
                rowView.findViewById(R.id.allergies1).setVisibility(V);
                ((TextView)rowView.findViewById(R.id.allergies1)).setText(item.allergy1);
            }
            if ("Others".equals(item.allergy2) || "Autres".equals(item.allergy2)) {
                rowView.findViewById(R.id.allergies3).setVisibility(V);
                ((TextView)rowView.findViewById(R.id.allergies3)).setText(item.allergy3);
            } else {
                rowView.findViewById(R.id.allergies2).setVisibility(V);
                ((TextView)rowView.findViewById(R.id.allergies2)).setText(item.allergy2);
            }
        }
        else {
            rowView.findViewById(R.id.divider_1).setVisibility(V);
            ((TextView)rowView.findViewById(R.id.allergies)).setText(context.getString(R.string.no_allergies));
            rowView.findViewById(R.id.allergies).setVisibility(V);
        }
        if (item.isGift) {
            rowView.findViewById(R.id.divider_10).setVisibility(V);
            rowView.findViewById(R.id.divider_11).setVisibility(V);
            rowView.findViewById(R.id.gift_title).setVisibility(V);
            rowView.findViewById(R.id.gift).setVisibility(V);
            ((TextView)rowView.findViewById(R.id.gift)).setText(item.gift);
        }
        return rowView;
    }
}


class getResponses extends AsyncTask<Object, Void, JSONObject> {
    seeResponsesNew baseActivity;
    String url;
    HashMap<String, Object> map;
    int offset;

    public getResponses(seeResponsesNew baseActivity, String url, HashMap<String, Object> map, int offset) {
        super();
        Log.d(TAG, "addEvenetTask: CREATD TASK");
        this.baseActivity = baseActivity;
        this.url = url;
        this.map = map;
        this.offset = offset;
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
            map.put("offset", String.valueOf(offset));
            return requestMaker.makeRequests(url,map,baseActivity);
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
            baseActivity.dialog.cancel();
            baseActivity.callback(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
