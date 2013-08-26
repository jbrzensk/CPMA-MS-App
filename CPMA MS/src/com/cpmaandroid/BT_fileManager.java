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
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.util.ArrayList;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.drawable.Drawable;

public class BT_fileManager {

	
	public static String objectName = "BT_fileManager";
	
	//not all documents are cachable...
	public static boolean canCacheDocumentType(String theFileName){
		BT_debugger.showIt(objectName + ":canCacheDocument \"" + theFileName + "\"");
		boolean ret = false;
		if(theFileName.length() > 1){
			
			ArrayList<String> validList = new ArrayList<String>();
			validList.add(".htm");
			validList.add(".txt");
			validList.add(".pdf");
			validList.add(".xls");
			validList.add(".ppt");
			validList.add(".doc");
		
			//is the theFileName in our list?
			for(int x = 0; x < validList.size(); x++){
				if(theFileName.contains(validList.get(x))){
					ret = true;
					break;
				}
			}
		}
		return ret;
	}	
	
	//does file exist in projects assets directory? assetsDirectory will be "BT_Images" or "BT_Audio", etc...
	public static boolean doesProjectAssetExist(String assetsDirectory, String fileName){
        boolean ret = false;
        final AssetManager am = cpmaandroid_appDelegate.getApplication().getAssets();
        try{
        	String path = assetsDirectory;
        	String list[] = am.list(path);
        	if (list != null){
                for(int i = 0; i < list.length; ++i){
                	//BT_debugger.showIt("assets/" + path + "/" + list[i]);
                	if(list[i].equalsIgnoreCase(fileName)){
                		ret = true;
                		break;
                	}
                }
        	}
        	
            //delete local ref..
            list = null;
     
        }catch(Exception e){
    		BT_debugger.showIt(objectName + ":doesProjectAssetExist EXCEPTION " + e.toString());
        }
        
        
        //fix-up for output..
		if(assetsDirectory.length() > 1) assetsDirectory += "/";

		if(ret){
			//BT_debugger.showIt(objectName + ":doesProjectAssetExist: YES assets/" + assetsDirectory + fileName);
			return true;
		}else{
			BT_debugger.showIt(objectName + ":doesProjectAssetExist: NO assets/" + assetsDirectory + fileName);
			return false;
		}
	}
	

	//copies a file from assets to the app's cache..
	public static boolean copyAssetToCache(String assetsDirectory, String fileName){
		BT_debugger.showIt(objectName + ":copyAssetToCache: " + fileName);
		boolean ret = false;
		try{
        	
        	//copy from assets to internal cache...
		    FileOutputStream outputStream = cpmaandroid_appDelegate.getApplication().openFileOutput(fileName, Context.MODE_WORLD_READABLE);
		    InputStream inputStream = cpmaandroid_appDelegate.getApplication().getAssets().open(assetsDirectory + "/" + fileName);
		   
		    //copy data
		    byte[] buffer = new byte[8192];
		    int length;
		    while((length = inputStream.read(buffer)) > 0) {
		        outputStream.write(buffer, 0, length);
		    }
		    
		    //close the streams
		    inputStream.close();
		    outputStream.flush();
		    outputStream.close();	    				
        	
		    //all good
		    ret = true;
        	
        }catch(Exception e){
        	ret = false;
    		BT_debugger.showIt(objectName + ":copyAssetToCache :EXCEPTION " + e.toString());
        }
        
        //return...
        return ret;
  	}	
		
	//copies a file from the app's cache to the SDCARD..
	public static boolean copyFileFromCacheToSDCard(String fileName){
		BT_debugger.showIt(objectName + ":copyFileFromCacheToSDCard: " + fileName);
		boolean ret = false;
		try{
        	
		   	//copy from assets to internal cache...
			FileInputStream fin = cpmaandroid_appDelegate.getApplication().openFileInput(fileName);
            File file = new File(cpmaandroid_appDelegate.getApplication().getExternalCacheDir(), fileName);
    		//BT_debugger.showIt("ABSOLUTE PATH: " + file.getAbsolutePath());
            
            OutputStream fos = new FileOutputStream(file);
            byte[] data = new byte[fin.available()];
            fin.read(data);
            fos.write(data);

            //close...
            fin.close();
            fos.close();
            
            //all good
            ret = true;
      	
        }catch(Exception e){
        	ret = false;
    		BT_debugger.showIt(objectName + ":copyFileFromCacheToSDCard :EXCEPTION " + e.toString());
        }
        
        //return...
        return ret;
  	}		
	
	
	//returns a list of all the assets in a folder. Much faster to search an array than the file system!
	public static ArrayList<String> getAssetListFromDirectory(String assetsDirectory){
		ArrayList<String> assets = new ArrayList<String>();
		final AssetManager am = cpmaandroid_appDelegate.getApplication().getAssets();
        try{
        	String path = assetsDirectory;
        	String list[] = am.list(path);
        	if (list != null){
                for(int i = 0; i < list.length; ++i){
                	//BT_debugger.showIt("assets/" + path + "/" + list[i]);
                	if(list[i].length() > 3){
                		assets.add(list[i]);
                	}
                }
           }
		}catch(Exception e){
			BT_debugger.showIt(objectName + ":getAssetsInDirectory: EXCEPTION loading assets list from " + assetsDirectory);
        }
		
		//return
		return assets;
	
	}
	
	
	//does file exist in applications download cache?
	public static boolean doesCachedFileExist(String fileName){
		boolean ret = false;
		try{
			
			File file = cpmaandroid_appDelegate.getApplication().getFileStreamPath(fileName);
			if(file.exists()){
				ret = true;
			}
		}catch(Exception e){
			BT_debugger.showIt(objectName + ":doesCachedFileExist An exception occurred trying to find " + fileName + " in the applications download cache. Returning false.");
			ret = false;
		}
		
		if(ret){
			//BT_debugger.showIt(objectName + ":doesCachedFileExist YES \"" + fileName + "\" does exist in the downloaded cache");
			return true;
		}else{
			//BT_debugger.showIt(objectName + ":doesCachedFileExist NO \"" + fileName + "\" does not exist in the downloaded cache");
			return false;
		}
		
	}	
	
	//returns resource id from projects bundle
	public static int getResourceIdFromBundle(String resourcesDirectory, String fileName){
		int ret = 0;
		try{
			//remove the file extension form the file name (not used when working with resources)
			String useResName = BT_strings.removeExtension(fileName);
			
			//make sure filename is a string...
			useResName = "" + useResName;
			ret = cpmaandroid_appDelegate.getApplication().getResources().getIdentifier(useResName, resourcesDirectory, cpmaandroid_appDelegate.getApplication().getPackageName());

		}catch(Exception e){
			BT_debugger.showIt(objectName + ": An exception occurred trying to find the resource id for: " + fileName);
			ret = 0;
		}
		return ret;
	}
	
	//returns drawable by name
	public static Drawable getDrawableByName(String fileName){
		Drawable d = null;
		try{
			//remove the file extension form the file name (not used when working with resources)
			String useResName = BT_strings.removeExtension(fileName);
			
			//make sure filename is a string...
			useResName = "" + useResName;
			int resId = cpmaandroid_appDelegate.getApplication().getResources().getIdentifier(useResName, "drawable", cpmaandroid_appDelegate.getApplication().getPackageName());
			d = cpmaandroid_appDelegate.getApplication().getResources().getDrawable(resId);

		}catch(Exception e){
			BT_debugger.showIt(objectName + ":getDrawableFromResourcesByName An exception occurred trying to get the drawable named: " + fileName);
			d = null;
		}
		return d;
	}
	
	
	//get image (Bitmap) from URL...
	public static Bitmap getImageFromURL(String theUrl){
		BT_debugger.showIt(objectName + ": getImageFromURL: " + theUrl);
		Bitmap ret = null;
		try{
    		//download the bitmap...
            ret = BitmapFactory.decodeStream((InputStream)new URL(theUrl).getContent());
		}catch (Exception je){
			BT_debugger.showIt(objectName + ": An exception occurred trying to get an image from a URL: " + theUrl);
		}
		return ret;
	}
	
		
	//get image (drawable) from cache...
	public static Drawable getDrawableFromCache(String fileName){
		Drawable d = null;
		try{
			FileInputStream fin = cpmaandroid_appDelegate.getApplication().openFileInput(fileName);
			if(fin != null){
				d = Drawable.createFromStream(fin, null);
				fin.close();
			}else{
				BT_debugger.showIt(objectName + ":getDrawableFromCache Could not find \"" + fileName + "\" in application's cache directory");
			}
		}catch(Exception c){
			BT_debugger.showIt(objectName + ":getDrawableFromCache An exception occurred trying to get an image from the cache: " + fileName);
		}
		return d;
		
	}
	
	//get image (Bitmap) from cache...
	public static Bitmap getBitmapFromCache(String fileName){
		Bitmap b = null;
		try{
			FileInputStream fin = cpmaandroid_appDelegate.getApplication().openFileInput(fileName);
			if(fin != null){
				b = BitmapFactory.decodeStream(fin);
				fin.close();
			}else{
				BT_debugger.showIt(objectName + ":getBitmapFromCache Could not find \"" + fileName + "\" in application's cache directory");
			}
		}catch(Exception c){
			BT_debugger.showIt(objectName + ":getBitmapFromCache An exception occurred trying to get an image from the cache: " + fileName);
		}
		return b;
		
	}	
	
	//save image to cache
	public static void saveImageToCache(Bitmap theImage, String saveAsFileName){
		BT_debugger.showIt(objectName + ": saveImageToCache: " + saveAsFileName);
		try{
			if(saveAsFileName.length() > 3 && theImage != null){
				FileOutputStream fos = cpmaandroid_appDelegate.getApplication().openFileOutput(saveAsFileName, Context.MODE_WORLD_READABLE);
				theImage.compress(CompressFormat.JPEG, 100, fos);
				fos.flush();
				fos.close();
			}
		}catch (Exception e) {
			BT_debugger.showIt(objectName + ": An exception occurred trying to save an image to the cache? " + e.toString());
		}
	}
	
	//save file to cache
	public static void saveTextFileToCache(String theData, String saveAsFileName){
		BT_debugger.showIt(objectName + ": saveTextFileToCache: " + saveAsFileName);
		try{
			if(saveAsFileName.length() > 3){
				OutputStreamWriter out = new OutputStreamWriter(cpmaandroid_appDelegate.getApplication().openFileOutput(saveAsFileName, Context.MODE_WORLD_READABLE));
				out.write(theData);
				out.close();
			}
		}catch (Exception e) {
			BT_debugger.showIt(objectName + ": An exception occurred trying to save a text file to the cache? " + e.toString());
		}
	}	

	
	//get text file contents from project assets... 
	public static String readTextFileFromAssets(String assetsDirectory, String fileName){
		BT_debugger.showIt(objectName + ": readTextFileFromAssets: \"" + assetsDirectory + "/" + fileName + "\"");
		String ret = "";
		final AssetManager am = cpmaandroid_appDelegate.getApplication().getResources().getAssets();
		InputStream is = null;
		try{
			if(assetsDirectory.length() < 1){
				is = am.open(fileName);
			}else{
				is = am.open(assetsDirectory + "/" + fileName);
			}
	    	
			if(is != null){
				BufferedReader r = new BufferedReader(new InputStreamReader(is), 8000);
				StringBuilder total = new StringBuilder();
				String line;
				while ((line = r.readLine()) != null) {
					total.append(line);
				}
				ret = total.toString();
			}
			
			
		}catch(Exception e){
			if(assetsDirectory.length() > 1) assetsDirectory += "/";
			BT_debugger.showIt(objectName + ":readTextFileFromAssets: EXCEPTION reading text file from assets/" + assetsDirectory + fileName + " Error: " + e.toString());
          }	
		
		//return
		return ret;

	}	
	
	//get text file contents from cache...
	public static String readTextFileFromCache(String fileName){
		BT_debugger.showIt(objectName + ": readTextFileFromCache: \"" + fileName + "\"");
		String ret = "";
    	if(fileName.length() > 1){
    		try{
    	    	FileInputStream fin = cpmaandroid_appDelegate.getApplication().openFileInput(fileName);
    			if(fin != null){
    				BufferedReader r = new BufferedReader(new InputStreamReader(fin), 8000);
    				StringBuilder total = new StringBuilder();
    				String line;
    				while ((line = r.readLine()) != null) {
    					total.append(line);
    				}
    				ret = total.toString();
    			}    	    	
      			
    		}catch (Exception je){
    			ret = "";
    			BT_debugger.showIt(objectName + ": ERROR 2. An exception occurred trying to read " + fileName + " from the cache.");
        	}
    	}else{
    		ret = "";
    	}
    	return ret;
	}
	
	//get size of cached data directory..
	public static int getCachedDataSize(){
		BT_debugger.showIt(objectName + ":getCachedDataSize");
		int ret = 0;
		try{
			String appDataDirectory = cpmaandroid_appDelegate.getApplication().getFilesDir().getAbsolutePath();
			if(appDataDirectory.length() > 3){
	    		try{
	    			File[] prefFiles = new File(appDataDirectory).listFiles();
	    			for (File f : prefFiles){
	    				if (f.isFile()){
	    					ret += f.length();
	    				}    				
	    			}			
	    		}catch(Exception e){
					BT_debugger.showIt(objectName + ":deleteAllCachedData Excpetion: " + e.toString());
	    		}
			}			
			
		}catch (Exception e){
			ret = 0;
			BT_debugger.showIt(objectName + ": EXCEPTION " + e.toString());
		}
		
		//return
		return ret;
	}
	
		
	//deletes one cached file...
	public static void deleteFile(String fileName){
		BT_debugger.showIt(objectName + ":deleteFile " + fileName);
		
		File file = cpmaandroid_appDelegate.getApplication().getFileStreamPath(fileName);
		if(file.exists()){
			BT_debugger.showIt(objectName + ":deleteFile deleting " + fileName);
			file.delete();
		}else{
			//BT_debugger.showIt(objectName + ":deleteFile " + fileName + " does not exist");
		}
		
	}
	
	//deletes all cached files...
	public static void deleteAllCachedData(String skipFile){
		BT_debugger.showIt(objectName + ": deleting application cache");
		String appDataDirectory = cpmaandroid_appDelegate.getApplication().getFilesDir().getAbsolutePath();
		BT_debugger.showIt(objectName + ": deleting application cache in: " + appDataDirectory);
		
		if(appDataDirectory.length() > 3){
    		try{
    			File[] prefFiles = new File(appDataDirectory).listFiles();
    			for (File f : prefFiles){
    				
    				boolean deleteFile = true;
    				
    				//do not delete a possible skipFile argument...
    				if(f.getName().equalsIgnoreCase(skipFile)){
    					deleteFile = false;
    				}
    				
    				//do not delete possible "persisted" data files...
    				if(f.getName().startsWith("persist_")){
    					deleteFile = false;
    				}
    				
    				//delete file?
    				if(deleteFile){
    					BT_debugger.showIt(objectName + ": deleting: \"" + f.getName() + "\"");
    					f.delete();
    				}else{
    					BT_debugger.showIt(objectName + ": NOT deleting (persisted): \"" + f.getName() + "\"");
    				}
    				
    			}	
    		}catch(Exception e){
				BT_debugger.showIt(objectName + ":deleteAllCachedData Excpetion: " + e.toString());
    		}
		}
	}
	
	
	
}










