package com.temenos.interaction.sdk;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.List;

public class FieldInfo {

	private String name;
	private String type;
	private List<String> annotations;
	
	public FieldInfo(String name, String type, List<String> annotations) {
		this.name = name;
		this.type = type;
		this.annotations = annotations;
	}
	
	public String getName() {
		return name;
	}
	
	public String getType() {
		return type;
	}
	
	public List<String> getAnnotations() {
		return annotations;
	}
	
	public boolean equals(Object other) {
		if (other instanceof FieldInfo) {
			FieldInfo theOther = (FieldInfo) other;
			if ((theOther.getName() != null && this.name != null) && theOther.getName().equals(this.name)) {
				if ((theOther.getType() != null && this.type != null) && theOther.getType().equals(this.type)) {
					return true;
				} else {
					return false;
				}
			} else {
				return false;
			}
		}
		return false;
	}
	
	public int hashCode() {
		int hash = 0;
		if ( name != null ) hash = name.hashCode();
		if ( type != null ) hash += 4097 * type.hashCode();
		return hash;
	}
}
