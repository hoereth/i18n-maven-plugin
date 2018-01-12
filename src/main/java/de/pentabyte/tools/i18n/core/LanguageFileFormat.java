package de.pentabyte.tools.i18n.core;

/**
 * Supported language file formats.
 * 
 * @author Michael Höreth
 */
public enum LanguageFileFormat {
	/**
	 * The typical "messages_x.properties" file
	 */
	JAVA_PROPERTIES("properties"),
	/**
	 * The typical "x/Localizable.strings" file
	 */
	C_STRINGS("strings"),
	/**
	 * Plain javascript.
	 */
	JAVASCRIPT("js");

	private String extension;

	private LanguageFileFormat(String extension) {
		this.extension = extension;
	}

	public String getExtension() {
		return extension;
	}

}