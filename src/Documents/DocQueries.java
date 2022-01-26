package Documents;

import java.util.HashSet;
import java.util.TreeMap;

public class DocQueries {
    //#region fields
    private final TreeMap<Long, HashSet<String>>  _DocQs;
    //#endregion fields

    //#region ctor
    public DocQueries()
    {
        _DocQs = new TreeMap<Long, HashSet<String>>(); //new AscDuplicateKeyComparer<long>()
    }
    //#endregion ctor

    //#region properties
    /**
     * Get Number of document Ids stored in this collection
     * @return
     */
    public long DocumentCount() { return _DocQs.size(); };
    //#endregion properties

    //#region methods
    public void AddDocumentQuery(long docId, String query)
    {
        if (_DocQs.containsKey(docId))
        {
            HashSet<String> docQ;
            docQ = _DocQs.get(docId);

            if (docQ.contains(query) == false)
                docQ.add(query);
        }
        else
        {
            HashSet<String> queries = new HashSet<String>();
            queries.add(query);
            _DocQs.put(docId, queries);
        }
    }

    public HashSet<String> TryGetDocumentQuery(long docId)
    {
        if (_DocQs.containsKey(docId) == false)
            return null;

        return _DocQs.get(docId);
    }
    //#endregion methods
}