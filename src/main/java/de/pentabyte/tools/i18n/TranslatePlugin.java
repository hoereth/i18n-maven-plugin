package de.pentabyte.tools.i18n;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;

import de.pentabyte.tools.i18n.core.LanguageFileFormat;
import de.pentabyte.tools.i18n.core.TableProducer;

/**
 * Goal which produces language files (or Localizable.string, or JAVASCRIPT)
 * files from i18n.xml Tables
 * 
 * @author Michael Höreth
 * 
 * @goal translate
 * @phase generate-resources
 */
public class TranslatePlugin extends AbstractMojo {
	/**
	 * Path of i18n-Table. (for when you only have one)
	 * 
	 * @parameter property="tableFile"
	 */
	private File tableFile;

	/**
	 * Recursivly check for all "i18n.xml"-Tables within that directory.
	 * 
	 * @parameter property="tableDirectory" default-value="${basedir}"
	 */
	private File tableDirectory;

	/**
	 * Language Files should be written here. Defaults to directory of
	 * respective table file.
	 * 
	 * @parameter property="outputDirectory"
	 * 
	 */
	private File outputDirectory;

	/**
	 * Base name of XML-Table-File.
	 * 
	 * @parameter property="inputBasename" default-value="i18n"
	 */
	private String inputBasename;

	/**
	 * Base name of Language Files.
	 * 
	 * @parameter property="outputBasename"
	 * 
	 */
	private String outputBasename;

	/**
	 * @parameter property="outputFormat" default-value="JAVA_PROPERTIES"
	 */
	private LanguageFileFormat outputFormat;

	/**
	 * Keys of nested entries will be separated with this value.
	 * 
	 * @parameter property="keySeparator" default-value="."
	 */
	private String keySeparator;

	/**
	 * Base directory of generated source code files. Use case: accessor files.
	 * 
	 * @parameter property="targetDir" default-value="${basedir}/src/main/java"
	 */
	private File targetDir;

	public void execute() throws MojoExecutionException {
		getLog().info("Run-Configuration: (tableFile=" + tableFile + ", tableDirectory=" + tableDirectory
				+ ", inputBasename=" + inputBasename + ", outputDirectory=" + outputDirectory + ", outputBasename="
				+ outputBasename + " (default: " + inputBasename + ")" + ", outputFormat=" + outputFormat
				+ ", keySeparator=" + keySeparator + ", targetDir=" + targetDir + ")");

		if (tableFile != null) {
			try {
				TableProducer.transformFile(tableFile, outputDirectory, outputBasename, outputFormat, getLog(),
						keySeparator, targetDir);
			} catch (Exception e) {
				throw new MojoExecutionException("Error translating tableFile [" + tableFile + "]", e);
			}
			if (tableDirectory != null) {
				getLog().warn("Skipping tableDirectory, because tableFile was set");
			}
		} else {
			if (tableDirectory != null) {
				try {
					TableProducer.transformRecursively(tableDirectory, inputBasename, outputBasename, outputFormat,
							getLog(), keySeparator, targetDir);
				} catch (Exception e) {
					throw new MojoExecutionException("Error translating tableDirectory [" + tableDirectory + "]", e);
				}
			}
		}
	}
}
