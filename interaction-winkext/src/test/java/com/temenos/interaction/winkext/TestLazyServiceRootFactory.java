package com.temenos.interaction.winkext;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.temenos.interaction.core.command.CommandController;
import com.temenos.interaction.core.entity.EntityMetadata;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.MethodNotAllowedException;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateProvider;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.core.rim.HTTPResourceInteractionModel;
import com.temenos.interaction.core.web.RequestContext;

@RunWith(PowerMockRunner.class)
@PrepareForTest(RequestContext.class)
public class TestLazyServiceRootFactory {

    @Before
    public void setup() {
        PowerMockito.mockStatic(RequestContext.class);
        RequestContext ctx = mock(RequestContext.class);
        when(RequestContext.getRequestContext()).thenReturn(ctx);
        when(RequestContext.getRequestContext().getRequestId()).thenReturn("SomeId");
    }
    
	@Test
	public void testPath() {
		LazyServiceRootFactory factory = new LazyServiceRootFactory();
		Set<HTTPResourceInteractionModel> serviceRoots = factory.getServiceRoots();
		assertEquals(1, serviceRoots.size());
		assertEquals("{var:.*}", serviceRoots.iterator().next().getResourcePath());
	}

	@Test
	public void testRegister() throws MethodNotAllowedException {
		LazyServiceRootFactory factory = new LazyServiceRootFactory();
		ResourceStateProvider resourceStateProvider = mock(ResourceStateProvider.class);
		ResourceState state = mock(ResourceState.class);
        when(state.getName()).thenReturn("resource");		
        when(state.getPath()).thenReturn("/test");		
		when(state.getResourcePath()).thenReturn("/test");		
		when(state.getEntityName()).thenReturn("Mock");
	    when(resourceStateProvider.getResourceStateId(eq("GET"), eq("/test"))).thenReturn("resource");
		when(resourceStateProvider.getResourceState(eq("GET"), eq("/test"))).thenReturn(state);
	    when(resourceStateProvider.getResourceState("resource")).thenReturn(state);
		factory.setResourceStateProvider(resourceStateProvider);
		
        Set<String> methods = new HashSet<String>();
        methods.add("GET");
		
        Map<String,Set<String>> resourceMethodsByState = new HashMap<String, Set<String>>();               
        resourceMethodsByState.put("resource", methods);
        when(resourceStateProvider.getResourceMethodsByState()).thenReturn(resourceMethodsByState);
		
		factory.setCommandController(mock(CommandController.class));
		factory.setMetadata(mockMetadata());
		Set<HTTPResourceInteractionModel> serviceRoots = factory.getServiceRoots();
		assertEquals(1, serviceRoots.size());
		
		HTTPResourceInteractionModel rim = serviceRoots.iterator().next();

        HttpHeaders headers = mock(HttpHeaders.class);
        String id = "123";		
        UriInfo uriInfo = mock(UriInfo.class);	
        when(uriInfo.getPath(eq(false))).thenReturn("test");
        when(uriInfo.getPathParameters(eq(true))).thenReturn(mock(MultivaluedMap.class));
        when(uriInfo.getQueryParameters(eq(false))).thenReturn(mock(MultivaluedMap.class));        

		Response response = rim.get(headers, id, uriInfo);
		
		assertNotNull(response);
		
        EntityResource resource = mock(EntityResource.class);
                
        response = rim.post(headers, id, uriInfo, resource);
            
        assertEquals(response.getStatus(), 404);
	}

	@Test
	public void testRegisterMultiple() throws MethodNotAllowedException {
        LazyServiceRootFactory factory = new LazyServiceRootFactory();
        ResourceStateProvider resourceStateProvider = mock(ResourceStateProvider.class);
        
        ResourceState state = mock(ResourceState.class);
        when(state.getName()).thenReturn("resource");        
        when(state.getPath()).thenReturn("/test");      
        when(state.getResourcePath()).thenReturn("/test");      
        when(state.getEntityName()).thenReturn("Mock");
        when(resourceStateProvider.getResourceState(eq("GET"), eq("/test"))).thenReturn(state);
        when(resourceStateProvider.getResourceStateId(eq("GET"), eq("/test"))).thenReturn("resource");
        when(resourceStateProvider.getResourceState(eq("GET"), eq("/test"))).thenReturn(state);
        when(resourceStateProvider.getResourceState("resource")).thenReturn(state);
        
        Set<String> methods = new HashSet<String>();
        methods.add("GET");
        
        Map<String,Set<String>> resourceMethodsByState = new HashMap<String, Set<String>>();               
        resourceMethodsByState.put("resource", methods);
        when(resourceStateProvider.getResourceMethodsByState()).thenReturn(resourceMethodsByState);
        
        
        ResourceState state2 = mock(ResourceState.class);
        when(state.getName()).thenReturn("resource2");        
        when(state2.getPath()).thenReturn("/test");      
        when(state2.getResourcePath()).thenReturn("/test");      
        when(state2.getEntityName()).thenReturn("Mock");
        when(resourceStateProvider.getResourceState(eq("POST"), eq("/test"))).thenReturn(state);
        when(resourceStateProvider.getResourceStateId(eq("POST"), eq("/test"))).thenReturn("resource");
        when(resourceStateProvider.getResourceState(eq("POST"), eq("/test"))).thenReturn(state);
        when(resourceStateProvider.getResourceState("resource")).thenReturn(state);

        
        Set<String> methods2 = new HashSet<String>();
        methods2.add("POST");
                       
        resourceMethodsByState.put("resource2", methods2);
        

        factory.setResourceStateProvider(resourceStateProvider);
        factory.setCommandController(mock(CommandController.class));
        factory.setMetadata(mockMetadata());
        Set<HTTPResourceInteractionModel> serviceRoots = factory.getServiceRoots();
        assertEquals(1, serviceRoots.size());
        
        HTTPResourceInteractionModel rim = serviceRoots.iterator().next();

        HttpHeaders headers = mock(HttpHeaders.class);
        String id = "123";      
        UriInfo uriInfo = mock(UriInfo.class);  
        when(uriInfo.getPath(eq(false))).thenReturn("test");
        when(uriInfo.getPathParameters(eq(true))).thenReturn(mock(MultivaluedMap.class));
        when(uriInfo.getQueryParameters(eq(false))).thenReturn(mock(MultivaluedMap.class));        
        
        Response response = rim.get(headers, id, uriInfo);
        
        assertNotNull(response);
        
        EntityResource resource = mock(EntityResource.class);        
        response = rim.post(headers, id, uriInfo, resource);
        
        assertNotNull(response);        
	}
	
	private Metadata mockMetadata() {
		Metadata metadata = new Metadata("MOCKMODEL");
		EntityMetadata entityMetadata = new EntityMetadata("mock");
		metadata.setEntityMetadata(entityMetadata);
		return metadata;
	}
}
