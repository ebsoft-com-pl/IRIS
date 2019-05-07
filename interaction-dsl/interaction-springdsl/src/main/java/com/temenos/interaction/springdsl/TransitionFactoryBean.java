package com.temenos.interaction.springdsl;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import java.util.Map;

import org.springframework.beans.factory.FactoryBean;

import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.Transition;
import com.temenos.interaction.core.hypermedia.expression.Expression;

public class TransitionFactoryBean implements FactoryBean<Transition> {

	private ResourceState source, target;
	private String label;
	private String linkId;
	private String sourceField;

	// TransitionCommand parameters
	private String method;
	private int flags;
	// conditional link evaluation expression
	private Expression evaluation;
	private Map<String, String> uriParameters;

	@Override
	public Transition getObject() throws Exception {
		Transition.Builder builder = new Transition.Builder();
		builder.label(label);
		builder.source(source);
		builder.target(target);
		builder.method(method);
		builder.flags(flags);
		builder.evaluation(evaluation);
		builder.uriParameters(uriParameters);
		builder.linkId(linkId);
		builder.sourceField(sourceField);
		return builder.build();
	}

	@Override
	public Class<? extends Transition> getObjectType() {
		return Transition.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}

	public ResourceState getSource() {
		return source;
	}

	public void setSource(ResourceState source) {
		this.source = source;
	}

	public ResourceState getTarget() {
		return target;
	}

	public void setTarget(ResourceState target) {
		this.target = target;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getMethod() {
		return method;
	}

	public void setMethod(String method) {
		this.method = method;
	}

	public int getFlags() {
		return flags;
	}

	public void setFlags(int flags) {
		this.flags = flags;
	}

	public Expression getEvaluation() {
		return evaluation;
	}

	public void setEvaluation(Expression evaluation) {
		this.evaluation = evaluation;
	}

	public Map<String, String> getUriParameters() {
		return uriParameters;
	}

	public void setUriParameters(Map<String, String> uriParameters) {
		this.uriParameters = uriParameters;
	}
	
	public String getLinkId() {
		return linkId;
	}

	public void setLinkId(String linkId) {
		this.linkId = linkId;
	}
	
	public String getSourceField() {
        return sourceField;
    }

    public void setSourceField(String sourceField) {
        this.sourceField = sourceField;
    }
}
