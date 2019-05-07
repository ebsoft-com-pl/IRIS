package com.temenos.useragent.generic.mediatype;

/*******************************************************************************
 * Copyright © Temenos Headquarters SA 1993-2019.  All rights reserved.
 *******************************************************************************/

import static com.temenos.useragent.generic.mediatype.AtomUtil.*;
import static com.temenos.useragent.generic.mediatype.PropertyNameUtil.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.abdera.model.Entry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Handles the Atom XML content part of the {@link Entry entry} for read and
 * update.
 * 
 * @author ssethupathi
 *
 */
public class AtomXmlContentHandler {

	private Document document;

	public AtomXmlContentHandler(Document document) {
		if (document == null) {
			throw new IllegalArgumentException(
					"Invalid content document 'null'");
		}
		this.document = document;
	}

	public Document getDocument() {
		return document;
	}

	/**
	 * Returns the count of the given element's existence in the document.
	 * 
	 * @param fqPropertyName
	 * @return count
	 * @throws IllegalArgumentException
	 *             if the fully qualified property is invalid
	 */
	public int getCount(String fqPropertyName) {
		String[] pathParts = flattenPropertyName(fqPropertyName);
		Node parent = identifyParentElement(fqPropertyName, document, pathParts);
		if (parent == null) {
			return 0;
		}
		validateElementName(pathParts[pathParts.length - 1], fqPropertyName);
		String elementName = buildElementName(pathParts[pathParts.length - 1]);
		Element element = identifyChildElement(parent, elementName, 0);
		return countElementSiblings(element);
	}

	/**
	 * Retruns the value for the given element's text node in the document.
	 * 
	 * @param fqPropertyName
	 * @return value
	 * @throws IllegalArgumentException
	 *             if the fully qualified property is invalid
	 */
	public String getValue(String fqPropertyName) {
		String[] pathParts = flattenPropertyName(fqPropertyName);
		Node parent = identifyParentElement(fqPropertyName, document, pathParts);
		if (parent == null) {
			return null;
		}
		validateElementName(pathParts[pathParts.length - 1], fqPropertyName);
		String elementName = buildElementName(pathParts[pathParts.length - 1]);
		Element element = getFirstChildElement(parent, elementName);
		if (element != null) {
			return element.getTextContent();
		} else {
			return null;
		}
	}

	/**
	 * Sets the given value to the element of the fully qualified property name.
	 * 
	 * @param fqPropertyName
	 * @param value
	 * @throws IllegalArgumentException
	 *             if the fully qualified property is invalid
	 */
	public void setValue(String fqPropertyName, String value) {
		String[] pathParts = flattenPropertyName(fqPropertyName);
		Node parent = checkAndCreateParent(fqPropertyName, document, pathParts);
		validateElementName(pathParts[pathParts.length - 1], fqPropertyName);
		String elementName = buildElementName(pathParts[pathParts.length - 1]);
		Element element = getFirstChildElement(parent, elementName);
		if (element != null) {
			element.setTextContent(value);
		} else {
			appendNewChildElement(parent, elementName, value);
		}
	}

	public void remove(String fqPropertyName) {
		String[] pathParts = flattenPropertyName(fqPropertyName);
		Node parent = identifyParentElement(fqPropertyName, document, pathParts);
		if (parent == null) {
			return;
		}
		int expectedIndex = extractIndex(pathParts[pathParts.length - 1]);
		String elementName = buildElementName(pathParts[pathParts.length - 1]);
		if (isPropertyNameWithIndex(pathParts[pathParts.length - 1])) {
			parent = identifyChildElement(parent, elementName, 0);
			elementName = "d:element";
		}
		Element element = identifyChildElement(parent, elementName,
				expectedIndex);
		parent.removeChild(element);
	}

	private int countElementSiblings(Element parent) {
		if (parent == null) {
			return 0;
		}
		List<Element> elementChildren = getChildElements(parent, "d:element");
		if (elementChildren.isEmpty()) {
			return 1;
		}
		return elementChildren.size();
	}

	private Element cloneExistingElement(Node parent, String fqElementName) {
		Element cloneableElement = getFirstChildElement(parent, fqElementName);
		if (cloneableElement == null) {
			throw new IllegalStateException("Invalid element name '"
					+ fqElementName + "' to set value to");
		}
		Node clonedNode = null;
		List<Element> childElements = getChildElements(cloneableElement);
		if (childElements.isEmpty()) {
			clonedNode = cloneableElement.cloneNode(false);
		} else {
			clonedNode = cloneableElement.cloneNode(true);
			removeNonContainerChildElements(clonedNode);
		}
		parent.appendChild(clonedNode);
		return (Element) clonedNode;
	}

	private void removeNonContainerChildElements(Node parent) {
		NodeList children = parent.getChildNodes();
		for (int idx = 0; idx < children.getLength(); idx++) {
			Node currentNode = children.item(idx);
			if (!getChildElements(currentNode).isEmpty()) {
				removeNonContainerChildElements(currentNode);
			} else if (Node.ELEMENT_NODE == currentNode.getNodeType()) {
				parent.removeChild(currentNode);
			}
		}
	}

	private void appendNewChildElement(Node parent, String fqPropertyName,
			String value) {
		Element newElement = parent.getOwnerDocument().createElementNS(
				NS_ODATA, fqPropertyName);
		newElement.setTextContent(value);
		parent.appendChild(newElement);
	}

	private Node identifyParentElement(String fqPropertyName,
			Document document, String... pathParts) {
		Node parent = document.getFirstChild();
		int pathIndex = 0;
		while (pathIndex < (pathParts.length - 1)) {
			String pathPart = pathParts[pathIndex];
			validateElementNameWithIndex(pathPart, fqPropertyName);
			parent = identifyChildElement(parent, buildElementName(pathPart), 0);
			parent = identifyChildElement(parent, buildElementName("element"),
					extractIndex(pathPart));
			pathIndex++;
		}
		return parent;
	}

	private Node checkAndCreateParent(String fqPropertyName, Document document,
			String... pathParts) {
		Node parent = document.getFirstChild();
		int pathIndex = 0;
		while (pathIndex < (pathParts.length - 1)) {
			String pathPart = pathParts[pathIndex];
			validateElementNameWithIndex(pathPart, fqPropertyName);
			parent = checkAndCreateChild(parent, buildElementName(pathPart), 0);
			parent = checkAndCreateChild(parent, buildElementName("element"),
					extractIndex(pathPart));
			pathIndex++;
		}
		return parent;
	}



	private String buildElementName(String path) {
		String elementName = path;
		if (isPropertyNameWithIndex(path)) {
			elementName = path.substring(0, path.indexOf("("));
		}
		return "d:" + elementName;
	}

	private Element checkAndCreateChild(Node parent, String childName,
			int expectedIndex) {
		if (parent == null) {
			return null;
		}
		List<Element> childElements = getChildElements(parent, childName);
		if (expectedIndex < childElements.size()) {
			return identifyChildElement(parent, childName, expectedIndex);
		} else if (expectedIndex == childElements.size()) {
			return (Element) cloneExistingElement(parent, childName);
		} else {
			throw new IllegalStateException(
					"Invalid index '"
							+ expectedIndex
							+ "' to set value to. Only supported indexs are between '0' and '"
							+ childElements.size() + "'");
		}
	}



	private void validateElementNameWithIndex(String elementName,
			String fqPropertyName) {
		if (!isPropertyNameWithIndex(elementName)) {
			throw new IllegalArgumentException("Invalid part '" + elementName
					+ "' in fully qualified property name '" + fqPropertyName
					+ "'");
		}
	}

	private void validateElementName(String elementName, String fqPropertyName) {
		if (elementName.contains("(")) {
			throw new IllegalArgumentException("Invalid part '" + elementName
					+ "' in fully qualified property name '" + fqPropertyName
					+ "'");
		}
	}



	private Element identifyChildElement(Node parent, String childName,
			int index) {
		if (parent == null) {
			return null;
		}
		return getSpecificElement(parent.getChildNodes(), childName, index);
	}

	private Element getFirstChildElement(Node node, String childName) {
		return getSpecificElement(node.getChildNodes(), childName, 0);
	}

	private Element getSpecificElement(NodeList nodes, String childName,
			int expectedIndex) {
		int matchingElementIdx = 0;
		for (int idx = 0; idx < nodes.getLength(); idx++) {
			Node node = nodes.item(idx);
			if (Node.ELEMENT_NODE == node.getNodeType()
					&& node.getNodeName().equals(childName)) {
				if (matchingElementIdx == expectedIndex) {
					return (Element) node;
				}
				matchingElementIdx++;
			}
		}
		return null;
	}

	private List<Element> getChildElements(Node parent) {
		List<Element> elements = new ArrayList<Element>();
		NodeList children = parent.getChildNodes();
		for (int idx = 0; idx < children.getLength(); idx++) {
			Node child = children.item(idx);
			if (Node.ELEMENT_NODE == child.getNodeType()) {
				elements.add((Element) child);
			}
		}
		return elements;
	}

	private List<Element> getChildElements(Node parent, String childName) {
		List<Element> elements = new ArrayList<Element>();
		NodeList children = parent.getChildNodes();
		for (int idx = 0; idx < children.getLength(); idx++) {
			Node child = children.item(idx);
			if (Node.ELEMENT_NODE == child.getNodeType()
					&& child.getNodeName().equals(childName)) {
				elements.add((Element) child);
			}
		}
		return elements;
	}
}
