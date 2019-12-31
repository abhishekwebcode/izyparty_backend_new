package com.izyparty.invitation.ui.new1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;
import com.izyparty.invitation.utils.network.Endpoints;
import com.izyparty.invitation.utils.network.requestMaker;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import static android.content.ContentValues.TAG;

;

public class new_Gifts extends BaseActivity {
    public String id;
    public String todo_new;
    ListView listView;
    ArrayList<GIFT> eventItemArrayList = new ArrayList<GIFT>();
    Boolean done = false;
    getGifts task;
    int offset = 0;
    CustomListGift adapter1;
    addGiftTask todoADDTASK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_gifts);
        id = getIntent().getStringExtra("id");
        listView = findViewById(R.id.listview);
        baseCreate(this);
        populate();
        setTitle(getString(R.string.gifts_title));
    }

    public void addGifts(View v) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(getString(R.string.add_gift));
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        builder.setView(input);
        builder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                todo_new = input.getText().toString();
                if ("".equals(todo_new)) {
                    Toast.makeText(new_Gifts.this, getString(R.string.gift_add_error), Toast.LENGTH_SHORT).show();
                    return;
                }
                HashMap<String,Object> map=new HashMap<>();
                map.put("todo",todo_new);
                map.put("eventId",id);
                todoADDTASK=new addGiftTask(new_Gifts.this, Endpoints.ADD_GIFT,map);
                todoADDTASK.execute();
                dialog.cancel();
            }
        });
        builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show().setCanceledOnTouchOutside(false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        populate();
    }

    void populate() {
        offset=0;
        eventItemArrayList.clear();
        this.adapter1 = new CustomListGift(this, eventItemArrayList);
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
        task = new getGifts(this, Endpoints.GET_GIFTS, map , offset);
        task.execute();
    }

    void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                JSONArray array = jsonObject.getJSONArray("gifts");
                if (array.length() != 10) {
                    done = true;
                }
                offset += array.length();
                GIFT.fromJSONArray(array, eventItemArrayList, id);
                adapter1.notifyDataSetChanged();
            } else {
                Toast.makeText(this, getString(R.string.error_loading_events), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_loading_events), Toast.LENGTH_SHORT).show();
        }
    }
}

class GIFT {
    public String eventId;
    public String id;
    public String gift;
    public Boolean checked = false;

    static void fromJSONArray(JSONArray array, ArrayList<GIFT> list, String eventId) {
        int i;
        for (i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                String date = new SimpleDateFormat("d MMM, yyyy").format((new Date((jsonObject.getLong("date_created")))));
                list.add(
                        new GIFT(
                                jsonObject.getString("_id"), eventId, jsonObject.getString("gift"), jsonObject.getBoolean("selected")
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public GIFT(String id, String a, String b, Boolean c) {
        this.id = id;
        this.eventId = a;
        this.gift = b;
        this.checked = c;
    }

}

class CustomListGift extends ArrayAdapter<GIFT> {
    new_Gifts context;
    ArrayList<GIFT> list;

    public CustomListGift(new_Gifts context, ArrayList<GIFT> list) {
        super(context, R.layout.new_row_todo, list);
        this.context = context;
        this.list = list;
    }

    @Override
    public int getCount() {
        return list.size();
    }

    @Override
    public View getView(final int position, View view, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();
        GIFT item = list.get(position);
        View rowView = inflater.inflate(R.layout.new_row_todo, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.text);
        final ImageView txtTitle1 = (ImageView) rowView.findViewById(R.id.checkbox);
        int img = item.checked?R.drawable.ic_yes:R.drawable.ic_no;
        txtTitle1.setImageDrawable(context.getResources().getDrawable(img));
        txtTitle.setText(item.gift);
        final String id1 = item.eventId;
        final String id2 = item.id;
        View delete = rowView.findViewById(R.id.delete);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,Object> map = new HashMap<>();
                map.put("giftId",id2);
                new delete(context,Endpoints.DELETE_GIFT,map,position).execute();
            }
        });
        /*txtTitle1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String,Object> map = new HashMap<>();
                map.put("item",id1);
                map.put("itemID",id2);
                map.put("status", String.valueOf(txtTitle1.isChecked()));
                Log.d(TAG, "onClick: ss"+String.valueOf(txtTitle1.isChecked()));
                //TODO change here to allow changing of gift status by creator
                //new markAsDoneGift(context,Endpoints.UPDATE_GIFTS,map,txtTitle1).execute();
            }
        });*/
        return rowView;
    }
}


class getGifts extends AsyncTask<Object, Void, JSONObject> {
    new_Gifts baseActivity;
    String url;
    HashMap<String, Object> map;
    int offset;

    public getGifts(new_Gifts baseActivity, String url, HashMap<String, Object> map, int offset) {
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
    new_Gifts baseActivity;
    String url;
    HashMap<String, Object> map;
    int offset;

    public addGiftTask(new_Gifts baseActivity, String url, HashMap<String, Object> map) {
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
            e.printStackTrace();
        }
    }
}



class delete extends AsyncTask<Object, Void, JSONObject> {
    new_Gifts baseActivity;
    String url;
    HashMap<String, Object> map;
    CheckBox temp;
    int position;
    public delete(new_Gifts baseActivity, String url, HashMap<String, Object> map,int positoin) {
        super();
        Log.d(TAG, "addEvenetTask: CREATD TASK");
        this.baseActivity = baseActivity;
        this.url = url;
        this.map = map;
        position=positoin;

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
                Toast.makeText(baseActivity, baseActivity.getString(R.string.successfully_updated), Toast.LENGTH_SHORT).show();
                baseActivity.adapter1.list.remove(position);
                baseActivity.adapter1.notifyDataSetChanged();
                baseActivity.populate();
            } else{
                //temp.setChecked(temp.isChecked());
                throw new Exception("error in server");
            }
        } catch (Exception e) {
            Toast.makeText(baseActivity, baseActivity.getString(R.string.couldnt_update_it), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}




