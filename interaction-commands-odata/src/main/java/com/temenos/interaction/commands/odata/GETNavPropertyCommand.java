package com.temenos.interaction.commands.odata;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.List;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.CountResponse;
import org.odata4j.producer.EntitiesResponse;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.PropertyResponse;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.resources.OptionsQueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;

public class GETNavPropertyCommand extends AbstractODataCommand implements InteractionCommand {
	private final Logger logger = LoggerFactory.getLogger(GETNavPropertyCommand.class);

	public GETNavPropertyCommand(ODataProducer producer) {
		super(producer);
	}

	/* Implement InteractionCommand interface */
	
	@Override
	public Result execute(InteractionContext ctx) throws InteractionException {
		assert(ctx != null);
		assert(ctx.getCurrentState() != null);
		assert(ctx.getCurrentState().getViewAction() != null);
		
		String entity = CommandHelper.getViewActionProperty(ctx, "entity"); 
		if(entity == null) {
			throw new InteractionException(Status.BAD_REQUEST, "'entity' must be provided");		
		}

		//Obtain the navigation property
		String navProperty = CommandHelper.getViewActionProperty(ctx, "navproperty"); 
		if(navProperty == null) {
			throw new InteractionException(Status.INTERNAL_SERVER_ERROR, "Command must be bound to an OData navigation property resource");	
		}
		
		//Create entity key (simple types only)
		OEntityKey key;
		try {
			key = CommandHelper.createEntityKey(getEdmMetadata(), entity, ctx.getId());
		} catch(Exception e) {
			throw new InteractionException(Status.INTERNAL_SERVER_ERROR, e);	
		}

		MultivaluedMap<String, String> queryParams = ctx.getQueryParameters();
		QueryInfo query = null;
		if (queryParams != null) {
			String inlineCount = queryParams.getFirst("$inlinecount");
			String top = queryParams.getFirst("$top");
			String skip = queryParams.getFirst("$skip");
			String filter = queryParams.getFirst("$filter");
			String orderBy = queryParams.getFirst("$orderby");
	// TODO what are format and callback used for
//			String format = queryParams.getFirst("$format");
//			String callback = queryParams.getFirst("$callback");
			String skipToken = queryParams.getFirst("$skiptoken");
			String expand = queryParams.getFirst("$expand");
			String select = queryParams.getFirst("$select");

			query = new QueryInfo(
					OptionsQueryParser.parseInlineCount(inlineCount),
					OptionsQueryParser.parseTop(top),
					OptionsQueryParser.parseSkip(skip),
					OptionsQueryParser.parseFilter(filter),
					OptionsQueryParser.parseOrderBy(orderBy),
					OptionsQueryParser.parseSkipToken(skipToken),
					null,
					OptionsQueryParser.parseExpand(expand),
					OptionsQueryParser.parseSelect(select));
		}

		CountResponse count = producer.getNavPropertyCount(entity, key, navProperty, query);
		if (count != null && count.getCount() > 0) {
			BaseResponse response = producer.getNavProperty(entity, key, navProperty, query);

			if (response instanceof PropertyResponse) {
				logger.error("We don't currently support the ability to get an item property");
			} else if (response instanceof EntityResponse) {
	        	OEntity oe = ((EntityResponse) response).getEntity();
	        	ctx.setResource(CommandHelper.createEntityResource(oe));
	        	return Result.SUCCESS;
	        } else if (response instanceof EntitiesResponse) {
	        	List<OEntity> entities = ((EntitiesResponse) response).getEntities();
	        	ctx.setResource(CommandHelper.createCollectionResource(entity, entities));
	        	return Result.SUCCESS;
	    	} else {
				logger.error("Other type of unsupported response from ODataProducer.getNavProperty");
	        }
		}

		return Result.FAILURE;
	}

}
