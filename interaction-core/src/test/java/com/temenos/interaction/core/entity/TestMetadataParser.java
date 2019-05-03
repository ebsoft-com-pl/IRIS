package com.temenos.interaction.core.entity;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.mockito.Mockito.*;

import java.io.InputStream;
import java.util.Set;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import com.temenos.interaction.core.entity.vocabulary.Term;
import com.temenos.interaction.core.entity.vocabulary.TermFactory;
import com.temenos.interaction.core.entity.vocabulary.terms.TermIdField;
import com.temenos.interaction.core.entity.vocabulary.terms.TermMandatory;
import com.temenos.interaction.core.entity.vocabulary.terms.TermValueType;

public class TestMetadataParser {
	public final static String METADATA_XML_FILE = "TestMetadataParser.xml";
	public final static String METADATA_CUSTOMER_NON_EXPANDED_XML_FILE = "CustomerNonExpandedMetadata.xml";

	private static Metadata metadata;
	private static Metadata metadataNonExpanded;
	
	@BeforeClass
	public static void setup()
	{
		TermFactory termFactory = new TermFactory() {
			public Term createTerm(String name, String value) throws Exception {
				if(name.equals("TEST_ENTITY_ALIAS")) {
					Term mockTerm = mock(Term.class);
					when(mockTerm.getValue()).thenReturn(value);
					when(mockTerm.getName()).thenReturn(name);
					return mockTerm;
				}
				else {
					return super.createTerm(name, value);
				}
			}			
		};
		MetadataParser parser = new MetadataParser(termFactory);
		InputStream is = parser.getClass().getClassLoader().getResourceAsStream(METADATA_XML_FILE);
		metadata = parser.parse(is);
		Assert.assertNotNull(metadata);

		MetadataParser parserNonExpanded = new MetadataParser();
		InputStream isNonExpanded = parserNonExpanded.getClass().getClassLoader().getResourceAsStream(METADATA_CUSTOMER_NON_EXPANDED_XML_FILE);
		metadataNonExpanded = parserNonExpanded.parse(isNonExpanded);
		Assert.assertNotNull(metadataNonExpanded);
	}
	
	@Test
	public void testModelName()
	{	
		Assert.assertEquals("CustomerServiceTest", metadata.getModelName());
	}
	
	@Test
	public void testPropertyVocabularyKeySet()
	{	
		Set<String> propertyKeys = metadata.getEntityMetadata("Customer").getPropertyVocabularyKeySet();
		Assert.assertEquals(12, propertyKeys.size());
		Assert.assertTrue(propertyKeys.contains("name"));
		Assert.assertTrue(propertyKeys.contains("address.street.streetType"));
		Assert.assertTrue(propertyKeys.contains("industry"));
	}
	
	@Test
	public void testIsPropertyComplex()
	{		
		EntityMetadata md = metadata.getEntityMetadata("Customer");
		Assert.assertFalse(md.isPropertyComplex("name"));
		Assert.assertTrue(md.isPropertyComplex("address"));
		Assert.assertFalse(md.isPropertyComplex("number"));
		Assert.assertTrue(md.isPropertyComplex("address.street"));
		Assert.assertFalse(md.isPropertyComplex("address.street.streetType"));
		Assert.assertFalse(md.isPropertyComplex("address.town"));
		Assert.assertFalse(md.isPropertyComplex("address.postCode"));
		Assert.assertFalse(md.isPropertyComplex("dateOfBirth"));
		Assert.assertFalse(md.isPropertyComplex("sector"));
		Assert.assertFalse(md.isPropertyComplex("industry"));
	}

	@Test
	public void testIsPropertyComplexNonExpanded()
	{		
		EntityMetadata md = metadataNonExpanded.getEntityMetadata("Customer");
		Assert.assertFalse(md.isPropertyComplex("name"));
		Assert.assertTrue(md.isPropertyComplex("address"));
		Assert.assertFalse(md.isPropertyComplex("number"));
		Assert.assertTrue(md.isPropertyComplex("street"));
		Assert.assertFalse(md.isPropertyComplex("streetType"));
		Assert.assertFalse(md.isPropertyComplex("town"));
		Assert.assertFalse(md.isPropertyComplex("postCode"));
		Assert.assertFalse(md.isPropertyComplex("dateOfBirth"));
		Assert.assertFalse(md.isPropertyComplex("sector"));
		Assert.assertFalse(md.isPropertyComplex("industry"));
	}
	
	@Test
	public void testGetComplexGroup()
	{		
		EntityMetadata md = metadata.getEntityMetadata("Customer");
		Assert.assertEquals("", md.getPropertyComplexGroup("name"));
		Assert.assertEquals("", md.getPropertyComplexGroup("address"));
		Assert.assertEquals("address", md.getPropertyComplexGroup("address.number"));
		Assert.assertEquals("address", md.getPropertyComplexGroup("address.street"));
		Assert.assertEquals("address.street", md.getPropertyComplexGroup("address.street.streetType"));
		Assert.assertEquals("address", md.getPropertyComplexGroup("address.town"));
		Assert.assertEquals("address", md.getPropertyComplexGroup("address.postCode"));
		Assert.assertEquals("", md.getPropertyComplexGroup("dateOfBirth"));
		Assert.assertEquals("", md.getPropertyComplexGroup("sector"));
		Assert.assertEquals("", md.getPropertyComplexGroup("industry"));
	}

	@Test
	public void testGetComplexGroupNonExpanded()
	{		
		EntityMetadata md = metadataNonExpanded.getEntityMetadata("Customer");
		Assert.assertEquals("", md.getPropertyComplexGroup("name"));
		Assert.assertEquals("", md.getPropertyComplexGroup("address"));
		Assert.assertEquals("address", md.getPropertyComplexGroup("number"));
		Assert.assertEquals("address", md.getPropertyComplexGroup("street"));
		Assert.assertEquals("street", md.getPropertyComplexGroup("streetType"));
		Assert.assertEquals("address", md.getPropertyComplexGroup("town"));
		Assert.assertEquals("address", md.getPropertyComplexGroup("postCode"));
		Assert.assertEquals("", md.getPropertyComplexGroup("dateOfBirth"));
		Assert.assertEquals("", md.getPropertyComplexGroup("sector"));
		Assert.assertEquals("", md.getPropertyComplexGroup("industry"));
	}

	@Test
	public void testIsPropertyText()
	{		
		EntityMetadata md = metadata.getEntityMetadata("Customer");
		Assert.assertTrue(md.isPropertyText("name"));
		Assert.assertTrue(md.isPropertyText("address"));
		Assert.assertFalse(md.isPropertyText("address.number"));
		Assert.assertTrue(md.isPropertyText("address.street"));
		Assert.assertTrue(md.isPropertyText("address.street.streetType"));
		Assert.assertTrue(md.isPropertyText("address.town"));
		Assert.assertTrue(md.isPropertyText("address.postCode"));
		Assert.assertFalse(md.isPropertyText("dateOfBirth"));
		Assert.assertTrue(md.isPropertyText("sector"));
		Assert.assertTrue(md.isPropertyText("industry"));
	}
	
	@Test
	public void testIsPropertyNumber()
	{		
		EntityMetadata md = metadata.getEntityMetadata("Customer");
		Assert.assertFalse(md.isPropertyNumber("name"));
		Assert.assertFalse(md.isPropertyNumber("address"));
		Assert.assertTrue(md.isPropertyNumber("address.number"));
		Assert.assertFalse(md.isPropertyNumber("address.street"));
		Assert.assertFalse(md.isPropertyNumber("address.street.streetType"));
		Assert.assertFalse(md.isPropertyNumber("address.town"));
		Assert.assertFalse(md.isPropertyNumber("address.postCode"));
		Assert.assertFalse(md.isPropertyNumber("dateOfBirth"));
		Assert.assertFalse(md.isPropertyNumber("sector"));
		Assert.assertFalse(md.isPropertyNumber("industry"));
		Assert.assertFalse(md.isPropertyNumber("loyal"));
		Assert.assertTrue(md.isPropertyNumber("loyalty_rating"));
	}
	
	@Test
	public void testGetTermValue()
	{		
		EntityMetadata md = metadata.getEntityMetadata("Customer");
		Assert.assertEquals(TermValueType.INTEGER_NUMBER, md.getTermValue("address.number", TermValueType.TERM_NAME));
		Assert.assertEquals(TermValueType.TEXT, md.getTermValue("sector", TermValueType.TERM_NAME));
		Assert.assertEquals("false", md.getTermValue("sector", TermMandatory.TERM_NAME));
		Assert.assertEquals("true", md.getTermValue("dateOfBirth", TermMandatory.TERM_NAME));
		Assert.assertEquals("true", md.getTermValue("name", TermIdField.TERM_NAME));
		Assert.assertEquals("false", md.getTermValue("dateOfBirth", TermIdField.TERM_NAME));
	}	

	@Test
	public void testCustomTermFactory()
	{	
		TermFactory termFactory = new TermFactory() {
			public Term createTerm(String name, String value) throws Exception {
				if(name.equals("TEST_ENTITY_ALIAS")) {
					Term mockTerm = mock(Term.class);
					when(mockTerm.getValue()).thenReturn(value);
					when(mockTerm.getName()).thenReturn(name);
					return mockTerm;
				}
				else if(name.equals(TermIdField.TERM_NAME)) {
					return new TermIdField(false);		//Override all ID fields to false
				}
				else {
					return super.createTerm(name, value);
				}
			}			
		};
		MetadataParser parser = new MetadataParser(termFactory);
		InputStream is = parser.getClass().getClassLoader().getResourceAsStream(METADATA_XML_FILE);
		Metadata metadata = parser.parse(is);
		Assert.assertNotNull(metadata);
		EntityMetadata md = metadata.getEntityMetadata("Customer");
		Assert.assertEquals("false", md.getTermValue("name", TermIdField.TERM_NAME));
	}

	@Test
	public void testNonCustomTermFactory()
	{	
		MetadataParser parser = new MetadataParser();
		InputStream is = parser.getClass().getClassLoader().getResourceAsStream(METADATA_XML_FILE);
		Metadata metadata = parser.parse(is);
		Assert.assertNull(metadata);	//Expecting parse error (Term TEST_ENTITY_ALIAS does not exist)
	}
	
	@Test
	public void testEntityTerm()
	{	
		EntityMetadata md = metadata.getEntityMetadata("Customer");
		Assert.assertEquals("MyCustomer", md.getTermValue("TEST_ENTITY_ALIAS"));
	}
}
