package com.izyparty.invitation.ui.new1;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
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

public class NewToDoList extends BaseActivity {
    public String id;
    public String todo_new;
    ListView listView;
    ArrayList<TODO> eventItemArrayList = new ArrayList<TODO>();
    Boolean done = false;
    getTodos task;
    int offset = 0;
    CustomList adapter1;
    addTodoTask todoADDTASK;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.new_todo_list);
        id = getIntent().getStringExtra("id");
        listView = findViewById(R.id.listview);
        baseCreate(this);
        populate();
        setTitle(getString(R.string.todo_title));
    }

    public void addTodo(View v) {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
        } else {
            builder = new AlertDialog.Builder(this);
        }
        builder.setTitle(getString(R.string.add_todo_titel));

// Set up the input
        final EditText input = new EditText(this);
// Specify the type of input expected; this, for example, sets the input as a password, and will mask the text
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        builder.setView(input);
// Set up the buttons
        builder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                todo_new = input.getText().toString();
                if ("".equals(todo_new)) {
                    Toast.makeText(NewToDoList.this, getString(R.string.todo_add_error), Toast.LENGTH_SHORT).show();
                    return;
                }
                //Toast.makeText(NewToDoList.this, todo_new, Toast.LENGTH_SHORT).show();
                HashMap<String, Object> map = new HashMap<>();
                map.put("todo", todo_new);
                map.put("eventId", id);
                todoADDTASK = new addTodoTask(NewToDoList.this, Endpoints.ADD_TODO, map, todo_new);
                todoADDTASK.execute();
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
        offset = 0;
        eventItemArrayList.clear();
        this.adapter1 = (new CustomList(this, eventItemArrayList));
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
        map.put("eventId", id);
        task = new getTodos(this, Endpoints.GET_TODOS, map, offset);
        task.execute();
    }

    void callback(JSONObject jsonObject) {
        try {
            if (jsonObject.getBoolean("success")) {
                JSONArray array = jsonObject.getJSONArray("todos");
                if (array.length() != 10) {
                    done = true;
                }
                offset += array.length();
                TODO.fromJSONArray(array, eventItemArrayList, id);
                adapter1.notifyDataSetChanged();
            } else {
                Toast.makeText(this, getString(R.string.error_loading_events), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.error_loading_events), Toast.LENGTH_SHORT).show();
        }
    }
}

class TODO {
    public String eventId;
    public String id;
    public String todo;
    public Boolean checked = null;

    static void fromJSONArray(JSONArray array, ArrayList<TODO> list, String eventId) {
        int i;
        for (i = 0; i < array.length(); i++) {
            try {
                JSONObject jsonObject = array.getJSONObject(i);
                String date = new SimpleDateFormat("d MMM, yyyy").format((new Date((jsonObject.getLong("date_created")))));
                list.add(
                        new TODO(
                                jsonObject.getString("_id"), eventId, jsonObject.getString("todo"), jsonObject.getBoolean("done")
                        )
                );
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public TODO(String id, String a, String b, Boolean c) {
        this.id = id;
        this.eventId = a;
        this.todo = b;
        this.checked = c;
    }

}

class CustomList extends ArrayAdapter<TODO> {
    NewToDoList context;
    ArrayList<TODO> list;

    public CustomList(NewToDoList context, ArrayList<TODO> list) {
        super(context, R.layout.row_todo, list);
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
        TODO item = list.get(position);
        View rowView = inflater.inflate(R.layout.row_todo, null, true);
        TextView txtTitle = (TextView) rowView.findViewById(R.id.text);
        final String text = item.todo;
        txtTitle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_EDIT);
                intent.setType("vnd.android.cursor.item/event");
                intent.putExtra(CalendarContract.Events.TITLE, text );
                context.startActivity(intent);
            }
        });
        final CheckBox txtTitle1 = (CheckBox) rowView.findViewById(R.id.checkbox);
        txtTitle1.setChecked(item.checked);
        txtTitle.setText(item.todo);
        final String id1 = item.eventId;
        final String id2 = item.id;
        txtTitle1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("item", id1);
                map.put("itemID", id2);
                map.put("status", String.valueOf(txtTitle1.isChecked()));
                Log.d(TAG, "onClick: ss" + String.valueOf(txtTitle1.isChecked()));
                new markAsDone(context, Endpoints.UPDATE_TODO, map, txtTitle1).execute();
            }
        });
        rowView.findViewById(R.id.delete).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HashMap<String, Object> map = new HashMap<>();
                map.put("todoId", id2);
                new deleteTask(context, Endpoints.DELETE_TODO, map, position).execute();
            }
        });
        return rowView;
    }
}


class getTodos extends AsyncTask<Object, Void, JSONObject> {
    NewToDoList baseActivity;
    String url;
    HashMap<String, Object> map;
    int offset;

    public getTodos(NewToDoList baseActivity, String url, HashMap<String, Object> map, int offset) {
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


class addTodoTask extends AsyncTask<Object, Void, JSONObject> {
    NewToDoList baseActivity;
    String url;
    HashMap<String, Object> map;
    int offset;
    String todo;

    public addTodoTask(NewToDoList baseActivity, String url, HashMap<String, Object> map, String todo) {
        super();
        this.todo = todo;
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
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(baseActivity, baseActivity.getString(R.string.successfully_added_todo), Toast.LENGTH_SHORT).show();
                try {
                    DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which) {
                                case DialogInterface.BUTTON_POSITIVE:
                                    Intent intent = new Intent(Intent.ACTION_EDIT);
                                    intent.setType("vnd.android.cursor.item/event");
                                    intent.putExtra(CalendarContract.Events.TITLE, todo);
                                    baseActivity.startActivity(intent);
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
                    Toast.makeText(baseActivity, baseActivity.getString(R.string.no_calendar_app), Toast.LENGTH_SHORT).show();
                }
                baseActivity.populate();
            } else {
                throw new Exception("error in server");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}


class markAsDone extends AsyncTask<Object, Void, JSONObject> {
    NewToDoList baseActivity;
    String url;
    HashMap<String, Object> map;
    CheckBox temp;

    public markAsDone(NewToDoList baseActivity, String url, HashMap<String, Object> map, CheckBox inp) {
        super();
        Log.d(TAG, "addEvenetTask: CREATD TASK");
        this.baseActivity = baseActivity;
        this.url = url;
        this.map = map;
        temp = inp;

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
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(baseActivity, baseActivity.getString(R.string.successfully_updated), Toast.LENGTH_SHORT).show();
                //baseActivity.populate();
            } else {
                temp.setChecked(temp.isChecked());
                throw new Exception("error in server");
            }
        } catch (Exception e) {
            Toast.makeText(baseActivity, baseActivity.getString(R.string.couldnt_update_it), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}


class deleteTask extends AsyncTask<Object, Void, JSONObject> {
    NewToDoList baseActivity;
    String url;
    HashMap<String, Object> map;
    int position;

    public deleteTask(NewToDoList baseActivity, String url, HashMap<String, Object> map, int positoin) {
        super();
        Log.d(TAG, "addEvenetTask: CREATD TASK");
        this.baseActivity = baseActivity;
        this.url = url;
        this.map = map;
        position = positoin;

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
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(baseActivity, baseActivity.getString(R.string.successfully_updated), Toast.LENGTH_SHORT).show();
                baseActivity.adapter1.list.remove(position);
                baseActivity.adapter1.notifyDataSetChanged();
                baseActivity.populate();
            } else {
                //temp.setChecked(temp.isChecked());
                throw new Exception("error in server");
            }
        } catch (Exception e) {
            Toast.makeText(baseActivity, baseActivity.getString(R.string.couldnt_update_it), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}

