package com.temenos.interaction.jdbc.command;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriInfo;

import org.junit.Test;

import com.temenos.interaction.core.MultivaluedMapImpl;
import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionCommand.Result;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.resource.CollectionResource;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.jdbc.producer.JdbcProducer;
import com.temenos.interaction.odataext.odataparser.ODataParser;

/**
 * Test the GETRawCommand class.
 */
public class TestGETJdbcRecordsCommand extends AbstractJdbcCommandTest {

	/**
	 * Test constructor
	 */
	@Test
	public void testConstructor() {
		try {
			new GETJdbcRecordsCommand(mock(JdbcProducer.class));
		} catch (Throwable e) {
			fail();
		}
	}

	/*
	 * Test command execution with valid key.
	 */
	@Test
	// Don't warn about the dodgy result type cast.
	@SuppressWarnings("unchecked")
	public void testExecute() {

		// Populate the database.
		populateTestTable();

		// Create a producer
		JdbcProducer producer = null;
		try {
			producer = new JdbcProducer(dataSource);
		} catch (Exception e) {
			fail();
		}

		// Create a command based on the producer.
		GETJdbcRecordsCommand command = null;
		try {
			command = new GETJdbcRecordsCommand(producer);
		} catch (Exception e) {
			fail();
		}

		// Create an interaction context.
		MultivaluedMap<String, String> queryParams = new MultivaluedMapImpl<String>();

		// Select two columns.
		queryParams.add(ODataParser.SELECT_KEY, VARCHAR_FIELD_NAME + "," + INTEGER_FIELD_NAME);

		// Set up the path.
		MultivaluedMap<String, String> pathParams = new MultivaluedMapImpl<String>();

		// Fake up a resource state.
		ResourceState state = new ResourceState(TEST_TABLE_NAME, "rubbish", null, "rubbish");

		InteractionContext ctx = new InteractionContext(mock(UriInfo.class), mock(HttpHeaders.class), pathParams,
				queryParams, state, mock(Metadata.class));

		// Execute the command
		try {
			InteractionCommand.Result result = command.execute(ctx);

			assertEquals(Result.SUCCESS, result);
		} catch (Exception e) {
			fail();
		}

		// Check the results.
		CollectionResource<Entity> resources = (CollectionResource<Entity>) ctx.getResource();
		assertFalse(null == resources);

		int entityCount = 0;
		for (EntityResource<Entity> resource : resources.getEntities()) {
			// Should be two results VARCHAR and INTEGER but not KEY.
			assertEquals(2, resource.getEntity().getProperties().getProperties().size());
			
			// Check property values
			assertEquals(TEST_VARCHAR_DATA + entityCount, resource.getEntity().getProperties().getProperties().get(VARCHAR_FIELD_NAME).getValue());
			assertEquals(TEST_INTEGER_DATA + entityCount, resource.getEntity().getProperties().getProperties().get(INTEGER_FIELD_NAME).getValue());
			
			entityCount++;
		}

		// Should be one entity per row
		assertEquals(TEST_ROW_COUNT, entityCount);		
	}
}
