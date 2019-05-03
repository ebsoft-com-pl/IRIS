package com.temenos.useragent.generic.internal;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import com.temenos.useragent.generic.Url;
import com.temenos.useragent.generic.http.DefaultHttpExecutor;
import com.temenos.useragent.generic.http.HttpMethod;
import com.temenos.useragent.generic.http.HttpMethodExecutor;

/**
 * Implements a {@link Url url} wrapping the {@link SessionContext session
 * context} to be able to support invoking Http methods.
 * 
 * @author ssethupathi
 *
 */
public class UrlWrapper implements Url {

	private String url = "";
	private String baseuri = "";
	private String path = "";
	private String queryParam = "";
	private SessionContext sessionContext;
	private boolean noBody;

	public UrlWrapper(SessionContext sessionContext) {
		this.sessionContext = sessionContext;
	}

	public UrlWrapper(String url, SessionContext callback) {
		this.url = url;
		this.sessionContext = callback;
	}

	@Override
	public Url baseuri(String baseuri) {
		this.baseuri = baseuri;
		return this;
	}

	@Override
	public Url path(String path) {
		this.path = path;
		return this;
	}

	@Override
	public Url queryParam(String queryParam) {
		this.queryParam = queryParam;
		return this;
	}

	@Override
	public Url noPayload() {
		noBody = true;
		return this;
	}

	@Override
	public void get() {
		HttpMethodExecutor executor = getExecutor(null);
		ResponseData output = executor.execute(HttpMethod.GET);
		sessionContext.setResponse(output);
	}

	@Override
	public void post() {
		EntityWrapper entity = sessionContext.getRequestEntity();
		if (noBody) {
			entity = null; // TODO remove null
		}
		HttpMethodExecutor executor = getExecutor(entity);
		ResponseData output = executor.execute(HttpMethod.POST);
		sessionContext.setResponse(output);
	}

	@Override
	public void put() {
		EntityWrapper entity = sessionContext.getRequestEntity();
		if (noBody) {
			entity = null; // TODO remove null
		}
		HttpMethodExecutor executor = getExecutor(entity);
		ResponseData output = executor.execute(HttpMethod.PUT);
		sessionContext.setResponse(output);
	}
	
	@Override
	public void delete() {
	    EntityWrapper entity = sessionContext.getRequestEntity();
        if (noBody) {
            entity = null; // TODO remove null
        }
	    HttpMethodExecutor executor = getExecutor(entity);
	    ResponseData output = executor.execute(HttpMethod.DELETE);
	    sessionContext.setResponse(output);
	}

	@Override
	public String url() {
		return completeUrlWithQueryParam(url.isEmpty() ? baseuri + "/" + path
				: url);
	}

	private String completeUrlWithQueryParam(String url) {
		if (queryParam.isEmpty()) {
			return url;
		}
		return url + "?" + queryParam;
	}
	
	protected HttpMethodExecutor getExecutor(EntityWrapper entity){
	    return new DefaultHttpExecutor(
            sessionContext.getHttpClient(), url(), new RequestDataImpl(
                    sessionContext.getRequestHeader(), entity
            )
        );
	}
}
