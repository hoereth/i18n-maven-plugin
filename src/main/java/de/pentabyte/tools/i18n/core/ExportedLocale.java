package de.pentabyte.tools.i18n.core;

/**
 * Relates to "exported-locale" entry from i18n.xml file.
 * 
 * @author Michael Höreth
 */
public class ExportedLocale {
	private String value;
	private String fallback;

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getFallback() {
		return fallback;
	}

	public void setFallback(String fallback) {
		this.fallback = fallback;
	}

}