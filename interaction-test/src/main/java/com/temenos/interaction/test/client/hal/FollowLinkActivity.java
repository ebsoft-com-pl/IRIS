package com.temenos.interaction.test.client.hal;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.HashMap;
import java.util.Map;

import com.temenos.interaction.test.client.Actions;
import com.temenos.interaction.test.client.Activity;
import com.theoryinpractise.halbuilder.api.Representation;

/**
 * This class implements an Activity that will follow a series of links 
 * to a goal for the HAL media type.
 * @author aphethean
 */
public class FollowLinkActivity extends Activity {

	private Map<LinkEvent, Actions> eventActions = new HashMap<LinkEvent, Actions>();
	
	public FollowLinkActivity() {};
	
	/**
	 * Add a new link event.  When a {@link LinkEvent}s is seen, the 
	 * associated {@link Actions} will be taken.
	 * @param event
	 * @param actions
	 */
	public void addLinkEvent(LinkEvent event, Actions actions) {
		eventActions.put(event, actions);
	}
	
	/**
	 * GET the supplied URI and process any {@link LinkEvent}s
	 */
	public Representation go(String startUri) {
		
		return null;
	}
}
