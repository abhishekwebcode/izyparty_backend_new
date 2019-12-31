package com.izyparty.invitation.templates;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import com.izyparty.invitation.R;
import com.izyparty.invitation.ui.OnboardingActivity;
import com.izyparty.invitation.ui.home.HomeActivity;
import com.pixplicity.easyprefs.library.Prefs;
import okhttp3.OkHttpClient;
import okhttp3.Request;

import java.util.Locale;

import static android.support.constraint.Constraints.TAG;

public class Splash extends BaseActivity {
IsInternetActive task;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        start();
    }

    public void afterLanguage(String language) {
        Configuration config = getBaseContext().getResources().getConfiguration();
        Locale locale = new Locale(language);
        Locale.setDefault(locale);
        config.locale = locale;
        getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
        checkInternet();
    }

    public void start() {
        if (Prefs.contains("language")) {
            afterLanguage(Prefs.getString("language","fr"));
        }
        else {
            String[] colors = {"Anglais (ENGLIGH)","Français (FRENCH)"};
            AlertDialog.Builder builder; if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        builder = new AlertDialog.Builder(this, android.R.style.Theme_Material_Light_Dialog_Alert);
    } else {
        builder = new AlertDialog.Builder(this);
    }
            builder.setTitle(getString(R.string.select_text_lang));
            builder.setItems(colors, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            Prefs.putString("language","en");
                            break;
                        case 1:
                            Prefs.putString("language","fr");
                            break;
                    }
                    dialog.dismiss();
                    afterLanguage(Prefs.getString("language","fr"));
                }
            });
            AlertDialog temp = builder.show();
            temp.setCancelable(false);
            temp.setCanceledOnTouchOutside(false);
            temp.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {

                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        start();
    }

    public void furnishIntent(Intent intent) {
        intent.fillIn(getIntent(),Intent.FILL_IN_DATA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
    }

    public void internetOK() {
        //dialog.dismiss();
        if (Prefs.contains("token")) {
            Log.d(TAG, "TOKEN: "+Prefs.getString("token",null));
            Intent intent = new Intent(this, HomeActivity.class);
            intent.putExtra("from", "home");
            furnishIntent(intent);
            startActivity(intent);
            finish();
        } else {
            Log.d(TAG, "internetOK: CALLED OK");
            Intent intent = new Intent(this, OnboardingActivity.class);
            furnishIntent(intent);
            startActivity(intent);
            finish();
        }
    }

    public void internetNOTOK() {
        //dialog.dismiss();
        //recycleDialog();ler`
        new AlertDialog.Builder(this)
                .setTitle(R.string.inernte_unaviable)
                .setMessage(R.string.splhash_retry)
                .setIcon(android.R.drawable.ic_dialog_alert)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        checkInternet();
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Splash.this.finish();
                    }
                }).show();
    }

    public void checkInternet() {
        /*recycleDialog();
        dialog = new ProgressDialog(this);
        dialog.show();*/
        if (task!=null && !task.isCancelled()) {
            task.cancel(true);
            task=null;
        }
        task=new IsInternetActive(Splash.this);
        task.execute();
    }

}

class IsInternetActive extends AsyncTask<Void, Void, String> {
    Splash baseActivity;
    String json = "NOTOK";

    public IsInternetActive(Splash splash) {
        this.baseActivity = splash;
        Log.d(TAG, "IsInternetActive: CREATING");
    }

    @Override
    protected String doInBackground(Void... params) {
        try {
            json = NetworkTest.pingTestWeb() ? "OK":"NOTOK";
        } catch (Exception e) {
            e.printStackTrace();
            json = "NOTOK";
        }
        return json;
    }

    @Override
    protected void onPostExecute(String result) {
        Log.d("ONPOS", "onPostExecute: " + result);
        try {
            switch (result) {
                case "OK":
                    baseActivity.internetOK();
                    Log.d(TAG, "internetOK: CALLED OKFROM PST");
                    break;
                case "NOTOK":
                    baseActivity.internetNOTOK();
                    break;
            }
        } catch (Throwable e) {e.printStackTrace();}
    }

}

class NetworkTest {
    public static boolean pingTestWeb() {
        try {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url("http://www.google.com")//My server address will go here
                    .build();
            client.newCall(request).execute();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}