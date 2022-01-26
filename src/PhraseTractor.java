
import java.io.FileNotFoundException;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import InvertedIndex.*;
import CSV.*;
import Documents.*;
import ProgramObjects.*;
import ProgramObjects.Errors.ErrorObject;

class PhraseTractor
{
    /**
     * Main method of this command line tool.
     * 
     * @param args
     * @throws FileNotFoundException
     * @throws IOException
     * @throws Exception
     */
    public static void main(String[] args) throws FileNotFoundException, IOException, Exception
	{
        Instant start = Instant.now();
        HashMap<String, Keyword> keys = null;

        System.out.printf("\n\n    %s: %s\n", Tool.Name, Tool.Version); 

        ProgramArgs progArgs = ProgramArgs.readConfigFromPropertiesFile(args);

        if (progArgs.getError() != null)
        {
            progArgs.getError().printDetails();
            System.exit(-1);
        }

        try
        {
            if (progArgs.getExtractKeys())
            {
                System.out.printf("       Reading Key File: '%s'\n", progArgs.getKeyFileName()); 
                keys = ReadKeywordsCSV(progArgs.getKeyFile().getFileInNamePath(), '|', true, "regex", "text");

                if (keys == null)
                    System.exit(-1);

                System.out.printf("             keys found: %d\n", keys.size());
            }
        }
        catch (Exception e)
        {
            System.out.printf("An Exception: '%s' occurred when reading key file.\n", e.getMessage());
            e.printStackTrace();
            System.exit(-1);
        }

        DocQueries docQueries = new DocQueries();

        FromCSV df = new FromCSV(progArgs.getTextFile().getFileInNamePath(), '|', true);
        int rows = df.CheckCSVFormat();

        if (rows <= 0)
            System.exit(-3); // return to Operating System since data is unavailable (no data or badly formated)
        
        System.out.println();
        System.out.printf("              Processing %d data rows from '%s'\n", rows, progArgs.getTextFile().getFileInName());

        ErrorObject err = df.OpenFile();
        if (err != null)
        {
            err.printDetails();
            System.exit(-3); // return to Operating System since data is unavailable (no data or badly formated)
        }
		
		IndexDocumentResult invIdxRes = IIndex.IndexDocuments(                 // Build index structure(s)
			  df
            , (progArgs.getGenerateRowId() ? null : progArgs.getRowIdColumn()), progArgs.getTextColumn()
            , progArgs.getRegexMask()
            , docQueries, keys
            , progArgs.getWordPairFequencies()
        );
        
        System.out.printf("            Index build: %s\n", (invIdxRes.getSuccess() ? "Successful" : "UN-SUCCESSFUL"));
        System.out.printf("       Documents parsed: %d\n", invIdxRes.getDocsParsed());
        System.out.printf("     Unique words found: %d\n", invIdxRes.getInvertedIIndex().WordCount());

        if (progArgs.getWordPairFequencies())
            System.out.printf("Unique word-pairs found: %d\n", invIdxRes.getNextWordIndex().WordCount());

        System.out.printf(" Regex Document Matches: %d\n", docQueries.DocumentCount());

		// Print all those keys that are not present in document collection and write frequencies to CSV output
		if (progArgs.getExtractKeys())
		{
			String keywordfrequencyfileName = progArgs.getBaseOutputDir() + progArgs.getKeyFile().getFileInNameWithoutExtension() + "_Keyword_Frequs.csv";

			QueryKeysOnDocuments(invIdxRes.getInvertedIIndex(), keys, docQueries, progArgs.getRegexMask(), keywordfrequencyfileName);
		}

        String docResultFileName = progArgs.getBaseOutputDir() + progArgs.getKeyFile().getFileInNameWithoutExtension() + "_DocsWithKeywords.csv";
        String docNoMatchResultFileName = progArgs.getBaseOutputDir() + progArgs.getKeyFile().getFileInNameWithoutExtension() + "_NoMatchDocsWithKeywords.csv";
        
        // Write document/keyword frequency results if there have been any document matches (otherwise results are trivial :-( )
        if (docQueries.DocumentCount() > 0)
        {
            System.out.printf("Writting %d documents retrieved via keyword queries into '%s' file.\n", docQueries.DocumentCount(), docResultFileName);
            System.out.printf("Writting all other documents without match via keyword queries into '%s' file.\n", docNoMatchResultFileName);
    
            df = new FromCSV(progArgs.getTextFile().getFileInNamePath(), '|', true);
            err = df.OpenFile();
            if (err != null)
            {
                err.printDetails();
                System.exit(-3); // return to Operating System since data is unavailable (no data or badly formated)
            }
                
            StoreDocumentKeywordfreques(df, docResultFileName, docNoMatchResultFileName, progArgs.getTextFileName(),
                                        (progArgs.getGenerateRowId() ? null : progArgs.getRowIdColumn()), progArgs.getTextColumn(), docQueries);
        }
        else
        {
            System.out.printf(" Documents retrieved via keyword query is %d - writing no result files with or without matching documents.\n", docQueries.DocumentCount());
        }

		invIdxRes.getInvertedIIndex().WriteWordsSortedByFrequ2CSV(progArgs.getBaseOutputDir() + progArgs.getTextFile().getFileInNameWithoutExtension() + "_AllWord_Frequs.csv");

        if (progArgs.getWordPairFequencies())
            invIdxRes.getNextWordIndex().WriteWordsSortedByFrequ2CSV(progArgs.getBaseOutputDir() + progArgs.getTextFile().getFileInNameWithoutExtension() + "_AllWord_WordPair_Frequs.csv");

        invIdxRes.getInvertedIIndex().WriteWordsSortedByAlpha2CSV(progArgs.getBaseOutputDir() + progArgs.getKeyFile().getFileInNameWithoutExtension() + "_AllWord_Remaining_Words.csv", keys);

        Instant finish = Instant.now();
        Duration duration = Duration.between(start, finish);
        long s = duration.getSeconds();
        System.out.printf("Elapsed processing time was: %d:%02d:%02d\n", s/3600, (s%3600)/60, (s%60));

        return;
    }

    /**
     * Queries contents from key CSV file on index build on text CSV file and returns search results
     * as CSV file(s) containing frequencies of resolved terms.
     * @param iindex
     * @param keys
     * @param docQueries
     * @param regexMask
     * @param keywordfrequencyfileName Output CSV file name and path
     * @throws Exception
     */
    private static void QueryKeysOnDocuments(
        IIndex iindex,
        HashMap<String, Keyword> keys,
        DocQueries docQueries,
        String regexMask,
        String keywordfrequencyfileName) throws Exception
    {
        ToCSV csvOut = new ToCSV(new String[] { "regex", "word_freq", "word" });

        // Sort entries by Id to spit keyword frequencies out in same order as they where read from input
        Lists.SortedList<Long, Keyword> listKeys = new Lists.SortedList<Long, Keyword>();
        for (Keyword item : keys.values())
            listKeys.add(item.IdKeyword(), item);

        for (Map.Entry<Long, List<Keyword>> entryItem : listKeys.getColl().entrySet())
        {
            for (Keyword item : entryItem.getValue())
            {
                // Parse query string with same regex expression as it was used for document/word split
                Pattern pattern = Pattern.compile(regexMask);
                Matcher matcher = pattern.matcher((item.getWord() == null ? "" : item.getWord())); 
                String textAlphaOnlyIn = matcher.replaceAll(" ");
                
                String[] words = textAlphaOnlyIn.toLowerCase().split("\\s+");

                if (item.getRegex() == true)
                    csvOut.WriteLine(new String[] { "1", String.valueOf(item.getMatchedDocuments()), item.getWord() });
                else
                {
                    WordDocumentOffests<String> dicItem = null;
                    HashSet<Long> docIds = null;
                    String QueryTypeId = "UNKNOWN";
                    
                    switch (item.getTypeOfQuery())
                    {
                        case PHRASE_QUERY:
                            dicItem = iindex.PhraseQuery(words);
                            QueryTypeId = "0";
                        break;

                        case BOOL_AND_QUERY:
                            docIds = iindex.BoolAndQuery(words);
                            QueryTypeId = "2";
                        break;

                        default: throw new IllegalArgumentException(String.format("Encountered unknown type of query '%s'", item.getTypeOfQuery()));
                    }

                    if (dicItem == null && docIds == null)
                    {
                        csvOut.WriteLine(new String[] { "0", "0", item.getWord() });
                        System.out.printf("Item not found: '%s'\n", item.getWord());
                    }
                    else
                    {
                        long sizeOfResult = -1;
                        if (docIds == null && dicItem != null)  // Retrieve result documents from either type of query
                        {
                            docIds = dicItem.DocumentIds();    // PhraseQuery or BOOL_AND_QUERY
                            sizeOfResult = dicItem.CountOffsets();
                        }
                        else
                            sizeOfResult = docIds.size();

                        String query = String.join(" ", words);
                        for (long docItem : docIds)
                            docQueries.AddDocumentQuery(docItem, query);

                        csvOut.WriteLine(new String[] { QueryTypeId, String.valueOf(sizeOfResult), query });

                        // Suggest alternative terms if a variation on phrase query is available and this keyword does not exists so far
                        if (QueryTypeId.compareTo("0") == 0)
                        {
                            if (item.getWord().contains("-"))
                            {
                                String alternativeWord = String.join(" ", item.getWord().replace('-', ' ').trim().split("\\s+"));
    
                                dicItem = iindex.PhraseQuery(alternativeWord.split( "\\s+"));
    
                                if (dicItem != null) // || nexFound == true)
                                {
                                    if (keys.containsKey(alternativeWord) == false)
                                    {
                                        //Console.WriteLine($"Proposed alternative term: '{alternativeWord}' with frequencies - inverted: {(dicItem == null ? 0 : dicItem.CountOffsets)} nextword: {(nxItem == null ? 0 : nxItem.CountOffsets)}");
                                        System.out.printf("Proposed alternative term: '0|%s' with frequencies - inverted: %d\n", alternativeWord, (dicItem == null ? 0 : dicItem.CountOffsets()));
                                    }
                                }
                            }
                        }
                    }
                }
            }

            csvOut.WriteFile(keywordfrequencyfileName);
        }
    }

    /**
     * Store 2 CSV files:
     * 1) Indicating the keywords that triggered extraction of a document
     * 2) Remaining documents that were not matched by a keyword
     * 
     * @param df
     * @param docResultFileName
     * @param docNoMatchResultFileName
     * @param textFileName
     * @param rowIdColumn
     * @param textColumn
     * @param docQueries
     * @throws IOException
     */
    private static void StoreDocumentKeywordfreques(
          FromCSV df
        , String docResultFileName
        , String docNoMatchResultFileName
        , String textFileName
        , String rowIdColumn
        , String textColumn
        , DocQueries docQueries) throws IOException
    {
        ToCSV csvResOut = new ToCSV(new String[] { "rowid", "queries", "text" });
        ToCSV csvNoMatchResOut = new ToCSV(new String[] { "rowid", "text" });

        // Retrieve all documents and write them into result CSV
        // if they where matched by a query
        // or if they were not matched by a query
        HashMap<String, String> dataRow = null;
        long docRow = 0;                      // Parse all documents
        for( ; (dataRow = df.ReadLine()) != null; docRow++)
        {
            String docText = dataRow.get(textColumn);

            if (docText == null)
                continue;

            String rowId;
            if (rowIdColumn != null)
                rowId = dataRow.get(rowIdColumn);
            else
                rowId = String.valueOf(docRow);

            HashSet<String> docQueryStrings = docQueries.TryGetDocumentQuery(Long.parseLong(rowId));
            if (docQueryStrings != null)
            {
                String queryStrings = "";
                for (String item : docQueryStrings)
                    queryStrings = (queryStrings.length() > 0 ? queryStrings + ',' + item : item);

                csvResOut.WriteLine(new String[] { rowId, queryStrings, docText });
            }
            else
            {
                csvNoMatchResOut.WriteLine(new String[] { rowId, docText });
            }
        }

        csvResOut.WriteFile(docResultFileName);
        csvNoMatchResOut.WriteFile(docNoMatchResultFileName);
    }

    /**
     * Read a collection of key words and store whether they are stated in plain text or as a regular expression.
     * 
     * @param keyFileName
     * @param separator
     * @param header
     * @param regexColumnName
     * @param dataColumnName
     * @return
     * @throws IOException
     */
    static HashMap<String, Keyword> ReadKeywordsCSV(String keyFileName, char separator, boolean header
                                                    , String regexColumnName
                                                    , String dataColumnName) throws IOException, Exception
    {
        HashMap<String, Keyword> keys = new HashMap<String, Keyword>();
        HashMap<String, String> dataRow;

        long rowId=1;
        try
        {
            FromCSV srcCSV = new FromCSV(keyFileName, separator, header);
            
            ErrorObject err = srcCSV.OpenFile();
            if (err != null)
            {
                err.printDetails();
                return null;
            }

            // Read all keywords and return them in a dictionary structure
            for(;(dataRow = srcCSV.ReadLine()) != null; rowId++)
            {
                String regex = dataRow.get(regexColumnName);
                String text = dataRow.get(dataColumnName);

                if (regex == null || text == null)
                {
                    System.out.printf("ERROR: Cannot retrieve values for '%s'='%s' or '%s'='%s' column in line %d\n", regexColumnName, regex, dataColumnName, text, rowId);
                    System.out.printf("ERROR: Make sure column names are correct and present in CSV file.\n");
                    return null;
                }

                Keyword key = new Keyword(rowId, Integer.parseInt(regex), text.toLowerCase());
                keys.put(key.getWord(), key);
            }
        }
        catch(Exception e)
        {
            System.out.printf("Error in line %d of file '%s'\n", rowId, keyFileName);
            e.printStackTrace();
            return null;
        }

        return keys;
    }

    /***
     * Helper method to determine if a string is empty or null.
     * 
     * @param s
     * @return
     */
    public static boolean IsNullOrEmpty(String s)
    {
        return s == null || s.length() == 0;
    }
}