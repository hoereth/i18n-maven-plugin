package de.pentabyte.tools.i18n.core;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Relates to "entry"-node from i18n.xml file. This is a NON-recursive data
 * structure.
 * 
 * @author Michael Höreth
 */
public class Entry {
	/**
	 * Informal description of the text.
	 */
	private String description;
	/**
	 * locale -> translation
	 */
	private Map<String, String> textMap;

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Map<String, String> getTextMap() {
		if (textMap == null) {
			textMap = new LinkedHashMap<String, String>();
		}
		return textMap;
	}

	public void setTextMap(Map<String, String> textMap) {
		this.textMap = textMap;
	}

}
