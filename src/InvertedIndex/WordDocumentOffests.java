package InvertedIndex;

import java.util.*;

/// <summary>Models an inverted indes list (the occurrence of words in Documents and their Offsets).</summary>
public class WordDocumentOffests<T>
{
    //region fields
    private final String _WordKey;
    private final ArrayList<long[]> _DocumentOffsets;
    //endregion fields

    //region ctors
    /// <summary>
    /// Using this constructor garanties that the resulting document offset list
    /// will never be empty.
    /// </summary>
    /// <param name="wordKey"></param>
    /// <param name="docKey"></param>
    /// <param name="offset"></param>
    public WordDocumentOffests(final String wordKey, final long docKey, final long offset)
    {
        _DocumentOffsets = new ArrayList<long[]>();
        this._WordKey = wordKey;

        if (docKey >= 0 && offset >= 0)
            AddDocumentOffset(docKey, offset);
    }

    /// <summary>Using this constructor CANNOT garanty that the resulting document offset list will never be empty.</summary>
    /// <param name="wordKey"></param>
    /// <param name="docKey"></param>
    /// <param name="offset"></param>
    public WordDocumentOffests(final String wordKey)
    {
        this(wordKey, -1, -1);
    }

    /// <summary>Standard Class constructor</summary>
    protected WordDocumentOffests()
    {
        this(null, -1, -1);
    }

    //endregion ctors

    //region properties
    /**
     * Gets the query words for which this document/offset list is relevant.
     * @return
     */
    public String WordKey() { return _WordKey; };

    /**
     * Get number of document offsets hosted in this collection.
     * @return
     */
    public long CountOffsets() { return _DocumentOffsets.size(); }
    //endregion properties

    //region methods

    /***
     * Adds another Document/Offset id pair to the current collection of document offsets.
     * @param docKey
     * @param offset
     */
    public void AddDocumentOffset(final long docKey, final long offset)
    {
        _DocumentOffsets.add(new long[] { docKey, offset });
    }

    /**
     * Merges the {@value other} word document/offset list with THIS offset list
     * assumming that THIS document/offset list occured one word before the {@value other}
     * document/offset list.
     * @param other
     * @param mergedWords Contains the string that represents the merged list of documents and offsets returned in result.
     * @return Returns the merged list of documents/offsets (if any) but is garantied to result in at
     * least an empty object containing no document id or offset.
     */
    public WordDocumentOffests<T> MergeDocumentOffset(final WordDocumentOffests<T> other, final String mergedWords)
    {
        // Always return at least an empty structure
        final WordDocumentOffests<T> resolvedDocOffsets = new WordDocumentOffests<T>(mergedWords);

        // Merging empty lists will result in... empty list
        if (other == null || other._DocumentOffsets.size() == 0 || _DocumentOffsets.size() == 0)
            return resolvedDocOffsets;

        int otherIdx = 0;

        for (int i = 0; i < _DocumentOffsets.size() && otherIdx < other._DocumentOffsets.size(); )
        {
            long[] thisDoc = _DocumentOffsets.get(i);
            long[] otherDoc = other._DocumentOffsets.get(otherIdx);

            // Attempt to match document and word offest sequence to find occurrance of
            // current word pair
            if (thisDoc[0] == otherDoc[0]) {
                if ((thisDoc[1] + 1) == otherDoc[1]) {
                    resolvedDocOffsets.AddDocumentOffset(otherDoc[0], otherDoc[1]);

                    otherIdx++;
                    i++;
                    continue;
                } else {
                    // Either increment left or right offset once but continue loop below in anyway
                    // to re-check current state of Document Ids when increment was done on either
                    // side
                    if ((thisDoc[1] + 1) < otherDoc[1]) {
                        if (i < _DocumentOffsets.size())
                            i++;
                    } else {
                        if (otherIdx < other._DocumentOffsets.size())
                            otherIdx++;
                    }

                    continue;
                }
            }

            if (thisDoc[0] < otherDoc[0]) {
                for (i = i + 1; i < _DocumentOffsets.size(); i++)
                {
                    thisDoc = _DocumentOffsets.get(i);

                    if (thisDoc[0] >= otherDoc[0]) {
                        thisDoc = _DocumentOffsets.get(i);
                        break;
                    }
                }
            }
            else
            {
                for (otherIdx = otherIdx + 1; otherIdx < other._DocumentOffsets.size(); otherIdx++)
                {
                    otherDoc = other._DocumentOffsets.get(otherIdx);

                    if (otherDoc[0] >= thisDoc[0])
                    {
                        otherDoc = other._DocumentOffsets.get(otherIdx);
                        break;
                    }
                }
            }
        }

        return (resolvedDocOffsets);
    }

    public HashSet<Long> DocumentIds()
    {
        final HashSet<Long> docs = new HashSet<Long>();

        for (long[] item : _DocumentOffsets)
        {
            if (docs.contains(item[0]) == false)
                docs.add(item[0]);
        }

        return docs;
    }
    //endregion methods
}
