package com.temenos.interaction.example.hateoas.banking;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import com.temenos.interaction.core.command.InteractionCommand;
import com.temenos.interaction.core.command.InteractionContext;
import com.temenos.interaction.core.resource.EntityResource;

public class GETPreferencesCommand implements InteractionCommand {

	/* Implement InteractionCommand interface */
	
	@Override
	public Result execute(InteractionContext ctx) {
		assert(ctx != null);
		// retrieve from a database, etc.
		EntityResource<Preferences> resource = new EntityResource<Preferences>(new Preferences("user", "UK", "en"));
		ctx.setResource(resource);
		return Result.SUCCESS;
	}

}
