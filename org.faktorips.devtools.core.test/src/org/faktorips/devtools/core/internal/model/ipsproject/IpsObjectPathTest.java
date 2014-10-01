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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchive;
import org.faktorips.devtools.core.model.ipsproject.IIpsArchiveEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsContainerEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPathEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectRefEntry;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Jan Ortmann
 */
public class IpsObjectPathTest extends AbstractIpsPluginTest {

    private static final String MY_RESOURCE_PATCH = "myResourcePatch";
    private IIpsProject ipsProject;
    private IpsObjectPath path;
    private IIpsPackageFragmentRoot refProject2Root;
    private IIpsProject refProject;
    private IIpsProject refProject2;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = this.newIpsProject("TestProject");
        path = (IpsObjectPath)ipsProject.getIpsObjectPath();
    }

    @Test
    public void testGetEntry() throws CoreException {
        assertNull(path.getEntry(null));
        assertNull(path.getEntry("unknown"));

        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        IIpsObjectPathEntry entry0 = path.newSourceFolderEntry(srcFolder);

        path.newIpsProjectRefEntry(newIpsProject("Project2"));

        IFile archiveFile = ipsProject.getProject().getFile("archive.jar");
        IIpsObjectPathEntry entry2 = path.newArchiveEntry(archiveFile.getLocation());

        assertEquals(entry0, path.getEntry("src"));
        assertNull(path.getEntry("Project2"));
        assertEquals(entry2, path.getEntry("archive.jar"));

        assertNull(path.getEntry("unknwon"));
        assertNull(path.getEntry(null));
    }

    @Test
    public void testNewSrcFolderEntry() {
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        IIpsSrcFolderEntry entry0 = path.newSourceFolderEntry(srcFolder);
        assertEquals(path, entry0.getIpsObjectPath());
        // default test project contains already 1 entry
        assertEquals(2, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);

        IIpsSrcFolderEntry entry1 = path.newSourceFolderEntry(srcFolder);
        assertEquals(path, entry1.getIpsObjectPath());
        assertEquals(3, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry1, path.getEntries()[2]);
    }

    @Test
    public void testNewProjectRefEntry() throws CoreException {
        IIpsProjectRefEntry entry0 = path.newIpsProjectRefEntry(ipsProject);
        assertEquals(path, entry0.getIpsObjectPath());
        // default test project contains already 1 entry
        assertEquals(2, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);

        IIpsProjectRefEntry entry1 = path.newIpsProjectRefEntry(ipsProject);
        assertEquals(path, entry1.getIpsObjectPath());
        // the same project should not be added twice
        assertEquals(2, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry1, path.getEntries()[1]);

        IIpsProjectRefEntry entry2 = path.newIpsProjectRefEntry(this.newIpsProject("TestProject2"));
        assertEquals(path, entry2.getIpsObjectPath());
        assertEquals(3, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry2, path.getEntries()[2]);
    }

    @Test
    public void testContainsProjectRefEntry() throws CoreException {
        path.newIpsProjectRefEntry(ipsProject);
        assertTrue(path.containsProjectRefEntry(ipsProject));

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(path.containsProjectRefEntry(ipsProject2));

        path.removeProjectRefEntry(ipsProject);
        assertFalse(path.containsProjectRefEntry(ipsProject));
    }

    @Test
    public void testRemoveProjectRefEntry() throws CoreException {
        IIpsProjectRefEntry entry0 = path.newIpsProjectRefEntry(ipsProject);
        assertEquals(path, entry0.getIpsObjectPath());
        // default test project contains already 1 entry
        assertEquals(2, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertTrue(path.containsProjectRefEntry(ipsProject));
        path.removeProjectRefEntry(ipsProject);
        assertFalse(path.containsProjectRefEntry(ipsProject));
        assertEquals(1, path.getEntries().length);

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(path.containsProjectRefEntry(ipsProject2));
        path.removeProjectRefEntry(ipsProject2);
        assertFalse(path.containsProjectRefEntry(ipsProject2));
        assertEquals(1, path.getEntries().length);
    }

    @Test
    public void testContainsArchiveEntry() throws Exception {
        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(ipsProject, archiveFile);
        IIpsArchive ipsArchive = path.newArchiveEntry(archiveFile.getLocation()).getIpsArchive();
        assertTrue(path.containsArchiveEntry(ipsArchive));

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(ipsProject2.getIpsObjectPath().containsArchiveEntry(ipsArchive));

        path.removeArchiveEntry(ipsArchive);
        assertFalse(path.containsArchiveEntry(ipsArchive));
    }

    @Test
    public void testRemoveArchiveEntry() throws Exception {
        IFile archiveFile = ipsProject.getProject().getFile("test.ipsar");
        createArchive(ipsProject, archiveFile);
        IIpsArchiveEntry entry0 = path.newArchiveEntry(archiveFile.getLocation());
        IIpsArchive archive0 = entry0.getIpsArchive();
        assertEquals(path, entry0.getIpsObjectPath());
        // default test project contains already 1 entry
        assertEquals(2, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertTrue(path.containsArchiveEntry(entry0.getIpsArchive()));
        path.removeArchiveEntry(archive0);
        assertEquals(1, path.getEntries().length);
        path.removeArchiveEntry(archive0);
        assertEquals(1, path.getEntries().length);
    }

    @Test
    public void testContainsSrcFolderEntry() throws CoreException {
        IFolder folder = ipsProject.getProject().getFolder("testfolder");
        path.newSourceFolderEntry(folder);
        assertTrue(path.containsSrcFolderEntry(folder));

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(ipsProject2.getIpsObjectPath().containsSrcFolderEntry(folder));

        path.removeSrcFolderEntry(folder);
        assertFalse(path.containsSrcFolderEntry(folder));
    }

    @Test
    public void testRemoveSrcFolderEntry() throws CoreException {
        IFolder folder = ipsProject.getProject().getFolder("testfolder");
        IIpsSrcFolderEntry entry0 = path.newSourceFolderEntry(folder);
        assertEquals(path, entry0.getIpsObjectPath());
        // default test project contains already 1 entry
        assertEquals(2, path.getEntries().length);
        assertEquals(entry0, path.getEntries()[1]);
        assertTrue(path.containsSrcFolderEntry(folder));
        path.removeSrcFolderEntry(folder);
        assertFalse(path.containsSrcFolderEntry(folder));
        assertEquals(1, path.getEntries().length);

        IIpsProject ipsProject2 = this.newIpsProject("TestProject2");
        assertFalse(ipsProject2.getIpsObjectPath().containsSrcFolderEntry(folder));
        path.removeSrcFolderEntry(folder);
        assertFalse(path.containsSrcFolderEntry(folder));
        assertEquals(1, path.getEntries().length);
    }

    @Test
    public void testGetDirectlyReferencedIpsProjects() {
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        refProject = ipsProject.getIpsModel().getIpsProject("RefProject1");
        refProject2 = ipsProject.getIpsModel().getIpsProject("RefProject2");
        path.newIpsProjectRefEntry(refProject);
        path.newSourceFolderEntry(srcFolder);
        path.newIpsProjectRefEntry(refProject2);

        List<IIpsProject> projects = path.getDirectlyReferencedIpsProjects();
        assertEquals(2, projects.size());
        assertEquals(refProject, projects.get(0));
        assertEquals(refProject2, projects.get(1));
    }

    @Test
    public void testGetDirectlyReferencedIpsProjects_refInContainer() {
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        refProject = ipsProject.getIpsModel().getIpsProject("RefProject1");
        refProject2 = ipsProject.getIpsModel().getIpsProject("RefProject2");
        path.newIpsProjectRefEntry(refProject);
        path.newSourceFolderEntry(srcFolder);
        path.newIpsProjectRefEntry(refProject2);
        IIpsObjectPathEntry[] entries = path.getEntries();
        IIpsContainerEntry newContainerEntry = mock(IIpsContainerEntry.class);
        path.setEntries(new IIpsObjectPathEntry[] { newContainerEntry });
        when(newContainerEntry.getIpsProject()).thenReturn(ipsProject);
        when(newContainerEntry.getType()).thenReturn(IIpsObjectPathEntry.TYPE_CONTAINER);
        when(newContainerEntry.isContainer()).thenReturn(true);
        when(newContainerEntry.resolveEntries()).thenReturn(Arrays.asList(entries));

        List<IIpsProject> projects = path.getDirectlyReferencedIpsProjects();

        assertEquals(2, projects.size());
        assertEquals(refProject, projects.get(0));
        assertEquals(refProject2, projects.get(1));
    }

    @Test
    public void testFindAllReferencedIpsProjects_refInContainer() throws CoreException {
        setUpReferencedProjects(false);

        List<IIpsProject> projects = path.getAllReferencedIpsProjects();

        assertEquals(1, projects.size());
        assertEquals(refProject, projects.get(0));
    }

    @Test
    public void testGetProjectRefEntries_container() {
        IFolder srcFolder = ipsProject.getProject().getFolder("src");
        refProject = ipsProject.getIpsModel().getIpsProject("RefProject1");
        refProject2 = ipsProject.getIpsModel().getIpsProject("RefProject2");
        IIpsProjectRefEntry refEntry1 = path.newIpsProjectRefEntry(refProject);
        path.newSourceFolderEntry(srcFolder);
        IIpsProjectRefEntry refEntry2 = path.newIpsProjectRefEntry(refProject2);
        IIpsObjectPathEntry[] entries = path.getEntries();
        IIpsContainerEntry newContainerEntry = mock(IIpsContainerEntry.class);
        path.setEntries(new IIpsObjectPathEntry[] { newContainerEntry });
        when(newContainerEntry.getType()).thenReturn(IIpsObjectPathEntry.TYPE_CONTAINER);
        when(newContainerEntry.isContainer()).thenReturn(true);
        when(newContainerEntry.resolveEntries()).thenReturn(Arrays.asList(entries));

        IIpsProjectRefEntry[] refEntries = path.getProjectRefEntries();

        assertEquals(2, refEntries.length);
        assertEquals(refEntry1, refEntries[0]);
        assertEquals(refEntry2, refEntries[1]);
    }

    @Test
    public void testSearchIpsObjectPath2IpsSrcFile_cleanUpTest() throws Exception {
        refProject2Root = newIpsPackageFragmentRoot(ipsProject, null, "root1");
        newIpsObject(refProject2Root, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.A");
        IpsObjectPathSearchContext searchContext = new IpsObjectPathSearchContext(ipsProject);
        // public void testFindAllReferencedProjectsIpsSrcFile_cleanUpTest() throws Exception {
        // root = newIpsPackageFragmentRoot(ipsProject, null, "root1");
        // newIpsObject(root, IpsObjectType.PRODUCT_CMPT_TYPE, "a.b.A");
        // IpsObjectPathSearchContext searchContext = new IpsObjectPathSearchContext();

        IIpsSrcFile ipsSrcFile1 = ((IpsProject)ipsProject).getIpsObjectPathInternal().findIpsSrcFile(
                new QualifiedNameType("a.b.A", IpsObjectType.PRODUCT_CMPT_TYPE), searchContext);
        IIpsSrcFile ipsSrcFile2 = ((IpsProject)ipsProject).getIpsObjectPathInternal().findIpsSrcFile(
                new QualifiedNameType("a.b.A", IpsObjectType.PRODUCT_CMPT_TYPE), searchContext);
        IIpsSrcFile ipsSrcFile3 = ((IpsProject)ipsProject).getIpsObjectPathInternal().findIpsSrcFile(
                new QualifiedNameType("a.b.A", IpsObjectType.PRODUCT_CMPT_TYPE), searchContext);

        assertSame(ipsSrcFile1, ipsSrcFile2);
        assertSame(ipsSrcFile1, ipsSrcFile3);
    }

    @Test
    public void testGetOutputFolders() {
        IProject project = ipsProject.getProject();
        IpsObjectPath path = new IpsObjectPath(ipsProject);
        IFolder out0 = project.getFolder("out0");
        IFolder ext0 = project.getFolder("ext0");
        path.setOutputFolderForMergableSources(out0);
        path.setOutputFolderForDerivedSources(ext0);

        IIpsSrcFolderEntry entry0 = path.newSourceFolderEntry(project.getFolder("src0"));
        IFolder out1 = project.getFolder("out1");
        entry0.setSpecificOutputFolderForMergableJavaFiles(out1);
        IIpsSrcFolderEntry entry1 = path.newSourceFolderEntry(project.getFolder("src1"));
        IFolder out2 = project.getFolder("out2");
        entry1.setSpecificOutputFolderForMergableJavaFiles(out2);
        IIpsSrcFolderEntry entry2 = path.newSourceFolderEntry(project.getFolder("src1"));
        entry2.setSpecificOutputFolderForMergableJavaFiles(null);
        path.newIpsProjectRefEntry(ipsProject);

        // one output folder for all src folders
        path.setOutputDefinedPerSrcFolder(false);
        IFolder[] outFolders = path.getOutputFolders();
        assertEquals(1, outFolders.length);
        assertEquals(out0, outFolders[0]);

        // one output folder, but it is null
        path.setOutputFolderForMergableSources(null);
        outFolders = path.getOutputFolders();
        assertEquals(0, outFolders.length);

        // output defined per src folder
        path.setOutputDefinedPerSrcFolder(true);
        outFolders = path.getOutputFolders();
        assertEquals(2, outFolders.length);
        assertEquals(out1, outFolders[0]);
        assertEquals(out2, outFolders[1]);
    }

    @Test
    public void testValidate() throws CoreException {
        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.size());

        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();

        // validate missing outputFolderGenerated
        IFolder folder1 = ipsProject.getProject().getFolder("none");
        path.setOutputFolderForMergableSources(folder1);
        path.setOutputDefinedPerSrcFolder(false);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsObjectPathEntry.MSGCODE_MISSING_FOLDER));

        // validate missing outputFolderExtension
        path.setOutputFolderForDerivedSources(folder1);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(2, ml.size());

        // validate missing folders only when general output folder needs to be defined
        path.setOutputDefinedPerSrcFolder(true);
        ipsProject.setProperties(props);
        ml = ipsProject.validate();
        assertEquals(0, ml.size());
    }

    @Test
    public void testValidateOutputFolderMergableAndDerivedEmpty() throws Exception {
        MessageList ml = ipsProject.validate();
        assertEquals(0, ml.size());

        IIpsProjectProperties props = ipsProject.getProperties();
        IIpsObjectPath path = props.getIpsObjectPath();

        ml = ipsProject.validate();
        assertNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED));
        assertNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED));

        path.setOutputDefinedPerSrcFolder(false);
        path.setOutputFolderForMergableSources(null);
        path.setOutputFolderForDerivedSources(null);
        ipsProject.setProperties(props);

        ml = ipsProject.validate();
        assertNotNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_MERGABLE_OUTPUT_FOLDER_NOT_SPECIFIED));
        assertNotNull(ml.getMessageByCode(IIpsObjectPath.MSGCODE_DERIVED_OUTPUT_FOLDER_NOT_SPECIFIED));
    }

    @Test(expected = NullPointerException.class)
    public void testConstructor_WithNull() {
        new IpsObjectPath(null);
        assertSame(ipsProject, path.getIpsProject());
    }

    @Test
    public void testConstructor() {
        path = new IpsObjectPath(ipsProject);
        assertSame(ipsProject, path.getIpsProject());
    }

    @Test
    public void testMoveEntries() throws Exception {
        // default test project contains already 1 entry
        IIpsObjectPathEntry entry0 = path.getEntries()[0];
        IIpsSrcFolderEntry entry1 = path.newSourceFolderEntry(ipsProject.getProject().getFolder("src"));
        IIpsSrcFolderEntry entry2 = path.newSourceFolderEntry(ipsProject.getProject().getFolder("src2"));
        IIpsSrcFolderEntry entry3 = path.newSourceFolderEntry(ipsProject.getProject().getFolder("src3"));

        assertEquals(4, path.getEntries().length);

        // move top two entries one position down
        int[] newIndices = path.moveEntries(new int[] { 0, 1 }, false);
        assertEquals(4, path.getEntries().length);
        assertEquals(2, newIndices.length);
        assertTrue((newIndices[0] == 1) || (newIndices[1] == 1));
        // returned array (no order guaranteed)
        assertTrue((newIndices[0] == 2) || (newIndices[1] == 2));

        assertEquals(entry2, path.getEntries()[0]);
        assertEquals(entry0, path.getEntries()[1]);
        assertEquals(entry1, path.getEntries()[2]);
        assertEquals(entry3, path.getEntries()[3]);

        // now move last three entries one position up
        newIndices = path.moveEntries(new int[] { 3, 1, 2 }, true);
        assertEquals(4, path.getEntries().length);
        assertEquals(3, newIndices.length);
        assertTrue((newIndices[0] == 0) || (newIndices[1] == 0) || (newIndices[2] == 0));
        assertTrue((newIndices[0] == 1) || (newIndices[1] == 1) || (newIndices[2] == 1));
        assertTrue((newIndices[0] == 2) || (newIndices[1] == 2) || (newIndices[2] == 2));

        assertEquals(entry0, path.getEntries()[0]);
        assertEquals(entry1, path.getEntries()[1]);
        assertEquals(entry3, path.getEntries()[2]);
        assertEquals(entry2, path.getEntries()[3]);

        // invalid values should not change the elements order
        newIndices = path.moveEntries(new int[] { -2, 42 }, true);
        assertEquals(entry0, path.getEntries()[0]);
        assertEquals(entry1, path.getEntries()[1]);
        assertEquals(entry3, path.getEntries()[2]);
        assertEquals(entry2, path.getEntries()[3]);

        // invalid values should not change the elements order
        newIndices = path.moveEntries(new int[] { -3, 21 }, false);
        assertEquals(entry0, path.getEntries()[0]);
        assertEquals(entry1, path.getEntries()[1]);
        assertEquals(entry3, path.getEntries()[2]);
        assertEquals(entry2, path.getEntries()[3]);
    }

    @Test
    public void testNewContainerEntry() throws Exception {
        String containerTypeId = "anyContainerId";
        String optionalPath = "anyOptionalPath";
        path.newSourceFolderEntry(ipsProject.getProject().getFolder("anyFolder"));

        IIpsContainerEntry containerEntry = path.newContainerEntry(containerTypeId, optionalPath);

        assertEquals(containerTypeId, containerEntry.getContainerTypeId());
        assertEquals(optionalPath, containerEntry.getOptionalPath());
    }

    @Test
    public void testFindAllReferencedProjectsExistingContainer_noResult() throws Exception {

        assertNull(path.findExistingContainer("containe", null));
    }

    @Test
    public void testFindAllReferencedProjectsExistingContainer_noOptionalPath() throws Exception {
        String containerTypeId = "anyContainerId";
        path.newSourceFolderEntry(ipsProject.getProject().getFolder("anyFolder"));
        IIpsContainerEntry containerEntry = path.newContainerEntry(containerTypeId, null);

        IIpsContainerEntry existingContainer = path.findExistingContainer(containerTypeId, null);

        assertEquals(containerEntry, existingContainer);
    }

    @Test
    public void testFindAllReferencedProjectsExistingContainer_withOptionalPath() throws Exception {
        String containerTypeId = "anyContainerId";
        String optionalPath = "anyOptionalPath";
        path.newSourceFolderEntry(ipsProject.getProject().getFolder("anyFolder"));
        IIpsContainerEntry containerEntry = path.newContainerEntry(containerTypeId, optionalPath);

        IIpsContainerEntry existingContainer = path.findExistingContainer(containerTypeId, optionalPath);

        assertEquals(containerEntry, existingContainer);
    }

    @Test
    public void testContainsResource_empty() throws Exception {
        assertFalse(path.containsResource(MY_RESOURCE_PATCH));
    }

    @Test
    public void testContainsResource_falseForIpsProjectRefEntry() throws Exception {
        setUpReferencedProjects(false);

        createFileWithContent((IFolder)refProject2Root.getCorrespondingResource(), "file.txt", "111");

        assertFalse(path.containsResource("file.txt"));
    }

    @Test
    public void testContainsResource_trueForIpsProjectRefEntry() throws Exception {
        setUpReferencedProjects(true);

        createFileWithContent((IFolder)refProject2Root.getCorrespondingResource(), "file.txt", "111");

        assertTrue(path.containsResource("file.txt"));
    }

    private void setUpReferencedProjects(boolean reexportProjects) throws CoreException {
        refProject = newIpsProject("RefProject");
        refProject2 = newIpsProject("RefProject2");
        // ipsProject = newIpsProject();
        refProject2Root = newIpsPackageFragmentRoot(refProject2, null, "packageFragment");

        IpsObjectPath pathRef = (IpsObjectPath)refProject.getIpsObjectPath();
        IpsObjectPath pathRef2 = (IpsObjectPath)refProject2.getIpsObjectPath();
        IIpsProjectRefEntry projectRefEntry = path.newIpsProjectRefEntry(refProject);
        IIpsProjectRefEntry projectRefEntry2 = pathRef.newIpsProjectRefEntry(refProject2);
        pathRef.newIpsProjectRefEntry(refProject2);

        projectRefEntry.setReexported(reexportProjects);
        projectRefEntry2.setReexported(reexportProjects);
        ipsProject.setIpsObjectPath(path);
        refProject.setIpsObjectPath(pathRef);
        refProject2.setIpsObjectPath(pathRef2);
    }

    @Test
    public void testGetIndex() throws Exception {
        IpsObjectPath ipsObjectPath = new IpsObjectPath(ipsProject);
        IIpsArchiveEntry newArchiveEntry = ipsObjectPath.newArchiveEntry(new Path("anyPath"));
        IIpsContainerEntry newContainerEntry = ipsObjectPath.newContainerEntry("MyContainer", "myContainerPath");

        assertEquals(0, ipsObjectPath.getIndex(newArchiveEntry));
        assertEquals(1, ipsObjectPath.getIndex(newContainerEntry));
    }

    @Test
    public void testGetIndex_inContainer() throws Exception {
        IIpsObjectPathEntry entry0 = mock(IIpsObjectPathEntry.class);
        IIpsContainerEntry entry1 = mock(IIpsContainerEntry.class);
        IIpsObjectPathEntry containerEntry2 = mock(IIpsObjectPathEntry.class);
        IIpsObjectPathEntry containerEntry3 = mock(IIpsObjectPathEntry.class);
        IIpsObjectPathEntry entry4 = mock(IIpsObjectPathEntry.class);
        when(entry1.resolveEntries()).thenReturn(Arrays.asList(containerEntry2, containerEntry3));
        path.setEntries(new IIpsObjectPathEntry[] { entry0, entry1, entry4 });

        assertEquals(0, path.getIndex(entry0));
        assertEquals(1, path.getIndex(entry1));
        assertEquals(2, path.getIndex(containerEntry2));
        assertEquals(3, path.getIndex(containerEntry3));
        assertEquals(4, path.getIndex(entry4));
    }

    @Test
    public void testGetResourceAsStream() throws CoreException, IOException {
        setUpReferencedProjects(true);
        createFileWithContent((IFolder)refProject2Root.getCorrespondingResource(), "file.txt", "111");

        InputStream resourceAsStream = path.getResourceAsStream("file.txt");
        assertEquals("111", getFileContent(resourceAsStream));
    }

    @Test
    public void testGetResourceAsStreamInternal() throws CoreException {
        setUpReferencedProjects(false);
        createFileWithContent((IFolder)refProject2Root.getCorrespondingResource(), "file.txt", "111");

        InputStream resourceAsStream = path.getResourceAsStream("file.txt");
        assertNull(resourceAsStream);
    }

}
