package com.temenos.interaction.winkext;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.DynamicResource;
import org.apache.wink.common.model.multipart.InMultiPart;

import com.temenos.interaction.core.command.CommandController;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.MethodNotAllowedException;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
import com.temenos.interaction.core.hypermedia.ResourceStateProvider;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.rim.HTTPHypermediaRIM;
import com.temenos.interaction.core.rim.HTTPResourceInteractionModel;
import com.temenos.interaction.core.rim.ResourceInteractionModel;

public class LazyResourceDelegate implements HTTPResourceInteractionModel, DynamicResource {

	private ResourceStateMachine hypermediaEngine;
	private ResourceStateProvider resourceStateProvider;
	private CommandController commandController;
	private Metadata metadata;

	private Map<String, Set<String>> resourceNamesToMethods = new HashMap<String, Set<String>>();
	private String path = null;	
		
	/**
	 * The class binding an instance of a ResourceState to a path
	 * @param hypermediaEngine registry of all resources
	 * @param resourceStateProvider interface that is used to lazily lookup/create ResourceState instance
	 * @param resourceName the state name
	 * @param path the resource path
	 */
	public LazyResourceDelegate(ResourceStateMachine hypermediaEngine,
			ResourceStateProvider resourceStateProvider, 
			CommandController commandController,
			Metadata metadata,
			String resourceName, 
			String path,
			Set<String> methods) {
		this.hypermediaEngine = hypermediaEngine;
		this.commandController = commandController;
		this.metadata = metadata;
		this.resourceStateProvider = resourceStateProvider;
		this.resourceNamesToMethods.put(resourceName, methods);
		this.path = path;
	}
	
	public void addResource(String name, Set<String> newMethods) {
		synchronized (resourceNamesToMethods) {
			Set<String> methods = resourceNamesToMethods.get(name);
			if (methods == null) {
				methods = new HashSet<String>();
			}
			methods.addAll(newMethods);
			resourceNamesToMethods.put(name, methods);
		}
	}
	
	@Override
    public String getBeanName() {
		return resourceNamesToMethods.toString();
    }

	@Override
    public void setBeanName(String beanName) {
        throw new AssertionError("Not supported");
    }

    public void setWorkspaceTitle(String workspaceTitle) {
        throw new AssertionError("Not supported");
    }

	@Override
    public String getWorkspaceTitle() {
		return "DefaultWorkspace";
    }

    public void setCollectionTitle(String collectionTitle) {
        throw new AssertionError("Not supported");
    }

	@Override
    public String getCollectionTitle() {
		return path;
    }

	@Override
    public String getPath() {
		return path;
    }

	@Override
    public void setParent(Object parent) {
        throw new AssertionError("Not supported");
    }

	@Override
    public HTTPResourceInteractionModel getParent() {
		return null;
    }
	
	/*
	 * This method unregisters in the resource state machine resources that are not currently loaded.
	 * It also registers the resource of interest even if it's already loaded, since being loaded doesn't
	 * imply it's registered, and currently there is no way of checking this.
	 */
	private HTTPHypermediaRIM getResource(UriInfo uriInfo, String httpMethod) throws MethodNotAllowedException {
        String resourceStateId = resourceStateProvider.getResourceStateId(httpMethod, "/" + uriInfo.getPath(false));
        
        if(resourceStateId == null) {
        	return null;
        } else {
            
            boolean loaded = resourceStateProvider.isLoaded(resourceStateId);
            // to register the resource state we are forced to call getResourceState,
            // which loads the resource if it wasn't
            ResourceState resourceState = resourceStateProvider.getResourceState(resourceStateId);
            // however, if it wasn't loaded we're assuming the resource has changed, see below

            Map<String, Set<String>> stateNameToHttpMethods = resourceStateProvider.getResourceMethodsByState();
            Set<String> httpMethods = stateNameToHttpMethods.get(resourceStateId);

            if(httpMethods == null) {
                // here it is assumed the resource wasn't loaded, so no need to unregister
                hypermediaEngine.register(resourceState, httpMethod);                    
            } else {
                for (String tmpHttpMethod : httpMethods) {
                    // if the resource wasn't loaded, we assume it has changed and therefore it needs
                    // first to be unregistered with the resource state machine
                    if(!loaded) {
                        // unregister unloaded resource state
                        hypermediaEngine.unregister(resourceState, tmpHttpMethod);
                    }
                    hypermediaEngine.register(resourceState, tmpHttpMethod);
                }                    
            }
            
            return new HTTPHypermediaRIM(null, commandController, hypermediaEngine, metadata, resourceState.getPath(), false);              
        }
  	}

	@Override
	public Response get(HttpHeaders headers, String id, UriInfo uriInfo) {		
        try {
            HTTPHypermediaRIM rim = getResource(uriInfo, "GET");
            
            if(rim == null) {
                return Response.status(404).build();
            }
            
			return rim.get(headers, id, uriInfo);
		} catch (MethodNotAllowedException e) {
			return handleMethodNotAllowedException(e);			
		}
	}

	@Override
	public Response post(HttpHeaders headers, UriInfo uriInfo, InMultiPart inMP) {
	    
		try {
		    HTTPHypermediaRIM rim = getResource(uriInfo, "POST");
		    
            if(rim == null) {
                return Response.status(404).build();
            }
		    
			return rim.post(headers, uriInfo, inMP);
		} catch (MethodNotAllowedException e) {
			return handleMethodNotAllowedException(e);			
		}
	}	
	
	@Override
    public Response post( @Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo, 
    		MultivaluedMap<String, String> formParams) {
		try {
		    HTTPHypermediaRIM rim = getResource(uriInfo, "POST");
		    
            if(rim == null) {
                return Response.status(404).build();
            }
		    
			return rim.post(headers, id, uriInfo, formParams);
		} catch (MethodNotAllowedException e) {
			return handleMethodNotAllowedException(e);			
		}
	}

	@Override
	public Response post(HttpHeaders headers, String id, UriInfo uriInfo, EntityResource<?> eresource) {
        try {
            HTTPHypermediaRIM rim = getResource(uriInfo, "POST");

            if(rim == null) {
                return Response.status(404).build();
            }
            
			return rim.post(headers, id, uriInfo, eresource);
		} catch (MethodNotAllowedException e) {
			return handleMethodNotAllowedException(e);			
		}
	}
	
	@Override
	public Response put(HttpHeaders headers, UriInfo uriInfo, InMultiPart inMP) {
		try {
		    HTTPHypermediaRIM rim = getResource(uriInfo, "PUT");
		    
            if(rim == null) {
                return Response.status(404).build();
            }
		    
			return rim.put(headers, uriInfo, inMP);
		} catch (MethodNotAllowedException e) {
			return handleMethodNotAllowedException(e);			
		}
	}	

	@Override
	public Response put(HttpHeaders headers, String id, UriInfo uriInfo, EntityResource<?> eresource) {
		try {
            HTTPHypermediaRIM rim = getResource(uriInfo, "PUT"); 
                    
            if(rim == null) {
                return Response.status(404).build();
            }
		    
			return rim.put(headers, id, uriInfo, eresource);
		} catch (MethodNotAllowedException e) {
			return handleMethodNotAllowedException(e);			
		}
		
	}

	@Override
	public Response delete(HttpHeaders headers, String id, UriInfo uriInfo) {	    
		try {
            HTTPHypermediaRIM rim = getResource(uriInfo, "DELETE");
            
            if(rim == null) {
                return Response.status(404).build();
            }
            
			return rim.delete(headers, id, uriInfo);
		} catch (MethodNotAllowedException e) {
			return handleMethodNotAllowedException(e);			
		}
	}

	private Response handleMethodNotAllowedException(MethodNotAllowedException e) {
		StringBuilder allowHeader = new StringBuilder();
		
		Set<String> allowedMethods = new HashSet<String>(e.getAllowedMethods());
		allowedMethods.add("HEAD");
		allowedMethods.add("OPTIONS");
		
		for(String method: allowedMethods) {				
			allowHeader.append(method);
			allowHeader.append(", ");
		}
		
		return Response.status(405).header("Allow", allowHeader.toString().substring(0, allowHeader.length() - 2)).build();
	}

	@Override
	public Response options(@Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo) {	    
		try {
		    HTTPHypermediaRIM rim = getResource(uriInfo, "OPTIONS");

            if(rim == null) {
                return Response.status(404).build();
            }
		    
			return rim.options(headers, id, uriInfo);
		} catch (MethodNotAllowedException e) {
			return handleMethodNotAllowedException(e);			
		}
	}
	
	@Override
	public ResourceState getCurrentState() {
		return null;
	}

	@Override
	public String getResourcePath() {
		return path;
	}

	@Override
	public String getFQResourcePath() {
		return path;
	}

	@Override
	public Collection<ResourceInteractionModel> getChildren() {
		return null;
	}

    @Override
    public Response delete(HttpHeaders headers, String id, UriInfo uriInfo, EntityResource<?> resource) {
        try {
            HTTPHypermediaRIM rim = getResource(uriInfo, "DELETE");
            
            if(rim == null) {
                return Response.status(404).build();
            }
            
            return rim.delete(headers, id, uriInfo, resource);
        } catch (MethodNotAllowedException e) {
            return handleMethodNotAllowedException(e);          
        }
    }    
}
