package com.izyparty.invitation.utils.push;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import com.izyparty.invitation.R;
import com.izyparty.invitation.event.Respond.GiftSelect;
import com.izyparty.invitation.event.Respond.showInvitation;
import com.izyparty.invitation.templates.Splash;
import com.izyparty.invitation.ui.eventsManage.seeResponsesNew;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pixplicity.easyprefs.library.Prefs;
import org.json.JSONArray;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import static io.fabric.sdk.android.Fabric.TAG;


public class messaging extends FirebaseMessagingService {
    public static String eventsArray = "eventsArray";
    public static String invitesArray = "invitesArray";
    public static String giftsArray = "giftsArray";
    public static String invitesGiftsArray = "invitesGiftsArray";


    public static ArrayList<String> getBadges( String type) {
        try {
            ArrayList<String> ids= new ArrayList<String>();
            JSONArray jsonArray = new JSONArray(Prefs.getString(type,"[]"));
            for (int i = 0, len = jsonArray.length(); i < len; i++) {
                String obj = jsonArray.getString(i);
                ids.add(obj);
            }
            return ids;
        } catch (Throwable e) {e.printStackTrace();return new ArrayList<String>(); }
    }
    public static void updateBadges(String eventName,String type,Boolean set) {
        try {
            JSONArray jsonArray = new JSONArray(Prefs.getString(type, "[]"));
            JSONArray gg = new JSONArray();
            for (int i = 0, len = jsonArray.length(); i < len; i++) {
                String obj = jsonArray.getString(i);
                if (!eventName.equals(obj)) {
                    gg.put(jsonArray.getString(i));
                }
            }
            Prefs.remove(type);
            Log.d(TAG, "updateBadges: "+type);
            Log.d(TAG, "updateBadges: "+gg.toString());
            Prefs.putString(type, gg.toString());
        } catch (Throwable e) {e.printStackTrace();}
    }

    public static void putBadges(String eventName,String type) {
        try {
            JSONArray jsonArray = new JSONArray(Prefs.getString(type, "[]"));
            jsonArray.put(eventName);
            Prefs.putString(type, jsonArray.toString());
        } catch (Throwable e) {e.printStackTrace();}
    }

    public void showNotification(Context context, String title, String body, Intent intent) {
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Date now = new Date();
        int id = Integer.parseInt(new SimpleDateFormat("ddHHmmss",  Locale.US).format(now));
        String channelId = "izyparty";
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            String channelName = "IzyParty";
            int importance = NotificationManager.IMPORTANCE_HIGH;
            NotificationChannel mChannel = new NotificationChannel(
                    channelId, channelName, importance);
            notificationManager.createNotificationChannel(mChannel);
        }
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setContentText(body);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addNextIntent(new Intent(context, Splash.class).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        stackBuilder.addNextIntent(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                22,
                PendingIntent.FLAG_UPDATE_CURRENT
        );
        mBuilder.setDefaults(Notification.DEFAULT_SOUND);
        mBuilder.setContentIntent(resultPendingIntent);
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        mBuilder.setPriority(NotificationCompat.PRIORITY_HIGH);
        notificationManager.notify(id, mBuilder.build());
    }



    @Override
    public void onNewToken(String s) {
        super.onNewToken(s);
        Prefs.putString("FCMToken", s);
        if (Prefs.contains("token")) {
            String fcmTOoen=s;
            String token = Prefs.getString("token","");
            // TODO UPDATE FCM TOKEN TO SERVER
            if (!token.equals("")) {
                //Toast.makeText(this,"",Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        //super.onMessageReceived(remoteMessage);
        Log.d("MESSAGING", "From: " + remoteMessage.getFrom());
        if (remoteMessage.getData().size() > 0) {
            Map<String, String> map = remoteMessage.getData();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                Log.d(TAG, "onMessageReceived: " + key + "||" + value);
            }
            switch (map.get("type")) {
                case "GIFT_DELETED":
                    putBadges(map.get("eventId"),invitesGiftsArray);
                    int existing8=Prefs.getInt("invitation",0);
                    Prefs.putInt("invitation",existing8+1);
                    Intent intent5 = new Intent(this, GiftSelect.class).putExtra("eventId",map.get("eventId")).putExtra("from","notif");
                    String title = getString(R.string.your_gift)+" "+map.get("gift")+" "+getString(R.string.was_deleted)+" "+map.get("organiser");
                    String body = getString(R.string.click_del)+" "+map.get("eventName");
                    showNotification(this,title,body,intent5);
                    break;
                case "GIFT_SELECTED":
                    int existing6=Prefs.getInt("event",0);
                    putBadges(map.get("eventId"),eventsArray);
                    Log.d(TAG, "onMessageReceived: EVENT SERVICE FROM "+String.valueOf(existing6));
                    Prefs.putInt("event",existing6+1);
                    /**
                     * Update badges for gifts section
                     */
                    int existing5=Prefs.getInt("gift",0);
                    putBadges(map.get("eventId"),giftsArray);
                    Log.d(TAG, "onMessageReceived: EVENT SERVICE FROM "+String.valueOf(existing5));
                    Prefs.putInt("gift",existing5+1);
                    /**
                     * Send notification
                     */
                    Intent intent4 = new Intent(this, seeResponsesNew.class).putExtra("eventId",map.get("eventId")).putExtra("from","notif").putExtra("guest",false);
                    showNotification(this,getString(R.string.new_gift_selected_for)+" "+map.get("childname")+" "+getString(R.string.party),getString(R.string.click_here_to_get_gift_selected_by)+" "+map.get("InviteeName"),intent4);
                    break;
                case "GIFT_ADD":
                    Log.d(TAG, "onMessageReceived: GIFT ADD");
                    putBadges(map.get("eventId"),invitesGiftsArray);
                    int existing7=Prefs.getInt("invitation",0);
                    Prefs.putInt("invitation",existing7+1);
                    Intent intent3 = new Intent(this, GiftSelect.class).putExtra("eventId",map.get("eventId")).putExtra("from","notif").putExtra("choice",map.get("gift"));
                    showNotification(this,getString(R.string.new_gift_added_for)+" "+map.get("childname")+" "+getString(R.string.party),getString(R.string.new_gift_title)+" "+map.get("gift")+". "+getString(R.string.tap_here_to_select_gift_added)+" "+map.get("OwnerName"),intent3);
                    break;
                case "CHANGE_EVENT":
                    Log.d(TAG, "onMessageReceived: UPDATE EVENT");
                    putBadges(map.get("eventId"),invitesArray);
                    int existing4=Prefs.getInt("invitation",0);
                    Prefs.putInt("invitation",existing4+1);
                    Intent intent2 = new Intent(this, showInvitation.class).putExtra("event_id",map.get("eventId")).putExtra("from","notif");
                    showNotification(this,getString(R.string.update_event_notification_text)+" "+map.get("childname")+" "+getString(R.string.party),getString(R.string.change_was_made_by)+" "+map.get("OwnerName"),intent2);
                    break;
                case "NEW_INVITE":
                    Log.d(TAG, "onMessageReceived: NEW INVITE");
                    putBadges(map.get("eventId"),invitesArray);
                    int existing=Prefs.getInt("invitation",0);
                    Prefs.putInt("invitation",existing+1);
                    Intent intent = new Intent(this, showInvitation.class).putExtra("event_id",map.get("eventId")).putExtra("from","notif");
                    showNotification(this,getString(R.string.new_invite_notification)+" "+map.get("childname")+" "+getString(R.string.party),getString(R.string.rsvp_noti)+" "+map.get("OwnerName"),intent);
                    break;
                case "INVITE_RESPOND":
                    int existing1=Prefs.getInt("event",0);
                    putBadges(map.get("eventId"),eventsArray);
                    Log.d(TAG, "onMessageReceived: EVENT SERVICE FROM "+String.valueOf(existing1));
                    Prefs.putInt("event",existing1+1);
                    Intent intent1 = (new Intent(this, seeResponsesNew.class).putExtra("eventId",map.get("eventId"))).putExtra("from","notif").putExtra("guest",false);
                    showNotification(this,getString(R.string.new_response_noti)+" "+map.get("eventName"),map.get("userName")+" "+getString(R.string.someones_noti_respond),intent1);
                    break;
            }
        }
    }
}

