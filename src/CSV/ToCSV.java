package CSV;

import java.io.*;
import java.nio.charset.StandardCharsets;

/** Store tabulated data in a CSV file. */
public class ToCSV
{
	private final char _deli;
	private final String[] _headers;
	private final java.lang.StringBuilder _CSVcontent;
	private boolean _headerWritten;
	private int _lines = 0;

	/**
	 * Class constructor
	 * @param headers
	 * @param deli
	 */
	public ToCSV(String[] headers, char deli)
	{
		_headers = headers;
		_deli = deli;
		_CSVcontent = new java.lang.StringBuilder();
	}

	/**
	 * Class constructor
	 * @param headers
	 */
	public ToCSV(String[] headers)
	{
		this(headers, '|');
	}

	/**
	 * Class constructor
	 */
	protected ToCSV()
	{
		this(null, '|');
		_headerWritten = false;
	}

	/**
	 * Gets the number of lines that have been written  to buffer via {@value WriteLine(String[] csvLine)} method.
	 * @return
	 */
	public int getLines() { return _lines; }

	/**
	 * Writes the current internal buffer to a file stored in the file system.
	 * @param filename
	 * @throws IOException
	 */
	public void WriteFile(String filename) throws IOException
	{
		BufferedWriter writer = null;
		try
		{
			// Enforce UTF-8 (Unicode) as output format to avoid conversion problems to ANSI (should this be a system's default)
			writer = new BufferedWriter
    		(new OutputStreamWriter(new FileOutputStream(filename), StandardCharsets.UTF_8));

			writer.write(_CSVcontent.toString());
		}
		finally
		{
			if (writer != null)
			{
				writer.close();
				writer = null;
			}
		}
	}

	/**
	 * Writes one line of text to the internal text buffer.
	 * @param csvLine
	 */
	public void WriteLine(String[] csvLine)
	{
		if (_headerWritten == false)
		{
			CSVHeader(_headers, _CSVcontent);
			_headerWritten = true;
		}

		_CSVcontent.append(csvLine[0]);

		for (int i = 1; i < csvLine.length; i++)
			_CSVcontent.append(_deli + csvLine[i]);

		_CSVcontent.append('\n');
		_lines++;
	}

	/**
	 * Appends the header entry to the string builder buffer
	 * @param headers
	 * @param sb
	 */
	protected void CSVHeader(String[] headers, StringBuilder sb)
	{
		if (headers != null)
		{
			if (headers.length > 0)
			{
				sb.append(headers[0]);
				
				for (int i = 1; i < headers.length; i++)
					sb.append(_deli + headers[i]);

				sb.append('\n');
			}
		}
	}
}
