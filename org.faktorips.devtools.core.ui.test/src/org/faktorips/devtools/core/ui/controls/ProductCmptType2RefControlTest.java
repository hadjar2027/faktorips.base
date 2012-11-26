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

package org.faktorips.devtools.core.ui.controls;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.swt.widgets.Shell;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.junit.Test;

public class ProductCmptType2RefControlTest extends AbstractIpsPluginTest {

    @Test
    public void testGetSrcFiles_SingleProject() throws CoreException {
        IIpsProject project = newIpsProject("BaseProject");

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");
        IProductCmptType secondProductCmptType = newProductCmptType(project, "SecondProductType");
        secondProductCmptType.setAbstract(true);

        IpsObjectRefControl productCmptRefControl = new ProductCmptType2RefControl(project, new Shell(), new UIToolkit(
                null), false);

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertTrue(list.contains(productCmptType.getIpsSrcFile()));
        assertTrue(list.contains(secondProductCmptType.getIpsSrcFile()));

        assertEquals(2, list.size());
    }

    @Test
    public void testGetSrcFiles_ExcludeAbstract() throws CoreException {
        IIpsProject project = newIpsProject("BaseProject");

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");
        productCmptType.setAbstract(false);
        IProductCmptType secondProductCmptType = newProductCmptType(project, "SecondProductType");
        secondProductCmptType.setAbstract(true);

        IpsObjectRefControl productCmptRefControl = new ProductCmptType2RefControl(project, new Shell(), new UIToolkit(
                null), true);

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertTrue(list.contains(productCmptType.getIpsSrcFile()));
        assertFalse(list.contains(secondProductCmptType.getIpsSrcFile()));

        assertEquals(1, list.size());
    }

    @Test
    public void testGetSrcFiles_MultiProject() throws CoreException {
        IIpsProject project = newIpsProject("BaseProject");
        IIpsProject subProject = newIpsProject("SubProject");
        IIpsProject anotherProject = newIpsProject("AnotherProject");

        IIpsObjectPath subIpsObjectPath = subProject.getIpsObjectPath();
        subIpsObjectPath.newIpsProjectRefEntry(project);
        subProject.setIpsObjectPath(subIpsObjectPath);

        IIpsObjectPath anotherIpsObjectPath = anotherProject.getIpsObjectPath();
        anotherIpsObjectPath.newIpsProjectRefEntry(project);
        anotherProject.setIpsObjectPath(anotherIpsObjectPath);

        List<IIpsProject> projects = Arrays.asList(project, subProject);

        IProductCmptType productCmptType = newProductCmptType(project, "ProductType");

        IProductCmptType subProductCmptType = newProductCmptType(subProject, "SubProductType");

        IProductCmptType anotherProductCmptType = newProductCmptType(anotherProject, "AnotherProduct");

        IpsObjectRefControl productCmptRefControl = new ProductCmptType2RefControl(projects, new Shell(),
                new UIToolkit(null), false);

        List<IIpsSrcFile> list = Arrays.asList(productCmptRefControl.getIpsSrcFiles());

        assertTrue(list.contains(productCmptType.getIpsSrcFile()));
        assertTrue(list.contains(subProductCmptType.getIpsSrcFile()));
        assertFalse(list.contains(anotherProductCmptType.getIpsSrcFile()));

        assertEquals(2, list.size());
    }
}