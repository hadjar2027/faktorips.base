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

package org.faktorips.devtools.stdbuilder.refactor;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragment;
import org.faktorips.devtools.core.model.type.IAttribute;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.stdbuilder.StandardBuilderSet;

/**
 * This class is loaded by the Faktor-IPS rename refactoring to participate in this process by
 * renaming the Java source code.
 * <p>
 * This is accomplished by successively calling JDT refactorings on the <tt>IJavaElement</tt>
 * generated by the code generator for the <tt>IIpsElement</tt> to be refactored.
 * 
 * @author Alexander Weickmann
 */
public class RenameRefactoringParticipant extends RenameParticipant {

    /** A helper providing shared standard builder refactoring functionality. */
    private RenameParticipantHelper refactoringHelper;

    /** Creates a <tt>RenameRefactoringParticipant</tt>. */
    public RenameRefactoringParticipant() {
        refactoringHelper = new RenameParticipantHelper();
    }

    @Override
    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context)
            throws OperationCanceledException {

        return refactoringHelper.checkConditions(pm, context);
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException {
        return refactoringHelper.createChange(pm);
    }

    @Override
    protected boolean initialize(Object element) {
        return refactoringHelper.initialize(element);
    }

    @Override
    public String getName() {
        return "StandardBuilder Rename Participant";
    }

    /** The <tt>RefactoringParticipantHelper</tt> for this participant. */
    private final class RenameParticipantHelper extends RefactoringParticipantHelper {

        @Override
        protected Refactoring createJdtRefactoring(IJavaElement originalJavaElement,
                IJavaElement targetJavaElement,
                RefactoringStatus status) throws CoreException {

            String javaRefactoringContributionId;
            switch (originalJavaElement.getElementType()) {
                case IJavaElement.FIELD:
                    javaRefactoringContributionId = IJavaRefactorings.RENAME_FIELD;
                    break;
                case IJavaElement.METHOD:
                    javaRefactoringContributionId = IJavaRefactorings.RENAME_METHOD;
                    break;
                case IJavaElement.TYPE:
                    javaRefactoringContributionId = IJavaRefactorings.RENAME_TYPE;
                    break;
                default:
                    throw new RuntimeException("This kind of Java element is not supported.");
            }

            RefactoringContribution contribution = RefactoringCore
                    .getRefactoringContribution(javaRefactoringContributionId);
            RenameJavaElementDescriptor descriptor = (RenameJavaElementDescriptor)contribution.createDescriptor();
            descriptor.setJavaElement(originalJavaElement);
            descriptor.setNewName(targetJavaElement.getElementName());
            descriptor.setUpdateReferences(getArguments().getUpdateReferences());
            return descriptor.createRefactoring(status);
        }

        @Override
        protected boolean initializeTargetJavaElements(IIpsElement ipsElement, StandardBuilderSet builderSet) {
            boolean success = false;
            if (ipsElement instanceof IAttribute) {
                success = initializeTargetJavaElementsForAttribute((IAttribute)ipsElement, builderSet);

            } else if (ipsElement instanceof IType) {
                IType type = (IType)ipsElement;
                IIpsPackageFragment targetIpsPackageFragment = type.getIpsPackageFragment();
                String newName = getArguments().getNewName();
                success = initTargetJavaElements(type, targetIpsPackageFragment, newName, builderSet);

            } else {
                success = false;
            }

            return success;
        }

        /**
         * Initializes the list of the <tt>IJavaElement</tt>s generated for the <tt>IAttribute</tt>
         * after it has been refactored.
         */
        private boolean initializeTargetJavaElementsForAttribute(IAttribute attribute, StandardBuilderSet builderSet) {
            String oldName = attribute.getName();
            attribute.setName(getArguments().getNewName());
            setTargetJavaElements(builderSet.getGeneratedJavaElements(attribute));
            attribute.setName(oldName);
            return true;
        }

    }

}
