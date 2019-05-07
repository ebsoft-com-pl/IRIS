package com.temenos.interaction.commands.odata;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.Map;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.InlineCount;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.resources.OptionsQueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;
import com.temenos.interaction.core.command.InteractionProducerException;
import com.temenos.interaction.core.resource.CollectionResource;
import com.temenos.interaction.odataext.entity.MetadataOData4j;

public class GETEntitiesCommand extends AbstractODataCommand implements InteractionCommand {
	private final Logger logger = LoggerFactory.getLogger(GETEntitiesCommand.class);

	public GETEntitiesCommand(ODataProducer producer) {
		super(producer);
	}

	public GETEntitiesCommand(MetadataOData4j metadataOData4j, ODataProducer producer) {
		super(metadataOData4j, producer);
	}
	
	/* Implement InteractionCommand interface */

	@Override
	public Result execute(InteractionContext ctx) throws InteractionException {
		assert(ctx != null);
		assert(ctx.getCurrentState() != null);
		assert(ctx.getCurrentState().getEntityName() != null && !ctx.getCurrentState().getEntityName().equals(""));
		assert(ctx.getResource() == null);

		String entityName = getEntityName(ctx);
		logger.debug("Getting entities for " + entityName);
		try {
			EdmEntitySet entitySet = getEdmEntitySet(entityName);
			String entitySetName = entitySet.getName();
			EntitiesResponse response = producer.getEntities(entitySetName, getQueryInfo(ctx));
			    
			CollectionResource<OEntity> cr = CommandHelper.createCollectionResource(entitySetName, response.getEntities());
			cr.setInlineCount(response.getInlineCount());
			cr.setColumnValue(response.getSkipToken());
			cr.setQueryToken(response.getSkipToken());
			cr.setSkipToken(response.getSkipToken());
			ctx.setResource(cr);
		} catch (InteractionProducerException ipe) {
			if (logger.isDebugEnabled()) {
				logger.debug("GET entities on [" + entityName + ", " + ctx.getId() + "] failed: ", ipe.getMessage());
			}			
			ctx.setResource(ipe.getEntityResource());
			throw new InteractionException(ipe.getHttpStatus(), ipe);
		} catch (ODataProducerException ope) {
			logger.debug("GET entities on [" + entityName + ", " + ctx.getId() + "] failed: ", ope);
			throw new InteractionException(ope.getHttpStatus(), ope);
		} catch (InteractionException e) {
			throw e;
		}
		catch(Exception e) {
			logger.error("Failed to GET entities [" + entityName + ", " + ctx.getId() + "]: ", e);
			throw new InteractionException(Status.INTERNAL_SERVER_ERROR, e);
		}
		return Result.SUCCESS;
	}

	/*
	 * Obtain the odata query information from the context's query parameters
	 * @param ctx interaction context
	 * @return query details
	 */
	private QueryInfo getQueryInfo(InteractionContext ctx) throws InteractionException {
		MultivaluedMap<String, String> queryParams = ctx.getQueryParameters();
		String top = queryParams.getFirst("$top");
		String skip = queryParams.getFirst("$skip");
		String actionFilter = CommandHelper.getViewActionProperty(ctx, "filter");		//Filter defined as action property 
		String filter = queryParams.getFirst("$filter");								//Query filter
		if (actionFilter != null && !actionFilter.isEmpty()
				&& !actionFilter.contains("{") && !actionFilter.contains("}")) {
			filter = filter != null ? actionFilter + " and " + filter : actionFilter;
		}
		String orderBy = queryParams.getFirst("$orderby");
		String skipToken = queryParams.getFirst("$skiptoken");
		String expand = queryParams.getFirst("$expand");
		String actionSelect = CommandHelper.getViewActionProperty(ctx, "select");
        String select = queryParams.getFirst("$select");
        
        if (select == null 
                && actionSelect != null && !actionSelect.isEmpty()
                && !actionSelect.contains("{") && !actionSelect.contains("}")) {
            select = actionSelect;
        }
		
		Map<String, String> customOptions = CommandHelper.populateCustomOptionsMap(ctx);
		
		try {
			return new QueryInfo(
					validateAndGetInlineCount(queryParams),
					OptionsQueryParser.parseTop(top),
					OptionsQueryParser.parseSkip(skip),
					OptionsQueryParser.parseFilter(filter),
					OptionsQueryParser.parseOrderBy(orderBy),
					OptionsQueryParser.parseSkipToken(skipToken),
					customOptions,
					OptionsQueryParser.parseExpand(expand),
					OptionsQueryParser.parseSelect(select));
		} catch (RuntimeException e) {
			// all runtime exceptions are due to failure in parsing the query options
		    logger.error("Invalid query option in '" + queryParams + "'. Error: ", e);
			throw new InteractionException(Status.BAD_REQUEST,"Invalid query option in '" + queryParams + "'. Error: ", e);
		}
	}

	private InlineCount validateAndGetInlineCount(
			MultivaluedMap<String, String> queryParams)
			throws InteractionException {
		String inlineCountParam = queryParams.getFirst("$inlinecount");
		InlineCount inlineCount = OptionsQueryParser
				.parseInlineCount(inlineCountParam);
		if (inlineCountParam != null && inlineCount == null) {
			// an invalid non-null inline count parameter should fail with 400
			throw new InteractionException(Status.BAD_REQUEST,
					"Invalid inline count '" + inlineCountParam
							+ "' in query options '" + queryParams);
		}
		return inlineCount;
	}
}
