/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.team.compare.productcmpt;

import org.eclipse.compare.structuremergeviewer.IStructureComparator;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectGeneration;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.ui.team.compare.AbstractCompareItemCreator;

/**
 * Creates a structure of <code>ProductCmptCompareItems</code> that is used for comparing
 * <code>ProductCmpt</code>s.
 * 
 * @author Stefan Widmaier
 */
public class ProductCmptCompareItemCreator extends AbstractCompareItemCreator {

    public ProductCmptCompareItemCreator() {
        super();
    }

    /**
     * Returns the title for the structure-differences viewer. {@inheritDoc}
     */
    @Override
    public String getName() {
        return Messages.ProductCmptCompareItemCreator_StructureViewer_title;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Creates a structure/tree of <code>ProductCmptCompareItem</code>s from the given
     * <code>IIpsSrcFile</code> to represent an <code>IProductCmpt</code>. The
     * <code>IIpsSrcFile</code>, the <code>IProductCmpt</code>, its
     * <code>IProductCmptGeneration</code>s and all contained <code>IConfigElement</code>s and
     * <code>IRelation</code>s are represented by a <code>ProductCmptCompareItem</code>.
     * <p>
     * The returned <code>ProductCmptCompareItem</code> is the root of the created structure and
     * contains the given <code>IIpsSrcFile</code>. It has exactly one child representing (and
     * referencing) the <code>IProductCmpt</code> contained in the srcfile. This
     * <code>ProductCmptCompareItem</code> has a child for each generation the productcomponent
     * posesses. Each generation-compareitem contains multiple <code>ProductCmptCompareItem</code>s
     * representing the attributes (<code>IConfigElement</code>) and relations (
     * <code>IRelation</code>) of the productcomponent (in the current generation).
     * 
     */
    @Override
    protected IStructureComparator getStructureForIpsSrcFile(IIpsSrcFile file) {
        try {
            if (file.getIpsObject() instanceof IProductCmpt) {
                ProductCmptCompareItem root = new ProductCmptCompareItem(null, file);
                IProductCmpt productCmpt = (IProductCmpt)file.getIpsObject();
                ProductCmptCompareItem productCmptItem = new ProductCmptCompareItem(root, productCmpt);

                IIpsElement[] children = productCmpt.getChildren();
                for (IIpsElement element : children) {
                    if (element instanceof IIpsObjectGeneration) {
                        continue;
                    } else if (element instanceof IDescription) {
                        continue;
                    }
                    new ProductCmptCompareItem(productCmptItem, element);
                }

                // Generations of product
                IIpsObjectGeneration[] gens = productCmpt.getGenerationsOrderedByValidDate();
                for (IIpsObjectGeneration gen : gens) {
                    ProductCmptCompareItem generationItem = new ProductCmptCompareItem(productCmptItem, gen);
                    IIpsElement[] genChildren = gen.getChildren();
                    for (IIpsElement element : genChildren) {
                        new ProductCmptCompareItem(generationItem, element);
                    }
                }
                // create the name, root document and ranges for all nodes
                root.init();
                return root;
            }
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }
        return null;
    }

}
