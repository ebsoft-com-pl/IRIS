package com.temenos.interaction.example.hateoas.dynamic.model;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Preferences {

	@Id
	@Basic(optional = false)
	private String userID;
	private String currency;
	private String language;
		
	public Preferences() {}
	public Preferences(String userID, String currency, String language) {
		this.userID = userID;
		this.currency = currency;
		this.language = language;
	}

	public String getUserID() {
		return userID;
	}
	public String getCurrency() {
		return currency;
	}
	public String getLanguage() {
		return language;
	}

}