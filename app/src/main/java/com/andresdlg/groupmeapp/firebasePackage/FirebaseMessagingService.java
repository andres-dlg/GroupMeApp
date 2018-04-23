package com.andresdlg.groupmeapp.firebasePackage;

import android.app.ActivityManager;
import android.app.AliasActivity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;

import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.uiPackage.MainActivity;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

/**
 * Created by andresdlg on 13/01/18.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        /*String messageTitle = remoteMessage.getNotification().getTitle();
        String messageBody = remoteMessage.getNotification().getBody();
        String click_action = remoteMessage.getNotification().getClickAction();*/

        Map<String,String> data = remoteMessage.getData();
        String click_action = data.get("click_action");
        String dataMessage = data.get("body");
        String dataTitle = data.get("title");
        String dataType = data.get("type");

        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this,getString(R.string.default_notification_channel_id))
                        .setColor(getResources().getColor(R.color.colorPrimary))
                        .setDefaults(Notification.DEFAULT_ALL)
                        .setSmallIcon(R.drawable.app_logo)
                        .setContentTitle(dataTitle)
                        .setContentText(dataMessage);

        Intent resultIntent = new Intent(click_action);

        resultIntent.putExtra("fragment","NotificationFragment");
        if(dataType.equals(NotificationTypes.FRIENDSHIP.toString())){
            resultIntent.putExtra("fragment","NotificationFragment");
        }
        if(dataType.equals(NotificationTypes.MESSAGE.toString())){
            resultIntent.putExtra("fragment","MessagesFragment");
        }

        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );

        mBuilder.setContentIntent(resultPendingIntent);


        int mNotificationId = (int) System.currentTimeMillis();

        NotificationManager mNotifyMgr =
                (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        if(!appInForeGround(this)){
            mNotifyMgr.notify(mNotificationId,
                    mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                            .setSound(alarmSound)
                            .build());
        }
    }

    private boolean appInForeGround(@NonNull Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if(runningAppProcesses == null){
            return false;
        }

        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses){
            if(runningAppProcess.processName.equals(context.getPackageName()) && runningAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                return true;
            }
        }

        return false;
    }
}
