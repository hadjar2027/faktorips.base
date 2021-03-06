/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards.fixdifferences;

import java.lang.reflect.InvocationTargetException;
import java.util.Set;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsobject.IFixDifferencesToModelSupport;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.util.ArgumentCheck;

/**
 * 
 * @author Daniel Hohenberger
 */
public class FixDifferencesToModelWizard extends Wizard implements IWorkbenchWizard {
    private Set<? extends Object> ipsElementsToFix;
    private ElementSelectionPage elementSelectionPage;

    public FixDifferencesToModelWizard(Set<? extends Object> ipsElementsToFix) {
        ArgumentCheck.notNull(ipsElementsToFix, this);
        this.ipsElementsToFix = ipsElementsToFix;
        setWindowTitle(Messages.FixDifferencesToModelWizard_Title);
        setDefaultPageImageDescriptor(
                IpsUIPlugin.getImageHandling().createImageDescriptor("wizards/FixDifferencesToModelWizard.png")); //$NON-NLS-1$
    }

    @Override
    public boolean performFinish() {
        final IFixDifferencesToModelSupport[] elementsToFix = elementSelectionPage.getElementsToFix();
        final IWorkspaceRunnable op = new IWorkspaceRunnable() {
            @Override
            public void run(IProgressMonitor monitor) {
                monitor.beginTask(Messages.FixDifferencesToModelWizard_beginTask, elementsToFix.length);
                try {
                    for (IFixDifferencesToModelSupport element : elementsToFix) {
                        element.fixAllDifferencesToModel(element.getIpsSrcFile().getIpsProject());
                        element.getIpsSrcFile().save(true, null);
                        monitor.worked(1);
                    }
                } catch (CoreException e) {
                    IpsPlugin.logAndShowErrorDialog(e);
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            getContainer().run(true, true, new IRunnableWithProgress() {
                @Override
                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
                    IpsPlugin.getDefault().getIpsModel().runAndQueueChangeEvents(op, monitor);
                }
            });
        } catch (InvocationTargetException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        } catch (InterruptedException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            Thread.currentThread().interrupt();
            return false;

        }
        return true;
    }

    @Override
    public void init(IWorkbench workbench, IStructuredSelection selection) {
        // initialization is handled in the calling action, which transforms the selection to the
        // needed set.
        setNeedsProgressMonitor(true);
    }

    @Override
    public void addPages() {
        super.addPages();
        elementSelectionPage = new ElementSelectionPage(ipsElementsToFix);
        super.addPage(elementSelectionPage);
    }

}
