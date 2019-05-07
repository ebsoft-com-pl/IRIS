package com.temenos.interaction.authorization.mock;

/*
 * Mock authorization bean. Used in testing. Instead of extracting real RowFilters for the current principle returns
 * pre-configured data passed in at construction and/or call time.
 */

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.authorization.IAuthorizationProvider;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.command.InteractionException;
import com.temenos.interaction.odataext.odataparser.ODataParser;
import com.temenos.interaction.odataext.odataparser.data.AccessProfile;
import com.temenos.interaction.odataext.odataparser.data.FieldName;
import com.temenos.interaction.odataext.odataparser.data.RowFilters;

/**
 * Once we have actual functionality available. PLEASE DELETE THIS CLASS
 * 
 * @author sjunejo
 * 
 */

public class MockAuthorizationBean implements IAuthorizationProvider {
	private final static Logger LOGGER = LoggerFactory.getLogger(MockAuthorizationBean.class);

	// Keys for test arguments that can be passed in as parameters.
	static final String TEST_FILTER_KEY = "$testAuthorizationKey";
	static final String TEST_SELECT_KEY = "$testSelectKey";

	// Somewhere to store dummy test arguments.
	private RowFilters testFilter = null;
	private Set<FieldName> testSelect = null;

	// Place to store an exception to be thrown on execute
	private InteractionException exception = null;

	// A mock bean with no arguments is meaningless.
	// public MockAuthorizationBean() {
	// }

	// Constructor that will throw on execute
	public MockAuthorizationBean(InteractionException exception) {
		this.exception = exception;
	}

	// Constructors enabling dummy RowFilters to be passed in for testing.
	// BREAKS AUTHORIZATION. DO NOT USE OTHER THAN FOR TESTING.
	public MockAuthorizationBean(String filter, String select) {
		CommonTestConstructor(filter, stringToSelect(select));
	}

	private void CommonTestConstructor(String testFilter, Set<FieldName> testSelect) {
		// Store test arguments
	    try {
	        this.testFilter = new RowFilters(testFilter);
	    }
	    catch (Exception e) {
	        // If there was any problem create the 'no result' filter.
	        this.testFilter = null;
	        LOGGER.debug("There was a problem creating a filter", e);	        
	    }
	    
		this.testSelect = testSelect;
	}
	
	// Helper to convert a select string into a select. Handles parsing and also
	// the edge 'all' and 'none' cases.
	private Set<FieldName> stringToSelect(String select) {
		if (null == select) {
			// An null string means do no filtering. Represented by a null empty
			// select list.
			return (null);
		}

		if (select.isEmpty()) {
			// Ae empty string means return nothing. Represented by an empty
			// select list.
			return (new HashSet<FieldName>());
		}

		// If we get here parse the select.
		try {
			Set<FieldName> selectList = ODataParser.parseSelect(select);
			return (selectList);
		} catch (Exception e) {
			LOGGER.info("Could not parse test seelct \"" + select + "\" : ", e);

			// Return the 'no output' case
			return (new HashSet<FieldName>());
		}
	}

	/*
	 * Get the select (column filter) for the current principle.
	 */
	@Override
	public Set<FieldName> getSelect(InteractionContext ctx) throws InteractionException {
		if (null != exception) {
			throw (exception);
		}

		MultivaluedMap<String, String> queryParams = ctx.getQueryParameters();

		// If a select was passed on the command line use that.
		if (null != queryParams) {
			String passedSelect = queryParams.getFirst(TEST_SELECT_KEY);
			if (null != passedSelect) {
				return (stringToSelect(passedSelect));
			}
		}
		return (testSelect);
	}

	@Override
	public AccessProfile getAccessProfile(InteractionContext ctx) throws InteractionException {
		if (null != exception) {
			throw (exception);
		}

		AccessProfile profile = new AccessProfile(getFilters(ctx), getSelect(ctx));
		return (profile);
	}
    
    /*
     * Get the filters for the current principle
     */
    @Override
    public RowFilters getFilters(InteractionContext ctx) throws InteractionException {
        if (null != exception) {
            throw (exception);
        }

        MultivaluedMap<String, String> queryParams = ctx.getQueryParameters();

        // If a filter was passed on the command line use that
        if (null != queryParams) {
            String passedFilter = queryParams.getFirst(TEST_FILTER_KEY);

            if (null != passedFilter) {
                return (ODataParser.parseFilters(passedFilter));
            }
        }

        return (testFilter);
    }
}
