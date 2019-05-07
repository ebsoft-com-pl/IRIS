package com.temenos.interaction.media.odata.xml;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/


import java.util.HashSet;
import java.util.Set;

import org.custommonkey.xmlunit.Difference;
import org.custommonkey.xmlunit.DifferenceConstants;
import org.custommonkey.xmlunit.DifferenceListener;
import org.w3c.dom.Node;

/**
 * XMLUnit difference listener to ignore the specified list of elements 
 */
public class IgnoreNamedElementsXMLDifferenceListener implements DifferenceListener {
    private Set<String> blackList = new HashSet<String>();

    public IgnoreNamedElementsXMLDifferenceListener(String ... elementNames) {
        for (String name : elementNames) {
            blackList.add(name);
        }
    }

    public int differenceFound(Difference difference) {
        if (difference.getId() == DifferenceConstants.TEXT_VALUE_ID) {
            if (blackList.contains(difference.getControlNodeDetail().getNode().getParentNode().getNodeName())) {
                return DifferenceListener.RETURN_IGNORE_DIFFERENCE_NODES_IDENTICAL;
            }
        }
        return DifferenceListener.RETURN_ACCEPT_DIFFERENCE;
    }

    public void skippedComparison(Node node, Node node1) {
    }
}