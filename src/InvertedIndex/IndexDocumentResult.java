package InvertedIndex;

import InvertedIndex.Nx.NxWord;

/***
 * This type of object contains result information (indexes and others) when building an index on a given colleciton of text.
 */
public class IndexDocumentResult
{
    private final IIndex _Iindex;
    private final long _DocsParsed;
    private final boolean _Success;
    private final NxWord _NxIndex;

    /**
     * Class constructor from parameters including nextword index.
     * @param iindex
     * @param nxIndex
     * @param docsParsed
     * @param success
     */
    public IndexDocumentResult(IIndex iindex, NxWord nxIndex, long docsParsed, boolean success)
    {
        _Iindex = iindex;
        _NxIndex = nxIndex;
        _DocsParsed = docsParsed;
        _Success = success;
    }

    /***
     * Class constructor from parameters excluding nextword index.
     * @param iindex
     * @param docsParsed
     * @param success
     */
    public IndexDocumentResult(IIndex iindex, long docsParsed, boolean success)
    {
        this(iindex, null, docsParsed, success);
    }

    /**
     * Gets the inverted index (for querying) as it was returned by the index build.
     * @return
     */
    public IIndex getInvertedIIndex() { return _Iindex; }

    /**
     * Gets the nextword index (if any, for querying) as it was returned by the index build.
     * @return
     */
    public NxWord getNextWordIndex() { return _NxIndex; }

    /**
     * Gets the number of documents being parsed during index build to determine
     * whether build was partial (if an error occured) or not.
     * @return
     */
    public long getDocsParsed() { return _DocsParsed; }

    /**
     * Gets an indicator of whether an error occurerd durring index build or not.
     * @return
     */
    public boolean getSuccess() { return _Success; }
}
