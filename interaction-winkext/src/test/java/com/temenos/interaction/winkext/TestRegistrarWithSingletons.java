package com.temenos.interaction.winkext;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.whenNew;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.wink.common.DynamicResource;
import org.apache.wink.common.internal.registry.ProvidersRegistry;
import org.apache.wink.server.internal.registry.ResourceRegistry;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
import com.temenos.interaction.core.rim.HTTPHypermediaRIM;
import com.temenos.interaction.core.rim.HTTPResourceInteractionModel;
import com.temenos.interaction.core.rim.ResourceInteractionModel;

public class TestRegistrarWithSingletons {

	private HTTPResourceInteractionModel createMockHTTPRIM(String entityName, String path) {
		HTTPHypermediaRIM rim = mock(HTTPHypermediaRIM.class);
		ResourceState rs = mock(ResourceState.class);
		when(rs.getName()).thenReturn("");
		when(rs.getEntityName()).thenReturn(entityName);
		ResourceStateMachine rsm = mock(ResourceStateMachine.class);
		when(rsm.getInitial()).thenReturn(rs);
		when(rim.getCurrentState()).thenReturn(rs);
		when(rim.getResourcePath()).thenReturn(path);
		when(rim.getFQResourcePath()).thenCallRealMethod();
		return rim;
	}

	@Test
	public void testSimpleServiceRoot() {
		HTTPResourceInteractionModel serviceRoot = createMockHTTPRIM("notes", "/notes");
		RegistrarWithSingletons rs = new RegistrarWithSingletons();
		rs.setServiceRoot(serviceRoot);
		assertNotNull(rs.getInstances());
		assertEquals(1, rs.getInstances().size());
	}

    @Test
    public void testSimpleServiceRootWithBrackets() {
        HTTPResourceInteractionModel serviceRoot = createMockHTTPRIM("notes", "/notes()");
        RegistrarWithSingletons rs = new RegistrarWithSingletons();
        rs.setServiceRoot(serviceRoot);
        assertNotNull(rs.getInstances());
        assertEquals(2, rs.getInstances().size());
        assertNotNull(rs.getDynamicResource("/notes"));
        assertNotNull(rs.getDynamicResource("/notes()"));
    }

	@Test
	public void testSimpleServiceRoots() {
		Set<HTTPResourceInteractionModel> serviceRoots = new HashSet<HTTPResourceInteractionModel>();
		serviceRoots.add(createMockHTTPRIM("notes", "/"));
		serviceRoots.add(createMockHTTPRIM("metadata", "/$metadata"));
		RegistrarWithSingletons rs = new RegistrarWithSingletons();
		rs.setServiceRoots(serviceRoots);
		assertNotNull(rs.getInstances());
		assertEquals(2, rs.getInstances().size());
	}

	@Test
	public void testHierarchyServiceRoot() throws Exception {
		HTTPResourceInteractionModel serviceRoot = createMockHTTPRIM("notes", "/notes");
		List<ResourceInteractionModel> children = new ArrayList<ResourceInteractionModel>();
		HTTPResourceInteractionModel childDraft = createMockHTTPRIM("draftNote", "/draft/{id}");
        HTTPResourceInteractionModel childNote = createMockHTTPRIM("note", "/{id}");		
		children.add(childDraft);
		children.add(childNote);
		when(serviceRoot.getChildren()).thenReturn(children);
        when(childDraft.getParent()).thenReturn(serviceRoot);
        when(childNote.getParent()).thenReturn(serviceRoot);
		
		whenNew(DynamicResourceDelegate.class).withParameterTypes(HTTPResourceInteractionModel.class, HTTPResourceInteractionModel.class).withArguments(any(DynamicResource.class), any(ResourceInteractionModel.class)).thenAnswer(new Answer<Object>() {
				public Object answer(InvocationOnMock invocation) throws Throwable {
					return new DynamicResourceDelegate((HTTPResourceInteractionModel) invocation.getArguments()[0], (HTTPResourceInteractionModel) invocation.getArguments()[1]);
				}
		});
		RegistrarWithSingletons rs = new RegistrarWithSingletons();
		rs.setServiceRoot(serviceRoot);
		assertNotNull(rs.getInstances());
	    // verify resource delegate created 3 times
		assertEquals(3, rs.getInstances().size());
		
		// all children should have the same parent
		// add parents to a set and then it should have only one element
		Set<DynamicResourceDelegate> drdSet = new HashSet<DynamicResourceDelegate>();
		DynamicResourceDelegate parent = null;
		for (Object obj : rs.getInstances()) {
			DynamicResourceDelegate rd = (DynamicResourceDelegate) obj;
			if (rd.getParent() != null) {
				parent = (DynamicResourceDelegate) rd.getParent();
				drdSet.add(parent);
			}
		}
		assertEquals(1, drdSet.size());
	}

    @Test
    public void testRegister() {
        HTTPResourceInteractionModel serviceRoot = createMockHTTPRIM("notes", "/notes");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ProvidersRegistry providerRegistry = mock(ProvidersRegistry.class);

        RegistrarWithSingletons rs = new RegistrarWithSingletons();
        rs.register(resourceRegistry, providerRegistry);
        rs.register(serviceRoot);
        assertNotNull(rs.getDynamicResource("/notes"));
        
        HTTPResourceInteractionModel childNote = createMockHTTPRIM("note", "/{id}");        
        when(childNote.getParent()).thenReturn(serviceRoot);

        rs.register(childNote);
        assertNotNull(rs.getDynamicResource("/notes/{id}"));
    }

    @Test
    public void testRegisterWithBrackets() {
        HTTPResourceInteractionModel serviceRoot = createMockHTTPRIM("notes", "/notes()");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ProvidersRegistry providerRegistry = mock(ProvidersRegistry.class);

        RegistrarWithSingletons rs = new RegistrarWithSingletons();
        rs.register(resourceRegistry, providerRegistry);
        rs.register(serviceRoot);
        assertNotNull(rs.getDynamicResource("/notes"));
        assertNotNull(rs.getDynamicResource("/notes()"));
    }
    
    @Test
    public void testRegisterChildWithUnregisteredParentWithBrackets() {
        HTTPResourceInteractionModel serviceRoot = createMockHTTPRIM("notes", "/notes");
        
        HTTPResourceInteractionModel childNote = createMockHTTPRIM("note", "/note");        
        when(childNote.getParent()).thenReturn(serviceRoot);
        
        HTTPResourceInteractionModel childDraft = createMockHTTPRIM("draft", "/draft()");        
        when(childDraft.getParent()).thenReturn(childNote);

        RegistrarWithSingletons rs = new RegistrarWithSingletons();
        rs.setServiceRoot(serviceRoot);
        assertNotNull(rs.getInstances());
        assertEquals(1, rs.getInstances().size());

        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ProvidersRegistry providerRegistry = mock(ProvidersRegistry.class);
        rs.register(resourceRegistry, providerRegistry);

        // this registers the childNote (its parent) as well
        rs.register(childDraft);
        // bracketed resources are registered with and without brackets
        assertEquals(4, rs.getInstances().size());
        assertNotNull(rs.getDynamicResource("/notes"));
        assertNotNull(rs.getDynamicResource("/notes/note"));
        // these paths should have /notes as prefix, but the current behaviour
        // of HTTPHypermediaRIM.getFQResourcePath() prevents to get a
        // fully qualified path from a resource with at least two ancestors
        assertNotNull(rs.getDynamicResource("/note/draft"));
        assertNotNull(rs.getDynamicResource("/note/draft()"));
        // should HTTPHypermediaRIM.getFQResourcePath() be fixed, these two
        // asserts would fail and their paths should be corrected 
    }
    
    @Test
    public void testRegisterExistentResource() {
        HTTPResourceInteractionModel serviceRoot = createMockHTTPRIM("notes", "/notes");
        ResourceRegistry resourceRegistry = mock(ResourceRegistry.class);
        ProvidersRegistry providerRegistry = mock(ProvidersRegistry.class);

        RegistrarWithSingletons rs = new RegistrarWithSingletons();
        rs.register(resourceRegistry, providerRegistry);
        rs.register(serviceRoot);
        assertNotNull(rs.getDynamicResource("/notes"));
        
        HTTPResourceInteractionModel childNote = createMockHTTPRIM("note", "/notes");        

        rs.register(childNote);
        assertNull(rs.getDynamicResource("/notes/notes"));
    }
    
    @Test(expected=IllegalArgumentException.class)
    public void testSetNullServiceRoot() {
        RegistrarWithSingletons rs = new RegistrarWithSingletons();
        rs.setServiceRoots(null);
    }
    
    @Test
    public void testSetServiceRootWithNoChildren() {
        HTTPResourceInteractionModel serviceRoot = createMockHTTPRIM("notes", "/notes");
        when(serviceRoot.getChildren()).thenReturn(null);

        RegistrarWithSingletons rs = new RegistrarWithSingletons();
        rs.setServiceRoot(serviceRoot);
        assertNotNull(rs.getInstances());
        assertEquals(1, rs.getInstances().size());
    }

}
