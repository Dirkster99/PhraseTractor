package Lists;

import java.util.Comparator;

/**
 * Implements an internal helper class used for sorting long values in
 * DESCENDING order based on long values.
 */
public class LongDescOrder implements Comparator<Long>
{
    @Override
    public int compare(final Long o1, final Long o2)
    {
        return o2.compareTo(o1);
    }
}
