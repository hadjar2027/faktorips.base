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

package org.faktorips.devtools.core.internal.model.ipsproject;

import java.util.Map;

import org.faktorips.devtools.core.model.ipsproject.IIpsArtefactBuilderSetConfig;
import org.faktorips.util.ArgumentCheck;

/**
 * The default implementation of the IIpsArtefactBuilderSetConfig interface
 * 
 * @author Peter Erzberger
 */
public class IpsArtefactBuilderSetConfig implements IIpsArtefactBuilderSetConfig {

    private Map properties;
    
    /**
     * This constructor is for test purposes only.
     */
    public IpsArtefactBuilderSetConfig(Map properties) {
        ArgumentCheck.notNull(properties);
        this.properties = properties;
    }
    
    /**
     * {@inheritDoc}
     */
    public Object getPropertyValue(String propertyName) {
        return properties.get(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    public String[] getPropertyNames() {
        return (String[])properties.keySet().toArray(new String[properties.size()]);
    }

    /**
     * {@inheritDoc}
     */
    public Boolean getPropertyValueAsBoolean(String propertyName) {
        return (Boolean)getPropertyValue(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    public Integer getPropertyValueAsInteger(String propertyName) {
        return (Integer)getPropertyValue(propertyName);
    }

    /**
     * {@inheritDoc}
     */
    public String getPropertyValueAsString(String propertyName) {
        return (String)getPropertyValue(propertyName);
    }

}
