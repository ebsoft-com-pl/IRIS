package com.temenos.interaction.test;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import javax.ws.rs.core.MediaType;

import org.odata4j.consumer.ODataConsumer;
import org.odata4j.format.FormatType;

public interface RuntimeFacade {

  public ODataConsumer create(String endpointUri, FormatType format, String methodToTunnel);

  public String getWebResource(String uri);

  public String acceptAndReturn(String uri, MediaType mediaType);

  public void accept(String uri, MediaType mediaType);

  public String getWebResource(String uri, String accept);

}
