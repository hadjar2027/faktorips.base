/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/f10-org:lizenzen:community eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.util;

/**
 * A utility class for string.
 * 
 * @author Peter Erzberger
 */
public class StringUtils {

    private StringUtils(){
    }
    
    /**
     * Wraps the provide text according to the split length and tabSize. 
     * 
     * @param text the text that needs to be wrapped up
     * @param length the length after which a line break should occur
     * @param lineSeparator the line separator that is used for the wrapped text
     * @return the wrapped up text
     */
    public final static String wrapText(String text, int length, String lineSeparator) {

        if (text == null || "".equals(text)) {
            return text;
        }
        String[] lines = text.split(lineSeparator);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < lines.length; i++) {
            String lineText = lines[i];
            while (lineText.length() > length) {
                int index = lineText.indexOf(' ', length);
                if (index != -1) {
                    buf.append(lineText.substring(0, index));
                    if (lineText.length() > index + 1) {
                        buf.append(lineSeparator);
                        lineText = lineText.substring(index + 1, lineText.length());
                    } else {
                        lineText = "";
                        break;
                    }
                } else {
                    break;
                }
            }
            buf.append(lineText);
            if(i+1 < lines.length && !"".equals(lines[i+1])){
                buf.append(lineSeparator);
            }
        }
        return buf.toString();
    }

}
