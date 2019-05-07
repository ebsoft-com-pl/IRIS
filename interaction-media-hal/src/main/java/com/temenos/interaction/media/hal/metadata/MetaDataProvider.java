package com.temenos.interaction.media.hal.metadata;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;

import org.odata4j.edm.EdmDataServices;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.resource.MetaDataResource;
import com.temenos.interaction.core.resource.RESTResource;
import com.temenos.interaction.core.resource.ResourceTypeHelper;

/**
 * JAX-RS Provider class for marshalling metadata resources.
 */
@Provider
@Consumes({MediaType.WILDCARD})
@Produces({MediaType.APPLICATION_JSON, com.temenos.interaction.media.hal.MediaType.APPLICATION_HAL_JSON})
public class MetaDataProvider implements MessageBodyReader<RESTResource>, MessageBodyWriter<RESTResource> {
	private final Logger logger = LoggerFactory.getLogger(MetaDataProvider.class);

	@Context
	private UriInfo uriInfo;
	
	public MetaDataProvider() {
	}

	@Override
	public boolean isWriteable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return ResourceTypeHelper.isType(type, genericType, MetaDataResource.class, Metadata.class);
	}

	@Override
	public long getSize(RESTResource t, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return -1;
	}

	/**
	 * Writes representation of {@link MetaDataResource} to the output stream.
	 * 
	 * @precondition supplied {@link MetaDataResource} is non null
	 * @precondition {@link MetaDataResource#getMetadata()} returns a valid EdmDataServices
	 * @postcondition non null meta data document written to OutputStream
	 * @invariant valid OutputStream
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void writeTo(RESTResource resource, Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, Object> httpHeaders,
			OutputStream outputStream) throws IOException,
			WebApplicationException {
		assert (resource != null);

		String outputString = "";
		if(ResourceTypeHelper.isType(type, genericType, MetaDataResource.class, Metadata.class)) {
			MetaDataResource<Metadata> metadataResource = (MetaDataResource<Metadata>) resource;
			if (mediaType.isCompatible(com.temenos.interaction.media.hal.MediaType.APPLICATION_HAL_JSON_TYPE)) {
				outputString = renderHalJSON(metadataResource.getMetadata());
			} else {
				throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
			}
		}
		else {
			throw new WebApplicationException(Response.Status.INTERNAL_SERVER_ERROR);
		}
		logger.debug("Produced [" + outputString + "]");
		outputStream.write(outputString.getBytes("UTF-8"));
		outputStream.flush();
	}

	@Override
	public boolean isReadable(Class<?> type, Type genericType,
			Annotation[] annotations, MediaType mediaType) {
		return ResourceTypeHelper.isType(type, genericType, MetaDataResource.class);
	}

	/**
	 * Reads a representation of {@link MetaDataResource} from the input stream.
	 * 
	 * @precondition {@link InputStream} contains a valid meta data representation
	 * @postcondition {@link MetaDataResource} will be constructed and returned.
	 * @invariant valid InputStream
	 */
	@Override
	public MetaDataResource<EdmDataServices> readFrom(Class<RESTResource> type,
			Type genericType, Annotation[] annotations, MediaType mediaType,
			MultivaluedMap<String, String> httpHeaders, InputStream entityStream)
			throws IOException, WebApplicationException {
		return null;
	}

	/*
	 * Render the metadata in Hal+json format
	 */
	protected String renderHalJSON(Metadata metadata) {
		//TODO
		String outputString = "";
		String entities = "";
		for(String entityName :metadata.getEntitiesMetadata().keySet()) {
			if(!entities.equals("")) {
				entities += ", ";				
			}
			entities += entityName;
		}
		outputString += "{";
		outputString += "  \"_links\" : {";
		outputString += "    \"self\" : { \"href\" : \"http://localhost:8080/example/api/$metadata\" }";
		outputString += "  },";
		outputString += "  \"modelName\" : \"" + metadata.getModelName() + "\",";
		outputString += "  \"entities\" : \"" + entities + "\"";
		outputString += "}";
		
		return outputString;
	}
}
