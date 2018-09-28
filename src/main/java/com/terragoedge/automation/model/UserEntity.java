package com.terragoedge.automation.model;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable(tableName = "User")
public class UserEntity {

	public static final String USER_ID = "userid";
	public static final String USER_NAME = "username";
	public static final String FIRST_NAME = "firstname";
	public static final String LAST_NAME = "lastname";
	public static final String EMAIL = "email";
	public static final String DEVICES = "devices";

	@DatabaseField(columnName = "userid")
	private int userId;
	@DatabaseField(columnName = "username")
	private String userName;
	@DatabaseField(columnName = "password")
	private String password;
	@DatabaseField(columnName = "firstname")
	private String firstName;
	@DatabaseField(columnName = "lastname")
	private String lastName;
	@DatabaseField(columnName = "email")
	private String email;
	@DatabaseField(columnName = "devices")
	private String devices;

	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getDevices() {
		return devices;
	}

	public void setDevices(String devices) {
		this.devices = devices;
	}
}
