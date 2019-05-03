/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/
package com.temenos.interaction.rimdsl.visualisation.providers;

import com.temenos.interaction.rimdsl.rim.State;

public class TransitionDescription {

	private String title;
	private String event;
	private String conditions;
	private State fromState;
	private State toState;

	public String getTitle() {
		return title;
	}

	public String getEvent() {
		return event;
	}

	public String getConditions() {
		return conditions;
	}

	public State getFromState() {
		return fromState;
	}

	public State getToState() {
		return toState;
	}

	public static class Builder {
		private String title;
		private String event;
		private String conditions;
		private State fromState;
		private State toState;

		public Builder title(String title) {
			this.title = title;
			return this;
		}

		public Builder event(String event) {
			this.event = event;
			return this;
		}

		public Builder conditions(String conditions) {
			this.conditions = conditions;
			return this;
		}

		public Builder fromState(State fromState) {
			this.fromState = fromState;
			return this;
		}

		public Builder toState(State toState) {
			this.toState = toState;
			return this;
		}

		public TransitionDescription build() {
			return new TransitionDescription(this);
		}
	}

	private TransitionDescription(Builder builder) {
		this.title = builder.title;
		this.event = builder.event;
		this.conditions = builder.conditions;
		this.fromState = builder.fromState;
		this.toState = builder.toState;
	}
}
