package com.temenos.interaction.odataext.odataparser.data;

import org.odata4j.expression.BinaryCommonExpression;
import org.odata4j.expression.CommonExpression;
import org.odata4j.producer.resources.OptionsQueryParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.odataext.odataparser.ODataParser;
import com.temenos.interaction.odataext.odataparser.ODataParser.UnsupportedQueryOperationException;

/*
 * Class containing information about a single row filters.
 * 
 * This code supports backwards compatibility with the old oDataParser which stored a list of individual 'and' terms. New
 * code should use the 'RowFilters' class which can handle more complex filter expression.
 */

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

public class RowFilter {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(RowFilter.class);

    // Wrapped OData4j object.
    private BinaryCommonExpression oData4jExpression;

    public RowFilter(FieldName name, Relation relation, String value) {
        this(name.getName(), relation, value);
    }

    // Constructor for callers that don't have a FieldName.
    public RowFilter(String name, Relation relation, String value) {
        // Only way to convert fields to an Expression is to print and then
        // parse it.
        String filterStr = name + " " + relation.getoDataString() + " " + value;
        CommonExpression expr = OptionsQueryParser.parseFilter(filterStr);

        if (!(expr instanceof BinaryCommonExpression)) {
            // Too complex to fit in a RowFIlter
            throw new RuntimeException("Expression too complex for row filter. Type=\"" + expr + "\"");
        }
        oData4jExpression = (BinaryCommonExpression) expr;
    }

    /*
     * Get wrapped oData4J object
     */
    public BinaryCommonExpression getOData4jExpression() {
        return oData4jExpression;
    }

    public FieldName getFieldName() {
        FieldName name = null;

        try {
            name = new FieldName(oData4jExpression.getLHS());
        } catch (UnsupportedQueryOperationException e) {
            LOGGER.error("LHS incompatible with FieldName.", e);
            throw new RuntimeException("LHS incompatible with FieldName.");
        }
        return name;
    }

    public Relation getRelation() {
        // Look for a matching relation
        for (Relation rel : Relation.values()) {
            if (rel.getOData4jClass().isInstance(oData4jExpression)) {
                return rel;
            }
        }
        return null;
    }

    public String getValue() {
        return ODataParser.OData4jToFilters(oData4jExpression.getRHS());
    }
}
