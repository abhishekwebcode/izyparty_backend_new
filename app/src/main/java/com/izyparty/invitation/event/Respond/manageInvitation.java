package com.izyparty.invitation.event.Respond;

import android.os.Bundle;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;

public class manageInvitation extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_invitation);
        baseCreate(this);
    }
}
