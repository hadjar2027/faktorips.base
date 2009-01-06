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

package org.faktorips.devtools.core.model.productcmpt.treestructure;

import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAssociation;

/**
 * A reference to a <code>IProductCmptTypeRelation</code> which is used in the 
 * <code>IProductCmptStructure</code>
 * 
 * @author Thorsten Guenther
 */
public interface IProductCmptTypeRelationReference extends IProductCmptStructureReference {

	/**
	 * Returns the <code>IProductCmptTypeAssociation</code> this reference refers to.
	 */
	public IProductCmptTypeAssociation getRelation();
	
}
