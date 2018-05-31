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

import com.andresdlg.groupmeapp.R;
import com.andresdlg.groupmeapp.Utils.NotificationTypes;
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

        //RECIBO LOS DATOS DE CLOUD
        Map<String,String> data = remoteMessage.getData();

        //OBTENGO LOS DATOS
        String click_action = data.get("click_action");
        String dataMessage = data.get("message");
        String s = data.get("timeMessage");
        long timeMessage = System.currentTimeMillis();
        if(s!=null){
            timeMessage = Long.parseLong(data.get("timeMessage"));
        }
        String dataTitle = data.get("title");
        String dataType = data.get("type");
        String groupKey = data.get("groupKey");
        String groupName = data.get("groupName");
        String userName = data.get("userName");
        String from_user_id = data.get("from_user_id");


        Uri alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);

        NotificationCompat.Builder mBuilder = null;

        NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

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
                                    .addMessage(message)
                                    .setConversationTitle("Nuevo mensaje"));
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
                    mBuilder.setColor(ContextCompat.getColor(this,R.color.colorPrimary))
                            .setDefaults(Notification.DEFAULT_ALL)
                            .setSmallIcon(R.drawable.app_logo_2)
                            .setContentTitle(dataTitle)
                            .setGroup(dataType)
                            .setContentText(dataMessage)
                            .setGroupSummary(true)
                            .setStyle(new NotificationCompat.MessagingStyle(userName)
                                    .addMessage(message)
                                    .setConversationTitle("Nuevo mensaje"));
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
        Intent resultIntent = new Intent(click_action);
        //resultIntent.putExtra("fragment","NotificationFragment");
        if(dataType.equals(NotificationTypes.FRIENDSHIP.toString())){
            resultIntent.putExtra("dialogFragment","NotificationFragment");
        }
        else if(dataType.equals(NotificationTypes.GROUP_INVITATION.toString())){
            resultIntent.putExtra("fragment","GroupsFragment");
        }
        else if(dataType.equals(NotificationTypes.SUBGROUP_INVITATION.toString())){
            resultIntent.putExtra("fragment","GroupsFragment");
        }
        else if(dataType.equals(NotificationTypes.MESSAGE.toString())){
            resultIntent.putExtra("fragment","MessagesFragment");
        }
        else if(dataType.equals(NotificationTypes.NEW_POST.toString())){
            resultIntent.putExtra("fragment","NewsFragment");
        }
        else if(dataType.equals(NotificationTypes.NEW_FILE.toString())){
            resultIntent.putExtra("fragment","GroupsFragment");
        }
        else if(dataType.equals(NotificationTypes.FRIENDSHIP_ACCEPTED.toString())){
            resultIntent.putExtra("dialogFragment","GroupsFragment");
        }

        //ASIGNO EL INTENT PARA CUANDO SE HAGA EL CLICK
        PendingIntent resultPendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        resultIntent,
                        PendingIntent.FLAG_UPDATE_CURRENT
                );
        mBuilder.setContentIntent(resultPendingIntent);

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
