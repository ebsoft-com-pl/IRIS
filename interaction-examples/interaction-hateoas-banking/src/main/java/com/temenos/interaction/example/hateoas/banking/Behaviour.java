package com.temenos.interaction.example.hateoas.banking;

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

	public ResourceState getInteractionModel() {

		// this will be the service root
		ResourceState initialState = new ResourceState("home", "initial", createActionList(new Action("NoopGET", Action.TYPE.VIEW), null), "/");
		ResourceState preferences = new ResourceState("Preferences", "preferences", createActionList(new Action("GETPreferences", Action.TYPE.VIEW), null), "/preferences");
		
		initialState.addTransition(new Transition.Builder().method("GET").target(preferences).build());
		ResourceStateMachine fundsTransferModel = getFundsTransferInteractionModel();
		initialState.addTransition("GET", fundsTransferModel);
		ResourceStateMachine customerModel = getCustomerInteractionModel();
		initialState.addTransition("GET", customerModel);

		// create a 'ServiceDocument' and add our entity sets to make OData4j metadata available
		ResourceState serviceDocumentState = new ResourceState("home", "ServiceDocument", createActionList(new Action("NoopGET", Action.TYPE.VIEW), null), "/Banking.svc");
		serviceDocumentState.addTransition("GET", fundsTransferModel);
		serviceDocumentState.addTransition("GET", customerModel);
		initialState.addTransition(new Transition.Builder().method("GET").target(serviceDocumentState).build());
		return initialState;
	}

	public ResourceStateMachine getFundsTransferInteractionModel() {
		CollectionResourceState fundstransfers = new CollectionResourceState("FundsTransfer", "fundstransfers", createActionList(new Action("GETFundTransfers", Action.TYPE.VIEW), null), "/fundtransfers");
		ResourceState newFtState = new ResourceState(fundstransfers, "new", createActionList(new Action("NoopGET", Action.TYPE.VIEW), new Action("NEWFundTransfer", Action.TYPE.ENTRY)), "/new");
		ResourceState fundstransfer = new ResourceState("FundsTransfer", "fundstransfer", createActionList(new Action("GETFundTransfer", Action.TYPE.VIEW), new Action("PUTFundTransfer", Action.TYPE.ENTRY)), "/fundtransfers/{id}", "id", "self".split(" "));
		ResourceState finalState = new ResourceState(fundstransfer, "end", createActionList(new Action("NoopGET", Action.TYPE.VIEW), null));

		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		fundstransfers.addTransition(new Transition.Builder().method("POST").target(newFtState).build());		

		uriLinkageMap.clear();
		newFtState.addTransition(new Transition.Builder().method("PUT").target(fundstransfer).uriParameters(uriLinkageMap).build());
		//newFtState.addTransition("GET", exists, uriLinkageMap);
		
		uriLinkageMap.clear();
		fundstransfers.addTransition(new Transition.Builder()
				.method("GET").target(fundstransfer)
				.uriParameters(uriLinkageMap)
				.flags(Transition.FOR_EACH)
				.build());		

		fundstransfer.addTransition(new Transition.Builder().method("PUT").target(fundstransfer).uriParameters(uriLinkageMap).build());		
		fundstransfer.addTransition(new Transition.Builder().method("DELETE").target(finalState).uriParameters(uriLinkageMap).build());
		return new ResourceStateMachine(fundstransfers);
	}

	public ResourceStateMachine getCustomerInteractionModel() {
		CollectionResourceState customers = new CollectionResourceState("Customer", "customers", createActionList(new Action("GETCustomers", Action.TYPE.VIEW), null), "/customers");
		ResourceState customer = new ResourceState("Customer", "customer", createActionList(new Action("GETCustomer", Action.TYPE.VIEW), new Action("PUTCustomer", Action.TYPE.ENTRY)), "/customers/{id}");
		ResourceState deleted = new ResourceState(customer, "deleted", createActionList(null, new Action("NoopDELETE", Action.TYPE.ENTRY)));
		
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.clear();
		uriLinkageMap.put("id", "name");
		customers.addTransition(new Transition.Builder()
				.method("GET")
				.target(customer)
				.uriParameters(uriLinkageMap)
				.build());		

		customer.addTransition(new Transition.Builder().method("PUT").target(customer).uriParameters(uriLinkageMap).build());		
		customer.addTransition(new Transition.Builder().method("DELETE").target(deleted).uriParameters(uriLinkageMap).build());
		return new ResourceStateMachine(customers);
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
