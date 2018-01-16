package de.pentabyte.tools.i18n.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Only needed for refactoring.
 * 
 * @author Michael Höreth
 */
public class EntryNode {
	private String description;

	private Map<String, EntryNode> nodes;

	private Map<String, String> textMap;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, EntryNode> getNodes() {
		if (nodes == null)
			nodes = new HashMap<>();
		return nodes;
	}

	public void setNodes(Map<String, EntryNode> nodes) {
		this.nodes = nodes;
	}

	public Map<String, String> getTextMap() {
		if (textMap == null)
			textMap = new HashMap<>();
		return textMap;
	}

}
