package com.temenos.interaction.odataext.odataparser;

/*
 * Test class for the oData parser/printer orderby operations.
 */

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.junit.Test;

import com.temenos.interaction.odataext.odataparser.output.OutputExpressionVisitor;

public class ODataParserOrderByTest {

    @Test
    public void testAscending() {
        testValid("col asc");
    }

    @Test
    public void testDescending() {
        testValid("col desc");
    }

    @Test
    public void testDefault() {
        testValid("col");
    }

    @Test
    public void testMultiple() {
        testValid("col asc, col2 desc, col3");
    }

    @Test
    public void testNull() {
        assertEquals(null, ODataParser.parseOrderBy(null));
    }
    
    @Test
    public void testVisitor() {
        testValid("col asc", true);
    }

    /**
     * Test invalid order by throw.
     */
    @Test
    public void testBadOrderBy() {
        // Can't parse a null string.
        testInvalid(null);

        // Bad direction
        testInvalid("col rubbish");

        // Wrong number of element
        testInvalid("a b c");
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
                actual = ODataParser.toOrderBy(ODataParser.parseOrderBy(expected), new OutputExpressionVisitor());
            } else {
                actual = ODataParser.toOrderBy(ODataParser.parseOrderBy(expected));
            }
        } catch (Exception caught) {
            threw = true;
            e = caught;
        }

        assertFalse("Threw : " + e, threw);

        // Convert to lists. The order is important.
        List<String> expectedList = Arrays.asList(expected.split("\\s*,\\s*"));
        List<String> actualList = Arrays.asList(actual.split("\\s*,\\s*"));

        assertEquals(expectedList.size(), actualList.size());

        for (int i = 0; i < expectedList.size(); i++) {
            assertTrue(match(expectedList.get(i), actualList.get(i)));
        }
    }

    /*
     * Check if two clauses match. In ascending case "asc" is optional.
     */
    private boolean match(String expected, String actual) {
        if (expected.equals(actual)) {
            return true;
        }

        // If either ends in "asc" remove it.
        if (expected.endsWith(" asc")) {
            expected = expected.replace(" asc", "");
        }
        if (actual.endsWith(" asc")) {
            actual = actual.replace(" asc", "");
        }
        return expected.equals(actual);
    }

    // Test invalid term throws
    private void testInvalid(String expected) {

        boolean threw = false;
        try {
            ODataParser.toOrderBy(ODataParser.parseOrderBy(expected));
        } catch (Exception e) {
            threw = true;
        }
        assertTrue(threw);
    }

}
