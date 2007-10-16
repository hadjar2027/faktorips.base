/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Composite;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.product.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;


/**
 *
 */
public class ProductCmptRefControl extends IpsObjectRefControl {

    private boolean includeCmptsForSubtypes = true;
    private IProductCmpt[] toExclude = new IProductCmpt[0];
    private IProductCmptType productCmptType;
    
    
    public ProductCmptRefControl(
            IIpsProject project, 
            Composite parent, 
            UIToolkit toolkit) {
        super(project, parent, toolkit, Messages.ProductCmptRefControl_title, Messages.ProductCmptRefControl_description);
    }
    
    /**
     * @param qPcTypeNmae The product component type for which product components should be selectable.
     * @param includeCmptsForSubtypes <code>true</code> if also product components for subtypes should be selectable.
     */
    public void setProductCmptType(IProductCmptType productCmptType, boolean includeCmptsForSubtypes) {
        this.productCmptType = productCmptType;
        this.includeCmptsForSubtypes = includeCmptsForSubtypes;
    }
    
	/**
     * {@inheritDoc}
     */
    protected IIpsSrcFile[] getIpsSrcFiles() throws CoreException {
        if (getIpsProject()==null) {
            return new IIpsSrcFile[0];
        }

        IIpsSrcFile[] ipsSrcFiles = getIpsProject().findAllProductCmptSrcFiles(productCmptType, includeCmptsForSubtypes);
        List result = new ArrayList(ipsSrcFiles.length);
        for (int i = 0; i < ipsSrcFiles.length; i++) {
            result.add(ipsSrcFiles[i]);
        }
        if (result.size() > 0){
            for (int i = 0; i < toExclude.length; i++) {
                if (toExclude[i] != null){
                    result.remove(toExclude[i].getIpsSrcFile());
                }
            }
        }
        
        return (IIpsSrcFile[])result.toArray(new IIpsSrcFile[result.size()]);
    }

    /**
	 * Set all product components to exclude from result.
	 * 
	 * @param cmpts All product components to exclude. 
	 */
	public void setProductCmptsToExclude(IProductCmpt[] cmpts) {
		if (cmpts == null) {
			toExclude = new IProductCmpt[0];
		}
		else {
			toExclude = cmpts;
		}
	}
}
