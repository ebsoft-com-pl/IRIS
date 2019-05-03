package com.temenos.interaction.winkext;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.Collection;

import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.wink.common.DynamicResource;
import org.apache.wink.common.model.multipart.InMultiPart;

import com.temenos.interaction.core.UriInfoImpl;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.rim.HTTPResourceInteractionModel;
import com.temenos.interaction.core.rim.ResourceInteractionModel;

public class DynamicResourceDelegate implements HTTPResourceInteractionModel, DynamicResource {

	private final HTTPResourceInteractionModel parent;
	private final HTTPResourceInteractionModel resource;
	
	public DynamicResourceDelegate(HTTPResourceInteractionModel parent, HTTPResourceInteractionModel resource) {
		this.parent = parent;
		this.resource = resource;
	}

	@Override
    public String getBeanName() {
		if (resource instanceof DynamicResource) {
			return ((DynamicResource)resource).getBeanName();
		}
        return resource.getCurrentState().getId();
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
		return resource.getResourcePath();
    }

	@Override
    public String getPath() {
        return resource.getResourcePath();
    }

	@Override
    public void setParent(Object parent) {
        throw new AssertionError("Not supported");
    }

	@Override
    public HTTPResourceInteractionModel getParent() {
        return parent;
    }

	@Override
	public Response get(HttpHeaders headers, String id, UriInfo uriInfo) {
		return resource.get(headers, id, new UriInfoImpl(uriInfo));
	}

	@Override
	public Response post(HttpHeaders headers, UriInfo uriInfo, InMultiPart inMP) {
		return resource.post(headers, new UriInfoImpl(uriInfo), inMP);
	}	
	
	@Override
    public Response post( @Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo, 
    		MultivaluedMap<String, String> formParams) {
		return resource.post(headers, id, new UriInfoImpl(uriInfo), formParams);
	}

	@Override
	public Response post(HttpHeaders headers, String id, UriInfo uriInfo, EntityResource<?> eresource) {
		return resource.post(headers, id, new UriInfoImpl(uriInfo), eresource);
	}
	
	@Override
	public Response put(HttpHeaders headers, UriInfo uriInfo, InMultiPart inMP) {
		return resource.put(headers, new UriInfoImpl(uriInfo), inMP);
	}	

	@Override
	public Response put(HttpHeaders headers, String id, UriInfo uriInfo, EntityResource<?> eresource) {
		return resource.put(headers, id, new UriInfoImpl(uriInfo), eresource);
	}

	@Override
	public Response delete(HttpHeaders headers, String id, UriInfo uriInfo) {
		return resource.delete(headers, id, new UriInfoImpl(uriInfo));
	}

	@Override
	public Response options(@Context HttpHeaders headers, @PathParam("id") String id, @Context UriInfo uriInfo) {
		return resource.options(headers, id, new UriInfoImpl(uriInfo));
	}

	@Override
	public ResourceState getCurrentState() {
		return resource.getCurrentState();
	}

	@Override
	public String getResourcePath() {
		return resource.getResourcePath();
	}

	@Override
	public String getFQResourcePath() {
		return resource.getFQResourcePath();
	}

	@Override
	public Collection<ResourceInteractionModel> getChildren() {
		return resource.getChildren();
	}

    @Override
    public Response delete(HttpHeaders headers, String id, UriInfo uriInfo, EntityResource<?> eresource) {
        return resource.delete(headers, id, new UriInfoImpl(uriInfo), eresource);
    }    
}
