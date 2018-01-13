/*BACKUP

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

        const fromDataPromise = admin.database().ref("/Users/" + from_user_id).once('value');
        const toDataPromise = admin.database().ref("/Users/"+ user_id).once('value');


        return Promise.all([fromDataPromise, toDataPromise]).then(result => {

            const from_name = result[0].val().name;
            const to_name = result[1].val().name;

            console.log("FROM : " + from_name + "TO : " + to_name);

        });

    });

});*/

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
                    click_action: "com.andresdlg.groupmeapp.firebasepushnotifications.TARGETNOTIFICATION"
                },
                data:{
                    message: from_message,
                    from_user_id: from_user_id
                }
            };

            return admin.messaging().sendToDevice(token_id, payload).then(result => {

                console.log("Notification Sent");

            });

        });

    });

});
