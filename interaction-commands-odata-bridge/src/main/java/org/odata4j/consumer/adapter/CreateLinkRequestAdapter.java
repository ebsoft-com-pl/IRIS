package org.odata4j.consumer.adapter;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import org.odata4j.core.OEntityId;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OEntityRequest;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.producer.ODataProducer;

public class CreateLinkRequestAdapter<T> implements OEntityRequest<T> {

  private ODataProducer producer;
  private OEntityId sourceEntity;
  private String navProperty;
  private OEntityId targetEntity;

  public CreateLinkRequestAdapter(ODataProducer producer,
          OEntityId sourceEntity, String navProperty, OEntityId targetEntity) {
    super();
    this.producer = producer;
    this.sourceEntity = sourceEntity;
    this.navProperty = navProperty;
    this.targetEntity = targetEntity;
  }

  @Override
  public OEntityRequest<T> nav(String navProperty, OEntityKey key) {
    throw new NotImplementedException("Not supported yet.");
  }

  @Override
  public OEntityRequest<T> nav(String navProperty) {
    throw new NotImplementedException("Not supported yet.");
  }

  @Override
  public T execute() {
    producer.createLink(sourceEntity, navProperty, targetEntity);
    return null;
  }

}
