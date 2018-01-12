/**
 * 
 */
package de.pentabyte.maven.i18n.format.java;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import de.pentabyte.maven.i18n.output.LanguageFileWriter;
import de.pentabyte.tools.i18n.core.Entry;
import de.pentabyte.tools.i18n.core.ExportedLocale;
import de.pentabyte.tools.i18n.core.LanguageFileFormat;
import de.pentabyte.tools.i18n.core.Table;

/**
 * Produces "properties" files for JAVA.
 * 
 * @author Michael Höreth
 */
public class JavaPropertiesWriter implements LanguageFileWriter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see de.pentabyte.maven.i18n.format.OutputWriter#write(java.io.File,
	 * java.lang.String, de.pentabyte.maven.i18n.core.ExportedLocale,
	 * de.pentabyte.maven.i18n.core.Table, java.lang.String)
	 */
	@Override
	public void write(String inputBasename, File outputDirectory, String outputBasename, ExportedLocale locale,
			Table table, String fileComment) throws FileNotFoundException, IOException {

		String basename = (outputBasename == null ? (inputBasename == null ? "messages" : inputBasename)
				: outputBasename);

		String suffix = StringUtils.isEmpty(locale.getValue()) ? "" : "_" + locale.getValue();
		Properties p = new LinkedProperties();
		for (String key : table.getEntries().keySet()) {
			Entry entry = table.getEntries().get(key);
			if (entry.getTextMap().size() > 0) {
				String value = table.getEntryText(key, locale.getValue());
				p.put(key, value);
			}
		}

		File file = new File(outputDirectory,
				basename + suffix + "." + LanguageFileFormat.JAVA_PROPERTIES.getExtension());
		p.store(new FileOutputStream(file), fileComment);

	}

}
