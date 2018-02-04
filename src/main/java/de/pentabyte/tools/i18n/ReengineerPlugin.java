package de.pentabyte.tools.i18n;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import de.pentabyte.tools.i18n.core.LanguageFileFormat;
import de.pentabyte.tools.i18n.core.TableProducer;

/**
 * Goal which constructs i18n.xml from messages files.
 * 
 * @author Michael Höreth
 * 
 * @goal reengineer
 */
public class ReengineerPlugin extends AbstractMojo {
	/**
	 * Path of i18n-Table.
	 * 
	 * @parameter property="tableFile"
	 */
	File tableFile;

	/**
	 * Recursivly check for all "i18n.xml"-Tables within that directory.
	 * 
	 * @parameter property="tableDirectory" default-value="${basedir}"
	 */
	File tableDirectory;

	/**
	 * Name of Properties Files. (optional)
	 * 
	 * @parameter property="outputBasename"
	 * 
	 */
	String outputBasename;

	/**
	 * Name of XML-File. (optional)
	 * 
	 * @parameter property="inputBasename"
	 */
	private String inputBasename;

	/**
	 * @parameter property="outputFormat" default-value="JAVA_PROPERTIES"
	 */
	LanguageFileFormat outputFormat;

	/**
	 * Die Keys verschachtelter Entries werden so in der Sprachdatei voneinander
	 * abgetrennt.
	 * 
	 * @parameter property="keySeparator" default-value="."
	 */
	String keySeparator;

	public void execute() throws MojoExecutionException {
		if (outputFormat == LanguageFileFormat.JAVASCRIPT)
			throw new MojoExecutionException("At the moment JAVASCRIPT Files cannot be reengineered.");

		getLog().info("Run-Configuration: (tableDirectory=" + tableDirectory + ", tableFile=" + tableFile
				+ ", outputBasename=" + outputBasename + ", inputBasename=" + inputBasename + ", outputFormat="
				+ outputFormat + ", keySeparator=" + keySeparator + ")");

		try {
			TableProducer.reengineer(getLog(), inputBasename, outputBasename, outputFormat, tableFile, tableDirectory,
					keySeparator);
		} catch (Exception e) {
			throw new MojoExecutionException("FEHLER", e);
		}
	}

	public static void main(String[] args) throws MojoExecutionException {
		ReengineerPlugin plugin = new ReengineerPlugin();
		plugin.tableFile = new File("/Users/hoereth/Documents/iOS/Maengelmelder/submodule/ChamaeleonLib/Main");
		plugin.tableDirectory = new File("/Users/hoereth/Documents/iOS/Maengelmelder");
		plugin.outputFormat = LanguageFileFormat.STRINGS;
		plugin.keySeparator = ".";
		plugin.execute();
	}

}
