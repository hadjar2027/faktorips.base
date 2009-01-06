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

package org.faktorips.devtools.core.model.productcmpttype;

import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.model.valueset.IValueSetOwner;

/**
 * An attribute of a product component type.
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public interface IProductCmptTypeAttribute extends IAttribute, IValueSetOwner, IProdDefProperty {

    /**
     * Returns the product component type the attribute belongs to.
     */
    public IProductCmptType getProductCmptType();
    
    /**
     * Creates a copy of the given value set and assigns it to this attribute. 
     */
    public void setValueSetCopy(IValueSet source);
    
}
