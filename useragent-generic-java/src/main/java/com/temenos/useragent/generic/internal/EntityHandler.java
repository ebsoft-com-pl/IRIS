package com.temenos.useragent.generic.internal;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import java.io.InputStream;
import java.util.List;

import com.temenos.useragent.generic.Link;

/**
 * Defines a handler for {@link Entity entity} so entities in different media
 * types are registered and handled in a generic way.
 * 
 * @author ssethupathi
 *
 */
public interface EntityHandler {

	/**
	 * Returns the id for from the underlying entity type.
	 * 
	 * @return id
	 */
	String getId();

	/**
	 * Returns the links from the underlying entity type.
	 * 
	 * @return links
	 */
	List<Link> getLinks();

	/**
	 * Returns the text value for the fully qualified property name which is
	 * part of the underlying entity content.
	 * <p>
	 * For entity types which do not have structured contents such as XML, json
	 * or with no content for the property will return empty string.
	 * </p>
	 * 
	 * @param fqPropertyName
	 * @return property value
	 */
	String getValue(String fqPropertyName);

	/**
	 * Sets the value for the fully qualified property name in the
	 * underlying entity.
	 * <p>
	 * This method on entity types which do not have structured contents such as
	 * XML, json or with no possible content for the property will have no
	 * effect.
	 * </p>
	 * 
	 * @param fqPropertyName
	 * @param value
	 */
	<T> void setValue(String fqPropertyName, T value);

	/**
	 * Returns the count for the fully qualified property name which is part of
	 * the underlying entity content.
	 * <p>
	 * For entity types which do not have structured contents such as XML, json
	 * or with no content for the property will return 0.
	 * </p>
	 * 
	 * @param fqPropertyName
	 * @return count
	 */

	int getCount(String fqPropertyName);

	/**
	 * Removes the property for the given fully qualified property name from the
	 * underlying entity content.
	 * 
	 * @param fqPropertyName
	 */
	void remove(String fqPropertyName);

	/**
	 * Sets the content for the underlying entity type.
	 * 
	 * @param stream
	 */
	void setContent(InputStream stream);

	/**
	 * Returns the content of the underlying entity.
	 * 
	 * @return content
	 */
	InputStream getContent();
	
	/**
	 * Returns the embedded payload of the underlying entity.
	 * 
	 * @return embedded payload or null if the underlying entity has no embedded
	 *         payload
	 */
	Payload embedded();

}
