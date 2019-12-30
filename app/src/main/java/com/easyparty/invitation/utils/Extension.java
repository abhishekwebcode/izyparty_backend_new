package com.easyparty.invitation.utils;

import android.app.Activity;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.view.View;
import com.easyparty.invitation.R;

public class Extension {
    static void changeStatusBarColor(int color, Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && color == R.color.white) {
            activity.getWindow().setStatusBarColor(ContextCompat.getColor(activity,color));
            activity.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        } else {

        }
    }
}
