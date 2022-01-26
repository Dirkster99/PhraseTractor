
package ProgramObjects;

import java.io.*;

/// <summary>
/// Simple file wrapper with basic properties to support common tasks
/// like get the name of the containing directory etc...
/// </summary>
public class FileImpl
{
	//region fields
	final String _FileInPathName;
	String _CsvSeparator;
	String[] _CsvColumns;

	private int _TextColumnIdx;
	private String _TextColumn;
	private int _LabelColumnIdx;
	private String _LabelColumn;
	private String _LanguageColumn;
	private int _LanguageColumnIdx = -1;
	//endregion fields

	//region ctors
	public FileImpl(String fileIn)
	{
		_FileInPathName = fileIn;
	}

	protected FileImpl()
	{
		this(null);
	}
	//endregion

	//region properties
	public String getFileInNamePath() { return _FileInPathName; }

	public String getFileInDirectory()
	{
		java.io.File f = new File(_FileInPathName);
  		return f.getPath() + java.io.File.separator;
	}

	public String getFileInName()
	{
		java.io.File f = new File(_FileInPathName);
  		return f.getName();
	}

	public String getFileInNameWithoutExtension()
	{
		String fileName = this.getFileInName();
		
		if (fileName.indexOf(".") > 0)
		{
		   return fileName.substring(0, fileName.lastIndexOf("."));
		}
		else
		{
		   return fileName;
		}		
	}

	public String getFileInExtension()
	{
		String fileName = this.getFileInName();
		
		int i = fileName.lastIndexOf('.');
		if (i > 0)
			return fileName.substring(i+1);

		return "";
	}

	public String getCsvSeparator() { return _CsvSeparator; }

	public String[] CsvColumns() { return _CsvColumns; }

	public String ColumnNameLng() { return _LanguageColumn; }

	public int CsvLngIndex() { return _LanguageColumnIdx; }

	public String ColumnNameText() { return _TextColumn; }

	public int CsvTextIndex() { return _TextColumnIdx; }

	public String ColumnNameLabel() { return _LabelColumn; }

	public int CsvLabelIndex() { return _LabelColumnIdx; }
	//endregion properties

	public String[] GetCSVColumns()
		throws java.io.FileNotFoundException, java.io.IOException
	{
		return GetCSVColumns("|");
	}
	/// <summary>
	/// Reads the first line of a text file to retrieve its array of columns
	/// </summary>
	/// <param name="csvSeparator"></param>
	/// <returns></returns>
	public String[] GetCSVColumns(String csvSeparator)
		throws FileNotFoundException, IOException
	{
		_CsvSeparator = csvSeparator;

		if (_CsvColumns != null)
			return _CsvColumns;

		BufferedReader br = null;
		String line1 = null;
		try
		{
			//String line1 = File.ReadLines(_FileInPathName).First();
			br = new BufferedReader(new FileReader(_FileInPathName));
			line1 = br.readLine();
		}
		finally
		{
			if (br != null)
				br.close();
		}

		if (line1 != null)
			_CsvColumns = line1.split(csvSeparator);

		return _CsvColumns;
	}
	
	public boolean SetColumns(String textColumn, String labelColumn)
		throws java.io.FileNotFoundException, java.io.IOException
	{
		return SetColumns(textColumn, labelColumn, null);
	}

	public boolean SetColumns(String textColumn
				 , String labelColumn
				 , String languageColumn)
		throws java.io.FileNotFoundException, java.io.IOException
	{
		if (_CsvColumns == null)
			_CsvColumns = GetCSVColumns();

		int iTextColumn = -1, iLabelColumn = -1, iLanguageColumn = -1;
		for (int idx = 0; idx < _CsvColumns.length; idx++)
		{
			if (textColumn == _CsvColumns[idx])
				iTextColumn = idx;

			if (labelColumn == _CsvColumns[idx])
				iLabelColumn = idx;

			if (languageColumn != null)
			{
				if (languageColumn == _CsvColumns[idx])
					iLanguageColumn = idx;
			}
		}

		if (iTextColumn >= 0 && iLabelColumn >= 0)
		{
			_TextColumnIdx = iTextColumn;
			_TextColumn = textColumn;

			_LabelColumnIdx = iLabelColumn;
			_LabelColumn = labelColumn;

			_LanguageColumn = languageColumn;
			_LanguageColumnIdx = iLanguageColumn;

			return true;
		}

		return false;
	}
}
