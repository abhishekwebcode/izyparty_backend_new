package com.izyparty.invitation.ui.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import com.izyparty.invitation.R;
import com.izyparty.invitation.event.Respond.RespondsScreen;
import com.izyparty.invitation.templates.BaseActivity;
import com.google.firebase.iid.FirebaseInstanceId;
import com.pixplicity.easyprefs.library.Prefs;

import static android.view.View.GONE;


public class HomeActivity extends BaseActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        //changeStatusBarColor(R.color.barBlue);
        Log.d("Firebase", "token "+ FirebaseInstanceId.getInstance().getToken());
        findViewById(R.id.mcvEvents).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.remove("event");
                startActivity(new Intent(HomeActivity.this,EventsActivity.class).putExtra("order","manage_events").putExtra("arrayFrom","event"));
            }
        });

        findViewById(R.id.mvcInvite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.remove("invitation");
                startActivity(new Intent(HomeActivity.this, RespondsScreen.class).putExtra("order","view_invitations"));
            }
        });

        findViewById(R.id.materialCardView3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.remove("todo");
                startActivity(new Intent(HomeActivity.this, EventsActivity.class).putExtra("order","todo"));
            }
        });

        findViewById(R.id.materialCardView4).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Prefs.remove("gift");
                startActivity(new Intent(HomeActivity.this, EventsActivity.class).putExtra("order","gifts").putExtra("arrayFrom","gift"));
            }
        });
        baseCreate(this);
        start(getWindow().getDecorView());
    }

    @Override
    protected void onResume() {
        super.onResume();
        start(getWindow().getDecorView());
    }

    public void start(View getter) {
        int show1=Prefs.getInt("event",0);
        if (show1!=0) {
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge1)).setVisibility(View.VISIBLE);
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge1)).setNumber(show1);
        }
        else {
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge1)).setVisibility(GONE);
        }
        int show2=Prefs.getInt("invitation",0);
        if (show2!=0) {
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge2)).setVisibility(View.VISIBLE);
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge2)).setNumber(show2);
        }
        else {
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge2)).setVisibility(GONE);
        }
        int show3=Prefs.getInt("todo",0);
        if (show3!=0) {
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge3)).setVisibility(View.VISIBLE);
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge3)).setNumber(show3);
        }
        else {
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge3)).setVisibility(GONE);
        }
        int show4=Prefs.getInt("gift",0);
        if (show4!=0) {
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge4)).setVisibility(View.VISIBLE);
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge4)).setNumber(show4);
        }
        else {
            ((com.nex3z.notificationbadge.NotificationBadge)findViewById(R.id.badge4)).setVisibility(GONE);
        }
    }


}
