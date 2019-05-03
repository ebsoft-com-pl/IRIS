package com.interaction.example.odata.northwind;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import org.core4j.Enumerable;
import org.junit.Assert;
import org.junit.Test;
import org.odata4j.consumer.ODataConsumer;
import org.odata4j.core.OEntity;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.jersey.consumer.ODataJerseyConsumer;

public class SimpleQueriesITCase {
	protected static final String endpointUri = "http://localhost:8080/northwind/Northwind.svc/";

	public SimpleQueriesITCase() {
	}
	
	@Test
	public void testMetadata() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(endpointUri).build();

		EdmDataServices metadata = consumer.getMetadata();

		Assert.assertEquals(EdmSimpleType.STRING, 
				metadata.findEdmEntitySet("Customers").getType().findProperty("Country").getType());
	}

	@Test
	public void getUkCustomers() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(endpointUri).build();

		Enumerable<OEntity> customers = consumer
				.getEntities("Customers")
				.filter("Country eq 'UK'")
				.execute();
		
		Assert.assertEquals(7, customers.count());
	}

	@Test
	public void getCategory() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(endpointUri).build();
		OEntity category = consumer.getEntity("Categories", 1).execute();
		Assert.assertEquals(1, category.getEntityKey().asSingleValue());
	}

	@Test
	public void getCategoryName() {
		ODataConsumer consumer = ODataJerseyConsumer.newBuilder(endpointUri).build();
		OEntity category = consumer.getEntity("Categories", 1).execute();
		Assert.assertEquals("Beverages", category.getProperty("CategoryName").getValue());
	}
}
