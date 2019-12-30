package com.easyparty.invitation.templates;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import com.easyparty.invitation.R;
import com.easyparty.invitation.ui.auth.RegisterActivity;
import com.easyparty.invitation.utils.network.Endpoints;
import com.easyparty.invitation.utils.network.requestMaker;
import com.facebook.accountkit.AccountKit;
import com.facebook.accountkit.AccountKitLoginResult;
import com.facebook.accountkit.ui.AccountKitActivity;
import com.facebook.accountkit.ui.AccountKitConfiguration;
import com.facebook.accountkit.ui.LoginType;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

public class ForgotPassword extends BaseActivity {
    static int FACEBOOK_INTENT_CODE = 234;
    public TextView password;
    public TextView COnfirmpassword;
    public resetTask asynctask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);
        password = (TextView) findViewById(R.id.password);
        COnfirmpassword = (TextView) findViewById(R.id.passwordConfirm);
        findViewById(R.id.signUpButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password.getText().toString().length() == 0 || COnfirmpassword.getText().toString().length() == 0) {
                    Toast.makeText(ForgotPassword.this, getString(R.string.password_fields_empty), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!RegisterActivity.checkPassword(password.getText().toString(),ForgotPassword.this)) {
                    return;
                }
                if (!RegisterActivity.checkPassword(COnfirmpassword.getText().toString(),ForgotPassword.this)) {
                    return;
                }
                if (!(password.getText().toString().equals(COnfirmpassword.getText().toString()))) {
                    Toast.makeText(ForgotPassword.this, getString(R.string.password_dont_match), Toast.LENGTH_SHORT).show();
                    return;
                }
                phoneLogin(v);
            }
        });
        findViewById(R.id.CancelButton).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    public void callback(JSONObject jsonObject) {
        try{
            Log.d("DEBUG JSON", "callback: "+jsonObject.toString());
            if (jsonObject.getBoolean("success")) {
                Toast.makeText(this, getString(R.string.successful_reset_password), Toast.LENGTH_SHORT).show();
                finish();
            }
            else {
                //Toast.makeText(this, "Cannot sign you up", Toast.LENGTH_SHORT).show();
                String someStringFromXML = getString(getResources().getIdentifier(jsonObject.getString("message"), getString(R.string.error), getPackageName()));
                Toast.makeText(this, someStringFromXML, Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {e.printStackTrace();Toast.makeText(this, getString(R.string.error), Toast.LENGTH_SHORT).show();}
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == FACEBOOK_INTENT_CODE) {
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
                toastMessage = getString(R.string.reset_cancelled);
                Toast.makeText(
                        this,
                        toastMessage,
                        Toast.LENGTH_LONG)
                        .show();
            } else {
                Log.d("FACEBOOK", "onActivityResult: " + loginResult.getAuthorizationCode());
                continueSignup(loginResult.getAuthorizationCode());
                AccountKit.logOut();
            }
        }
    }

    public void continueSignup(String code) {
        HashMap<String, Object> map = new HashMap<String, Object>();
        map.put("password", password.getText().toString());
        map.put("code", code);
        if (asynctask != null) {
            asynctask.cancel(true);
            asynctask = null;
        }
        asynctask = new resetTask(ForgotPassword.this, Endpoints.RESET_PASSWORD, map);
        asynctask.execute();
    }


    public void phoneLogin(final View view) {
        final Intent intent = new Intent(this, AccountKitActivity.class);
        AccountKitConfiguration.AccountKitConfigurationBuilder configurationBuilder =
                new AccountKitConfiguration.AccountKitConfigurationBuilder(LoginType.PHONE, AccountKitActivity.ResponseType.CODE); // or .ResponseType.TOKEN
        // ... perform additional configuration ...
        intent.putExtra(AccountKitActivity.ACCOUNT_KIT_ACTIVITY_CONFIGURATION, configurationBuilder.build());
        startActivityForResult(intent, FACEBOOK_INTENT_CODE);
    }

    class resetTask extends AsyncTask<Object, Void, JSONObject> {
        ForgotPassword baseActivity;
        String url;
        HashMap<String, Object> map;

        public resetTask(ForgotPassword baseActivity, String url, HashMap<String, Object> map) {
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
            baseActivity.dialog.setMessage(baseActivity.getString(R.string.resetting));
            baseActivity.dialog.setCanceledOnTouchOutside(false);
            baseActivity.dialog.show();
        }

        @Override
        protected JSONObject doInBackground(Object... objects) {
            try {
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
            baseActivity.callback(jsonObject);
        }
    }

}


