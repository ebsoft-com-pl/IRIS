package com.interaction.example.odata.airline;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.text.DateFormat;
import java.util.Locale;

import org.core4j.Enumerable;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.junit.Assert;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.core.OProperties;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

/**
 * This test is a copy of odata4j-fit
 * org.odata4j.examples.producer.jpa.airline.test
 * 
 * @author aphethean
 */
public class EdmTimeTemporalITCase {

	private final static String FLIGHT_SCHEDULE_ENTITYSET_NAME = "FlightSchedules";
	private final static String PASSENGERS_ENTITYSET_NAME = "Passengers";

	public EdmTimeTemporalITCase() throws Exception {
		super();
	}
	
	@Test
	public void testMetadata() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();

		EdmDataServices metadata = consumer.getMetadata();

		Assert.assertEquals(EdmSimpleType.TIME,
				metadata.findEdmEntitySet(FLIGHT_SCHEDULE_ENTITYSET_NAME).getType()
						.findProperty("departureTime").getType());
		Assert.assertEquals(EdmSimpleType.TIME,
				metadata.findEdmEntitySet(FLIGHT_SCHEDULE_ENTITYSET_NAME).getType()
						.findProperty("arrivalTime").getType());
		Assert.assertEquals(EdmSimpleType.DATETIME,
				metadata.findEdmEntitySet(FLIGHT_SCHEDULE_ENTITYSET_NAME).getType()
						.findProperty("firstDeparture").getType());
	}

	@Test
	public void testComplexMetadata() {
		// TODO: Test assertions will be enabled to verify metadata for complex types  
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();
		
		EdmDataServices metadata = consumer.getMetadata();

		Assert.assertEquals(EdmSimpleType.STRING,
				metadata.findEdmEntitySet(PASSENGERS_ENTITYSET_NAME).getType()
						.findProperty("name").getType());
		Assert.assertEquals(EdmSimpleType.DATETIME,
				metadata.findEdmEntitySet(PASSENGERS_ENTITYSET_NAME).getType()
						.findProperty("dateOfBirth").getType());
//		Assert.assertEquals(false,
//				metadata.findEdmEntitySet(PASSENGERS_ENTITYSET_NAME).getType()
//						.findProperty("Passenger_address").getType().isSimple());
//		Assert.assertEquals(null,
//				metadata.findEdmEntitySet(PASSENGERS_ENTITYSET_NAME).getType()
//						.findProperty("Passenger_street"));
//		Assert.assertEquals(false,
//				((EdmComplexType)metadata.findEdmEntitySet(PASSENGERS_ENTITYSET_NAME).getType()
//						.findProperty("Passenger_address").getType()).findProperty("Passenger_street").getType().isSimple());
	}
	
	
	@Test
	/**
	 *handling of Date fields with different @Temporal
	 */
	public void createWithDifferentTemporal() throws Exception {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI)).build();

		OEntity flightSchedule = consumer
				.createEntity(FLIGHT_SCHEDULE_ENTITYSET_NAME)
				.properties(OProperties.string("flightNo", "LH460"))
				.properties(OProperties.string("arrivalAirportCode", "MUC"))
				.properties(
						OProperties.time("departureTime", new LocalTime(9, 30,
								0)))
				.properties(
						OProperties.time("arrivalTime", DateFormat
								.getTimeInstance(DateFormat.SHORT, Locale.US)
								.parse("2:10 pm")))
				.properties(OProperties.string("departureAirportCode", "JFK"))
				.properties(
						OProperties.datetime("firstDeparture",
								new LocalDateTime(2011, 03, 28, 9, 30)))
				.properties(
						OProperties.datetime("lastDeparture", DateFormat
								.getDateInstance(DateFormat.SHORT, Locale.US)
								.parse("07/05/2011"))).execute();

		Long id = (Long) flightSchedule.getProperty("flightScheduleID")
				.getValue();
		Assert.assertEquals(new LocalTime(9, 30, 0), flightSchedule
				.getProperty("departureTime").getValue());
		Assert.assertEquals(new LocalTime(14, 10, 0), flightSchedule
				.getProperty("arrivalTime").getValue());
		Assert.assertEquals(new LocalDateTime(2011, 03, 28, 9, 30),
				flightSchedule.getProperty("firstDeparture").getValue());
		Assert.assertEquals(new LocalDateTime(2011, 07, 05, 0, 0),
				flightSchedule.getProperty("lastDeparture").getValue());

		flightSchedule = consumer.getEntity(FLIGHT_SCHEDULE_ENTITYSET_NAME, id).execute();
		Assert.assertEquals(new LocalTime(9, 30, 0), flightSchedule
				.getProperty("departureTime").getValue());
		Assert.assertEquals(new LocalTime(14, 10, 0), flightSchedule
				.getProperty("arrivalTime").getValue());
		Assert.assertEquals(new LocalDateTime(2011, 03, 28, 9, 30),
				flightSchedule.getProperty("firstDeparture").getValue());
		Assert.assertEquals(new LocalDateTime(2011, 07, 05, 0, 0),
				flightSchedule.getProperty("lastDeparture").getValue());
	}

	@Test
	public void filterTime() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(ConfigurationHelper.getTestEndpointUri(ConfigurationHelper.getTestEndpointUri(Configuration.TEST_ENDPOINT_URI))).build();

		Enumerable<OEntity> schedules = consumer
				.getEntities(FLIGHT_SCHEDULE_ENTITYSET_NAME)
				.filter("departureTime ge time'PT11H' and departureTime lt time'PT12H'")
				.execute();

		Assert.assertEquals(2, schedules.count());

	}

}
