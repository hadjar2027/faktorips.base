/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.core.ui.preferencepages;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.resource.LocalResourceManager;
import org.eclipse.jface.resource.ResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.Widget;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.ipsproject.IIpsObjectPath;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.binding.BindingContext;
import org.faktorips.devtools.core.ui.binding.ControlPropertyBinding;
import org.faktorips.devtools.core.ui.binding.PresentationModelObject;
import org.faktorips.devtools.core.ui.controller.fields.CheckboxField;

/**
 * Ips object path preference page container
 * 
 * @author Roman Grutza
 */
public class IpsObjectPathContainer {

    private static final ImageDescriptor packageFragmentRootImage = IpsUIPlugin.getImageHandling()
            .createImageDescriptor("IpsPackageFragmentRoot.gif"); //$NON-NLS-1$

    private static final ImageDescriptor projectImage = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "IpsProject.gif"); //$NON-NLS-1$

    private static final ImageDescriptor archiveImage = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "IpsAr.gif"); //$NON-NLS-1$

    private static final ImageDescriptor objectPathImage = IpsUIPlugin.getImageHandling().createImageDescriptor(
            "obj16/cp_order_obj.gif"); //$NON-NLS-1$

    private IIpsProject currentIpsProject;

    private IIpsObjectPath ipsObjectPath;

    private int pageIndex;

    private ReferencedProjectsComposite refProjectsComposite;
    private ArchiveComposite archiveComposite;
    private SrcFolderComposite srcFolderComposite;
    private ObjectPathOrderComposite orderComposite;

    private ResourceManager resourceManager;

    protected BindingContext bindingContext;

    private IpsObjectPathContainerPmo ipsObjectPathContainerPmo;

    public IpsObjectPathContainer(int pageToShow) {
        pageIndex = pageToShow;
        resourceManager = new LocalResourceManager(JFaceResources.getResources());
    }

    /**
     * @param ipsProject The IPS project to configure.
     */
    public void init(IIpsProject ipsProject) throws CoreException {
        currentIpsProject = ipsProject;
        ipsObjectPath = ipsProject.getIpsObjectPath();
        bindingContext = new BindingContext();
        ipsObjectPathContainerPmo = new IpsObjectPathContainerPmo(ipsObjectPath);

        reinitComposites();
    }

    /**
     * Main UI creation method for configuring IPS objectpath entries
     * 
     * @param parent control
     * @return IPS objectpath control
     * @throws CoreException if the control could not be created
     */
    public Control createControl(final Composite parent) throws CoreException {
        final UIToolkit toolkit = new UIToolkit(null);
        ipsObjectPathContainerPmo.addPropertyChangeListener(new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent pce) {
                if (pce.getOldValue() == Boolean.FALSE && pce.getNewValue() == Boolean.TRUE
                        && pce.getPropertyName().equals(IpsObjectPathContainerPmo.PROPERTY_ENABLED_VALUE)) {
                    boolean openQuestion = MessageDialog.openQuestion(parent.getShell(),
                            Messages.UseManifest_Question_Title, Messages.UseManifest_Question_Message);
                    if (!openQuestion) {
                        ipsObjectPathContainerPmo.setUsingManifest(false);
                        ipsObjectPathContainerPmo.setDataChanged(false);
                    }
                }
            }
        });

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());

        GridLayout layout = new GridLayout();
        layout.marginWidth = 0;
        layout.marginHeight = 0;
        layout.numColumns = 1;
        composite.setLayout(layout);

        CheckboxField useManifestCheck = new CheckboxField(toolkit.createCheckbox(composite,
                Messages.UseManifest_checkbox_label));
        GridData gd = new GridData(GridData.VERTICAL_ALIGN_BEGINNING | GridData.FILL_HORIZONTAL);
        gd.horizontalSpan = 2;
        useManifestCheck.getCheckbox().setLayoutData(gd);
        bindingContext.bindContent(useManifestCheck, ipsObjectPathContainerPmo,
                IpsObjectPathContainerPmo.PROPERTY_ENABLED_VALUE);

        final TabFolder folder = new TabFolder(composite, SWT.NONE);
        folder.setLayoutData(new GridData(GridData.FILL_BOTH));
        folder.setFont(composite.getFont());

        bindingContext.add(new ControlPropertyBinding(useManifestCheck.getControl(), ipsObjectPathContainerPmo,
                IpsObjectPathContainerPmo.PROPERTY_ENABLED_VALUE, Boolean.TYPE) {

            @Override
            public void updateUiIfNotDisposed(String nameOfChangedProperty) {
                toolkit.setDataChangeable(folder, !ipsObjectPathContainerPmo.isUsingManifest());
            }

        });

        srcFolderComposite = new SrcFolderComposite(folder);
        refProjectsComposite = new ReferencedProjectsComposite(folder);
        archiveComposite = new ArchiveComposite(folder);
        orderComposite = new ObjectPathOrderComposite(folder);

        addTabItem(folder, Messages.IpsObjectPathContainer_tab_source,
                (Image)resourceManager.get(packageFragmentRootImage), srcFolderComposite);

        addTabItem(folder, Messages.IpsObjectPathContainer_tab_projects, (Image)resourceManager.get(projectImage),
                refProjectsComposite);
        addTabItem(folder, Messages.IpsObjectPathContainer_tab_archives, (Image)resourceManager.get(archiveImage),
                archiveComposite);
        addTabItem(folder, Messages.IpsObjectPathContainer_tab_path_order, (Image)resourceManager.get(objectPathImage),
                orderComposite);

        srcFolderComposite.init(ipsObjectPath);
        refProjectsComposite.init(ipsObjectPath);
        archiveComposite.init(ipsObjectPath);
        orderComposite.init(ipsObjectPath);

        folder.setSelection(pageIndex);

        folder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                tabChanged(e.item);
            }
        });

        Dialog.applyDialogFont(composite);
        bindingContext.updateUI();
        return composite;
    }

    private TabItem addTabItem(TabFolder parent, String tabName, Image tabImage, Composite composite) {
        TabItem item = new TabItem(parent, SWT.NONE);
        item.setText(tabName);
        item.setImage(tabImage);
        item.setData(composite);
        item.setControl(composite);

        return item;
    }

    private void tabChanged(Widget widget) {
        if (widget instanceof TabItem) {
            TabItem tabItem = (TabItem)widget;
            pageIndex = tabItem.getParent().getSelectionIndex();
        }
        doUpdateUI();
    }

    /**
     * Persists changes made in the property page dialog.
     * 
     * @return true if changes made in the dialog could be committed successfully, false otherwise
     */
    public boolean saveToIpsProjectFile() {
        try {
            currentIpsProject.setIpsObjectPath(ipsObjectPath);
        } catch (CoreException e) {
            IpsPlugin.logAndShowErrorDialog(e);
            return false;
        }
        return true;
    }

    /**
     * Check whether values have been modified
     * 
     * @return true if data has changed, false otherwise
     */
    public boolean hasChangesInDialog() {
        return (ipsObjectPathContainerPmo.isDataChanged() || archiveComposite.isDataChanged()
                || orderComposite.isDataChanged() || refProjectsComposite.isDataChanged() || srcFolderComposite
                    .isDataChanged());
    }

    /**
     * Manually update the UI
     */
    public void doUpdateUI() {
        switch (pageIndex) {
            case 0:
                srcFolderComposite.doUpdateUI();
                break;
            case 1:
                refProjectsComposite.doUpdateUI();
                break;
            case 2:
                archiveComposite.doUpdateUI();
                break;
            case 3:
                orderComposite.doUpdateUI();
                break;
        }
    }

    private void reinitComposites() {
        if (archiveComposite != null) {
            archiveComposite.init(ipsObjectPath);
        }
        if (orderComposite != null) {
            orderComposite.init(ipsObjectPath);
        }
        if (refProjectsComposite != null) {
            refProjectsComposite.init(ipsObjectPath);
        }
        if (srcFolderComposite != null) {
            srcFolderComposite.init(ipsObjectPath);
        }
    }

    public void dispose() {
        resourceManager.dispose();
        bindingContext.dispose();
    }

    public static class IpsObjectPathContainerPmo extends PresentationModelObject {

        public static final String PROPERTY_ENABLED_VALUE = "usingManifest"; //$NON-NLS-1$

        private IIpsObjectPath ipsObjectPath;
        private boolean dataChanged = false;

        public IpsObjectPathContainerPmo(IIpsObjectPath ipsObjectPath) {
            this.ipsObjectPath = ipsObjectPath;
        }

        public boolean isUsingManifest() {
            return ipsObjectPath.isUsingManifest();
        }

        public void setUsingManifest(boolean enabled) {
            boolean oldValue = isUsingManifest();
            ipsObjectPath.setUsingManifest(enabled);
            setDataChanged(true);
            if (enabled && !oldValue) {
                notifyListeners(new PropertyChangeEvent(this, PROPERTY_ENABLED_VALUE, Boolean.FALSE, Boolean.TRUE));
            }
            notifyListeners();
        }

        public boolean isDataChanged() {
            return dataChanged;
        }

        public void setDataChanged(boolean dataChanged) {
            this.dataChanged = dataChanged;
        }
    }
}
