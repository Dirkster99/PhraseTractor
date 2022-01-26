package InvertedIndex;

//import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Model different types of queries againt an index. This can be either:
 * 1) a string of words (phrase query) or
 * 2) a regular expression or
 * 3) a relaxed query with all words present in any order in a document (boolean AND query)
 * 
 * to be matched in a document collection.
 */
public class Keyword
{
    private long _MatchedDocuments;
    private QueryType _TypeOfQuery = QueryType.PHRASE_QUERY;
    private final String _Word;
    private final  Pattern _RegexEpression;
    private final long _IdKeyword;

    /**
     * Class constructor
     * @param idKeyword
     * @param queryTypeID
     * @param keyword
     */
    public Keyword(long idKeyword, int queryTypeID, String keyword)
    {
        _IdKeyword = idKeyword;
        _Word = keyword;

        switch (queryTypeID)
        {
            case 0: _TypeOfQuery = QueryType.PHRASE_QUERY;
            break;

            case 1: _TypeOfQuery = QueryType.REGULAR_EXPRESSION;
            break;

            case 2: _TypeOfQuery = QueryType.BOOL_AND_QUERY;
            break;

            default: throw new IllegalArgumentException(String.format("Unknown type of query identifier %d in Keyword class constructor.", queryTypeID));
        }

        if (_TypeOfQuery == QueryType.REGULAR_EXPRESSION)       // Unescape pipe since this can collide with CSV format
            _RegexEpression = Pattern.compile(keyword.replace("_pipe_", "|"), Pattern.CASE_INSENSITIVE);
        else
            _RegexEpression = null;
    }

    /** Class constructor */
    protected Keyword()
    {
        this(-1, 0, null);
        _MatchedDocuments = 0;
    }

    /**
     * Gets an Id for a keyword.
     * @return
     */
    public long IdKeyword(){ return _IdKeyword; }

    /**
     * Is the keyword read a regular expression or a string 'as is'
     * @return
     */
    public boolean getRegex() { return _TypeOfQuery == QueryType.REGULAR_EXPRESSION; }

    /**
     * Gets the type of query that should be implemented to satisfy this object's content.
     * @return
     */
    public QueryType getTypeOfQuery() { return _TypeOfQuery; }

    /**
     * Keyword extracted from text to identify keyphrases for a certain class of labels
     * @return
     */
    public String getWord() { return _Word; }

    /**
     * Get the compiled regular expression (if this is a regular expression query).
     * Use this to match text with a statement like this:
     * Matcher matcher = pattern.matcher("Searching strings for pattern rocks!");
     * @return
     */
    public Pattern getRegexEpression() { return _RegexEpression; }

    /**
     * Get the number of documents that matched this query on input the text collection.
     * @return
     */
    public long getMatchedDocuments() { return _MatchedDocuments; }

    /** Increment number of documents that match this query
     * (this is used durring query evaluation to keep track of statistics on hits). */
    public void IncrementMatchedDocuments() {_MatchedDocuments += 1; }

    @Override
    public String toString()
    {
        return String.format(String.valueOf(_TypeOfQuery) + " " + _Word);
    }
}