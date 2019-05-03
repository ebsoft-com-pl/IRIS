package com.temenos.interaction.example.hateoas.simple;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.resource.EntityResource;
import com.temenos.interaction.example.hateoas.simple.model.Preferences;

public class GETPreferencesCommand implements InteractionCommand {

	/* Implement InteractionCommand interface */
	
	@Override
	public Result execute(InteractionContext ctx) {
		assert(ctx != null);
		// retrieve from a database, etc.
		EntityResource<Preferences> resource = new EntityResource<Preferences>(new Preferences("user", "GBP", "en"));
		ctx.setResource(resource);
		return Result.SUCCESS;
	}

}
