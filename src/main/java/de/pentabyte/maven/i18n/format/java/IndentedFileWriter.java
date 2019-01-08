package de.pentabyte.maven.i18n.format.java;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Handles writing text files and keeps track of indentation.
 * 
 * @author Michael Höreth
 */
public class IndentedFileWriter {
	Writer writer;
	int indentation = 0;

	public IndentedFileWriter(File file) throws IOException {
		writer = new OutputStreamWriter(new FileOutputStream(file), StandardCharsets.UTF_8);
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

	/**
	 * Allows you to write comments as plain text. Takes care of all slashes and
	 * asterisks of a typical block comment for you.
	 */
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

	/**
	 * Allows to write text, but takes care of opening and closing curly
	 * brackets around your text.
	 */
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
	 * Convenience method to write just one comment.
	 * 
	 * @param indentedFileWriter
	 * @param comment
	 * @throws IOException
	 */
	public void writeComment(String comment) throws IOException {
		if (comment != null && comment.length() > 0) {
			CommentWriter writer = createCommentWriter();
			writer.writeLines(comment.split("\n"));
			writer.close();
		}

	}
}
