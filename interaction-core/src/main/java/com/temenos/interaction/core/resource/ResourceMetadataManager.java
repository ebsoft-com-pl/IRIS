package com.temenos.interaction.core.resource;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.entity.MetadataParser;
import com.temenos.interaction.core.entity.vocabulary.TermFactory;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;



/**
 * This class provides EDM metadata for the current service.
 */
public class ResourceMetadataManager {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ResourceMetadataManager.class);
	
	private Metadata metadata;
	private TermFactory termFactory;
	private AbstractConfigLoaders configLoader = new ConfigLoader();
	
	/**
	 * Construct the metadata object
	 */
	public ResourceMetadataManager(Metadata metadata, ResourceStateMachine hypermediaEngine)
	{
		this.metadata = metadata;
	}

	/**
	 * Construct the metadata object
	 */
	@Deprecated
	public ResourceMetadataManager(String metdataXml, ResourceStateMachine hypermediaEngine)
	{
		metadata = parseMetadataXML(metdataXml);
	}
	
	/**
	 * Construct the metadata object
	 */
	@Deprecated
	public ResourceMetadataManager(ResourceStateMachine hypermediaEngine)
	{
		metadata = parseMetadataXML();
	}

	/**
	 * Construct the metadata object
	 */
	public ResourceMetadataManager(ResourceStateMachine hypermediaEngine, TermFactory termFactory)
	{
		metadata = configLoader.parseMetadataXML(termFactory);
		this.termFactory = termFactory;
	}
	
	/*
	 * construct termFactory & abstractConfigLoader
	 */
	public ResourceMetadataManager(TermFactory termFactory, AbstractConfigLoaders configLoader)
	{
		this.termFactory = termFactory;
		this.configLoader = configLoader;
	}
	
	/*
     * construct termFactory & Metadata
     */
    public ResourceMetadataManager(TermFactory termFactory, Metadata entityMetadata)
    {
        this.termFactory = termFactory;
        this.metadata = entityMetadata;
    }
	
	/*
	 * construct only term factory
	 */
 	public ResourceMetadataManager()
	{
		termFactory = new TermFactory();
	}

	/*
	 * construct only term factory
	 */
 	public ResourceMetadataManager(TermFactory termFactory)
	{
		this.termFactory = termFactory;
	} 	
 	
	/**
	 * Return the entity model metadata
	 * @return metadata
	 */
	public Metadata getMetadata() {
		return this.metadata;
	}		
	
	/**
	 * @param configLoader the configLoader to set
	 */
	@Autowired(required = false)	
	public void setConfigLoader(ConfigLoader configLoader) {
		this.configLoader = configLoader;
	}

	/*
	 * Parse the XML metadata file with the default Vocabulary Term Factory
	 */
	protected Metadata parseMetadataXML() {
		return parseMetadataXML(new TermFactory());
	}
	
	
	 /*
     * Parse the XML metadata file
     */
	 public Metadata parseMetadataXML(TermFactory termFactory) {
	     
	     return configLoader.parseMetadataXML(termFactory);
	 }

	/*
	 * Parse the XML metadata string
	 */
	protected Metadata parseMetadataXML(String xml) {
		try {
			InputStream is = new ByteArrayInputStream(xml.getBytes());
			return new MetadataParser().parse(is);
		}
		catch(Exception e) {
			LOGGER.error("Failed to parse metadata xml: ", e);
			throw new RuntimeException("Failed to parse metadata xml: ", e);
		}
	}
	
	
	/*
	 *  get metadadata
	 */
	public Metadata getMetadata(String entityName) {
		if(termFactory == null) {
			LOGGER.error("TermFactory Missing");
			throw new RuntimeException("TermFactory Missing");
		}
		return configLoader.parseMetadataXML(entityName, termFactory);
	}


   
}
