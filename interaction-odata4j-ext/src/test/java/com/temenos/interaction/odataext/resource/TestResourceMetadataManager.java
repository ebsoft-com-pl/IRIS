package com.temenos.interaction.odataext.resource;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.odata4j.core.ImmutableList;
import org.odata4j.edm.EdmDataServices;
import org.odata4j.edm.EdmEntityContainer;
import org.odata4j.edm.EdmEntitySet;
import org.odata4j.edm.EdmEntityType;
import org.odata4j.edm.EdmProperty;
import org.odata4j.edm.EdmSchema;
import org.odata4j.edm.EdmSimpleType;
import org.odata4j.producer.ODataProducer;

import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
import com.temenos.interaction.odataext.resource.ResourceMetadataManager;

public class TestResourceMetadataManager {

	@Test
	public void testMetadata() throws Exception {
		final ODataProducer producerA = createMockODataProducer("A");
		ResourceMetadataManager mdProducer = new ResourceMetadataManager(null) {
			protected Metadata parseMetadataXML() {
				return null;
			}
			protected EdmDataServices populateOData4jMetadata(Metadata metadata, ResourceStateMachine hypermediaEngine) {
				return producerA.getMetadata();
			}
		};
		assertTrue(mdProducer.getOData4jMetadata() != null);

		EdmDataServices metadata = mdProducer.getOData4jMetadata();
		EdmDataServices metadataA = producerA.getMetadata();
		assertTrue(metadata.equals(metadataA)); 
	}

	@Test
	public void testEntityType() throws Exception {
		final ODataProducer producerA = createMockODataProducer("A");
		ResourceMetadataManager mdProducer = new ResourceMetadataManager(null) {
			protected Metadata parseMetadataXML() {
				return null;
			}
			protected EdmDataServices populateOData4jMetadata(Metadata metadata, ResourceStateMachine hypermediaEngine) {
				return producerA.getMetadata();
			}
		};
		assertTrue(mdProducer.getOData4jMetadata() != null);

		EdmDataServices metadata = mdProducer.getOData4jMetadata();
		assertTrue(metadata.findEdmEntityType("MyNamespaceA.FlightA").getFullyQualifiedTypeName().equals("MyNamespaceA.FlightA"));
	}
	
	private ODataProducer createMockODataProducer(String suffix) {
		ODataProducer mockProducer = mock(ODataProducer.class);
		EdmDataServices mockEDS = createMetadata(suffix);
		when(mockProducer.getMetadata()).thenReturn(mockEDS);
	
		return mockProducer;
	}			

	private EdmDataServices createMetadata(String suffix) {
		EdmDataServices mockEDS = mock(EdmDataServices.class);

		//Mock EdmDataServices
		List<String> keys = new ArrayList<String>();
		keys.add("MyId" + suffix);
		List<EdmProperty.Builder> properties = new ArrayList<EdmProperty.Builder>();
		EdmProperty.Builder ep = EdmProperty.newBuilder("MyId" + suffix).setType(EdmSimpleType.STRING);
		properties.add(ep);
		EdmEntityType.Builder eet = EdmEntityType.newBuilder().setNamespace("MyNamespace" + suffix).setAlias("MyAlias" + suffix).setName("Flight" + suffix).addKeys(keys).addProperties(properties);
		EdmEntitySet.Builder ees = EdmEntitySet.newBuilder().setName("Flight" + suffix).setEntityType(eet);
		List<EdmEntityType.Builder> mockEntityTypes = new ArrayList<EdmEntityType.Builder>();
		mockEntityTypes.add(eet);
		List<EdmEntitySet.Builder> mockEntitySets = new ArrayList<EdmEntitySet.Builder>();
		mockEntitySets.add(ees);
		EdmEntityContainer.Builder eec = EdmEntityContainer.newBuilder().setName("MyEntityContainer" + suffix).addEntitySets(mockEntitySets);
		List<EdmEntityContainer.Builder> mockEntityContainers = new ArrayList<EdmEntityContainer.Builder>();
		mockEntityContainers.add(eec);
		EdmSchema.Builder es = EdmSchema.newBuilder().setNamespace("MyNamespace" + suffix).setAlias("MyAlias" + suffix).addEntityTypes(mockEntityTypes).addEntityContainers(mockEntityContainers);
		List<EdmSchema> mockSchemas = new ArrayList<EdmSchema>();
		mockSchemas.add(es.build());
		when(mockEDS.getSchemas()).thenReturn(ImmutableList.copyOf(mockSchemas));

		when(mockEDS.findEdmEntityType("MyNamespaceA.FlightA")).thenReturn(eet.build());

		return mockEDS;
	}
}
