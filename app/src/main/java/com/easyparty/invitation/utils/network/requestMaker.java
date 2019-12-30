package com.easyparty.invitation.utils.network;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import com.easyparty.invitation.templates.BaseActivity;
import com.easyparty.invitation.ui.auth.LoginActivity;
import com.pixplicity.easyprefs.library.Prefs;
import okhttp3.*;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import static android.content.ContentValues.TAG;
import static com.easyparty.invitation.utils.network.Endpoints.BASE_URL;

public class requestMaker {

    public static String getURL(String url) {
        return BASE_URL+url;
    }

    public static JSONObject makeRequests(String url, HashMap<String,Object> map, Activity activity) throws Exception  {
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        for (Map.Entry<String,Object> entry : map.entrySet()) {
            Log.d(TAG, "makeRequests: "+entry.getKey());
            Log.d(TAG, "makeRequests: "+entry.getValue());
            builder.addFormDataPart(entry.getKey(),entry.getValue().toString());
        }
        RequestBody requestBody = builder.build();
        String token = Prefs.getString("token","");
        Request request = new Request.Builder()
                .url(getURL(url))
                .addHeader("Authorization","Bearer "+ token)
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        try {
            Log.d(TAG, "makeRequests: PUTTING REQUEST");
            Log.d(TAG, "makeRequests: PUTTING REQUEST"+request.toString());
            Response response = okHttpClient.newCall(request).execute();
            String res = response.body().string().toString();
            Log.d("JSON", "makeRequests: " + res);
            Log.e("response ", "onResponse(): " + res );
            JSONObject jsonObject = new JSONObject(res);
            if (jsonObject.has("loggedOutError")) {
                if (jsonObject.getBoolean("loggedOutError")) {
                    Prefs.remove("token");
                    Intent intent = new Intent(activity, LoginActivity.class);
                    intent.putExtra("logged_out",true);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
                    activity.startActivity(intent);
                    activity.finish();
                    return new JSONObject();
                }
            }
            return jsonObject;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("ERROR_FROM_REQUESTER");
        }
    }

    public static asynctask doRequests(BaseActivity baseActivity,String url,HashMap<String,Object> map) {
        return new asynctask(baseActivity, url, map);
    }

}

