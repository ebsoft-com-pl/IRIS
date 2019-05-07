package com.temenos.interaction.odataext.odataparser.data;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.odata4j.expression.BoolCommonExpression;
import org.odata4j.producer.resources.OptionsQueryParser;

import com.temenos.interaction.odataext.odataparser.ODataParser;
import com.temenos.interaction.odataext.odataparser.ODataParser.UnsupportedQueryOperationException;

public class RowFiltersTest {

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testConstruct() {
        String expectedStr = "aname eq avalue";
        BoolCommonExpression expected = OptionsQueryParser.parseFilter(expectedStr);

        RowFilters filters = new RowFilters(expected);

        assertEquals(expected, filters.getOData4jExpression());
    }

    @Test
    public void testEmptyConstruct() {
        RowFilters filters = new RowFilters();

        assertTrue(filters.isEmpty());
    }

    @Test
    public void testStringConstruct() {
        String expectedStr = "aname eq avalue";

        RowFilters filters = new RowFilters(expectedStr);

        assertEquals(expectedStr, ODataParser.toFilters(filters));
    }

    @Test
    public void testStringConstructEmpty() {
        String expectedStr = "";
        RowFilters filters = new RowFilters("");

        assertEquals(null, filters.getOData4jExpression());
        assertEquals(expectedStr, ODataParser.toFilters(filters));
        assertTrue(filters.isEmpty());
    }

    @Test
    public void testAddFilter() {
        String expectedStr1 = "aname eq avalue";
        String expectedStr2 = "aname ne avalue";
        String expectedStr3 = "aname gt avalue";
        String expected = expectedStr1 + " and " + expectedStr2 + " and " + expectedStr3;

        RowFilters filters = new RowFilters(expectedStr1);
        filters.addFilters(expectedStr2);
        filters.addFilters(expectedStr3);

        assertEquals(expected, ODataParser.toFilters(filters));
    }
    
    @Test
    public void testAddFilters() {
        String expectedStr1 = "aname eq avalue";
        String expectedStr2 = "aname ne avalue";
        String expected = expectedStr1 + " and " + expectedStr2;

        RowFilters filters = new RowFilters(expectedStr1);
        RowFilters addFilters = new RowFilters(expectedStr2);
        
        filters.addFilters(addFilters);

        assertEquals(expected, ODataParser.toFilters(filters));
    }
    
    @Test
    public void testAddEmptyFilters() {
        String expected= "aname eq avalue";

        RowFilters filters = new RowFilters(expected);
        RowFilters addFilters = new RowFilters();
        
        filters.addFilters(addFilters);

        assertEquals(expected, ODataParser.toFilters(filters));
    }
    
    @Test
    public void testAddBlockAllFilters() {
        String expected= "aname eq avalue";

        RowFilters filters = new RowFilters(expected);
        
        // Not currently blocking all
        assertFalse(filters.isBlockAll());
        
        filters.addFilters((RowFilters)null);
        
        // Now we are blocking all
        assertTrue(filters.isBlockAll());
    }
    
    @Test
    public void testAddToBlockAllFilters() {
        String expected= "aname eq avalue";

        RowFilters filters = new RowFilters(expected);
        filters.addFilters((RowFilters)null);
        
        // Now we are blocking all
        assertTrue(filters.isBlockAll());
        
        RowFilters addFilters = new RowFilters("aname eq avalue");
        filters.addFilters(addFilters);
        
        // Should still be blocking all
        assertTrue(filters.isBlockAll());
    }
    
    @Test
    @Deprecated
    public void testAddFRowilter() {

        RowFilter filter1 = new RowFilter("aname", Relation.EQ, "avalue");
        RowFilter filter2 = new RowFilter("aname", Relation.NE, "avalue");

        List<RowFilter> filterList = new ArrayList<RowFilter>();
        filterList.add(filter1);
        filterList.add(filter2);

        String expected = ODataParser.toFilter(filterList);

        RowFilters filters = new RowFilters(filterList);

        assertEquals(expected, ODataParser.toFilters(filters));
    }

    @Test
    public void testSimpleToRowFilters() {
        String expectedStr = "aname ne avalue";

        RowFilters filters = new RowFilters(expectedStr);

        List<RowFilter> list = null;
        try {
            list = filters.asRowFilters();
        } catch (Exception e) {
            fail("Threw " + e);
        }

        assertEquals(1, list.size());

        assertEquals("aname", list.get(0).getFieldName().getName());
        assertEquals(Relation.NE, list.get(0).getRelation());
        assertEquals("avalue", list.get(0).getValue());
    }

    @Test
    public void testLiteralToRowFilters() {
        String expectedStr = "aname eq 1111";

        RowFilters filters = new RowFilters(expectedStr);

        List<RowFilter> list = null;
        try {
            list = filters.asRowFilters();
        } catch (Exception e) {
            fail("Threw " + e);
        }

        assertEquals(1, list.size());

        assertEquals("aname", list.get(0).getFieldName().getName());
        assertEquals(Relation.EQ, list.get(0).getRelation());
        assertEquals("1111", list.get(0).getValue());
    }

    @Test
    public void testStringLiteralToRowFilters() {
        String expectedStr = "aname gt 'abc'";

        RowFilters filters = new RowFilters(expectedStr);

        List<RowFilter> list = null;
        try {
            list = filters.asRowFilters();
        } catch (Exception e) {
            fail("Threw " + e);
        }

        assertEquals(1, list.size());

        assertEquals("aname", list.get(0).getFieldName().getName());
        assertEquals(Relation.GT, list.get(0).getRelation());
        assertEquals("'abc'", list.get(0).getValue());
    }

    @Test
    public void testMultipleToRowFilters() {
        String expectedStr = "aname1 eq avalue1 and aname2 ne avalue2";

        RowFilters filters = new RowFilters(expectedStr);

        List<RowFilter> list = null;
        try {
            list = filters.asRowFilters();
        } catch (Exception e) {
            fail("Threw " + e);
        }

        assertEquals(2, list.size());

        assertEquals("aname1", list.get(0).getFieldName().getName());
        assertEquals(Relation.EQ, list.get(0).getRelation());
        assertEquals("avalue1", list.get(0).getValue());

        assertEquals("aname2", list.get(1).getFieldName().getName());
        assertEquals(Relation.NE, list.get(1).getRelation());
        assertEquals("avalue2", list.get(1).getValue());
    }

    @Test(expected = UnsupportedQueryOperationException.class)
    @Deprecated
    public void testTooComplexToRowFilter() throws UnsupportedQueryOperationException {
        String expectedStr = "aname eq length('rubbish')";

        RowFilters filters = new RowFilters(expectedStr);

        filters.asRowFilters();
    }

    @Test
    @Deprecated
    public void testToEmptyRowFilter() {
        String expectedStr = "";

        RowFilters filters = new RowFilters(expectedStr);

        List<RowFilter> list = null;
        try {
            list = filters.asRowFilters();
        } catch (Exception e) {
            fail("Threw " + e);
        }

        assertEquals(0, list.size());
    }
}
