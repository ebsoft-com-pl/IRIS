package com.temenos.interaction.commands.odata;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.consumer.ODataConsumerAdapter;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmType;
import org.odata4j.producer.BaseResponse;
import org.odata4j.producer.EntityQueryInfo;
import org.odata4j.producer.EntityResponse;
import org.odata4j.producer.ODataProducer;
import org.odata4j.producer.QueryInfo;
import org.odata4j.producer.Responses;

import com.temenos.interaction.commands.odata.consumer.GETNavPropertyCommand;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;

public class TestGETNavPropertyCommand {

	class MyEdmType extends EdmType {
		public MyEdmType(String name) {
			super(name);
		}
		public boolean isSimple() { return false; }
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

		List<EdmEntityType> mockEntityTypes = new ArrayList<EdmEntityType>();
		mockEntityTypes.add(eet.build());

		EdmDataServices mockEDS = mock(EdmDataServices.class);
		when(mockEDS.getEdmEntitySet(anyString())).thenReturn(ees.build());
		when(mockEDS.getEntityTypes()).thenReturn(mockEntityTypes);
		when(mockProducer.getMetadata()).thenReturn(mockEDS);

		EntityResponse mockEntityResponse = mock(EntityResponse.class);
		when(mockEntityResponse.getEntity()).thenReturn(mock(OEntity.class));
		when(mockProducer.getEntity(anyString(), any(OEntityKey.class), any(EntityQueryInfo.class))).thenReturn(mockEntityResponse);
				        
        return mockProducer;
	}
	
	private ODataConsumer createMockODataConsumer(String entityName, String keyTypeName) {
		return new ODataConsumerAdapter(createMockODataProducer(entityName, keyTypeName));
	}
	
	@Test(expected = AssertionError.class)
	public void testEntitySetNameMatches() {
		new GETNavPropertyCommand("DOESNOTMATCH", "navProperty", createMockODataConsumer("MyEntity", "KeyType"));
	}

	@Test
	public void testInvalidKeyType() {
		ODataConsumer mockConsumer = createMockODataConsumer("MyEntity", "KeyType");
		GETNavPropertyCommand command = new GETNavPropertyCommand("MyEntity", "navProperty", mockConsumer);
		InteractionCommand.Result result = command.execute(mock(InteractionContext.class));
		assertEquals(InteractionCommand.Result.FAILURE, result);
	}

	@Test
	public void testNullQueryParams() {
		GETNavPropertyCommand command = new GETNavPropertyCommand("MyEntity", "navProperty", createMockODataConsumer("MyEntity", "Edm.String"));
		InteractionCommand.Result result = command.execute(mock(InteractionContext.class));
		assertEquals(InteractionCommand.Result.FAILURE, result);
	}

	@Test
	public void testNullNavResponse() {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		// mock return of an unsupported BaseResponse
		when(mockProducer.getNavProperty(anyString(), any(OEntityKey.class), eq("navProperty"), any(QueryInfo.class)))
			.thenReturn(new BaseResponse() {});
		ODataConsumer mockConsumer = new ODataConsumerAdapter(mockProducer);
		GETNavPropertyCommand command = new GETNavPropertyCommand("MyEntity", "navProperty", mockConsumer);
		InteractionCommand.Result result = command.execute(mock(InteractionContext.class));
		assertEquals(InteractionCommand.Result.FAILURE, result);
	}

	@Test
	public void testUnsupportedNavResponse() {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		// mock return of an unsupported BaseResponse
		when(mockProducer.getNavProperty(anyString(), any(OEntityKey.class), eq("navProperty"), any(QueryInfo.class)))
			.thenReturn(new BaseResponse() {});
		ODataConsumer mockConsumer = new ODataConsumerAdapter(mockProducer);
		GETNavPropertyCommand command = new GETNavPropertyCommand("MyEntity", "navProperty", mockConsumer);
		InteractionCommand.Result result = command.execute(mock(InteractionContext.class));
		assertEquals(InteractionCommand.Result.FAILURE, result);
	}

	@Test
	public void testPropertyNavResponse() {
		ODataProducer mockProducer = createMockODataProducer("MyEntity", "Edm.String");
		// mock return of an unsupported BaseResponse
		when(mockProducer.getNavProperty(anyString(), any(OEntityKey.class), eq("navProperty"), any(QueryInfo.class)))
			.thenReturn(Responses.property(null));
		ODataConsumer mockConsumer = new ODataConsumerAdapter(mockProducer);
		GETNavPropertyCommand command = new GETNavPropertyCommand("MyEntity", "navProperty", mockConsumer);
		InteractionCommand.Result result = command.execute(mock(InteractionContext.class));
		assertEquals(InteractionCommand.Result.FAILURE, result);
	}

}
