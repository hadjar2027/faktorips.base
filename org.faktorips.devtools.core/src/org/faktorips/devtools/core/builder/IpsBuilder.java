/***************************************************************************************************
 *  * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.  *  * Alle Rechte vorbehalten.  *  *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,  * Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der  * Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community)  * genutzt werden, die Bestandteil der Auslieferung ist und auch
 * unter  *   http://www.faktorips.org/legal/cl-v01.html  * eingesehen werden kann.  *  *
 * Mitwirkende:  *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de  *  
 **************************************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.IpsStatus;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.IpsSrcFile;
import org.faktorips.devtools.core.model.IIpsArtefactBuilder;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.QualifiedNameType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;

/**
 * The ips builder generates Java sourcecode and xml files based on the ips objects contained in the
 * ips project. It runs before the Java builder, so that first the Java sourcecode is generated by
 * the ips builder and then the Java builder compiles the Java sourcecode into classfiles.
 */
public class IpsBuilder extends IncrementalProjectBuilder {

    /**
     * The builders extension id.
     */
    public final static String BUILDER_ID = IpsPlugin.PLUGIN_ID + ".ipsbuilder"; //$NON-NLS-1$

    public IpsBuilder() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {

        MultiStatus buildStatus = new MultiStatus(IpsPlugin.PLUGIN_ID, 0, Messages.IpsBuilder_msgBuildResults, null);
        try {
            monitor.beginTask("build", 100000); //$NON-NLS-1$
            monitor.subTask(Messages.IpsBuilder_validatingProject);
            getProject().deleteMarkers(IpsPlugin.PROBLEM_MARKER, true, 0);
            MessageList list = getIpsProject().validate();
            createMarkersFromMessageList(getProject(), list, IpsPlugin.PROBLEM_MARKER);
            monitor.worked(100);
            if (!getIpsProject().canBeBuild()) {
                IMarker marker = getProject().createMarker(IpsPlugin.PROBLEM_MARKER);
                String msg = Messages.IpsBuilder_msgInvalidProperties;
                updateMarker(marker, msg, IMarker.SEVERITY_ERROR);
                return getProject().getReferencedProjects();
            }
            monitor.subTask(Messages.IpsBuilder_preparingBuild);
            applyBuildCommand(buildStatus, new BeforeBuildProcessCommand(kind), monitor);
            monitor.worked(100);
            if (kind == IncrementalProjectBuilder.FULL_BUILD || kind == IncrementalProjectBuilder.CLEAN_BUILD
                    || getDelta(getProject()) == null) {
                // delta not available
                monitor.subTask(Messages.IpsBuilder_startFullBuild);
                fullBuild(buildStatus, new SubProgressMonitor(monitor, 99700));
            } else {
                monitor.subTask(Messages.IpsBuilder_startIncrementalBuild);
                incrementalBuild(buildStatus, new SubProgressMonitor(monitor, 99700));
            }
            monitor.subTask(Messages.IpsBuilder_finishBuild);
            applyBuildCommand(buildStatus, new AfterBuildProcessCommand(kind), monitor);
            monitor.worked(100);
            if (buildStatus.getSeverity() == IStatus.OK) {
                return getProject().getReferencedProjects();
            }

            // reinitialize the builders of the current builder set if an error
            // occurs
            reinitBuilderSets(buildStatus);
            throw new CoreException(buildStatus);
            
        } catch(OperationCanceledException e){
            reinitBuilderSets(buildStatus);
        }
        finally {
            monitor.done();
        }
        return getProject().getReferencedProjects();
    }

    private void reinitBuilderSets(MultiStatus buildStatus){
        try {
            IIpsArtefactBuilderSet builderSet = getIpsProject().getArtefactBuilderSet();
            builderSet.initialize();
        } catch (Exception e) {
            buildStatus.add(new IpsStatus(Messages.IpsBuilder_msgErrorExceptionDuringBuild));
        }
    }
    
    private void applyBuildCommand(MultiStatus buildStatus, BuildCommand command, IProgressMonitor monitor) throws CoreException {
        // Despite the fact that generating is disabled in the faktor ips
        // preferences the
        // validation of the modell class instances and marker updating of the
        // regarding resource files still takes place
        if (!IpsPlugin.getDefault().getIpsPreferences().getEnableGenerating()) {
            return;
        }
        IIpsArtefactBuilderSet currentBuilderSet = getIpsProject().getArtefactBuilderSet();
        IIpsArtefactBuilder[] artefactBuilders = currentBuilderSet.getArtefactBuilders();
        for (int i = 0; i < artefactBuilders.length; i++) {
            try {
                command.build(artefactBuilders[i], buildStatus);
            } catch (Exception e) {
                addIpsStatus(artefactBuilders[i], command, buildStatus, e);
            }
        }
        if(monitor.isCanceled()){
            throw new OperationCanceledException();
        }
    }

    private void addIpsStatus(IIpsArtefactBuilder builder,
            BuildCommand command,
            MultiStatus buildStatus,
            Exception e) {
        String text = builder.getName() + ": Error during: " + command + "."; //$NON-NLS-1$ //$NON-NLS-2$
        buildStatus.add(new IpsStatus(text, e));
    }

    private DependencyGraph getDependencyGraph() throws CoreException {
        IpsModel model = ((IpsModel)getIpsProject().getIpsModel());
        return model.getDependencyGraph(getIpsProject());
    }

    /*
     * Returns the ips project the build is currently building.
     */
    private IIpsProject getIpsProject() {
        return IpsPlugin.getDefault().getIpsModel().getIpsProject(getProject());
    }

    private void collectIpsSrcFilesForFullBuild(List allIpsSrcFiles) throws CoreException {
        IIpsPackageFragmentRoot[] roots = getIpsProject().getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            IIpsPackageFragment[] packs = roots[i].getIpsPackageFragments();
            for (int j = 0; j < packs.length; j++) {
                IIpsElement[] elements = packs[j].getChildren();
                for (int k = 0; k < elements.length; k++) {
                    if (elements[k] instanceof IIpsSrcFile) {
                        allIpsSrcFiles.add(elements[k]);
                    }
                }
            }
        }
    }

    private void removeEmptyFolders() throws CoreException {
        IIpsPackageFragmentRoot[] roots = getIpsProject().getIpsPackageFragmentRoots();
        for (int i = 0; i < roots.length; i++) {
            removeEmptyFolders(roots[i].getArtefactDestination(), false);
        }
    }

    /**
     * Full build generates Java source files for all IPS objects.
     */
    private MultiStatus fullBuild(MultiStatus buildStatus, IProgressMonitor monitor) {
        System.out.println("Full build started."); //$NON-NLS-1$
        long begin = System.currentTimeMillis();

        try {
            ArrayList allIpsSrcFiles = new ArrayList();
            collectIpsSrcFilesForFullBuild(allIpsSrcFiles);
            monitor.beginTask("full build", 2 * allIpsSrcFiles.size()); //$NON-NLS-1$
            getDependencyGraph().reInit();
            monitor.worked(allIpsSrcFiles.size());
            removeEmptyFolders();

            for (Iterator it = allIpsSrcFiles.iterator(); it.hasNext();) {
                try {
                    IIpsSrcFile ipsSrcFile = (IIpsSrcFile)it.next();
                    monitor.subTask(Messages.IpsBuilder_building + ipsSrcFile.getName());
                    buildIpsSrcFile(ipsSrcFile, buildStatus, monitor);
                    monitor.worked(1);
                } catch (Exception e) {
                    buildStatus.add(new IpsStatus(e));
                }
            }
        } catch (CoreException e) {
            buildStatus.add(new IpsStatus(e));
        } finally {
            monitor.done();
        }
        long end = System.currentTimeMillis();
        System.out.println("Full build finished. Duration: " + (end - begin)); //$NON-NLS-1$
        return buildStatus;
    }

    /**
     * {@inheritDoc}
     */
    protected void clean(IProgressMonitor monitor) throws CoreException {
        getIpsProject().getArtefactBuilderSet().clean();
    }

    private void removeEmptyFolders(IFolder parent, boolean removeThisParent) throws CoreException {
        if (!parent.exists()) {
            return;
        }
        IResource[] members = parent.members();
        if (removeThisParent && members.length == 0) {
            parent.delete(true, null);
            return;
        }
        for (int i = 0; i < members.length; i++) {
            if (members[i].getType() == IResource.FOLDER) {
                removeEmptyFolders((IFolder)members[i], true);
            }
        }
    }

    private Set getBuildCandidatesSet(IProject project, Map buildCandidatesForProjectMap){
        Set buildCandidatesSet = (Set)buildCandidatesForProjectMap.get(project);
        if(buildCandidatesSet == null){
            buildCandidatesSet = new HashSet(1000);
            buildCandidatesForProjectMap.put(project, buildCandidatesSet);
        }
        return buildCandidatesSet;
    }
    
    /**
     * Incremental build generates Java source files for all PdObjects that have been changed.
     */
    private void incrementalBuild(MultiStatus buildStatus, IProgressMonitor monitor) {
        System.out.println("Incremental build started."); //$NON-NLS-1$
        try {
            IResourceDelta delta = getDelta(getProject());
            IncBuildVisitor visitor = new IncBuildVisitor();
            delta.accept(visitor);
            Map buildCandidatesForProjectsMap = new HashMap(10);
            int numberOfBuildCandidates = collectBuildCandidatesForIncrementalBuild(
                    visitor.changedAndAddedIpsSrcFiles, visitor.removedIpsSrcFiles, buildCandidatesForProjectsMap) + 
                    visitor.removedIpsSrcFiles.size() + 
                    visitor.changedAndAddedIpsSrcFiles.size();
            monitor.beginTask("build incremental", numberOfBuildCandidates); //$NON-NLS-1$
            for (Iterator it = visitor.removedIpsSrcFiles.iterator(); it.hasNext();) {
                IpsSrcFile ipsSrcFile = (IpsSrcFile)it.next();
                monitor.subTask(Messages.IpsBuilder_deleting + ipsSrcFile.getName());
                applyBuildCommand(buildStatus, new DeleteArtefactBuildCommand(ipsSrcFile), monitor);
                monitor.worked(1);
            }
            
            for (Iterator it = visitor.changedAndAddedIpsSrcFiles.iterator(); it.hasNext();) {
                IpsSrcFile ipsSrcFile = (IpsSrcFile)it.next();
                monitor.subTask(Messages.IpsBuilder_building + ipsSrcFile.getName());
                buildIpsSrcFile(ipsSrcFile, buildStatus, monitor);
                monitor.worked(1);
            }
            
            IIpsModel ipsModel = getIpsProject().getIpsModel();
            for (Iterator it = buildCandidatesForProjectsMap.keySet().iterator(); it.hasNext();) {
                IProject project = (IProject)it.next();
                Set buildCandidates = (Set)buildCandidatesForProjectsMap.get(project);
                IIpsProject ipsProject = ipsModel.getIpsProject(project);
                for (Iterator it2 = buildCandidates.iterator(); it2.hasNext();) {
                    QualifiedNameType nameType = (QualifiedNameType)it2.next();
                    IIpsObject ipsObject = ipsProject.findIpsObject(nameType);
                    if(ipsObject == null){
                        continue;
                    }
                    monitor.subTask(Messages.IpsBuilder_building + nameType);
                    buildIpsSrcFile(ipsObject.getIpsSrcFile(), buildStatus, monitor);
                    ((IpsModel)ipsModel).getDependencyGraph(ipsProject).update(nameType);
                    monitor.worked(1);
                }
            }
        } catch (Exception e) {
            buildStatus.add(new IpsStatus(e));
        } finally {
            monitor.done();
            System.out.println("Incremental build finished."); //$NON-NLS-1$
        }
    }

    private void updateMarkers(IIpsObject object) throws CoreException {
        if (object == null) {
            return;
        }
        IResource resource = object.getEnclosingResource();
        if (!resource.exists()) {
            return;
        }
        resource.deleteMarkers(IMarker.PROBLEM, true, IResource.DEPTH_INFINITE);
        MessageList list = object.validate();
        createMarkersFromMessageList(resource, list, IMarker.PROBLEM);
    }

    private void createMarkersFromMessageList(IResource resource, MessageList list, String markerType)
            throws CoreException {
        for (int i = 0; i < list.getNoOfMessages(); i++) {
            Message msg = list.getMessage(i);
            IMarker marker = resource.createMarker(markerType);
            updateMarker(marker, msg.getText(), getMarkerSeverity(msg));
        }
    }

    private void updateMarker(IMarker marker, String text, int severity) throws CoreException {
        marker.setAttributes(new String[] { IMarker.MESSAGE, IMarker.SEVERITY }, new Object[] { text,
                new Integer(severity) });
    }

    private int getMarkerSeverity(Message msg) {
        int msgSeverity = msg.getSeverity();
        if (msgSeverity == Message.ERROR) {
            return IMarker.SEVERITY_ERROR;
        } else if (msgSeverity == Message.WARNING) {
            return IMarker.SEVERITY_WARNING;
        } else if (msgSeverity == Message.INFO) {
            return IMarker.SEVERITY_INFO;
        }
        throw new RuntimeException("Unknown severity " + msgSeverity); //$NON-NLS-1$
    }

    /**
     * Builds the indicated file and updates its markers.
     */
    private IIpsObject buildIpsSrcFile(IIpsSrcFile file, MultiStatus buildStatus, IProgressMonitor monitor) throws CoreException {
        if (!file.isContentParsable()) {
            return null;
        }
        IIpsObject ipsObject = file.getIpsObject();
        applyBuildCommand(buildStatus, new BuildArtefactBuildCommand(file), monitor);
        updateMarkers(ipsObject);
        return ipsObject;
    }

    private int collectBuildCandidatesForIncrementalBuild(List addedOrChangesIpsSrcFiles, List removedIpsSrcFiles, Map buildCandidatesForProjectsMap) throws CoreException{
        int numberOfBuildCandidates = 0;
        for (Iterator it = addedOrChangesIpsSrcFiles.iterator(); it.hasNext();) {
            IpsSrcFile ipsSrcFile = (IpsSrcFile)it.next();
            numberOfBuildCandidates += collectDependantForDependantProjects(ipsSrcFile.getQualifiedNameType(), getProject(), new HashSet(), buildCandidatesForProjectsMap);
        }
        for (Iterator it = removedIpsSrcFiles.iterator(); it.hasNext();) {
            IpsSrcFile ipsSrcFile = (IpsSrcFile)it.next();
            // TODO do we still need to find out if the file is in the ips projects roots?
            IIpsPackageFragmentRoot[] roots = ipsSrcFile.getIpsProject().getSourceIpsPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++) {
                if (ipsSrcFile.getIpsPackageFragment().getRoot().equals(roots[i])) {
                    numberOfBuildCandidates += collectDependantForDependantProjects(ipsSrcFile.getQualifiedNameType(), getProject(), new HashSet(), buildCandidatesForProjectsMap);
                }
            }
        }
        return numberOfBuildCandidates;
    }
    
    private int collectDependants(DependencyGraph graph, Set dependantQualifiedNames, QualifiedNameType nameType) {
        QualifiedNameType[] dependants = graph.getDependants(nameType);
        for (int i = 0; i < dependants.length; i++) {
            if (!dependantQualifiedNames.contains(dependants[i])) {
                dependantQualifiedNames.add(dependants[i]);
            }
        }
        return dependants.length;
    }

    private int collectDependantForDependantProjects(QualifiedNameType root, 
                                                     IProject project, 
                                                     Set alreadyBuildProjects, 
                                                     Map buildCandidatesForProjectMap) 
        throws CoreException {
        
        int numberOfCandidates = 0;
        IpsModel model = (IpsModel)IpsPlugin.getDefault().getIpsModel();
        DependencyGraph graph = model.getDependencyGraph(model.getIpsProject(project));
        if (graph == null) {
            return numberOfCandidates;
        }
        Set alreayBuild = getBuildCandidatesSet(project, buildCandidatesForProjectMap);
        numberOfCandidates += collectDependants(graph, alreayBuild, root);
        alreadyBuildProjects.add(project);
        buildCandidatesForProjectMap.put(project, alreayBuild);
        IProject[] dependantProjects = project.getReferencingProjects();
        
        for (int i = 0; i < dependantProjects.length && !alreadyBuildProjects.contains(dependantProjects[i]); i++) {
            numberOfCandidates += collectDependantForDependantProjects(root, dependantProjects[i], alreadyBuildProjects, buildCandidatesForProjectMap);
        }
        return numberOfCandidates;
    }

    /**
     * ResourceDeltaVisitor for the incremental build.
     */
    private class IncBuildVisitor implements IResourceDeltaVisitor {

        private IFolder[] outputFolders;
        private List removedIpsSrcFiles = new ArrayList(100);
        private List changedAndAddedIpsSrcFiles = new ArrayList(100);

        private IncBuildVisitor() throws CoreException {
            outputFolders = getIpsProject().getOutputFolders();
        }

        public List getRemovedIpsSrcFiles() {
            return removedIpsSrcFiles;
        }

        public List getChangedOrAddedIpsSrcFiles() {
            return changedAndAddedIpsSrcFiles;
        }

        /**
         * Checks if the provided resource is the java output folder resource or the IpsProject
         * output folder resource.
         * 
         * @throws CoreException
         */
        private boolean ignoredResource(IResource resource) throws CoreException {
            IPath outPutLocation = getIpsProject().getJavaProject().getOutputLocation();
            IPath resourceLocation = resource.getFullPath();
            if (outPutLocation.equals(resourceLocation)) {
                return true;
            }
            for (int i = 0; i < outputFolders.length; i++) {
                if (outputFolders[i].getFullPath().equals(resourceLocation)) {
                    return true;
                }
            }
            return false;
        }

        public boolean visit(IResourceDelta delta) throws CoreException {
            IResource resource = delta.getResource();
            if (resource == null || resource.getType() == IResource.PROJECT) {
                return true;
            }
            // resources in the output folders of the ipsProject and the
            // assigned java project are
            // ignored
            if (ignoredResource(resource)) {
                return false;
            }

            // only interested in IpsSrcFile changes
            IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(resource);
            if (!(element instanceof IIpsSrcFile)) {
                return true;
            }

            switch (delta.getKind()) {
                case IResourceDelta.ADDED:
                    if (element.exists()) {
                        changedAndAddedIpsSrcFiles.add(element);
                    }
                    return true;
                case IResourceDelta.REMOVED:
                    removedIpsSrcFiles.add(element);
                case IResourceDelta.CHANGED: {
                    // skip changes, not caused by content changes,
                    if (delta.getFlags() != 0 && element.exists()) {
                        changedAndAddedIpsSrcFiles.add(element);
                        return true;
                    }
                }
                    break;
            }
            return true;
        }
    }

    /*
     * The applyBuildCommand method of this class uses this interface.
     */
    private interface BuildCommand {
        public void build(IIpsArtefactBuilder builder, MultiStatus status) throws CoreException;
    }

    private class BeforeBuildProcessCommand implements BuildCommand {

        private int buildKind;

        public BeforeBuildProcessCommand(int buildKind) {
            this.buildKind = buildKind;
        }

        public void build(IIpsArtefactBuilder builder, MultiStatus status) throws CoreException {
            builder.beforeBuildProcess(getIpsProject(), buildKind);
        }

        public String toString() {
            return "BeforeBuildProcessCmd[kind=" + buildKind + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }

    }

    private class AfterBuildProcessCommand implements BuildCommand {

        private int buildKind;

        public AfterBuildProcessCommand(int buildKind) {
            this.buildKind = buildKind;
        }

        public void build(IIpsArtefactBuilder builder, MultiStatus status) throws CoreException {
            builder.afterBuildProcess(getIpsProject(), buildKind);
        }

        public String toString() {
            return "AfterBuildProcessCmd[kind=" + buildKind + "]"; //$NON-NLS-1$ //$NON-NLS-2$
        }
    }

    private static class BuildArtefactBuildCommand implements BuildCommand {

        private IIpsSrcFile ipsSrcFile;

        public BuildArtefactBuildCommand(IIpsSrcFile ipsSrcFile) {
            this.ipsSrcFile = ipsSrcFile;
        }

        public void build(IIpsArtefactBuilder builder, MultiStatus status) throws CoreException {
            if (builder.isBuilderFor(ipsSrcFile)) {
                try {
                    builder.beforeBuild(ipsSrcFile, status);
                    builder.build(ipsSrcFile);
                } finally {
                    builder.afterBuild(ipsSrcFile);
                }
            }
        }

        public String toString() {
            return "Build file " + ipsSrcFile; //$NON-NLS-1$
        }
    }

    private static class DeleteArtefactBuildCommand implements BuildCommand {

        private IIpsSrcFile toDelete;

        public DeleteArtefactBuildCommand(IIpsSrcFile toDelete) {
            this.toDelete = toDelete;
        }

        public void build(IIpsArtefactBuilder builder, MultiStatus status) throws CoreException {
            if (builder.isBuilderFor(toDelete)) {
                builder.delete(toDelete);
            }
        }

        public String toString() {
            return "Delete file " + toDelete; //$NON-NLS-1$
        }

    }
}
