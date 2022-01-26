package ProgramObjects;

import java.util.*;

import ProgramObjects.Errors.ErrorObject;
import ProgramObjects.Errors.ErrorSeverity;

import java.io.IOException;
import java.nio.file.*;

/** Manage reading and basic processing of program parameters */
public class ProgramArgs
{
	//region fields
	private final String _TextFileName;
	private final FileImpl _TextFile;
	private final String _RowIdColumn;
	private final String _TextColumn;
	private final String _KeyFileName;
	private final FileImpl _KeyFile;
	private final String _BaseOutputDir;
	
	private final boolean _WordPairFequencies;
	private final boolean _GenerateRowId;

	private String[] _CsvColumns;
	private final HashMap<String, Integer> _MapColumnName2Index;
	private boolean _ExtractKeys;
	private String _RegexMask;

	private ErrorObject _Err = null;

	//endregion fields

	//region ctors
	/**
	 * Class constructor from parameters.
	 * See {@value getError()} to check if/when required program parameters are missing. 
	 */
	public ProgramArgs(
		String textFileName,
		String rowIdColumn,
		String textColumn,
		String keyFileName,
		String baseOutputDir,
		boolean wordPairFequencies,
		String regexMask
		)
	{
		this._TextFileName = textFileName;
		this._RowIdColumn = rowIdColumn;
		this._TextColumn = textColumn;

		this._KeyFileName = keyFileName;

		this._BaseOutputDir = baseOutputDir;
		
		this._WordPairFequencies = wordPairFequencies;
		this._RegexMask = regexMask; // "[^a-zA-Z0-9-äüöÄÜÖß_#!]+"; // ;

		this._ExtractKeys = (keyFileName == null || keyFileName.trim().length() == 0 ? false : true);;

		this._GenerateRowId = (rowIdColumn == null || rowIdColumn.trim().length() == 0 ? true : false);
		this._MapColumnName2Index = new java.util.HashMap<String, Integer>();

		_Err = this.Check4Errors();  // Detected an error and do not continue processing if required params are missing
		if (_Err != null)
		{
			this._TextFile = null;
			this._KeyFile = null;
			return;
		}

		this._TextFile = new FileImpl(textFileName);
		this._KeyFile = new FileImpl(keyFileName);
	}

	/**
	 * Class constructor from parameters.
	 * See {@value getError()} to check if/when required program parameters are missing. 
	 */
	public ProgramArgs(
		String textFileName,
		String rowIdColumn,
		String textColumn,
		String keyFileName,
		String baseOutputDir)
	{
		this(textFileName, rowIdColumn, textColumn, keyFileName,baseOutputDir, false, "[^a-zA-Z0-9-äüöÄÜÖß_#!]+");
	}

	/** Class constructor */
	protected ProgramArgs()
	{
		this(null, null, null, null,null, false, "[^a-zA-Z0-9-äüöÄÜÖß_#!]+");
	}
	//endregion ctors

	//region props
	/**
	 * Gets the name of the column that contains the row id (if any since row id can also be generated on the fly)
	 * @return
	 */
	public String getRowIdColumn() { return _RowIdColumn; }

	/**
	 * Gets whether row id should be generated or not.
	 * @return
	 */
	public boolean getGenerateRowId() { return _GenerateRowId; }

	/**
	 * Gets the name of the column that contains the text to be searched and evaluated
	 * @return
	 */
	public String getTextColumn() { return _TextColumn; }

	/**
	 * Gets names of all columns (if available) or null
	 * @return
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public String[] getColumnNames()
		throws java.io.FileNotFoundException, java.io.IOException
	{
		return GetCSVColumns("|");
	}

	/**
	 * Gets the name of the CSV file that contains the text to be searched.
	 * @return
	 */
	public String getTextFileName() { return _TextFileName; }

	/**
	 * Gets the text file instance of the CSV file that contains the text to be searched.
	 * @return
	 */
	public FileImpl getTextFile() { return _TextFile; }

	/**
	 * Gets the name of the key CSV file that contains the keywords that
	 * are used to search relevant text in the text CSV file.
	 * @return
	 */
	public String getKeyFileName() { return _KeyFileName; }

	/**
	 * Gets the file instance the key CSV file that contains the keywords that
	 * are used to search relevant text in the text CSV file.
	 * @return
	 */
	public FileImpl getKeyFile() { return _KeyFile; }

	/**
	 * Gets the path to the output directory that should be used to output result files.
	 * @return
	 */
	public String getBaseOutputDir() { return _BaseOutputDir; }

	/**
	 * Gets whether to output a file with frequencies of word-pairs or not.
	 * @return
	 */
	public boolean getWordPairFequencies() { return _WordPairFequencies; }

	/**
	 * Whether to extract keys from text file or not.
	 * Tool will not read a key file and process no keys for parsing text to output matching strings.
	 * @return
	 */
	public boolean getExtractKeys() { return _ExtractKeys; }

	/**
	 * Gets the regular expression mask that is used to decide on the characters
	 * that are relevant to form tokens (words) from the text input.
	 * @return
	 */
	public String getRegexMask() { return _RegexMask; }

	/**
	 * Get an error object (if any) to describe an error in the context of (missing) required program parameters.
	 * @return
	 */
	public ErrorObject getError() { return _Err; }

	//endregion props

	//region methods
	/**
	 * Read program argument parameters from a Java properties file and return settings as Java object.
	 * @param filePath
	 * @return
	 * @throws Exception
	 */
	public static ProgramArgs readConfigFromPropertiesFile(String[] args) throws Exception
	{
		// Parse command line options and read config file if available
        String path2ConfigFile = null;
        for (int i=0; i<args.length; i++)
        {
            switch(args[i])
            {
                case "-config":
                    if (args.length > i+1)
                    {
                        path2ConfigFile = args[i+1];
                        i++;
                    }
                break;

                default:
                    System.out.printf("ERROR: Unrecognized command line option '%s'\n", args[i]);
                    return null;
            }
        }

		System.out.printf("  Properties from: '%s'\n", path2ConfigFile);
		HashMap<String, String> prop = Config.readConfig(path2ConfigFile);

        String TextFile = prop.get("TextFile");
        String RowIDColumnName = prop.get("RowIDColumnName");
        String TextColumnName = prop.get("TextColumnName");
        String KeyFile = prop.get("KeyFile");
        String OutputDir = prop.get("OutputDir");
		boolean WordPairFrequency = Boolean.parseBoolean((IsEmpty(prop.get("WordPairFrequency")) ? "false" : prop.get("WordPairFrequency")));
        String RegexMask = prop.get("RegexMask");

		// Transform relative paths into absolute paths based on path given for config file
		if (TextFile.startsWith(".") == true)
			TextFile = convertRelative2AbsolutPath(path2ConfigFile, TextFile);

		if (KeyFile.startsWith(".") == true)
			KeyFile = convertRelative2AbsolutPath(path2ConfigFile, KeyFile);

		if (OutputDir.startsWith(".") == true)
			OutputDir = convertRelative2AbsolutPath(path2ConfigFile, OutputDir);

        System.out.printf("         TextFile: '%s'\n", TextFile);
        System.out.printf("  RowIDColumnName: '%s'\n", RowIDColumnName);
        System.out.printf("   TextColumnName: '%s'\n", TextColumnName);
        System.out.printf("          KeyFile: '%s'\n", KeyFile);
		System.out.printf("        OutputDir: '%s'\n", OutputDir);
        System.out.printf("WordPairFrequency: '%b'\n", WordPairFrequency);
        System.out.printf("        RegexMask: '%s'\n", RegexMask);

		ProgramArgs pargs = new ProgramArgs(TextFile, RowIDColumnName, TextColumnName, KeyFile, OutputDir, WordPairFrequency, RegexMask);

		return pargs;
	}

	/**
	 * Gets an array of Strings to describe with names of all CSV columns.
	 * @return
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public String[] GetCSVColumns() throws java.io.FileNotFoundException, java.io.IOException
	{
		return GetCSVColumns("|");
	}

	/**
	 * Reads the first line of a text file to retrieve its array of columns.
	 * @param csvSeparator
	 * @return
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public String[] GetCSVColumns(String csvSeparator)
		throws java.io.FileNotFoundException, java.io.IOException
	{
		_TextFile.GetCSVColumns(csvSeparator);

		return _TextFile.CsvColumns();
	}

	/**
	 * Determines the integer column index [0-n] for a column named {@param columnName} in a CSV file.
	 * 
	 * @param columnName
	 * @return
	 * @throws IllegalArgumentException
	 * @throws java.io.FileNotFoundException
	 * @throws java.io.IOException
	 */
	public int GetColumnIndex(String columnName)
		throws IllegalArgumentException, java.io.FileNotFoundException, java.io.IOException
	{
		if (_MapColumnName2Index.size() == 0)
			BuildColumnName2Index();

		if (_MapColumnName2Index.containsKey(columnName))
			return _MapColumnName2Index.get(columnName);

		throw new IllegalArgumentException(columnName + " -> NOT Found.");
	}

	/**
	 * Determines if a (trimmed) string is empty or null and return True if string
	 * holds no other content than whitespaces or, otherwise, False.
	 * @param s
	 * @return
	 */
	public static boolean IsEmpty(String s)
	{
		if (s == null)
			return true;
		
		if (s.trim().length() == 0)
			return true;
		
		return false;
	}

	/**
	 * Converts a relative path to a directory or file (against an absolute path)
	 * into an absolut path and returns the resulting absolute path to a directory or file.
	 * @param absolutPath2File
	 * @param relativeDirOrFilePath
	 * @return
	 * @throws IOException
	 */
	public static String convertRelative2AbsolutPath(String absolutPath2File
	                                                , String relativeDirOrFilePath) throws IOException
	{
		// create an object of Path
		java.io.File file = new java.io.File(absolutPath2File);
		Path path = null;

		if (file.exists())
			path = Paths.get(absolutPath2File).getParent();
		else
			path = Paths.get(absolutPath2File);

		Path resolvedPath = path.resolve(relativeDirOrFilePath);
		relativeDirOrFilePath = resolvedPath.toRealPath().toString();

		file = new java.io.File(relativeDirOrFilePath);

		// Normalize directory references to always end with a (back)slash
		if (file.isDirectory() == true && relativeDirOrFilePath.endsWith(java.io.File.separator) == false)
			relativeDirOrFilePath += java.io.File.separator;

		return relativeDirOrFilePath;
	}

	/**
	 * Builds 2 internal collections (if not present yet) to:
	 * - hold all column names of a CSV file via {@value _CsvColumns} and
	 * - associates each column name with its index in the CSV file via {@value _MapColumnName2Index}
	 */
	private void BuildColumnName2Index() throws java.io.FileNotFoundException, java.io.IOException
	{
		if (_CsvColumns == null)
			_CsvColumns = GetCSVColumns();

		_MapColumnName2Index.clear();

		int idx = 0;
		for (String item : _CsvColumns)
			_MapColumnName2Index.put(item, idx++);
	}

	/**
	 * Check if required program options are present 
	 * @return
	 */
	private ErrorObject Check4Errors()
	{
		if (IsEmpty(this._TextFileName))
			return new ErrorObject("TextFile property in program config file cannot be empty.", ErrorSeverity.ERROR);

		if (IsEmpty(this._TextColumn))
			return new ErrorObject("TextColumnName property in program config file cannot be empty.", ErrorSeverity.ERROR);

		if (IsEmpty(this._KeyFileName))
			return new ErrorObject("KeyFile property in program config file cannot be empty.", ErrorSeverity.ERROR);

		if (IsEmpty(this._BaseOutputDir))
			return new ErrorObject("OutputDir property in program config file cannot be empty.", ErrorSeverity.ERROR);

		if (IsEmpty(this._BaseOutputDir))
			return new ErrorObject("OutputDir property in program config file cannot be empty.", ErrorSeverity.ERROR);

		//this._WordPairFequencies = wordPairFequencies;
		if (IsEmpty(this._RegexMask))
			return new ErrorObject("RegexMask property in program config file cannot be empty.", ErrorSeverity.ERROR);

		return null;
	}
	//endregion methods
}
