package com.easyparty.invitation.event.Respond;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import androidx.annotation.NonNull;
import com.easyparty.invitation.R;
import com.easyparty.invitation.templates.BaseActivity;
import com.easyparty.invitation.ui.eventsManage.seeResponsesNew;
import com.easyparty.invitation.utils.network.Endpoints;
import com.easyparty.invitation.utils.network.requestMaker;
import com.easyparty.invitation.utils.push.messaging;
import com.pixplicity.easyprefs.library.Prefs;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

public class RespondsScreen extends BaseActivity {
    String order;
    ListView listView;
    ArrayList<RespondItem> RespondItemArrayList=new ArrayList<RespondItem>();
    Boolean done=false;
    getInvitesTask task;
    int offset=0;
    adapter adapter1;
    public ArrayList<String> notifs;
    public ArrayList<String> notifsGifts;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_responds_screen);
        notifs= messaging.getBadges(messaging.invitesArray);
        notifsGifts= messaging.getBadges(messaging.invitesGiftsArray);
        listView=findViewById(R.id.listview);
        baseCreate(this);
        populate();
        setTitle(getString(R.string.invitation_screen_title));
    }


    @Override
    protected void onResume() {
        super.onResume();
        notifs= messaging.getBadges(messaging.invitesArray);
        notifsGifts= messaging.getBadges(messaging.invitesGiftsArray);
        populate();
    }

    void populate() {
        offset=0;
        listView.setAdapter(null);
        RespondItemArrayList.clear();
        this.adapter1=(new adapter(this,RespondItemArrayList));
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
        task=new getInvitesTask(this, Endpoints.GET_INVITES,new HashMap<String, Object>(),offset);
        task.execute();
    }

    void clicker(RespondItem item) {
        messaging.updateBadges(item.id,messaging.invitesArray,false);
        RespondsScreen baseActivity = RespondsScreen.this;
        baseActivity.startActivity(new Intent(baseActivity,showInvitation.class).putExtra("event_id",item.id));
    }

    void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                JSONArray array = jsonObject.getJSONArray("invites");
                if (array.length()!=10) {
                    done=true;
                }
                offset+=array.length();
                Log.d(TAG, "callback: "+ Arrays.toString(RespondItemArrayList.toArray()));
                RespondItem.fromJSONArray(array,RespondItemArrayList,this);
                adapter1.notifyDataSetChanged();
            } else {
                Toast.makeText(this, getString(R.string.error_loading_events), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, getString(R.string.error_loading_events), Toast.LENGTH_SHORT).show();
        }
    }
}

class RespondItem {
    private static String convertMongoDate(String val){
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat outputFormat= new SimpleDateFormat("yyyy/MM/dd");
        try {
            String finalStr = outputFormat.format(inputFormat.parse(val));
            System.out.println(finalStr);
            return finalStr;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }
    static void fromJSONArray(JSONArray array,ArrayList<RespondItem> list,RespondsScreen respondsScreen) {
        int i;
        for (i=0;i<array.length();i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                String date=convertMongoDate(jsonObject.getString("date"));
                RespondItem temp = new RespondItem(
                        jsonObject.getString("_id"), jsonObject.getString("childName"), date,jsonObject.getBoolean("guestSee")
                );
                if (jsonObject.has("showGiftOption")) {
                    temp.showGift=true;
                    temp.responseId=jsonObject.getString("response_id");
                }
                for (int j = 0; j < respondsScreen.notifs.size(); j++) {
                    if (temp.id.equals(respondsScreen.notifs.get(j))) {
                        temp.showBadge=true;
                    }
                }
                for (int j = 0; j < respondsScreen.notifsGifts.size(); j++) {
                    if (temp.id.equals(respondsScreen.notifsGifts.get(j))) {
                        temp.showGiftBadge=true;
                    }
                }
                list.add(temp);
            } catch (Exception e){e.printStackTrace();}
        }
    }
    public String id;
    public String name;
    public String date;
    public Boolean isGuestAllowed;
    public Boolean showBadge=false;
    public Boolean showGift = false;
    public String responseId="";
    public Boolean showGiftBadge = false;
    public RespondItem(String a, String b, String c,Boolean d) {
        this.id=a;
        this.name=b;
        this.date=c;
        this.isGuestAllowed=d;
    }
}


class adapter extends ArrayAdapter<RespondItem> {
    RespondsScreen context;
    ArrayList<RespondItem> list;
    public adapter(@NonNull RespondsScreen context, ArrayList<RespondItem> list)  {
        super(context,0,list);
        this.context=context;
        this.list=list;
    }

    @Override
    public int getCount() {
        return list.size();
    }


    @Override
    public RespondItem getItem(int position) {
        return list.get(position);
    }

    @NonNull
    @Override
    public View getView(int position, @androidx.annotation.Nullable View convertView, @NonNull ViewGroup parent) {
        convertView = LayoutInflater.from(context).inflate(R.layout.new_event_list_row,parent,false);
        com.nex3z.notificationbadge.NotificationBadge badge = convertView.findViewById(R.id.badge1);
        final RespondItem item=list.get(position);
        if (item.showBadge) {
            badge.setVisibility(View.VISIBLE);
            badge.setMaxTextLength(99);
            //badge.setText(context.getString(R.string.new1));
            badge.setText("!");
        }
        String language=Prefs.getString("language","fr");
        if (language.equals("fr")) {
            ((TextView)convertView.findViewById(R.id.name)).setText("Anniversaire de "+item.name);
        } else {
            ((TextView)convertView.findViewById(R.id.name)).setText(item.name+"'s BDay");
        }

        ((TextView)convertView.findViewById(R.id.date_event)).setText(item.date);
        if (item.isGuestAllowed) {
            convertView.findViewById(R.id.linear).setVisibility(View.VISIBLE);
            convertView.findViewById(R.id.responses_guest).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    context.startActivity(new Intent(context, seeResponsesNew.class).putExtra("eventId",item.id).putExtra("guest",true));
                }
            });
        }
        else {
            convertView.findViewById(R.id.linear).setVisibility(View.GONE);
        }
        final RespondItem send = item;
        convertView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                context.clicker(send);
            }
        });
        if (item.showGift) {
            convertView.findViewById(R.id.linearGift).setVisibility(View.VISIBLE);
            if (item.showGiftBadge) {
                com.nex3z.notificationbadge.NotificationBadge badgeGift = convertView.findViewById(R.id.badgeGift);
                badgeGift.setVisibility(View.VISIBLE);
                badgeGift.setMaxTextLength(99);
                badgeGift.setText("!");
            }
            convertView.findViewById(R.id.giftChoose).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    messaging.updateBadges(item.id,messaging.invitesGiftsArray,false);
                    context.startActivity(new Intent(context,GiftSelect.class).putExtra("eventId",send.id).putExtra("responseId",send.responseId));
                }
            });
        }
        return convertView;
    }
}


class getInvitesTask extends AsyncTask<Object,Void, JSONObject> {
    RespondsScreen baseActivity;
    String url;
    HashMap<String,Object> map;
    int offset;
    public getInvitesTask(RespondsScreen baseActivity,String url,HashMap<String,Object> map,int offset) {
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
        baseActivity.dialog.setCanceledOnTouchOutside(false);
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
        try {baseActivity.callback(jsonObject);baseActivity.dialog.cancel();} catch (Exception e) {e.printStackTrace();}
    }
}
