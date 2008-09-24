/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;

/**
 * 
 * @author Jan Ortmann
 */
public class ArchiveIpsSrcFileTest extends AbstractIpsPluginTest {

    private IIpsProject project;
    private IIpsPackageFragmentRoot root;
    private IIpsPackageFragment pack;
    private IIpsSrcFile srcFile;
    private IPolicyCmptType originalType; 
    
    /*
     * @see AbstractIpsPluginTest#setUp()
     */
    protected void setUp() throws Exception {
        super.setUp();
        IIpsProject archiveProject = newIpsProject("ArchiveProject");
        originalType = newPolicyCmptType(archiveProject, "motor.Policy");
        originalType.newPolicyCmptTypeAttribute();
        originalType.getIpsSrcFile().save(true, null);
        
        project = newIpsProject();
        IFile archiveFile = project.getProject().getFile("test.ipsar");
        IPath archivePath = archiveFile.getLocation();
        createArchive(archiveProject, archiveFile);
        
        IIpsObjectPath path = project.getIpsObjectPath();
        path.newArchiveEntry(archivePath);
        project.setIpsObjectPath(path);
        
        root = project.getIpsPackageFragmentRoots()[1];
        pack = root.getIpsPackageFragment("motor");
        srcFile = pack.getIpsSrcFile(IpsObjectType.POLICY_CMPT_TYPE.getFileName("Policy"));
    }
    
    public void testExists() {
        assertTrue(srcFile.exists());
        
        srcFile = pack.getIpsSrcFile(IpsObjectType.POLICY_CMPT_TYPE.getFileName("Unknown"));
        assertFalse(srcFile.exists());
    }
    
    public void testGetIpsObject() throws CoreException {
        assertNotNull(srcFile.getIpsObject());
        IPolicyCmptType type = (IPolicyCmptType)srcFile.getIpsObject();
        assertEquals(originalType.getProductCmptType(), type.getProductCmptType());
        assertEquals(1, type.getNumOfAttributes());
    }
    
    public void testGetParent() {
        assertEquals(pack, srcFile.getParent());
    }

    public void testGetIpsPackageFragment() {
        assertEquals(pack, srcFile.getIpsPackageFragment());
    }

    public void testGetCorrespondingFile() {
        assertNull(srcFile.getCorrespondingFile());
    }

    public void testGetCorrespondingResource() {
        assertNull(srcFile.getCorrespondingResource());
    }

    public void testGetContentFromEnclosingResource() throws CoreException {
        assertNotNull(srcFile.getContentFromEnclosingResource());
    }

    public void testGetEnclosingResource() {
        assertEquals(root.getCorrespondingResource(), srcFile.getEnclosingResource());
    }
    

}
