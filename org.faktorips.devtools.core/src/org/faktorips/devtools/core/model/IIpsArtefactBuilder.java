package org.faktorips.devtools.core.model;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.MultiStatus;

/**
 * An implementation of this interface is supposed to create one artefact for an IpsObject. The
 * isBuilderFor() method indicates to the ips build framework which kind of IpsObjects this builder
 * is interested in. This interface describes a defined build cyle. For every IpsObject this builder
 * builds an artefact for, the following methods are called sequentially beforeBuild(), build(),
 * afterBuild(). If a full build is started the beforeFullBuild() method is called before the first
 * IpsSrcFile that hosts the IpsObject is provided to this builder and the afterFullBuild() method
 * is called after the last IpsSrcFile has been provided to this builder. A set of
 * IpsArtefactBuilders are collected within an IpsArtefactBuilderSet. The builders are made
 * available to the building system by registering the IpsArtefactBuilderSet at the according
 * extension point.
 * 
 * @author Peter Erzberger
 */
public interface IIpsArtefactBuilder {

	/**
     * Is called on every registered IpsArtefactBuilder before a full build starts.
     * 
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or RuntimeException will stop the current build cycle of this
     *             builder.
     */
    public void beforeFullBuild() throws CoreException;

    /**
     * Is called on every registered IpsArtefactBuilder after a full has finished.
     * 
     * @throws CoreException implementations can throw or delegate rising CoreExceptions.
     */
    public void afterFullBuild() throws CoreException;

    /**
     * Is called before the build starts if the isBuilderFor method has returned true for the
     * provided IpsSrcFile. This method is supposed to be used to set the builder in a defined state
     * before the actual build process starts.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artefact builder
     * @param status exceptional states can be reported to this multi status object. This will not
     *            interrupt the current build cycle. The exception will be reported to the used by
     *            means of a dialog at the end of the build routine. In addition the exception will
     *            be logged to the eclipse log file.
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or a RuntimeException will stop the current build cycle of this
     *             builder. Only the afterBuild(IpsSrcFile) method is called to be able to clean up
     *             a builder implementation. The exception will be reported to the used by means of
     *             a dialog at the end of the build routine. In addition the exception will be
     *             logged to the eclipse log file.
     */
    public void beforeBuild(IIpsSrcFile ipsSrcFile, MultiStatus status) throws CoreException;

    /**
     * Is called after the build method has finished only if the isBuilderFor method has returned
     * true for the provided IpsSrcFile. This method is supposed to be used for clean up after the
     * build has finished.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artefact builder
     * @param monitor can be used within this method report progress
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. The
     *             exception will be reported to the used by means of a dialog at the end of the
     *             build routine. In addition the exception will be logged to the eclipse log file.
     */
    public void afterBuild(IIpsSrcFile ipsSrcFile) throws CoreException;

    /**
     * Is called during full or incremental build if the isBuilderFor method has returned true for
     * the provided IpsSrcFile.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artefact builder
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. Throwing a
     *             CoreException or a RuntimeException will stop the current build cycle of this
     *             builder. Only the afterBuild(IpsSrcFile) method is called to be able to clean up
     *             a builder implementation. The exception will be reported to the used by means of
     *             a dialog at the end of the build routine. In addition the exception will be
     *             logged to the eclipse log file.
     */
    public void build(IIpsSrcFile ipsSrcFile) throws CoreException;

    /**
     * Is supposed to return true if this builder is a builder for the provided IpsSrcFile.
     */
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException;

    /**
     * Deletes the artefact that is created by this builder upon the provided IpsSrcFile.
     * 
     * @param ipsSrcFile the IpsSrcFile that is used by this artefact builder
     * @throws CoreException implementations can throw or delegate rising CoreExceptions. The
     *             exception will be reported to the used by means of a dialog at the end of the
     *             build routine. In addition the exception will be logged to the eclipse log file.
     */
    public void delete(IIpsSrcFile ipsSrcFile) throws CoreException;
}
