/*  File Version: 3.0
 *	Copyright, David Book, buzztouch.com
 *
 *	All rights reserved.
 *
 *	Redistribution and use in source and binary forms, with or without modification, are 
 *	permitted provided that the following conditions are met:
 *
 *	Redistributions of source code must retain the above copyright notice which includes the
 *	name(s) of the copyright holders. It must also retain this list of conditions and the 
 *	following disclaimer. 
 *
 *	Redistributions in binary form must reproduce the above copyright notice, this list 
 *	of conditions and the following disclaimer in the documentation and/or other materials 
 *	provided with the distribution. 
 *
 *	Neither the name of David Book, or buzztouch.com nor the names of its contributors 
 *	may be used to endorse or promote products derived from this software without specific 
 *	prior written permission.
 *
 *	THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *	ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *	WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. 
 *	IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, 
 *	INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT 
 *	NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR 
 *	PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, 
 *	WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 *	ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY 
 *	OF SUCH DAMAGE. 
 */
package com.cpmaandroid;
 
import static com.cpmaandroid.BT_gcmConfig.SENDER_ID;
import static com.cpmaandroid.BT_gcmConfig.displayMessage;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;

import com.google.android.gcm.GCMBaseIntentService;

public class GCMIntentService extends GCMBaseIntentService {

	//this class handles Google Cloud Messaging Services...
	static final String serviceName = "BT_gcmIntentService";
	static final String googleAPIProjectId = BT_gcmConfig.SENDER_ID;
		
	//constructor needs Google Project Id for this GCM Project (comes from GCM website, API key screen)...
	public GCMIntentService(){
        super(SENDER_ID);
		BT_debugger.showIt(serviceName + ":CONSTRUCTOR Google GCM Project ID: " + SENDER_ID);
    }
	
	//onRegistered...
    @Override
    protected void onRegistered(Context context, String registrationId) {
		BT_debugger.showIt(serviceName + ":onRegistered Registration ID: " + registrationId);	
		displayMessage(context, "Device registered for Push Notifications");
    	cpmaandroid_appDelegate.rootApp.setPushRegistrationId(registrationId);
    	BT_gcmServerUtils.gcmRegisterOnServer(context, registrationId);    	
    }
 
    //onUnRegistered...
    @Override
    protected void onUnregistered(Context context, String registrationId) {
		BT_debugger.showIt(serviceName + ":onUnregistered Registration ID: " + registrationId);	
		displayMessage(context, "Device un-registered for Push Notifications");
		cpmaandroid_appDelegate.rootApp.setPushRegistrationId("");
    	BT_gcmServerUtils.gcmUnregisterOnServer(context, registrationId);    	
    }
 
    //onMessage...
    @Override
    protected void onMessage(Context context, Intent intent) {
        
		//get the message...
		String message = intent.getExtras().getString("message");
		
		//log...
		BT_debugger.showIt(serviceName + ":onMessage notification: " + message);	

		//display notification on current screen of app...
		displayMessage(context, message);
		
		//notify user in built in Android notificstion bar...
	    generateNotification(context, message);		

    }
 
    //onDeletedMessages...
    @Override
    protected void onDeletedMessages(Context context, int total) {
		BT_debugger.showIt(serviceName + ":onDeletedMessages notification Deleted Count: " + total);	

		//Called when the GCM server tells pending messages have been deleted because the device was idle.
    
    }
 
    //onError...
    @Override
    public void onError(Context context, String errorId) {
		BT_debugger.showIt(serviceName + ":onError notification: " + errorId);	
		displayMessage(context, "There was a problem communicating with the Google Cloud Messaging system. Is the device online? Is the device logged into a Google Account?");
    }
 
    //onRecoverableError...
    @Override
    protected boolean onRecoverableError(Context context, String errorId) {
		BT_debugger.showIt(serviceName + ":onRecoverableError notification: " + errorId);	
		displayMessage(context, "There was a problem communicating with the Google Cloud Messaging system. Is the device online? Is the device logged into a Google Account?");
		return super.onRecoverableError(context, errorId);
    }
 
    //generates actual notification...
    private static void generateNotification(Context context, String message) {
		BT_debugger.showIt(serviceName + ":generateNotification Message: " + message);	
    	int icon = R.drawable.icon;
        long tmpTime = System.currentTimeMillis();
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        Notification notification = new Notification(icon, message, tmpTime);

        //title of message...
        String title = context.getString(R.string.app_name);
 
        //messages show in the built in notification center...
        Intent notificationIntent = new Intent(context, BT_activity_root.class);
        
        //flag so intent does not start a new activity...
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |  Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent intent =  PendingIntent.getActivity(context, 0, notificationIntent, 0);
        notification.setLatestEventInfo(context, title, message, intent);
        notification.flags |= Notification.FLAG_AUTO_CANCEL;
 
        //play sound for notification...
        notification.defaults |= Notification.DEFAULT_SOUND;
 
        //vibrate if enabled...
        notification.defaults |= Notification.DEFAULT_VIBRATE;
        notificationManager.notify(0, notification);      
        
    }	
	
	
}













