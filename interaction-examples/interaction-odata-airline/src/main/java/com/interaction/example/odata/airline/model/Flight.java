package com.interaction.example.odata.airline.model;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import javax.persistence.Basic;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@SuppressWarnings("unused")
@Entity
public class Flight {

	@Id
	@Basic(optional = false)
	private Long flightID;
	private Long flightScheduleNum;
	@Temporal(TemporalType.TIMESTAMP)
	private java.util.Date takeoffTime;
	
	//@OneToMany(cascade = CascadeType.ALL, mappedBy = "flight")
	//private Collection<Passenger> flightPassengers;
	
	public Flight() {}
}