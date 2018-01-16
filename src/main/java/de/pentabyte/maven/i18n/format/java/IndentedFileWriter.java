/**
 * 
 */
package de.pentabyte.maven.i18n.format.java;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Michael Höreth
 */
public class IndentedFileWriter {
	FileWriter writer;
	int indentation = 0;

	public IndentedFileWriter(File file) throws IOException {
		writer = new FileWriter(file);
	}

	public void writeLine(String line) throws IOException {
		for (int i = 0; i < indentation; i++) {
			writer.write("\t");
		}
		writer.write(line + "\n");
	}

	public void incrementIndentation() {
		indentation++;
	}

	public void decrementIndentation() {
		if (indentation == 0)
			throw new RuntimeException("not possible");
		indentation--;
	}

	public void close() throws IOException {
		writer.close();
	}

	public void emptyLine() throws IOException {
		writer.write("\n");
	}

	public static class CommentWriter {
		final IndentedFileWriter fileWriter;

		public CommentWriter(IndentedFileWriter fileWriter) throws IOException {
			this.fileWriter = fileWriter;
			fileWriter.writeLine("/**");
		}

		public void writeLine(String line) throws IOException {
			fileWriter.writeLine(" * " + line);
		}

		public void writeLines(String[] lines) throws IOException {
			for (String line : lines)
				writeLine(line);
		}

		public void close() throws IOException {
			fileWriter.writeLine(" */");
		}
	}

	public CommentWriter createCommentWriter() throws IOException {
		return new CommentWriter(this);
	}

	public static class CurlyBracketWriter {
		final IndentedFileWriter fileWriter;

		public CurlyBracketWriter(IndentedFileWriter fileWriter, String firstLine) throws IOException {
			this.fileWriter = fileWriter;
			fileWriter.writeLine(firstLine + " {");
			fileWriter.incrementIndentation();
		}

		public void writeLine(String line) throws IOException {
			fileWriter.writeLine(line);
		}

		public void close() throws IOException {
			fileWriter.decrementIndentation();
			fileWriter.writeLine("}");
		}
	}

	public CurlyBracketWriter createCurlyBracketWriter(String comment, String firstLine) throws IOException {
		this.emptyLine();
		writeComment(comment);
		return new CurlyBracketWriter(this, firstLine);
	}

	/**
	 * @param indentedFileWriter
	 * @param comment
	 * @throws IOException
	 */
	public void writeComment(String comment) throws IOException {
		if (StringUtils.isNotEmpty(comment)) {
			CommentWriter writer = createCommentWriter();
			writer.writeLines(comment.split("\n"));
			writer.close();
		}

	}
}
