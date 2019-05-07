package com.temenos.interaction.loader.classloader;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.net.URL;
import java.net.URLClassLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class manages the loading of jar files, which would contain new InteractionCommands.
 * The jar files are supplied as an array of URLs.
 * It extends the URLClassLoader and basically forces the supplied jar files to the front of the search
 * down the classpath.
 * It performs the following search:
 *   1) Tries to find the required class loaded in the local classpath.
 *   2) Tries to find the required class in the supplied jar files.
 *   3) Delegates the search to the parent class loader, which will typically be the context class loader from the current thread.
 *
 * If the class is not found a ClassNotFoundException is thrown.
 *
 * @author trojanbug
 */

public class ParentLastURLClassloader extends URLClassLoader {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(ParentLastURLClassloader.class);

    public ParentLastURLClassloader(URL[] urls, ClassLoader cl) {
        super(urls, cl);
    }

    public ParentLastURLClassloader(URL[] urls) {
        super(urls);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        // check if loaded locally
        Class<?> result = findLoadedClass(name);
        if (result == null) {
            try {
                // search locally 
                result = findClass(name);
            } catch (ClassNotFoundException ex) {
                // Next, delegate to the parent, if not found locally.
                if (LOGGER.isWarnEnabled()) {
                    LOGGER.warn("Next, delegate to the parent, if not found locally.", ex);
                }
                try {
                result = getParent().loadClass(name);
                } catch (ClassNotFoundException ex2) {
                    LOGGER.warn("Classloader failed to find class " + name, ex2);
                }
            }
        }

        if (resolve) {
            resolveClass(result);
        }
        return result;
    }
}
