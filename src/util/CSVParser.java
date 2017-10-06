package util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;

public class CSVParser {

	private final String resourcePath;
	private final String resourceEncoding;
	
	public CSVParser(final String resourcePath, final String resourceEncoding) {
		super();
		this.resourcePath = resourcePath;
		this.resourceEncoding = resourceEncoding;
	}

	public void parse(
			final String[] lineTokens, 
			final IConsumer<String[]> lineConsumer
			) throws IOException {
		this.parse(lineTokens, '"', ',', lineConsumer);
	}

	public void parse(
			final String[] lineTokens, 
			final char quoteChar, 
			final char tokenDelimiterChar,
			final IConsumer<String[]> lineConsumer
			) throws IOException {
		try (final InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(this.resourcePath);
			 final InputStreamReader reader = new InputStreamReader(is, this.resourceEncoding);) {

			while (reader.ready()) {
				final StringWriter sw = new StringWriter();
				if (lineTokens.length == this.parseLine(reader, sw, lineTokens, quoteChar, tokenDelimiterChar)) {
					lineConsumer.accept(lineTokens);
				}
			}
		}
	}

	private int parseLine(
			final InputStreamReader reader, 
			final StringWriter sw, 
			final String[] fields, 
			final char quoteChar, 
			final char fieldDelimiter
			) throws IOException {
		int fieldIndex = 0;
		boolean isQuoted = false;
		boolean preceededByQuote = false;
		int currentChar = -1;
		sw.getBuffer().setLength(0);
		while ((currentChar = reader.read()) >= 0 && currentChar != 13 && currentChar != 10) {
			if (fieldIndex < fields.length) {
				if (currentChar == quoteChar) {
					isQuoted = !isQuoted;
					if (isQuoted) {
						if (preceededByQuote) {
							sw.write(currentChar);
							preceededByQuote = false;
						}
					} else {
						preceededByQuote = true;
					}
					continue;
				} else {
					preceededByQuote = false;
				}
				if (!isQuoted && currentChar == fieldDelimiter) {
					fields[fieldIndex] = sw.toString();
					sw.getBuffer().setLength(0);
					fieldIndex++;
					continue;
				}
				sw.write(currentChar);
			}
		}
		if (fieldIndex < fields.length) {
			fields[fieldIndex] = sw.toString();
			fieldIndex++;
		}
		return fieldIndex;
	}
}
