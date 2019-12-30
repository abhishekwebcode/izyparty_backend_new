package com.easyparty.invitation.event.Respond;


import android.app.ProgressDialog;
import android.app.TaskStackBuilder;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.easyparty.invitation.R;
import com.easyparty.invitation.templates.BaseActivity;
import com.easyparty.invitation.ui.home.HomeActivity;
import com.easyparty.invitation.utils.network.Endpoints;
import com.easyparty.invitation.utils.network.requestMaker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static io.fabric.sdk.android.Fabric.TAG;

public class GiftSelect extends BaseActivity {
    String response_id;
    public String id;
    public String todo_new;
    ListView listView;
    ArrayList<GIFT1> eventItemArrayList = new ArrayList<GIFT1>();
    Boolean done = false;
    getGifts1 task;
    int offset = 0;
    CustomListGift1 adapter1;
    Boolean fromNotif=false;
    TextView gift_initial_text;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gift_select);
        baseCreate(this);
        setTitle(getString(R.string.gifts_title));
        fromNotif=getIntent().hasExtra("from");
        id = getIntent().getStringExtra("eventId");
        listView = findViewById(R.id.listview);
        baseCreate(this);
        gift_initial_text=findViewById(R.id.gift_initial_text);
        if (getIntent().hasExtra("showLoading")) {
            continueLoading();
            return;
        }
        HashMap<String,Object> map=new HashMap<>();
        map.put("eventId",id);
        if (getIntent().hasExtra("responseId")) {
            response_id=getIntent().getStringExtra("responseId");
        }
        new getGiftStatus(this,Endpoints.CHECK_GIFT,map).execute();
    }

    public void giftCallback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                if (jsonObject.has("NEVER_SELECTED")) {
                    continueLoading();
                    return;
                }
                if (jsonObject.has("NO_GIFT")) {
                    gift_initial_text.setText(getString(R.string.no_gift_selected));
                } else {
                    gift_initial_text.setText(getString(R.string.gift_selected_title)+" "+jsonObject.getString("GIFT")+" . "+getString(R.string.change_question_gift));
                }
                findViewById(R.id.accept).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        continueLoading();
                    }
                });
                findViewById(R.id.reject).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startActivity(new Intent(GiftSelect.this, RespondsScreen.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK));
                        finish();
                    }
                });

            } else {
                Toast.makeText(this, getString(R.string.error_retrieiving_gufts), Toast.LENGTH_SHORT).show();
            }
        } catch (Throwable e) {e.printStackTrace();}
    }

    void continueLoading() {
        findViewById(R.id.mainLayoutGifts).setVisibility(View.GONE);
        if (fromNotif) {
            HashMap<String,Object> map = new HashMap<>();
            map.put("eventId",id);
            new getResponseId(this,Endpoints.GET_RESPONSE_ID,map).execute();
        } else {
            response_id = getIntent().getStringExtra("responseId");
            populate();
        }
    }


    void populate() {
        offset=0;
        eventItemArrayList.clear();
        this.adapter1 = new CustomListGift1(this, eventItemArrayList);
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
        map.put("eventId",id);
        task = new getGifts1(this, Endpoints.GET_GIFTS_INVITEE, map , offset);
        task.execute();
    }

    void selectGift(String giftID,Boolean nullGift) {
        if (nullGift) {
            HashMap<String,Object> map = new HashMap<>();
            map.put("todo",giftID);
            map.put("unselect","true");
            map.put("eventId",id);
            map.put("responseId",response_id);
            new markAsSelectedGift(this,Endpoints.MARK_GIFT,map).execute();
            return;
        }
        HashMap<String,Object> map = new HashMap<>();
        map.put("todo",giftID);
        map.put("unselect","false");
        map.put("eventId",id);
        map.put("responseId",response_id);
        new markAsSelectedGift(this,Endpoints.MARK_GIFT,map).execute();
    }

    void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                Boolean emptySelected = jsonObject.isNull("giftSelected");
                Log.d(TAG, "callback: SELETCED GIFT"+emptySelected.toString());
                if (jsonObject.has("giftSelected")) {
                    if (!jsonObject.isNull("giftSelected") ) {
                        JSONObject selected = jsonObject.getJSONObject("giftSelected");
                        if (selected != null) {
                            GIFT1 temp = new GIFT1(
                                    selected.getString("_id"), id, selected.getString("gift"), selected.getBoolean("selected")
                            );
                            temp.isSelectedOne = true;
                            eventItemArrayList.add(temp);
                        }
                    }
                }
                JSONArray array = jsonObject.getJSONArray("gifts");
                if (array.length() != 10) {
                    done = true;
                }
                if (offset==0) {
                    GIFT1 temp = new GIFT1("",id,getString(R.string.NO_GIFT_TITLE),emptySelected);
                    temp.nullGift=true;
                    if (emptySelected) {temp.isSelectedOne=true;}
                    eventItemArrayList.add(temp);
                }
                offset += array.length();
                GIFT1.fromJSONArray(array, eventItemArrayList, id);
                adapter1.notifyDataSetChanged();
            } else {
                //Toast.makeText(this,getString(R.string.couldnt_update_choce_gift), Toast.LENGTH_SHORT).show();
                Toast.makeText(this, getString(R.string.Error_loading_gifts), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.Error_loading_gifts), Toast.LENGTH_SHORT).show();
            //Toast.makeText(this,getString(R.string.couldnt_update_choce_gift), Toast.LENGTH_SHORT).show();
        }
    }

}


class GIFT1 {
    public String eventId;
    public String id;
    public String gift;
    public Boolean nullGift=false;
    public Boolean checked = false;
    public Boolean isSelectedOne = false;

    static void fromJSONArray(JSONArray array, ArrayList<GIFT1> list, String eventId) {
        int i;
        for (i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                String date = new SimpleDateFormat("d MMM, yyyy").format((new Date((jsonObject.getLong("date_created")))));
                list.add(
                        new GIFT1(
                                jsonObject.getString("_id"), eventId, jsonObject.getString("gift"), jsonObject.getBoolean("selected")
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public GIFT1(String id, String eventId, String gift, Boolean checked) {
        this.id = id;
        this.eventId = eventId;
        this.gift = gift;
        this.checked = checked;
    }

}

class CustomListGift1 extends ArrayAdapter<GIFT1> {
    GiftSelect context;
    ArrayList<GIFT1> list;

    public CustomListGift1(GiftSelect context, ArrayList<GIFT1> list) {
        super(context, R.layout.row_select_gift, list);
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
        GIFT1 item = list.get(position);
        View rowView = inflater.inflate(R.layout.row_select_gift, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.text);
        final Button txtTitle1 = (Button) rowView.findViewById(R.id.checkbox);
        if (item.isSelectedOne) {
            txtTitle1.setText(context.getString(R.string.selected));
        }
        txtTitle.setText(item.gift);
        final String id1 = item.eventId;
        final String id2 = item.id;
        final Boolean sendNULL = item.nullGift;
        txtTitle1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.selectGift(id2,sendNULL);
            }
        });
        return rowView;
    }
}


class getGifts1 extends AsyncTask<Object, Void, JSONObject> {
    GiftSelect baseActivity;
    String url;
    HashMap<String, Object> map;
    int offset;

    public getGifts1(GiftSelect baseActivity, String url, HashMap<String, Object> map, int offset) {
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
        baseActivity.dialog.cancel();
        try {
            baseActivity.callback(jsonObject);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class addGiftTask extends AsyncTask<Object, Void, JSONObject> {
    GiftSelect baseActivity;
    String url;
    HashMap<String, Object> map;
    int offset;

    public addGiftTask(GiftSelect baseActivity, String url, HashMap<String, Object> map) {
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
        baseActivity.dialog.cancel();
        try {
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(baseActivity, baseActivity.getString(R.string.successfully_added_gift), Toast.LENGTH_SHORT).show();
                baseActivity.populate();
            } else{throw new Exception("error in server");}
        } catch (Exception e) {
            Toast.makeText(baseActivity, baseActivity.getString(R.string.couldnt_update_choce_gift), Toast.LENGTH_SHORT).show();
            baseActivity.finish();
            e.printStackTrace();
        }
    }
}



class markAsSelectedGift extends AsyncTask<Object, Void, JSONObject> {
    GiftSelect baseActivity;
    String url;
    HashMap<String, Object> map;

    public markAsSelectedGift(GiftSelect baseActivity, String url, HashMap<String, Object> map) {
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
        baseActivity.dialog.cancel();
        try {
            if (jsonObject.has("CODE")) {
                if (jsonObject.getString("CODE").equals("ALREADY_SELECTED")) {
                    Toast.makeText(baseActivity, baseActivity.getString(R.string.already_selected), Toast.LENGTH_SHORT).show();
                    baseActivity.finish();
                    return;
                }
            }
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(baseActivity, baseActivity.getString(R.string.successfully_updated), Toast.LENGTH_SHORT).show();
                //intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                //baseActivity.startActivity(intent);
                TaskStackBuilder
                        .create(baseActivity)
                        .addNextIntentWithParentStack(new Intent(baseActivity, HomeActivity.class))
                        .addNextIntent(new Intent(baseActivity, RespondsScreen.class))
                        .startActivities();
                baseActivity.finish();
            } else{
                throw new Exception("error in server");
            }
        } catch (Exception e) {
            Toast.makeText(baseActivity, baseActivity.getString(R.string.couldnt_update_choce_gift), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}




class getResponseId extends AsyncTask<Object, Void, JSONObject> {
    GiftSelect baseActivity;
    String url;
    HashMap<String, Object> map;

    public getResponseId(GiftSelect baseActivity, String url, HashMap<String, Object> map) {
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
        baseActivity.dialog.cancel();
        try {
            if (jsonObject.getBoolean("success")) {
                baseActivity.response_id = jsonObject.getString("responseId");
                baseActivity.populate();
            } else{
                throw new Exception("error in server");
            }
        } catch (Exception e) {
            Toast.makeText(baseActivity, baseActivity.getString(R.string.couldnt_update_choce_gift), Toast.LENGTH_SHORT).show();
            baseActivity.finish();
            e.printStackTrace();
        }
    }
}

class getGiftStatus extends AsyncTask<Object, Void, JSONObject> {
    GiftSelect baseActivity;
    String url;
    HashMap<String, Object> map;

    public getGiftStatus(GiftSelect baseActivity, String url, HashMap<String, Object> map) {
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
        baseActivity.dialog.cancel();
        try {
            baseActivity.giftCallback(jsonObject);
        } catch (Throwable e) {e.printStackTrace();}
    }
}
