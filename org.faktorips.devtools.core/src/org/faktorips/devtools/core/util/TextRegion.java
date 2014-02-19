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

/**
 * The TextRegion describes a certain range in a String. The TextRegion is defined by the start and
 * end variables. The variables don't change over time.
 * 
 */
public class TextRegion implements Comparable<TextRegion> {

    private final int start;

    private final int end;

    /**
     * Creates a new Instance.
     * 
     * @param start The starting position
     * @param end The end position
     */
    public TextRegion(int start, int end) {
        super();
        this.start = start;
        this.end = end;
    }

    /**
     * Returns the start position for an examined String
     * 
     * @return start
     */
    public int getStart() {
        return start;
    }

    /**
     * Returns the end position for an examined String
     * 
     * @return end
     */
    public int getEnd() {
        return end;
    }

    /**
     * Creates a new text region that is moved by the specified offset. The starting position of the
     * new text region is <code>start + offset</code> the ending position is at
     * <code>end + offset</code>.
     * 
     * @param offset The offset by which the new {@link TextRegion} is moved.
     * @return The new {@link TextRegion} moved by the offset
     */
    public TextRegion offset(int offset) {
        return new TextRegion(start + offset, end + offset);
    }

    /**
     * Creates a new text region thats start position is moved by the specified offset. The starting
     * position of the new text region is <code>start + offset</code> the ending position stays the
     * same.
     * 
     * @param offset The offset by which the start of the new {@link TextRegion} is moved.
     * @return The new {@link TextRegion} with the moved starting position
     */
    public TextRegion startOffset(int offset) {
        return new TextRegion(start + offset, end);
    }

    /**
     * Creates a new text region thats end position is moved by the specified offset. The starting
     * position stays the same the ending position is <code>end + offset</code>.
     * 
     * @param offset The offset by which the end of the new {@link TextRegion} is moved.
     * @return The new {@link TextRegion} with the moved ending position
     */
    public TextRegion endOffset(int offset) {
        return new TextRegion(start, end + offset);
    }

    /**
     * Replaces a part of the given input string with the replacement string. The region to be
     * replaced is defined by "start" and "end" values of this {@link TextRegion}. If the positions
     * are invalid, the method will return the input String without any changes.
     * 
     * @param inputString The string of which a region should be replaced
     * @param replacementString the string that replaces a region in the input string.
     * @return the resulting string
     */
    public String replaceTextRegion(String inputString, String replacementString) {
        if (!isValidStartAndEnd(inputString)) {
            return inputString;
        }
        return inputString.substring(0, getStart()) + replacementString + inputString.substring(getEnd());
    }

    private boolean isValidStartAndEnd(String completeIdentifierString) {
        if (isInitParametersValid() && getEnd() <= completeIdentifierString.length()) {
            return true;
        }
        return false;
    }

    private boolean isInitParametersValid() {
        if (getStart() >= 0 && getStart() <= getEnd()) {
            return true;
        }
        return false;
    }

    /**
     * Returns the substring that is defined by this text region within the input string. If the
     * text region is invalid the inputString is returned without any changes.
     * <p>
     * For example in string "abc123" you get the following results:
     * <ul>
     * <li>Text region 1 to 5: <b>bc12</b></li>
     * <li>Text region 1 to -5: <b>abc123</b></li>
     * </ul>
     * 
     * @param inputString The input from which you want to get the substring defined by this region
     * @return The string that is the defined substring of the input string.
     */
    public String getSubstring(String inputString) {
        if (!isValidStartAndEnd(inputString)) {
            return inputString;
        }
        return inputString.substring(getStart(), getEnd());
    }

    @Override
    public int compareTo(TextRegion o) {
        int compareStart = getStart() - o.getStart();
        if (compareStart == 0) {
            return getEnd() - o.getEnd();
        } else {
            return compareStart;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + end;
        result = prime * result + start;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        TextRegion other = (TextRegion)obj;
        if (end != other.end) {
            return false;
        }
        if (start != other.start) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "TextRegion [start=" + start + ", end=" + end + "]"; //$NON-NLS-1$//$NON-NLS-2$ //$NON-NLS-3$
    }

}
