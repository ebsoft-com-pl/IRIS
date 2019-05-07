package com.interaction.example.odata.error;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OError;
import org.odata4j.exceptions.ODataProducerException;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

/**
 * Test error responses and exception handling
 */
public class ErrorHandlingITCase {

	private final static String EXTENDED_ENTITYSET_NAME = "Extended";

	public ErrorHandlingITCase() throws Exception {
		super();
	}
	
	@Test
	public void flightError503ServiceUnavailable() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();
		try {
			consumer.
					getEntity(EXTENDED_ENTITYSET_NAME, 123).
					nav("error503").
					execute();
			fail("error503 should have returned an odata error response.");
		}
		catch(ODataProducerException ope) {
			OError error = ope.getOError();
			assertEquals("503", error.getCode());
			assertEquals("Failed to connect to resource manager.", error.getMessage());
		}
	}

	@Test
	public void flightError504GatewayTimeout() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();
		try {
			consumer.
					getEntity(EXTENDED_ENTITYSET_NAME, 123).
					nav("error504").
					execute();
			fail("error504 should have returned an odata error response.");
		}
		catch(ODataProducerException ope) {
			OError error = ope.getOError();
			assertEquals("504", error.getCode());
			assertEquals("Request has timed out.", error.getMessage());
		}
	}

	@Test
	public void flightError500CommandRuntimeException() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();
		try {
			consumer.
					getEntity(EXTENDED_ENTITYSET_NAME, 123).
					nav("error500RuntimeException").
					execute();
			fail("error500RuntimeException should have thrown an exception.");
		}
		catch(ODataProducerException ope) {
			OError error = ope.getOError();
			assertEquals("500", error.getCode());
		}
	}

	@Test
	public void flightError403Forbidden() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();
		try {
			consumer.
					getEntity(EXTENDED_ENTITYSET_NAME, 123).
					nav("error403").
					execute();
			fail("error403 should have returned an odata error response.");
		}
		catch(ODataProducerException ope) {
			OError error = ope.getOError();
			assertEquals("403", error.getCode());
			assertEquals("User is not allowed to access this resource.", error.getMessage());
		}
	}

	@Test
	public void flightError500RequestFailure() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();
		try {
			consumer.
					getEntity(EXTENDED_ENTITYSET_NAME, 123).
					nav("error500RequestFailure").
					execute();
			fail("error500RequestFailure should have returned an odata error response.");
		}
		catch(ODataProducerException ope) {
			OError error = ope.getOError();
			assertEquals("FAILURE", error.getCode());
			assertEquals("Error while processing request.", error.getMessage());
		}
	}

	@Test
	public void flightError400InvalidRequest() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();
		try {
			consumer.
					getEntity(EXTENDED_ENTITYSET_NAME, 123).
					nav("error400").
					execute();
			fail("error400 should have returned an odata error response.");
		}
		catch(ODataProducerException ope) {
			OError error = ope.getOError();
			assertEquals("INVALID_REQUEST", error.getCode());
			assertEquals("Resource manager: 4 validation errors.", error.getMessage());
		}
	}

	@Test
	public void flightError404NotFound() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();
		try {
			consumer.
					getEntity(EXTENDED_ENTITYSET_NAME, 123).
					nav("error404").
					execute();
			fail("error404 should have returned an odata error response.");
		}
		catch(ODataProducerException ope) {
			OError error = ope.getOError();
			assertEquals("404", error.getCode());
			assertEquals("Resource manager: entity not found or currently unavailable.", error.getMessage());
		}
	}
	
	@Test
	public void flightError404WithOtherErrorHandlerInSeperateRIM() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();
		try {
			consumer.
					getEntity(EXTENDED_ENTITYSET_NAME, 123).
					nav("error404WithOtherErrorHandler").
					execute();
			fail("error404 should have returned an odata error response.");
		}
		catch(ODataProducerException ope) {
			OError error = ope.getOError();
			assertEquals("404", error.getCode());
			assertEquals("Resource manager: entity not found or currently unavailable. For ErrorHanlder in seperate RIM", error.getMessage());
		}
	}
}
