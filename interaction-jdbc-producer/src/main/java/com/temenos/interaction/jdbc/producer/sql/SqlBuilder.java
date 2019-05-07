package com.temenos.interaction.jdbc.producer.sql;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.jdbc.JDBCProducerConstants;
import com.temenos.interaction.jdbc.ServerMode;
import com.temenos.interaction.jdbc.SqlRelation;
import com.temenos.interaction.odataext.odataparser.data.AccessProfile;
import com.temenos.interaction.odataext.odataparser.data.FieldName;
import com.temenos.interaction.odataext.odataparser.data.OrderBy;
import com.temenos.interaction.odataext.odataparser.data.RowFilters;

/**
 * Interface for writing multiple implementation of sql bilder
 *
 * @author sjunejo
 *
 */
public abstract class SqlBuilder {

    public static final int MAX_ROWS_DEFAULT = 99;
    public static final int SKIP_ROWS_DEFAULT = 0;

    // Somewhere to store arguments
    protected String tableName;
    protected String keyValue;
    protected AccessProfile accessProfile;
    protected ColumnTypesMap colTypesMap;
    protected String top;
    protected String skip;
    protected List<OrderBy> orderBy;

    // Server compatibility mode.
    protected ServerMode serverMode;

    // Flag indicating that the server is really H2. i.e. an emulated server for
    // testing.
    protected boolean serverIsEmulated;

    // Name of rownum exported form inner select.
    protected static final String INNER_RN_NAME = "rn";

    // Inner table name used when ordering rows.
    protected static final String INNER_TABLE_NAME = "inner_tab";

    protected static final Logger LOGGER = LoggerFactory.getLogger(SqlBuilder.class);

    /**
     * @param tableName
     * @param keyValue
     * @param accessProfile
     * @param colTypesMap
     * @param top
     * @param skip
     * @param orderBy
     */
    public SqlBuilder(String tableName, String keyValue, AccessProfile accessProfile, ColumnTypesMap colTypesMap,
            String top, String skip, List<OrderBy> orderBy) {
        this.tableName = tableName;
        this.keyValue = keyValue;
        this.accessProfile = accessProfile;
        this.colTypesMap = colTypesMap;
        this.top = top;
        this.skip = skip;
        this.orderBy = orderBy;

        // Check if reserved row number column name is present.
        boolean exists = true;
        try {
            colTypesMap.getType(INNER_RN_NAME);
        } catch (SecurityException e) {
            // Not found. This is what we want.
            exists = false;
            LOGGER.debug("Column name " + INNER_RN_NAME + " does not exist",e);
        }
        if (exists) {
            // Possibly should throw or maybe dynamically work out an unique
            // column name. For now just warn the user.
            LOGGER.warn("Table contains a column with the reserved name \"" + INNER_RN_NAME
                    + "\" pagination may not perform as expected");
        }

        setCompatibilityMode();
    }

    /*
     * Utility to check if a string is representable as a Jdbc numeric.
     */
    protected boolean isJdbcNumeric(String value) {
        try {
            // Java "BigDecimal" appears to be the closest data type to Jdbc
            // "numeric".
            BigDecimal x = new BigDecimal(value);
            return x != null ? true : false;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Add Selected column names to query
     * 
     * @param builder
     */
    protected void addSelects(StringBuilder builder) {

        // Add columns to select
        Set<FieldName> names = accessProfile.getFieldNames();
        if (null == names) {
            throw new SecurityException("Cannot generate Sql command for null field set.");
        }
        if (names.isEmpty()) {
            // Empty select list means "return all columns".
            builder.append(" *");
        } else {
            // Add comma separated list of select terms. Need to detect the last
            // operation so use old style iterator.
            Iterator<FieldName> iterator = names.iterator();
            while (iterator.hasNext()) {
                FieldName name = iterator.next();
                addSelect(builder, name);

                // If not the last entry
                if (iterator.hasNext()) {
                    builder.append(",");
                }
            }
        }
    }

    private void addSelect(StringBuilder builder, FieldName name) {
        String fieldName = name.getName();
        // Check if we have to append its alias.
        //
        // Note: This is an extension of the odata standard. If/when the column
        // aliasing is standardized this should be
        // changed to match the official syntax.
        int aliasSepInd = fieldName.indexOf(JDBCProducerConstants.SELECT_FIELD_NAME_ALIAS_SEP);
        if (aliasSepInd > 0) {
            if (aliasSepInd + JDBCProducerConstants.SELECT_FIELD_NAME_ALIAS_SEP_LEN == fieldName.length()) {
                // Alias provided seems to be empty :(, we should log at-least
                LOGGER.info("FieldName recieved with empty alias, this should be corrected while constructing select list...");
                // Append the name before :AS: and ignore the rest as its empty
                // anyway
                builder.append(" \"" + fieldName.substring(0, aliasSepInd) + "\"");
            } else {
                // Append the name before :AS:
                builder.append(" \"" + fieldName.substring(0, aliasSepInd) + "\" AS");
                // Append alias after :AS:
                builder.append(" \""
                        + fieldName.substring(aliasSepInd + JDBCProducerConstants.SELECT_FIELD_NAME_ALIAS_SEP_LEN)
                        + "\"");
            }
        } else {
            // Append the name as is
            builder.append(" \"" + name.getName() + "\"");
        }
    }

    /**
     * Append Table/View entity name to the query
     * 
     * @param builder
     */
    protected void addFromTerm(StringBuilder builder) {
        addFrom(builder);
        addTableName(builder);
    }

    private void addFrom(StringBuilder builder) {
        builder.append(" FROM");
    }

    private void addTableName(StringBuilder builder) {
        builder.append(" \"" + tableName + "\"");
    }

    /*
     * add the "WHERE x AND y" etc clause. Adds filters and/or key.
     */
    protected void addWhereTerms(StringBuilder builder) {

        // If there are no filters or key return
        if (accessProfile.getNewRowFilters().isEmpty() && (null == keyValue)) {
            return;
        }

        addWhere(builder);

        if (null != keyValue) {
            if (null == colTypesMap.getPrimaryKeyName()) {
                throw new SecurityException("No primary key column defined for \"" + tableName
                        + "\". Cannot look up key.");
            }

            // Add key as a filter
            accessProfile.getNewRowFilters().addFilters(
                    colTypesMap.getPrimaryKeyName() + " " + SqlRelation.EQ.getoDataString() + " '" + keyValue + "'");
        }

        if (!accessProfile.getNewRowFilters().isEmpty()) {
            addFilters(builder);
        }
    }

    private void addWhere(StringBuilder builder) {
        builder.append(" WHERE");
    }

    private void addAnd(StringBuilder builder) {
        builder.append(" AND");
    }

    private void addFilters(StringBuilder builder) {

        // Add row filters
        RowFilters filters = accessProfile.getNewRowFilters();
        if ((null == filters) || (filters.isBlockAll())) {
            throw new SecurityException("Cannot generate Sql command for 'block all' row filter.");
        }

        // Create an OData4j visitor and use it to print out the filters. 
        SQLExpressionVisitor v = new SQLExpressionVisitor();
        filters.getOData4jExpression().visit(v);
        String parameters = v.toString();
        if (!parameters.isEmpty()) {
            builder.append(" ");
            builder.append(v.toString());
        }
    }
    
    /*
     * Add order by term. If this is not present rows will be returned in a
     * random order.
     */
    protected void addOrderByTerms(StringBuilder builder) {
        if (null != orderBy) {
            addOrderBy(builder);
            boolean first = true;
            for (OrderBy order : orderBy) {
                if (!first) {
                    builder.append(",");
                } else {
                    first = false;
                }
                addOrderByTerm(builder, order.getFieldName().getName(), order.isAcsending());
            }
        } else {
            // By default order by the primary key.
            if (null == colTypesMap.getPrimaryKeyName()) {
                LOGGER.warn("Primary key name not known. Cannot add \"ORDER BY\" clause.");
                return;
            }
            addOrderBy(builder);
            addOrderByTerm(builder, colTypesMap.getPrimaryKeyName(), true);
        }
    }

    private void addOrderBy(StringBuilder builder) {
        builder.append(" ORDER BY");
    }

    private void addOrderByTerm(StringBuilder builder, String columnName, boolean ascending) {
        builder.append(" \"" + columnName + "\"");
        if (!ascending) {
            builder.append(" DESC");
        }
    }

    /*
     * Add $top and $skip components for this server type.
     * 
     * This is messy. To support pagination an inner select is wrapped by an
     * outer select. For more information search online for "oracle pagination".
     */
    protected void addTopAndSkip(StringBuilder builder) {
        if ((null == top) && (null == skip)) {
            // Nothing to do
            return;
        }

        // Builder for starting part of the string.
        StringBuilder startBuilder = new StringBuilder();

        // Add start of outer command
        startBuilder.append("SELECT * FROM ( SELECT " + INNER_TABLE_NAME + ".*,");

        // If we are doing top or skip need the row number column
        addRowNumSelect(startBuilder);

        // Also select everything form the inner select
        startBuilder.append(" FROM ( ");

        // Add starting part.
        builder.insert(0, startBuilder);

        // Add inner command end bracket
        builder.append(" ) " + INNER_TABLE_NAME + " )");

        // Add where clauses
        addSkip(builder);
        addTop(builder);
    }

    /*
     * To select row number ranges ($top and $skip) in outer select we need the
     * INNER_RN_NAME alias in the inner select.
     */
    private void addRowNumSelect(StringBuilder builder) {
        if ((null != top) || (null != skip)) {
            switch (serverMode) {
            case ORACLE:
            default:
                builder.append(" ROWNUM \"" + INNER_RN_NAME + "\"");
                break;
            }
        }
    }

    private void addTop(StringBuilder builder) {
        if (null != top) {
            // Work out max row
            int maxRow = Integer.parseInt(top);
            if (null != skip) {
                maxRow += Integer.parseInt(skip);
            }

            if (null != skip) {
                // If we already have a skip add "AND" to existing "WHERE"
                addAnd(builder);
            } else {
                addWhere(builder);
            }
            builder.append(" \"" + INNER_RN_NAME + "\" <= " + maxRow);
        }
    }

    private void addSkip(StringBuilder builder) {
        if (null != skip) {
            addWhere(builder);
            builder.append(" \"" + INNER_RN_NAME + "\" > " + skip);
        }
    }

    /*
     * Utility to obtain the reserved 'row number' column name. Used mainly in
     * testing but could be useful to the end user.
     */
    public static String getRnName() {
        return INNER_RN_NAME;
    }

    /**
     * Returns the SQL Statement as String
     * 
     * @return
     */
    public abstract String getCommand();

    /**
     * Sets the compatibility mode
     */
    public abstract void setCompatibilityMode();
}
