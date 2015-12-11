/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.core.util;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.google.common.collect.Maps.EntryTransformer;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.ObjectUtils;
import org.faktorips.values.Decimal;

/**
 * Class that is used to get a histogram of elements' values, i.e. an overview how often which value
 * occurs. The value of an element is determined using a {@link Function}. Equality of values can be
 * determined using an {@link Comparator}.
 * 
 * @param <V> the type of values over which this histogram is created
 * @param <E> the type of elements over whose values this histogram is created
 */
public class Histogram<V, E> {

    /** Scale to which decimal values in {@link #getRelativeDistribution()} are rounded. */
    public static final int SCALE = 2;

    /**
     * Map containing the mapping of a value to the elements containing that value. The amount of
     * elements is the distribution how often the value occurs.
     */
    private final TreeMultimap<V, E> valueToElements;

    /** Function to determine an element's value. */
    private final Function<E, V> elementToValueFunction;

    /** Total number of elements in this histogram. */
    private final int totalCount;

    /**
     * Creates a new histogram of the given elements. Equality of elements' values is determined
     * using the values' equals method.
     */
    public Histogram(Function<E, V> elementToValueFunction, E... elements) {
        this(elementToValueFunction, new EqualToComparator<V>(), Arrays.asList(elements));
    }

    /**
     * Creates a new histogram of the given elements. Equality of elements' values is determined
     * using the values' equals method.
     */
    public Histogram(Function<E, V> elementToValueFunction, Collection<E> elements) {
        this(elementToValueFunction, new EqualToComparator<V>(), elements);
    }

    /**
     * Creates a new histogram of the given elements. Equality of elements' values is determined
     * using the given comparator.
     */
    public Histogram(Function<E, V> elementToValueFunction, Comparator<? super V> valueComparator, E... elements) {
        this(elementToValueFunction, valueComparator, Arrays.asList(elements));
    }

    /**
     * Creates a new histogram of the given elements. Equality of elements' values is determined
     * using the given comparator.
     */
    public Histogram(Function<E, V> elementToValueFunction, Comparator<? super V> valueComparator,
            Collection<E> elements) {
        super();
        this.totalCount = elements.size();
        this.elementToValueFunction = elementToValueFunction;
        this.valueToElements = TreeMultimap.create(valueComparator, new SameInstanceComparator<E>());
        initValueToElementsMap(elements);
    }

    /**
     * Returns the absolute distribution of values in this histogram as a sorted map. The keys in
     * the map are the values in this histogram, the associated value in the map is the total count
     * how often the value occurs. The map is sorted so that values occurring more often come first.
     */
    public SortedMap<V, Integer> getAbsoluteDistribution() {
        TreeMap<V, Integer> sortedDistribution = Maps.newTreeMap(new DistributionComparator());
        sortedDistribution.putAll(transformToOccurenceCountMap(valueToElements));
        return Collections.unmodifiableSortedMap(sortedDistribution);
    }

    /**
     * Returns the relative distribution of values in this histogram as a sorted map. The keys in
     * the map are the values in this histogram, the associated value in the map is a decimal n with
     * 0 &lt; n &lt;= 1 that indicates how often an value occurs relative to the other values in the
     * histogram. The decimal is rounded to a scale of {@link #SCALE}. The map is sorted so that
     * values occurring more often come first.
     */
    public SortedMap<V, Decimal> getRelativeDistribution() {
        SortedMap<V, Integer> absoluteDistribution = getAbsoluteDistribution();
        SortedMap<V, Decimal> relativeDistribution = transformToRelativeDistribution(absoluteDistribution);
        return Collections.unmodifiableSortedMap(relativeDistribution);
    }

    public Set<E> getElements(V value) {
        return Collections.unmodifiableSet(valueToElements.get(value));
    }

    /**
     * Transforms the given map containing the absolute distribution of values to a new map
     * containing the relative distribution of values.
     */
    private SortedMap<V, Decimal> transformToRelativeDistribution(SortedMap<V, Integer> map) {
        return Maps.transformEntries(map, new EntryTransformer<V, Integer, Decimal>() {
            @Override
            public Decimal transformEntry(V value, Integer elementCount) {
                return Decimal.valueOf(elementCount).divide(totalCount, SCALE, BigDecimal.ROUND_HALF_UP);
            }
        });
    }

    /**
     * Transforms the given {@code Multimap} to a simple {@code Map}, the keys are the key from the
     * {@code Multimap}, the value is the number of values to that key in the {@code Multimap}.
     */
    private Map<V, Integer> transformToOccurenceCountMap(Multimap<V, E> map) {
        return Maps.transformEntries(map.asMap(), new EntryTransformer<V, Collection<E>, Integer>() {

            @Override
            public Integer transformEntry(V value, Collection<E> elements) {
                return CollectionUtils.size(elements);
            }

        });
    }

    /**
     * Initializes the distribution map. Values in the map are the values of the elements in the
     * given list, keys in the map are the counts how often those values occur.
     * <p>
     * Note that by using a {@link TreeMap} with a comparator, we explicitly want to determine
     * equality of values using said comparator (instead of the values' equals methods). In other
     * words, values that are equal according to the comparator are counted together, no matter if
     * they would be equal according to their equal method or not.
     */
    private void initValueToElementsMap(Collection<E> elements) {
        for (E e : elements) {
            valueToElements.put(elementToValueFunction.apply(e), e);
        }
    }

    private static int compareIdentityHashcode(Object o1, Object o2) {
        int idCompare = IntegerUtils.compare(System.identityHashCode(o1), System.identityHashCode(o2));
        if (idCompare == 0) {
            // Fallback for (hopefully extremely) rare cases where objects are not equal but
            // do have the same identity hash code. Return a value != 0 to prevent different
            // objects overwriting each other in the distribution map. TreeMap probably
            // won't find the entries, but hey, at least we tried...
            return 1;
        }
        return idCompare;
    }

    /**
     * Comparator that compares values by their occurrence count in
     * {@link Histogram#valueToElements} so that a value e1 occurring more often than e2 is less
     * than e2 and thus is sorted before e2. In other words this comparator implements the inverse
     * natural order of the occurrence count of values.
     */
    private class DistributionComparator implements Comparator<V> {

        @Override
        public int compare(V value1, V value2) {
            Integer occurences1 = valueToElements.get(value1).size();
            Integer occurences2 = valueToElements.get(value2).size();
            // reverse natural order
            return ObjectUtils.compare(occurences2, occurences1);
        }

    }

    /**
     * A {@link Comparator} that uses object identity to compare objects. This comparator returns
     * <ul>
     * <li>0 when tow objects are the same instance</li>
     * <li>1 or -1 by comparing the objects' {@link System#identityHashCode(Object)} if they are not
     * the same instance according to their equals method</li>
     * </ul>
     * */
    private static class SameInstanceComparator<U> implements Comparator<U>, Serializable {

        private static final long serialVersionUID = -5480214299260180838L;

        @Override
        public int compare(U o1, U o2) {
            if (o1 == o2) {
                return 0;
            } else {
                return compareIdentityHashcode(o1, o2);
            }
        }
    }

    /**
     * A {@link Comparator} that uses the equals method to compare objects. This comparator returns
     * <ul>
     * <li>0 for objects that are equal according to their equals method</li>
     * <li>1 or -1 by comparing the objects' {@link System#identityHashCode(Object)} if they are not
     * equal according to their equals method</li>
     * </ul>
     * */
    private static class EqualToComparator<U> implements Comparator<U>, Serializable {

        private static final long serialVersionUID = -5480214299260180838L;

        @Override
        public int compare(U o1, U o2) {
            if (ObjectUtils.equals(o1, o2)) {
                return 0;
            } else {
                return compareIdentityHashcode(o1, o2);
            }
        }
    }

}
