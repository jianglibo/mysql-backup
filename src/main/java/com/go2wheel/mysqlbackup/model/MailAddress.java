package com.go2wheel.mysqlbackup.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class MailAddress extends BaseModel {
	
	@NotNull
	@Email
	private String email;
	
	private String description;
	
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
}
