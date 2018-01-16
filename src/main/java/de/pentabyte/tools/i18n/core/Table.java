package de.pentabyte.tools.i18n.core;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Will be populated with i18n.xml file.
 * 
 * @author Michael Höreth
 */
public class Table {
	private static Pattern PLACHOLDER_PATTERN = Pattern.compile("\\$\\{([^\\}]*)\\}");

	private List<ExportedLocale> exportedLocales;
	/**
	 * Key: z.B. "UserInterface.Login.Username"
	 */
	private Map<String, Entry> entries;

	private List<Output> output;

	public List<ExportedLocale> getExportedLocales() {
		if (exportedLocales == null) {
			exportedLocales = new ArrayList<ExportedLocale>();
		}
		return exportedLocales;
	}

	public void setExportedLocales(List<ExportedLocale> exportedLocales) {
		this.exportedLocales = exportedLocales;
	}

	public Map<String, Entry> getEntries() {
		if (entries == null) {
			entries = new LinkedHashMap<String, Entry>();
		}
		return entries;
	}

	public List<Output> getOutput() {
		if (output == null) {
			output = new ArrayList<>();
		}
		return output;
	}

	public void setOutput(List<Output> output) {
		this.output = output;
	}

	public String getEntryText(String key, String locale) {
		Entry entry = getEntries().get(key);
		if (entry == null) {
			throw new IllegalArgumentException("There is no key [" + key + "] for locale [" + locale + "]");
		}

		String value = entry.getTextMap().get(locale);

		if (value == null) {
			value = entry.getTextMap().get("");
		}

		if (value == null) {
			ExportedLocale exportedLocale = findExportedLocale(locale);
			if (exportedLocale == null) {
				throw new RuntimeException("The locale [" + locale + "] will not be exported.");
			}
			if (exportedLocale.getFallback() != null) {
				value = getEntryText(key, exportedLocale.getFallback());
			}
		}

		if (value == null) {
			throw new RuntimeException("Value is null for key [" + key + "] and locale [" + locale + "]");
		}

		Matcher m = PLACHOLDER_PATTERN.matcher(value);
		while (m.find()) {
			String find = m.group();
			String mnemonic = m.group(1);
			String replace = getEntryText(mnemonic, locale);
			value = value.replace(find, replace);
		}

		return value;
	}

	public ExportedLocale findExportedLocale(String locale) {
		for (ExportedLocale exported : getExportedLocales()) {
			if (exported.getValue().equals(locale)) {
				return exported;
			}
		}
		return null;
	}

	/**
	 * Die "Table" Datenstruktur ist flach. Hiermit kann man eine Hierarchie
	 * erzeugen.
	 */
	public Map<String, EntryNode> createHierarchy(String keySeparator) {
		// Mal ganz schnell die flache Liste in eine Hierarchie überführen.
		Map<String, EntryNode> nodes = new LinkedHashMap<String, EntryNode>();
		for (Map.Entry<String, Entry> entry : getEntries().entrySet()) {
			String key = entry.getKey();
			Entry e = entry.getValue();

			String[] parts = key.split(Pattern.quote(keySeparator));

			EntryNode node = nodes.get(parts[0]);

			if (node == null) {
				node = new EntryNode();
				nodes.put(parts[0], node);
			}

			for (int level = 0; level < parts.length; level++) {
				if (level > 0) {
					String keyFragment = parts[level];
					// nur Struktur
					EntryNode nextLevel = node.getNodes().get(keyFragment);
					if (nextLevel == null) {
						nextLevel = new EntryNode();
						node.getNodes().put(keyFragment, nextLevel);
						node.setDescription(e.getDescription());
					}
					node = nextLevel;
				}

				if (level == parts.length - 1) {
					// Werte reinschreiben
					node.getTextMap().putAll(e.getTextMap());
					node.setDescription(e.getDescription());
				}
			}
		}
		return nodes;
	}
}