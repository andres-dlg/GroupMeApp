package com.andresdlg.groupmeapp.firebasePackage;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.andresdlg.groupmeapp.Entities.Group;
import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
import com.andresdlg.groupmeapp.uiPackage.GroupActivity;
import com.andresdlg.groupmeapp.uiPackage.MainActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.RemoteMessage;

import java.util.List;
import java.util.Map;

/**
 * Created by andresdlg on 13/01/18.
 */

public class FirebaseMessagingService extends com.google.firebase.messaging.FirebaseMessagingService {

    NotificationCompat.Builder mBuilder;
    NotificationManager mNotifyMgr;

    String groupKey;
    String dataType;
    String from_user_id;
    Uri alarmSound;

    @Override
    public void onMessageReceived(RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        //LAS NOTIFICACIONES SOLO SE MUESTRAN SI LA APP ESTA EN BACKGROUND
        if(appInForeGround(this)){

            //RECIBO LOS DATOS DE CLOUD
            Map<String,String> data = remoteMessage.getData();

            //OBTENGO LOS DATOS
            //String click_action = data.get("click_action");
            String dataMessage = data.get("message");
            String s = data.get("timeMessage");
            long timeMessage = System.currentTimeMillis();
            if(s!=null){
                timeMessage = Long.parseLong(data.get("timeMessage"));
            }
            String dataTitle = data.get("title");
            dataType = data.get("type");
            groupKey = data.get("groupKey");
            String groupName = data.get("groupName");
            String groupImageUrl = data.get("groupImageUrl");
            String userName = data.get("userName");
            from_user_id = data.get("from_user_id");


            alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

            mBuilder = null;

            mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            //ESTO SOLO LO USO CUANDO SON NOTIICACIONES DE MENSAJES
            NotificationCompat.MessagingStyle.Message message =
                    new NotificationCompat.MessagingStyle.Message(dataMessage,
                            timeMessage,
                            userName);

            //SI ANDROID ES OREO O MAYOR TENGO QUE USAR LOS NOTIFICATION CHANNEL NUEVOS
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                //DEFINO EL CHANNEL
                int importance = NotificationManager.IMPORTANCE_HIGH;
                NotificationChannel mChannel = mNotifyMgr.getNotificationChannel(getString(R.string.default_notification_channel_id));
                if (mChannel == null) {
                    mChannel = new NotificationChannel(getString(R.string.default_notification_channel_id), getString(R.string.default_notification_channel_id), importance);
                    mChannel.setDescription(getString(R.string.default_notification_channel_id));
                    mChannel.enableVibration(true);
                    mChannel.setVibrationPattern(new long[]{100, 200, 300, 400, 500, 400, 300, 200, 400});
                    mNotifyMgr.createNotificationChannel(mChannel);
                }

                //CREO UN CONTRUCTOR PARA LA NOTIFICACION
                mBuilder = new NotificationCompat.Builder(this, getString(R.string.default_notification_channel_id));

                //SI LA NOTIFICACION ES UN MENSAJE
                if(dataType.equals(NotificationTypes.MESSAGE.toString())){

                    //SI LA NOTIFICACION ES UN MENSAJE DE GRUPO
                    if(groupKey!= null){
                        mBuilder.setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setSmallIcon(R.drawable.app_logo_2)
                                .setContentTitle(dataTitle)
                                .setGroup(dataType) // AGRUPO LAS NOTIFICACIONES DEL MISMO TIPO
                                .setContentText(dataMessage)
                                .setGroupSummary(true)
                                .setStyle(new NotificationCompat.MessagingStyle(userName)
                                        .addMessage(message)
                                        .setConversationTitle("Nuevos mensajes en " + groupName));
                    }
                    //SI LA NOTIFICACION ES UN MENSAJE INDIVIDUAL
                    else {
                        mBuilder.setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setSmallIcon(R.drawable.app_logo_2)
                                .setContentTitle(dataTitle)
                                .setGroup(dataType) // AGRUPO LAS NOTIFICACIONES DEL MISMO TIPO
                                .setContentText(dataMessage)
                                .setGroupSummary(true)
                                .setStyle(new NotificationCompat.MessagingStyle(userName)
                                        .addMessage(message));
                    }
                }
                //SI LA NOTIFICACION NO ES UN MENSAJE
                else{
                    mBuilder.setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setSmallIcon(R.drawable.app_logo_2)
                            .setContentTitle(dataTitle)
                            .setGroup(dataType)
                            .setContentText(dataMessage)
                            .setGroupSummary(true);
                }
            }

            //SI LA VERSION ES NOUGAT O MENOR
            else{
                if(dataType.equals(NotificationTypes.MESSAGE.toString())){
                    if(groupKey!= null){
                        mBuilder = new NotificationCompat.Builder(this,getString(R.string.default_notification_channel_id))
                                .setColor(getResources().getColor(R.color.colorPrimary))
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setSmallIcon(R.drawable.app_logo_2)
                                .setContentTitle(dataTitle)
                                .setGroup(dataType) // AGRUPO LAS NOTIFICACIONES DEL MISMO TIPO
                                .setContentText(dataMessage)
                                .setGroupSummary(true)
                                .setStyle(new NotificationCompat.MessagingStyle(userName)
                                        .addMessage(message)
                                        .setConversationTitle("Nuevos mensajes en " + groupName));
                    }else{
                        mBuilder = new NotificationCompat.Builder(this,getString(R.string.default_notification_channel_id))
                                .setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                                .setDefaults(Notification.DEFAULT_ALL)
                                .setSmallIcon(R.drawable.app_logo_2)
                                .setContentTitle(dataTitle)
                                .setGroup(dataType)
                                .setContentText(dataMessage)
                                .setGroupSummary(true)
                                .setStyle(new NotificationCompat.MessagingStyle(userName)
                                        .addMessage(message));
                    }
                }else{
                    mBuilder = new NotificationCompat.Builder(this,getString(R.string.default_notification_channel_id))
                            .setColor(getResources().getColor(R.color.colorPrimary))
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setSmallIcon(R.drawable.app_logo_2)
                            .setContentTitle(dataTitle)
                            .setGroup(dataType) // AGRUPO LAS NOTIFICACIONES DEL MISMO TIPO
                            .setContentText(dataMessage)
                            .setGroupSummary(true);
                }
            }

            //TODO: IMPLEMENTAR LOS CLICKS PARA CADA OPCIÓN
            //DEFINO LOS RESULT INTENT PARA CUANDO SE HAGA CLICK EN LA NOTIFICACION
            //Intent resultIntent = new Intent(click_action);
            Intent resultIntent;
            //resultIntent.putExtra("fragment","NotificationFragment");
            if(dataType.equals(NotificationTypes.FRIENDSHIP.toString())){
                resultIntent = new Intent(this, MainActivity.class);
                resultIntent.putExtra("notification",NotificationTypes.FRIENDSHIP.toString());

                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                showNotification();

            }
            else if(dataType.equals(NotificationTypes.GROUP_INVITATION.toString())){
                resultIntent = new Intent(this, MainActivity.class);
                resultIntent.putExtra("notification",NotificationTypes.GROUP_INVITATION.toString());
                resultIntent.putExtra("groupKey",from_user_id);

                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                showNotification();
            }
            else if(dataType.equals(NotificationTypes.SUBGROUP_INVITATION.toString())){
                FirebaseDatabase.getInstance().getReference("Groups").child(from_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Group group = dataSnapshot.getValue(Group.class);
                        Intent i = new Intent(FirebaseMessagingService.this, GroupActivity.class);
                        i.putExtra("groupImage", group.getImageUrl());
                        i.putExtra("groupName",group.getName());
                        i.putExtra("groupKey",group.getGroupKey());
                        i.putExtra("setSubGroupTab",true);
                        i.putExtra("fromNotificationSubGroupInvitation",true);

                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        FirebaseMessagingService.this,
                                        0,
                                        i,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);

                        showNotification();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
           /* resultIntent = new Intent(this, MainActivity.class);
            resultIntent.putExtra("notification",NotificationTypes.SUBGROUP_INVITATION.toString());*/
            }
            else if(dataType.equals(NotificationTypes.MESSAGE.toString())){
                if(groupKey == null){
                    resultIntent = new Intent(this, MainActivity.class);
                    resultIntent.putExtra("notification",NotificationTypes.MESSAGE.toString());

                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(
                                    this,
                                    0,
                                    resultIntent,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    showNotification();

                }else{
                    Intent i = new Intent(FirebaseMessagingService.this, GroupActivity.class);
                    i.putExtra("groupImage", groupImageUrl);
                    i.putExtra("groupName",groupName);
                    i.putExtra("groupKey",groupKey);
                    i.putExtra("setChatTab",true);
                    i.putExtra("fromNotificationSubGroupInvitation",true);

                    PendingIntent resultPendingIntent =
                            PendingIntent.getActivity(
                                    FirebaseMessagingService.this,
                                    0,
                                    i,
                                    PendingIntent.FLAG_UPDATE_CURRENT
                            );
                    mBuilder.setContentIntent(resultPendingIntent);

                    showNotification();

                }
            }
            else if(dataType.equals(NotificationTypes.NEW_POST.toString())){
                FirebaseDatabase.getInstance().getReference("Groups").child(from_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Group group = dataSnapshot.getValue(Group.class);
                        Intent i = new Intent(FirebaseMessagingService.this, GroupActivity.class);
                        i.putExtra("groupImage", group.getImageUrl());
                        i.putExtra("groupName",group.getName());
                        i.putExtra("groupKey",group.getGroupKey());
                        i.putExtra("setNewsTab",true);
                        i.putExtra("fromNotificationNewPost",true);

                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        FirebaseMessagingService.this,
                                        0,
                                        i,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);

                        showNotification();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            /*resultIntent = new Intent(this, MainActivity.class);
            resultIntent.putExtra("notification",NotificationTypes.NEW_POST.toString());*/
            }
            else if(dataType.equals(NotificationTypes.NEW_FILE.toString())){
                FirebaseDatabase.getInstance().getReference("Groups").child(from_user_id).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Group group = dataSnapshot.getValue(Group.class);
                        Intent i = new Intent(FirebaseMessagingService.this, GroupActivity.class);
                        i.putExtra("groupImage", group.getImageUrl());
                        i.putExtra("groupName",group.getName());
                        i.putExtra("groupKey",group.getGroupKey());
                        i.putExtra("setSubGroupTab",true);
                        i.putExtra("fromNotificationSubGroupInvitation",true);

                        PendingIntent resultPendingIntent =
                                PendingIntent.getActivity(
                                        FirebaseMessagingService.this,
                                        0,
                                        i,
                                        PendingIntent.FLAG_UPDATE_CURRENT
                                );
                        mBuilder.setContentIntent(resultPendingIntent);

                        showNotification();

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            /*resultIntent = new Intent(this, MainActivity.class);
            resultIntent.putExtra("notification",NotificationTypes.NEW_FILE.toString());*/
            }
            else if(dataType.equals(NotificationTypes.FRIENDSHIP_ACCEPTED.toString())){
                resultIntent = new Intent(this, MainActivity.class);
                resultIntent.putExtra("notification",NotificationTypes.FRIENDSHIP_ACCEPTED.toString());

                PendingIntent resultPendingIntent =
                        PendingIntent.getActivity(
                                this,
                                0,
                                resultIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );
                mBuilder.setContentIntent(resultPendingIntent);

                showNotification();

            }

            //ASIGNO EL INTENT PARA CUANDO SE HAGA EL CLICK
        /*if(!dataType.equals(NotificationTypes.SUBGROUP_INVITATION.toString())){
            PendingIntent resultPendingIntent =
                    PendingIntent.getActivity(
                            this,
                            0,
                            resultIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT
                    );
            mBuilder.setContentIntent(resultPendingIntent);
        }*/

        }



    }

    private void showNotification(){
        //ASIGNO UN NOTIFICATION ID | ESTO LO USO PARA LAS NOTIFICACIONES QUE NO SON MENSAJES
        int mNotificationId = (int) System.currentTimeMillis();

        //EL PROXIMO BLOQUE ES PARA MOSTRAR LA NOTIFICACION | LA MISMA SE MUESTRA SI NO ESTOY DENTRO DE LA APP O TENGO EL CELU BLOQUEADO

        //SI LA NOTIFICACION NO ES UN MENSAJE DE GRUPO
        if(groupKey == null){

            //SI LA NOTIFICACION ES UN MENSAJE INDIVIDUAL
            if(dataType.equals(NotificationTypes.MESSAGE.toString())){
                Map<String,Integer> map = ((FireApp) getApplicationContext()).getContactsIds();
                for(Map.Entry<String, Integer> entry: map.entrySet()) {
                    if(entry.getKey().equals(from_user_id)){
                        if(appInForeGround(this)){
                            mNotifyMgr.notify(entry.getValue(),
                                    mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                                            .setSound(alarmSound)
                                            .setAutoCancel(true)
                                            .build());
                        }
                        break;
                    }
                }
            }
            //SI NO ES UN MENSAJE
            else {
                if(appInForeGround(this)){
                    mNotifyMgr.notify(mNotificationId,
                            mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                                    .setSound(alarmSound)
                                    .setAutoCancel(true)
                                    .build());
                }
            }
        }
        //SI LA NOTIFICACION ES UN MENSAJE DE GRUPO
        else{
            Map<String,Integer> map = ((FireApp) getApplicationContext()).getGroupsIds();
            for(Map.Entry<String, Integer> entry: map.entrySet()) {
                if(entry.getKey().equals(groupKey)){
                    if(appInForeGround(this)){
                        mNotifyMgr.notify(entry.getValue(),
                                mBuilder.setVibrate(new long[] { 1000, 1000, 1000, 1000, 1000 })
                                        .setSound(alarmSound)
                                        .setAutoCancel(true)
                                        .build());
                    }
                    break;
                }
            }
        }

        //PARA AGRUPAR NOTIFICACIONES TENGO QUE ejectuar el metodo setGroup en mBuilder y ponerle un mNotificationId IGUALLLL

        //PARA AGRUPAR LOS MENSAJES DEBERIA VER SI LOS MENSAJES SON DEL MISMO AUTOR ADEMAS DE TENER EL MISMO ID JAJAJAJA
    }

    private boolean appInForeGround(@NonNull Context context){
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = activityManager.getRunningAppProcesses();
        if(runningAppProcesses == null){
            return true;
        }

        for (ActivityManager.RunningAppProcessInfo runningAppProcess : runningAppProcesses){
            if(runningAppProcess.processName.equals(context.getPackageName()) && runningAppProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND){
                return false;
            }
        }

        return true;
    }
}
