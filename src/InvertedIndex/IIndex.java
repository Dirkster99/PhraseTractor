package InvertedIndex;

import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import CSV.FromCSV;
import CSV.ToCSV;
import Documents.DocQueries;
import InvertedIndex.Nx.NxWord;

/***
 * Build an inverted index to query word occurences and resolve them<
 */
public class IIndex
{
    private final HashMap<String, WordDocumentOffests<String>> _iindex;

    /** Class  constructor */
    public IIndex()
    {
        _iindex = new HashMap<String, WordDocumentOffests<String>>();
    }

    /**
     * Gets the number of words indexed in this collection
     * @return
     */
    public long WordCount() { return _iindex.size(); }

    //region methods
    /**
     * Index all positions of all words in all lines of a given text document collection. 
     * 
     * @param df
     * @param textColumn
     * @param rowIdColumn
     * @param regexMask
     * @param docQueries
     * @param keys
     * @param mainKeyWord
     * @param mainKeys
     * @param iindex
     * @param createNextWordIndex
     */
    public static IndexDocumentResult IndexDocuments(
        FromCSV df,
        String rowIdColumn, String textColumn, 
        String regexMask,
        DocQueries docQueries,
        HashMap<String, Keyword> keys,
        boolean createNextWordIndex
        )
        throws IOException, Exception
    {
        IIndex iIndex = new IIndex();
        NxWord nxIndex = (createNextWordIndex ? new NxWord(2) : null);
        
        HashMap<String, String> dataRow;
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

            String input = (IsNullOrEmpty(docText) ? "" : docText);
            
            // Loop through keyword entries and attempt to match Regex Expressions on document 'as is'
            if (keys != null)
            {
                for (Keyword item : keys.values())
                {
                    if (item.getRegex() == true)
                    {
                        if (item.getRegexEpression().matcher(docText).find())
                        {
                            docQueries.AddDocumentQuery(Long.parseLong(rowId), item.getWord());
                            item.IncrementMatchedDocuments();
                        }
                    }                
                }
            }

            // Parse document and emit a list of words representing the content of this document
            // get a matcher object
            Pattern pattern = Pattern.compile(regexMask);
            Matcher matcher = pattern.matcher(input); 
            String textAlphaOnlyIn = matcher.replaceAll(" ");
            
            String[] words = textAlphaOnlyIn.toLowerCase().trim().split("\\s+");

    	    iIndex.IndexDocument(Long.parseLong(rowId), words);      // Build an inverted index

            if (createNextWordIndex)
                nxIndex.IndexDocument(Long.parseLong(rowId), words);    // Build a nextword index for phrase browsing
        }

        return new IndexDocumentResult(iIndex, nxIndex, docRow, true);
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

    /**
     * Add the text content of a given document word by word into the inverted index.
     * @param docKey
     * @param words
     */
    public void IndexDocument(long docKey, String[] words)
    {
        if (words.length <= 0)  // String does not have enough words for nGram process
            return;

        // Run a sliding window through words array and accumulate an nGram per loop
        // Insert each nGram into dictionary
        for (long docOffset = 0; docOffset < words.length; docOffset++)
        {
            String wordKey = words[(int)docOffset];
            if (_iindex.containsKey(wordKey))
            {
                WordDocumentOffests<String> docOffItem = _iindex.get(wordKey);
                docOffItem.AddDocumentOffset(docKey, docOffset);
            }
            else
            {
                WordDocumentOffests<String> newItem = new WordDocumentOffests<String>(wordKey, docKey, docOffset);
                _iindex.put(wordKey, newItem);
            }
        }
    }

    /**
     * Gets the Document Offset collection associated with the specified words.
     * @param words
     * @return
     * @throws Exception
     */
    public WordDocumentOffests<String> PhraseQuery(String[] words) throws Exception
    {
        WordDocumentOffests<String> dicItem = null;
        String queryResolution = "";

        if (words == null || words.length == 0)
            throw new Exception("The number of words in a query cannot be zero.");

        dicItem = this.QueryDocumentOffests(words[0]); // Resolve first word in query

        // We are done because we either have
        // 1) only 1 word to query for or
        // 2) we could not find the first word
        if (words.length == 1 || dicItem == null)
            return dicItem;

        queryResolution = words[0];

        // Merge occurrances of each word in the given query
        for (int i = 1; i < words.length; i++)
        {
            WordDocumentOffests<String> secondDocOffsets = null;

            secondDocOffsets = QueryDocumentOffests(words[i]);
            if (secondDocOffsets == null)
                return null;     // Next Word could not be resolved so we return empty handed

            // Merge 2 Document Offset lists to generate a third result list to be returned or used in next loop
            queryResolution = queryResolution + " " + words[i];
            WordDocumentOffests<String> resolvedDocOffsets;
            if ((resolvedDocOffsets = dicItem.MergeDocumentOffset(secondDocOffsets, queryResolution)).CountOffsets() == 0)
                return null;     // Next Word could not be resolved so we return empty handed

            dicItem = resolvedDocOffsets; // setup for next loop's merge operation
        }

        return dicItem;
    }

    /**
     * Write a list of indexed words sorted by their frequencies to a CSV file
     * @param fileName
     * @throws IOException
     */
    public void WriteWordsSortedByFrequ2CSV(String fileName) throws IOException
    {
        // Sort by positiv frequencies and output result
        Lists.SortedList<Long, String> slist = new Lists.SortedList<Long, String>(new Lists.LongDescOrder());
        for (Map.Entry<String, WordDocumentOffests<String>> item : _iindex.entrySet())
            slist.add(item.getValue().CountOffsets(), item.getValue().WordKey());

        ToCSV csvOut = new ToCSV(new String[] { "frequency", "word" });
        for (Map.Entry<Long,List<String>> entry : slist.getColl().entrySet())
        {
            for (String s : entry.getValue())
                csvOut.WriteLine(new String[] { entry.getKey().toString(), s });
        }

        csvOut.WriteFile(fileName);
    }

    /***
     * Write a list of indexed words sorted by their alphanumeric value and whether
     * they're alread being keyed/used in a CSV file query (1) or not (0).
     * @param fileName
     * @param keys
     * @throws IOException
     */
    public void WriteWordsSortedByAlpha2CSV(String fileName, HashMap<String, Keyword> keys) throws IOException
    {
        // Sort by alphanumeric string value and output result
        Lists.SortedList<String, String> slist = new Lists.SortedList<String, String>();
        for (Map.Entry<String, WordDocumentOffests<String>> item : _iindex.entrySet())
            slist.add(item.getValue().WordKey(), item.getValue().WordKey());

        ToCSV csvOut = new ToCSV(new String[] { "keyed", "word" });
        for (Map.Entry<String,List<String>> entry : slist.getColl().entrySet())
        {
            String sWordOut = "0";
            for (String s : entry.getValue())
            {
                if (keys.containsKey(s))
                    sWordOut = "1";

                csvOut.WriteLine(new String[] { sWordOut, s });
            }
        }

        csvOut.WriteFile(fileName);
    }

    /**
     * Performs a relaxed phrase query - that is a document is part of the result collection
     * if it contains all of the given query words in any order (also known as boolean AND query).
     * @param words
     * @return
     * @throws Exception
     */
	public HashSet<Long> BoolAndQuery(String[] words) throws Exception
    {
        HashSet<Long> dicItem = null;
        String queryResolution = "";

        if (words == null || words.length == 0)
            throw new Exception("The number of words in a query cannot be zero.");

        dicItem = this.QueryDocuments(words[0]); // Resolve first word in query

        // We are done because we either have
        // 1) only 1 word to query for or
        // 2) we could not find the first word
        if (words.length == 1 || dicItem == null)
            return dicItem;

        queryResolution = words[0];

        // Merge occurrances of each word in the given query
        for (int i = 1; i < words.length; i++)
        {
            HashSet<Long> secondDocOffsets = null;

            secondDocOffsets = QueryDocuments(words[i]);
            if (secondDocOffsets == null)
                return null;     // Next Word could not be resolved so we return empty handed

            // Merge 2 Document Offset lists to generate a third result list to be returned or used in next loop
            queryResolution = queryResolution + " " + words[i];

            HashSet<Long> resolvedDocOffsets = IntersectDocumentIds(dicItem, secondDocOffsets);
            if (resolvedDocOffsets.size() == 0)
                return null;
            else
                dicItem = resolvedDocOffsets;
        }

        return dicItem;
	}

    /**
     * Intersects document Ids and returns the intersection of both collections.
     * @param firstDoc
     * @param secDoc
     * @return
     */
    private HashSet<Long> IntersectDocumentIds(HashSet<Long> firstDoc, HashSet<Long> secDoc)
    {
        HashSet<Long> ret = new HashSet<Long>();

        if (secDoc.size() < firstDoc.size()) // optimize execution for size of either collection       
        {
            for (Long docId : secDoc)
            {
                if (firstDoc.contains(docId))
                    ret.add(docId);
            }
        }
        else
        {
            for (Long docId : firstDoc)
            {
                if (secDoc.contains(docId))
                    ret.add(docId);
            }
        }

        return ret;
    }

    /***
     * Gets the Document Offset collection associated with the documents
     * in which the specified word is contained.
     * @param Word
     * @return
     */
    protected WordDocumentOffests<String> QueryDocumentOffests(String Word)
    {
        if (_iindex.containsKey(Word))
            return _iindex.get(Word);

        return null;
    }

    /**
     * Resolve a query for the occurrence of a word in the text collection
     * and return all document Ids of the documents in which the given word is contained.
     * @param Word
     * @return
     */
    protected HashSet<Long> QueryDocuments(String Word)
    {
        if (_iindex.containsKey(Word))
        {
            WordDocumentOffests<String> docOffsets = _iindex.get(Word);
            return docOffsets.DocumentIds();
        }

        return null;
    }
    //endregion methods
}

