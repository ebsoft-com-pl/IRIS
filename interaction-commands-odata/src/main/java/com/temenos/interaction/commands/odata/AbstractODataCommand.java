package com.temenos.interaction.commands.odata;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import javax.ws.rs.core.Response.Status;

import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.producer.ODataProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;
import com.temenos.interaction.core.hypermedia.Action;
import com.temenos.interaction.odataext.entity.MetadataOData4j;

public abstract class AbstractODataCommand {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractODataCommand.class);
	/**
	 * Use this property to configure an action to use this entity 
	 * instead of the entity specified for the Resource.
	 */
	public final static String ENTITY_PROPERTY = "entity";
	protected ODataProducer producer; 
	private MetadataOData4j metadataOData4j;
	
	public AbstractODataCommand(ODataProducer producer) {
		this.producer = producer;
	}

	public AbstractODataCommand(MetadataOData4j metadataOData4j, ODataProducer producer) {
		this.producer = producer;
		this.metadataOData4j = metadataOData4j;
	}
	
	public String getEntityName(InteractionContext ctx) {
		String entityName = ctx.getCurrentState().getEntityName();
		// TODO improve this naive implementation, only using properties from first action
		Action action = null;
		if (ctx.getCurrentState().getActions().size() > 0)
			action = ctx.getCurrentState().getActions().get(0);
		
		if (action != null && action.getProperties() != null && action.getProperties().getProperty(ENTITY_PROPERTY) != null) {
			entityName = action.getProperties().getProperty(ENTITY_PROPERTY);
		}
		return entityName;
	}
	
	protected EdmDataServices getEdmMetadata() {
		return producer.getMetadata();
	}
	
	/**
	 * get EdmEntitySet from EdmDataServices
	 * @param entityName
	 * @return EdmEntitySet
	 * @throws Exception
	 */
	public EdmEntitySet getEdmEntitySet(String entityName) throws Exception {
		// We should try to get EdmEntitySet from MetadataOdata4j
		if (metadataOData4j != null) {
			try {
				EdmEntitySet entitySet = metadataOData4j.getEdmEntitySetByEntityName(entityName);
				if( null == entitySet ) {
					throw new Exception("Entity type does not exist");
				}
				return entitySet;
			} catch (Exception e) {
				throw new InteractionException(Status.INTERNAL_SERVER_ERROR,"Error retrieving metadata for entity" + entityName, e);
			}
		} else { // We fall back to default way of looking at EdmEntitySet
			return CommandHelper.getEntitySet(entityName, getEdmMetadata());
		}
	}

	/**
	 * get EdmEntitySetName from EdmEntitySet
	 * @param entityName
	 * @return entityName
	 * @throws Exception
	 */
	public String getEdmEntitySetName(String entityName) throws Exception {
		try {
			return getEdmEntitySet(entityName).getName();
		} catch (NotFoundException notFoundException) {
		    LOGGER.error("Entity not found.", notFoundException);
			throw notFoundException;
		}
	}
}
