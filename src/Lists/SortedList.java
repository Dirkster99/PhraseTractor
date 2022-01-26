package Lists;

import java.util.*;

    /**
     * Orders a list of keys (which can contain dublicates) in ascending or descending order
     * depending on whether @param comp == null (default ascending order) or is set to a
     * Comparator object. A comperator for an ordered list of long values (K is instanciated with 'Long')
     * looks, for example, like this:
     * 
     * <pre>
     *class DescOrder implements Comparator&lt;Long>
     *{
     *    &#064;Override
     *    public int compare(Long o1, Long o2)
     *    {
     *        return o2.compareTo(o1);
     *    }
     *}
     * 
     *SortedList&lt;long, String> = new SortedList&lt;long, String>(new DescOrder());
     * 
     * 
     * 
     * </pre>
     */
public class SortedList<K,T>
{
    private Comparator<K> _Comp;
    TreeMap<K, List<T>> _Coll = new TreeMap<K, List<T>>(_Comp);

    /**
     * Constructor with custom Comperator&gt;K> instance to sort by descending order or by ascending order
     * if default Comperator&gt;K> implementation results in ascending order or if Comperator&gt;K> implementation
     * is missing for a given type K.
     * 
     * @param comp Comperator that should be set if a class implements no default Comperator interface or
     * if sorting should be done in descending order.
     */
    /** Default constructor to sort keys in ascending order by default if Comperator interface is implemented by K.*/
    public SortedList(final Comparator<K> comp) {
        _Comp = comp;

        if (comp != null)
            _Coll = new TreeMap<K, List<T>>(_Comp); // Sort keys in order of comperator e.g.: ascending order
        else
            _Coll = new TreeMap<K, List<T>>();
    }

    /**
     * Default constructor to sort keys in ascending order by default if Comperator interface is implemented by type K.
     * */
    public SortedList() {
        this(null);
    }

    /**
     * Add another key value pair into the sorted colleciton of keys,values that can contain dublicate keys.
     * 
     * @param key
     * @param value
     */
    public void add(final K key, final T value) {
        if (_Coll.containsKey(key)) {
            final List<T> list = _Coll.get(key);
            list.add(value);
        } else {
            final List<T> list = new ArrayList<T>();
            list.add(value);
            _Coll.put(key, list);
        }
    }

    /**
     * Gets the sorted collection of (dublicate) keys and values for retrieval.
     * @return
     */
    public TreeMap<K, List<T>> getColl()
    {
        return _Coll;
    }
}
