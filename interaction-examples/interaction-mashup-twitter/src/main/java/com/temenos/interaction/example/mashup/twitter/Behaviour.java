package com.temenos.interaction.example.mashup.twitter;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.temenos.interaction.core.hypermedia.Action;
import com.temenos.interaction.core.hypermedia.CollectionResourceState;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
import com.temenos.interaction.core.hypermedia.Transition;

public class Behaviour {

	// the entity that stores users
	private final static String USER_ENTITY_NAME = "User";

	public ResourceState getInteractionModel() {
		// this will be the service root
		ResourceState initialState = new ResourceState("home", "initial", createActionList(new Action("NoopGET", Action.TYPE.VIEW), null), "/");
		
		// work list
//		initialState.addTransition("GET", getProcessSM());
		
		// users and what they are doing on Twitter
		initialState.addTransition("GET", getUsersInteractionModel());
		return initialState;
	}

	private ResourceStateMachine getUsersInteractionModel() {
		CollectionResourceState allUsers = new CollectionResourceState(USER_ENTITY_NAME, "allUsers", createActionList(new Action("GETUsers", Action.TYPE.VIEW), null), "/users");
		ResourceState userProfile = new ResourceState(USER_ENTITY_NAME, "exists", createActionList(new Action("GETUser", Action.TYPE.VIEW), null), "/users/{userID}", "userID", "self".split(" "));
		
		// view user twitter activity
		CollectionResourceState tweets = new CollectionResourceState("Timeline", "activity", createActionList(new Action("GETUserTwitterUpdates", Action.TYPE.VIEW), null), "/tweets/{username}");
		ResourceState tweet = new ResourceState("Tweet", "posted", createActionList(new Action("NoopGET", Action.TYPE.VIEW), null), "/tweets/{username}/{tweet}", "wtf", "self".split(" "));

		// a linkage map (target URI element, source entity element)
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		
		/*
		 * a link on each user to their Twitter activity
		 */
		uriLinkageMap.put("username", "{twitterHandle}");
		allUsers.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(tweets).uriParameters(uriLinkageMap).build());

		/*
		 * Add link from the user item (same linkage map as from collection of users to tweets
		 */
		userProfile.addTransition(new Transition.Builder().method("GET").target(tweets).uriParameters(uriLinkageMap).build());
		// TODO fix this dodgy self link, need this to add subresource in HAL
		uriLinkageMap.clear();
		uriLinkageMap.put("username", "username");
		uriLinkageMap.put("tweet", "message");
		tweets.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(tweet).uriParameters(uriLinkageMap).build());
		
		/* 
		 * a link on each user in the collection to get view the user
		 * no linkage map as target URI element (self) must exist in source entity element (also self)
		 */
		uriLinkageMap.clear();
		allUsers.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(userProfile).uriParameters(uriLinkageMap).build());
		
		return new ResourceStateMachine(allUsers);
	}

	/*
	private ResourceStateMachine getProcessSM() {
		String PROCESS_ENTITY_NAME = "process";
		// process behaviour
		ResourceState processes = new ResourceState(PROCESS_ENTITY_NAME, "processes", "/processes");
		ResourceState newProcess = new ResourceState(PROCESS_ENTITY_NAME, "new", "/processes/new");
		// create new process
		processes.addTransition("POST", newProcess);

		// Process states
		ResourceState processInitial = new ResourceState(PROCESS_ENTITY_NAME, "initialProcess", "/processes/{id}");
		ResourceState nextTask = new ResourceState(PROCESS_ENTITY_NAME,	"taskAvailable", "/processes/nextTask");
		ResourceState processCompleted = new ResourceState(PROCESS_ENTITY_NAME, "completedProcess", "/processes/{id}");
		// start new process
		newProcess.addTransition("PUT", processInitial);
		// do a task
		processInitial.addTransition("GET", nextTask);
		// finish the process
		processInitial.addTransition("DELETE", processCompleted);

		// acquire task by a PUT to the initial state of the task state machine (acquired)
		ResourceStateMachine taskSM = getTaskSM();
		nextTask.addTransition("PUT", taskSM);

		return new ResourceStateMachine(processes);
	}
	
	private ResourceStateMachine getTaskSM() {
		String TASK_ENTITY_NAME = "task";
		// Task states
		ResourceState taskAcquired = new ResourceState(TASK_ENTITY_NAME, "acquired", "/acquired");
		ResourceState taskComplete = new ResourceState(TASK_ENTITY_NAME, "complete", "/completed");
		ResourceState taskAbandoned = new ResourceState(TASK_ENTITY_NAME, "abandoned", "/acquired");
		// abandon task
		taskAcquired.addTransition("DELETE", taskAbandoned);
		// complete task
		taskAcquired.addTransition("PUT", taskComplete);

		return new ResourceStateMachine(taskAcquired);
	}
*/

	private List<Action> createActionList(Action view, Action entry) {
		List<Action> actions = new ArrayList<Action>();
		if (view != null)
			actions.add(view);
		if (entry != null)
			actions.add(entry);
		return actions;
	}

}
