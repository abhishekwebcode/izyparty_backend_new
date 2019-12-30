package com.easyparty.invitation.event.Respond;

import android.os.Bundle;
import com.easyparty.invitation.R;
import com.easyparty.invitation.templates.BaseActivity;

public class manageInvitation extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_invitation);
        baseCreate(this);
    }
}
