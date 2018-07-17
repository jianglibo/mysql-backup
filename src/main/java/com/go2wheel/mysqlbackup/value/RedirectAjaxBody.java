package com.go2wheel.mysqlbackup.value;

public class RedirectAjaxBody {
	
	private boolean reload;
	
	private String redirect;
	
	public RedirectAjaxBody() {
	}
	
	public RedirectAjaxBody(String redirect) {
		this.redirect = redirect;
	}

	public RedirectAjaxBody(boolean reload) {
		this.reload = reload;
	}

	public String getRedirect() {
		return redirect;
	}

	public void setRedirect(String redirect) {
		this.redirect = redirect;
	}

	public boolean isReload() {
		return reload;
	}

	public void setReload(boolean reload) {
		this.reload = reload;
	}
	
	

}
