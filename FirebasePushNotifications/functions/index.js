'use-strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');
admin.initializeApp(functions.config().firebase);

exports.sendNotification = functions.database.ref("/Users/{user_id}/notifications/{notification_id}").onWrite(event => {

    const user_id = event.params.user_id;
    const notification_id = event.params.notification_id;

    var notificationPromise = admin.database().ref("/Users/"+ user_id + "/notifications/" + notification_id).once('value');

    return notificationPromise.then(results => {

        var notification = results.val();

        const from_user_id = notification.from;
        const from_message = notification.message;
        const notification_title = notification.title;
        const notification_type = notification.type;

        const fromDataPromise = admin.database().ref("/Users/" + from_user_id).once('value');
        const toDataPromise = admin.database().ref("/Users/"+ user_id).once('value');

        return Promise.all([fromDataPromise, toDataPromise]).then(result => {

            const from_name = result[0].val().name;
            const to_name = result[1].val().name;
            const token_id = result[1].val().token_id;

            const payload = {
                notification: {
                    title: notification_title,
                    body: from_message + from_name,
                    icon: "default",
                    click_action: "com.andresdlg.groupmeapp.firebasepushnotifications.TARGETNOTIFICATION",
                    type: notification.type
                },
                data:{
                    message: from_message,
                    from_user_id: from_user_id,
                    type: notification.type
                }
            };

            return admin.messaging().sendToDevice(token_id, payload).then(result => {
                console.log("Notification Sent");
            });

        });
    });
});


exports.sendMessageNotification = functions.database.ref("/Conversations/{conversation_id}/messages/{message_id}").onWrite(event => {

    const conversation_id = event.params.conversation_id;
    const message_id = event.params.message_id;

    var notificationPromise = admin.database().ref("/Conversations/"+ conversation_id + "/messages/" + message_id).once('value');

    return notificationPromise.then(results => {

        var notification = results.val();

        const from_user_id = notification.idSender;
        const to_user_id = notification.idReceiver;
        const from_message = notification.text;
        const notification_title = "Nuevo mensaje de ";

        const fromDataPromise = admin.database().ref("/Users/" + from_user_id).once('value');
        const toDataPromise = admin.database().ref("/Users/"+ to_user_id).once('value');

        return Promise.all([fromDataPromise, toDataPromise]).then(result => {

            const from_name = result[0].val().name;
            const to_name = result[1].val().name;
            const token_id = result[1].val().token_id;

            const payload = {
                /*notification: {
                    title: notification_title + from_name,
                    body: from_message,
                    icon: "default",
                    click_action: "com.andresdlg.groupmeapp.firebasepushnotifications.TARGETNOTIFICATION",
                    sound:"default"
                },*/
                data:{
                    title: notification_title + from_name,
                    body: from_message,
                    icon: "default",
                    click_action: "com.andresdlg.groupmeapp.firebasepushnotifications.TARGETNOTIFICATION",
                    sound:"default",
                    type: "MESSAGE"
                }
            };

            return admin.messaging().sendToDevice(token_id, payload).then(result => {
                console.log("Notification Sent");
            });

        });
    });
});
