package com.temenos.interaction.springdsl;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.temenos.interaction.core.hypermedia.Event;
import com.temenos.interaction.core.hypermedia.MethodNotAllowedException;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateProvider;
import com.temenos.interaction.core.hypermedia.Transition;

public class TestSpringDSLResourceStateProvider {

    static ApplicationContext ctx;
    protected ResourceStateProvider resourceStateProvider;
    
    @Before
    public void setUp() {
        ctx = new ClassPathXmlApplicationContext("classpath:/com/temenos/interaction/springdsl/TestSpringDSLResourceStateProvider-context.xml");
        resourceStateProvider = (ResourceStateProvider) ctx.getBean("resourceStateProvider");
       
    }

    @Test
	public void testGetResourceState() {
		ResourceState actual = resourceStateProvider.getResourceState("SimpleModel_Home_home");
		assertEquals("home", actual.getName());
	}

	@Test
	public void testGetResourceStateWithTransitionsInitialised() {
		ResourceState actual = resourceStateProvider.getResourceState("SimpleModel_Home_TestTransition");
		assertEquals("TestTransition", actual.getName());
		List<Transition> transitions = actual.getTransitions();
		assertEquals(1, transitions.size());
		assertEquals("access a property from the target to check it is lazy loaded", "LAZY", transitions.get(0).getTarget().getPath());
	}
	
	@Test
	public void testGetResourceStateMultipleStatesPerFile() {
		ResourceState actual = resourceStateProvider.getResourceState("SimpleModel_Home-home");
		assertEquals("home", actual.getName());
	}
	

	@Test
	public void testGetResourceStatesByPath() {
		Properties properties = new Properties();
		properties.put("SimpleModel_Home_home", "GET /test");
		ResourceStateProvider rsp = new SpringDSLResourceStateProvider(properties);
		Map<String, Set<String>> statesByPath = rsp.getResourceStatesByPath();
		assertEquals(1, statesByPath.size());
		assertEquals(1, statesByPath.get("/test").size());
		assertEquals("SimpleModel_Home_home", statesByPath.get("/test").toArray()[0]);
	}
	
    @Test
    public void testGetResourceMethodsByState() {
        Map<String, Set<String>> methods = resourceStateProvider.getResourceMethodsByState();
        assertNotNull(methods);
        assertEquals("Methods for state SimpleModel_Home_home", 2, methods.get("SimpleModel_Home_home").size());
    }

	@Test
	public void testGetResourceStateByRequest() {
		// properties: SimpleModel_Home_home=GET,PUT /test
		ResourceState foundGetState = resourceStateProvider.determineState(new Event("GET", "GET"), "/test");
		assertEquals("home", foundGetState.getName());
		ResourceState foundPutState = resourceStateProvider.determineState(new Event("PUT", "PUT"), "/test");
		assertEquals("home", foundPutState.getName());
	}

    @Test
    public void testGetResourceStateByMethodUrl() throws MethodNotAllowedException {
        // properties: SimpleModel_Home_home=GET,PUT /test
        ResourceState foundGetState = resourceStateProvider.getResourceState("GET", "/test");
        assertEquals("home", foundGetState.getName());
        ResourceState foundPutState = resourceStateProvider.getResourceState("PUT", "/test");
        assertEquals("home", foundPutState.getName());
    }

    @Test
    public void testGetResourceStateId() throws MethodNotAllowedException {
        String actual = resourceStateProvider.getResourceStateId("GET", "/test");
        assertEquals("SimpleModel_Home_home", actual);
        // as opposed to "home", which is the resource state name
        ResourceState foundGetState = resourceStateProvider.getResourceState("GET", "/test");
        assertThat("SimpleModel_Home_home", not(foundGetState.getName()));
    }
    
    @Test
    public void testIsLoaded() {
        SpringDSLResourceStateProvider rsp = (SpringDSLResourceStateProvider) resourceStateProvider;

        assertFalse(rsp.isLoaded("SimpleModel_Home_home"));
        assertFalse(rsp.isLoaded("inexistentState"));

        // this is the current way of loading resources...
        rsp.getResourceState("SimpleModel_Home_home");

        assertTrue(rsp.isLoaded("SimpleModel_Home_home"));
        assertFalse(rsp.isLoaded("inexistentState"));
    }

    @Test
    public void testUnload() {
        SpringDSLResourceStateProvider rsp = (SpringDSLResourceStateProvider) resourceStateProvider;
        
        // loading the resource
        rsp.getResourceState("SimpleModel_Home_home");
        
        assertTrue(rsp.isLoaded("SimpleModel_Home_home"));
        rsp.unload("SimpleModel_Home_home");
        assertFalse(rsp.isLoaded("SimpleModel_Home_home"));
    }

    @Test
    public void testUnloadInexistentResource() {
        SpringDSLResourceStateProvider rsp = (SpringDSLResourceStateProvider) resourceStateProvider;
        assertEquals("Number of resources: ", 1, rsp.getResourceMethodsByState().size());

        String inexistentStateName = "inexistentState";
        // try to load the resource
        rsp.getResourceState(inexistentStateName);

        assertFalse(rsp.isLoaded(inexistentStateName));
        rsp.unload(inexistentStateName);
        assertEquals("Number of resources: ", 1, rsp.getResourceMethodsByState().size());
    }

    @Test(expected=MethodNotAllowedException.class)
    public void testMethodNotAllowedExceptionforGetResourceStateId() throws Exception {
        SpringDSLResourceStateProvider rsp = (SpringDSLResourceStateProvider) resourceStateProvider;

        // allowed method
        String resourceId = rsp.getResourceStateId("GET", "/test");
        assertEquals("SimpleModel_Home_home", resourceId);

        // not allowed method
        rsp.getResourceStateId("DELETE", "/test");
    }
    
    @Test(expected=MethodNotAllowedException.class)
    public void testMethodNotAllowedExceptionforGetResourceState() throws Exception {
        SpringDSLResourceStateProvider rsp = (SpringDSLResourceStateProvider) resourceStateProvider;
        
        // allowed method
        ResourceState resource = rsp.getResourceState("GET", "/test");
        assertEquals("home", resource.getName());

        // not allowed method
        rsp.getResourceState("DELETE", "/test");
    }
}
