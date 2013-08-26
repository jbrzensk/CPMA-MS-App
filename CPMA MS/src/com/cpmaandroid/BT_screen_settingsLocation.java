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
import java.util.List;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;



public class BT_screen_settingsLocation extends BT_activity_base{
	private boolean didLoadData = false;
	public boolean didCreate = false;
	public String JSONData = "";
	public ChildItemAdapter childItemAdapter;
	private ListView myListView = null;
	
	//layout properties (probably not used on settings screens?)...
	public String listStyle = "";
	private String preventAllScrolling = "";
	private String listBackgroundColor = "";
	private String listRowBackgroundColor = "";
	public String listTitleFontColor = "";
	private String listRowSeparatorColor = "";
	
	//these depend on the device size..
	public int listRowHeight = 0;
	public int listTitleHeight = 0;
	public int listTitleFontSize = 0;

	
	//////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.
	
	//onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        this.activityName = "BT_screen_settingsLocation";
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
		
		///////////////////////////////////////////////////////////////////
		//settings for any sized device...
	
		listStyle = BT_strings.getStyleValueForScreen(this.screenData, "listStyle", "plain");
		preventAllScrolling = BT_strings.getStyleValueForScreen(this.screenData, "preventAllScrolling", "0");
		listBackgroundColor = BT_strings.getStyleValueForScreen(this.screenData, "listBackgroundColor", "#000000");
		listRowBackgroundColor = BT_strings.getStyleValueForScreen(this.screenData, "listRowBackgroundColor", "#000000");
		listTitleFontColor = BT_strings.getStyleValueForScreen(this.screenData, "listTitleFontColor", "#FFFFFF");
		listRowSeparatorColor = BT_strings.getStyleValueForScreen(this.screenData, "listRowSeparatorColor", "#CCCCCC");
		
		//settings based on device size...
		if(cpmaandroid_appDelegate.rootApp.getRootDevice().getIsLargeDevice()){
			
			//large devices...
			listRowHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listRowHeightLargeDevice", "45"));
			listTitleHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleHeightLargeDevice", "25"));
			listTitleFontSize = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleFontSizeLargeDevice", "20"));
		
		}else{
			
			//small devices...
			listRowHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listRowHeightSmallDevice", "45"));
			listTitleHeight = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleHeightSmallDevice", "25"));
			listTitleFontSize = Integer.parseInt(BT_strings.getStyleValueForScreen(this.screenData, "listTitleFontSizeSmallDevice", "20"));
			
		}
		
		//inflate this screens layout file..
		LayoutInflater vi = (LayoutInflater)thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View thisScreensView = vi.inflate(R.layout.screen_settingslocation, null);
		
		//reference to list..
		myListView = (ListView) thisScreensView.findViewById(R.id.listView);
		myListView.setVerticalScrollBarEnabled(false);
		myListView.setHorizontalScrollBarEnabled(false);
		
		//prevent scrolling...
		if(preventAllScrolling.equalsIgnoreCase("1")){
			//Need to figure out how to prevent scrolling?
		}
		
		//list background color...
		if(listBackgroundColor.length() > 0){
			myListView.setBackgroundColor(BT_color.getColorFromHexString(listBackgroundColor));
		}
		
		//row background color...
		if(listRowBackgroundColor.length() > 0){
			//not useful, use listBackgroundColor instead.
		}
		
		//divider / separator color...
		if(listRowSeparatorColor.length() > 0){
			ColorDrawable dividerColor = new ColorDrawable(BT_color.getColorFromHexString(listRowSeparatorColor));
			myListView.setDivider(dividerColor);
			myListView.setDividerHeight(1);
		}
	
		//add the view to the base view...
		baseView.addView(thisScreensView);
		
 		//flag as created..
        didCreate = true;
        
 		
	}//onCreate

	//onStart
	@Override 
	protected void onStart() {
		//BT_debugger.showIt(activityName + ":onStart");	
		super.onStart();
		
		//ignore this if we already created the screen...
		if(!didLoadData){
			parseScreenData();
		}
		
	}
	
    //onResume
    @Override
    public void onResume() {
       super.onResume();
       	//BT_debugger.showIt(activityName + ":onResume");       
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
	}	
	
	
	//onDestroy
    @Override
    public void onDestroy() {
        super.onDestroy();
        //BT_debugger.showIt(activityName + ":onDestroy");	
    }
    
	//end activity life-cycle events
	//////////////////////////////////////////////////////////////////////////
    
    //parse screenData...
    public void parseScreenData(){
        BT_debugger.showIt(activityName + ":parseScreenData");	
    	
        //invalidate list...
        myListView.invalidate();
        
		//setup dataModel for listView
		ArrayAdapter<ChildItemsDataModel> adapter = new ChildItemAdapter(this, getModel());
		myListView.setAdapter(adapter);
 
       
    }
    
    ///////////////////////////////////////////////////////////////////
    //childItems Data Model for selectable list items..
    public class ChildItemsDataModel{

    	private String name;
    	private boolean selected;

    	public ChildItemsDataModel(String name) {
    		this.name = name;
    		selected = false;
    	}

    	public String getName() {
    		return name;
    	}

    	public void setName(String name) {
    		this.name = name;
    	}

    	public boolean isSelected() {
    		return selected;
    	}

    	public void setSelected(boolean selected) {
    		this.selected = selected;
    	}

    }
    //END Data Model for selectable items
    ///////////////////////////////////////////////////////////////////

    
    //fill data model with childItems..
	private List<ChildItemsDataModel> getModel() {
		List<ChildItemsDataModel> list = new ArrayList<ChildItemsDataModel>();
	        	
		//allow GPS
   		list.add(getChildItem(getString(R.string.settingsAllowGPS)));
	    		
   		//prevent GPS
   		list.add(getChildItem(getString(R.string.settingsPreventGPS)));
	        		
   		//add last empty item so we have a border below last row (tricky!)...
   		list.add(getChildItem(""));
 		
		//initially select the items..
		if(BT_strings.getPrefString("userAllowLocation").equalsIgnoreCase("prevent")){
			list.get(0).setSelected(false);
			list.get(1).setSelected(true);
		}else{
			list.get(0).setSelected(true);
			list.get(1).setSelected(false);
		}
   		
   		//flag, return...
   		didLoadData = true;
		return list;
	}

	private ChildItemsDataModel getChildItem(String s) {
		return new ChildItemsDataModel(s);
	}    
    
       
    ///////////////////////////////////////////////////////////////////
    //Adapter for Child Items
    public class ChildItemAdapter extends ArrayAdapter<ChildItemsDataModel> {
    	
    	private final List<ChildItemsDataModel> list;
    	private final Activity context;

    	public ChildItemAdapter(Activity context, List<ChildItemsDataModel> list) {
    		super(context, R.layout.menu_list_checkbox, list);
    		this.context = context;
    		this.list = list;
    	}

    	private class ViewHolder {
    		protected TextView text;
    		protected CheckBox checkbox;
    	}
    	
    	
    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		View view = null;
    		if (convertView == null) {
    			LayoutInflater inflator = context.getLayoutInflater();
    			view = inflator.inflate(R.layout.menu_list_checkbox, null);
    			final ViewHolder viewHolder = new ViewHolder();
    			viewHolder.text = (TextView) view.findViewById(R.id.titleView);
    			viewHolder.checkbox = (CheckBox) view.findViewById(R.id.checkboxView);
    			viewHolder.checkbox.setOnClickListener(new OnClickListener(){
					public void onClick(View v) {
    					String theText = viewHolder.text.getText().toString();
 	          			
    					//update allow / prevent userAllowLocation preference...
    	           		if(theText.equalsIgnoreCase(getString(R.string.settingsAllowGPS))){
    	           			BT_strings.setPrefString("userAllowLocation", "allow");
    	           			cpmaandroid_appDelegate.foundUpdatedLocation = false;
    	           		}
    	           		if(theText.equalsIgnoreCase(getString(R.string.settingsPreventGPS))){
    	           			BT_strings.setPrefString("userAllowLocation", "prevent");
    	           			cpmaandroid_appDelegate.rootApp.getRootDevice().setDeviceLatitude("0");
    	           			cpmaandroid_appDelegate.rootApp.getRootDevice().setDeviceLongitude("0");
    	           			cpmaandroid_appDelegate.rootApp.getRootDevice().setDeviceAccuracy("0");
    	           			cpmaandroid_appDelegate.foundUpdatedLocation = true;
    	           		}
						
	  	           		//delay refresh...
		           		Handler mHandler = new Handler();
		           		mHandler.postDelayed(mLaunchTask, 300); 
					}
    			});
    			viewHolder.checkbox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
    				public void onCheckedChanged(CompoundButton buttonView,	boolean isChecked) {
    					ChildItemsDataModel element = (ChildItemsDataModel) viewHolder.checkbox.getTag();
    					element.setSelected(buttonView.isChecked());
    					
    				}
    			});
    			view.setTag(viewHolder);
    			viewHolder.checkbox.setTag(list.get(position));
    		} else {
    			view = convertView;
    			((ViewHolder) view.getTag()).checkbox.setTag(list.get(position));
    		}
    		ViewHolder holder = (ViewHolder) view.getTag();
    		holder.text.setText(list.get(position).getName());
    		holder.checkbox.setChecked(list.get(position).isSelected());
    		
    		//hide the last checkbox (it's the last empty row)
    		if(list.get(position).getName().length() < 1){
    			holder.checkbox.setVisibility(View.GONE);
    		}else{
    			holder.checkbox.setVisibility(View.VISIBLE);
    		}
    		
    		//return...
    		return view;
    	}    
    }
    //END child items adapter...
    ///////////////////////////////////////////////////////////////////
         
    private Runnable mLaunchTask = new Runnable(){
        public void run() {
            try {
               parseScreenData();
            }catch(Exception e){
               
            }

        }
};
		  
		  
		  
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
		optionsView.setGravity(Gravity.CENTER_VERTICAL);
		optionsView.setPadding(0, 0, 0, 20);
		
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







