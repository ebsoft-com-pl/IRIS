package com.temenos.interaction.core.hypermedia;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implements transformations from regular Java beans (POJOs)
 * @see {@link Transformer}
 * @author aphethean
 */
public class BeanTransformer implements Transformer {
	private final Logger logger = LoggerFactory.getLogger(BeanTransformer.class);
	
	static class ReservedProperty {
		private static final String[] ALL_RESERVED_PROPERTIES = {"CLASS"};
		
		private ReservedProperty() {}
		public static boolean contains(String value) {
			for (String s : ALL_RESERVED_PROPERTIES) {
				if (s.equalsIgnoreCase(value)) {
					return true;
				}
			}
			return false;
		}
	}
	
	/**
	 * @precondition entity not null
	 */
	@Override
	public Map<String, Object> transform(Object entity) {
		assert(entity != null);
		Map<String, Object> map = new HashMap<String, Object>();
		
		try {
			BeanInfo beanInfo = Introspector.getBeanInfo(entity.getClass());
			for (PropertyDescriptor propertyDesc : beanInfo.getPropertyDescriptors()) {
			    String propertyName = propertyDesc.getName();
			    if (!ReservedProperty.contains(propertyName)) {
			    	Method readMethod = propertyDesc.getReadMethod();
			    	if (readMethod != null) {
			    		Object value = readMethod.invoke(entity);
			    		map.put(propertyName, value);
			    	}
			    }
			}
		} catch (IllegalArgumentException e) {
			logger.error("Error accessing bean property", e);
		} catch (IntrospectionException e) {
			logger.error("Error accessing bean property", e);
		} catch (IllegalAccessException e) {
			logger.error("Error accessing bean property", e);
		} catch (InvocationTargetException e) {
			logger.error("Error accessing bean property", e);
		}
		return map;
	}

	/**
	 * This transformer will accept any object and push its properties
	 * {@see PropertyDescriptor} into the returned Map
	 */
	@Override
	public boolean canTransform(Object entity) {
		if (entity != null) {
			return true;
		}
		return false;
	}
}
