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



import android.app.Application;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;

import com.parse.Parse;


public class cpmaandroid_appDelegate extends Application{

	/*
		Debugging Output
		---------------------------------------------------------------------------
		Set showDebugInfo to TRUE to print details to the Android LogCat console.
		To see the Android LogCat Console, choose Window > Show View > Other > Android > LogCat 
		while the simulator or a connected device is running. Nearly every method has output 
		to show you details about how the program is executing. It looks like lots of 
		data (it is), but it's very useful for understanding how the application is behaving.
	*/
	
	public static boolean showDebugInfo = true;
	
	/*
		Application Configuration File / Data
		---------------------------------------------------------------------------
		One file holds all the configuration data associated with the application. This file must exist
		in the assets/ folder in the Eclipse project. This file is normally
		named BT_config.txt and can be read / edited with a normal text editor. If this configuration data
		uses a dataURL, a remote server will be polled for content changes. Changes will be downloaded and 
		saved locally. Once	this happens, the bt_config.txt file is no longer used and instead the application 
		refers to it's newly downloaded and cached data. In other words, if a dataURL is used then the 
		configuration file in the Eclipse project is only referenced so it can find the buzztouchAppId, buzztouchAPIKey,
		and dataURL. After that, it uses the data that was saved from the URL.  
		If no dataURL is provided, the file in the bundle will be read and parsed everytime the app is started.
	 */
	public static String configurationFileName = "BT_config.txt";
	
	//init the allowed input characters string. ONLY these characters will be allowed in input fields.
	public static String allowedInputCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789_-.@!$";

	//file names for apps cached configuration file
	public static String cachedConfigDataFileName = "cachedAppConfig.txt";
	public static String cachedConfigModifiedFileName = "appModified.txt";
	
	//references to the application object for use in other classes
	public static String delegateName = "cpmaandroid_appDelegate";
	public static Context theContext;
	public static Application theApplication;
	
	//media player for screen background sounds
	public MediaPlayer audioPlayer;	
	
	//array of sound effect names and matching sound effect MediaPlayers
    public static ArrayList<String> soundEffectNames = new ArrayList<String>();
	public static ArrayList<MediaPlayer> soundEffectPlayers = new ArrayList<MediaPlayer>();
	
	//root objects
	public static BT_application rootApp;
	
	//flag for locationManager in BT_activity_base
	public static boolean foundUpdatedLocation;
	
	//when the application is created...
    public void onCreate(){
        super.onCreate();
        
        //Parse Push Notification Stuff
		Parse.initialize(this, "61Pg4dFxYB7JRSaIcjqiJRpJxrS6HQBVJTFbWsUO", "KlNNy537eS7nxraoExpc5oYIITVyudHHgxLUYNAD"); 
        
        //debug
		BT_debugger.showIt("cpmaandroid_appDelegate: onCreate");

       //save a reference to the applications context
		theContext = this;
		theApplication = this;
		
		//flag locationManager
		foundUpdatedLocation = false;
		
        //init an audio player to play screen background sounds...
        initAudioPlayer();
	
        //load sound effects so they are prepared to play when needed...
        SoundEffectLoader soundEffectsLoader = new SoundEffectLoader();
        soundEffectsLoader.execute("", "");
        
 		//create the root app object.
		rootApp = new BT_application();
       
		/*
		 	*********************************************************************************
		 	*********************************************************************************
			BT_activity_root loads here. It's the starting activity (see AndroidManifest.xml)
		 	*********************************************************************************
		 	*********************************************************************************
		 */

		
    }
	
	//onLowMemory...
    public void onLowMemory(){
		BT_debugger.showIt("cpmaandroid_appDelegate: onLowMemory");
        super.onLowMemory();
    }
    
	//onTerminate...
    public void onTerminate(){
		BT_debugger.showIt("cpmaandroid_appDelegate: onTerminate");
        super.onTerminate();
    }
    
	//onTrimMemory...
    public void onTrimMemory(int level){
		BT_debugger.showIt("cpmaandroid_appDelegate: onTrimMemory with level: " + level);
    }    
	

    //return the application itself
    public static Application getApplication(){
    	return theApplication;
    }
    
    //return the applications context
    public static Context getContext(){
        return theContext;
    }	

	//init audio player
	public void initAudioPlayer(){
		BT_debugger.showIt(delegateName + ":loadAudioPlayer");			
		audioPlayer = null;
	}    
	
	//load audio with screen data..
	public void loadAudioForScreen(BT_item theScreenData){
		BT_debugger.showIt(delegateName + ":loadAudioForScreen with nickname: " + theScreenData.getItemNickname());	
		
		//do we have a file name for the audio?
		String audioFileName = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "audioFileName", "");
		String audioFileURL = BT_strings.getJsonPropertyValue(theScreenData.getJsonObject(), "audioFileURL", "");

		//if we have a local file...
		if(audioFileName.length() > 1){
			
			//use the cached version of audio if available..
			if(BT_fileManager.doesCachedFileExist(audioFileName)){
				
				try{

					//reset if it's already playing...
					if(audioPlayer != null) audioPlayer = null;
					audioPlayer = MediaPlayer.create(this, null);
					audioPlayer.setVolume(0.5f, 0.5f);
					audioPlayer.setDataSource(audioFileName);	
					audioPlayer.start();
					
				}catch(Exception e){
					BT_debugger.showIt(delegateName + ":loadAudioForScreen Exception loading an audio file from the cache: " + audioFileName + " ERROR: " + e.toString());
				}	
				
			}else{
				
				//if file exists in the project..
				String audioFilePath = "file://android_asset/BT_Audio/" + audioFileName;
				if(BT_fileManager.doesProjectAssetExist("BT_Audio", audioFileName)){
					try{
						
						//reset if it's already playing...
						if(audioPlayer != null) audioPlayer = null;
						audioPlayer = MediaPlayer.create(this, Uri.parse(audioFilePath));
						audioPlayer.setVolume(0.8f, 0.8f);
						audioPlayer.start();
						
					}catch(Exception e){
						BT_debugger.showIt(delegateName + ":loadAudioForScreen Exception loading an audio file included in the Android project: "  + audioFileName + " ERROR: " + e.toString());
					}
				}
				
			}
			
		}else{
			
			//play audio URL
			if(audioFileURL.length() > 1){
				
				try{
	
					//reset if it's already playing...
					if(audioPlayer != null) audioPlayer = null;
					audioPlayer = new MediaPlayer();
					audioPlayer.setVolume(0.5f, 0.5f);
					audioPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
					audioPlayer.setDataSource(audioFileURL);
					audioPlayer.prepare(); 
					audioPlayer.start();
					
				}catch(Exception e){
					BT_debugger.showIt(delegateName + ":loadAudioForScreen Exception loading an audio file from a URL: "  + audioFileURL + " ERROR: " + e.toString());
				}
			
			}
			
		}
		
		//if we had no audioFileName and no audioFileURL...
		if(audioFileName.length() < 1 && audioFileURL.length() < 1){
			BT_debugger.showIt(delegateName + ":loadAudioForScreen No audio name or URL for this screen.");
		}
		

	}	
	
	//Sound Effect Loader class so we can run an Async task...
	private class SoundEffectLoader extends AsyncTask<String, Void, String>{
		protected String doInBackground(String... theParams) {
			BT_debugger.showIt(delegateName + ":SoundEffectLoader:doInBackground" + " initSoundEffects");	
			BT_debugger.showIt(delegateName + ":SoundEffectLoader:doInBackground" + " initSoundEffects DISABLED");	
			String response = "";
			/*
			
			//add each sound effect to the list of...
			soundEffectNames.add("basso.mp3");
			soundEffectNames.add("blow.mp3");
			soundEffectNames.add("bottle.mp3");
			soundEffectNames.add("frog.mp3");
			soundEffectNames.add("funk.mp3");
			soundEffectNames.add("glass.mp3");
			soundEffectNames.add("hero.mp3");
			soundEffectNames.add("morse.mp3");
			soundEffectNames.add("ping.mp3");
			soundEffectNames.add("pop.mp3");
			soundEffectNames.add("purr.mp3");
			soundEffectNames.add("right.mp3");
			soundEffectNames.add("sosumi.mp3");
			soundEffectNames.add("submarine.mp3");
			soundEffectNames.add("tink.mp3");
			
			//create a MediaPlayer for each audio file...
			for(int i = 0; i < soundEffectNames.size(); i++){
				try{
					if(BT_fileManager.getResourceIdFromBundle("raw", soundEffectNames.get(i)) > 0){
						
						try {
							MediaPlayer mp = MediaPlayer.create(cpmaandroid_appDelegate.getContext(), BT_fileManager.getResourceIdFromBundle("raw", soundEffectNames.get(i)));														//mp.setDataSource(afd.getFileDescriptor()); 
						    mp.prepare();
							soundEffectPlayers.add(mp);
						}catch (IllegalStateException e) {
							BT_debugger.showIt("SoundEffectLoader: IllegalStateException: " + e.getMessage());
						}
						catch (IOException e) {
							BT_debugger.showIt("SoundEffectLoader: IOException: " + e.getMessage());
						}
						catch (IllegalArgumentException e) {
							BT_debugger.showIt("SoundEffectLoader: IllegalArgumentException: " + e.getMessage());
						}
						catch (SecurityException e) {
							BT_debugger.showIt("SoundEffectLoader: SecurityException: " + e.getMessage());
						}						

						
					}
				}catch(Exception e){
					BT_debugger.showIt(delegateName + ":SoundEffectLoader:loadSoundEffects Exception loading: "  + soundEffectNames.get(i) + " ERROR: " + e.toString());
				}
			}
			
			*/
			
			return response;
		}

	}	
	
	
	//play sound effect
	public static void playSoundEffect(String theFileName){
		BT_debugger.showIt(delegateName + ":playSoundEffect: " + theFileName);
		BT_debugger.showIt(delegateName + ":playSoundEffect: DISABLED");
		
		/*
		try{
			if(soundEffectNames.contains(theFileName)){
				soundEffectPlayers.get(soundEffectNames.indexOf(theFileName)).start();
			}
		}catch(Exception e){
			BT_debugger.showIt(delegateName + ":playSoundEffect: EXCEPTION " + e.toString());
		}
		*/
	}
	
	
	
	
}



















