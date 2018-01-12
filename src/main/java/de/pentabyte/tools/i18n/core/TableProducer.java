package de.pentabyte.tools.i18n.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.CharEncoding;
import org.apache.commons.lang3.StringUtils;
import org.apache.maven.plugin.logging.Log;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.pentabyte.maven.i18n.format.cstrings.StringsWriter;
import de.pentabyte.maven.i18n.format.java.JavaPropertiesWriter;
import de.pentabyte.maven.i18n.format.javascript.JavascriptWriter;

/**
 * Converts between XML files (i18n-table.xsd format) and several kinds of
 * language file formats. Provides the reverse direction for some formats (see
 * reengieer).
 * 
 * @author Michael Höreth
 */
public class TableProducer {
	final static DocumentBuilderFactory factory;

	static {
		factory = DocumentBuilderFactory.newInstance();
		factory.setIgnoringElementContentWhitespace(true);
		factory.setNamespaceAware(true);
		final SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);

		Schema schema;
		try {
			schema = sf.newSchema(new StreamSource(TableProducer.class.getResourceAsStream("table-1.0.xsd")));
			factory.setSchema(schema);
		} catch (SAXException e) {
			throw new RuntimeException(e);
		}
	}

	/**
	 * Translates an i18n-Table into the specified outputFormat.
	 * 
	 * @param inputFile
	 * @param outputDirectory
	 *            defaults to directory of inputFile when null
	 * @param outputBasename
	 *            e.g. "messages"
	 * @param outputFormat
	 */
	public static void transformFile(File inputFile, File outputDirectory, String outputBasename,
			LanguageFileFormat outputFormat, Log log, String keySeparator) {

		if (outputDirectory == null)
			outputDirectory = inputFile.getParentFile();
		try {
			log.info("processing: " + inputFile);
			transformXML(new FileInputStream(inputFile), FilenameUtils.getBaseName(inputFile.getName()),
					inputFile.getParentFile(), outputDirectory, outputBasename, outputFormat, keySeparator);
		} catch (Exception e) {
			throw new RuntimeException("Problem with [" + inputFile.getAbsolutePath() + "]", e);
		}
	}

	/**
	 * Sucht rekursiv alle "i18n.xml" Dateien und übersetzt sie.
	 * 
	 * @param tableDirectory
	 * @param outputBasename
	 * @param outputFormat
	 */
	public static void transformRecursively(File tableDirectory, String inputBasename, String outputBasename,
			LanguageFileFormat outputFormat, Log log, String keySeparator) {
		if (!tableDirectory.exists())
			throw new RuntimeException(tableDirectory + " does not exist!");

		String inputFilename = inputBasename + ".xml";

		File[] files = tableDirectory.listFiles();
		for (File file : files) {
			if (file.getName().equalsIgnoreCase(inputFilename)) {
				transformFile(file, file.getParentFile(), outputBasename, outputFormat, log, keySeparator);
			}
			if (file.isDirectory()) {
				transformRecursively(file, inputBasename, outputBasename, outputFormat, log, keySeparator);
			}
		}
	}

	/**
	 * Reads all xml data and produces a flat table with all entries.
	 * 
	 * @param is
	 *            Data Source
	 * @param keySeparator
	 *            Composite keys will be separated with this.
	 */
	private static Table readXmlFile(InputStream is, String keySeparator) throws Exception {
		Table table = new Table();
		DocumentBuilder db = factory.newDocumentBuilder();
		db.setErrorHandler(new ErrorHandler() {
			@Override
			public void warning(SAXParseException exception) throws SAXException {
				throw new RuntimeException(exception);
			}

			@Override
			public void fatalError(SAXParseException exception) throws SAXException {
				throw new RuntimeException(exception);
			}

			@Override
			public void error(SAXParseException exception) throws SAXException {
				throw new RuntimeException(exception);
			}
		});
		Document doc = db.parse(is);
		Element rootElem = doc.getDocumentElement();

		NodeList nodeList = rootElem.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element e = (Element) node;
				if (e.getTagName().equals("locale-config")) {
					NodeList localeList = e.getChildNodes();
					for (int c = 0; c < localeList.getLength(); c++) {
						Node n3 = localeList.item(c);
						if (n3 instanceof Element)
							processLocale((Element) n3, table);
					}
				}
				if (e.getTagName().equals("entry")) {
					processEntry(e, table, "", keySeparator);
				}
				if (e.getTagName().equals("output")) {
					processOutput(e, table);
				}
			}
		}
		return table;
	}

	private static void processLocale(Element element, Table table) {
		String value = element.getAttribute("value");
		String fallback = element.getAttribute("fallback");

		for (ExportedLocale test : table.getExportedLocales()) {
			if (test.getValue().equals(value)) {
				throw new RuntimeException("Locale [" + value + "] has already been defined.");
			}
		}

		ExportedLocale locale = new ExportedLocale();
		locale.setValue(value);
		if (!"".equals(fallback))
			locale.setFallback(fallback);

		table.getExportedLocales().add(locale);
	}

	/**
	 * Liest einen "output" Konfig-Eintrag ein
	 */
	private static void processOutput(Element element, Table table) {
		String directory = element.getAttribute("directory");
		String basename = element.getAttribute("basename");
		String keySeparator = element.getAttribute("basename");
		String format = element.getAttribute("format");

		Output output = new Output();
		if (StringUtils.isNotEmpty(directory))
			output.setDirectory(new File(directory));
		if (StringUtils.isNotEmpty(basename))
			output.setBasename(basename);
		if (StringUtils.isNotEmpty(keySeparator))
			output.setKeySeparator(keySeparator);
		if (StringUtils.isNotEmpty(format))
			output.setFormat(LanguageFileFormat.valueOf(format));

		table.getOutput().add(output);
	}

	/**
	 * Reads one entry.
	 */
	private static void processEntry(Element element, Table table, String keyPrefix, String keySeparator)
			throws DOMException {
		Entry entry = new Entry();

		String key = keyPrefix + element.getAttribute("key");
		if (table.getEntries().containsKey(key)) {
			throw new RuntimeException("Key [" + key + "] already exists.");
		}
		table.getEntries().put(key, entry);

		NodeList nodeList = element.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node n = nodeList.item(i);
			if (n instanceof Element) {
				Element e = (Element) n;

				if (e.getTagName().equals("description")) {
					entry.setDescription(e.getTextContent());
				}

				if (e.getTagName().equals("text")) {
					String localized = e.getTextContent();
					if (localized.trim().length() > 0) {
						String locale = e.getAttribute("locale");
						if (entry.getTextMap().containsKey(locale)) {
							throw new RuntimeException("Locale [" + locale + "] already exists for key [" + key + "]");
						}
						entry.getTextMap().put(locale, localized);
					}
				}

				if (e.getTagName().equals("entry")) {
					processEntry(e, table, key + keySeparator, keySeparator);
				}
			}
		}
	}

	/**
	 * Output-File is written here.
	 */
	private static void generateLocalizedFile(String inputBasename, File outputDirectory, String outputBasename,
			ExportedLocale locale, Table table, LanguageFileFormat outputFormat, String keySeparator)
			throws IOException {

		String fallback = locale.getFallback() != null ? ", Fallback: " + locale.getFallback() : "";

		String fileComment = "AUTO-GENERATED from schema file [" + inputBasename
				+ "] with i18n-maven-plugin. Localization: " + locale.getValue() + fallback + ". "
				+ "Please do not edit this file manually!";

		switch (outputFormat) {
		case JAVA_PROPERTIES: {
			new JavaPropertiesWriter().write(inputBasename, outputDirectory, outputBasename, locale, table,
					fileComment);
			break;
		}
		case C_STRINGS: {
			new StringsWriter().write(inputBasename, outputDirectory, outputBasename, locale, table, fileComment);
			break;
		}
		case JAVASCRIPT: {
			if (keySeparator != null && !keySeparator.equals(".")) {
				throw new RuntimeException("Für JAVASCRIPT ist nur der Punkt als Separator erlaubt!");
			}

			new JavascriptWriter().write(inputBasename, outputDirectory, outputBasename, locale, table, fileComment);

			break;
		}
		}

	}

	/**
	 * Generates individual properties files based on the processed XML file
	 * with a prior call to processXmlFile method.
	 * 
	 * @param baseName
	 *            path to the generated properties files excluding locale
	 *            information and extension.
	 */
	public static void generateLocalizedFiles(String inputBasename, File outputDirectory, String baseName, Table table,
			LanguageFileFormat outputFormat, String keySeparator) throws IOException {
		for (ExportedLocale locale : table.getExportedLocales()) {
			generateLocalizedFile(inputBasename, outputDirectory, baseName, locale, table, outputFormat, keySeparator);
		}
	}

	/**
	 * Transforms an i18n.xml File into its properties-Files components.
	 * 
	 * @param is
	 * @throws Exception
	 */
	private static void transformXML(InputStream is, String inputBasename, File includePath, File outputDirectory,
			String outputBasename, LanguageFileFormat outputFormat, String keySeparator) throws Exception {
		Table table = readXmlFile(is, keySeparator);

		if (table.getOutput().size() == 0) {
			generateLocalizedFiles(inputBasename, outputDirectory, outputBasename, table, outputFormat, keySeparator);
		} else {
			for (Output output : table.getOutput()) {
				File customDirectory = output.getDirectory() == null ? outputDirectory : output.getDirectory();
				String customBasename = output.getBasename() == null ? outputBasename : output.getBasename();
				LanguageFileFormat customFormat = output.getFormat() == null ? outputFormat : output.getFormat();
				String customKeySeparator = output.getKeySeparator() == null ? keySeparator : output.getKeySeparator();

				generateLocalizedFiles(inputBasename, customDirectory, customBasename, table, customFormat,
						customKeySeparator);
			}
		}
	}

	/**
	 * rekonstruiert eine i18n.xml Datei
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static void reengineer(String inputBasename, String outputBasename, LanguageFileFormat outputFormat,
			File tableFile, File tableDirectory, String keySeparator)
			throws FileNotFoundException, IOException, ParserConfigurationException, TransformerException {
		Table table = new Table();

		File directory;

		if (tableFile != null) {
			directory = tableFile.getParentFile();
		} else {
			directory = tableDirectory;
		}

		if (inputBasename == null) {
			if (tableFile != null) {
				inputBasename = FilenameUtils.getBaseName(tableFile.getName());
			} else {
				inputBasename = outputBasename;
			}
		}

		if (outputBasename == null)
			outputBasename = inputBasename;

		if (inputBasename == null)
			inputBasename = "i18n";

		switch (outputFormat) {
		case JAVA_PROPERTIES:
		case JAVASCRIPT: {
			File[] files = directory.listFiles();
			Pattern pattern = Pattern.compile(outputBasename + "(_(\\w+))\\." + outputFormat.getExtension());

			for (File file : files) {
				Matcher matcher = pattern.matcher(file.getName());

				if (matcher.matches()) {
					String lang = matcher.group(2);
					if (StringUtils.isEmpty(lang))
						lang = "de"; // Standardsprache

					readLocalizedFile(file, table, lang, outputFormat);
				}
			}
		}
			break;
		case C_STRINGS: {
			File[] dirs = directory.listFiles();
			Pattern filePattern = Pattern.compile(outputBasename + "\\." + outputFormat.getExtension());
			Pattern dirPattern = Pattern.compile("(\\w+_?\\w*)\\.lproj");
			for (File dir : dirs) {
				if (dir.isDirectory()) {
					Matcher dirMatcher = dirPattern.matcher(dir.getName());
					if (dirMatcher.matches()) {
						for (File file : dir.listFiles()) {
							Matcher fileMatcher = filePattern.matcher(file.getName());
							if (fileMatcher.matches()) {
								String lang = dirMatcher.group(1);
								readLocalizedFile(file, table, lang, outputFormat);
							}
						}
					}
				}
			}
		}
		}

		writeTableToXml(table, new File(directory, inputBasename + ".xml"), keySeparator);
	}

	private static void readLocalizedFile(File file, Table table, String lang, LanguageFileFormat outputFormat)
			throws FileNotFoundException, IOException {
		ExportedLocale exported = new ExportedLocale();
		exported.setValue(lang);
		table.getExportedLocales().add(exported);

		Properties p = new Properties();

		switch (outputFormat) {
		case JAVA_PROPERTIES: {
			p.load(new FileInputStream(file));
			break;
		}
		case C_STRINGS: {
			Pattern pattern = Pattern
					.compile(" *\"?([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"? *= *\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\" *; *");
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), CharEncoding.UTF_16));
			try {
				String line = br.readLine();
				while (line != null) {
					Matcher m = pattern.matcher(line);
					if (m.matches()) {
						String key = m.group(1);
						String value = m.group(2).replace("\\\"", "\"");
						p.put(key, value);
					}
					line = br.readLine();
				}
			} finally {
				br.close();
			}
			break;
		}
		default:
			throw new IllegalArgumentException(outputFormat + " is not supported");
		}

		for (Map.Entry<Object, Object> entry : p.entrySet()) {
			String key = entry.getKey().toString();
			String value = entry.getValue().toString();

			Entry e = table.getEntries().get(key);
			if (e == null) {
				e = new Entry();
				table.getEntries().put(key, e);
			}

			e.getTextMap().put(lang, value);
		}
	}

	private static void writeTableToXml(Table table, File outputFile, String keySeparator)
			throws ParserConfigurationException, TransformerException {
		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element rootElement = doc.createElement("table");
		doc.appendChild(rootElement);

		// exported locales elements
		Element localeConfig = doc.createElement("locale-config");
		rootElement.appendChild(localeConfig);

		for (ExportedLocale exported : table.getExportedLocales()) {
			Element exLocale = doc.createElement("exported-locale");
			exLocale.setAttribute("value", exported.getValue());
			localeConfig.appendChild(exLocale);
		}

		// Mal ganz schnell die flache Liste in eine Hierarchie überführen.
		Map<String, EntryNode> nodes = createHierarchy(table, keySeparator);

		for (Map.Entry<String, EntryNode> node : nodes.entrySet()) {
			append(doc, rootElement, node.getKey(), node.getValue());
		}

		// write the content into xml file
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		t.setOutputProperty(OutputKeys.INDENT, "yes");
		t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
		DOMSource source = new DOMSource(doc);
		StreamResult result = new StreamResult(outputFile);

		t.transform(source, result);
	}

	/**
	 * Die "Table" Datenstruktur ist flach. Hiermit kann man eine Hierarchie
	 * erzeugen.
	 */
	private static Map<String, EntryNode> createHierarchy(Table table, String keySeparator) {
		// Mal ganz schnell die flache Liste in eine Hierarchie überführen.
		Map<String, EntryNode> nodes = new LinkedHashMap<String, EntryNode>();
		for (Map.Entry<String, Entry> entry : table.getEntries().entrySet()) {
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
					}
					node = nextLevel;
				}

				if (level == parts.length - 1) {
					// Werte reinschreiben
					node.getTextMap().putAll(e.getTextMap());
				}
			}
		}
		return nodes;
	}

	private static void append(Document doc, Element parent, String key, EntryNode value) {
		Element entry = doc.createElement("entry");
		parent.appendChild(entry);
		entry.setAttribute("key", key);

		for (Map.Entry<String, String> textEntry : value.getTextMap().entrySet()) {
			Element text = doc.createElement("text");
			entry.appendChild(text);
			text.setAttribute("locale", textEntry.getKey());
			text.setTextContent(textEntry.getValue());
		}

		for (Map.Entry<String, EntryNode> childNode : value.getNodes().entrySet()) {
			append(doc, entry, childNode.getKey(), childNode.getValue());
		}
	}
}