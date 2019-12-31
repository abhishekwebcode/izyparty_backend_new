package com.izyparty.invitation.event;

import android.os.Bundle;
import com.izyparty.invitation.R;
import com.izyparty.invitation.templates.BaseActivity;

public class changeContacts extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_contacts);
        baseCreate(this);
    }
}
