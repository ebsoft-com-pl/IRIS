package com.temenos.interaction.core.hypermedia;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


/**
 * A UriSpecification is responsible for providing template paths
 * according to a defined set of URI conventions for a service. 
 * @author aphethean
 */
public class UriSpecification {

	private String name;
	private String template;
	public UriSpecification(String name, String template) {
		this.name = name;
		this.template = template;
	}

	public String getName() { return name; };
	public String getTemplate() { return template; };
	
	
}
