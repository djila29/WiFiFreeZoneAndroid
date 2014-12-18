package com.expample.model;

public class Network {
	private int networkID;
	private String SSID;
	private String password;
	private String latitude;
	private String longitude;
	private String validity;
	
	public Network () {}
	public Network (int netID, String ssid, String pass, String latitude, String longitude, String validity) {
		setNetworkID(netID);
		setSSID(ssid);
		setPassword(pass);
		setLatitude(latitude);
		setLongitude(longitude);
		setValidity(validity);
	}
	public int getNetworkID() {
		return networkID;
	}
	public void setNetworkID(int networkID) {
		this.networkID = networkID;
	}
	public String getSSID() {
		return SSID;
	}
	public void setSSID(String sSID) {
		SSID = sSID;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public String getLatitude() {
		return latitude;
	}
	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}
	public String getLongitude() {
		return longitude;
	}
	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}
	public String getValidity() {
		return validity;
	}
	public void setValidity(String validity) {
		this.validity = validity;
	}
}
