package com.temenos.interaction.core.hypermedia.expression;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;

import com.temenos.interaction.core.command.CommandHelper;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.hypermedia.HypermediaTemplateHelper;
import com.temenos.interaction.core.hypermedia.LazyResourceState;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
import com.temenos.interaction.core.hypermedia.Transition;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.resource.RESTResource;
import com.temenos.interaction.core.rim.HTTPHypermediaRIM;
import com.temenos.interaction.core.rim.ResourceRequestConfig;
import com.temenos.interaction.core.rim.ResourceRequestResult;
import com.temenos.interaction.core.rim.SequentialResourceRequestHandler;

public class ResourceGETExpression implements Expression {

	public enum Function {
		OK,
		NOT_FOUND
	}
	
	public final Function function;
	public final String state;
	public final Transition transition;
	public final Set<Transition> transitions = new HashSet<Transition>();
	
	public ResourceGETExpression(ResourceState target, Function function) {
		this.function = function;
		this.state = null;
		this.transition = new Transition.Builder().method("GET").target(target).flags(Transition.EXPRESSION).build();
		this.transitions.add(transition);
	}

	// keep old way until 0.5.1
	@Deprecated
	public ResourceGETExpression(String state, Function function) {
		this.function = function;
		this.state = state;
		this.transition = null;
	}
	
	public Function getFunction() {
		return function;
	}

	public String getState() {
		String state = null;
		if (transition != null) {
			state = transition.getTarget().getName();
		}
		return state;
	}
	
	@Override
	public boolean evaluate(HTTPHypermediaRIM rimHandler, InteractionContext ctx, EntityResource<?> resource) {
		ResourceStateMachine hypermediaEngine = rimHandler.getHypermediaEngine();
		ResourceState target = null;
		Transition ourTransition = transition;
		if (ourTransition == null) {
			target = hypermediaEngine.getResourceStateByName(state);
			ourTransition = ctx.getCurrentState().getTransition(target);
		} else {
			target = ourTransition.getTarget();
		}
		
        target = hypermediaEngine.checkAndResolve(target);
    	assert(ourTransition != null);
		if (target == null)
			throw new IllegalArgumentException("Indicates a problem with the RIM, it allowed an invalid state to be supplied");
		assert(target.getActions() != null);
		
		if(ourTransition.getTarget() instanceof LazyResourceState) {
		    ourTransition.setTarget(target);
		}
        //Create a new interaction context for this state
        MultivaluedMap<String, String> pathParameters = getPathParametersForTargetState(hypermediaEngine, ctx, ourTransition);
    	InteractionContext newCtx = new InteractionContext(ctx, null, pathParameters, null, target);
		EntityResource<?> entityResourceCopy = CommandHelper.createEntityResource(resolveEntityResource(resource, ctx.getResource()));
		newCtx.setResource(entityResourceCopy);

    	//Get the target resource
		ResourceRequestConfig config = new ResourceRequestConfig.Builder()
				.transition(ourTransition)
				.injectLinks(false)
				.embedResources(false)
				.build();
		Map<Transition, ResourceRequestResult> results = new SequentialResourceRequestHandler().getResources(rimHandler, null, newCtx, entityResourceCopy, config);
		assert(results.values() != null && results.values().size() == 1);
		ResourceRequestResult result = results.values().iterator().next();
		
		//Ignore the resource and its links, just interested in the result status
		if (Status.OK.getStatusCode() == result.getStatus() 
				&& getFunction().equals(Function.OK)) {
			return true;
		}
		if (Status.OK.getStatusCode() != result.getStatus() 
				&& getFunction().equals(Function.NOT_FOUND)) {
			return true;
		}
		return false;
	}

	@Override
	public Set<Transition> getTransitions() {
		return transitions;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		if (getFunction().equals(ResourceGETExpression.Function.OK))
			sb.append("OK(").append(getState()).append(")");
		if (getFunction().equals(ResourceGETExpression.Function.NOT_FOUND))
			sb.append("NOT_FOUND").append(getState()).append(")");
		return sb.toString();
	}

	private EntityResource<?> resolveEntityResource(EntityResource<?> entityResource, RESTResource restResource) {
		if (entityResource != null) {
			return entityResource;
		}
		if (restResource instanceof EntityResource) {
			return (EntityResource<?>) restResource;
		}
		return null;
	}
	
	/*
	 * Obtain path parameters to use when accessing
	 * a resource state on an expression.  
	 */
	private MultivaluedMap<String, String> getPathParametersForTargetState(ResourceStateMachine hypermediaEngine, InteractionContext ctx, Transition transition) {
    	Map<String, Object> transitionProperties = new HashMap<String, Object>();
    	// by default add all the path parameters to access the target
    	if (ctx.getPathParameters() != null) {
    		for (String key : ctx.getPathParameters().keySet()){
    			transitionProperties.put(key, ctx.getPathParameters().getFirst(key));
    		}
    	}
   		RESTResource resource = ctx.getResource();
   		if(resource != null && resource instanceof EntityResource) {
   			Object entity = ((EntityResource<?>) resource).getEntity();
   	    	transitionProperties.putAll(hypermediaEngine.getTransitionProperties(transition, entity, null, null)); 
   		}    		
    	
    	//apply transition properties to path parameters 
    	return HypermediaTemplateHelper.getPathParametersForTargetState(transition, transitionProperties);
	}
}
