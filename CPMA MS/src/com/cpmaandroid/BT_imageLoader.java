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
import java.io.IOException;
import java.util.HashMap;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;

public class BT_imageLoader {
	private final static String objectName = "BT_imageLoader";
	private static HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();
	
	//loadImage called from activity context...
	public static void loadImage(final Activity activity, final BT_item listItem, final ImageView imageView, final String url) {
		
		synchronized (cache) {
			if (cache.containsKey(url)) {
				imageView.setVisibility(View.VISIBLE);
				
				Drawable d = new BitmapDrawable(cache.get(url));
				imageView.setImageDrawable(d);
				
				
				//imageView.setImageBitmap(cache.get(url));
				//BT_debugger.showIt(objectName + " loading image from the cache...");
				return;
			}
		}
		
		(new Thread() {
			public void run() {
				final Bitmap bitmap;
				
				try {
					bitmap = loadImage(url, listItem);
				} catch (ClientProtocolException e) {
					return;
				} catch (IOException e) {
					return;
				}
				
				activity.runOnUiThread(new Runnable() {
					public void run() {
						//BT_debugger.showIt(objectName + " setting image on UI thread...");
						imageView.setVisibility(View.VISIBLE);
						imageView.setImageBitmap(bitmap);
					}
				});
				
				synchronized (cache) {
					cache.put(url, bitmap);
				}
			}
		}).start();
	}
	
	private static Bitmap loadImage(String url, BT_item listItem) throws ClientProtocolException, IOException {
		DefaultHttpClient client = new DefaultHttpClient();
		HttpResponse response = client.execute(new HttpGet(url));
		Bitmap image = BitmapFactory.decodeStream(response.getEntity().getContent());
		//BT_debugger.showIt(objectName + " loading image from " + url + "...");
		if (image == null) {
			BT_debugger.showIt(objectName + " downloaded image is null?");
			throw new IOException("Empty image.");
		}else{
			int listIconCornerRadius = Integer.parseInt(BT_strings.getStyleValueForScreen(listItem, "listIconCornerRadius", "0"));
			if(listIconCornerRadius < 1){
				return image;
			}else{
				return BT_viewUtilities.getRoundedImage(image, listIconCornerRadius);
			}
			
		}
		
	}
}







