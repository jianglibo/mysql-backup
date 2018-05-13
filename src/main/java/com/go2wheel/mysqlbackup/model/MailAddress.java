package com.go2wheel.mysqlbackup.model;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotNull;

public class MailAddress extends BaseModel {
	
	@NotNull
	@Email
	private String email;
	
	private String description;
	
	@Override
	public String toString() {
		return String.format("[%s, %s]", email, description);
	}
	
	public MailAddress() {}
	
	public MailAddress(String email, String description) {
		this.email = email;
		this.description = description;
	}
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
