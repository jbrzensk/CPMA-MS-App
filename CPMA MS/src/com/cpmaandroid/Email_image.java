/*
 *	Copyright 2012, Mark S Fleming
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
import java.io.FileOutputStream;
import java.util.ArrayList;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;


public class Email_image extends BT_activity_base{
	private boolean didCreate = false;
	private ImageView imageViewForEmailedImage;
	private String tempImageFileName = "";
	private String pathToImage = "";
    Context context = null;
    Dialog optionsDialog = null;
    private Bitmap selectedImage;

    
    //email vars...
    String emailToAddress = "";
    String emailSubject = "";
    
    //////////////////////////////////////////////////////////////////////////
	//activity life-cycle events.

	//onCreate
	@Override
    public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
        this.activityName = "Email_image";
		BT_debugger.showIt(activityName + ":onCreate");	
		
		//reference to base layout..
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
		
		//inflate this screens layout file...
		LayoutInflater vi = (LayoutInflater)thisActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View thisScreensView = vi.inflate(R.layout.screen_email_image, null);
		
		//add the view to the base view...
		baseView.addView(thisScreensView);
	     
		//reference to imageView that holds the image to send in email...
		imageViewForEmailedImage = (ImageView) thisScreensView.findViewById(R.id.imageViewForEmailedImage);

		//set the "no image selected" image...
		imageViewForEmailedImage.setScaleType(ScaleType.CENTER);
		
		//add click listener to image so user can always option the options panel...
		imageViewForEmailedImage.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	showImageOptions();
            }
        }); 
		
		//build image file name and path (saved on SDK card until we email it)...
        tempImageFileName = "img_" + this.screenData.getItemId() + ".png";
		File folder = new File(Environment.getExternalStorageDirectory() + "/emailImage");
        if(!folder.exists()){
            folder.mkdir();
        }    
        pathToImage = Environment.getExternalStorageDirectory() +"/emailImage/" + tempImageFileName;
		
        //remember thi context...
        context = Email_image.this;
        
        //file JSON vars...
        emailToAddress = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "emailToAddress", "");
        emailSubject = BT_strings.getJsonPropertyValue(this.screenData.getJsonObject(), "emailSubject", "");

         //flag as created..
        didCreate = true;
        
 		
	}//onCreate

	//onStart
	@Override 
	protected void onStart() {
		//BT_debugger.showIt(activityName + ":onStart");	
		super.onStart();
	}
	
    //onResume
    @Override
    public void onResume() {
       super.onResume();
       	//BT_debugger.showIt(activityName + ":onResume");
		
       //verify onCreate already ran...
       if(didCreate){
 
       }
       
       //if we did this before, set the image...
       File file = new File(pathToImage);
        if(file.exists()){
      		Bitmap bitmap = BitmapFactory.decodeFile(pathToImage);
      		selectedImage = bitmap;
        	updateImageView(bitmap);
        }else{
           	Drawable d = BT_fileManager.getDrawableByName("emailerpickimage.png");
        	imageViewForEmailedImage.setImageDrawable(d);
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
		
		//free up memory from selected image...
		if(selectedImage != null) {
			selectedImage.recycle();
			selectedImage = null;
			System.gc();
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
 
	//selectImage from gallery...
    public void selectImage(){
        BT_debugger.showIt(activityName + ":selectImage");	
        if(optionsDialog.isShowing()){
        	optionsDialog.dismiss();
        }
        
        //launch built in image gallery...
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        
        //pass a "1" so we can determine where we came from after user chooses existing photo...
        startActivityForResult(intent, 1);

    }
    
    //takeNewImage...
    public void takeNewImage(){
        BT_debugger.showIt(activityName + ":takeNewImage");	
        if(optionsDialog.isShowing()){
        	optionsDialog.dismiss();
        }
        
         //launch camera intent...
    	Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
    	File photo = new File(pathToImage); 
    	intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photo));
    
    	//pass a "2" so we can determine where we came from after user snaps new photo...
    	startActivityForResult(intent, 2);
        	
        
    }    
    
    //emailImage...
    public void emailImage(){
        BT_debugger.showIt(activityName + ":emailImage");
        if(optionsDialog.isShowing()){
        	optionsDialog.dismiss();
        }
        
        //does image exist?
		File file = new File(pathToImage);
        if(file.exists()){
 
	        //launch native Email Compose Sheet with image attached.
	        Intent intent = new Intent(Intent.ACTION_SEND);
	        if(emailToAddress.length() > 0){
	        	intent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] {emailToAddress});
	        }
	        if(emailSubject.length() > 0){
	        	intent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
	        }
	        
	        //attach file to email...
	        intent.setType("image/png");
	        intent.putExtra(Intent.EXTRA_STREAM, Uri.parse("file://" + pathToImage));

	        //start activity...
	        startActivity(Intent.createChooser(intent, "Email:"));
        
        }else{
        	showAlert("No Image?", "Please select or take an image to email");
        }
    }     
    
    //show image options...
    public void showImageOptions(){
        BT_debugger.showIt(activityName + ":showImageOptions");	
        
        //dismiss if it's already showing...
        if(optionsDialog != null){
        	if(optionsDialog.isShowing()){
        		optionsDialog.dismiss();
        	}
        }
        
        //setup the new options dialog...
        optionsDialog = new Dialog(this);

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
		btnCancel.setText(getString(R.string.close));
		btnCancel.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	optionsDialog.cancel();
            }
        });
		options.add(btnCancel);

		//email image option if an image has been selected...
		if(selectedImage != null){

			final Button btnClearPhoto = new Button(this);
			btnClearPhoto.setText("Clear Photo");
			btnClearPhoto.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
					optionsDialog.cancel();
					deletePhotos();
				}
			});
			options.add(btnClearPhoto);
			
			
			final Button btnEmailImage = new Button(this);
			btnEmailImage.setText("Email Photo");
			btnEmailImage.setOnClickListener(new OnClickListener(){
				public void onClick(View v){
					optionsDialog.cancel();
					emailImage();
				}
			});
			options.add(btnEmailImage);
		}
		
		//take new photo...
		final Button btnTakeNew = new Button(this);
		btnTakeNew.setText("Take New Photo");
		btnTakeNew.setOnClickListener(new OnClickListener(){
			public void onClick(View v){
				optionsDialog.cancel();
				takeNewImage();
			}
		});
		options.add(btnTakeNew);
		
		//select existing photo...
		final Button btnSelectExisting = new Button(this);
		btnSelectExisting.setText("Select from Gallery");
		btnSelectExisting.setOnClickListener(new OnClickListener(){
            public void onClick(View v){
            	optionsDialog.cancel();
            	selectImage();
            }
        });
		options.add(btnSelectExisting);
		
		//add each option to layout, set layoutParams as we go...
		for(int x = 0; x < options.size(); x++){
			options.get(x).setLayoutParams(btnLayoutParams);
			options.get(x).setPadding(5, 5, 5, 5);
			optionsView.addView(options.get(x));
		}
		
	
		//set content view..        
		optionsDialog.setContentView(optionsView);
        if(options.size() > 1){
        	optionsDialog.setTitle(getString(R.string.menuOptions));
        }else{
        	optionsDialog.setTitle(getString(R.string.menuNoOptions));
        }
        
        //show
        optionsDialog.show();
        
        
    }
    
    //this method fires after user chooses an existing photo or snaps a new one...
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data){
        BT_debugger.showIt(activityName + ":onActivityResult requestCode: " + requestCode + " resultCode: " + resultCode);	
    	super.onActivityResult(requestCode, resultCode, data);
        if(requestCode > 0){
        
 	    	//photo from gallery...
	        if(requestCode == 1){
		    	Uri photoUri = data.getData();
		        if (photoUri != null){
		            
	                try{
	                	selectedImage = MediaStore.Images.Media.getBitmap(this.getContentResolver(), photoUri);
	                	
			            BT_debugger.showIt(activityName + ":onActivityResult Saving selected image...");	
	                	
			            try {
			                FileOutputStream out = new FileOutputStream(pathToImage);
			                selectedImage.compress(Bitmap.CompressFormat.PNG, 90, out);
			            } catch (Exception e) {
				            BT_debugger.showIt(activityName + ":onActivityResult Exception saving image: " + e.toString());	
			            }
			            
	                } catch (Exception e) {
			            BT_debugger.showIt(activityName + ":onActivityResult Error loading selected image");	
	                }
	                
		        }else{	        	
		            
		            //path to saved image after selecting...
		            BT_debugger.showIt(activityName + ":onActivityResult Path after selection is NULL");	

		        }   
		    
	    	}
	    	
	    	//photo from camera...
	        if(requestCode == 2 && resultCode != 0){
	        	
	             
	            //load and remember image...
	            selectedImage = BitmapFactory.decodeFile(pathToImage);       

	            //update view...
	            updateImageView(selectedImage);
	        
	        }
	    
        }else{
    		Drawable d = BT_fileManager.getDrawableByName("emailerpickimage.png");
    		imageViewForEmailedImage.setImageDrawable(d);
            BT_debugger.showIt(activityName + ":onActivityResult Result Code is 0");	
        }//result code = 0
    }    
    
    //delete photos (clean up)...
    public void deletePhotos(){
        BT_debugger.showIt(activityName + ":deletePhotos");	
        selectedImage = null;
        String folder=Environment.getExternalStorageDirectory() +"/emailImage";
        File f = new File(folder);
        if(f.isDirectory()){
            File[] files = f.listFiles();
            for(int i=0; i<files.length; i++){
                String fpath = folder+File.separator+files[i].getName().toString().trim();
                File nf = new File(fpath);
                if(nf.exists()){
                    nf.delete();
                }
            }
        }
        
        //update image view...
        updateImageView(null);
        
    }
    
    
 
    
    //set image after loading...
    public void updateImageView(Bitmap imageToShow){
        BT_debugger.showIt(activityName + ":updateImageView");	
         
        //do we have an image?
        if(imageToShow != null){
        	imageViewForEmailedImage.setImageBitmap(imageToShow);
        }else{
       		Drawable d = BT_fileManager.getDrawableByName("emailerpickimage.png");
    		imageViewForEmailedImage.setImageDrawable(d);
        }
        
    }
    
    
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
			options.get(x).setLayoutParams(btnLayoutParams);
			options.get(x).setPadding(5, 5, 5, 5);
			optionsView.addView(options.get(x));
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


 


