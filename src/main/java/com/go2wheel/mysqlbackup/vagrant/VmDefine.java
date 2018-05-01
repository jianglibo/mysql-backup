package com.go2wheel.mysqlbackup.vagrant;

public class VmDefine {
	
	private String name;
	
	private String provider;
	
	private String box;
	
	private String network;
	private String ip;
	
	public void parseLine(String line) {
		
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getProvider() {
		return provider;
	}

	public void setProvider(String provider) {
		this.provider = provider;
	}

	public String getBox() {
		return box;
	}

	public void setBox(String box) {
		this.box = box;
	}

	public String getNetwork() {
		return network;
	}

	public void setNetwork(String network) {
		this.network = network;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}
	
//    config.vm.define "centos" do |centos|
//    centos.vm.provider "virtualbox" do |v|
//#    	  v.gui = true
//#      	  v.memory = 8192
//#      	  v.customize ['createmedium', '--filename',  'f:/vms/desktop', '--size', 100000]
//	end
//	centos.vm.box = "geerlingguy/centos7"
//	centos.vm.network "private_network", ip: "192.168.33.110"
//	end
}
