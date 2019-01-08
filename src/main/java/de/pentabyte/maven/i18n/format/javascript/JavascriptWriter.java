/**
 * 
 */
package de.pentabyte.maven.i18n.format.javascript;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonGenerator.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

import de.pentabyte.maven.i18n.output.LanguageFileWriter;
import de.pentabyte.tools.i18n.core.Entry;
import de.pentabyte.tools.i18n.core.ExportedLocale;
import de.pentabyte.tools.i18n.core.LanguageFileFormat;
import de.pentabyte.tools.i18n.core.Output;
import de.pentabyte.tools.i18n.core.Table;

/**
 * Produces plain "javascript" language files.
 * 
 * @author Michael Höreth
 */
public class JavascriptWriter implements LanguageFileWriter {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * de.pentabyte.maven.i18n.format.LanguageFileWriter#write(java.lang.String,
	 * java.io.File, java.lang.String,
	 * de.pentabyte.maven.i18n.core.ExportedLocale,
	 * de.pentabyte.maven.i18n.core.Table, java.lang.String)
	 */
	@Override
	public void write(File tableDirectory, String inputBasename, Output output, ExportedLocale locale, Table table,
			String fileComment) throws FileNotFoundException, IOException {
		String suffix = StringUtils.isEmpty(locale.getValue()) ? "" : "_" + locale.getValue();

		Map<String, Object> structure = new LinkedHashMap<>();

		for (String key : table.getEntries().keySet()) {
			Entry entry = table.getEntries().get(key);
			if (entry.getTextMap().size() > 0) {
				String value = table.getEntryText(key, locale.getValue());
				addToStructure(structure, key, value, ".");
			}
		}

		String resultingBasename = (output.getBasename() == null ? (inputBasename == null ? "messages" : inputBasename)
				: output.getBasename());

		ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter().without(Feature.AUTO_CLOSE_TARGET);

		PrintWriter writer = new PrintWriter(new File(output.getDirectory(),
				resultingBasename + suffix + "." + LanguageFileFormat.JAVASCRIPT.getExtension()));
		writer.write("/* " + fileComment + " */\n\n");
		writer.write("var " + resultingBasename + " = ");
		ow.writeValue(writer, structure);
		writer.write(";");
		writer.close();
	}

	/**
	 * wird für die JAVASCRIPT Struktur benötigt
	 * 
	 * @param key
	 *            Falls noch eine Hierarchie dahinter steckt - dann aufdröseln
	 */
	private static void addToStructure(Map<String, Object> structure, String key, String value, String keySeparator) {
		String[] parts = key.split(Pattern.quote(keySeparator));
		String top = parts[0];
		if (parts.length == 1) {
			structure.put(top + "$", value);
		} else {
			@SuppressWarnings("unchecked")
			Map<String, Object> subStructure = (Map<String, Object>) structure.get(top);
			if (subStructure == null)
				subStructure = new LinkedHashMap<>();
			structure.put(top, subStructure);
			addToStructure(subStructure, key.substring((top + keySeparator).length()), value, keySeparator);
		}
	}

}
