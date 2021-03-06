/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsproject;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.osgi.util.ManifestElement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class IpsBundleManifestTest {

    private static final String MY_BASE_PACKAGE = "myBasePackage";

    private static final String MY_BASE_PACKAGE2 = "myBasePackage2";

    private static final String MY_UNIQUE_QUALIFIER = "myQualifier";

    private static final String MY_UNIQUE_QUALIFIER2 = "myQualifier2";

    private static final String MY_OBJECT_DIR = "myObjectdir";

    private static final String INVALID_OBJECT_DIR = "invalidObjectdir";

    private static final String MY_SRC_OUT = "mySrcOut";

    private static final String MY_SRC_OUT2 = "mySrcOut2";

    private static final String MY_RESOURCE_OUT = "myResourceOut";

    private static final String MY_RESOURCE_OUT2 = "myResourceOut2";

    private static final String MY_TOC = "myToc";

    private static final String MY_MESSAGES = "myMessages";

    @Mock
    private Manifest manifest;

    private IpsBundleManifest ipsBundleManifest;

    @Mock
    private IpsProject ipsProject;

    @Before
    public void createIpsBundleManifest() {
        mockManifest();
        mockProject();
        ipsBundleManifest = new IpsBundleManifest(manifest);
    }

    public void mockManifest() {
        Attributes attributes = mock(Attributes.class);
        when(manifest.getMainAttributes()).thenReturn(attributes);
        when(attributes.getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(MY_BASE_PACKAGE);
        when(attributes.getValue(IpsBundleManifest.HEADER_UNIQUE_QUALIFIER)).thenReturn(MY_UNIQUE_QUALIFIER);
        when(attributes.getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(MY_SRC_OUT);
        when(attributes.getValue(IpsBundleManifest.HEADER_RESOURCE_OUT)).thenReturn(MY_RESOURCE_OUT);
        when(attributes.getValue(IpsBundleManifest.HEADER_OBJECT_DIR)).thenReturn(
                MY_OBJECT_DIR + " ; toc=\"" + MY_TOC + "\";messages=\"" + MY_MESSAGES + "\"");

        Attributes attributesForObjectDir = mock(Attributes.class);
        when(manifest.getAttributes(MY_OBJECT_DIR)).thenReturn(attributesForObjectDir);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(MY_BASE_PACKAGE2);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_UNIQUE_QUALIFIER)).thenReturn(
                MY_UNIQUE_QUALIFIER2);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(MY_SRC_OUT2);
        when(attributesForObjectDir.getValue(IpsBundleManifest.HEADER_RESOURCE_OUT)).thenReturn(MY_RESOURCE_OUT2);
    }

    public void mockProject() {
        IProject project = mock(IProject.class);
        when(ipsProject.getProject()).thenReturn(project);
        IFolder folder = mock(IFolder.class);
        when(project.getFolder(anyString())).thenReturn(folder);
    }

    @Test
    public void testGetBasePackage() {
        when(manifest.getMainAttributes().getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(
                " " + MY_BASE_PACKAGE + " ");

        String basePackage = ipsBundleManifest.getBasePackage();

        assertEquals(MY_BASE_PACKAGE, basePackage);
    }

    @Test
    public void testGetBasePackage_trim() {
        String basePackage = ipsBundleManifest.getBasePackage();

        assertEquals(MY_BASE_PACKAGE, basePackage);
    }

    @Test
    public void testGetBasePackage_forObjectDir() {
        String basePackage = ipsBundleManifest.getBasePackage(MY_OBJECT_DIR);

        assertEquals(MY_BASE_PACKAGE2, basePackage);
    }

    @Test
    public void testGetBasePackage_forObjectDirTrim() {
        when(manifest.getAttributes(MY_OBJECT_DIR).getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(
                " " + MY_BASE_PACKAGE2 + " ");

        String basePackage = ipsBundleManifest.getBasePackage(MY_OBJECT_DIR);

        assertEquals(MY_BASE_PACKAGE2, basePackage);
    }

    @Test
    public void testGetBasePackage_forInvalidObjectDir() {
        String basePackage = ipsBundleManifest.getBasePackage(INVALID_OBJECT_DIR);

        assertEquals(MY_BASE_PACKAGE, basePackage);
    }

    @Test
    public void testGetUniqueQualifier() {
        String uniqueQualifier = ipsBundleManifest.getUniqueQualifier();

        assertEquals(MY_UNIQUE_QUALIFIER, uniqueQualifier);
    }

    @Test
    public void testGetUniqueQualifier_trim() {
        when(manifest.getMainAttributes().getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(
                " " + MY_UNIQUE_QUALIFIER + " ");

        String uniqueQualifier = ipsBundleManifest.getUniqueQualifier();

        assertEquals(MY_UNIQUE_QUALIFIER, uniqueQualifier);
    }

    @Test
    public void testGetUniqueQualifier_forObjectDir() {
        String uniqueQualifier = ipsBundleManifest.getUniqueQualifier(MY_OBJECT_DIR);

        assertEquals(MY_UNIQUE_QUALIFIER2, uniqueQualifier);
    }

    @Test
    public void testGetUniqueQualifier_forObjectDirTrim() {
        when(manifest.getAttributes(MY_OBJECT_DIR).getValue(IpsBundleManifest.HEADER_BASE_PACKAGE)).thenReturn(
                " " + MY_UNIQUE_QUALIFIER2 + " ");

        String uniqueQualifier = ipsBundleManifest.getUniqueQualifier(MY_OBJECT_DIR);

        assertEquals(MY_UNIQUE_QUALIFIER2, uniqueQualifier);
    }

    @Test
    public void testGetUniqueQualifier_forInvalidObjectDir() {
        String uniqueQualifier = ipsBundleManifest.getUniqueQualifier(INVALID_OBJECT_DIR);

        assertEquals(MY_UNIQUE_QUALIFIER, uniqueQualifier);
    }

    @Test
    public void testGetSourcecodeOutput() {
        String srcOutput = ipsBundleManifest.getSourcecodeOutput();

        assertEquals(MY_SRC_OUT, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_forObjectDir() {
        String srcOutput = ipsBundleManifest.getSourcecodeOutput(MY_OBJECT_DIR);

        assertEquals(MY_SRC_OUT2, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_trim() {
        when(manifest.getMainAttributes().getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(MY_SRC_OUT + " ");

        String srcOutput = ipsBundleManifest.getSourcecodeOutput();

        assertEquals(MY_SRC_OUT, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_objectDirAndTrim() {
        when(manifest.getAttributes(MY_OBJECT_DIR).getValue(IpsBundleManifest.HEADER_SRC_OUT)).thenReturn(
                MY_SRC_OUT2 + " ");

        String srcOutput = ipsBundleManifest.getSourcecodeOutput(MY_OBJECT_DIR);

        assertEquals(MY_SRC_OUT2, srcOutput);
    }

    @Test
    public void testGetSourcecodeOutput_forInvalidObjectDir() {
        String srcOutput = ipsBundleManifest.getSourcecodeOutput(INVALID_OBJECT_DIR);

        assertEquals(MY_SRC_OUT, srcOutput);
    }

    @Test
    public void testGetResourceOutput() {
        String srcOutput = ipsBundleManifest.getResourceOutput();

        assertEquals(MY_RESOURCE_OUT, srcOutput);
    }

    @Test
    public void testGetResourceOutput_trim() {
        when(manifest.getMainAttributes().getValue(IpsBundleManifest.HEADER_RESOURCE_OUT)).thenReturn(
                MY_RESOURCE_OUT + " ");

        String srcOutput = ipsBundleManifest.getResourceOutput();

        assertEquals(MY_RESOURCE_OUT, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forObjectDir() {
        String srcOutput = ipsBundleManifest.getResourceOutput(MY_OBJECT_DIR);

        assertEquals(MY_RESOURCE_OUT2, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forObjectDirTrim() {
        when(manifest.getAttributes(MY_OBJECT_DIR).getValue(IpsBundleManifest.HEADER_RESOURCE_OUT)).thenReturn(
                MY_RESOURCE_OUT2 + " ");

        String srcOutput = ipsBundleManifest.getResourceOutput(MY_OBJECT_DIR);

        assertEquals(MY_RESOURCE_OUT2, srcOutput);
    }

    @Test
    public void testGetResourceOutput_forInvalidObjectDir() {
        String srcOutput = ipsBundleManifest.getResourceOutput(INVALID_OBJECT_DIR);

        assertEquals(MY_RESOURCE_OUT, srcOutput);
    }

    @Test
    public void testGetObjectDirs() {
        List<IPath> objectDir = ipsBundleManifest.getObjectDirs();

        assertEquals(1, objectDir.size());
        assertEquals(new Path(MY_OBJECT_DIR), objectDir.get(0));
    }

    @Test
    public void testGetObjectDirElements_empty() {
        ipsBundleManifest = new IpsBundleManifest(mock(Manifest.class));
        ManifestElement[] objectDir = ipsBundleManifest.getObjectDirElements();

        assertEquals(0, objectDir.length);
    }

    @Test
    public void testGetObjectDirElements_noValue() {
        Attributes attributes = mock(Attributes.class);
        when(manifest.getMainAttributes()).thenReturn(attributes);
        ManifestElement[] objectDir = ipsBundleManifest.getObjectDirElements();

        assertEquals(0, objectDir.length);
    }

    @Test
    public void testGetObjectDirElements() {
        ManifestElement[] objectDir = ipsBundleManifest.getObjectDirElements();

        assertEquals(1, objectDir.length);
        assertEquals(1, objectDir[0].getValueComponents().length);
        assertEquals(MY_OBJECT_DIR, objectDir[0].getValue());
    }

    @Test
    public void testHasObjectDirs() {
        List<IPath> objectDir = ipsBundleManifest.getObjectDirs();

        assertEquals(1, objectDir.size());
        assertEquals(new Path(MY_OBJECT_DIR), objectDir.get(0));
        assertEquals(true, ipsBundleManifest.hasObjectDirs());
    }

    @Test
    public void testHasObjectDirs_NoObjectDirs() {
        ipsBundleManifest = new IpsBundleManifest(mock(Manifest.class));
        List<IPath> objectDir = ipsBundleManifest.getObjectDirs();

        assertEquals(0, objectDir.size());
        assertEquals(false, ipsBundleManifest.hasObjectDirs());
    }

    @Test
    public void testGetTocPath() {
        ManifestElement objectDirElement = ipsBundleManifest.getObjectDirElements()[0];

        String toc = ipsBundleManifest.getTocPath(objectDirElement);

        assertEquals(MY_TOC, toc);
    }

    @Test
    public void testGetValidationMessagesBundle() {
        ManifestElement objectDirElement = ipsBundleManifest.getObjectDirElements()[0];

        String messages = ipsBundleManifest.getValidationMessagesBundle(objectDirElement);

        assertEquals(MY_MESSAGES, messages);
    }

}
