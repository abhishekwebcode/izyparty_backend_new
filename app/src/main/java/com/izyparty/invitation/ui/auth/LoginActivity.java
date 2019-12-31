package com.izyparty.invitation.ui.auth;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;
import com.izyparty.invitation.templates.ForgotPassword;
import com.izyparty.invitation.ui.home.HomeActivity;
import com.izyparty.invitation.utils.network.Endpoints;
import com.izyparty.invitation.utils.network.requestMaker;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.pixplicity.easyprefs.library.Prefs;
import jp.wasabeef.blurry.Blurry;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class LoginActivity extends BaseActivity {
    public logintask asynctask;
    public EditText email;
    public EditText password;

    void callback(JSONObject jsonObject) throws Exception {
        if (jsonObject.getBoolean("success")) {
            Prefs.putString("token", jsonObject.getString("token"));
            Prefs.putString("number", jsonObject.getString("number"));
            Intent in = new Intent(this, HomeActivity.class);
            getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
            //in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(in);
            try {
                finish();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            Toast.makeText(this, getString(R.string.login_error_text), Toast.LENGTH_SHORT).show();
        }
    }

    public void forgotPassword(View v) {
        startActivity(new Intent(this,ForgotPassword.class).putExtra("from","login"));
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        if (getIntent().getBooleanExtra("logged_out",false)) {
            AlertDialog.Builder builder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
            } else {
                builder = new AlertDialog.Builder(this);
            }
            builder.setTitle(getString(R.string.logged_out_title));
            builder.setMessage(getString(R.string.logged_out_message));
            builder.setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show().setCanceledOnTouchOutside(false);
        }
        //changeStatusBarColor(R.color.black)
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cake);
        Blurry.with(this)
                .sampling(4)
                .from(bitmap)
                .into((ImageView) findViewById(R.id.background));
        email = (EditText) findViewById(R.id.email);
        password = (EditText) findViewById(R.id.password1);
        findViewById(R.id.textView5).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final HashMap<String, Object> map = new HashMap<>();
                map.put("number", "+33"+ email.getText().toString());
                map.put("password", password.getText().toString());
                FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(LoginActivity.this, new OnSuccessListener<InstanceIdResult>() {
                    @Override
                    public void onSuccess(InstanceIdResult instanceIdResult) {
                        String newToken = instanceIdResult.getToken();
                        map.put("token", newToken);
                        if (asynctask != null) {
                            asynctask.cancel(true);
                            asynctask = null;
                        }
                        asynctask = new logintask(LoginActivity.this, Endpoints.LOGIN_URL, map);
                        asynctask.execute();
                    }
                });
            }
        });

        ((TextView) findViewById(R.id.termsandc)).setText(Html.fromHtml(getString(R.string.signup_html)));

        findViewById(R.id.create).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });
    }

}


class logintask extends AsyncTask<Object, Void, JSONObject> {
    LoginActivity baseActivity;
    String url;
    HashMap<String, Object> map;

    public logintask(LoginActivity baseActivity, String url, HashMap<String, Object> map) {
        super();
        this.baseActivity = baseActivity;
        this.url = url;
        this.map = map;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        baseActivity.recycleDialog();
        baseActivity.dialog = new ProgressDialog(baseActivity);
        baseActivity.dialog.setMessage(baseActivity.getString(R.string.logging_in_text));
        baseActivity.dialog.setCanceledOnTouchOutside(false);
        baseActivity.dialog.show();
    }

    @Override
    protected JSONObject doInBackground(Object... objects) {
        try {
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
        baseActivity.dialog.dismiss();
        try {
            baseActivity.callback(jsonObject);
        } catch (Exception e) {
            Toast.makeText(baseActivity, baseActivity.getString(R.string.error_logging_in), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }
}
