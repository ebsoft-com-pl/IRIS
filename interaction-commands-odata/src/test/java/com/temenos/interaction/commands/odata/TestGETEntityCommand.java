package com.temenos.interaction.commands.odata;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNotNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriInfo;

import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.odata4j.core.ImmutableList;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OEntityKey.KeyType;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmType;
import org.odata4j.exceptions.NotFoundException;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;

import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.command.CommandHelper;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;
import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.entity.EntityProperties;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.resource.EntityResource;

public class TestGETEntityCommand {

	class MyEdmType extends EdmType {
		public MyEdmType(String name) {
			super(name);
		}
		public boolean isSimple() { return false; }
	}

	@SuppressWarnings("unchecked")
	@Test
	public void testEntitySetFound() throws InteractionException {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test assertion for entity set not found
        InteractionContext ctx = createInteractionContext("MyEntity", "1");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		EntityResource<OEntity> er = (EntityResource<OEntity>) ctx.getResource();
		assertEquals("MyEntity", er.getEntityName());
		assertEquals("MyEntity", er.getEntity().getEntityType().getName());
		assertEquals("MyEntity", er.getEntity().getEntitySetName());
	}
	
	@SuppressWarnings("unchecked")
	@Test(expected = AssertionError.class)
	public void testEntitySetNotFound() throws InteractionException {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test assertion for entity set not found
        InteractionContext ctx = createInteractionContext("DOESNOTMATCH", "1");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		EntityResource<OEntity> er = (EntityResource<OEntity>) ctx.getResource();
		assertEquals("DOESNOTMATCH", er.getEntityName());
		assertEquals("DOESNOTMATCH", er.getEntity().getEntityType().getName());
		assertEquals("DOESNOTMATCH", er.getEntity().getEntitySetName());
	}

	@Test
	public void testEntityKeyTypeString() throws InteractionException {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", "abc");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		assertTrue(ctx.getResource() instanceof EntityResource);
		// check the key was constructed correctly
		class StringOEntityKey extends ArgumentMatcher<OEntityKey> {
		      public boolean matches(Object obj) {
		    	  OEntityKey ek = (OEntityKey) obj;
		    	  assertNotNull(ek);
		    	  assertEquals(KeyType.SINGLE, ek.getKeyType());
		    	  assertEquals("abc", (String) ek.asSingleValue());
		          return true;
		      }
		   }
		verify(mockProducer).getEntity(eq("MyEntity"), argThat(new StringOEntityKey()), isNotNull(EntityQueryInfo.class));
	}

	@Test
	public void testEntityKeyTypeStringQuoted() throws InteractionException {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", "'abc'");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		assertTrue(ctx.getResource() instanceof EntityResource);
		// check the key was constructed correctly
		class StringOEntityKey extends ArgumentMatcher<OEntityKey> {
		      public boolean matches(Object obj) {
		    	  OEntityKey ek = (OEntityKey) obj;
		    	  assertNotNull(ek);
		    	  assertEquals(KeyType.SINGLE, ek.getKeyType());
		    	  assertEquals("abc", (String) ek.asSingleValue());
		          return true;
		      }
		   }
		verify(mockProducer).getEntity(eq("MyEntity"), argThat(new StringOEntityKey()), isNotNull(EntityQueryInfo.class));
	}

	@Test
	public void testEntityKeyInt64() throws InteractionException {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.Int64");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", Long.toString(Long.MAX_VALUE));
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		assertTrue(ctx.getResource() instanceof EntityResource);
		// check the key was constructed correctly
		class Int64OEntityKey extends ArgumentMatcher<OEntityKey> {
		      public boolean matches(Object obj) {
		    	  OEntityKey ek = (OEntityKey) obj;
		    	  assertNotNull(ek);
		    	  assertEquals(KeyType.SINGLE, ek.getKeyType());
		    	  assertEquals(new Long(Long.MAX_VALUE), (Long) ek.asSingleValue());
		          return true;
		      }
		   }
		verify(mockProducer).getEntity(eq("MyEntity"), argThat(new Int64OEntityKey()), isNotNull(EntityQueryInfo.class));
	}

	@Test
	public void testEntityKeyInt64Error() throws InteractionException {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.Int64");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", "A1");
        try {
        	gec.execute(ctx);
        	fail("Should have failed.");
        }
        catch(InteractionException ie) {
    		assertEquals(Status.INTERNAL_SERVER_ERROR, ie.getHttpStatus());
    		assertEquals("Entity key type A1 is not supported.", ie.getCause().getMessage());
        }
	}
	
	@Test
	public void testEntityKeyDateTime() throws InteractionException {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.DateTime");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", "datetime'2012-02-06T18:05:53'");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		assertTrue(ctx.getResource() instanceof EntityResource);
		// check the key was constructed correctly
		class TimestampOEntityKey extends ArgumentMatcher<OEntityKey> {
		      public boolean matches(Object obj) {
		    	  OEntityKey ek = (OEntityKey) obj;
		    	  assertNotNull(ek);
		    	  assertEquals(KeyType.SINGLE, ek.getKeyType());
		    	  assertEquals(new LocalDateTime(2012,2,6,18,5,53), (LocalDateTime) ek.asSingleValue());
		          return true;
		      }
		   }
		verify(mockProducer).getEntity(eq("MyEntity"), argThat(new TimestampOEntityKey()), isNotNull(EntityQueryInfo.class));
	}

	@Test
	public void testEntityKeyTime() throws InteractionException {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.Time");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		// test our method
        InteractionContext ctx = createInteractionContext("MyEntity", "time'PT18H05M53S'");
		InteractionCommand.Result result = gec.execute(ctx);
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		assertTrue(ctx.getResource() instanceof EntityResource);
		// check the key was constructed correctly
		class DateOEntityKey extends ArgumentMatcher<OEntityKey> {
		      public boolean matches(Object obj) {
		    	  OEntityKey ek = (OEntityKey) obj;
		    	  assertNotNull(ek);
		    	  assertEquals(KeyType.SINGLE, ek.getKeyType());
		    	  assertEquals(new LocalTime(18,5,53), (LocalTime) ek.asSingleValue());
		          return true;
		      }
		   }
		verify(mockProducer).getEntity(eq("MyEntity"), argThat(new DateOEntityKey()), isNotNull(EntityQueryInfo.class));
	}

	@Test
	public void testGetEntityNotFound() throws InteractionException {
		ODataProducer mockProducer = createMockODataProducer("GetEntityNotFound", "Edm.String");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
        InteractionContext ctx = createInteractionContext("GetEntityNotFound", "1");
    	InteractionCommand.Result result = gec.execute(ctx);
    	assertEquals("Entity does not exist", InteractionCommand.Result.FAILURE, result);
	}
	
	@Test	
	public void testExecuteWithQueryParams() throws InteractionException {
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();
		queryParams.add("$filter", "a eq 'b'");
		queryParams.add("$select", "a");
		queryParams.add("MYPARAM", "MYVALUE");
		
		InteractionContext mockContext = createInteractionContext("MyEntity", queryParams, "1");
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		GETEntityCommand command = new GETEntityCommand(mockProducer);
			
		command.execute(mockContext);
		
		verify(mockProducer).getEntity(any(String.class), any(OEntityKey.class), argThat(new ArgumentMatcher<EntityQueryInfo>() {
		
			@Override
			public boolean matches(Object argument) {
				boolean result = false;
				
				if(argument instanceof QueryInfo) {
					QueryInfo queryInfo = (QueryInfo)argument;
																	
					Map<String,String> customOptions = queryInfo.customOptions;
					
					if("a eq 'b'".equals(customOptions.get("$filter"))) {
					    if("a".equals(customOptions.get("$select"))) {
					        if("MYVALUE".equals(customOptions.get("MYPARAM"))) {
	                            result = true;
	                        }
					    }
					}
				}
				
				return result;
			}			
		}));	
	}
	

	@Test
	public void testDeleteEntityNotFound() {
		ODataProducer mockProducer = createMockODataProducer("DeleteEntityNotFound", "Edm.String");
		DeleteEntityCommand gec = new DeleteEntityCommand(mockProducer);
		
        InteractionContext ctx = createInteractionContext("DeleteEntityNotFound", "1");
        try {
        	gec.execute(ctx);
        	fail("Should have thrown exception");
        }
        catch(InteractionException ie) {
        	assertEquals(Status.NOT_FOUND, ie.getHttpStatus());
        }
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void testGetEntityIgnoreInputResource() throws InteractionException {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		GETEntityCommand gec = new GETEntityCommand(mockProducer);
		
		//Set an entity resource before calling GETEntity - it should ignore the resource
        InteractionContext ctx = createInteractionContext("MyEntity", "abc");
        ctx.setResource(CommandHelper.createEntityResource(new Entity("IgnoreThisEntity", new EntityProperties())));
		InteractionCommand.Result result = gec.execute(ctx);
		
		assertEquals(InteractionCommand.Result.SUCCESS, result);
		assertTrue(ctx.getResource() instanceof EntityResource);
		EntityResource<OEntity> er = (EntityResource<OEntity>) ctx.getResource();
		assertEquals("MyEntity", er.getEntityName());
	}
	
	private ODataProducer createMockODataProducer(String entityName, String keyTypeName) {
		ODataProducer mockProducer = mock(ODataProducer.class);
		List<String> keys = new ArrayList<String>();
		keys.add("MyId");
		List<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();
		EdmProperty.Builder ep = EdmProperty.newBuilder("MyId").setType(new MyEdmType(keyTypeName));
		properties.add(ep);
		EdmEntityType.Builder eet = EdmEntityType.newBuilder().setNamespace("MyNamespace").setAlias("MyAlias").setName(entityName).addKeys(keys).addProperties(properties);
		EdmEntitySet.Builder ees = EdmEntitySet.newBuilder().setName(entityName).setEntityType(eet);
		EdmSchema.Builder es = EdmSchema.newBuilder().setNamespace("MyNamespace");

		List<EdmEntityType> mockEntityTypes = new ArrayList<EdmEntityType>();
		mockEntityTypes.add(eet.build());
		List<EdmSchema> mockSchemas = new ArrayList<EdmSchema>();
		mockSchemas.add(es.build());
		ImmutableList<EdmSchema> mockSchemaList = ImmutableList.copyOf(mockSchemas);

		EdmDataServices mockEDS = mock(EdmDataServices.class);
		when(mockEDS.getEdmEntitySet(anyString())).thenReturn(ees.build());
		when(mockEDS.getEdmEntitySet((EdmEntityType) any())).thenReturn(ees.build());
		when(mockEDS.getEntityTypes()).thenReturn(mockEntityTypes);
		when(mockEDS.findEdmEntityType(anyString())).thenReturn(eet.build());
		when(mockEDS.getSchemas()).thenReturn(mockSchemaList);
		when(mockProducer.getMetadata()).thenReturn(mockEDS);
		

		EntityResponse mockEntityResponse = mock(EntityResponse.class);
		OEntity oe = mock(OEntity.class);
		when(oe.getEntityType()).thenReturn(eet.build());
		when(oe.getEntitySetName()).thenReturn(ees.build().getName());
		when(mockEntityResponse.getEntity()).thenReturn(oe);
		if(entityName.equals("GetEntityNotFound")) {
			when(mockProducer.getEntity(anyString(), any(OEntityKey.class), any(EntityQueryInfo.class))).thenThrow(new NotFoundException("Entity does not exist."));
		}
		else if(entityName.equals("DeleteEntityNotFound")) {
			doThrow(new NotFoundException("Entity does not exist.")).when(mockProducer).deleteEntity(anyString(), any(OEntityKey.class));
		}
		else {
			when(mockProducer.getEntity(anyString(), any(OEntityKey.class), any(EntityQueryInfo.class))).thenReturn(mockEntityResponse);
		}
        return mockProducer;
	}
	
	private InteractionContext createInteractionContext(String entity, MultivaluedMap<String, String> queryParams, String id) {
		ResourceState resourceState = mock(ResourceState.class);
		when(resourceState.getEntityName()).thenReturn(entity);
        InteractionContext ctx = mock(InteractionContext.class);
        when(ctx.getCurrentState()).thenReturn(resourceState);
        when(ctx.getQueryParameters()).thenReturn(queryParams);
        //new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), mock(MultivaluedMap.class), queryParams ,resourceState, mock(Metadata.class));
        when(ctx.getId()).thenReturn(id);
        
        return ctx;
	}
	
	
	@SuppressWarnings("unchecked")
	private InteractionContext createInteractionContext(String entity, String id) {
		ResourceState resourceState = mock(ResourceState.class);
		when(resourceState.getEntityName()).thenReturn(entity);
		MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();
		pathParams.add("id", id);

		InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams, mock(MultivaluedMap.class), resourceState, mock(Metadata.class));
        return ctx;
	}

}
