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

package org.faktorips.devtools.htmlexport.pages.elements.core;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.htmlexport.generators.ILayouter;

/**
 * {@link AbstractRootPageElement} is the abstract implementation of the root of the page
 * 
 * @author dicker
 * 
 */
public abstract class AbstractRootPageElement extends AbstractCompositePageElement {

    /*
     * TODO Liste fuellen in abgeleiteten Seiten
     */
    private final List<IpsStatus> ipsStatusList = new ArrayList<IpsStatus>();

    @Override
    public void build() {
        createId();
        subElements = new ArrayList<PageElement>();
    }

    @Override
    public void acceptLayouter(ILayouter layouter) {
        layouter.layoutRootPageElement(this);
    }

    /**
     * @return path to the root (used for setting the right relative path in links from the page
     */
    public abstract String getPathToRoot();

    protected void addIpsStatus(IpsStatus ipsStatus) {
        ipsStatusList.add(ipsStatus);
    }

    protected List<IpsStatus> getIpsStatusList() {
        return ipsStatusList;
    }
}
