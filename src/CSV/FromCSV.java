package CSV;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;

import ProgramObjects.Errors.ErrorObject;
import ProgramObjects.Errors.ErrorSeverity;

/** Read text data formated as CSV file from the file system. */
public class FromCSV
{    
    private final String _Filename;
    private final char _Separator;
    private final boolean _Header;

    private BufferedReader _bufreader = null;
    private int _lineNumber = 0;
    private String[] _HeaderRow;

    /**
     * Class constructor from parameters.
     * @param fileName
     * @param separator
     * @param header
     */
    public FromCSV(String fileName, char separator, boolean header)
    {
        _Filename = fileName;
        _Separator = separator;
        _Header = header;
    }

    /** Class destructor */
    protected void finalize()
    {
        try
        {
            this.Close();
        }
        catch(Exception e){}
    }

    /** Gets the currently parsed line number in the CSV file which can be used for parser error descriptions. */
    public int getLineNumber(){ return _lineNumber; }

    /**
     * Opens the access to a file or returns an Error object if an error was registered.
     * Otherwise, null is returned to indicated that everything went OK.
     * @return
     * @throws Exception
     */
    public ErrorObject OpenFile() throws Exception
    {
        try  // Sanity check for existance of file to be processed
        {
            File file = new File(_Filename);
            if(file.exists() == false)
                return (new ErrorObject(String.format("ERROR file '%s' does not exist in file system or cannot be accessed.", _Filename)
                                                    , ErrorSeverity.ERROR));
        }
        catch(Exception e){ return (new ErrorObject(e)); }

        try {
                this.Close();
                
                if (_bufreader == null) // create a reader
                {
                    _bufreader = Files.newBufferedReader(Paths.get(_Filename), StandardCharsets.UTF_8);
                    _HeaderRow = null;

                    if (_Header) // Read the header and skip it for output since users usually want the data rows :-)
                    {
                        String line;
                        if ((line = _bufreader.readLine()) != null)
                        {
                            _HeaderRow = line.split(String.valueOf((_Separator == '|' ? "\\|" : _Separator)));
                            _lineNumber++;
                        }
                    }
                }
        }
        catch(Exception e)
        {
            this.Close();
            return (new ErrorObject(String.format("An ERROR occurred when opening file :'%s'\n", _Filename), e));
        }

        return null; // Return empty error object if everything went OK.
    }

    /**
     * Checks a CSV file format in terms of its format including delimiters and headers and throws an exception
     * if an error can be verified or returns the number of rows in the file if everything looks OK.
     * @return
     * @throws IOException
     */
    public int CheckCSVFormat() throws IOException
    {
        int rows = 0;
        try
        {
                this.Close();
                ErrorObject err = this.OpenFile();
                if (err != null)
                {
                    err.printDetails();
                    return -1;            // return to Operating System since data is unavailable (no data or badly formated)
                }
        
                HashMap<String, String> dataRow;
                for( ;(dataRow = this.ReadLine()) != null; rows++)  // Read all keywords and return them in a dictionary structure
                {
                    if (dataRow.size() != _HeaderRow.length)
                    {
                        System.out.printf("ERROR: Number of header items is not aligned with content in line:%d (Header count: %d, Content %d))\n", rows, dataRow.size(), _HeaderRow.length);
                        
                        for (java.util.Map.Entry<String, String> s : dataRow.entrySet())
                            System.out.printf("Key '%s, value '%s'\n", s.getKey(), s.getValue());
                        
                        return -1;
                    }
                }

                return rows;
        }
        catch(Exception e)
        {
            ErrorObject err = new ErrorObject(String.format("fault CSV file format in '%s' line %d", this._Filename, rows), ErrorSeverity.ERROR);
            err.printDetails();
            return -1;
        }
        finally
        {
            this.Close();
        }
    }

    /**
     * Gets a dictionary of strings 
     * @return
     * @throws IOException
     */
    public HashMap<String, String> ReadLine() throws IOException
    {
        HashMap<String, String> dataRow = null;

        // read the file line by line
        String line="-1";
        try
        {
            if ((line = _bufreader.readLine()) != null)
            {
                    // convert line into tokens based on separator (escape pipe for Regex if present)
                    String[] tokens = line.split(String.valueOf((_Separator == '|' ? "\\|" : _Separator)));

                    if (_HeaderRow == null) // Generate artificial header if none was given
                    {
                        _HeaderRow = new String[tokens.length];
                        for (int i=0; i< _HeaderRow.length; i++)
                            _HeaderRow[i] = "_c" + String.valueOf(i);
                    }

                    dataRow = new HashMap<String, String>();
                    for (int i=0; i< _HeaderRow.length; i++)
                        dataRow.put(_HeaderRow[i], tokens[i]);

                    _lineNumber++;
            }
            else
                this.Close();
        }
        catch(Exception e)
        {
            System.out.printf("An exception occurred in FromCSV.ReadLine() when reading line %d\n", _lineNumber);

            System.out.printf("Header content: ");
            for (int i=0; i< _HeaderRow.length; i++)
                System.out.printf("'%s' ", _HeaderRow[i]);

            System.out.printf("\nLine content: '%s'\n", line);
            e.printStackTrace();
        }

        return dataRow;
    }

    /**
     * Close the current CSV file and free all resources.
     * @throws IOException
     */
    public void Close() throws IOException
    {
        if (_bufreader != null)
        {
            _bufreader.close();
            _bufreader = null;
            _lineNumber = 0;
            _HeaderRow = null;
        }
    }
}