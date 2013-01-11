/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.editors.productcmpt.link;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptLink;

/**
 * This Class filters empty associations of an {@link IProductCmptGeneration}.
 * <p>
 * It works, when an association has an {@link IProductCmptGeneration} as parentElement in the
 * Viewer. The association must be represented by an {@link AbstractAssociationViewItem}.
 * </p>
 * 
 * @author dicker
 */
public class EmptyAssociationFilter extends ViewerFilter {

    @Override
    public boolean select(Viewer viewer, Object parentElement, Object element) {
        if (parentElement instanceof IProductCmptGeneration && element instanceof AbstractAssociationViewItem) {
            IProductCmptGeneration generation = (IProductCmptGeneration)parentElement;
            AbstractAssociationViewItem associationViewItem = (AbstractAssociationViewItem)element;
            IProductCmptLink[] links = generation.getLinks(associationViewItem.getAssociationName());
            return links.length != 0;
        }
        return true;
    }
}