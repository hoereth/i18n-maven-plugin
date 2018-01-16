/**
 * 
 */
package de.pentabyte.maven.i18n.output;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import de.pentabyte.tools.i18n.core.ExportedLocale;
import de.pentabyte.tools.i18n.core.Output;
import de.pentabyte.tools.i18n.core.Table;

/**
 * Is able to write the contents of {@link Table} to the file system. File
 * format depends on the implementation.
 * 
 * @author Michael Höreth
 */
public interface LanguageFileWriter {

	/**
	 * @param outputDirectory
	 * @param basename
	 * @param locale
	 * @param table
	 * @param fileComment
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	public void write(File tableDirectory, String inputBasename, Output output, ExportedLocale locale, Table table,
			String fileComment) throws FileNotFoundException, IOException;

}
