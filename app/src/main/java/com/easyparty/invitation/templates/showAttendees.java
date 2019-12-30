package com.easyparty.invitation.templates;

import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.easyparty.invitation.R;
import com.easyparty.invitation.utils.network.Endpoints;
import com.easyparty.invitation.utils.network.requestMaker;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

import static android.content.ContentValues.TAG;
import static android.view.View.VISIBLE;

public class showAttendees extends BaseActivity {

    public String id;
    ListView listView;
    ArrayList<Item> eventItemArrayList = new ArrayList<>();
    Boolean done = false;
    getAttendees task;
    int offset = 0;
    CustomListAttendees adapter1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_attendees);
        baseCreate(this);
        setTitle(getString(R.string.participants));
        id = getIntent().getStringExtra("id");
        listView = findViewById(R.id.listview);
        populate();
    }

    static class Item {
        public Boolean changeToUsers = false;
        public Boolean changeToPhone = false;
        public Boolean isPhone = false;
        public String name = "";
        public String phone = "";
        public Item(Boolean a , String b,String c) {
            this.isPhone=a;
            this.name=b;
            this.phone=c;
        }
        static ArrayList<Item> fromJSON(JSONObject jsonObject) throws Exception {
            ArrayList<Item> list=new ArrayList<>();
            JSONObject users =jsonObject.getJSONObject("users");
            JSONObject numbers =jsonObject.getJSONObject("numbers");
            JSONObject jobject = users;
            try {
                for(int i = 0; i<jobject.names().length(); i++){
                    String key=jobject.names().getString(i);
                    String value=jobject.get(jobject.names().getString(i)).toString();
                    Item temp = new Item(false, key, value);
                    temp.changeToUsers=i==0;
                    list.add(temp);
                }
            } catch(Throwable e) {e.printStackTrace();}
            try {
                for (int i = 0; i < numbers.names().length(); i++) {
                    String key = numbers.names().getString(i);
                    String value = numbers.get(numbers.names().getString(i)).toString();
                    Item temp = new Item(false, key, value);
                    temp.changeToPhone = i == 0;
                    list.add(temp);
                }
            } catch(Throwable e) {e.printStackTrace();}
            return list;
        }
    }


    void populate() {
        offset=0;
        eventItemArrayList.clear();
        this.adapter1 = new CustomListAttendees(this, eventItemArrayList);
        listView.setAdapter(adapter1);
        listView.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView view, int scrollState) {
            }

            @Override
            public void onScroll(AbsListView absListView, int firstItem, int visibleItemCount, final int totalItems) {
                if (true) return;
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
        task = new getAttendees(this, Endpoints.GET_ATTENDEES, map , offset);
        task.execute();
    }

    void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                //JSONArray array = jsonObject.getJSONArray("gifts");
                adapter1.list=Item.fromJSON(jsonObject.getJSONObject("data"));
                eventItemArrayList=adapter1.list;
                adapter1.notifyDataSetChanged();
            } else {
                Toast.makeText(this, getString(R.string.error_loading_attendees), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_loading_attendees), Toast.LENGTH_SHORT).show();
        }
    }


}




class CustomListAttendees extends ArrayAdapter<showAttendees.Item> {
    showAttendees context;
    ArrayList<showAttendees.Item> list;

    public CustomListAttendees(showAttendees context, ArrayList<showAttendees.Item> list) {
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
        showAttendees.Item item = list.get(position);
        View rowView = inflater.inflate(R.layout.row_event_attendee_registered, null, true);
        if (item.changeToPhone||item.changeToUsers) {
            LinearLayout tailer = rowView.findViewById(R.id.outer);
            tailer.setVisibility(VISIBLE);
            TextView tailText = rowView.findViewById(R.id.tail_text);
            if (item.changeToUsers) {
                tailText.setText(context.getString(R.string.registeredUsers));
            }
            else {
                tailText.setText(context.getString(R.string.nonAppPhoneNumbers));
            }
        }
        TextView name = rowView.findViewById(R.id.name);
        TextView content = rowView.findViewById(R.id.content);
        if (item.isPhone) {
            name.setText(item.name);
            content.setText("");
        }
        else {
            name.setText(item.name);
            content.setText(item.phone);
        }

        return rowView;
    }
}


class getAttendees extends AsyncTask<Object, Void, JSONObject> {
    showAttendees baseActivity;
    String url;
    HashMap<String, Object> map;
    int offset;

    public getAttendees(showAttendees baseActivity, String url, HashMap<String, Object> map, int offset) {
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
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error_loading_attendees), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
