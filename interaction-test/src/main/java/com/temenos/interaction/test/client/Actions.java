package com.temenos.interaction.test.client;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.ArrayList;

public class Actions extends ArrayList<Activity> {

    
    private static final long serialVersionUID = 7455318429430311610L;

    public boolean has(Class<?> clazz) {
        for(Activity act : this) {
            if(act.getClass() == clazz) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
	public <T extends Activity> T get(Class<?> clazz) {
        
        for(Activity act : this) {
            if(act.getClass() == clazz) {
                return (T) clazz.cast(act);
            }
        }
        
        return null;
    }
}
