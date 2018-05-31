'use-strict'

const functions = require('firebase-functions');
const admin = require('firebase-admin');

admin.initializeApp();

exports.sendNotification = functions.database.ref("/Users/{user_id}/notifications/{notification_id}").onWrite((change, context) => {

    const user_id = context.params.user_id;
    const notification_id = context.params.notification_id;

    var notificationPromise = admin.database().ref("/Users/"+ user_id + "/notifications/" + notification_id).once('value');

    return notificationPromise.then(results => {

        var notification = results.val();

        const from_user_id = notification.from;
        const from_message = notification.message;
        const notification_title = notification.title;
        const notification_type = notification.type;

        admin.database().ref("/Users/"+ user_id).once('value').then(function(result) {

            const to_name = result.val().name;
            const token_id = result.val().token_id;

            const payload = {
                data:{
                    title: notification_title,
                    message: from_message,
                    from_user_id: from_user_id,
                    icon: "default",
                    type: notification_type,
                    click_action: "com.andresdlg.groupmeapp.firebasepushnotifications.TARGETNOTIFICATION",
                }
            };

            return admin.messaging().sendToDevice(token_id, payload).then(result => {
                console.log("Notification Sent");
            });
        });
    });
});
/*
exports.sendMessageWithNotification = functions.database.ref("/Users/{user_id}/conversation/{conversation_id}/messages/{message_id}").onWrite((change, context) => {

    const user_id = context.params.user_id;
    const conversation_id = context.params.conversation_id;
    const message_id = context.params.message_id;

    console.log(user_id);

    var notificationPromise = admin.database().ref("/Users/" + user_id + "/conversation/" + conversation_id + "/messages/" + message_id).once('value');

    return notificationPromise.then(results => {

        var notification = results.val();

        const from_user_id = notification.idSender;
        const to_user_id = notification.idReceiver;
        const from_message = notification.text;

        //PARA GRUPOS
        if(to_user_id === undefined || to_user_id === null){

            const notification_title = "Nuevo mensaje en ";

            var groupPromise = admin.database().ref("/Groups/"+ conversation_id).once('value').then(groupSnapshot => {

                var groupName = groupSnapshot.val().name;
                
                var groupMembersPromise = admin.database().ref("/Groups/"+ conversation_id + "/members/").once('value').then(result => {

                    result.forEach(element => {
                        var user_id = element.key;
    
                        if(user_id !== from_user_id){
                            var userPromise = admin.database().ref("/Users/"+ user_id).once('value').then(function(res) {
    
                                const token_id = res.val().token_id;
    
                                const payload = {
                                    data:{
                                        title: notification_title + groupName,
                                        message: from_message,
                                        icon: "default",
                                        click_action: "com.andresdlg.groupmeapp.firebasepushnotifications.TARGETNOTIFICATION",
                                        type: "MESSAGE",
                                        from_user_id: from_user_id,
                                    }
                                };
                
                                return admin.messaging().sendToDevice(token_id, payload).then(result => {
                                    console.log("Notification Sent");
                                });
                            });
                        }
                    });                
                });
            })

        // PARA MENSAJES INDIVIDUALES
        }else{
            const notification_title = "Nuevo mensaje de ";

            const fromDataPromise = admin.database().ref("/Users/" + from_user_id).once('value');
            const toDataPromise = admin.database().ref("/Users/"+ to_user_id).once('value');

            return Promise.all([fromDataPromise, toDataPromise]).then(result => {

                const from_name = result[0].val().name;
                const token_id = result[1].val().token_id;

                const payload = {
                    data:{
                        title: notification_title + from_name,
                        message: from_message,
                        icon: "default",
                        click_action: "com.andresdlg.groupmeapp.firebasepushnotifications.TARGETNOTIFICATION",
                        type: "MESSAGE",
                        from_user_id: from_user_id,
                    }
                };

                return admin.messaging().sendToDevice(token_id, payload).then(result => {
                    console.log("Notification Sent");
                });
            });
        }
    });
});*/

exports.sendMessageWithNotification = functions.database.ref("/Users/{user_id}/conversation/{conversation_id}/messages/{message_id}").onWrite((change, context) => {

    const user_id = context.params.user_id;
    const conversation_id = context.params.conversation_id;
    const message_id = context.params.message_id;

    console.log(user_id);

    var notificationPromise = admin.database().ref("/Users/" + user_id + "/conversation/" + conversation_id + "/messages/" + message_id).once('value');

    return notificationPromise.then(results => {

        var notification = results.val();

        const from_user_id = notification.idSender;
        const to_user_id = notification.idReceiver;
        const from_message = notification.text;
        const notification_title = "Nuevo mensaje de ";
        const time_message = notification.timestamp;

        const fromDataPromise = admin.database().ref("/Users/" + from_user_id).once('value');
        const toDataPromise = admin.database().ref("/Users/"+ to_user_id).once('value');

        return Promise.all([fromDataPromise, toDataPromise]).then(result => {

            const from_name = result[0].val().name;
            const token_id = result[1].val().token_id;

            const payload = {
                data:{
                    title: notification_title + from_name,
                    message: from_message,
                    icon: "default",
                    click_action: "com.andresdlg.groupmeapp.firebasepushnotifications.TARGETNOTIFICATION",
                    type: "MESSAGE",
                    from_user_id: from_user_id,
                    userName: from_name,
                    timeMessage: time_message + '',
                }
            };

            return admin.messaging().sendToDevice(token_id, payload).then(result => {
                console.log("Notification Sent");
            });
        });
        
    });
});

exports.sendMessageWithNotificationForGroups = functions.database.ref("/Conversations/{conversation_id}/messages/{message_id}/id").onWrite((change, context) => {

    const conversation_id = context.params.conversation_id;
    const message_id = context.params.message_id;

    var notificationPromise = admin.database().ref("/Conversations/" + conversation_id + "/messages/" + message_id).once('value');

    return notificationPromise.then(results => {

        var notification = results.val();

        const from_user_id = notification.idSender;
        const to_user_id = notification.idReceiver;
        const from_message = notification.text;
        const time_message = notification.timestamp;

        var iPromise = admin.database().ref("/Users/"+ from_user_id).once('value');
        
        return iPromise.then(userSnapshot => {

            var userName = userSnapshot.val().name;

            const notification_title = "Nuevo mensaje en ";

            var groupPromise = admin.database().ref("/Groups/"+ conversation_id).once('value');

            return groupPromise.then(groupSnapshot => {
                
                var groupName = groupSnapshot.val().name;

                var groupKey = groupSnapshot.val().groupKey;
                
                var groupMembersPromise = admin.database().ref("/Groups/"+ conversation_id + "/members/").once('value');

                return groupMembersPromise.then(result => {

                    const members_quantity = result.numChildren();

                    var tokens_to_send = []

                    result.forEach(element => {

                        var user_id = element.key;
    
                        if(user_id !== from_user_id){

                            var userPromise = admin.database().ref("/Users/"+ user_id).once('value');

                            userPromise.then(function(res) {
    
                                const token_id = res.val().token_id;

                                //Check if token_id is not empty
                                if(token_id){
                                    tokens_to_send.push(token_id);
                                }
                                
                                if(tokens_to_send.length == members_quantity-1){

                                    const payload = {
                                        data:{
                                            title: notification_title + groupName,
                                            message: from_message,
                                            timeMessage: time_message + '',
                                            icon: "default",
                                            click_action: "com.andresdlg.groupmeapp.firebasepushnotifications.TARGETNOTIFICATION",
                                            type: "MESSAGE",
                                            from_user_id: from_user_id,
                                            userName: userName,
                                            groupKey: groupKey,
                                            groupName: groupName
                                        }
                                    };
                    
                                    return admin.messaging().sendToDevice(tokens_to_send, payload).then(result => {
                                        console.log(groupName + ": Group Message sent to " + user_id);
                                    });
                                }
                            });
                        } 
                    });                
                });
            });
        })
    });
});