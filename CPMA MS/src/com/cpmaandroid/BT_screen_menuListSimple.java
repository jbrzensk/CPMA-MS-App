/*
 *	Copyright 2011, David Book, buzztouch.com
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
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;



public class BT_screen_menuListSimple extends BT_activity_base implements OnScrollListener{
	
	private boolean didCreate = false;
	private boolean didLoadData = false;
	private DownloadScreenDataWorker downloadScreenDataWorker;
	private String JSONData = "";
	private ArrayList<BT_item> childItems = null;
	private ChildItemAdapter childItemAdapter;
	private ListView myListView = null;
	private int selectedIndex = -1;
	public boolean isFlinging = false;
	
	//properties from JSON
	public String dataURL = "";
	public String saveAsFileName = "";
	
	public String listStyle = "";
	public String preventAllScrolling = "";
	public String listBackgroundColor = "";
	public String listRowBackgroundColor = "";
	public String listRowSelectionStyle = "";
	public String listTitleFontColor = "";
	public String listRowSeparatorColor = "";
	
	//these depend on large or small device...
	public int listRowHeight = 0;
	public int listTitleHeight = 0;
	public int listTitleFontSize = 0;

	
	//////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.
	
	//onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        this.activityName = "BT_screen_menuListSimple";
		BT_debugger.showIt(activityName + ":onCreate");	
		
		LinearLayout baseView = (LinearLayout)findViewById(R.id.baseView);
		
		//setup background colors...
		BT_viewUtilities.updateBackgroundColorsForScreen(this, this.screenData);
		
		//setup background images..
		if(backgroundImageWorkerThread == null){
			backgroundImageWorkerThread = new BackgroundImageWorkerThread();
			backgroundImageWorkerThread.start();
		}			
		
		//setup navigation bar...
		LinearLayout navBar = BT_viewUtilities.getNavBarForScreen(this, this.screenData);
		if(navBar != null){
			baseView.addView(navBar);
		}
		
		
		//init properties for JSON data...
		childItems = new ArrayList<BT_item>();
		dataURL = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "dataURL", "");
		saveAsFileName = this.screenData.getItemId() + "_screenData.txt";
		if(dataURL.length() < 1) BT_fileManager.deleteFile(saveAsFileName);
		
		///////////////////////////////////////////////////////////////////
		//properties for both large and small devices...
	
		
		listBackgroundColor = BT_strings.getStyleValueForScreen(this.screenData, "listBackgroundColor", "0");
		preventAllScrolling = BT_strings.getStyleValueForScreen(this.screenData, "preventAllScrolling", "0");
		listBackgroundColor = BT_strings.getStyleValueForScreen(this.screenData, "listBackgroundColor", "#FFFFFF"); //was #000000
		listRowBackgroundColor = BT_strings.getStyleValueForScreen(this.screenData, "listRowBackgroundColor", "#FFFFFF"); //was #000000
		listRowSelectionStyle = BT_strings.getStyleValueForScreen(this.screenData, "listRowSelectionStyle", "");
		listTitleFontColor = BT_strings.getStyleValueForScreen(this.screenData, "listTitleFontColor", "#000000"); //was ""
		listRowSeparatorColor = BT_strings.getStyleValueForScreen(this.screenData, "listRowSeparatorColor", "");
		
		//settings that depend on large or small devices...
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().getIsLargeDevice()){
			
			//large devices...
			listRowHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listRowHeightLargeDevice", "50"));
			listTitleHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleHeightLargeDevice", "30"));
			listTitleFontSize = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleFontSizeLargeDevice", "22"));
		
		}else{
			
			//small devices...
			listRowHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listRowHeightSmallDevice", "50"));
			listTitleHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleHeightSmallDevice", "50"));
			listTitleFontSize = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleFontSizeSmallDevice", "22"));
			
		}
		
		
		//inflate this screens layout file...
		LayoutInflater vi = (LayoutInflater)thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View thisScreensView = vi.inflate(R.layout.screen_menulistsimple, null);
		
		//reference to ListView in the layout file...
		myListView = (ListView) thisScreensView.findViewById(R.id.listView);
		myListView.setOnScrollListener(this);
		myListView.setVerticalScrollBarEnabled(false);
		myListView.setHorizontalScrollBarEnabled(false);
		myListView.setFastScrollEnabled(false);
		myListView.setSmoothScrollbarEnabled(false);

		//setup adapter for data...
    	childItemAdapter = new ChildItemAdapter();
		myListView.setAdapter(childItemAdapter);
		
		//prevent scrolling....
		preventAllScrolling = "1";
		if(preventAllScrolling.equalsIgnoreCase("1")){
			//need to figure out how to prevent scrolling...
		}
		
		if(listBackgroundColor.length() > 0){
			myListView.setCacheColorHint(BT_color.getColorFromHexString(listBackgroundColor));
			myListView.setBackgroundColor(BT_color.getColorFromHexString(listBackgroundColor));
		}
		
		//separtor color...
		if(listRowSeparatorColor.length() > 0){
			ColorDrawable dividerColor = new ColorDrawable(BT_color.getColorFromHexString(listRowSeparatorColor));
			myListView.setDivider(dividerColor);
			myListView.setDividerHeight(1);
		}
		
			
		
		//add the view to the base view...
		baseView.addView(thisScreensView);
		
		//invalidate view to it repaints...
		baseView.invalidate();
		
		//flag as created..
        didCreate = true;
        
 		
	}//onCreate

	//onStart
	@Override 
	protected void onStart() {
		//BT_debugger.showIt(activityName + ":onStart");	
		super.onStart();
		
		//make sure data adapter is set...
		if(childItemAdapter == null){
			childItemAdapter = new ChildItemAdapter();
		}
		
		//ignore this if we already created the screen...
		if(!didLoadData){
			
			if(saveAsFileName.length() > 1){
				
				//check cache...
				String parseData = "";
				if(BT_fileManager.doesCachedFileExist(saveAsFileName)){
					BT_debugger.showIt(activityName + ":onStart using cached screen data");	
					parseData = BT_fileManager.readTextFileFromCache(saveAsFileName);
					parseScreenData(parseData);
				}else{
					//get data from URL if we have one...
					if(this.dataURL.length() > 1){
						BT_debugger.showIt(activityName + ":onStart downloading screen data from URL");	
						refreshScreenData();
					}else{
						//parse with "empty" data...
						BT_debugger.showIt(activityName + ":onStart using data from app's configuration file");	
						parseScreenData("");
					}
				}
					
			}//saveAsFileName
		}//did load data
		
		
	}
	
    //onResume
    @Override
    public void onResume() {
       super.onResume();
       	//BT_debugger.showIt(activityName + ":onResume");
		
	   //select previous item if coming "back"...
       if(didCreate && didLoadData){
	   		if(selectedIndex > -1){
				if(myListView != null){
					if(myListView.getAdapter().getCount() >= selectedIndex){
						myListView.requestFocusFromTouch();
						myListView.setSelection(selectedIndex);
					}
				}	
			}
       }
       
    }
    
    //onPause
    @Override
    public void onPause() {
        super.onPause();
        //BT_debugger.showIt(activityName + ":onPause");	
    }
    
	
	//onStop
	@Override 
	protected void onStop(){
		super.onStop();
        //BT_debugger.showIt(activityName + ":onStop");	
		if(downloadScreenDataWorker != null){
			boolean retry = true;
			downloadScreenDataWorker.setThreadRunning(false);
			while(retry){
				try{
					downloadScreenDataWorker.join();
					retry = false;
				}catch (Exception je){
				}
			}
		}
	}	
	
	
	//onDestroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        //BT_debugger.showIt(activityName + ":onDestroy");	
    }
    
	//end activity life-cycle events
	//////////////////////////////////////////////////////////////////////////
  
    //handles onScrollStateChanged..
	public void onScrollStateChanged(AbsListView view, int scrollState) {
        //BT_debugger.showIt(activityName + ":onScrollStateChanged");	
        
        //don't load images when the list is moving - bogus experience!
        if(scrollState != OnScrollListener.SCROLL_STATE_FLING){
            //BT_debugger.showIt(activityName + ":onScrollStateChanged NOT flinging");	
        	isFlinging = false;
        	
        }else{
            //BT_debugger.showIt(activityName + ":onScrollStateChanged IS flinging");	
        	isFlinging = true;
        }
    }

	//onScroll event
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount,int totalItemCount){
        //BT_debugger.showIt(activityName + ":onScroll");	
    }
    
    
  
    //parse screenData...
    public void parseScreenData(String theJSONString){
        BT_debugger.showIt(activityName + ":parseScreenData");	
        //BT_debugger.showIt(activityName + ":parseScreenData " + theJSONString);
		//parse JSON string
    	try{

    		//empty data if previously filled...
    		childItems.clear();

            //if theJSONString is empty, look for child items in this screen's config data..
    		JSONArray items = null;
    		if(theJSONString.length() < 1){
    			if(this.screenData.getJsonObject().has("childItems")){
        			items =  this.screenData.getJsonObject().getJSONArray("childItems");
    			}
    		}else{
        		JSONObject raw = new JSONObject(theJSONString);
        		if(raw.has("childItems")){
        			items =  raw.getJSONArray("childItems");
        		}
    		}
  
    		//loop items..
    		if(items != null){
                for (int i = 0; i < items.length(); i++){
                	
                	JSONObject tmpJson = items.getJSONObject(i);
                	BT_item tmpItem = new BT_item();
                	if(tmpJson.has("itemId")) tmpItem.setItemId(tmpJson.getString("itemId"));
                	if(tmpJson.has("itemType")) tmpItem.setItemType(tmpJson.getString("itemType"));
                	tmpItem.setJsonObject(tmpJson);
                	childItems.add(tmpItem);
                	
                }//for
                
                
                //flag data loaded...
                didLoadData = true;
    			
    		}else{
    			BT_debugger.showIt(activityName + ":parseScreenData NO CHILD ITEMS?");
    			
    		}
    	}catch(Exception e){
			BT_debugger.showIt(activityName + ":parseScreenData EXCEPTION " + e.toString());
    	}
        
    	
 	    //setup list click listener
    	final OnItemClickListener listItemClickHandler = new OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v, int position, long id){
            	
            	//remember the selected item's index...
           		selectedIndex = position;
            	
              	//the BT_item
	        	BT_item tappedItem = (BT_item) childItems.get(position);
				String loadScreenWithItemId = BT_strings.getJsonPropertyValue(tappedItem.getJsonObject(), "loadScreenWithItemId", "");
				String loadScreenWithNickname = BT_strings.getJsonPropertyValue(tappedItem.getJsonObject(), "loadScreenWithNickname", "");
				
  				//bail if none...
				if(loadScreenWithItemId.equalsIgnoreCase("none")){
					return;
				}				
				
				
	           	//itemId, nickname or object...
	            BT_item tapScreenLoadObject = null;
	        	if(loadScreenWithItemId.length() > 1 && !loadScreenWithItemId.equalsIgnoreCase("none")){
	    			BT_debugger.showIt(activityName + ":handleItemTap loads screen with itemId: \"" + loadScreenWithItemId + "\"");
	        		tapScreenLoadObject = cpmaandroid_appDelegate.rootApp.getScreenDataByItemId(loadScreenWithItemId);
	        	}else{
	        		if(loadScreenWithNickname.length() > 1){
	    				BT_debugger.showIt(activityName + ":handleItemTap loads screen with nickname: \"" + loadScreenWithNickname + "\"");
	        			tapScreenLoadObject = cpmaandroid_appDelegate.rootApp.getScreenDataByItemNickname(loadScreenWithNickname);
	        		}else{
	        			try{
	    	    			JSONObject obj = tappedItem.getJsonObject();
	    		            if(obj.has("loadScreenObject")){
	    						BT_debugger.showIt(activityName + ":handleItemTap button loads screen object configured with JSON object.");
	    		            	JSONObject tmpLoadScreen = obj.getJSONObject("loadScreenObject");
	    		            	tapScreenLoadObject = new BT_item();
	        		            if(tmpLoadScreen.has("itemId")) tapScreenLoadObject.setItemId(tmpLoadScreen.getString("itemId"));
	        		            if(tmpLoadScreen.has("itemNickname")) tapScreenLoadObject.setItemNickname(tmpLoadScreen.getString("itemNickname"));
	        		            if(tmpLoadScreen.has("itemType")) tapScreenLoadObject.setItemType(tmpLoadScreen.getString("itemType"));
	        		            if(obj.has("loadScreenObject")) tapScreenLoadObject.setJsonObject(tmpLoadScreen);
	    		            }
	        			}catch(Exception e){
	    					BT_debugger.showIt(activityName + ":handleItemTap EXCEPTION reading screen-object for item: " + e.toString());
	        			}
	        		}
	        	}

	        	//if we have a screen object to load from the right-button tap, build a BT_itme object...
	        	if(tapScreenLoadObject != null){
	        		
	            	//call loadScreenObject (static method in BT_act_controller class)...
	       			BT_act_controller.loadScreenObject(thisActivity, screenData, tappedItem, tapScreenLoadObject);
	       		
	        	}else{
	    			BT_debugger.showIt(activityName + ":handleItemTap ERROR. No screen is connected to this item?");	
	        		BT_activity_base.showAlertFromClass(cpmaandroid_appDelegate.getApplication().getString(R.string.errorTitle), cpmaandroid_appDelegate.getApplication().getString(R.string.errorNoScreenConnected));
	        	}
	        	            	
           	}
        };    
        myListView.setOnItemClickListener(listItemClickHandler);             

        //set the item adapter...
		myListView.setAdapter(childItemAdapter);
        
        //invalidate list so it repaints...
        myListView.invalidate();
        
        //hide progress...
        hideProgress();
        
    }
    
 
   
    //refresh screenData
    public void refreshScreenData(){
        BT_debugger.showIt(activityName + ":refreshScreenData");	
        showProgress(null, null);
        
        if(dataURL.length() > 1){
	      	downloadScreenDataWorker = new DownloadScreenDataWorker();
        	downloadScreenDataWorker.setDownloadURL(dataURL);
        	downloadScreenDataWorker.setSaveAsFileName(saveAsFileName);
        	downloadScreenDataWorker.setThreadRunning(true);
        	downloadScreenDataWorker.start();
        }else{
            BT_debugger.showIt(activityName + ":refreshScreenData NO DATA URL for this screen? Not downloading.");	
        }
        
    }    
       
    ///////////////////////////////////////////////////////////////////
    //Adapter for Child Items
    private class ChildItemAdapter extends BaseAdapter{
 
        public int getCount(){
            return childItems.size();
        }

		public Object getItem(int position){
			if (position == childItems.size()){
				return null;
			}else{
				return childItems.get(position);
			}
		}

		public long getItemId(int position){
			if(position == childItems.size()){
				return -1;
			}else{
				return childItems.get(position).getItemIndex();
			}
		}
  
		//images for indicator (right side) only instantiate these once, not for every row...
		Drawable chevron_detailsDrawable = null;
		Drawable chevron_arrowDrawable = null;

		TextView titleView = null;
		ImageView indicatorView = null;
		
		//returns the view for each row...only inflate once to enable view recyling..
		public View getView(int position, View convertView, ViewGroup parent) {
			View rowView = convertView;
			
	       	//the BT_item values for this item..
        	BT_item tmpItem = childItems.get(position);
			String titleText = BT_strings.getJsonPropertyValue(tmpItem.getJsonObject(), "titleText", "");
			//String transitionType = BT_strings.getJsonPropertyValue(tmpItem.getJsonObject(), "transitionType", "");
			String rowAccessoryType = BT_strings.getJsonPropertyValue(tmpItem.getJsonObject(), "rowAccessoryType", "");
			
			//inflate the layout and set size / position properties only when we have to...
			if(rowView == null) {
				
				//images for indicator (right side)...
				chevron_detailsDrawable = BT_fileManager.getDrawableByName("bt_chevron_details.png");
				chevron_arrowDrawable = BT_fileManager.getDrawableByName("bt_chevron_arrow.png");
				
				//inflate the view so we can get a reference to it's parts..				
				LayoutInflater inflater = (LayoutInflater) thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				rowView = inflater.inflate(R.layout.menu_list_rowsimple, parent, false);		
				
				
			}//rowView is null
			
			/*
			 * 	listRowHeight and listTitleHeight should be the same because the layout 'wraps' height...
			 */
			
			//get layout parts for this row..
			titleView = (TextView) rowView.findViewById(R.id.titleView);
			titleView.setTextSize(listTitleFontSize);
			titleView.setHeight(listRowHeight);
			indicatorView = (ImageView) rowView.findViewById(R.id.indicatorView);

			//tag the view...
			rowView.setTag(tmpItem);
		
			//title font color				
			if(listTitleFontColor.length() > 0){
				titleView.setTextColor(BT_color.getColorFromHexString(listTitleFontColor));
			}
			
			//title font size
			if(listTitleFontSize > 0){
				titleView.setTextSize(listTitleFontSize);
			}
			
			//title text...
			titleView.setText(titleText);
			
			//setup indicator view...
			if(rowAccessoryType.equalsIgnoreCase("none")){
				indicatorView.setVisibility(View.GONE);
			}else{
				indicatorView.setVisibility(View.VISIBLE);
				if(rowAccessoryType.equalsIgnoreCase("details")){
					indicatorView.setImageDrawable(chevron_detailsDrawable);
				}else{
					indicatorView.setImageDrawable(chevron_arrowDrawable);
				}
			}
			//hide indicator if we have no title...
			if(titleText.length() < 1){
				indicatorView.setVisibility(View.GONE);
			}else{
				indicatorView.setVisibility(View.VISIBLE);
			}			
			
			//return
			return rowView;
		}

      
        
    }

   //END child items adapter...
    ///////////////////////////////////////////////////////////////////
         

    
    ///////////////////////////////////////////////////////////////////
	//DownloadScreenDataThread and Handler
	Handler downloadScreenDataHandler = new Handler(){
		@Override public void handleMessage(Message msg){
			if(JSONData.length() < 1){
				hideProgress();
				showAlert(getString(R.string.errorTitle), getString(R.string.errorDownloadingData));
			}else{
				parseScreenData(JSONData);
			}
		}
	};	   
    
	public class DownloadScreenDataWorker extends Thread{
		 boolean threadRunning = false;
		 String downloadURL = "";
		 String saveAsFileName = "";
		 void setThreadRunning(boolean bolRunning){
			 threadRunning = bolRunning;
		 }	
		 void setDownloadURL(String theURL){
			 downloadURL = theURL;
		 }
		 void setSaveAsFileName(String theFileName){
			 saveAsFileName = theFileName;
		 }
		 @Override 
    	 public void run(){
			
			 //downloader will fetch and save data..Set this screen data as "current" to be sure the screenId
			 //in the URL gets merged properly. Several screens could be loading at the same time...
			 cpmaandroid_appDelegate.rootApp.setCurrentScreenData(screenData);
			 String useURL = BT_strings.mergeBTVariablesInString(dataURL);
			 BT_debugger.showIt(activityName + ": downloading screen data from " + useURL);
			 BT_downloader objDownloader = new BT_downloader(useURL);
			 objDownloader.setSaveAsFileName(saveAsFileName);
			 JSONData = objDownloader.downloadTextData();
			
			 //send message to handler..
			 this.setThreadRunning(false);
			 downloadScreenDataHandler.sendMessage(downloadScreenDataHandler.obtainMessage());
   	 	
		 }
	}	
	//END DownloadScreenDataThread and Handler
	///////////////////////////////////////////////////////////////////
	

	/////////////////////////////////////////////////////
	//options menu (hardware menu-button)
	@Override 
	public boolean onPrepareOptionsMenu(Menu menu) { 
		super.onPrepareOptionsMenu(menu); 
		
		 //set up dialog
        final Dialog dialog = new Dialog(this);
        
		//linear layout holds all the options...
		LinearLayout optionsView = new LinearLayout(this);
		optionsView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		optionsView.setOrientation(LinearLayout.VERTICAL);
		optionsView.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
		optionsView.setPadding(20, 0, 20, 20); //left, top, right, bottom
		
		//options have individual layout params
		LinearLayout.LayoutParams btnLayoutParams = new LinearLayout.LayoutParams(400, LayoutParams.WRAP_CONTENT);
		btnLayoutParams.setMargins(10, 10, 10, 10);
		btnLayoutParams.leftMargin = 10;
		btnLayoutParams.rightMargin = 10;
		btnLayoutParams.topMargin = 0;
		btnLayoutParams.bottomMargin = 10;
		
		//holds all the options 
		ArrayList<Button> options = new ArrayList<Button>();

		//cancel...
		final Button btnCancel = new Button(this);
		btnCancel.setText(getString(R.string.okClose));
		
		btnCancel.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
                dialog.cancel();
            }
        });
		options.add(btnCancel);
		
		//refresh screen data...
		if(dataURL.length() > 1){
			final Button btnRefreshScreenData = new Button(this);
			btnRefreshScreenData.setText(getString(R.string.refreshScreenData));
			btnRefreshScreenData.setOnClickListener(new OnClickListener(){
            	public void onClick(View v){
                	dialog.cancel();
            		refreshScreenData();
            	}
			});
			options.add(btnRefreshScreenData);
		}
		
		//refreshAppData (if we are on home screen)
		if(this.screenData.isHomeScreen() && cpmaandroid_appDelegate.rootApp.getDataURL().length() > 1){
			
			final Button btnRefreshAppData = new Button(this);
			btnRefreshAppData.setText(getString(R.string.refreshAppData));
			btnRefreshAppData.setOnClickListener(new OnClickListener(){
	            public void onClick(View v){
	                dialog.cancel();
					refreshAppData();
	            }
	        });
			options.add(btnRefreshAppData);			
		}		
		
		//add each option to layout, set layoutParams as we go...
		for(int x = 0; x < options.size(); x++){
            Button btn = (Button)options.get(x);
            btn.setTextSize(18);
            btn.setLayoutParams(btnLayoutParams);
            btn.setPadding(5, 5, 5, 5);
			optionsView.addView(btn);
		}
		
	
		//set content view..        
        dialog.setContentView(optionsView);
        if(options.size() > 1){
        	dialog.setTitle(getString(R.string.menuOptions));
        }else{
        	dialog.setTitle(getString(R.string.menuNoOptions));
        }
        
        //show
        dialog.show();
		return true;
		
	} 
	//end options menu
	/////////////////////////////////////////////////////
	    
    
    
}













