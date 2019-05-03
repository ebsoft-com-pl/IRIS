package com.temenos.interaction.odataext.odataparser;

/*
 * Test class for the oData parser/printer select operations.
 * 
 * Not too concerned with the intermediate format of data but it must survive the 'round trip' into intermediate format
 * and back to a string.
 */

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odata4j.expression.EntitySimpleProperty;
import org.odata4j.producer.resources.OptionsQueryParser;

import com.temenos.interaction.odataext.odataparser.data.FieldName;
import com.temenos.interaction.odataext.odataparser.output.OutputExpressionVisitor;

public class ODataParserSelectTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test empty Selects.
     */
    @Test
    public void testEmptySelect() {
        testValid("");
    }

    /*
     * Test Select containing multiple terms
     */
    @Test
    public void testMultipleSelect() {
        testValid("a, b, c");
    }

    /*
     * Test Select containing quoted elements
     * 
     * This appears to be invalid. Doc implies that spaces in oData col names
     * are illegal. However if this is incorrect feel free to amend this test.
     */
    @Test
    public void testQuotesSelect() {
        testInvalid("'a b'");
    }

    /**
     * Test invalid Selects throw.
     */
    @Test
    public void testBadSelect() {
        // Bad condition. Not sure this is 'bad'.
        // testInvalid(",,");

        // Can't parse a null string.
        testInvalid(null);

        // Wrong number of element (unquoted)
        testInvalid("a b");
        testInvalid("a b c");
    }

    /**
     * Test null intermediate select.
     */
    @Test
    public void testNullSelect() {

        String actual = null;
        boolean threw = false;
        try {
            actual = ODataParser.toSelect(null);
        } catch (Exception e) {
            threw = true;
        }
        assertTrue("Didn't throw. Expected \"" + null + "\"Actual is \"" + actual + "\"", threw);
    }

    /*
     * Test the visitor passing interface.
     */
    @Test
    public void testVisitorSelect() {
        testValid("a, b, c", true);
    }

    // Test round trip for a valid Select
    private void testValid(String expected) {
        testValid(expected, false);
    }

    private void testValid(String expected, boolean useVisitor) {

        Exception e = null;

        String actual = null;
        boolean threw = false;
        try {
            if (useVisitor) {
                actual = ODataParser.toSelect(ODataParser.parseSelect(expected), new OutputExpressionVisitor());
            } else {
                actual = ODataParser.toSelect(ODataParser.parseSelect(expected));
            }
        } catch (Exception caught) {
            threw = true;
            e = caught;
        }

        assertFalse("Threw : " + e, threw);

        // Order may have been changed so we have to do out own parsing (which
        // could also be wrong).
        List<String> expectedList = Arrays.asList(expected.split("\\s*,\\s*"));
        List<String> actualList = Arrays.asList(actual.split("\\s*,\\s*"));

        for (String str : expectedList) {
            assertTrue("Expected \"" + expected + "\"Actual is \"" + actual + "\"", actualList.contains(str));
        }

        for (String str : actualList) {
            assertTrue("Expected \"" + expected + "\"Actual is \"" + actual + "\"", expectedList.contains(str));
        }
    }

    // Test invalid Select throws
    private void testInvalid(String expected) {

        boolean threw = false;
        try {
            ODataParser.toSelect(ODataParser.parseSelect(expected));
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw);
    }

    /**
     * Test parsing a OData4j expression
     */
    @Test
    @Deprecated
    public void testExpressionSelect() {
        List<EntitySimpleProperty> selects = OptionsQueryParser.parseSelect("a, b");

        Set<FieldName> actual = null;
        try {
            actual = ODataParser.parseSelect(selects);
        } catch (Exception e) {
            fail("Failed with " + e);
        }

        assertFalse(null == actual);
        assertEquals(2, actual.size());
        assertTrue(actual.contains(new FieldName("a")));
        assertTrue(actual.contains(new FieldName("b")));
    }

}
