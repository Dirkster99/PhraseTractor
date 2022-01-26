package InvertedIndex.Nx;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import InvertedIndex.WordDocumentOffests;
import CSV.ToCSV;

public class NxWord
{
    //region fields
    private HashMap<NGram, WordDocumentOffests<NGram>> _iindex = null;
    private int _NGramLen = -1;
    //endregion fields

    /**
     * Class constructor
     * @param NGramLen
     */
    public NxWord(int NGramLen)
    {
        _NGramLen = NGramLen;
        _iindex = new HashMap<NGram, WordDocumentOffests<NGram>>();
    }

    //region properties
    /**
     * Gets the number of NGrams indexed in this collection
     * 
     * @return
     */
    public long WordCount() { return _iindex.size(); }

    public int NGramLen() { return _NGramLen; }
    //endregion properties

    //region methods
    /**
     * Add the text content of a given document word by word into the inverted index
     * 
     * @param docKey
     * @param words
     */
    public void IndexDocument(long docKey, String[] words) throws Exception
    {
        try
        {
            if (words.length <= _NGramLen)  // String does not have enough words for nGram to process
                return;

            // Run a sliding window through words array and accumulate an nGram per loop
            // Insert each nGram into dictionary
            for (int docOffset = 0; docOffset < words.length - _NGramLen + 1; docOffset++)
            {
                String[] NGramwords = NGram.NGramFromStringArray(_NGramLen, words, docOffset);

                NGram wordKey = new NGram(NGramwords, 0);
                
                if (_iindex.containsKey(wordKey))
                {
                    WordDocumentOffests<NGram> docOffItem;
                    docOffItem = _iindex.get(wordKey);
                    docOffItem.AddDocumentOffset(docKey, docOffset);
                }
                else
                {
                    WordDocumentOffests<NGram> newItem = new WordDocumentOffests<NGram>(wordKey.getWordsString(), docKey, docOffset);
                    _iindex.put(wordKey, newItem);
                }
            }
        }
        catch(Exception e)
        {
            System.out.println();
            System.out.printf("Caught an exception in NxWord:IndexDocument on docKey: %d\n", docKey);
            for (int i=0; i < words.length; i++)
            {
                System.out.printf("   Word:%d '%s'\n", i, words[i]);
            }
            System.out.println();

            throw new Exception(e);
        }
    }

    /**
     * Gets the Document Offset collection associated with the specified word.
     * 
     * @param Word
     * @return
     */
    protected WordDocumentOffests<NGram> PhraseQuery(NGram Word)
    {
        if (_iindex.containsKey(Word))
            return _iindex.get(Word);
        
        return null;
    }

    /**
     * Gets the Document Offset collection associated with the specified words.
     * 
     * @param words
     * @return
     * @throws Exception
     */
    public WordDocumentOffests<NGram> PhraseQuery(NGram[] words) throws Exception
    {
        WordDocumentOffests<NGram> dicItem = null;
        String queryResolution = "";

        if (words == null || words.length == 0)
            throw new Exception("The number of words queries cannot be empty.");

        dicItem = this.PhraseQuery(words[0]); // Resolve first word in query

        // We are done because we either have
        // 1) only 1 word to query for or
        // 2) we could not find the first word
        if (words.length == 1 || dicItem == null)
            return dicItem;

        queryResolution = words[0].getWordsString();

        // Merge occurrances of each word in the given query
        for (int i = 1; i < words.length; i++)
        {
            WordDocumentOffests<NGram> secondDocOffsets;
            secondDocOffsets = PhraseQuery(words[i]);
            if (secondDocOffsets == null)
                return null;     // Next Word could not be resolved so we return empty handed


            // Merge 2 Document Offset lists to generate a third result list to be returned or used in next loop
            queryResolution = queryResolution + " " + words[i].getWordsString();
            WordDocumentOffests<NGram> resolvedDocOffsets;
            if ((resolvedDocOffsets = dicItem.MergeDocumentOffset(secondDocOffsets, queryResolution)).CountOffsets() == 0)
                return null;     // Next Word could not be resolved so we return empty handed

            dicItem = resolvedDocOffsets; // setup for next loop's merge operation
        }

        return dicItem;
    }

    /**
     * Write a list of indexed words sorted by their frequencies to a CSV file
     * 
     * @param fileName
     * @throws Exception
     */
    public void WriteWordsSortedByFrequ2CSV(String fileName) throws Exception
    {
        // Sort by positiv frequencies and output result
        Lists.SortedList<Long, String> slist = new Lists.SortedList<Long, String>(new Lists.LongDescOrder());
        for (Map.Entry<NGram, WordDocumentOffests<NGram>> item : _iindex.entrySet())
            slist.add(item.getValue().CountOffsets(), item.getValue().WordKey());

        ToCSV csvOut = new ToCSV(new String[] { "frequency", "word" });
        for (Map.Entry<Long, List<String>> entry : slist.getColl().entrySet())
        {
            for (String s : entry.getValue())
                csvOut.WriteLine(new String[] { entry.getKey().toString(), s });
        }

        csvOut.WriteFile(fileName);
    }
    //endregion methods

}