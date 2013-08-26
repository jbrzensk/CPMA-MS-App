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

import org.json.JSONArray;
import org.json.JSONObject;

public class BT_application {

	private static String className = "BT_application";
	
	//properties...
	private ArrayList<BT_item> themes = null;
	private ArrayList<BT_item> tabs = null;
	private ArrayList<BT_item> screens = null;
	private ArrayList<String> transitionTypeHistory = null;
	private int selectedTab = -1;
	private String itemId = "";
	private String itemType = "";
	private String buzztouchAppId = "";
	private String buzztouchAPIKey = "";
	private String dataURL = "";
	private String reportToCloudURL = "";
	private String registerForPushURL = "";
	private String pushRegistrationId = "";
	private String name = "";
	private String version = "";
	private String currentMode = "";
	private String startLocationUpdates = "";
	private String promptForPushNotifications = "";
	private String allowRotation = "";
	private String jsonVars = "";

	//objects that don't change during app's lifecycle...
	private BT_item rootTheme = null;
	private BT_device rootDevice = null;
	private BT_user rootUser = null;
	
	//objects that change during navigation...
	private BT_item currentScreenData = null;
	private BT_item previousScreenData = null;
	private BT_item currentMenuItemData = null;
	private BT_item previousMenuItemData = null;
	private BT_item currentItemUpload = null;
	
	//remember the home screen for each tab...
	private BT_item tab0ScreenData = null;
	private BT_item tab1ScreenData = null;
	private BT_item tab2ScreenData = null;
	private BT_item tab3ScreenData = null;
	private BT_item tab4ScreenData = null;
	
	
	//constructor
	public BT_application(){
		
		BT_debugger.showIt(className + ": Creating root-app object.");


		//init arrays
		themes = new ArrayList<BT_item>();
		tabs = new ArrayList<BT_item>();
		screens = new ArrayList<BT_item>();
		transitionTypeHistory = new ArrayList<String>();
		selectedTab = 0;
		
		//strings
		itemId = "";
		itemType = "";
		buzztouchAppId = "";
		buzztouchAPIKey = "";
		dataURL = "";
		reportToCloudURL = "";
		registerForPushURL = "";
		name = "";
		version = "";
		currentMode = "";
		startLocationUpdates = "0";
		promptForPushNotifications = "0";
		allowRotation = "";
		jsonVars = "";

		//other objects
		rootTheme = new BT_item();
		rootDevice = new BT_device();
		rootUser = new BT_user();
		
		//null objects
		currentScreenData = null;
		previousScreenData = null;
		currentMenuItemData = null;
		previousMenuItemData = null;
		currentItemUpload = null;
		
		tab0ScreenData = null;
		tab1ScreenData = null;
		tab2ScreenData = null;
		tab3ScreenData = null;
		tab4ScreenData = null;
		
		
	}
	
	//validates application data
	public boolean validateApplicationData(String theAppData){
		BT_debugger.showIt(className + ": validateApplicationData");	
		boolean isValid = true;
    	try{
            JSONObject obj = new JSONObject(theAppData);
            if(obj.has("BT_appConfig")){
	            JSONObject rootObj =  obj.getJSONObject("BT_appConfig");
	            if(rootObj.has("BT_items")){
		            JSONArray BT_items =  rootObj.getJSONArray("BT_items");

		    		for(int i = 0; i < BT_items.length(); i++){
		            	
		            	//first item in the list...the appItem
    		            JSONObject rootBT_item = BT_items.getJSONObject(0);
			            
		            	//need themes
			            if(rootBT_item.has("BT_themes")){
			            	JSONArray BT_themes =  rootBT_item.getJSONArray("BT_themes");
			            	if(BT_themes.length() < 1){
				               	isValid = false;
				    			BT_debugger.showIt(className + ": validateApplicationData: Zero themes?");
			            	}
			            }else{
			            	isValid = false;
			            	BT_debugger.showIt(className + ": validateApplicationData: No themes array?");			
			            }
			            
		            	//need screens
			            if(rootBT_item.has("BT_screens")){
			            	JSONArray BT_screens =  rootBT_item.getJSONArray("BT_screens");
			            	if(BT_screens.length() < 1){
				               	isValid = false;
				    			BT_debugger.showIt(className + ": validateApplicationData: Zero screens?");
			            	}
			            }else{
			            	isValid = false;
			            	BT_debugger.showIt(className + ": validateApplicationData: No screens array?");			
			            }
		            
		    		}//end for
		            
	            }else{
	            	isValid = false;
	    			BT_debugger.showIt(className + ": validateApplicationData: No BT_items?");			
	            }
            }else{
            	isValid = false;
    			BT_debugger.showIt(className + ": validateApplicationData: No BT_appConfig?");			
            }
            
	    }catch (Exception e){
        	isValid = false;
			BT_debugger.showIt(className + ":validateApplicationData EXCEPTION trying to parse JSON data? ERROR: " + e.toString());			
        }	

		
	    return isValid;
	}
	
	//parse app's JSON data...
	public boolean parseAppJSONData(String configData){
		boolean ret = true;
		BT_debugger.showIt(className + ": parseJSONData");			
	    
		//init properties
		this.dataURL = "";
		themes.clear();
		tabs.clear();
		screens.clear();
		transitionTypeHistory.clear();
		
    	try{
            JSONObject obj = new JSONObject(configData);
            if(obj.has("BT_appConfig")){
	            JSONObject rootObj =  obj.getJSONObject("BT_appConfig");
	            if(rootObj.has("BT_items")){
		            JSONArray BT_items =  rootObj.getJSONArray("BT_items");
		    		for(int i = 0; i < BT_items.length(); i++){
		            	
		            	//first item in the list...the appItem
    		            JSONObject rootBT_item = BT_items.getJSONObject(0);
     		            
    		            //set the string properties for this app
		    			BT_debugger.showIt(className + ": parsing core settings...");			
    		            if(rootBT_item.has("itemId")) this.itemId = rootBT_item.getString("itemId");
    		            if(rootBT_item.has("itemType")) this.itemType = rootBT_item.getString("itemType");
    		            if(rootBT_item.has("buzztouchAppId")) this.buzztouchAppId = rootBT_item.getString("buzztouchAppId");
    		            if(rootBT_item.has("buzztouchAPIKey")) this.buzztouchAPIKey = rootBT_item.getString("buzztouchAPIKey");
    		            if(rootBT_item.has("dataURL")) this.dataURL = rootBT_item.getString("dataURL");
    		            if(rootBT_item.has("reportToCloudURL")) this.reportToCloudURL = rootBT_item.getString("reportToCloudURL");
    		            if(rootBT_item.has("registerForPushURL")) this.registerForPushURL = rootBT_item.getString("registerForPushURL");
    		            if(rootBT_item.has("name")) this.name = rootBT_item.getString("name");
    		            if(rootBT_item.has("version")) this.version = rootBT_item.getString("version");
    		            if(rootBT_item.has("currentMode")) this.currentMode = rootBT_item.getString("currentMode");
    		            if(rootBT_item.has("startLocationUpdates")) this.startLocationUpdates = rootBT_item.getString("startLocationUpdates");
    		            if(rootBT_item.has("promptForPushNotifications")) this.promptForPushNotifications = rootBT_item.getString("promptForPushNotifications");
    		            if(rootBT_item.has("allowRotation")) this.allowRotation = rootBT_item.getString("allowRotation");
     		            
		    			//theme
    		            if(rootBT_item.has("BT_themes")){
    		    			BT_debugger.showIt(className + ": parsing themes...");			
    			            JSONArray BT_themes = rootBT_item.getJSONArray("BT_themes");
    			            JSONObject firstBT_theme = BT_themes.getJSONObject(0);
        		            if(firstBT_theme.has("itemType")){
        		            	if(firstBT_theme.getString("itemType").equalsIgnoreCase("BT_theme")){
        		            		
        		            		//create a theme object
        		            		BT_item tmpTheme = new BT_item();
        		            		if(firstBT_theme.has("itemId")) tmpTheme.setItemId(firstBT_theme.getString("itemId"));
        		            		if(firstBT_theme.has("itemType")) tmpTheme.setItemType(firstBT_theme.getString("itemType"));
        		            		if(firstBT_theme.has("itemNickname")) tmpTheme.setItemNickname(firstBT_theme.getString("itemNickname"));
        		            		
        		            		this.themes.add(tmpTheme);
        		            		this.rootTheme = tmpTheme;
        		            		this.rootTheme.setJsonObject(firstBT_theme);
        		            		
        		            	}
        		            }
    		            }//end theme
    		            
    	            	//tabs
    		            if(rootBT_item.has("BT_tabs")){
    		    			BT_debugger.showIt(className + ": parsing tabs...");			
       			            JSONArray BT_tabs = rootBT_item.getJSONArray("BT_tabs");
       			    		for(int t = 0; t < BT_tabs.length(); t++){
       			    			JSONObject thisTab = BT_tabs.getJSONObject(t);
       			    			if(thisTab.has("itemType")){
	        		            	if(thisTab.getString("itemType").equalsIgnoreCase("BT_tab")){
	        		            		
	            		    			//create each tab object
	        		            		BT_item tmpTab = new BT_item();
	        		            		if(thisTab.has("itemId")) tmpTab.setItemId(thisTab.getString("itemId"));
	        		            		if(thisTab.has("itemType")) tmpTab.setItemType(thisTab.getString("itemType"));
	        		            		if(thisTab.has("itemNickname")) tmpTab.setItemNickname(thisTab.getString("itemNickname"));
	        		            		tmpTab.setJsonObject(thisTab);
	        		            		this.tabs.add(tmpTab);
	        		            		
	        		            		
	        		            	}
       			    			}
        		            }
    		            }//end tabs

       	            	//screens
    		            if(rootBT_item.has("BT_screens")){
    		    			BT_debugger.showIt(className + ": parsing screens...");			
    			            JSONArray BT_screens = rootBT_item.getJSONArray("BT_screens");
       			    		for(int s = 0; s < BT_screens.length(); s++){
       			    			JSONObject thisScreen = BT_screens.getJSONObject(s);
       			    			if(thisScreen.has("itemType")){
	        		            		
        		            		//create each screen object. These will have different itemTypes. The item
       			    				//type determines what kind of screen this is.
        		            		BT_item tmpScreen = new BT_item();
        		            		if(thisScreen.has("itemId")) tmpScreen.setItemId(thisScreen.getString("itemId"));
        		            		if(thisScreen.has("itemType")) tmpScreen.setItemType(thisScreen.getString("itemType"));
        		            		if(thisScreen.has("itemNickname")) tmpScreen.setItemNickname(thisScreen.getString("itemNickname"));
        		            		tmpScreen.setJsonObject(thisScreen);
        		            		this.screens.add(tmpScreen);
        		            		        		            		
       			    			}
        		            }        		            
    		            }
    		            
  		    			
		    		}//for each BT_item
		            
		            
	            }else{
	    			BT_debugger.showIt(className + ":parseJSONData does NOT contain any BT_items?");			
	            }
            }else{
    			BT_debugger.showIt(className + ":parseJSONData does NOT contain BT_appConfig?");			
            }
	    }catch (Exception je){
			BT_debugger.showIt(className + ":parseJSONData JSONObject ERROR: " + je.toString());			
        }	
		BT_debugger.showIt(className + ":parseJSONData done parsing application data");	

		//send it back..
		return ret;
	}
	
	//returns the dataURL or an empty string from the apps config data. This method allows us to 
	//look for a dataURL without parsing the entire file...
	@SuppressWarnings("unused")
	public String getDataURLFromAppData(String configData){
		BT_debugger.showIt(className + ": getDataURLFromAppData");			
	    String ret = "";
		
    	try{
            JSONObject obj = new JSONObject(configData);
            if(obj.has("BT_appConfig")){
	            JSONObject rootObj =  obj.getJSONObject("BT_appConfig");
	            if(rootObj.has("BT_items")){
		            JSONArray BT_items =  rootObj.getJSONArray("BT_items");
		    		for(int i = 0; i < BT_items.length(); i++){
		            	
		            	//first item in the list...the appItem
    		            JSONObject rootBT_item = BT_items.getJSONObject(0);
     		            
    		            //look for a dataURL property...
    		            if(rootBT_item.has("dataURL")){
    		            	ret = rootBT_item.getString("dataURL");
    		            }
    		            
    		            //get out of loop (we only look at the first BT_item (the app) in the data...
    		            break;
		    		}
		    	}else{
	    			BT_debugger.showIt(className + ":parseJSONData does NOT contain any BT_items?");			
	            }
            }else{
    			BT_debugger.showIt(className + ":parseJSONData does NOT contain BT_appConfig?");			
            }
	    }catch (Exception je){
			BT_debugger.showIt(className + ":parseJSONData JSONObject ERROR: " + je.toString());			
        }	
		return ret;
	}
	
	
	
	
	//get screen data by itemId
	public BT_item getScreenDataByItemId(String theScreenItemId){
		BT_debugger.showIt(className + ":getScreenDataByItemId with itemId: = \"" + theScreenItemId + "\"");			
		BT_item ret = null;
		int foundIt = 0;
		for(int s = 0; s < this.screens.size(); s++){
			if(this.screens.get(s).getItemId().equalsIgnoreCase(theScreenItemId)){
    			ret = this.screens.get(s);
    			foundIt = 1;
    			break;
    		}
   		}
    	if(foundIt == 1){
    		BT_debugger.showIt(className + ":getScreenDataByItemId with itemType: \"" + ret.getItemType() + "\" with nickname: \"" + ret.getItemNickname() + "\"");			
    	}else{
    		BT_debugger.showIt(className + ":getScreenDataByItemId could not find screen with itemId: \"" + theScreenItemId + "\"");			
    	}
		return ret;
	}
	
	//get screen data by itemNickname
	public BT_item getScreenDataByItemNickname(String theScreenNickname){
		BT_debugger.showIt(className + ":getScreenDataByItemNickname with itemNickname: \"" + theScreenNickname + "\"");			
		BT_item ret = null;
		int foundIt = 0;
		for(int s = 0; s < this.screens.size(); s++){
    		if(this.screens.get(s).getItemNickname().equalsIgnoreCase(theScreenNickname)){
    			ret = this.screens.get(s);
    			foundIt = 1;
    			break;
    		}
    			
   		}
    	if(foundIt == 1){
    		BT_debugger.showIt(className + ":getScreenDataByItemNickname with itemType: \"" + ret.getItemType() + "\" and itemNickname: \"" + ret.getItemNickname() + "\"");			
    	}else{
    		BT_debugger.showIt(className + ":getScreenDataByItemNickname could not find screen with itemNicknake: \"" + theScreenNickname + "\"");			
    	}
		return ret;
	}	
	
	//get home screen...
	@SuppressWarnings("unused")
	public BT_item getHomeScreen(){
		BT_debugger.showIt(className + ": getHomeScreen");			
		BT_item ret = null;
		int foundIt = 0;
		for(int s = 0; s < this.screens.size(); s++){
			
   			ret = this.screens.get(s);
   			foundIt = 1;
   			break;
    			
   		}
    	if(foundIt < 1){
    		BT_debugger.showIt(className + ":findHomeScreen no home screen found?");			
    	}
		return ret;
	}	

	//get theme data by itemId
	public BT_item getThemeDataByItemId(String theThemeItemId){
		BT_debugger.showIt(className + ":getThemeDataByItemId " + theThemeItemId);			
		BT_item ret = null;
		int foundIt = 0;
		for(int t = 0; t < this.screens.size(); t++){
    		if(this.themes.get(t).getItemId().equalsIgnoreCase(theThemeItemId)){
    			ret = this.themes.get(t);
    			foundIt = 1;
    			break;
    		}
    			
   		}
    	if(foundIt == 1){
    		BT_debugger.showIt(className + ":getThemeDataByItemId or theme with itemNickname: " + ret.getItemNickname());			
    	}else{
    		BT_debugger.showIt(className + ":getThemeDataByItemId fint theme with itemId: " + theThemeItemId);			
    	}
		return ret;
	}	
	
	//getters and setters
	public ArrayList<BT_item> getThemes() {
		return themes;
	}

	public void setThemes(ArrayList<BT_item> themes) {
		this.themes = themes;
	}

	public ArrayList<BT_item> getTabs() {
		return tabs;
	}

	public void setTabs(ArrayList<BT_item> tabs) {
		this.tabs = tabs;
	}

	public ArrayList<BT_item> getScreens() {
		return screens;
	}

	public void setScreens(ArrayList<BT_item> screens) {
		this.screens = screens;
	}

	public ArrayList<String> getTransitionTypeHistory() {
		return this.transitionTypeHistory;
	}

	public void setTransitionTypeHistory(
			ArrayList<String> transitionTypeHistory) {
		this.transitionTypeHistory = transitionTypeHistory;
	}
	public int getSelectedTab(){
		return this.selectedTab;
	}
	public void setSelectedTab(int selectedTab){
		this.selectedTab = selectedTab;
	}
 	
	public String getItemId() {
		return this.itemId;
	}

	public void setItemId(String itemId) {
		this.itemId = itemId;
	}
	
	public String getItemType() {
		return this.itemType;
	}

	public void setItemType(String itemType) {
		this.itemType = itemType;
	}	
	
	public String getBuzztouchAppId() {
		return this.buzztouchAppId;
	}

	public void setBuzztouchAppId(String buzztouchAppId) {
		this.buzztouchAppId = buzztouchAppId;
	}	
	
	public String getBuzztouchAPIKey() {
		return this.buzztouchAPIKey;
	}

	public void setBuzztouchAPIKey(String buzztouchAPIKey) {
		this.buzztouchAPIKey = buzztouchAPIKey;
	}		
	
	public String getDataURL() {
		return this.dataURL;
	}

	public void setDataURL(String dataURL) {
		this.dataURL = dataURL;
	}
	
	public String getReportToCloudURL() {
		return this.reportToCloudURL;
	}
	public void setReportToCloudURL(String reportToCloudURL) {
		this.reportToCloudURL = reportToCloudURL;
	}
	
	public void setRegisterForPushURL(String registerForPushURL) {
		this.registerForPushURL = registerForPushURL;
	}	
	public String getRegisterForPushURL() {
		return this.registerForPushURL;
	}

	
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getVersion() {
		return this.version;
	}

	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getCurrentMode() {
		return this.currentMode;
	}

	public void setCurrentMode(String currentMode) {
		this.version = currentMode;
	}	
	
	public String getStartLocationUpdates() {
		return this.startLocationUpdates;
	}

	public void setStartLocationUpdates(String startLocationUpdates) {
		this.startLocationUpdates = startLocationUpdates;
	}	
	
	public String getPromptForPushNotifications() {
		return this.promptForPushNotifications;
	}

	public void setPromptForPushNotifications(String promptForPushNotifications) {
		this.promptForPushNotifications = promptForPushNotifications;
	}	
	
	public String getPushRegistrationId() {
		return pushRegistrationId;
	}

	public void setPushRegistrationId(String pushRegistrationId) {
		this.pushRegistrationId = pushRegistrationId;
	}
	
	public String getAllowRotation() {
		return this.allowRotation;
	}

	public void setAllowRotation(String allowRotation) {
		this.allowRotation = allowRotation;
	}	
	
	public String getJsonVars() {
		return this.jsonVars;
	}

	public void setJsonVars(String jsonVars) {
		this.jsonVars = jsonVars;
	}

	public BT_item getRootTheme() {
		return this.rootTheme;
	}

	public void setRootTheme(BT_item rootTheme) {
		this.rootTheme = rootTheme;
	}

	public BT_device getRootDevice() {
		return this.rootDevice;
	}

	public void setRootDevice(BT_device rootDevice) {
		this.rootDevice = rootDevice;
	}

	public BT_user getRootUser() {
		return this.rootUser;
	}

	public void setRootUser(BT_user rootUser) {
		this.rootUser = rootUser;
	}
	
	public BT_item getCurrentScreenData() {
		return currentScreenData;
	}

	public void setCurrentScreenData(BT_item currentScreenData) {
		this.currentScreenData = currentScreenData;
	}

	public BT_item getPreviousScreenData() {
		return previousScreenData;
	}

	public void setPreviousScreenData(BT_item previousScreenData) {
		this.previousScreenData = previousScreenData;
	}
	

	public BT_item getCurrentMenuItemData() {
		return currentMenuItemData;
	}

	public void setCurrentMenuItemData(BT_item currentMenuItemData) {
		this.currentMenuItemData = currentMenuItemData;
	}

	public BT_item getPreviousMenuItemData() {
		return previousMenuItemData;
	}

	public void setPreviousMenuItemData(BT_item previousMenuItemData) {
		this.previousMenuItemData = previousMenuItemData;
	}

	public BT_item getCurrentItemUpload() {
		return currentItemUpload;
	}

	public void setCurrentItemUpload(BT_item currentItemUpload) {
		this.currentItemUpload = currentItemUpload;
	}

	public BT_item getTab0ScreenData() {
		return tab0ScreenData;
	}

	public void setTab0ScreenData(BT_item tab0ScreenData) {
		this.tab0ScreenData = tab0ScreenData;
	}

	public BT_item getTab1ScreenData() {
		return tab1ScreenData;
	}

	public void setTab1ScreenData(BT_item tab1ScreenData) {
		this.tab1ScreenData = tab1ScreenData;
	}

	public BT_item getTab2ScreenData() {
		return tab2ScreenData;
	}

	public void setTab2ScreenData(BT_item tab2ScreenData) {
		this.tab2ScreenData = tab2ScreenData;
	}

	public BT_item getTab3ScreenData() {
		return tab3ScreenData;
	}

	public void setTab3ScreenData(BT_item tab3ScreenData) {
		this.tab3ScreenData = tab3ScreenData;
	}

	public BT_item getTab4ScreenData() {
		return tab4ScreenData;
	}

	public void setTab4ScreenData(BT_item tab4ScreenData) {
		this.tab4ScreenData = tab4ScreenData;
	}


	
	
	
}









