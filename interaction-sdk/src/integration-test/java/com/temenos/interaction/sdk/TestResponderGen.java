package com.temenos.interaction.sdk;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Test;

public class TestResponderGen {

	@Test
	public void testSourceGenerationFromEDMX() {
	    File srcTargetDir = new File("./target/integration-test/java");
	    srcTargetDir.mkdirs();
	    File configTargetDir = new File("./target/integration-test/resources");
	    configTargetDir.mkdirs();
	    
		assertEquals(6, countFiles(new File(srcTargetDir, "AirlineModel")));
		assertEquals(3, countFiles(new File(configTargetDir, "META-INF")));
	}
	
	private int countFiles(File dir) {
		int cnt = 0;
		File[] files = dir.listFiles();
		if (files != null) {
			for (int i = 0; i < files.length; i++) {
				if (files[i].isDirectory()) {
					cnt += countFiles(files[i]);
				} else {
					cnt++;
				}
			}
		}
		return cnt;
	}
}
