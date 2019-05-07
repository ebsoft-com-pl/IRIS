package com.temenos.interaction.sdk.command;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.ArrayList;
import java.util.List;

/**
 * This class holds information about an IRIS command
 */
public class Command {
	private String id;
	private String className;
	private List<Parameter> parameters = new ArrayList<Parameter>();

	public Command(String id, String className) {
		this.id = id;
		this.className = className;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	public String getParameterName(int i) {
		return parameters.get(i).getValue();
	}

	public boolean isParameterByReference(int i) {
		return parameters.get(i).isByReference();
	}

	public List<Parameter> getParameters() {
		return parameters;
	}
	
	public void addParameter(String value, boolean isByReference, String refId) {
		parameters.add(new Parameter(value, isByReference, refId));
	}

	public void addParameter(Parameter parameter) {
		parameters.add(parameter);
	}
}
