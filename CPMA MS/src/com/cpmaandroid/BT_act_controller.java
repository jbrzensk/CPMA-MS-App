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

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

public class BT_act_controller {

	private static String objectName = "BT_act_controller";

	//handles right-side navigation bar buttons...
	public static void handleRightNavButton(final Activity parentActivity, final BT_item parentActivityScreenData, final BT_item theScreenData){
		BT_debugger.showIt(objectName + ":handleRightNavButton");
		
    	//itemId, nickname or object...
        String loadScreenItemId = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "navBarRightButtonTapLoadScreenItemId", "");
        String loadScreenNickname = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "navBarRightButtonTapLoadScreenNickname", "");
        BT_item tapScreenLoadObject = null;
        BT_item tapMenuItemObject = null;
    	if(loadScreenItemId.length() > 1 && !loadScreenItemId.equalsIgnoreCase("none")){
			BT_debugger.showIt(objectName + ":handleRightNavButton right button loads screen with itemId: \"" + loadScreenItemId + "\"");
    		tapScreenLoadObject = cpmaandroid_appDelegate.rootApp.getScreenDataByItemId(loadScreenItemId);
    	}else{
    		if(loadScreenNickname.length() > 1){
				BT_debugger.showIt(objectName + ":handleRightNavButton right button loads screen with nickname: \"" + loadScreenNickname + "\"");
    			tapScreenLoadObject = cpmaandroid_appDelegate.rootApp.getScreenDataByItemNickname(loadScreenNickname);
    		}else{
    			try{
	    			JSONObject obj = theScreenData.getJsonObject();
		            if(obj.has("navBarRightButtonTapLoadScreenObject")){
						BT_debugger.showIt(objectName + ":handleRightNavButton right button loads screen object configured with JSON object.");
		            	JSONObject tmpRightScreen = obj.getJSONObject("navBarRightButtonTapLoadScreenObject");
		            	tapScreenLoadObject = new BT_item();
    		            if(tmpRightScreen.has("itemId")) tapScreenLoadObject.setItemId(tmpRightScreen.getString("itemId"));
    		            if(tmpRightScreen.has("itemNickname")) tapScreenLoadObject.setItemNickname(tmpRightScreen.getString("itemNickname"));
    		            if(tmpRightScreen.has("itemType")) tapScreenLoadObject.setItemType(tmpRightScreen.getString("itemType"));
    		            tapScreenLoadObject.setJsonObject(tmpRightScreen);
		            }
    			}catch(Exception e){
					BT_debugger.showIt(objectName + ":handleRightNavButton EXCEPTION reading screen-object configured for right-side nav button: " + e.toString());
    			}
    		}
    	}

    	//if we have a screen object to load from the right-button tap, build a BT_itme object...
    	if(tapScreenLoadObject != null){
    		
    		tapMenuItemObject = new BT_item();
    		tapMenuItemObject.setItemId("unused");
    		tapMenuItemObject.setItemNickname("unused");
    		tapMenuItemObject.setItemType("BT_menuItem");
    		
    		//create json object for the BT_item...
    		try{
	    		JSONObject tmpMenuJson = new JSONObject();
	    		tmpMenuJson.put("itemId", "unused");
	    		tmpMenuJson.put("itemNickname", "unused");
	    		tmpMenuJson.put("itemType", "BT_menuItem");
	    		
   	    		//possible transition type
	    		String navBarRightButtonTapTransitionType = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "navBarRightButtonTapTransitionType", "");
	    		if(navBarRightButtonTapTransitionType.length() > 1){
	    			tmpMenuJson.put("transitionType", navBarRightButtonTapTransitionType);
	    		}
    		
	    		//set JSON
	    		tapMenuItemObject.setJsonObject(tmpMenuJson);
	    		
    		}catch(Exception e){
				BT_debugger.showIt(objectName + ":handleRightNavButton EXCEPTION creating right-button BT_menuItem: " + e.toString());
    		}
    		
        	//call loadScreenObject (static method in this class)...
   			BT_act_controller.loadScreenObject(parentActivity, parentActivityScreenData, tapMenuItemObject, tapScreenLoadObject);
   		
    	}else{
			BT_debugger.showIt(objectName + ":handleRightNavButton ERROR. No screen is connected to this button?");	
    		BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.errorTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.errorNoScreenConnected));
    	}
		
	} //handleRightNavButton
	
	//loads a screen object...
	public static void loadScreenObject(Activity parentActivity, BT_item parentScreenData, BT_item theMenuItemData, BT_item theScreenData){
		BT_debugger.showIt(objectName + ":loadScreenObject for screen with itemId: \"" + theScreenData.getItemId() + "\" and itemNickname: \"" + theScreenData.getItemNickname() + "\" and itemType: \"" + theScreenData.getItemType() + "\"");
		
		//if the loadScreenItemId == "none".....
		if(theMenuItemData != null){
			if(BT_strings.getJsonPropertyValue(theMenuItemData.getJsonObject(), "loadScreenWithItemId", "").equalsIgnoreCase("none")){
				return;
			}
		}
		
		//if this screen data has loadScreenItemId == "none"...
		if(theScreenData != null){
			if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "loadScreenWithItemId", "").equalsIgnoreCase("none")){
				return;
			}
		}
		
        //next screen may require a login..
        boolean allowNextScreen = true;
        if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "loginRequired", "").equalsIgnoreCase("1")){
        	if(!cpmaandroid_appDelegate.rootApp.getRootUser().getIsLoggedIn()){
        		allowNextScreen = false;
        		BT_debugger.showIt(objectName + ":loadScreenObject login required - user IS NOT logged in");
        	}else{
        		BT_debugger.showIt(objectName + ":loadScreenObject login required - user IS logged in");
        	}
        }
        
        //alert if not allowed...
        if(!allowNextScreen){
    		BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.logInRequiredTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.logInRequiredMessage));
    		//bail
    		return;
        }

        //if continuing...
        if(allowNextScreen){
        	
         	//if we passed in a menu-item..
        	if(theMenuItemData != null){
	        	
        		//possible sound effect...
	            if(BT_strings.getJsonPropertyValue(theMenuItemData.getJsonObject(), "soundEffectFileName", "").length() > 1){
	            	cpmaandroid_appDelegate.playSoundEffect(BT_strings.getJsonPropertyValue(theMenuItemData.getJsonObject(), "soundEffectFileName", ""));
	            }
	        	
	            //possible stop audio track...
	            if(BT_strings.getJsonPropertyValue(parentScreenData.getJsonObject(), "audioStopsOnScreenExit", "").equalsIgnoreCase("1")){
	            	BT_debugger.showIt(objectName + ":loadScreenObject stopping audio....");
	            }
	            
	       		//remember previous menu object before setting current menu object
	            cpmaandroid_appDelegate.rootApp.setPreviousMenuItemData(cpmaandroid_appDelegate.rootApp.getCurrentMenuItemData());
	    		            
	    		//remember current menu object
	            cpmaandroid_appDelegate.rootApp.setCurrentMenuItemData(theMenuItemData);
	         
        	}
        	
    		//remember current screen object
        	cpmaandroid_appDelegate.rootApp.setCurrentScreenData(theScreenData);
            
    		//remember previous screen object
        	cpmaandroid_appDelegate.rootApp.setPreviousScreenData(parentScreenData);
            
        	//type of screen to load next...
            String nextScreenType = theScreenData.getItemType();
            
    		//some screens aren't screens at all! Like "Call Us" and "Email Us" item. In these cases, we only
    		//trigger a method and do not load a custom activity...
            
            //call us...
            if(nextScreenType.equalsIgnoreCase("BT_screen_call") || 
            		nextScreenType.equalsIgnoreCase("BT_placeCall")){
            	BT_debugger.showIt(objectName + ":launching dialer...");
            	BT_act_controller.launchPhoneDialerWithScreenData(parentActivity, theScreenData);
            	return;
            }

            //email / share email...
            if(nextScreenType.equalsIgnoreCase("BT_screen_email") || 
            		nextScreenType.equalsIgnoreCase("BT_shareEmail") ||
            		nextScreenType.equalsIgnoreCase("BT_sendEmail")){
            	BT_debugger.showIt(objectName + ":launching email compose sheet...");
            	BT_act_controller.sendEmailWithScreenData(parentActivity, theScreenData);
            	return;
            }        
            
            //text us / share text (SMS)...
            if(nextScreenType.equalsIgnoreCase("BT_screen_sms") || 
            		nextScreenType.equalsIgnoreCase("BT_shareSms") ||
            		nextScreenType.equalsIgnoreCase("BT_sendSms") ||
            		nextScreenType.equalsIgnoreCase("BT_shareSms")){
            	BT_debugger.showIt(objectName + ":launching SMS / Text Message compose sheet...");
            	BT_act_controller.sendSMSWithScreenData(parentActivity, theScreenData);
            	return;
            }  
            
            //launch native app...
            if(nextScreenType.equalsIgnoreCase("BT_launchNativeApp")){
            	BT_debugger.showIt(objectName + ":launching native app....");
            
        		/*
                Launching native app requires an "appToLaunch" and a "dataURL"
                App Types:	browser, youTube, googleMaps, musicStore, appStore, mail, dialer, sms
                */
            	
               	//get the document file name or the URL...
            	String appToLaunch = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "appToLaunch", "");
            	String dataURL = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "dataURL", "");
            	if(appToLaunch.length() > 1 && dataURL.length() > 1){
            	
        			try{
        				
        				//browser...
        				if(appToLaunch.equalsIgnoreCase("browser")){
         			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dataURL)); 
         			    	 
        			    	  //ask user for the best app to use...
        			    	  parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
         				}
        				
        				//youTube...
        				if(appToLaunch.equalsIgnoreCase("youTube")){
         			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dataURL)); 
         			    	 
        			    	  //ask user for the best app to use...
        			    	  parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
         				}

        				//googleMaps...
        				if(appToLaunch.equalsIgnoreCase("googleMaps")){
         			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + dataURL)); 
        			    	  
         			    	  //ask user for the best app to use...
        			    	  parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
        				}
        				
           				//nativeMaps...
        				if(appToLaunch.equalsIgnoreCase("nativeMaps")){
         			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?" + dataURL)); 
        			    	  
         			    	  //ask user for the best app to use...
        			    	  parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
        				}

        				//musicStore...
        				if(appToLaunch.equalsIgnoreCase("musicStore")){
         			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dataURL)); 
         			    	 
        			    	  //ask user for the best app to use...
        			    	  parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
         				}
        				
          				//appStore...
        				if(appToLaunch.equalsIgnoreCase("appStore")){
        			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(dataURL)); 
 
        			    	  //ask user for the best app to use...
        			    	  parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
        				}
        				
         				//mail...
        				if(appToLaunch.equalsIgnoreCase("mail")){

        			    	  Intent intent = new Intent(android.content.Intent.ACTION_SEND);  
        			    	  intent.setType("plain/text");  
        			    	  
         			    	  //email to address...
       			    		  String[] emailToAddressList = {dataURL};
       			    		  intent.putExtra(android.content.Intent.EXTRA_EMAIL, emailToAddressList);  

        			    	  //ask user for the best app to use...
        			    	  parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
        				}
        				
         				//sms...
        				if(appToLaunch.equalsIgnoreCase("sms")){
        			    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW);  
        			    	  intent.setType("vnd.android-dir/mms-sms");  
        			    	  
        			    	  //set the to-number....
       			    		  intent.putExtra("address", dataURL); 

        			    	  //ask user for the best app to use...
       			    		  parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
        				}
        				
        				//dialer...
        				if(appToLaunch.equalsIgnoreCase("dialer")){
        					Intent intent = new Intent(Intent.ACTION_VIEW);
       			    		intent.setData(Uri.parse("tel:" + dataURL));
        			    	
        					//ask user for the best app to use...
        			    	parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
        				}
        				
        				//customURLScheme...
        				if(appToLaunch.equalsIgnoreCase("customURLScheme")){
        					Intent intent = new Intent(android.content.Intent.ACTION_VIEW);  
        					
        					//ask user for the best intent to use...
        					parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
        				}
        		    	  
        		    	  
        		    	  
       		    	}catch(Exception e){
                    	BT_debugger.showIt(objectName + ":EXCEPTION Launching native app: " + e.toString());
       		    	}			
            	
            	
            	}else{
                	BT_debugger.showIt(objectName + ":ERROR Launching native app. appToLaunch or dataURL empty?");
            	}
               	
            	//bail...
            	return;
            	
            }            
            
 			//if the screen we are loading has an audio track, show "not supported on Android message"...
            if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "audioFileName", "").length() > 3 || BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "audioFileURL", "").length() > 3){
            	BT_debugger.showIt(objectName + ":screen uses audio file or URL. BACKGROUND AUDIO IS UNSUPPORTED ON ANDROID");

            }
            
            //figure out possible transition type...
            String theTransitionType = "";
            if(theMenuItemData != null){
            	theTransitionType = BT_strings.getJsonPropertyValue(theMenuItemData.getJsonObject(), "transitionType", "");
            }
            
			//remember the transition type so we can go back...
            cpmaandroid_appDelegate.rootApp.getTransitionTypeHistory().add(theTransitionType);
			
			//create the new intent
			try{
				
				//create an intent using theScreenData's "class name"
                String theActivityClass = "com.cpmaandroid." + nextScreenType;
        		Intent i = new Intent(parentActivity, Class.forName(theActivityClass));
			
        		//send the JSON data for the next screen along with the intent...
        		i.putExtra("screenData", theScreenData.getJsonObject().toString());
        		
       			//send a flag if this BT_item "isHomeScreen"...
        		if(theScreenData.isHomeScreen()){
            		i.putExtra("screenDataIsHomeScreen", "1");
        		}
				
				//start the new activity from parent Activity...
				parentActivity.startActivity(i);
				
				//get animation resource identifier...
				int anim1Id = -1;
				int anim2Id = -1;
				
				//find id of each animation file in resources bundle (layout/anim directory)...
				if(theTransitionType.equalsIgnoreCase("fade")){
					anim1Id = BT_fileManager.getResourceIdFromBundle("anim", "fadein.xml");
					anim2Id = BT_fileManager.getResourceIdFromBundle("anim", "fadeout.xml");
				}
				
				/* ANIMATIONS NEED HELP!!! SEE EACH ANIMATIONS LAYOUT FILE...*/
				if(theTransitionType.equalsIgnoreCase("flip")){
					anim1Id = BT_fileManager.getResourceIdFromBundle("anim", "flipin.xml");
					anim2Id = BT_fileManager.getResourceIdFromBundle("anim", "flipout.xml");
				}
				if(theTransitionType.equalsIgnoreCase("curl")){
					anim1Id = BT_fileManager.getResourceIdFromBundle("anim", "curlin.xml");
					anim2Id = BT_fileManager.getResourceIdFromBundle("anim", "curlout.xml");
				}
				if(theTransitionType.equalsIgnoreCase("grow")){
					anim1Id = BT_fileManager.getResourceIdFromBundle("anim", "growin.xml");
					anim2Id = BT_fileManager.getResourceIdFromBundle("anim", "growout.xml");
				}
				if(theTransitionType.equalsIgnoreCase("slideUp")){
					anim1Id = BT_fileManager.getResourceIdFromBundle("anim", "fadein.xml");
					anim2Id = BT_fileManager.getResourceIdFromBundle("anim", "fadeout.xml");
				}
				if(theTransitionType.equalsIgnoreCase("slideDown")){
					anim1Id = BT_fileManager.getResourceIdFromBundle("anim", "fadein.xml");
					anim2Id = BT_fileManager.getResourceIdFromBundle("anim", "fadeout.xml");
				}
				
				
				//if we found the animation...
				if(anim1Id > -1 && anim2Id > -1){
					parentActivity.overridePendingTransition(anim1Id, anim2Id);
				}
			
			}catch(Exception e){
				BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.errorTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.errorLoadingScreen));
				BT_debugger.showIt(objectName + ": EXCEPTION transitioning to next screen. ERROR: " + e.toString());
			
			}
			
        }//allow next screen..
        
	}//end loadScreenObject...
	
	//send email with screen data...
	public static void sendEmailWithScreenData(final Activity parentActivity, final BT_item theScreenData){
		BT_debugger.showIt(objectName + ":sendEmailWithScreenData");
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().canSendEmail()){
		
			try{
			
	    	  Intent intent = new Intent(android.content.Intent.ACTION_SEND);  
	    	  intent.setType("plain/text");  
	    	  
	    	  //do we have a subject....
	    	  String subject = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "emailSubject", "");
	    	  if(subject.length() > 1){
	    		  intent.putExtra(android.content.Intent.EXTRA_SUBJECT, subject);  
	    	  }
	    	  
	    	  //do we have a message...
	    	  String emailMessage = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "emailMessage", "");
	    	  if(emailMessage.length() > 1){
	    		  intent.putExtra(android.content.Intent.EXTRA_TEXT, emailMessage);  
	    	  }	    	  
	    	  
	    	  //email to address...
	    	  String emailToAddress = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "emailToAddress", "");
	    	  BT_debugger.showIt("EMAIL TO: " + emailToAddress);
	    	  if(emailToAddress.length() > 1){
	    		  String[] emailToAddressList = {emailToAddress};
	    		  intent.putExtra(android.content.Intent.EXTRA_EMAIL, emailToAddressList);  
	    	  }	 	          

	    	  //ask user for the best app to use...
	    	  parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
			
	    	}catch(Exception e){
	    		BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.noNativeAppTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.noNativeAppDescription));
	    	}
	    	
		}else{
			BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.errorTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.noNativeAppTitle));
		}
	}
	
	//send SMS with screen data...
	public static void sendSMSWithScreenData(final Activity parentActivity, final BT_item theScreenData){
		BT_debugger.showIt(objectName + ":sendSMSWithScreenData");
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().canSendSMS()){
		
			try{
				
	    	  Intent intent = new Intent(android.content.Intent.ACTION_VIEW);  
	    	  intent.setType("vnd.android-dir/mms-sms");  
	    	  
	    	  //do we have a text-to-number....
	    	  String textToNumber = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "textToNumber", "");
	    	  if(textToNumber.length() > 1){
	    		  intent.putExtra("address", textToNumber); 
	    	  }
	    	  
	    	  //do we have a textMessage...
	    	  String textMessage = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "textMessage", "");
	    	  if(textMessage.length() > 1){
	    		  intent.putExtra("sms_body", textMessage); 
	    	  }	    	  

	    	  //ask user for the best app to use...
	    	  parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
			
	    	}catch(Exception e){
	    		BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.noNativeAppTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.noNativeAppDescription));
	    	}			
			
		}else{
			BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.errorTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.noNativeAppTitle));
		}
	}	
	
	//start phone call with screen data...
	public static void launchPhoneDialerWithScreenData(final Activity parentActivity, final BT_item theScreenData){
		BT_debugger.showIt(objectName + ":launchDialerWithScreenData");
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().canMakeCalls()){
		
	    	try{
	    		
				Intent intent = new Intent(Intent.ACTION_VIEW);
				
				//number to call...
		    	String number = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "number", "");
		    	if(number.length() > 1){
		    		intent.setData(Uri.parse("tel:" + number));
		    	}
		    	
				//ask user for the best app to use...
		    	parentActivity.startActivity(Intent.createChooser(intent, cpmaandroid_appDelegate.getApplication().getString(R.string.openWithWhatApp)));  
	    		
	    	}catch(Exception e){
	    		BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.noNativeAppTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.noNativeAppDescription));
	    	}
			
		}else{
			BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.errorTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.noNativeAppTitle));
		}
	}	
	
	
	//is intent available?
	public static boolean isIntentAvailable(Context context, String action) {
	    final PackageManager packageManager = context.getPackageManager();
	    final Intent intent = new Intent(action);
	    List<?> list = packageManager.queryIntentActivities(intent,PackageManager.MATCH_DEFAULT_ONLY);
	    if(list.size() > 0){
			BT_debugger.showIt(objectName + ":isIntentAvailable YES: " + action);			
	    }else{
			BT_debugger.showIt(objectName + ":isIntentAvailable:isIntentAvailable: NO: " + action);			
	    }
	    return list.size() > 0;
	}	
	
	//not all documents load in the built-in webView....
	public static boolean canLoadDocumentInWebView(String theFileNameOrURL){
		BT_debugger.showIt(objectName + ":canLoadDocumentInWebView \"" + theFileNameOrURL + "\"");
		boolean ret = true;
		if(theFileNameOrURL.length() > 1){
			
			//this is a list of file types that cannot load in a webView, add types as needed.
			
			ArrayList<String> doNotLoadList = new ArrayList<String>();
			doNotLoadList.add(".mp3");
			doNotLoadList.add(".zip");
			doNotLoadList.add(".doc");
			doNotLoadList.add(".pdf");
			doNotLoadList.add(".mpeg");
			doNotLoadList.add(".mp4");
			doNotLoadList.add(".xls");
			doNotLoadList.add(".mov");
			doNotLoadList.add("mailto");
			doNotLoadList.add("tel");

			//is our localFileName valid?
			for(int x = 0; x < doNotLoadList.size(); x++){
				if(theFileNameOrURL.contains(doNotLoadList.get(x))){
    				ret = false;
					break;
				}
			}
		}
		return ret;
	}	
	
	
	
	
	
}









