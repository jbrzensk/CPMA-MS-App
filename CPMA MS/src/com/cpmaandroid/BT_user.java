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

public class BT_user {
	
	private String objectName = "BT_user";
	private boolean isLoggedIn;
	private String userDisplayName = "";
	private String userId = "";
	private String userEmail = "";
	private String userLogInId = "";
	private String userLogInPassword = "";
	
	//constructor
	public BT_user(){
		BT_debugger.showIt(objectName + ": Creating root-user object.");
		
		//init vars...
		isLoggedIn = false;
		userDisplayName = BT_strings.getPrefString("userDisplayName");
		userId = BT_strings.getPrefString("userDisplayName");
		userEmail = BT_strings.getPrefString("userDisplayName");
		userLogInId = BT_strings.getPrefString("userDisplayName");
		userLogInPassword = BT_strings.getPrefString("userLogInPassword");
		
		//if we have an id in user preferences we are logged in...
		if(BT_strings.getPrefString("userId").length() > 1 || BT_strings.getPrefString("userGuid").length() > 1){
			isLoggedIn = true;
		}else{
			isLoggedIn = false;
		}
		
		
	}

	//getters // setters
	public boolean getIsLoggedIn() {
		return isLoggedIn;
	}
	
	public void setIsLoggedIn(boolean isLoggedIn) {
		this.isLoggedIn = isLoggedIn;
	}

	public String getUserId() {
		return userId;
	}


	public void setUserId(String userId) {
		this.userId = userId;
	}


	public String getUserEmail() {
		return userEmail;
	}


	public void setUserEmail(String userEmail) {
		this.userEmail = userEmail;
	}


	public String getUserLogInId() {
		return userLogInId;
	}


	public void setUserLogInId(String userLogInId) {
		this.userLogInId = userLogInId;
	}


	public String getUserLogInPassword() {
		return userLogInPassword;
	}

	public void setUserLogInPassword(String userLogInPassword) {
		this.userLogInPassword = userLogInPassword;
	}

	public String getUserDisplayName() {
		return userDisplayName;
	}


	public void setUserDisplayName(String userDisplayName) {
		this.userDisplayName = userDisplayName;
	}
	
	
	
	
}







