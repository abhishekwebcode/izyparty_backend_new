package com.easyparty.invitation.event;

import android.os.Bundle;
import com.easyparty.invitation.R;
import com.easyparty.invitation.templates.BaseActivity;

public class changeContacts extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_contacts);
        baseCreate(this);
    }
}
