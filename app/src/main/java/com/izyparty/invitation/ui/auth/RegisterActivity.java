package com.izyparty.invitation.ui.auth;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.MainThread;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;
import com.izyparty.invitation.ui.home.HomeActivity;
import com.izyparty.invitation.utils.network.Endpoints;
import com.izyparty.invitation.utils.network.asynctask;
import com.izyparty.invitation.utils.network.callback;
import com.izyparty.invitation.utils.network.requestMaker;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import com.pixplicity.easyprefs.library.Prefs;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

;

public class RegisterActivity extends BaseActivity implements callback {
public EditText email;
public EditText name;
public EditText password;
public EditText passwordConfirm;
public signupTasl asynctask;



    public static int APP_REQUEST_CODE = 99;

    @Override
    protected void onActivityResult(
            final int requestCode,
            final int resultCode,
            final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == APP_REQUEST_CODE) {
            // confirm that this response matches your request
            AccountKitLoginResult loginResult = data.getParcelableExtra(AccountKitLoginResult.RESULT_KEY);
            String toastMessage;
            if (loginResult.getError() != null) {
                toastMessage = loginResult.getError().getErrorType().getMessage();
                //showErrorActivity(loginResult.getError());
                //Toast.makeText(this, "Error verifying your phone", Toast.LENGTH_SHORT).show();
                Toast.makeText(
                        this,
                        toastMessage,
                        Toast.LENGTH_LONG)
                        .show();
            } else if (loginResult.wasCancelled()) {
                toastMessage = getString(R.string.Signup_cancelled);
                Toast.makeText(
                        this,
                        toastMessage,
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                Log.d("FACEBOOK", "onActivityResult: "+loginResult.getAuthorizationCode());
                continueSignup(loginResult.getAuthorizationCode());
                AccountKit.logOut();
            }
        }
    }

    public void continueSignup(String code) {
        final HashMap<String,Object> map = new HashMap<>();
        map.put("email",email.getText().toString());
        map.put("name",name.getText().toString());
        map.put("password",password.getText().toString());
        map.put("passwordConfirm",passwordConfirm.getText().toString());
        map.put("code",code);
        //final HashMap<String, Object> map = new HashMap<>();
        FirebaseInstanceId.getInstance().getInstanceId().addOnSuccessListener(RegisterActivity.this, new OnSuccessListener<InstanceIdResult>() {
            @Override
            public void onSuccess(InstanceIdResult instanceIdResult) {
                String newToken = instanceIdResult.getToken();
                map.put("token", newToken);
                if (RegisterActivity.this.asynctask!=null) {
                    asynctask.cancel(true);
                    asynctask=null;
                }
                RegisterActivity.this.asynctask=new signupTasl(RegisterActivity.this,Endpoints.SIGNUP_URL,map);
                asynctask.execute();
            }
        });

    }


    public void phoneLogin(final View view) {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.CODE); // or .ResponseType.TOKEN
        configurationBuilder.setSMSWhitelist(new String[] {"FR"});
        configurationBuilder.setDefaultCountryCode("FR");
        // ... perform additional configuration ...
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intent, APP_REQUEST_CODE);
    }


    @Override
    public void callback(JSONObject jsonObject) {
        try{
            Log.d("DEBUG JSON", "callback: "+jsonObject.toString());
            if (jsonObject.getBoolean("success")) {
                Prefs.putString("token", jsonObject.getString("token"));
                Prefs.putString("number", jsonObject.get("number").toString());
                /*
                    as per client request , signup also logs in successfully
                 */
                //Toast.makeText(this, getString(R.string.successful_signup), Toast.LENGTH_SHORT).show();
                Intent in = new Intent(this, HomeActivity.class);
                getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                //in.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK|Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(in);
                try {
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            else {
                //Toast.makeText(this, "Cannot sign you up", Toast.LENGTH_SHORT).show();
                Toast.makeText(this, jsonObject.getString("message"), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {e.printStackTrace();}
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        //changeStatusBarColor1(R.color.barBlue);
        email=findViewById(R.id.email);
        name=findViewById(R.id.name);
        password=findViewById(R.id.password);
        passwordConfirm=findViewById(R.id.passwordConfirm);
        findViewById(R.id.CancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cancel();
            }
        });
        findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signup();
            }
        });
    }

    void signup() {
        Log.d("okk", "signup: log sample messa");
        HashMap<String,Object> map = new HashMap<>();
        map.put("email",email.getText().toString().toLowerCase());
        map.put("name",name.getText().toString());
        map.put("password",password.getText().toString());
        map.put("passwordConfirm",passwordConfirm.getText().toString());
        for (Map.Entry<String,Object> entry : map.entrySet()) {
            if (entry.getValue().equals("")) {
                Toast.makeText(this, getString(R.string.fill_alert_fields_signup), Toast.LENGTH_SHORT).show();
                return;
            }
        }
        if (!(map.get("password").equals(map.get("passwordConfirm")))) {
            Toast.makeText(this, getString(R.string.fill_passwords_coorecy), Toast.LENGTH_SHORT).show();
            return;
        }
        if (!checkPassword(password.getText().toString(),this)) { return; }
        if (!checkPassword(passwordConfirm.getText().toString(),this)) {return;}
        phoneLogin(null);
    }
    void cancel() {
        finish();
    }
    static Pattern p = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
    public static boolean checkPassword(String password, Context context) {
        if (password.length() < 8) {
            Toast.makeText(context, context.getString(R.string.PASSWORD_LENGTH), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (!password.matches(".*\\d.*")) {
            Toast.makeText(context, context.getString(R.string.PASSWORD_NODIGIT), Toast.LENGTH_SHORT).show();
            return false;
        }
        if (password.equals(password.toLowerCase())) {
            Toast.makeText(context, context.getString(R.string.PASSWORD_NOUPPER), Toast.LENGTH_SHORT).show();
            return false;
        }
        Matcher m = p.matcher(password);
        if (!m.find()) {
            Toast.makeText(context, context.getString(R.string.PASSWORD_SPECIALCHAR), Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

}


 class signupTasl extends AsyncTask<Object,Void, JSONObject> {
    RegisterActivity baseActivity;
    String url;
    HashMap<String,Object> map;
    public signupTasl(RegisterActivity baseActivity,String url,HashMap<String,Object> map) {
        super();
        this.baseActivity=baseActivity;
        this.url=url;
        this.map=map;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        baseActivity.recycleDialog();
        baseActivity.dialog=new ProgressDialog(baseActivity);
        baseActivity.dialog.setMessage(baseActivity.getString(R.string.signing_up));
        baseActivity.dialog.setCanceledOnTouchOutside(false);
        baseActivity.dialog.show();
    }

    @Override
    protected JSONObject doInBackground(Object... objects) {
        try {
            return requestMaker.makeRequests(url,map,baseActivity);
        } catch (Exception e )  {

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
    protected void onPostExecute(JSONObject jsonObject) {
        super.onPostExecute(jsonObject);
        baseActivity.dialog.cancel();
        baseActivity.callback(jsonObject);
    }
}
