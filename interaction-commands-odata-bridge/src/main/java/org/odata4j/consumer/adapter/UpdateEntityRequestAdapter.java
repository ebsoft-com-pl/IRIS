package org.odata4j.consumer.adapter;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import java.util.ArrayList;
import java.util.List;

import org.odata4j.core.OEntities;
import org.odata4j.core.OEntity;
import org.odata4j.core.OEntityKey;
import org.odata4j.core.OLink;
import org.odata4j.core.OLinks;
import org.odata4j.core.OModifyRequest;
import org.odata4j.core.OProperty;
import org.odata4j.producer.ODataProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.temenos.interaction.commands.odata.consumer.GETNavPropertyCommand;

public class UpdateEntityRequestAdapter<T> implements OModifyRequest<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(GETNavPropertyCommand.class);

    private final ODataProducer producer;
    private final String entitySetName;
    private final OEntityKey entityKey;

    private List<OProperty<?>> properties = new ArrayList<OProperty<?>>();
    private List<OLink> links = new ArrayList<OLink>();

    public UpdateEntityRequestAdapter(ODataProducer producer, String entitySetName, OEntityKey entityKey) {
        super();
        this.producer = producer;
        this.entitySetName = entitySetName;
        this.entityKey = entityKey;
    }

    @Override
    public OModifyRequest<T> properties(OProperty<?>... props) {
        for (OProperty<?> prop : props) {
            properties.add(prop);
        }
        return this;
    }

    @Override
    public OModifyRequest<T> properties(Iterable<OProperty<?>> props) {
        for (OProperty<?> prop : props) {
            properties.add(prop);
        }
        return this;
    }

    @Override
    public OModifyRequest<T> link(String navProperty, OEntity target) {
        OLink link = OLinks.relatedEntityInline(navProperty, null, null, target);
        links.add(link);
        return this;
    }

    @Override
    public OModifyRequest<T> link(String navProperty, OEntityKey targetKey) {
        OLink link = OLinks.relatedEntity(navProperty, navProperty, null);
        links.add(link);
        return this;
    }

    @Override
    public void execute() {
        OEntity entity = OEntities.create(producer.getMetadata().getEdmEntitySet(entitySetName), entityKey, properties,
                links);
        try {
            producer.updateEntity(entitySetName, entity);
        } catch (Exception e) {
            LOGGER.error("FAiled to update the entity", e);
        }
    }

    @Override
    public OModifyRequest<T> nav(String navProperty, OEntityKey key) {
        return this;
    }

    @Override
    public OModifyRequest<T> ifMatch(String precondition) {
        return null;
    }

}
