package com.izyparty.invitation.templates;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.PopupMenu;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import com.izyparty.invitation.R;
import com.izyparty.invitation.ui.OnboardingActivity;
import com.izyparty.invitation.ui.home.HomeActivity;
import com.izyparty.invitation.utils.network.Endpoints;
import com.izyparty.invitation.utils.network.logoutTask;
import com.pixplicity.easyprefs.library.Prefs;

import java.util.Locale;

public class BaseActivity extends AppCompatActivity {
    public ProgressDialog dialog;
    public AlertDialog alertDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void setTitle(String title) {
        try {
            ((TextView) findViewById(R.id.title_text)).setText(title);
        } catch (Exception e){e.printStackTrace();}
    }

    @Override
    public void onBackPressed() {
        if (dialog!=null) {dialog.cancel();dialog=null;}
        super.onBackPressed();
    }

    public void recycleDialog() {if (dialog!=null) {dialog.cancel();dialog=null;}}

    public void baseCreate(final BaseActivity baseActivity) {
        new Handler(  ).postDelayed(new Runnable() {
            @Override
            public void run() {
                baseCreate1(baseActivity);
            }
        }, 500 );
    }

    public void baseCreate1(final BaseActivity baseActivity) {
        if (baseActivity.getWindow().getDecorView().getRootView().isShown()) {
            try {
                View menuItemView = findViewById(R.id.menu); // SAME ID AS MENU ID
                final PopupMenu popupMenu = new PopupMenu(baseActivity, menuItemView);
                popupMenu.inflate(R.menu.menu_options_home);
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        switch (menuItem.getItemId()) {
                            case R.id.logout:
                                try {
                                    logoutTask.callbackLogout callback = new logoutTask.callbackLogout(){
                                        @Override
                                        public void onDoneCallBack() {
                                            super.onDoneCallBack();
                                            String lang = Prefs.getString("language","not");
                                            Prefs.remove("token");
                                            Prefs.getPreferences().edit().clear().apply();
                                            Prefs.edit().clear().commit();
                                            if (!("not".equals(lang))) {
                                                Configuration config = getBaseContext().getResources().getConfiguration();
                                                Locale locale = new Locale(lang);
                                                Locale.setDefault(locale);
                                                config.locale = locale;
                                                getBaseContext().getResources().updateConfiguration(config, getBaseContext().getResources().getDisplayMetrics());
                                            }
                                            Intent intent = new Intent(BaseActivity.this, OnboardingActivity.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);
                                            finish();
                                        }
                                    };
                                    new logoutTask(BaseActivity.this, Endpoints.LOGOUT,callback).execute();
                                } catch (Throwable e ) {e.printStackTrace();Log.d("LOGOUT","--ABOVE--");}
                                break;
                            case R.id.action_profile:
                                startActivity(new Intent(baseActivity,My_Profile.class));
                                break;
                            case R.id.forgot:
                                startActivity(new Intent(baseActivity,ForgotPassword.class));
                                break;
                            case R.id.action_password:
                                startActivity(new Intent(baseActivity,ChangePassword.class));
                                break;
                        }
                        return true;
                    }
                });
                menuItemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        popupMenu.show();
                    }
                });
            } catch (Throwable e) {
                e.printStackTrace();
            }
            try {
                if (!(baseActivity instanceof HomeActivity)) {
                    ImageView imageview = ((ImageView) findViewById(R.id.firstImage));
                    imageview.setImageDrawable(getResources().getDrawable(R.drawable.back));
                    imageview.getLayoutParams().height = (int) getResources().getDimension(R.dimen.back_height);
                    imageview.getLayoutParams().width = (int) getResources().getDimension(R.dimen.back_width);
                    findViewById(R.id.firstImage).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            onBackPressed();
                        }
                    });
                    try {
                        findViewById(R.id.backer).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                onBackPressed();
                            }
                        });
                    } catch (Exception e) {e.printStackTrace();}
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
