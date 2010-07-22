/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.datatype;

import org.faktorips.util.message.MessageList;

/**
 * Abstract super class for Datatype implementations.
 * 
 * @author Jan Ortmann
 */
public abstract class AbstractDatatype implements Datatype {

    public MessageList checkReadyToUse() {
        return new MessageList();
    }

    public boolean isVoid() {
        return false;
    }

    public boolean isEnum() {
        return this instanceof EnumDatatype;
    }

    @Override
    public int hashCode() {
        return getName().hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Datatype)) {
            return false;
        }
        return getQualifiedName().equals(((Datatype)o).getQualifiedName());
    }

    /**
     * Returns the type's name.
     */
    @Override
    public String toString() {
        return getQualifiedName();
    }

    /**
     * Compares the two type's alphabetically by their name.
     */
    public int compareTo(Datatype o) {
        Datatype type = o;
        return getQualifiedName().compareTo(type.getQualifiedName());
    }

    public boolean hasNullObject() {
        return false;
    }

}
