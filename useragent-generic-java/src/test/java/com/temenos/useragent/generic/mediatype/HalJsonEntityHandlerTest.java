package com.temenos.useragent.generic.mediatype;

import static org.hamcrest.CoreMatchers.equalTo;
/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.matchers.JUnitMatchers.containsString;

import java.io.IOException;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.temenos.useragent.generic.Link;
import com.temenos.useragent.generic.internal.EntityWrapper;
import com.temenos.useragent.generic.internal.Payload;

public class HalJsonEntityHandlerTest {

	private HalJsonEntityHandler entityHandler;

	@Before
	public void setUp() {
		initEntityHandler("/haljson_item_with_all_properties.json");
	}

	@Test
	public void testGetValueForSimpleProperty() {
		assertEquals("2002", entityHandler.getValue("AccountOfficer"));
		assertEquals("", entityHandler.getValue("AllowBulkProcess"));
		assertEquals("GB0010001", entityHandler.getValue("CoCode"));
		assertNull(entityHandler.getValue("Foo"));
	}

	@Test
	public void testGetValueForNestedProperty() {
		assertEquals("INCORP/CUS*200 FROM 66052 NOT RECEIVED",
				entityHandler.getValue("OverrideGroup(0)/Override"));
		assertEquals("MEMORANDUM/CUS*200 FROM 66052 NOT RECEIVED",
				entityHandler.getValue("OverrideGroup(1)/Override"));
		assertNull(entityHandler.getValue("Foo(1)/Bar"));
		assertNull(entityHandler.getValue("OverrideGroup(1)/Foo"));

		assertEquals("1", entityHandler.getValue("CommTypeGroup(0)/CommType"));
		assertEquals("EMAIL",
				entityHandler.getValue("CommTypeGroup(0)/PrefChannel"));
		assertEquals("2", entityHandler.getValue("CommTypeGroup(1)/CommType"));
		assertEquals("MOBILE",
				entityHandler.getValue("CommTypeGroup(1)/PrefChannel"));
	}

	@Test
	public void testGetValueForDeepNestedProperty() {
		assertEquals("94086 Main street",
				entityHandler
						.getValue("AddressGroup(0)/AddressSubGroup(0)/Address"));
		assertEquals(
				"en",
				entityHandler
						.getValue("AddressGroup(0)/AddressSubGroup(0)/LanguageCode"));
		assertEquals("94086 Rue principale",
				entityHandler
						.getValue("AddressGroup(0)/AddressSubGroup(1)/Address"));
		assertEquals(
				"fr",
				entityHandler
						.getValue("AddressGroup(0)/AddressSubGroup(1)/LanguageCode"));

	}

	@Test
	public void testGetValueForMixedNestedProperty() {
		assertEquals(
				"2486 Main street",
				entityHandler
						.getValue("EmploymentStatusGroup(0)/EmployersAddSubGroup(0)/EmployersAdd"));
		assertEquals("USD",
				entityHandler
						.getValue("EmploymentStatusGroup(0)/CustomerCurrency"));
		assertEquals(
				"1222 Kings road",
				entityHandler
						.getValue("EmploymentStatusGroup(1)/EmployersAddSubGroup(0)/EmployersAdd"));
		assertEquals(
				"220 Kings road",
				entityHandler
						.getValue("EmploymentStatusGroup(1)/EmployersAddSubGroup(1)/EmployersAdd"));
		assertEquals("GBP",
				entityHandler
						.getValue("EmploymentStatusGroup(1)/CustomerCurrency"));

	}

	@Test
	public void testGetCountForNestedProperty() {
		assertEquals(2, entityHandler.getCount("OverrideGroup"));
		assertEquals(2, entityHandler.getCount("CommTypeGroup"));
		assertEquals(0, entityHandler.getCount("Foo"));
	}

	@Test
	public void testGetCountForDeepNestedProperty() {
		assertEquals(1, entityHandler.getCount("AddressGroup"));
		assertEquals(2,
				entityHandler.getCount("AddressGroup(0)/AddressSubGroup"));
	}

	@Test
	public void testGetCountForMixedNestedProperty() {
		assertEquals(2, entityHandler.getCount("EmploymentStatusGroup"));
		assertEquals(
				1,
				entityHandler
						.getCount("EmploymentStatusGroup(0)/EmployersAddSubGroup"));
		assertEquals(
				2,
				entityHandler
						.getCount("EmploymentStatusGroup(1)/EmployersAddSubGroup"));
	}

	@Test
	public void testSetValueForSimpleProperty() throws IOException {
		entityHandler.setValue("AccountOfficer", "2003");
		assertEquals("2003", entityHandler.getValue("AccountOfficer"));
		entityHandler.setValue("CoCode", "");
		assertEquals("", entityHandler.getValue("CoCode"));
		entityHandler.setValue("AllowBulkProcess", "TRUE");
		assertEquals("TRUE", entityHandler.getValue("AllowBulkProcess"));
		entityHandler.setValue("Foo", "Bar");
		assertEquals("Bar", entityHandler.getValue("Foo"));
		
		String content = IOUtils.toString(entityHandler.getContent());
		assertTrue(content.contains("\"AccountOfficer\" : \"2003\""));
	}

	@Test
	public void testSetValueForExistingNestedProperty() {
		entityHandler.setValue("OverrideGroup(0)/Override", "foo");
		assertEquals("foo", entityHandler.getValue("OverrideGroup(0)/Override"));
		entityHandler.setValue("OverrideGroup(1)/Override", "bar");
		assertEquals("bar", entityHandler.getValue("OverrideGroup(1)/Override"));

		entityHandler.setValue("CommTypeGroup(0)/CommType", "100");
		assertEquals("100", entityHandler.getValue("CommTypeGroup(0)/CommType"));
		entityHandler.setValue("CommTypeGroup(0)/PrefChannel", "MAIL");
		assertEquals("MAIL",
				entityHandler.getValue("CommTypeGroup(0)/PrefChannel"));
		entityHandler.setValue("CommTypeGroup(1)/CommType", "200");
		assertEquals("200", entityHandler.getValue("CommTypeGroup(1)/CommType"));
		entityHandler.setValue("CommTypeGroup(1)/PrefChannel", "EMAIL");
		assertEquals("EMAIL",
				entityHandler.getValue("CommTypeGroup(1)/PrefChannel"));
	}

	@Test
	public void testSetValueForExistingDeepNestedProperty() {

		entityHandler.setValue("AddressGroup(0)/AddressSubGroup(0)/Address",
				"foo");
		assertEquals("foo",
				entityHandler
						.getValue("AddressGroup(0)/AddressSubGroup(0)/Address"));
		entityHandler.setValue(
				"AddressGroup(0)/AddressSubGroup(0)/LanguageCode", "de");
		assertEquals(
				"de",
				entityHandler
						.getValue("AddressGroup(0)/AddressSubGroup(0)/LanguageCode"));
		entityHandler.setValue("AddressGroup(0)/AddressSubGroup(1)/Address",
				"bar");
		assertEquals("bar",
				entityHandler
						.getValue("AddressGroup(0)/AddressSubGroup(1)/Address"));
		entityHandler.setValue(
				"AddressGroup(0)/AddressSubGroup(1)/LanguageCode", "en");
		assertEquals(
				"en",
				entityHandler
						.getValue("AddressGroup(0)/AddressSubGroup(1)/LanguageCode"));

	}

	@Test
	public void testSetValueForExistingMixedNestedProperty() {
		assertEquals(
				"2486 Main street",
				entityHandler
						.getValue("EmploymentStatusGroup(0)/EmployersAddSubGroup(0)/EmployersAdd"));
		assertEquals("USD",
				entityHandler
						.getValue("EmploymentStatusGroup(0)/CustomerCurrency"));
		assertEquals(
				"1222 Kings road",
				entityHandler
						.getValue("EmploymentStatusGroup(1)/EmployersAddSubGroup(0)/EmployersAdd"));
		assertEquals(
				"220 Kings road",
				entityHandler
						.getValue("EmploymentStatusGroup(1)/EmployersAddSubGroup(1)/EmployersAdd"));
		assertEquals("GBP",
				entityHandler
						.getValue("EmploymentStatusGroup(1)/CustomerCurrency"));

	}

	@Test
	public void testSetValueForNewNestedPropertyWithValidIndex() {
		entityHandler.setValue("OverrideGroup(2)/Override", "foo");
		assertEquals("foo", entityHandler.getValue("OverrideGroup(2)/Override"));
		// to make sure the existing properties untouched
		assertEquals("INCORP/CUS*200 FROM 66052 NOT RECEIVED",
				entityHandler.getValue("OverrideGroup(0)/Override"));
		assertEquals("MEMORANDUM/CUS*200 FROM 66052 NOT RECEIVED",
				entityHandler.getValue("OverrideGroup(1)/Override"));

		entityHandler.setValue("CommTypeGroup(2)/CommType", "100");
		assertEquals("100", entityHandler.getValue("CommTypeGroup(2)/CommType"));
		entityHandler.setValue("CommTypeGroup(2)/PrefChannel", "MAIL");
		assertEquals("MAIL",
				entityHandler.getValue("CommTypeGroup(2)/PrefChannel"));
		// to make sure the existing properties untouched
		assertEquals("1", entityHandler.getValue("CommTypeGroup(0)/CommType"));
		assertEquals("EMAIL",
				entityHandler.getValue("CommTypeGroup(0)/PrefChannel"));
		assertEquals("2", entityHandler.getValue("CommTypeGroup(1)/CommType"));
		assertEquals("MOBILE",
				entityHandler.getValue("CommTypeGroup(1)/PrefChannel"));

	}

	@Test
	public void testSetValueForNewNestedPropertyWithInValidIndex() {
		try {
			entityHandler.setValue("OverrideGroup(3)/Override", "bar");
			fail("IllegalArgumentException should have thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		try {
			entityHandler.setValue("CommTypeGroup(3)/CommType", "200");
			fail("IllegalArgumentException should have thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		try {
			entityHandler.setValue("CommTypeGroup(3)/PrefChannel", "EMAIL");
			fail("IllegalArgumentException should have thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetValueForNewDeepNestedPropertyWithValidIndex() {
		entityHandler.setValue("AddressGroup(0)/AddressSubGroup(2)/Address",
				"foo");
		assertEquals("foo",
				entityHandler
						.getValue("AddressGroup(0)/AddressSubGroup(2)/Address"));

		entityHandler.setValue(
				"AddressGroup(1)/AddressSubGroup(0)/LanguageCode", "de");
		assertEquals(
				"de",
				entityHandler
						.getValue("AddressGroup(1)/AddressSubGroup(0)/LanguageCode"));
	}

	@Test
	public void testSetValueForNewDeepNestedPropertyWithInvalidIndex() {
		try {
			entityHandler.setValue(
					"AddressGroup(1)/AddressSubGroup(3)/Address", "bar");
			fail("IllegalArgumentException should have thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		try {
			entityHandler.setValue(
					"AddressGroup(0)/AddressSubGroup(3)/LanguageCode", "en");
			fail("IllegalArgumentException should have thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testSetValueForNewMixedNestedPropertyWithValidIndex() {
		entityHandler
				.setValue(
						"EmploymentStatusGroup(0)/EmployersAddSubGroup(1)/EmployersAdd",
						"foo");
		assertEquals(
				"foo",
				entityHandler
						.getValue("EmploymentStatusGroup(0)/EmployersAddSubGroup(1)/EmployersAdd"));

		entityHandler.setValue("EmploymentStatusGroup(2)/CustomerCurrency",
				"SGD");
		assertEquals("SGD",
				entityHandler
						.getValue("EmploymentStatusGroup(2)/CustomerCurrency"));

		entityHandler
				.setValue(
						"EmploymentStatusGroup(2)/EmployersAddSubGroup(0)/EmployersAdd",
						"bar");
		assertEquals(
				"bar",
				entityHandler
						.getValue("EmploymentStatusGroup(2)/EmployersAddSubGroup(0)/EmployersAdd"));

	}

	@Test
	public void testSetValueForNewMixedNestedPropertyWithInValidIndex() {
		try {
			entityHandler
					.setValue(
							"EmploymentStatusGroup(0)/EmployersAddSubGroup(2)/EmployersAdd",
							"foo");
			fail("IllegalArgumentException should have thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}

		try {
			entityHandler.setValue("EmploymentStatusGroup(3)/CustomerCurrency",
					"SGD");
			fail("IllegalArgumentException should have thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

    @Test
    public void testSetPrimitiveValueForProperty() throws IOException {
        entityHandler.setValue("AccountOfficer", 2003L);
        assertEquals("2003", entityHandler.getValue("AccountOfficer"));
        entityHandler.setValue("CoCode", 'D');
        assertEquals("D", entityHandler.getValue("CoCode"));
        entityHandler.setValue("AllowBulkProcess", true);
        assertEquals("true", entityHandler.getValue("AllowBulkProcess"));
        entityHandler.setValue("Foo", "Bar");
        assertEquals("Bar", entityHandler.getValue("Foo"));
        entityHandler.setValue("Balance", 12345789.87654321);
        assertEquals("1.234578987654321E7", entityHandler.getValue("Balance"));
        entityHandler.setValue("Credit", -10203.50f);
        assertEquals("-10203.5", entityHandler.getValue("Credit"));
        entityHandler.setValue("Baz", null);
        assertEquals(null, entityHandler.getValue("Baz"));

        String content = IOUtils.toString(entityHandler.getContent());
        assertThat(content, containsString("\"AccountOfficer\" : 2003"));
        assertThat(content, containsString("\"CoCode\" : \"D\""));
        assertThat(content, containsString("\"AllowBulkProcess\" : true"));
        assertThat(content, containsString("\"Balance\" : 1.234578987654321E7"));
        assertThat(content, containsString("\"Credit\" : -10203.5"));
    }
	 
	@Test
	public void testRemovePropertyAtTheTop() {
		entityHandler.remove("AccountOfficer");
		assertNull(entityHandler.getValue("AccountOfficer"));

		entityHandler.remove("OverrideGroup");
		assertNull(entityHandler.getValue("OverrideGroup(0)/Override"));

		entityHandler.remove("CommTypeGroup");
		assertNull(entityHandler.getValue("CommTypeGroup(0)/CommType"));

		entityHandler.remove("EmploymentStatusGroup");
		assertNull(entityHandler
				.getValue("EmploymentStatusGroup(0)/EmployersAddSubGroup(0)/EmployersAdd"));
	}

	@Test
	public void testRemoveNonExistentProperty() throws Exception {
		JSONObject orignalObj = new JSONObject(IOUtils.toString(entityHandler
				.getContent()));
		entityHandler.remove("foo");
		JSONObject nonExistentPropertyRemovedObject = new JSONObject(
				IOUtils.toString(entityHandler.getContent()));
		assertTrue(nonExistentPropertyRemovedObject.similar(orignalObj));
	}

	@Test
	public void testRemoveObjectProperty() {
		entityHandler.remove("OverrideGroup(1)/Override");
		assertNull(entityHandler.getValue("OverrideGroup(1)/Override"));

		entityHandler.remove("CommTypeGroup(0)/CommType");
		assertNull(entityHandler.getValue("CommTypeGroup(0)/CommType"));
	}

	@Test
	public void testRemoveArrayProperty() {

		entityHandler.remove("OverrideGroup(0)");
		assertEquals(1, entityHandler.getCount("OverrideGroup"));

		entityHandler
				.remove("EmploymentStatusGroup(1)/EmployersAddSubGroup(0)");
		assertEquals(
				"220 Kings road",
				entityHandler
						.getValue("EmploymentStatusGroup(1)/EmployersAddSubGroup(0)/EmployersAdd"));

		entityHandler.remove("EmploymentStatusGroup(1)");
		assertNull(entityHandler
				.getValue("EmploymentStatusGroup(1)/EmployersAddSubGroup(0)/EmployersAdd"));
	}

	@Test
	public void testSetContent() {
		try {
			entityHandler.setContent(null);
			fail("IllegalArgumentException should be thrown");
		} catch (Exception e) {
			assertTrue(e instanceof IllegalArgumentException);
		}
	}

	@Test
	public void testGetContent() throws Exception {
		String contentFromEntity = IOUtils.toString(entityHandler.getContent());
		String contentFromFile = IOUtils
				.toString(HalJsonEntityHandlerTest.class
						.getResourceAsStream("/haljson_item_with_all_properties.json"));
		assertEquals(contentFromFile, contentFromEntity);
	}
	
	@Test
	public void testSetSimpleValueWithNoEntity() throws Exception {
		entityHandler = new HalJsonEntityHandler();
		entityHandler.setValue("id", "ABC");
		entityHandler.setValue("name", "RetailSuite - dev");
		assertEquals("ABC", entityHandler.getValue("id"));
		assertEquals("RetailSuite - dev", entityHandler.getValue("name"));
		assertNull(entityHandler.getValue("NonExistentProperty"));
	}
	
	@Test
	public void testSetValueForNewlyCreatedNestedProperty() {
		entityHandler = new HalJsonEntityHandler();
		entityHandler.setValue("contact(0)/type", "personal");
		entityHandler.setValue("contact(0)/status", "active");
		
		assertEquals("personal", entityHandler.getValue("contact(0)/type"));
		assertEquals("active", entityHandler.getValue("contact(0)/status"));
		assertNull(entityHandler.getValue("contact(1)/type"));
		assertNull(entityHandler.getValue("contact(1)/foo"));
	}
	
	@Test
	public void testSetValueForNewlyCreatedDeepNestedMixedProperty() {
		entityHandler = new HalJsonEntityHandler();
		
		// set values to new nested properties
		entityHandler.setValue("contact(0)/type", "personal");
		entityHandler.setValue("contact(0)/status", "active");
		entityHandler.setValue("contact(0)/emails(0)/email", "foo00@bar.com");
		entityHandler.setValue("contact(0)/emails(1)/email", "foo01@bar.com");
		
		entityHandler.setValue("contact(0)/postals(0)/preferred", "Y");
		entityHandler.setValue("contact(0)/postals(0)/regulars(0)/postcode", "REGULAR 000");
		entityHandler.setValue("contact(0)/postals(0)/urgents(0)/postcode", "URGENT 000");
		entityHandler.setValue("contact(0)/postals(0)/regulars(1)/postcode", "REGULAR 001");
		entityHandler.setValue("contact(0)/postals(1)/urgents(0)/postcode", "URGENT 010");
		
		entityHandler.setValue("contact(1)/type", "official");
		entityHandler.setValue("contact(1)/status", "inactive");
		entityHandler.setValue("contact(1)/postals(0)/urgents(0)/postcode", "URGENT 100");
		entityHandler.setValue("contact(1)/emails(0)/email", "foo10@bar.com");
		entityHandler.setValue("contact(1)/postals(1)/regulars(0)/postcode", "REGULAR 110");
		
		// check for the set values in the underlying entity
		assertEquals("personal", entityHandler.getValue("contact(0)/type"));
		assertEquals("active", entityHandler.getValue("contact(0)/status"));
		assertEquals("foo00@bar.com", entityHandler.getValue("contact(0)/emails(0)/email"));
		assertEquals("foo01@bar.com", entityHandler.getValue("contact(0)/emails(1)/email"));
		
		assertEquals("Y", entityHandler.getValue("contact(0)/postals(0)/preferred"));
		assertEquals("REGULAR 000", entityHandler.getValue("contact(0)/postals(0)/regulars(0)/postcode"));
		assertEquals("URGENT 000", entityHandler.getValue("contact(0)/postals(0)/urgents(0)/postcode"));
		assertEquals("REGULAR 001", entityHandler.getValue("contact(0)/postals(0)/regulars(1)/postcode"));
		assertEquals("URGENT 010", entityHandler.getValue("contact(0)/postals(1)/urgents(0)/postcode"));
		
		assertEquals("official", entityHandler.getValue("contact(1)/type"));
		assertEquals("inactive", entityHandler.getValue("contact(1)/status"));
		assertEquals("URGENT 100", entityHandler.getValue("contact(1)/postals(0)/urgents(0)/postcode"));
		assertEquals("foo10@bar.com", entityHandler.getValue("contact(1)/emails(0)/email"));
		assertEquals("REGULAR 110", entityHandler.getValue("contact(1)/postals(1)/regulars(0)/postcode"));
		
		assertNull(entityHandler.getValue("contact(0)/emails(0)/unknown"));
		assertNull(entityHandler.getValue("contact(0)/postals(0)/unknown"));
		assertNull(entityHandler.getValue("contact(0)/postals(0)/regulars(0)/unknown"));
		assertNull(entityHandler.getValue("contact(0)/unknown"));
	}
	
	@Test
	public void testGetValidEmbedded() {
		initEntityHandler("/haljson_item_with_inline_item.json");
		Payload embeddedPayload = entityHandler.embedded();
		assertNotNull(embeddedPayload);
		assertThat(embeddedPayload.links().size(), equalTo(2));
		
		List<Link> links = embeddedPayload.links();
		assertThat(links.get(0).embedded().isCollection(), equalTo(true));
		assertThat(links.get(1).embedded().isCollection(), equalTo(false));

		List<EntityWrapper> metadata = links.get(0).embedded().entities();
		assertThat(metadata.size(), equalTo(2));
		assertThat(metadata.get(0).get("Id"), equalTo("BankService.AccountOfficer"));
		assertThat(metadata.get(1).get("Id"), equalTo("BankService.CoCode"));

		EntityWrapper error = links.get(1).embedded().entity();
        assertThat(error.get("Errors(0)/Text"), equalTo("Duplicate"));
        assertThat(error.get("Errors(0)/Code"), equalTo("DUPLICATE"));
	}

	private void initEntityHandler(String jsonFileName) {
		entityHandler = new HalJsonEntityHandler();
		entityHandler.setContent(HalJsonEntityHandlerTest.class
				.getResourceAsStream(jsonFileName));
	}
}
