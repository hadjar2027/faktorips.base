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

package org.faktorips.devtools.core.internal.model.productcmpt.deltaentries;

import org.faktorips.devtools.core.internal.model.productcmpt.GenerationToTypeDelta;
import org.faktorips.devtools.core.model.productcmpt.DeltaType;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.ProdDefPropertyType;

/**
 * 
 * @author Jan Ortmann
 */
public class ValueWithoutPropertyEntry extends AbstractDeltaEntryForProperty {

    private IPropertyValue value;
    
    public ValueWithoutPropertyEntry(GenerationToTypeDelta delta, IPropertyValue value) {
        super(delta);
        this.value = value;
    }
    
    /**
     * {@inheritDoc}
     */
    public ProdDefPropertyType getPropertyType() {
        return value.getPropertyType();
    }
    
    /**
     * {@inheritDoc}
     */
    public String getPropertyName() {
        return value.getPropertyName();
    }
    
    /**
     * {@inheritDoc}
     */
    public DeltaType getDeltaType() {
        return DeltaType.VALUE_WITHOUT_PROPERTY;
    }

    /**
     * {@inheritDoc}
     */
    public void fix() {
        value.delete();
    }

}
