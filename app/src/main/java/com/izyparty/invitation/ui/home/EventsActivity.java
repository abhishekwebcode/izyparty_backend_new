package com.izyparty.invitation.ui.home;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;
import com.izyparty.invitation.ui.eventsManage.EventOverview;
import com.izyparty.invitation.ui.new1.NewToDoList;
import com.izyparty.invitation.ui.new1.new_Gifts;
import com.izyparty.invitation.utils.network.Endpoints;
import com.izyparty.invitation.utils.network.requestMaker;
import com.izyparty.invitation.utils.push.messaging;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

import static android.content.ContentValues.TAG;


public class EventsActivity extends   BaseActivity {
    String order;
    ListView listView;
    ArrayList<EventItem> eventItemArrayList=new ArrayList<EventItem>();
    Boolean done=false;
    getEventsTask task;
    int offset=0;
    adapter adapter1;
    public ArrayList<String> notifs;
    String casefrom=null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_events);
        order=getIntent().getStringExtra("order");
        if (getIntent().hasExtra("arrayFrom")) {
            casefrom=getIntent().getStringExtra("arrayFrom");
            switch (getIntent().getStringExtra("arrayFrom")) {
                case "event":
                    notifs= messaging.getBadges(messaging.eventsArray);
                    break;
                case "gift":
                    notifs= messaging.getBadges(messaging.giftsArray);
                    break;
            }
        } else {
            notifs=new ArrayList<String>();
            Log.d(TAG, "onCreate: ARRAYFROM EMPTY");
        }
        Log.d(TAG, "onCreate: NOTIFS "+casefrom+" "+ Arrays.toString(notifs.toArray()));
        if (!(order.equals("manage_events"))) {
            findViewById(R.id.addEventFab).setVisibility(View.GONE);
        }
        else {
            findViewById(R.id.addEventFab).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(EventsActivity.this,AddEventPage1.class));
                }
            });
        }
        listView=findViewById(R.id.listview);
        baseCreate(this);
        setTitle(getString(R.string.mes_v_nements));
        populate();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (casefrom!=null) {
            switch (casefrom) {
                case "event":
                    notifs= messaging.getBadges(messaging.eventsArray);
                    break;
                case "gift":
                    notifs= messaging.getBadges(messaging.giftsArray);
                    break;
            }
        }
        else {
            notifs=new ArrayList<String>();
            Log.d(TAG, "onCreate: ARRAYFROM EMPTY");
        }
        populate();
    }

    void populate() {
        offset=0;
        listView.setAdapter(null);
        eventItemArrayList.clear();
        this.adapter1=(new adapter(this,eventItemArrayList));
        listView.setAdapter(adapter1);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {}
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
        if (task!=null && !task.isCancelled()) {task.cancel(true);task=null;}
        task=new getEventsTask(this, Endpoints.GET_EVENTS,new HashMap<String, Object>(),offset);
        task.execute();
    }

    void clicker(EventItem item,Boolean hightlight) {
        //Toast.makeText(this, item.id, Toast.LENGTH_SHORT).show();
        Log.d(TAG, "clicker: "+order);
        switch (order) {
            case "manage_events":
                messaging.updateBadges(item.id,messaging.eventsArray,false);
                startActivity(new Intent(this, EventOverview.class).putExtra("eventId",item.id).putExtra("highlight",hightlight));
                break;
            case "todo":
                startActivity(new Intent(this, NewToDoList.class).putExtra("id",item.id));
                break;
            case "gifts":
                messaging.updateBadges(item.id,messaging.giftsArray,false);
                startActivity(new Intent(this, new_Gifts.class).putExtra("id",item.id));
                break;
        }

    }

    void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                JSONArray array = jsonObject.getJSONArray("events");
                if (array.length()!=10) {
                    done=true;
                }
                if (array.length()==0&&offset==0) {
                    if (!(order.equals("manage_events"))) {
                        ((TextView)findViewById(R.id.no_events)).setText(getString(R.string.no_events_other));
                    }
                    findViewById(R.id.no_events).setVisibility(View.VISIBLE);
                }
                offset+=array.length();
                EventItem.fromJSONArray(array,eventItemArrayList,this);
                adapter1.notifyDataSetChanged();
            } else {
                Toast.makeText(this, getString(R.string.Error_loading_events), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.Error_loading_events), Toast.LENGTH_SHORT).show();
        }
    }

}


class EventItem {
    static void fromJSONArray(JSONArray array,ArrayList<EventItem> list,EventsActivity eventsActivity) {
        int i;
        for (i=0;i<array.length();i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                String date=new SimpleDateFormat("d MMM, yyyy").format((new Date((jsonObject.getLong("date")))));
                EventItem temp=new EventItem(
                        jsonObject.getString("id"), jsonObject.getString("name"), date
                );
                for (int j = 0; j < eventsActivity.notifs.size(); j++) {
                    if (temp.id.equals(eventsActivity.notifs.get(j))) {
                        temp.showBadge=true;
                        break;
                    }
                }
                list.add(temp);
            } catch (Exception e){e.printStackTrace();}
        }
    }
    public String id;
    public String name;
    public String date;
    public Boolean showBadge=false;
    public EventItem(String a, String b, String c) {
        this.id=a;
        this.name=b;
        this.date=c;
    }
}


class adapter extends ArrayAdapter<EventItem> {
    EventsActivity context;
    ArrayList<EventItem> list;
    public adapter(@NonNull EventsActivity context,ArrayList<EventItem> list)  {
        super(context,0,list);
        this.context=context;
        this.list=list;
    }

    @Override
    public int getCount() {
        return list.size();
    }


    @Override
    public EventItem getItem(int position) {
        return list.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @androidx.annotation.Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.new_event_list_row,parent,false);
        EventItem item=list.get(position);
        com.nex3z.notificationbadge.NotificationBadge badge = convertView.findViewById(R.id.badge1);
        Boolean highlight = false;
        if (context.order.equals("manage_events") || context.order.equals("gifts")) {
            if (item.showBadge) {
                highlight=true;
                badge.setVisibility(View.VISIBLE);
                badge.setMaxTextLength(99);
                //badge.setText(context.getString(R.string.new1));
                badge.setText("!");
            }
        }
        ((TextView)convertView.findViewById(R.id.name)).setText(item.name);
        ((TextView)convertView.findViewById(R.id.date_event)).setText(item.date);
        final EventItem send = item;
        final Boolean highLight = highlight;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.clicker(send,highLight);
            }
        });
        return convertView;
    }
}


class getEventsTask extends AsyncTask<Object,Void, JSONObject> {
    EventsActivity baseActivity;
    String url;
    HashMap<String,Object> map;
    int offset;
    public getEventsTask(EventsActivity baseActivity,String url,HashMap<String,Object> map,int offset) {
        super();
        Log.d(TAG, "addEvenetTask: CREATD TASK");
        this.baseActivity=baseActivity;
        this.url=url;
        this.map=map;
        this.offset=offset;
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
            map.put("offset",String.valueOf(offset));
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

