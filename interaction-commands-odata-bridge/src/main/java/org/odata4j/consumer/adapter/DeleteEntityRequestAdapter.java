package org.odata4j.consumer.adapter;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import org.odata4j.core.OEntityKey;
import org.odata4j.core.OEntityRequest;
import org.odata4j.exceptions.NotImplementedException;
import org.odata4j.producer.ODataProducer;

public class DeleteEntityRequestAdapter<T> extends
        AbstractOEntityRequestAdapter<T> {

  public DeleteEntityRequestAdapter(ODataProducer producer,
          String entitySetName, OEntityKey entityKey) {
    super(producer, entitySetName, entityKey);
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
    producer.deleteEntity(entitySetName, entityKey);
    return null;
  }

}
