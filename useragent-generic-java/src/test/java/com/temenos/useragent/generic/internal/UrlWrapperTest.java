package com.temenos.useragent.generic.internal;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;

import com.temenos.useragent.generic.http.HttpClient;
import com.temenos.useragent.generic.http.HttpHeader;
import com.temenos.useragent.generic.http.HttpMethod;
import com.temenos.useragent.generic.http.HttpMethodExecutor;
import com.temenos.useragent.generic.http.HttpRequest;
import com.temenos.useragent.generic.http.HttpResponse;

public class UrlWrapperTest {

	private HttpClient mockHttpClient;
	private SessionContext mockSessionContext;
	private HttpResponse mockResponse;
	private HttpHeader mockHeader;
	private HttpMethodExecutor mockExecutor;

	@Before
	public void setUp() {
		mockHttpClient = mock(HttpClient.class);
		mockSessionContext = mock(SessionContext.class);
		mockResponse = mock(HttpResponse.class);
		mockHeader = mock(HttpHeader.class);
		mockExecutor = mock(HttpMethodExecutor.class);
		when(mockSessionContext.getHttpClient()).thenReturn(mockHttpClient);
	}

	@Test
	public void testBaseuriWithGet() {
		when(mockHttpClient.get(anyString(), any(HttpRequest.class)))
				.thenReturn(mockResponse);
		when(mockResponse.headers()).thenReturn(mockHeader);
		when(mockHeader.get("Content-Type")).thenReturn("text/plain");
		when(mockResponse.payload()).thenReturn("plain text payload");
		UrlWrapper wrapper = new UrlWrapper(mockSessionContext);
		wrapper.baseuri("foo");
		wrapper.get();
		verify(mockHttpClient, times(1))
				.get(eq("foo/"), any(HttpRequest.class));
	}

	@Test
	public void testPathWithPost() {
		when(mockHttpClient.post(anyString(), any(HttpRequest.class)))
				.thenReturn(mockResponse);
		when(mockResponse.headers()).thenReturn(mockHeader);
		when(mockHeader.get("Content-Type")).thenReturn("text/plain");
		when(mockResponse.payload()).thenReturn("plain text payload");
		UrlWrapper wrapper = new UrlWrapper(mockSessionContext);
		wrapper.baseuri("foo");
		wrapper.path("bar()");
		wrapper.post();
		verify(mockHttpClient, times(1)).post(eq("foo/bar()"),
				any(HttpRequest.class));
	}

	@Test
	public void testQueryParamWithGet() {
		when(mockHttpClient.get(anyString(), any(HttpRequest.class)))
				.thenReturn(mockResponse);
		when(mockResponse.headers()).thenReturn(mockHeader);
		when(mockHeader.get("Content-Type")).thenReturn("text/plain");
		when(mockResponse.payload()).thenReturn("plain text payload");
		UrlWrapper wrapper = new UrlWrapper(mockSessionContext);
		wrapper.baseuri("foo");
		wrapper.path("bar()");
		wrapper.queryParam("$top=10");
		wrapper.get();
		verify(mockHttpClient, times(1)).get(eq("foo/bar()?$top=10"),
				any(HttpRequest.class));
	}

	@Test
	public void testNoPayloadWithPost() {
		when(mockHttpClient.post(anyString(), any(HttpRequest.class)))
				.thenReturn(mockResponse);
		when(mockResponse.headers()).thenReturn(mockHeader);
		when(mockHeader.get("Content-Type")).thenReturn("text/plain");
		when(mockResponse.payload()).thenReturn("plain text payload");
		UrlWrapper wrapper = new UrlWrapper("foo/bar('x')", mockSessionContext);
		wrapper.noPayload();
		wrapper.post();
		verify(mockHttpClient, times(1)).post(eq("foo/bar('x')"),
				any(HttpRequest.class));
	}

	@Test
	public void testGet() {
		when(mockHttpClient.get(anyString(), any(HttpRequest.class)))
				.thenReturn(mockResponse);
		when(mockResponse.headers()).thenReturn(mockHeader);
		when(mockHeader.get("Content-Type")).thenReturn("text/plain");
		when(mockResponse.payload()).thenReturn("plain text payload");
		UrlWrapper wrapper = new UrlWrapper("foo/bar()", mockSessionContext);
		wrapper.get();
		verify(mockHttpClient, times(1)).get(eq("foo/bar()"),
				any(HttpRequest.class));
	}

	@Test
	public void testPost() {
		when(mockHttpClient.post(anyString(), any(HttpRequest.class)))
				.thenReturn(mockResponse);
		when(mockResponse.headers()).thenReturn(mockHeader);
		when(mockHeader.get("Content-Type")).thenReturn("text/plain");
		when(mockResponse.payload()).thenReturn("plain text payload");
		UrlWrapper wrapper = new UrlWrapper("foo/bar('x')", mockSessionContext);
		wrapper.noPayload();
		wrapper.post();
		verify(mockHttpClient, times(1)).post(eq("foo/bar('x')"),
				any(HttpRequest.class));
	}

	@Test
	public void testPut() {
		when(mockHttpClient.put(anyString(), any(HttpRequest.class)))
				.thenReturn(mockResponse);
		when(mockResponse.headers()).thenReturn(mockHeader);
		when(mockHeader.get("Content-Type")).thenReturn("text/plain");
		when(mockResponse.payload()).thenReturn("plain text payload");
		UrlWrapper wrapper = new UrlWrapper("foo/bar('y')", mockSessionContext);
		wrapper.noPayload();
		wrapper.put();
		verify(mockHttpClient, times(1)).put(eq("foo/bar('y')"),
				any(HttpRequest.class));
	}
	
	@Test
    public void testDelete(){
        //given
        UrlWrapper wrapper = spy(new UrlWrapper("foo/bar('y')", mockSessionContext));
        doReturn(mockExecutor).when(wrapper).getExecutor(any(EntityWrapper.class));
        
        //when
        wrapper.delete();
        
        //then
        verify(mockExecutor).execute(eq(HttpMethod.DELETE));
        verify(mockSessionContext, times(1)).getRequestEntity();
    }

	@Test
	public void testUrl() {
		UrlWrapper wrapper = new UrlWrapper(mockSessionContext);
		wrapper.baseuri("foo");
		wrapper.path("bar('123')");
		assertEquals("foo/bar('123')", wrapper.url());

		wrapper = new UrlWrapper("foo/bar('x')", mockSessionContext);
		wrapper.baseuri("foo"); // ignores baseuri
		wrapper.path("bar('123')");// ignores path
		assertEquals("foo/bar('x')", wrapper.url());
	}
	
}
