package com.temenos.interaction.core.hypermedia;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple reflection based ResourceFactory that is used by the generated Behaviour
 * class to construct runtime instances of our resources.
 * @author aphethean
 */
public class ResourceFactory {
	private final static Logger logger = LoggerFactory.getLogger(ResourceFactory.class);

	private Map<String, ResourceState> resources = new HashMap<String, ResourceState>();
	private ResourceLocatorProvider resourceLocatorProvider;
	
	@SuppressWarnings("unchecked")
	public ResourceState getResourceState(String name) {
		ResourceState rs = resources.get(name);
		if (rs == null) {
			try {
				Class<ResourceState> rsClass = (Class<ResourceState>) Class.forName(name + "ResourceState");
				Constructor<ResourceState> rsCtr = rsClass.getConstructor(ResourceFactory.class);
				rs = rsCtr.newInstance(this);
				resources.put(name, rs);
				// now initialise
				if (rs instanceof LazyResourceLoader) {
					((LazyResourceLoader) rs).initialise();
				}
			} catch (ClassNotFoundException e) {
				logger.error("An error occurred constructing ResourceState " + name, e);
			} catch (InstantiationException e) {
				logger.error("An error occurred constructing ResourceState " + name, e);
			} catch (IllegalAccessException e) {
				logger.error("An error occurred constructing ResourceState " + name, e);
			} catch (NoSuchMethodException e) {
				logger.error("An error occurred constructing ResourceState " + name, e);
			} catch (IllegalArgumentException e) {
				logger.error("An error occurred constructing ResourceState " + name, e);
			} catch (InvocationTargetException e) {
				logger.error("An error occurred constructing ResourceState " + name, e);
			}
			
		}
		
		return rs;
	}

	/**
	 * @return the resourceLocatorProvider
	 */
	public ResourceLocatorProvider getResourceLocatorProvider() {
		return resourceLocatorProvider;
	}

	/**
	 * @param resourceLocatorProvider the resourceLocatorProvider to set
	 */
	public void setResourceLocatorProvider(ResourceLocatorProvider resourceLocatorProvider) {
		this.resourceLocatorProvider = resourceLocatorProvider;
	}
	
	
	
}
