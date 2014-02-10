/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.valueset;

/**
 * An interface for a range. Ranges with a lower bound higher than the upper bound are considered
 * empty. Note that it is only useful to define ranges for objects that have a total ordering. The
 * natural ordering is defined by the implementation of the <code>Comparable<code> interface.
 * 
 * @author Jan Ortmann
 * @author Daniel Hohenberger conversion to Java5
 * @see java.lang.Comparable
 */
public interface Range<T extends Comparable<? super T>> extends ValueSet<T> {

    /**
     * Returns the range's lower bound, <code>null</code> means that the range is unbounded.
     */
    public T getLowerBound();

    /**
     * Returns the range's upper bound, <code>null</code> means that the range is unbounded.
     */
    public T getUpperBound();

    /**
     * Returns <code>true</code> if the indicated value is contained in the range, otherwise
     * <code>false</code>. Returns <code>false</code> if the value is null.
     */
    public boolean contains(T value);

    /**
     * {@inheritDoc}
     * 
     * By convention a range is empty if the upper bound is less than the lower bound.
     */
    public boolean isEmpty();

    /**
     * The unit that defines the discrete values that are allowed to be within this range. The
     * values that are allowed to be within this range have to meet the condition: <i>bound + x*step
     * = value</i>, while <i>x</i> must be an integer value. The variable bound is either the upper
     * or the lower bound while one of these needs to be different from null. The returned value can
     * be null indicating that this is a continuous range.
     */
    public T getStep();

}
