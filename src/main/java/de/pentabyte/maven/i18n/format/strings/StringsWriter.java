/**
 * 
 */
package de.pentabyte.maven.i18n.format.strings;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import de.pentabyte.maven.i18n.output.LanguageFileWriter;
import de.pentabyte.tools.i18n.core.Entry;
import de.pentabyte.tools.i18n.core.ExportedLocale;
import de.pentabyte.tools.i18n.core.LanguageFileFormat;
import de.pentabyte.tools.i18n.core.Output;
import de.pentabyte.tools.i18n.core.Table;

/**
 * Produces "Strings" files for macOS, iOS, tvOS.
 * 
 * @author Michael Höreth
 */
public class StringsWriter implements LanguageFileWriter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.pentabyte.maven.i18n.format.LanguageFileWriter#write(java.io.File,
	 * java.lang.String, de.pentabyte.maven.i18n.core.ExportedLocale,
	 * de.pentabyte.maven.i18n.core.Table, java.lang.String)
	 */
	@Override
	public void write(File tableDirectory, String inputBasename, Output output, ExportedLocale locale, Table table,
			String fileComment) throws FileNotFoundException, IOException {
		File subdir = new File(output.getDirectory(), locale.getValue() + ".lproj");
		subdir.mkdir();

		File file = new File(subdir,
				(output.getBasename() == null ? (inputBasename == null ? "Localizable" : inputBasename)
						: output.getBasename()) + "." + LanguageFileFormat.STRINGS.getExtension());

		BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF16"));
		writer.write("/* " + fileComment + " */\n\n");

		for (String key : table.getEntries().keySet()) {
			Entry entry = table.getEntries().get(key);
			if (entry.getDescription() != null) {
				writer.write("\n/* " + entry.getDescription() + " */\n");
			}

			if (entry.getTextMap().size() > 0) {
				String value = table.getEntryText(key, locale.getValue());
				writer.write("\"" + escapeStringResource(key) + "\" = \"" + escapeStringResource(value) + "\";\n");
			}
		}
		writer.close();
	}

	/**
	 * Anwendung: Objective-C resource file
	 */
	private static String escapeStringResource(String input) {
		input = input.replace("\\", "\\\\");
		input = input.replace("\"", "\\\"");
		input = input.replace("\n", "\\n");
		input = input.replace("\r", "\\r");
		return input;
	}

}
