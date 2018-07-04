package de.pentabyte.tools.i18n.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
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

import de.pentabyte.maven.i18n.format.java.JavaAccessorCreator;
import de.pentabyte.maven.i18n.format.java.JavaPropertiesWriter;
import de.pentabyte.maven.i18n.format.javascript.JavascriptWriter;
import de.pentabyte.maven.i18n.format.strings.StringsWriter;
import de.pentabyte.maven.i18n.output.LanguageFileWriter;

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
			schema = sf.newSchema(new StreamSource(TableProducer.class.getResourceAsStream(Constants.schemaFileName)));
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
			LanguageFileFormat outputFormat, Log log, String keySeparator, File targetDir) {

		if (outputDirectory == null)
			outputDirectory = inputFile.getParentFile();
		try {
			log.info("processing: " + inputFile);
			transformXML(log, inputFile, FilenameUtils.getBaseName(inputFile.getName()), inputFile.getParentFile(),
					outputDirectory, outputBasename, outputFormat, keySeparator, targetDir);
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
			LanguageFileFormat outputFormat, Log log, String keySeparator, File targetDir) {
		if (!tableDirectory.exists())
			throw new RuntimeException(tableDirectory + " does not exist!");

		String inputFilename = inputBasename + ".xml";

		File[] files = tableDirectory.listFiles();
		for (File file : files) {
			if (file.getName().equalsIgnoreCase(inputFilename)) {
				transformFile(file, file.getParentFile(), outputBasename, outputFormat, log, keySeparator, targetDir);
			}
			if (file.isDirectory()) {
				transformRecursively(file, inputBasename, outputBasename, outputFormat, log, keySeparator, targetDir);
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
	private static Table readXmlFile(File tableFile, String keySeparator) throws Exception {
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
		Document doc = db.parse(tableFile);
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
					processOutput(tableFile.getParentFile(), e, table);
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
	private static void processOutput(File tableDirectory, Element element, Table table) {
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

		NodeList nodeList = element.getChildNodes();

		for (int i = 0; i < nodeList.getLength(); i++) {
			Node node = nodeList.item(i);
			if (node instanceof Element) {
				Element e = (Element) node;
				if (e.getTagName().equals("javaAccessor")) {
					output.setJavaAccessor(readJavaAccessor(tableDirectory, e));
				}
			}
		}

		table.getOutput().add(output);
	}

	/**
	 * @param e
	 * @return
	 */
	private static JavaAccessor readJavaAccessor(File tableDirectory, Element e) {
		JavaAccessor accessor = new JavaAccessor();

		String packageName = e.getAttribute("packageName");
		String className = e.getAttribute("className");

		if (StringUtils.isNotEmpty(packageName))
			accessor.setPackageName(packageName);
		if (StringUtils.isNotEmpty(className))
			accessor.setClassName(className);

		return accessor;
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
					String locale = e.getAttribute("locale");
					if (entry.getTextMap().containsKey(locale)) {
						throw new RuntimeException("Locale [" + locale + "] already exists for key [" + key + "]");
					}
					entry.getTextMap().put(locale, localized);
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
	private static void generateLocalizedFile(File tableDirectory, String inputBasename, ExportedLocale locale,
			Table table, Output output) throws IOException {

		String fallback = locale.getFallback() != null ? ", Fallback: " + locale.getFallback() : "";

		String fileComment = "AUTO-GENERATED from schema file [" + inputBasename
				+ "] with i18n-maven-plugin. Localization: " + locale.getValue() + fallback + ". "
				+ "Please do not edit this file manually!";

		LanguageFileWriter writer;

		switch (output.getFormat()) {
		case JAVA_PROPERTIES:
			writer = new JavaPropertiesWriter();
			break;
		case STRINGS:
			writer = new StringsWriter();
			break;
		case JAVASCRIPT: {
			if (output.getKeySeparator() != null && !output.getKeySeparator().equals(".")) {
				throw new RuntimeException("Für JAVASCRIPT ist nur der Punkt als Separator erlaubt!");
			}

			writer = new JavascriptWriter();
			break;
		}
		default:
			throw new RuntimeException("OutputFormat not implemented: " + output.getFormat());
		}

		writer.write(tableDirectory, inputBasename, output, locale, table, fileComment);
	}

	/**
	 * Generates individual properties files based on the processed XML file.
	 * 
	 * @param log
	 * 
	 * @param baseName
	 *            path to the generated properties files excluding locale
	 *            information and extension.
	 */
	public static void generateLocalizedFiles(Log log, File tableDirectory, String keySeparator, String inputBasename,
			Table table, Output output, File targetDir) throws IOException {

		for (ExportedLocale locale : table.getExportedLocales()) {
			generateLocalizedFile(tableDirectory, inputBasename, locale, table, output);
		}

		if (output.getJavaAccessor() != null) {
			JavaAccessor accessor = output.getJavaAccessor();
			JavaAccessorCreator creator = new JavaAccessorCreator(table, keySeparator, targetDir, output.getBasename());
			creator.write(log, accessor);
		}
	}

	/**
	 * Transforms an i18n.xml File into its properties-Files components.
	 * 
	 * @param log
	 * 
	 * @param is
	 * @throws Exception
	 */
	private static void transformXML(Log log, File inputFile, String inputBasename, File includePath,
			File outputDirectory, String outputBasename, LanguageFileFormat outputFormat, String keySeparator,
			File targetDir) throws Exception {
		Table table = readXmlFile(inputFile, keySeparator);

		Output pluginOutput = new Output();
		pluginOutput.setBasename(outputBasename);
		pluginOutput.setDirectory(outputDirectory);
		pluginOutput.setFormat(outputFormat);
		pluginOutput.setKeySeparator(keySeparator);

		if (table.getOutput().size() == 0) {
			generateLocalizedFiles(log, inputFile.getParentFile(), keySeparator, inputBasename, table, pluginOutput,
					targetDir);
		} else {
			for (Output output : table.getOutput()) {
				Output custom = new Output();
				custom.setBasename(output.getBasename() == null ? pluginOutput.getBasename() : output.getBasename());
				custom.setDirectory(
						output.getDirectory() == null ? pluginOutput.getDirectory() : output.getDirectory());
				custom.setFormat(output.getFormat() == null ? pluginOutput.getFormat() : output.getFormat());
				custom.setJavaAccessor(output.getJavaAccessor());

				generateLocalizedFiles(log, inputFile.getParentFile(), keySeparator, inputBasename, table, custom,
						targetDir);
			}
		}
	}

	/**
	 * rekonstruiert eine i18n.xml Datei
	 * 
	 * @param log
	 * 
	 * @throws IOException
	 * @throws FileNotFoundException
	 * @throws ParserConfigurationException
	 * @throws TransformerException
	 */
	public static void reengineer(Log log, String inputBasename, String outputBasename, LanguageFileFormat outputFormat,
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

					readLocalizedFile(log, file, table, lang, outputFormat);
				}
			}
		}
			break;
		case STRINGS: {
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
								readLocalizedFile(log, file, table, lang, outputFormat);
							}
						}
					}
				}
			}
			break;
		}
		default:
			throw new RuntimeException("OutputFormat not implemented: " + outputFormat);
		}

		writeTableToXml(log, table, new File(directory, inputBasename + ".xml"), keySeparator, outputFormat,
				outputBasename);
	}

	private static void readLocalizedFile(Log log, File file, Table table, String lang, LanguageFileFormat outputFormat)
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
		case STRINGS: {
			Pattern pattern = Pattern.compile(
					" *\"?([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\"? *= *\"([^\"\\\\]*(?:\\\\.[^\"\\\\]*)*)\" *; *(?://.*)?");
			BufferedReader br = new BufferedReader(
					new InputStreamReader(new FileInputStream(file), CharEncoding.UTF_8));
			try {
				String line = br.readLine();
				while (line != null) {
					Matcher m = pattern.matcher(line);
					if (m.matches()) {
						String key = m.group(1).trim();
						String value = m.group(2).replace("\\t", "	").replace("\\n", "\n").replace("\\\"", "\"")
								.trim();
						p.put(key, value);
					} else if (StringUtils.isNotEmpty(line)) {
						log.warn("Ignored line: " + line);
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

	private static void writeTableToXml(Log log, Table table, File outputFile, String keySeparator,
			LanguageFileFormat format, String outputBasename)
			throws ParserConfigurationException, TransformerException {
		log.info("Now creating language table: " + outputFile);

		DocumentBuilderFactory docFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder docBuilder = docFactory.newDocumentBuilder();

		// root elements
		Document doc = docBuilder.newDocument();
		Element root = doc.createElement("table");
		root.setAttribute("xmlns", "http://pentabyte.de/maven/i18n");
		root.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
		root.setAttribute("xsi:schemaLocation",
				"http://pentabyte.de/maven/i18n http://pentabyte.de/maven/i18n/" + Constants.schemaFileName);

		doc.appendChild(root);

		// exported locales elements
		Element localeConfig = doc.createElement("locale-config");
		root.appendChild(localeConfig);

		for (ExportedLocale exported : table.getExportedLocales()) {
			Element exLocale = doc.createElement("exported-locale");
			exLocale.setAttribute("value", exported.getValue());
			localeConfig.appendChild(exLocale);
		}

		// Output Format
		Element output = doc.createElement("output");
		root.appendChild(output);
		output.setAttribute("format", format.name());
		output.setAttribute("basename", outputBasename);

		// Mal ganz schnell die flache Liste in eine Hierarchie überführen.
		Map<String, EntryNode> nodes = table.createHierarchy(keySeparator);

		for (Map.Entry<String, EntryNode> node : nodes.entrySet()) {
			append(doc, root, node.getKey(), node.getValue());
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