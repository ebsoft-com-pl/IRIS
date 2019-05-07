/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/
package com.temenos.interaction.rimdsl;

import org.eclipse.xtext.conversion.IValueConverterService;
import org.eclipse.xtext.naming.IQualifiedNameProvider;
import org.eclipse.xtext.parser.IEncodingProvider;
import org.eclipse.xtext.resource.IResourceServiceProvider;
import org.eclipse.xtext.resource.impl.ResourceDescriptionsProvider;
import org.eclipse.xtext.service.DispatchingProvider;

import com.google.inject.Binder;
import com.temenos.interaction.rimdsl.encoding.RIMEncodingProvider;
import com.temenos.interaction.rimdsl.scoping.RIMDslQualifiedNameProvider;
import com.temenos.interaction.rimdsl.scoping.RIMDslResourceServiceProvider;
import com.temenos.interaction.rimdsl.scoping.SpecialResourceDescriptionsProvider;

public class RIMDslRuntimeModule extends AbstractRIMDslRuntimeModule {

	@Override
	public Class<? extends IValueConverterService> bindIValueConverterService() {
		return RIMDslTerminalConverters.class;
	}

	@Override
	public Class<? extends IQualifiedNameProvider> bindIQualifiedNameProvider() {
		return RIMDslQualifiedNameProvider.class;
	}

	// no @Override
	public Class<? extends IResourceServiceProvider> bindIResourceServiceProvider() {
		return RIMDslResourceServiceProvider.class;
	}
	
	public Class<? extends ResourceDescriptionsProvider> bindResourceDescriptionsProvider() {
		return SpecialResourceDescriptionsProvider.class;
	}

	@Override
	public Class<? extends org.eclipse.xtext.resource.containers.IAllContainersState.Provider> bindIAllContainersState$Provider() {
		return FasterResourceSetBasedAllContainersStateProvider.class;
	}
    
	@Override
    public void configureRuntimeEncodingProvider(Binder binder) {
        binder.bind(IEncodingProvider.class)
        .annotatedWith(DispatchingProvider.Runtime.class)
        .to(RIMEncodingProvider.class);
    }

}
