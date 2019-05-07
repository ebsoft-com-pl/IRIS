package com.temenos.interaction.core.rim;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;

import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.hypermedia.Event;
import com.temenos.interaction.core.hypermedia.LazyCollectionResourceState;
import com.temenos.interaction.core.hypermedia.LazyResourceState;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
import com.temenos.interaction.core.hypermedia.Transition;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.resource.RESTResource;

/**
 * <p>Simply iterate through the transitions and get each one in turn.</p>
 * @author aphethean
 */
public class SequentialResourceRequestHandler implements ResourceRequestHandler {

	@Override
	public Map<Transition, ResourceRequestResult> getResources(HTTPHypermediaRIM rimHandler, HttpHeaders headers, InteractionContext ctx, EntityResource<?> resource, ResourceRequestConfig config) {
		return getResources(rimHandler, headers, ctx, resource, null, config);
	}

	public Map<Transition, ResourceRequestResult> getResources(HTTPHypermediaRIM rimHandler, HttpHeaders headers, InteractionContext ctx, EntityResource<?> resource, Object entity, ResourceRequestConfig config) {	
		assert(config != null);
		assert(config.getTransitions() != null);
		ResourceStateMachine hypermediaEngine = rimHandler.getHypermediaEngine();
		Map<Transition, ResourceRequestResult> resources = new HashMap<Transition, ResourceRequestResult>(); 
		for (Transition t : config.getTransitions()) {
			String method = t.getCommand().getMethod();
			if ((t.getCommand().getFlags() & Transition.AUTO) == Transition.AUTO) {
				method = t.getCommand().getMethod();
			}
	    	Event event = new Event("", method);
			// determine action
	    	ResourceState targetState = t.getTarget();
			if (targetState instanceof LazyResourceState || targetState instanceof LazyCollectionResourceState) {
				targetState = rimHandler.getHypermediaEngine().getResourceStateProvider().getResourceState(targetState.getName());
				t.setTarget(targetState);
			}
	    	
	    	InteractionCommand action = hypermediaEngine.buildWorkflow(event, targetState.getActions());
	    	
			MultivaluedMap<String, String> newPathParameters = new MultivaluedMapImpl<String>();
			newPathParameters.putAll(ctx.getPathParameters());
			
            Object resEntity = entity;
            if (resource != null) {
                resEntity = ((EntityResource<?>) resource).getEntity();
            }

            Map<String, Object> transitionProperties = hypermediaEngine.getTransitionProperties(t, resEntity,
                    ctx.getPathParameters(), ctx.getQueryParameters());

            for (String key : transitionProperties.keySet()) {
                if (transitionProperties.get(key) != null) {
                    newPathParameters.add(key, transitionProperties.get(key).toString());
                }
            }			
			

			MultivaluedMap<String, String> newQueryParameters = new MultivaluedMapImpl<String>();
			newQueryParameters.putAll(ctx.getQueryParameters());
						
			if (entity != null) {
				/* Handle cases where we may be embedding a resource that has filter criteria whose values are contained in the current resource's 
				 * entity properties.				
				 */				
                Map<String, Object> transitionPropertiesFilter = hypermediaEngine.getTransitionProperties(t, entity,
                        ctx.getPathParameters(), ctx.getQueryParameters());
				
                for (String key : transitionPropertiesFilter.keySet()) {
                    if (transitionPropertiesFilter.get(key) != null) {
                        newQueryParameters.add(key, transitionPropertiesFilter.get(key).toString());
                    }
				}
			}
			
			
	    	InteractionContext newCtx = new InteractionContext(ctx, null, newPathParameters, newQueryParameters, targetState);
	    	newCtx.setResource(null);
			Response response = rimHandler.handleRequest(headers, 
					newCtx, 
					event, 
					action, 
					resource, 
					config);
			RESTResource targetResource = null;
			if (response.getEntity() != null) {
				targetResource = (RESTResource) ((GenericEntity<?>) response.getEntity()).getEntity();
			}
			resources.put(t, new ResourceRequestResult(response.getStatus(), targetResource));
		}
		return resources;
	}

}
