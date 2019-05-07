package com.temenos.interaction.example.odata.notes;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.temenos.interaction.commands.odata.ODataUriSpecification;
import com.temenos.interaction.core.hypermedia.Action;
import com.temenos.interaction.core.hypermedia.CollectionResourceState;
import com.temenos.interaction.core.hypermedia.ResourceState;
import com.temenos.interaction.core.hypermedia.ResourceStateMachine;
import com.temenos.interaction.core.hypermedia.Transition;

public class Behaviour {

	// entities
	private final static String NOTE = "Note";
	private final static String PERSON = "Person";
	
	private final static String PERSONS_PATH = "/Persons";
	private final static String PERSON_ITEM_PATH = "/Persons({id})";
	private final static String NOTES_PATH = "/Notes";
	private final static String NOTE_PERSON_PATH = "/Notes({id})/NotePerson";
	private final static String PERSON_NOTES_PATH = "/Persons({id})/PersonNotes";
	
	public ResourceState getSimpleODataInteractionModel() {
		// the service root
		ResourceState initialState = new ResourceState("ServiceDocument", "ServiceDocument", createActionList(new Action("GETServiceDocument", Action.TYPE.VIEW), null), "/");

		// notes service
		ResourceStateMachine notes = getNotesSM();
		initialState.addTransition("GET", notes);
		// persons service
		ResourceStateMachine persons = getPersonsSM();
		initialState.addTransition("GET", persons);
		
		// now link the two entity sets
		addTransitionsBetweenRSMs(new ResourceStateMachine(initialState));
		
		return initialState;
	}

	public void addTransitionsBetweenRSMs(ResourceStateMachine root) {
		Map<String, String> uriLinkageMap = new HashMap<String, String>();

		// links to entities of the same type, therefore same id linkage
		uriLinkageMap.clear();
		// link NotePerson to PersonNotes
		root.getResourceStateByName("NotePerson").addTransition(new Transition.Builder().method("GET").target(root.getResourceStateByName("PersonNotes")).uriParameters(uriLinkageMap).build());
		// link PersonNotes NotePerson
		((CollectionResourceState) root.getResourceStateByName("PersonNotes")).addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(root.getResourceStateByName("NotePerson")).uriParameters(uriLinkageMap).build());
		// link back to person
		root.getResourceStateByName("NotePerson").addTransition(new Transition.Builder().method("GET").target(root.getResourceStateByName("person")).uriParameters(uriLinkageMap).build());
		// link back to note
		((CollectionResourceState) root.getResourceStateByName("PersonNotes")).addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(root.getResourceStateByName("note")).uriParameters(uriLinkageMap).build());

		// Links from a note to a person
		uriLinkageMap.clear();
		uriLinkageMap.put("id", "{personId}");
		// link from each person's notes back to person
		((CollectionResourceState) root.getResourceStateByName("PersonNotes")).addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(root.getResourceStateByName("person")).uriParameters(uriLinkageMap).build());
		// link from each note to their person
		((CollectionResourceState) root.getResourceStateByName("Notes")).addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(root.getResourceStateByName("person")).uriParameters(uriLinkageMap).build());

		uriLinkageMap.clear();
	
	}

	public ResourceState getNotes() {
		CollectionResourceState notes = new CollectionResourceState(NOTE, "Notes", createActionList(new Action("GETEntities", Action.TYPE.VIEW), null), NOTES_PATH);
		ResourceState pseudoCreated = new ResourceState(notes, "Note.PseudoCreated", createActionList(null, new Action("CreateEntity", Action.TYPE.ENTRY)));
		// Option 1 for configuring the interaction - use another state as a parent
		ResourceState note = new ResourceState(notes, 
				"note", 
				createActionList(new Action("GETEntity", Action.TYPE.VIEW), null), 
				"({id})");
		ResourceState noteUpdated = new ResourceState(note, 
				"updated", 
				createActionList(null, new Action("UpdateEntity", Action.TYPE.ENTRY)),
				null,
				"edit".split(" ")
				);
		ResourceState noteDeleted = new ResourceState(note, 
				"deleted", 
				createActionList(null, new Action("DeleteEntity", Action.TYPE.ENTRY)),
				null,
				"edit".split(" ")
				);
		/* 
		 * this navigation property demonstrates an Action properties and 
		 * uri specification to get conceptual configuration into a Command
		 */
		Properties personNotesNavProperties = new Properties();
		personNotesNavProperties.put("entity", NOTE);
		personNotesNavProperties.put("navproperty", "NotePerson");

		/*
		 * The link relation for a NavProperty must match the NavProperty name to keep ODataExplorer happy
		 */
		ResourceState notePerson = new ResourceState(PERSON, 
				"NotePerson", 
				createActionList(new Action("GETNavProperty", Action.TYPE.VIEW, personNotesNavProperties), null), 
				NOTE_PERSON_PATH, 
				new ODataUriSpecification().getTemplate(NOTES_PATH, ODataUriSpecification.NAVPROPERTY_URI_TYPE));
		
		// use to add collection transition to individual items
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.put("id", "{id}");
		// edit
		notes.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("PUT").target(noteUpdated).uriParameters(uriLinkageMap).build());
		notes.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(note).uriParameters(uriLinkageMap).build());
		notes.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(notePerson).uriParameters(uriLinkageMap).build());
		notes.addTransition(new Transition.Builder().method("POST").target(pseudoCreated).build());
		// auto transition to new note that was just created
		pseudoCreated.addTransition(new Transition.Builder().flags(Transition.AUTO).target(note).build());
		note.addTransition(new Transition.Builder().method("GET").target(notePerson).uriParameters(uriLinkageMap).build());
		note.addTransition(new Transition.Builder().method("PUT").target(noteUpdated).build());
		note.addTransition(new Transition.Builder().method("DELETE").target(noteDeleted).build());

		return notes;
	}
	
	public ResourceStateMachine getNotesSM() {
		return new ResourceStateMachine(getNotes());
	}

	public ResourceState getPersons() {
		CollectionResourceState persons = new CollectionResourceState(PERSON, "Persons", createActionList(new Action("GETEntities", Action.TYPE.VIEW), null), PERSONS_PATH);
		ResourceState pseudo = new ResourceState(persons, "Person.PseudoCreated", createActionList(null, new Action("CreateEntity", Action.TYPE.ENTRY)));
		// Option 2 for configuring the interaction - specify the entity, state, and fully qualified path
		ResourceState person = new ResourceState(PERSON, 
				"person", 
				createActionList(new Action("GETEntity", Action.TYPE.VIEW), null), 
				PERSON_ITEM_PATH);
		/* 
		 * this navigation property demostrates an Action properties and 
		 * uri specification to get conceptual configuration into a Command
		 */
		Properties personNotesNavProperties = new Properties();
		personNotesNavProperties.put("entity", PERSON);
		personNotesNavProperties.put("navproperty", "PersonNotes");
				
		/*
		 * The link relation for a NavProperty must match the NavProperty name to keep ODataExplorer happy
		 */
		CollectionResourceState personNotes = new CollectionResourceState(NOTE, 
				"PersonNotes", 
				createActionList(new Action("GETNavProperty", Action.TYPE.VIEW, personNotesNavProperties), null), 
				PERSON_NOTES_PATH, 
				new ODataUriSpecification().getTemplate(PERSONS_PATH, ODataUriSpecification.NAVPROPERTY_URI_TYPE));
		
		// add collection transition to individual items
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.put("id", "{id}");
		persons.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(person).uriParameters(uriLinkageMap).build());
		persons.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(personNotes).uriParameters(uriLinkageMap).build());
		persons.addTransition(new Transition.Builder().method("POST").target(pseudo).build());
		// add auto transition to new person that was just created
		pseudo.addTransition(new Transition.Builder().flags(Transition.AUTO).target(person).build());
		person.addTransition(new Transition.Builder().method("GET").target(personNotes).uriParameters(uriLinkageMap).build());
	
		return persons;
	}
	
	public ResourceStateMachine getPersonsSM() {
		return new ResourceStateMachine(getPersons());
	}

	private List<Action> createActionList(Action view, Action entry) {
		List<Action> actions = new ArrayList<Action>();
		if (view != null)
			actions.add(view);
		if (entry != null)
			actions.add(entry);
		return actions;
	}

}
