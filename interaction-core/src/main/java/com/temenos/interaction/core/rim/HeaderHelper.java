package com.temenos.interaction.core.rim;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.*;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response.ResponseBuilder;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * This class contains a number of utility methods to manipulate
 * HTTP response headers via a system of ResponseBuilders.
 *
 * @author dgroves
 *
 */
public class HeaderHelper {
    
    private static final Logger logger = LoggerFactory.getLogger(HeaderHelper.class);
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * Add an HTTP Allow header to the response.
     * @param rb
     * @param httpMethods
     * @return
     */
    public static ResponseBuilder allowHeader(ResponseBuilder rb, Set<String> httpMethods) {
    	if (httpMethods != null) {
        	StringBuilder result = new StringBuilder();
        	for (String method : httpMethods) {
                result.append(" ");
                result.append(method);
                result.append(",");
        	}
        	return rb.header("Allow", (result.length() > 0 ? result.substring(1, result.length() - 1) : ""));
    	}
    	return rb;
    }
    
    /**
     * Add an HTTP Location header to the response without query parameters.
     * @param rb
     * @param target
     * @return
     */
    public static ResponseBuilder locationHeader(ResponseBuilder rb, String target) {
    	return locationHeader(rb, target, null);
    }
    
    /**
     * Add an HTTP Location header to the response with query parameters.
     * @param rb
     * @param target
     * @param queryParam
     * @return
     */
    public static ResponseBuilder locationHeader(ResponseBuilder rb, String target, 
            MultivaluedMap<String, String> queryParam) {
        Set<String> encodedQueryParameters = filterParameters(target, encodeQueryParameters(queryParam));
        if (target != null && !encodedQueryParameters.isEmpty() && target.contains("?")) {
            return rb.header(HttpHeaders.LOCATION, target + "&" + toString(encodedQueryParameters));
        }else if(target != null && !encodedQueryParameters.isEmpty()){
            return rb.header(HttpHeaders.LOCATION, target + "?" + toString(encodedQueryParameters));
        }else if(target != null){
            return rb.header(HttpHeaders.LOCATION, target);
        }else{
            return rb;
        }
    }

    /**
     * Add an E-Tag header
     * @param rb response builder
     * @param entityTag etag
     * @return response builder
     */
    public static ResponseBuilder etagHeader(ResponseBuilder rb, String entityTag) {
    	if (entityTag != null && !entityTag.isEmpty()) {
        	return rb.header(HttpHeaders.ETAG, entityTag);
    	}
    	return rb;
    }
    
    public static ResponseBuilder maxAgeHeader(ResponseBuilder rb, int maxAge) {
    	return rb.header(HttpHeaders.CACHE_CONTROL, "max-age=" + maxAge );
    }
    
    /**
     * Returns the first HTTP header entry for the specified header
     * @param headers HTTP headers
     * @param header header to return
     * @return first header entry
     */
    public static String getFirstHeader(HttpHeaders headers, String header) {
    	if (headers == null) {
    	    return null;
    	}
        List<String> headerList = headers.getRequestHeader(header);
        if(headerList != null && headerList.size() > 0) {
            return headerList.get(0);
        }
    	return null;
    }

    /**
     * Returns the first HTTP header entry for the specified header ignoring case,
     * that is, it does a case insensitive search.
     *
     * @param headers HTTP headers
     * @param header header to return
     * @return first header entry
     */
    public static String getFirstHeaderCaseInsensitive(HttpHeaders headers, String header) {
        if (headers == null) {
            return null;
        }
        MultivaluedMap<String, String> headerMap = headers.getRequestHeaders();
        if (headerMap == null) {
            return null;
        }
        for (String key : headerMap.keySet()) {
            if (key.equalsIgnoreCase(header)) {
                return headerMap.getFirst(key);
            }
        }
        return null;
    }

    /**
     * Encode a MultivaluedMap as URL query parameters, omitting any duplicate
     * key/value pairings.
     * @param requestParameters The query parameters to encode.
     * @return An encoded query string suitable for use with a URL.
     */
    public static String encodeMultivalueQueryParameters(
            MultivaluedMap<String, String> queryParam){
        if(isNullOrEmpty(queryParam)){
            return "";
        }
        return toString(encodeQueryParameters(queryParam));
    }

    private static Set<String> encodeQueryParameters(MultivaluedMap<String, String> queryParameters) {
        Set<String> encodedQueryParameters = new HashSet<>();
        if (queryParameters == null) {
            return encodedQueryParameters;
        }
        int outerIndex = 0;
        List<String> filter = new ArrayList<>();
        for(Map.Entry<String, List<String>> parameterKeyAndValues : queryParameters.entrySet()){
            filterDuplicateQueryKeyValuePairings(parameterKeyAndValues.getValue(), filter);
            String keyAndValue = constructQueryKeyValuePairing(parameterKeyAndValues.getKey(), filter, outerIndex < queryParameters.size() - 1);
            encodedQueryParameters.add(keyAndValue);
            filter.clear();
            outerIndex++;
        }
        return encodedQueryParameters;
    }

    private static Set<String> filterParameters(String target, Set<String> parameters) {
        Set<String> filteredParameters = new HashSet<>();
        for (String parameter : parameters) {
            if (!target.contains(parameter)) {
                filteredParameters.add(parameter);
            }
        }
        return filteredParameters;
    }

    private static boolean isNullOrEmpty(MultivaluedMap<String, String> queryParam){
        return queryParam == null || queryParam.size() == 0;
    }
    
    private static void filterDuplicateQueryKeyValuePairings(List<String> src, List<String> dest){
        for(String value : src){
            if(!dest.contains(value)){
                dest.add(value);
            }
        }
        if(dest.isEmpty()){
            dest.add(""); //interpret empty list as a key without a value (e.g. ?x=&y=)
        }
    }
    
    private static String constructQueryKeyValuePairing(String key, List<String> values, boolean appendAmpersand) {
        StringBuilder sb = new StringBuilder();
        int index = 0;
        for(String value : values){
            sb.append(encodeQueryParameter(key, DEFAULT_ENCODING));
            sb.append("=");
            sb.append(encodeQueryParameter(value, DEFAULT_ENCODING));
            if(index < values.size() - 1){
                sb.append("&");
            }
            index++;
        }
        if(appendAmpersand && sb.length() > 0){
            sb.append("&");
        }
        return sb.toString();
    }
    
    private static String encodeQueryParameter(String queryParam, String encoding){
        try{
            return URLEncoder.encode(queryParam, encoding);
        }catch(UnsupportedEncodingException uee){
            logger.error("Unsupported encoding type {} used to encode {}",
                    encoding, queryParam);
            throw new RuntimeException(uee);
        }
    }

    private static String toString(Set<String> set) {
        StringBuilder sb = new StringBuilder();
        for(String s : set){
            sb.append(s);
        }
        return sb.toString();
    }

}
