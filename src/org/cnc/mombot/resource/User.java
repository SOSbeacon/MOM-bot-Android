package org.cnc.mombot.resource;

import com.google.gson.annotations.SerializedName;

public class User {
	public String id;
	
	public String email;
	
	@SerializedName("first_name")
	public String firstName;
	
	@SerializedName("last_name")
	public String lastName;
}
