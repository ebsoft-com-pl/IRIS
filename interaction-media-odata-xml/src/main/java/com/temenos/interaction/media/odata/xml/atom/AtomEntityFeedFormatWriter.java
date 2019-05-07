package com.temenos.interaction.media.odata.xml.atom;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.io.Writer;
import java.util.Collection;

import javax.ws.rs.core.UriInfo;

import org.apache.abdera.Abdera;
import org.apache.abdera.writer.StreamWriter;
import org.apache.commons.io.output.WriterOutputStream;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.odata4j.internal.InternalUtil;

import com.temenos.interaction.core.entity.Entity;
import com.temenos.interaction.core.entity.Metadata;
import com.temenos.interaction.core.hypermedia.Link;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.resource.CollectionResource;
import com.temenos.interaction.core.resource.EntityResource;

/**
 * Writes a collection resource out as an Atom XML feed. 
 */
public class AtomEntityFeedFormatWriter {
	// Constants for OData
	public static final String d = "http://schemas.microsoft.com/ado/2007/08/dataservices";
	public static final String m = "http://schemas.microsoft.com/ado/2007/08/dataservices/metadata";
	public static final String scheme = "http://schemas.microsoft.com/ado/2007/08/dataservices/scheme";
	public static final String atom_entry_content_type = "application/atom+xml;type=entry";
	public static final String href_lang = "en";

	private ResourceState serviceDocument;
	private AtomEntityEntryFormatWriter entryWriter;
	
	public AtomEntityFeedFormatWriter(ResourceState serviceDocument, Metadata metadata) {
		this.serviceDocument = serviceDocument;
		this.entryWriter = new AtomEntityEntryFormatWriter(serviceDocument, metadata);
	}
	
	/**
	 * Write a collection resource as an Atom XML feed
	 * @param uriInfo Current URI
	 * @param w Java writer to stream to atom+xml output
	 * @param collectionResource collection resource
	 * @param inlineCount inline count
	 * @param skipToken skip token
	 * @param modelName Model name
	 * @param queryToken query token value
	 * @param entityMetadata Metadata of entity
	 */
	public void write(UriInfo uriInfo,
			Writer w,
			CollectionResource<Entity> collectionResource,
			Integer inlineCount,
			String skipToken,
			String modelName, String queryToken) {
		String baseUri = AtomXMLProvider.getBaseUri(serviceDocument, uriInfo);
		String entitySetName = collectionResource.getEntitySetName();
		Collection<Link> links = collectionResource.getLinks();

		DateTime utc = new DateTime().withZone(DateTimeZone.UTC);
		String updated = InternalUtil.toString(utc);

		Abdera abdera = new Abdera();
		StreamWriter writer = abdera.newStreamWriter();
		writer.setOutputStream(new WriterOutputStream(w));
		writer.setAutoflush(false);
		writer.setAutoIndent(true);
		writer.startDocument();
	    writer.startFeed();
	    
	    //Write attributes
	    writer.writeNamespace("m", m);
	    writer.writeNamespace("d", d);
	    writer.writeAttribute("xml:base", baseUri);

	    //Write elements
	    writeElement(writer, "title", entitySetName, "type", "text");
	    writeElement(writer, "id", AtomXMLProvider.getAbsolutePath(uriInfo));
	    writeElement(writer, "updated", updated);

	    assert(links != null);
	    for (Link link : links) {
	    	String href = link.getRelativeHref(baseUri);
	    	String title = link.getTitle();
			String rel = link.getRel();
	        writeElement(writer, "link", null, "rel", rel, "title", title, "href", href);
	    }

	    if (inlineCount != null) {
	      writeElement(writer, "m:count", inlineCount.toString());
	    }
	    
	    //Write entries
	    for (EntityResource<Entity> entityResource : collectionResource.getEntities()) {
			assert(collectionResource.getEntityName().equals(entityResource.getEntityName()));
	    	entryWriter.writeEntry(writer, entitySetName, entityResource.getEntityName(), entityResource.getEntity(), entityResource.getLinks(), entityResource.getEmbedded(), uriInfo, updated);
	    }

	    if (skipToken != null) {
	      String nextHref = uriInfo.getRequestUriBuilder().replaceQueryParam("$skiptoken", skipToken).build().toString();
	      writeElement(writer, "link", null, "rel", "next", "href", nextHref);
	    }
		
	    if (queryToken != null) {
	      String searchString = "$queryToken";

	      Boolean isPresent = uriInfo.getRequestUriBuilder().build().toString().contains("%24queryToken");
	      if(isPresent){
	          searchString = "%24queryToken";
	      }
	      String nextHref = uriInfo.getRequestUriBuilder().replaceQueryParam(searchString, queryToken).build().toString();
	      writeElement(writer, "link", null, "rel", "next", "href", nextHref);
	    }
		writer.endFeed();
		writer.endDocument();
		writer.flush();
	}

	
	protected void writeElement(StreamWriter writer, String elementName, String elementText, String... attributes) {
		writer.startElement(elementName, "http://www.w3.org/2005/Atom");
		for (int i = 0; i < attributes.length; i += 2) {
			writer.writeAttribute(attributes[i], attributes[i + 1]);
		}
		if (elementText != null) {
			writer.writeElementText(elementText);
		}
		writer.endElement();
	}
}
