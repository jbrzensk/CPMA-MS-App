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

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AlphaAnimation;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class BT_viewUtilities{

	private static String objectName = "BT_viewUtilities";

	//updates a screens background color...
	public static void updateBackgroundColorsForScreen(final Activity theActivity, final BT_item theScreenData){
		BT_debugger.showIt(objectName + ":updateBackgroundColorsForScreen with nickname: \"" + theScreenData.getItemNickname() + "\"");

		//will hold background color...
		String backgroundColor = "";
		RelativeLayout backgroundSolidColorView = null;
		
		try{
		
			//global theme is used if screen does not have a setting
			BT_item theThemeData = cpmaandroid_appDelegate.rootApp.getRootTheme();
	
			//reference to background color relative layout in act_base.xml (this layout file is used by all the plugins)...
			backgroundSolidColorView = (RelativeLayout) theActivity.findViewById(R.id.backgroundSolidColorView);
			
			//solid background color
			if(BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "backgroundColor", "").length() > 1){
				backgroundColor = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "backgroundColor", "");
			}else{
				backgroundColor = BT_strings.getJsonPropertyValue(theThemeData.getJsonObject(), "backgroundColor", "");
			}
		
		}catch (Exception e){
			BT_debugger.showIt(objectName + ":updateBackgroundColorsForScreen: EXCEPTION (1) " + e.toString());
		}			
		
		
		try{
			if(backgroundColor.length() > 5 && backgroundSolidColorView != null){
				BT_debugger.showIt(objectName + ":updateBackgroundColorsForScreen: setting background color to: \"" + backgroundColor + "\"");
				try{
					if(backgroundColor.equalsIgnoreCase("clear")){
						backgroundSolidColorView.setBackgroundColor(Color.TRANSPARENT);
						backgroundSolidColorView.setVisibility(0);
					}else{
						backgroundSolidColorView.setBackgroundColor(BT_color.getColorFromHexString(backgroundColor));
						backgroundSolidColorView.setVisibility(1);
					}
					backgroundSolidColorView.invalidate();
				}catch(Exception e){
					BT_debugger.showIt(objectName + ":updateBackgroundColorsForScreen: Exception setting background color (\"" + backgroundColor + "\") " + e.toString());
				}
			}
		
		}catch (Exception e){
			BT_debugger.showIt(objectName + ":updateBackgroundColorsForScreen: EXCEPTION (2)" + e.toString());
		}
	
	}	

		
	//returns navigation bar for a screen...
	public static LinearLayout getNavBarForScreen(final Activity theActivity, final BT_item theScreenData){
		BT_debugger.showIt(objectName + ":getNavBarForScreen building nav. bar for screen with nickname: \"" + theScreenData.getItemNickname() + "\"");
		
		//global theme is used if screen does not have a setting
		String navBarStyle = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "navBarStyle", "");
		String navBarTitleText = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "navBarTitleText", "");
		String navBarRightButtonType = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "navBarRightButtonType", "");
		String navBarBackgroundColor = BT_strings.getStyleValueForScreen(theScreenData, "navBarBackgroundColor", "#000000");
		
		//inflate this screens layout file..
		LayoutInflater vi = (LayoutInflater)theActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View titleView = vi.inflate(R.layout.act_title, null);
		
		//reference to titles container view, this is the LinearView we return as the navBar...
		LinearLayout navBar = (LinearLayout) titleView.findViewById(R.id.titleContainer);
		navBar.setBackgroundColor(BT_color.getColorFromHexString(navBarBackgroundColor));
		 		
		//reference to titleParts, this is a LinearLayout that holds the title text and possible right-button...
		RelativeLayout titleParts = (RelativeLayout) titleView.findViewById(R.id.titleParts);
		
		//apply background overlay the background color for a gradient affect...
		Drawable d = BT_fileManager.getDrawableByName("bt_bg_title.png");
		titleParts.setBackgroundDrawable(d);
		
		//reference to text view in act_title.xml file...
		TextView titleText = (TextView) titleView.findViewById(R.id.titleView);
		titleText.setText(navBarTitleText);
		
		//reference to right side button, unused if we are using an image...
		final Button rightButton = (Button) titleView.findViewById(R.id.rightButton);
				
		//reference to the right-side image view for a image, unused if we are using a button...
		final ImageView rightImage = (ImageView) titleView.findViewById(R.id.rightImage);
		//rightButtonView.setImageDrawable(BT_fileManager.getDrawableByName("bt_info_light.png"));
		
		//ignore all this if we are hiding the nav bar...
		if(navBarStyle.equalsIgnoreCase("hidden")){
			
			BT_debugger.showIt(objectName + ":getNavBarForScreen Hiding nav. bar for screen with nickname: \"" + theScreenData.getItemNickname() + "\"");
			navBar.setVisibility(View.GONE);
			return navBar;
			
		}else{	
	    	
			//button is either an icon or text, not both...
			String navBarButtonIcon = "";
			String navBarButtonText = "";
			
			//these button types show text...
			if(navBarRightButtonType.equalsIgnoreCase("next")) navBarButtonText = theActivity.getString(R.string.next);
			if(navBarRightButtonType.equalsIgnoreCase("done")) navBarButtonText = theActivity.getString(R.string.done);
			if(navBarRightButtonType.equalsIgnoreCase("cancel")) navBarButtonText = theActivity.getString(R.string.cancel);
			if(navBarRightButtonType.equalsIgnoreCase("edit")) navBarButtonText = theActivity.getString(R.string.edit);
			if(navBarRightButtonType.equalsIgnoreCase("save")) navBarButtonText = theActivity.getString(R.string.save);

			//these button types show an image...
			if(navBarRightButtonType.equalsIgnoreCase("details")) navBarButtonIcon = "bt_info_light.png";
			if(navBarRightButtonType.equalsIgnoreCase("home")) navBarButtonIcon = "bt_house.png";
			if(navBarRightButtonType.equalsIgnoreCase("infoLight")) navBarButtonIcon = "bt_info_light.png";
			if(navBarRightButtonType.equalsIgnoreCase("infoDark")) navBarButtonIcon = "bt_info_dark.png";
			if(navBarRightButtonType.equalsIgnoreCase("addBlue")) navBarButtonIcon = "bt_add.png";
			if(navBarRightButtonType.equalsIgnoreCase("add")) navBarButtonIcon = "bt_add.png";
			if(navBarRightButtonType.equalsIgnoreCase("compose")) navBarButtonIcon = "bt_compose.png";
			if(navBarRightButtonType.equalsIgnoreCase("reply")) navBarButtonIcon = "bt_reply.png";
			if(navBarRightButtonType.equalsIgnoreCase("action")) navBarButtonIcon = "bt_action.png";
			if(navBarRightButtonType.equalsIgnoreCase("organize")) navBarButtonIcon = "bt_box.png";
			if(navBarRightButtonType.equalsIgnoreCase("bookmark")) navBarButtonIcon = "bt_bookmark.png";
			if(navBarRightButtonType.equalsIgnoreCase("search")) navBarButtonIcon = "bt_search.png";
			if(navBarRightButtonType.equalsIgnoreCase("refresh")) navBarButtonIcon = "bt_refresh.png";
			if(navBarRightButtonType.equalsIgnoreCase("camera")) navBarButtonIcon = "bt_camera.png";
			if(navBarRightButtonType.equalsIgnoreCase("trash")) navBarButtonIcon = "bt_trash.png";
			if(navBarRightButtonType.equalsIgnoreCase("play")) navBarButtonIcon = "bt_play.png";
			if(navBarRightButtonType.equalsIgnoreCase("pause")) navBarButtonIcon = "bt_pause.png";
			if(navBarRightButtonType.equalsIgnoreCase("stop")) navBarButtonIcon = "bt_stop.png";
			if(navBarRightButtonType.equalsIgnoreCase("rewind")) navBarButtonIcon = "bt_rewind.png";
			if(navBarRightButtonType.equalsIgnoreCase("fastForward")) navBarButtonIcon = "bt_fastforward.png";
			
			//image or text for button...
	        if(navBarButtonText.length() > 1){
	        	
	        	//show button, hide image...
	        	rightButton.setVisibility(View.VISIBLE);
	        	rightImage.setVisibility(View.GONE);
 	        	
  	        	//button text and click handler...
	    		rightButton.setText(navBarButtonText);
	    		rightButton.setOnClickListener(new OnClickListener(){
	                public void onClick(View v){
        	    		BT_act_controller.handleRightNavButton(theActivity, theScreenData, theScreenData);
	                }
	            });
	        	
	        }else{
	        	
	        	//show button, hide image...
	        	rightButton.setVisibility(View.GONE);
	        	rightImage.setVisibility(View.VISIBLE);
	        	rightImage.setScaleType(ScaleType.CENTER);
	        	
	        	//button with image...
	        	if(navBarButtonIcon.length() > 1){
	        		
	                
	                Drawable d1 = BT_fileManager.getDrawableByName(navBarButtonIcon);
	                rightImage.setImageDrawable(d1);
	                rightImage.setOnClickListener(new OnClickListener(){
	                    public void onClick(View v){
	                    	
		                	//image animation...
	            			AlphaAnimation alphaFade = new AlphaAnimation(0.3f, 1.0f);
	            			alphaFade.setDuration(500);
	            			alphaFade.setFillAfter(true);
	            			rightImage.startAnimation(alphaFade);
	        	    		BT_act_controller.handleRightNavButton(theActivity, theScreenData, theScreenData);
	        	    		
	                    }
	                });
	         		
	         	}
	        	
	        	
	        	
	        }//right button text	

		}
		
		//return...
		return navBar;
		
	}
	
	//returns bottom tool bar for webViews...
	public static RelativeLayout getWebToolBarForScreen(final Activity theActivity, final BT_item theScreenData){
	
		BT_debugger.showIt(objectName + ":getWebToolBarForScreen with nickname: \"" + theScreenData.getItemNickname() + "\"");

		//buttons options...
		String showBrowserBarBack = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "showBrowserBarBack", "0");
		String showBrowserBarLaunchInNativeApp = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "showBrowserBarLaunchInNativeApp", "0");
		String showBrowserBarEmailDocument = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "showBrowserBarEmailDocument", "0");
		String showBrowserBarRefresh = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "showBrowserBarRefresh", "0");
		
		//only show the bar if we have a button..
		boolean showBar = false;
		
		
		//parent layout..
		RelativeLayout toolBar = new RelativeLayout(theActivity);
		RelativeLayout.LayoutParams toolBarParams = new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT, 65);
		toolBarParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
		toolBar.setLayoutParams(toolBarParams);
		toolBar.setBackgroundColor(Color.BLACK);
		
		//Linear layout holds all the parts...
		LinearLayout toolBarContainer = new LinearLayout(theActivity);
		toolBarContainer.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		toolBarContainer.setGravity(Gravity.CENTER_VERTICAL);
		
		Drawable d = BT_fileManager.getDrawableByName("bt_bg_title.png");
		toolBarContainer.setBackgroundDrawable(d);

		//Left Buttons: padding: left, top, right, bottom 
		LinearLayout leftButtons = new LinearLayout(theActivity);
		leftButtons.setOrientation(LinearLayout.HORIZONTAL);
		leftButtons.setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
		leftButtons.setGravity(Gravity.LEFT);

		
		//Right Buttons: padding: left, top, right, bottom 
		LinearLayout rightButtons = new LinearLayout(theActivity);
		rightButtons.setOrientation(LinearLayout.HORIZONTAL);
		rightButtons.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT));
		rightButtons.setGravity(Gravity.RIGHT);
		
		
		//showBrowserBack...
		if(showBrowserBarBack.equalsIgnoreCase("1")){
			showBar = true;
			final ImageView imgView = new ImageView(theActivity);
	        imgView.setScaleType(ScaleType.CENTER_INSIDE);
	        
			Drawable d1 = BT_fileManager.getDrawableByName("bt_prev.png");
			imgView.setImageDrawable(d1);
	        
	        imgView.setTag("btn_back");
	        imgView.setPadding(20, 15, 20, 15);
	        leftButtons.addView(imgView);
		}
		
		
		//launch native app...
		if(showBrowserBarLaunchInNativeApp.equalsIgnoreCase("1")){
			showBar = true;
			final ImageView imgView = new ImageView(theActivity);
	        imgView.setScaleType(ScaleType.CENTER);

			Drawable d1 = BT_fileManager.getDrawableByName("bt_action.png");
			imgView.setImageDrawable(d1);
			
	        imgView.setTag("btn_launchNativeApp");
	        imgView.setPadding(20, 15, 20, 15);
	        rightButtons.addView(imgView);
			
		}		
		
		//email document...
		if(showBrowserBarEmailDocument.equalsIgnoreCase("1")){
			showBar = true;
			final ImageView imgView = new ImageView(theActivity);
	        imgView.setScaleType(ScaleType.CENTER);

			Drawable d1 = BT_fileManager.getDrawableByName("bt_compose.png");
			imgView.setImageDrawable(d1);
			
	        imgView.setTag("btn_emailDocument");
	        imgView.setPadding(20, 15, 20, 15);
	        rightButtons.addView(imgView);
			
		}	
		
		//refresh...
		if(showBrowserBarRefresh.equalsIgnoreCase("1")){
			showBar = true;
			final ImageView imgView = new ImageView(theActivity);
	        imgView.setScaleType(ScaleType.CENTER);
	        
			Drawable d1 = BT_fileManager.getDrawableByName("bt_refresh.png");
			imgView.setImageDrawable(d1);
	        
			imgView.setTag("btn_refresh");
	        imgView.setPadding(20, 15, 20, 15);
	        rightButtons.addView(imgView);
			
		}
		
		//add button layouts...
        toolBarContainer.addView(leftButtons);
        toolBarContainer.addView(rightButtons);

		//add view, return
		toolBar.addView(toolBarContainer);
		
		if(showBar){
			return toolBar;
		}else{
			return null;
		}
	}
	
	
	//build a webView...
	public static WebView getWebViewForScreen(final Activity theActivity, final BT_item theScreenData){
		BT_debugger.showIt(objectName + ":getWebViewForScreen with nickname: \"" + theScreenData.getItemNickname() + "\"");

		WebView	webView = new WebView(theActivity);
		webView.setBackgroundColor(0);
		webView.setInitialScale(0);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setSupportZoom(true);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.getSettings().setPluginsEnabled(true);

        //return
		return webView;
	
		
	}
	
	
	//round corners of bitmap - return bitmap
	public static Bitmap getRoundedImage(Bitmap bitmap, int pixels){
		//BT_debugger.showIt(objectName + ":getRoundedImage rounding image with radius: " + pixels);
		Bitmap output = null;
		try{
			output = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Bitmap.Config.ARGB_8888);
			Canvas canvas = new Canvas(output);

	        final int color = 0xff424242;
	        final Paint paint = new Paint();
	        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
	        final RectF rectF = new RectF(rect);
	        final float roundPx = pixels;
	
	        paint.setAntiAlias(true);
	        canvas.drawARGB(0, 0, 0, 0);
	        paint.setColor(color);
	        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
	        
	        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
	        canvas.drawBitmap(bitmap, rect, rect, paint);
		
		}catch(Exception e){
			BT_debugger.showIt(objectName + ":convertToPixels EXCEPTION " + e.toString());
		}
		
		//return...
		return output;
    }
	
	//resize bitmap - return bitmap
	public static Bitmap getScaledBitmap(Bitmap image, int maxHeight, int maxWidth){
		//BT_debugger.showIt(objectName + ":getScaledBitmap setting max width: " + maxWidth + " max height: " + maxHeight);
		Bitmap scaledImage = null;
		try{
			int imgWidth = image.getWidth();
			int imgHeight = image.getHeight();
	
			//keep aspect ratio
			float scaleFactor = Math.min(((float) maxWidth) / imgWidth, ((float) maxHeight) / imgHeight);
	
			Matrix scale = new Matrix();
			scale.postScale(scaleFactor, scaleFactor);
			scaledImage = Bitmap.createBitmap(image, 0, 0, imgWidth, imgHeight, scale, false);
	
		}catch(Exception e){
			BT_debugger.showIt(objectName + ":getScaledBitmap EXCEPTION " + e.toString());
		}
		
		//return...
		return scaledImage;
    }	
	

	
	public static int convertToPixels(int theIntValue){
		//BT_debugger.showIt(objectName + ":convertToPixels");
		int ret = 0;
		try{
			//Resources r = cpmaandroid_appDelegate.getApplication().getResources();
			//float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, theIntValue, r.getDisplayMetrics());
			
			//this method is incomplete and unused. Look at the return value, it's returning the
			//save integer value that was passed in as an arugment!
			
			ret = (int)theIntValue;
		}catch(Exception e){
			BT_debugger.showIt(objectName + ":convertToPixels EXCEPTION " + e.toString());
		}	
		
		//return...
		return ret;
		
	}
	

}




