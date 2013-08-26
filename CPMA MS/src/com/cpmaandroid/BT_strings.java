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
import java.io.File;
import org.json.JSONException;
import org.json.JSONObject;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;

public class BT_strings {

	private static String objectName = "BT_strings";
	
	//removes file extension from a file name
	public static String removeExtension(String s) {

	    String separator = System.getProperty("file.separator");
	    String filename;

	    // Remove the path upto the filename.
	    int lastSeparatorIndex = s.lastIndexOf(separator);
	    if (lastSeparatorIndex == -1) {
	        filename = s;
	    } else {
	        filename = s.substring(lastSeparatorIndex + 1);
	    }

	    // Remove the extension.
	    int extensionIndex = filename.lastIndexOf(".");
	    if (extensionIndex == -1)
	        return filename;

	    return filename.substring(0, extensionIndex);
	}	
	
	//get property value from JSON string...
	public static String getJsonPropertyValue(JSONObject theJSONObject, String nameOfProperty, String defaultValue){
		//BT_debugger.showIt(objectName + ":getJsonPropertyValue from " + theJSONString);			
		//if the property exists in the JSON, return it's value...
    	String r = defaultValue;
		try{
			if(theJSONObject != null){
				if(theJSONObject.has(nameOfProperty)){
					r = theJSONObject.getString(nameOfProperty);
				}
			}
	    }catch (JSONException je){
			BT_debugger.showIt(objectName + ":getJsonPropertyValue ERROR: " + je.getMessage());			
        }			
		return r;
	}
	
	//returns a style value for a screen setting from theme or screen data...
	public static String getStyleValueForScreen(BT_item theParentScreenData, String nameOfProperty, String defaultValue){
		//BT_debugger.showIt(objectName + ":getStyleValueForScreen \"" + theParentScreenData.getItemNickname() + "\"");			
		
		//returning this..
    	String ret = defaultValue;
		
		//global theme is used if screen does not have a setting
		BT_item theThemeData = cpmaandroid_appDelegate.rootApp.getRootTheme();
		
		try{
			
			//check screen first..
			if(theParentScreenData.getJsonObject().has(nameOfProperty)){
				ret = theParentScreenData.getJsonObject().getString(nameOfProperty);
			}
			
			//if we don't have the property...check the theme...
			if(ret.length() < 1){
				if(theThemeData.getJsonObject().has(nameOfProperty)){
					ret = theThemeData.getJsonObject().getString(nameOfProperty);
				}		
			}

		}catch(Exception e){
			
		}
		
		//if we still don't have it, return the default...
		if(ret.length() < 1){
			ret = defaultValue;
		}		
		
		return ret;
	}

	
	
	//gets a "saveAsFileName" from a URL
	public static String getSaveAsFileNameFromURL(String theURL){
		String ret = "";
			
		//if we have a 'saveAsFileName' in the querystring, return that..else..return file name
		Uri uri = Uri.parse(theURL);
		try{
			ret = uri.getQueryParameter("saveAsFileName");
		}catch(Exception n){
			ret = "";			
		}
		
		//getQueryParameter may return null..
		if(ret == null){
			ret = "";
		}
		
		//if ret still empty...
		if(ret.length() < 1){
			File f = new File("" + uri);
			ret = f.getName();
		}
			
		BT_debugger.showIt(objectName + ":getSaveAsFileNameFromURL from: \"" + theURL + "\" file name: \"" + ret + "\"");			
		return ret;
	}
	
	//gets a value of a preference saved on the device...
	public static String getPrefString(String nameOfPreference){
		//BT_debugger.showIt(objectName + ":getPrefString getting value of \"" + nameOfPreference + "\" from the devices settings");			
		String ret = "";
		try{
			if(nameOfPreference.length() > 1){
				ret = "get the value here...";
				
				SharedPreferences BT_prefs = cpmaandroid_appDelegate.getApplication().getSharedPreferences("BT_prefs", Context.MODE_PRIVATE);
		        ret = BT_prefs.getString(nameOfPreference, "");
			    
				//BT_debugger.showIt(objectName + ":getPrefString value is: \"" + ret + "\"");			
		        
			}
		}catch(Exception e){
			BT_debugger.showIt(objectName + "getPrefString EXCEPTION " + e.toString());
		}
		return ret;
	}
	
	//sets a value of a preference to the device...
	public static void setPrefString(String nameOfPreference, String valueOfPreference){
		//BT_debugger.showIt(objectName + ":setPrefString setting \"" + nameOfPreference + "\" to \"" + valueOfPreference + "\" in the devices settings");			
		try{
    		
			SharedPreferences BT_prefs = cpmaandroid_appDelegate.getApplication().getSharedPreferences("BT_prefs", Context.MODE_PRIVATE);
	        SharedPreferences.Editor prefsEditor = BT_prefs.edit();
	        prefsEditor.putString(nameOfPreference, valueOfPreference);
	        prefsEditor.commit();				

		}catch(Exception e){
			//BT_debugger.showIt(objectName + "setPrefString EXCEPTION " + e.toString());
		}		
		
	}

	//gets a value of a preference saved on the device...
	public static Integer getPrefInteger(String nameOfPreference){
		//BT_debugger.showIt(objectName + ":getPrefInteger getting value of \"" + nameOfPreference + "\" from the devices settings");			
		int ret = -1;
		try{
			if(nameOfPreference.length() > 1){
				SharedPreferences BT_prefs = cpmaandroid_appDelegate.getApplication().getSharedPreferences("BT_prefs", Context.MODE_PRIVATE);
		        ret = BT_prefs.getInt(nameOfPreference, -1);
			}
		}catch(Exception e){
			BT_debugger.showIt(objectName + "getPrefInteger EXCEPTION " + e.toString());
		}
		return ret;
	}
	
	//sets a value of a preference to the device...
	public static void setPrefInteger(String nameOfPreference, Integer valueOfPreference){
		//BT_debugger.showIt(objectName + ":setPrefInteger setting \"" + nameOfPreference + "\" to \"" + valueOfPreference + "\" in the devices settings");			
		try{
    		
			SharedPreferences BT_prefs = cpmaandroid_appDelegate.getApplication().getSharedPreferences("BT_prefs", Context.MODE_PRIVATE);
	        SharedPreferences.Editor prefsEditor = BT_prefs.edit();
	        prefsEditor.putInt(nameOfPreference, valueOfPreference);
	        prefsEditor.commit();				

		}catch(Exception e){
			//BT_debugger.showIt(objectName + "setPrefInteger EXCEPTION " + e.toString());
		}		
		
	}	
	
	
	//delete local prefs
	public static void deleteAllLocalPrefs(){
		BT_debugger.showIt(objectName + ":deleteAllLocalPrefs deleting all preferences saved in the devices settings");			
    	try{
  
			SharedPreferences BT_prefs = cpmaandroid_appDelegate.getApplication().getSharedPreferences("BT_prefs", Context.MODE_PRIVATE);
	        SharedPreferences.Editor prefsEditor = BT_prefs.edit();
	        prefsEditor.clear();
	        prefsEditor.commit();				
    		
    	}catch (Exception je){
    		BT_debugger.showIt(objectName + ":deleteAllLocalPrefs Exception: " + je.toString());			
        }
		
	}	

	//merges variables in string (usually a URL)
	public static String mergeBTVariablesInString(String theString){
		//BT_debugger.showIt(objectName + ":mergeBTVariablesInString 1: " + theString);			
	
		//application
		if(cpmaandroid_appDelegate.rootApp != null){
			
			theString = theString.replace("[buzztouchAppId]", cpmaandroid_appDelegate.rootApp.getBuzztouchAppId());
			theString = theString.replace("[buzztouchAPIKey]", cpmaandroid_appDelegate.rootApp.getBuzztouchAPIKey());

			//screen
			if(cpmaandroid_appDelegate.rootApp.getCurrentScreenData() != null){
				theString = theString.replace("[screenId]", cpmaandroid_appDelegate.rootApp.getCurrentScreenData().getItemId());
			}
		
			//user
			if(cpmaandroid_appDelegate.rootApp.getRootUser() != null){
				theString = theString.replace("[userId]", cpmaandroid_appDelegate.rootApp.getRootUser().getUserId());
				theString = theString.replace("[userEmail]", cpmaandroid_appDelegate.rootApp.getRootUser().getUserEmail());
				theString = theString.replace("[userLogInId]", cpmaandroid_appDelegate.rootApp.getRootUser().getUserLogInId());
				theString = theString.replace("[userLogInPassword]", cpmaandroid_appDelegate.rootApp.getRootUser().getUserLogInPassword());
			}	
			
			//device
			if(cpmaandroid_appDelegate.rootApp.getRootDevice() != null){
				theString = theString.replace("[deviceId]", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceId());
				theString = theString.replace("[deviceLatitude]", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceLatitude());
				theString = theString.replace("[deviceLongitude]", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceLongitude());
				theString = theString.replace("[deviceModel]", cpmaandroid_appDelegate.rootApp.getRootDevice().getDeviceModel());
			}
			
		}//rootApp
		
		//return the merged string..
		return theString;		
	
	}
	
	//formats bytes to human readable string
	public static String formatBytes(long bytes, boolean si){
		BT_debugger.showIt(objectName + ":formatBytes Formatting: " + bytes);			
	    int unit = si ? 1000 : 1024;
	    if (bytes < unit) return bytes + " B";
	    int exp = (int) (Math.log(bytes) / Math.log(unit));
	    String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
	    return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
	}
	
}


















