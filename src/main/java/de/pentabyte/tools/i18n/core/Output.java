package de.pentabyte.tools.i18n.core;

import java.io.File;

/**
 * Output configuration.
 * 
 * @author Michael Höreth
 */
public class Output {
	private File directory;
	private String basename;
	private LanguageFileFormat format;
	private String keySeparator;

	public File getDirectory() {
		return directory;
	}

	public void setDirectory(File directory) {
		this.directory = directory;
	}

	public String getBasename() {
		return basename;
	}

	public void setBasename(String basename) {
		this.basename = basename;
	}

	public LanguageFileFormat getFormat() {
		return format;
	}

	public void setFormat(LanguageFileFormat format) {
		this.format = format;
	}

	public String getKeySeparator() {
		return keySeparator;
	}

	public void setKeySeparator(String keySeparator) {
		this.keySeparator = keySeparator;
	}

}
