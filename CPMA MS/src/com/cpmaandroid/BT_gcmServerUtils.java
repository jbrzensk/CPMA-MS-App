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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import android.content.Context;
import android.net.Uri;

import com.google.android.gcm.GCMRegistrar;

public class BT_gcmServerUtils{

	//gcmRegisterOnServer...
    public static void gcmRegisterOnServer(final Context context, final String regId) {
		BT_debugger.showIt("BT_gcmServerUtils:gcmRegisterOnServer");

		//build URL...
		String gcmURL = cpmaandroid_appDelegate.rootApp.getRegisterForPushURL();
		gcmURL = BT_strings.mergeBTVariablesInString(gcmURL);
		
		//pull apiKey and apiSecret from URL params...
		Uri uri= Uri.parse(gcmURL);
		String useApiKey = uri.getQueryParameter("apiKey");
		String useApiSecret = uri.getQueryParameter("apiSecret");
		
		//build params for post...
		Map<String, String> params = new HashMap<String, String>();
        params.put("command", "registerForPush");
        params.put("appGuid", cpmaandroid_appDelegate.rootApp.getBuzztouchAppId());
        params.put("apiKey", useApiKey);
        params.put("apiSecret", useApiSecret);
        params.put("deviceId", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceId());
        params.put("deviceLatitude", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceLatitude());
        params.put("deviceLongitude", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceLongitude());
        params.put("deviceModel", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceModel());
        params.put("userId", cpmaandroid_appDelegate.rootApp.getRootUser().getUserId());
        params.put("deviceType", "android");
        params.put("deviceToken", regId);
        params.put("currentMode", cpmaandroid_appDelegate.rootApp.getCurrentMode());
		
      	//send request...
        gcmServerPOST(gcmURL, "registerDevice", params);
        
    }
    
    //gcmUnregisterOnServer...
    public static void gcmUnregisterOnServer(final Context context, final String regId){
		BT_debugger.showIt("BT_gcmServerUtils:gcmUnregisterOnServer");

		//build URL...
		String gcmURL = cpmaandroid_appDelegate.rootApp.getRegisterForPushURL();
		gcmURL = BT_strings.mergeBTVariablesInString(gcmURL);
		
		//pull apiKey and apiSecret from URL params...
		Uri uri= Uri.parse(gcmURL);
		String useApiKey = uri.getQueryParameter("apiKey");
		String useApiSecret = uri.getQueryParameter("apiSecret");
		
		//if not null...
		if(useApiKey != null && useApiSecret != null){
			
			//build post params...
	        Map<String, String> params = new HashMap<String, String>();
	        params.put("command", "registerForPush");
	        params.put("appGuid", cpmaandroid_appDelegate.rootApp.getBuzztouchAppId());
	        params.put("apiKey", useApiKey);
	        params.put("apiSecret", useApiSecret);
	        params.put("deviceId", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceId());
	        params.put("deviceLatitude", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceLatitude());
	        params.put("deviceLongitude", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceLongitude());
	        params.put("deviceModel", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceModel());
	        params.put("userId", cpmaandroid_appDelegate.rootApp.getRootUser().getUserId());
	        params.put("deviceType", "android");
	        params.put("gcmCommand", "unregisterDevice");
	        params.put("deviceToken", regId);
        	params.put("currentMode", cpmaandroid_appDelegate.rootApp.getCurrentMode());

			//make request...
			gcmServerPOST(gcmURL, "unregisterDevice", params);
		
		}else{
			BT_debugger.showIt("BT_gcmServerUtils:gcmServerPOST URL is NULL?");
		}
     }

    
	//send a push related HTTP request...
	public static void gcmServerPOST(String serverURL, String command, Map<String, String> params){
		BT_debugger.showIt("BT_gcmServerUtils:gcmServerPOST URL: " + serverURL);
        
		//holds results from server...
		String results = "";

		//build body to post with request...
        try{
        	
        	//create an HttpClient and HttpPost object...
            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(serverURL);

            //create name/value pairs from params...
            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);
			Iterator<Entry<String, String>> iterator = params.entrySet().iterator();
            while (iterator.hasNext()){
                Entry<String, String> param = iterator.next();
                nameValuePairs.add(new BasicNameValuePair(param.getKey(), param.getValue()));
            }
            
            //set type of entities...
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            //execute post...
            HttpResponse response = httpclient.execute(httppost);
            
            //get input stream from results...
            HttpEntity entity = response.getEntity();
            InputStream is = entity.getContent();	  
            
            //convert input stream to string...
            StringBuilder data = convertStreamToText(is);	 
            results = data.toString();
            BT_debugger.showIt("BT_gcmServerUtils:gcmServerPOST Response:: " + results);
                
                
        }catch (Exception e) {
	       	BT_debugger.showIt("BT_gcmServerUtils:gcmServerPOST EXCEPTION with String Builder: " + e.toString());
        }
    	 
       //parse JSON data...
       try{
       		if(results.toString().length() > 1){
			
	       		//raw JSON results (entire string returned by server)...
	            JSONObject objRaw = new JSONObject(results.toString());
	            if(objRaw.has("result")){
	            	
	            	//get "failed" or "valid" from JSON...
	            	JSONObject result = objRaw.getJSONObject("result");
	    			if(result.has("status")){
	    				if(result.getString("status").equalsIgnoreCase("valid")){
	    					
	    		            //if registering...
	    		            if(command.equals("registerDevice")){
	    		               	
	    		              	//set registered on GCM...
	    		                GCMRegistrar.setRegisteredOnServer(cpmaandroid_appDelegate.getContext(), true);
	    		                
	    		            }
	    		            
	    		            //if unregistering...
	    		            if(command.equals("unregisterDevice")){
	    		                
	    		            	//set NOT registered on GCM...
	    		            	GCMRegistrar.setRegisteredOnServer(cpmaandroid_appDelegate.getContext(), false);
	 
	    		            }
	    					
	    					
	    				}else{
			            	GCMRegistrar.setRegisteredOnServer(cpmaandroid_appDelegate.getContext(), false);
	    					BT_debugger.showIt("BT_gcmServerUtils:gcmServerPOST JSON results returned \"failed\" status");
	    				}//valid
	    			}//status
	    			
	            }else{
	            	GCMRegistrar.setRegisteredOnServer(cpmaandroid_appDelegate.getContext(), false);
	            }//objRaw
       		}else{
            	BT_debugger.showIt("BT_gcmServerUtils:gcmServerPOST The server did not return a result?");
       		}//results are empty...                  
        }catch (Exception e) {
        	BT_debugger.showIt("BT_gcmServerUtils:gcmServerPOST EXCEPTION parsing JSON returned from server: " + e.toString());
        }
     	

    	
    }
	
	//returns string from input stream...
	public static StringBuilder convertStreamToText(InputStream is) {
	    String line = "";
	    StringBuilder total = new StringBuilder();
	    
	    //wrap a BufferedReader around the input stream...
	    BufferedReader rd = new BufferedReader(new InputStreamReader(is));

	    //read response until the end...
	    try{
			while ((line = rd.readLine()) != null) { 
			    total.append(line); 
			}
		}catch(IOException e){
		}
	    
	    //return full string
	    return total;
	}

	
	
}





