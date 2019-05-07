package com.interaction.example.odata.northwind;

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

	public ResourceState getSimpleODataInteractionModel() {
		// the service root
		ResourceState initialState = new ResourceState("ServiceDocument", "ServiceDocument", createActionList(new Action("GETServiceDocument", Action.TYPE.VIEW), null), "/");
		ResourceState metadata = new ResourceState("Metadata", "metadata", createActionList(new Action("GETMetadata", Action.TYPE.VIEW), null), "/$metadata");

		ResourceStateMachine categories = getCategoriesSM();
		ResourceStateMachine customers = getCustomersSM();
		ResourceStateMachine employees = getEmployeesSM();
		ResourceStateMachine orders = getOrdersSM();
		ResourceStateMachine orderDetails = getOrderDetailsSM();
		ResourceStateMachine products = getProductsSM();
		ResourceStateMachine suppliers = getSuppliersSM();

		// Add transitions from ServiceDocument to RSMs
		initialState.addTransition(new Transition.Builder().method("GET").target(metadata).build());
		initialState.addTransition("GET", categories);
		initialState.addTransition("GET", customers);
		initialState.addTransition("GET", employees);
		initialState.addTransition("GET", orders);
		initialState.addTransition("GET", orderDetails);
		initialState.addTransition("GET", products);
		initialState.addTransition("GET", suppliers);
	
		// Add transitions between two RSMs
		addTransitionsBetweenRSMs(new ResourceStateMachine(initialState));

		return initialState;
	}

	public void addTransitionsBetweenRSMs(ResourceStateMachine root) {
	
	}

	public ResourceStateMachine getCategoriesSM() {
		CollectionResourceState categories = new CollectionResourceState("Categories", "Categories", createActionList(new Action("GETEntities", Action.TYPE.VIEW), null), "/Categories");
		ResourceState pseudo = new ResourceState(categories, "Categories_pseudo_created", createActionList(null, new Action("CreateEntity", Action.TYPE.ENTRY)));
		ResourceState category = new ResourceState("Categories", "category", createActionList(new Action("GETEntity", Action.TYPE.VIEW), null), "/Categories({id})");

		//Add state transitions
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.put("id", "{CategoryID}");
		categories.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(category).uriParameters(uriLinkageMap).build());
		categories.addTransition(new Transition.Builder().method("POST").target(pseudo).build());

		return new ResourceStateMachine(categories);
	}
	
	public ResourceStateMachine getCustomersSM() {
		CollectionResourceState customers = new CollectionResourceState("Customers", "Customers", createActionList(new Action("GETEntities", Action.TYPE.VIEW), null), "/Customers");
		ResourceState pseudo = new ResourceState(customers, "Customers_pseudo_created", createActionList(null, new Action("CreateEntity", Action.TYPE.ENTRY)));
		ResourceState category = new ResourceState("Customers", "category", createActionList(new Action("GETEntity", Action.TYPE.VIEW), null), "/Customers({id})");

		//Add state transitions
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.put("id", "{CustomerID}");
		customers.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(category).uriParameters(uriLinkageMap).build());
		customers.addTransition(new Transition.Builder().method("POST").target(pseudo).build());

		return new ResourceStateMachine(customers);
	}

	public ResourceStateMachine getEmployeesSM() {
		CollectionResourceState employees = new CollectionResourceState("Employees", "Employees", createActionList(new Action("GETEntities", Action.TYPE.VIEW), null), "/Employees");
		ResourceState pseudo = new ResourceState(employees, "Employees_pseudo_created", createActionList(null, new Action("CreateEntity", Action.TYPE.ENTRY)));
		ResourceState employee = new ResourceState("Employees", "employee", createActionList(new Action("GETEntity", Action.TYPE.VIEW), null), "/Employees({id})");

		//Add state transitions
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.put("id", "{EmployeeID}");
		employees.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(employee).uriParameters(uriLinkageMap).build());
		employees.addTransition(new Transition.Builder().method("POST").target(pseudo).build());

		return new ResourceStateMachine(employees);
	}
	
	public ResourceStateMachine getOrdersSM() {
		CollectionResourceState orders = new CollectionResourceState("Orders", "Orders", createActionList(new Action("GETEntities", Action.TYPE.VIEW), null), "/Orders");
		ResourceState pseudo = new ResourceState(orders, "Orders_pseudo_created", createActionList(null, new Action("CreateEntity", Action.TYPE.ENTRY)));
		ResourceState order = new ResourceState("Orders", "order", createActionList(new Action("GETEntity", Action.TYPE.VIEW), null), "/Orders({id})");

		//Add state transitions
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.put("id", "{OrderID}");
		orders.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(order).uriParameters(uriLinkageMap).build());
		orders.addTransition(new Transition.Builder().method("POST").target(pseudo).build());

		return new ResourceStateMachine(orders);
	}

	public ResourceStateMachine getOrderDetailsSM() {
		CollectionResourceState orderDetails = new CollectionResourceState("Order_Details", "OrderDetails", createActionList(new Action("GETEntities", Action.TYPE.VIEW), null), "/Order_Details");
		ResourceState pseudo = new ResourceState(orderDetails, "OrderDetails_pseudo_created", createActionList(null, new Action("CreateEntity", Action.TYPE.ENTRY)));
		ResourceState orderDetail = new ResourceState("Order_Details", "orderDetail", createActionList(new Action("GETEntity", Action.TYPE.VIEW), null), "/Order_Details({id})");

		//Add state transitions
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.put("id", "{OrderID}");
		orderDetails.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(orderDetail).uriParameters(uriLinkageMap).build());
		orderDetails.addTransition(new Transition.Builder().method("POST").target(pseudo).build());

		return new ResourceStateMachine(orderDetails);
	}

	public ResourceStateMachine getProductsSM() {
		CollectionResourceState products = new CollectionResourceState("Products", "Products", createActionList(new Action("GETEntities", Action.TYPE.VIEW), null), "/Products");
		ResourceState pseudo = new ResourceState(products, "Products_pseudo_created", createActionList(null, new Action("CreateEntity", Action.TYPE.ENTRY)));
		ResourceState product = new ResourceState("Products", "product", createActionList(new Action("GETEntity", Action.TYPE.VIEW), null), "/Products({id})");
		ResourceState productUpdated = new ResourceState(product, 
				"Product", 
				createActionList(null, new Action("UpdateEntity", Action.TYPE.ENTRY)),
				null,
				"edit".split(" ")
				);
		ResourceState productDeleted = new ResourceState(product, 
				"deleted", 
				createActionList(null, new Action("DeleteEntity", Action.TYPE.ENTRY)),
				null,
				"edit".split(" ")
				);

		/* 
		 * Add navigation property Product({id})/Category
		 */
		Properties productCategoryNavProperties = new Properties();
		productCategoryNavProperties.put("entity", "Products");
		productCategoryNavProperties.put("navproperty", "Category");
		ResourceState productCategory = new ResourceState("Categories", 
				"ProductCategory", 
				createActionList(new Action("GETNavProperty", Action.TYPE.VIEW, productCategoryNavProperties), null), 
				"Products({id})/Category", 
				new ODataUriSpecification().getTemplate("/Products", ODataUriSpecification.NAVPROPERTY_URI_TYPE));

		/* 
		 * Add navigation property Product({id})/Order_Details
		 */
		Properties productOrderDetailsNavProperties = new Properties();
		productOrderDetailsNavProperties.put("entity", "OrderDetails");
		productOrderDetailsNavProperties.put("navproperty", "Order_Details");
		ResourceState productOrderDetails = new ResourceState("Order_Details", 
				"ProductOrderDetails", 
				createActionList(new Action("GETNavProperty", Action.TYPE.VIEW, productOrderDetailsNavProperties), null), 
				"Products({id})/Order_Details", 
				new ODataUriSpecification().getTemplate("/Products", ODataUriSpecification.NAVPROPERTY_URI_TYPE));

		/* 
		 * Add navigation property Product({id})/Supplier
		 */
		Properties productSupplierNavProperties = new Properties();
		productSupplierNavProperties.put("entity", "Products");
		productSupplierNavProperties.put("navproperty", "Supplier");
		ResourceState productSupplier = new ResourceState("Suppliers", 
				"ProductSupplier", 
				createActionList(new Action("GETNavProperty", Action.TYPE.VIEW, productSupplierNavProperties), null), 
				"Products({id})/Supplier", 
				new ODataUriSpecification().getTemplate("/Products", ODataUriSpecification.NAVPROPERTY_URI_TYPE));

		//Add state transitions
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.put("id", "{ProductID}");
		products.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(product).uriParameters(uriLinkageMap).build());
		products.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(productCategory).uriParameters(uriLinkageMap).build());
		products.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(productOrderDetails).uriParameters(uriLinkageMap).build());
		products.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(productSupplier).uriParameters(uriLinkageMap).build());
		products.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("PUT").target(productUpdated).uriParameters(uriLinkageMap).build());
		products.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("DELETE").target(productDeleted).uriParameters(uriLinkageMap).build());
		products.addTransition(new Transition.Builder().method("POST").target(pseudo).build());
		product.addTransition(new Transition.Builder().method("GET").target(productCategory).uriParameters(uriLinkageMap).build());
		product.addTransition(new Transition.Builder().method("GET").target(productOrderDetails).uriParameters(uriLinkageMap).build());
		product.addTransition(new Transition.Builder().method("GET").target(productSupplier).uriParameters(uriLinkageMap).build());
		product.addTransition(new Transition.Builder().method("PUT").target(productUpdated).uriParameters(uriLinkageMap).build());
		product.addTransition(new Transition.Builder().method("DELETE").target(productDeleted).uriParameters(uriLinkageMap).build());

		return new ResourceStateMachine(products);
	}
	
	public ResourceStateMachine getSuppliersSM() {
		CollectionResourceState suppliers = new CollectionResourceState("Suppliers", "Suppliers", createActionList(new Action("GETEntities", Action.TYPE.VIEW), null), "/Suppliers");
		ResourceState pseudo = new ResourceState(suppliers, "Suppliers_pseudo_created", createActionList(null, new Action("CreateEntity", Action.TYPE.ENTRY)));
		ResourceState supplier = new ResourceState("Suppliers", "supplier", createActionList(new Action("GETEntity", Action.TYPE.VIEW), null), "/Suppliers({id})");

		//Add state transitions
		Map<String, String> uriLinkageMap = new HashMap<String, String>();
		uriLinkageMap.put("id", "{SupplierID}");
		suppliers.addTransition(new Transition.Builder().flags(Transition.FOR_EACH).method("GET").target(supplier).uriParameters(uriLinkageMap).build());
		suppliers.addTransition(new Transition.Builder().method("POST").target(pseudo).build());

		return new ResourceStateMachine(suppliers);
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
