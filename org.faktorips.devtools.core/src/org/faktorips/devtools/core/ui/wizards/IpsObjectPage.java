/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.wizards;

import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsPackageFragment;
import org.faktorips.devtools.core.model.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectNamingConventions;
import org.faktorips.devtools.core.model.IIpsSrcFile;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.product.IProductCmptReference;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.controller.fields.FieldValueChangedEvent;
import org.faktorips.devtools.core.ui.controller.fields.TextButtonField;
import org.faktorips.devtools.core.ui.controller.fields.TextField;
import org.faktorips.devtools.core.ui.controller.fields.ValueChangeListener;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRefControl;
import org.faktorips.devtools.core.ui.controls.IpsPckFragmentRootRefControl;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;


/**
 * A page that provides basic functionality for creating ips objects. Allows the user to specify the
 * ipssourcefolder, the ips package and the object's name.
 * Subclasses can specify an image for the page by setting it with the <code>setImageDescriptor()</code>
 * method within the subclass constructor. Alteratively the image can also be set in the constructor of
 * the wizard if the wizard contains only one page or if the image doesn't change when the page within
 * the wizard changes. 
 */
public abstract class IpsObjectPage extends WizardPage implements ValueChangeListener {

    // the resource that was selected in the workbench or null if none.
    private IResource selectedResource;
    
    // edit controls
    private IpsPckFragmentRootRefControl sourceFolderControl;
    private IpsPckFragmentRefControl packageControl;
    
    // edit fields
    private TextField nameField;
    private TextButtonField sourceFolderField;
    private TextButtonField packageField;
    
    // true if the input is validated and errors are displayed in the messes area.
    protected boolean validateInput = true;
    
    // page control as defined by the wizard page class
    private Composite pageControl;
    
    // composite holding the control for the object's name.
    // subclasses can add their own controls.
    private Composite nameComposite;
    
    private IpsObjectType ipsObjectType;
    
    /**
     * @param pageName
     * @param selection
     * @throws JavaModelException
     */
    public IpsObjectPage(IpsObjectType ipsObjectType, IStructuredSelection selection, String pageName) throws JavaModelException {
        super(pageName);
        ArgumentCheck.notNull(ipsObjectType, this);
        ArgumentCheck.notNull(pageName, this);
        this.ipsObjectType = ipsObjectType;
        selectedResource = getSelectedResourceFromSelection(selection);
        setTitle(Messages.NewIpsObjectWizard_title + ipsObjectType.getName());
    }
    
    private IResource getSelectedResourceFromSelection(IStructuredSelection selection) {
        if (selection==null) {
            return null;
        }
        try {
            Object element = selection.getFirstElement();
            if (element instanceof IResource) {
                return (IResource)selection.getFirstElement();
            } else if (element instanceof IJavaElement) {
                return ((IJavaElement)element).getCorrespondingResource();                
            } else if (element instanceof IIpsElement) {
                return ((IIpsElement)element).getEnclosingResource();
            } else if (element instanceof IProductCmptReference) {
                return ((IProductCmptReference)element).getProductCmpt().getEnclosingResource();
            }
        } catch (Exception e) {
            // if we can't get the selected ressource, we can't put default in the controls
            // but no need to bother the user with this, so we just log the exception
            IpsPlugin.log(e);
        }
        return null;
    }
    
    protected Control createControlInternal(Composite parent){
        UIToolkit toolkit = new UIToolkit(null);
        validateInput = false;
        setTitle(getIpsObjectType().getName());
        setMessage(NLS.bind(Messages.IpsObjectPage_msgNew, getIpsObjectType().getName())); 
        
        // dont set the layout of the parent composite - this will lead to 
        // layout-problems when this wizard-page is opened within allready open dialogs
        // (for example when the user wants a new policy class and starts the wizard using
        // the file-menu File->New->Other).
        
        // parent.setLayout(new GridLayout(1, false));
        
        pageControl = new Composite(parent, SWT.NONE);
        GridData data = new GridData(SWT.FILL, SWT.TOP, true, true);
        pageControl.setLayoutData(data);
        GridLayout pageLayout = new GridLayout(1, false);
        pageLayout.verticalSpacing = 20;
        pageControl.setLayout(pageLayout);
        
        Composite locationComposite  = toolkit.createLabelEditColumnComposite(pageControl);
        toolkit.createFormLabel(locationComposite, Messages.IpsObjectPage_labelSrcFolder);
        sourceFolderControl = toolkit.createPdPackageFragmentRootRefControl(locationComposite, true);
        sourceFolderField = new TextButtonField(sourceFolderControl);
        sourceFolderField.addChangeListener(this);

        toolkit.createFormLabel(locationComposite, Messages.IpsObjectPage_labelPackage);
        packageControl = toolkit.createPdPackageFragmentRefControl(locationComposite);
        packageField = new TextButtonField(packageControl);
        packageField.addChangeListener(this);
        
        Label line = new Label(pageControl, SWT.SEPARATOR | SWT.HORIZONTAL);
        line.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        
        nameComposite = toolkit.createLabelEditColumnComposite(pageControl);        
        fillNameComposite(nameComposite, toolkit);
        try {
            setDefaults(selectedResource);
        } catch (CoreException e) {
            IpsPlugin.log(e);
        }

        validateInput = true;
        return pageControl;
    }
    
    /**
     * {@inheritDoc}
     */
    public void createControl(Composite parent) {
        setControl(createControlInternal(parent));
    }
    
    /**
     * Derives the default values for source folder and package from
     * the selected resource.
     * 
     * @param selectedResource The resource that was selected in the current selection when
     * the wizard was opened.
     */
    protected void setDefaults(IResource selectedResource) throws CoreException {
        if (selectedResource==null) {
            setIpsPackageFragmentRoot(null);
            sourceFolderControl.setFocus();
            return;
        }
        IIpsElement element = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
        if (element instanceof IIpsProject) {
            IIpsPackageFragmentRoot[] roots;
            try {
                roots = ((IIpsProject)element).getIpsPackageFragmentRoots();
                if (roots.length>0) {
                    setIpsPackageFragmentRoot(roots[0]);
                }
            } catch (CoreException e) {
                IpsPlugin.log(e); // user can still work with the system, just so the defaults are missing
                // so just log it. 
            }
        } else if (element instanceof IIpsPackageFragmentRoot) {
            setIpsPackageFragmentRoot((IIpsPackageFragmentRoot)element);
        } else if (element instanceof IIpsPackageFragment) {
            IIpsPackageFragment pack = (IIpsPackageFragment)element;
            setIpsPackageFragment(pack);
        } else if (element instanceof IIpsSrcFile) {
            IIpsPackageFragment pack = (IIpsPackageFragment)element.getParent();
            setIpsPackageFragment(pack);
        } else {
            setIpsPackageFragmentRoot(null);    
        }
    }
    
    protected void fillNameComposite(Composite nameComposite, UIToolkit toolkit) {
        Text nameText = addNameLabelField(toolkit, nameComposite);
        nameText.setFocus();
    }
    
    protected Text addNameLabelField(UIToolkit toolkit, Composite parent) {
        toolkit.createFormLabel(parent, Messages.IpsObjectPage_labelName); 
        return addNameField(toolkit, parent);
    }
    
    protected Text addNameField(UIToolkit toolkit, Composite parent) {
        Text nameText = toolkit.createText(parent);
        nameText.setFocus();
        nameField = new TextField(nameText);
        nameField.addChangeListener(this);
        return nameText;
    }
    
    public String getIpsObjectName() {
        return nameField.getText();
    }
    
    public void setIpsObjectName(String newName) {
        nameField.setText(newName);
    }
    
    public String getPackage() {
        return packageField.getText();
    }
    
    public void setPackage(String packageName){
        packageField.setText(packageName);
    }
    
    public String getSourceFolder() {
        return sourceFolderField.getText();
    }

    public void setSourceFolder(String sourceFolder) {
        sourceFolderField.setText(sourceFolder);
    }
    
    public IIpsPackageFragmentRoot getIpsPackageFragmentRoot() {
        return sourceFolderControl.getPdPckFragmentRoot();
    }
    
    protected void sourceFolderChanged() {
        IIpsPackageFragmentRoot root = sourceFolderControl.getPdPckFragmentRoot();
        packageControl.setIpsPckFragmentRoot(root);
    }
    
    protected void packageChanged() {
        
    }
    
    protected void nameChanged() {
        
    }
    
    private void setIpsPackageFragment(IIpsPackageFragment pack) {
        packageControl.setIpsPackageFragment(pack);
        if (pack!=null) {
            setIpsPackageFragmentRoot(pack.getRoot());    
        }
    }
    
    private void setIpsPackageFragmentRoot(IIpsPackageFragmentRoot root) {
        sourceFolderControl.setPdPckFragmentRoot(root);
    }
    
    public IIpsPackageFragment getIpsPackageFragment() {
        return packageControl.getIpsPackageFragment();
    }
    
    public IIpsProject getIpsProject() {
    	if (getIpsPackageFragmentRoot() == null) {
    		return null;
    	}
    	return getIpsPackageFragmentRoot().getIpsProject();
    }
    
    /**
     * Returns the ips object that is stored in the resource that was selected when
     * the wizard was opened or <code>null</code> if none is selected.
     * 
     * @throws CoreException if the contents of the resource can't be parsed. 
     */
    public IIpsObject getSelectedIpsObject() throws CoreException {
    	if (selectedResource==null) {
    		return null;
    	}
    	IIpsElement el = IpsPlugin.getDefault().getIpsModel().getIpsElement(selectedResource);
    	if (el instanceof IIpsSrcFile) {
    		return ((IIpsSrcFile)el).getIpsObject();
    	}
    	return null;
    }
    
    protected IpsObjectType getIpsObjectType() {
        if(ipsObjectType == null){
            return ((INewIpsObjectWizard)getWizard()).getIpsObjectType();
        }
        return ipsObjectType;
    }
    

    public void setIpsObjectType(IpsObjectType ipsObjectType) {
        this.ipsObjectType = ipsObjectType;
    }


    protected Composite getNameComposite() {
        return nameComposite;
    }

    public void valueChanged(FieldValueChangedEvent e) {
        if (e.field==sourceFolderField) {
            sourceFolderChanged();
        }
        if (e.field==packageField) {
            packageChanged();
        }
        if (e.field==nameField) {
            nameChanged();
        }
        if (validateInput) { // don't validate during control creating!
            try {
                validatePage();    
            } catch (CoreException coreEx) {
                IpsPlugin.logAndShowErrorDialog(coreEx);
            }
            
        }
        updatePageComplete();
    }
    
    /**
     * Validates the page and generates error messages that are displayed in the message area of the wizard container.
     * If subclasses what to add further validations they can override the validatePageExtension() Method. The 
     * validationPageExtension() Method is called by this method before the page get updated. This method is protected
     * because subclasses might need to call within event szenarios implemented within the subclass. 
     */
    protected final void validatePage() throws CoreException {
        setMessage("", IMessageProvider.NONE); //$NON-NLS-1$
		setErrorMessage(null);
        validateSourceRoot();
        if (getErrorMessage()!=null) {
            return;
        }
        validatePackage();
        if (getErrorMessage()!=null) {
            return;
        }
        validateName();
        validatePageExtension();
        updatePageComplete();
    }
    
    /**
     * This method is empty by default. Subclasses might override it to add specific validations.
     * This method is called by the validatePage() method before the page will be updated.
     * 
     * @throws CoreException if these exceptions are thrown by subclasses they will be logged and
     *             displayed in an error dialog
     */
    protected void validatePageExtension() throws CoreException {
    }
    
	/**
     * The method validates the package.
     */
	private void validateSourceRoot() {
	    IIpsPackageFragmentRoot root = sourceFolderControl.getPdPckFragmentRoot(); 
        if (root!=null) {
            if (!root.getCorrespondingResource().exists()) {
                setErrorMessage(NLS.bind(Messages.IpsObjectPage_msgRootMissing, root.getName())); 
            } else if (!root.exists()) {
                setErrorMessage(NLS.bind(Messages.IpsObjectPage_msgRootNoIPSSrcFolder, root.getName())); 
            }
        }
        else {
            if (sourceFolderControl.getText().length() == 0) {
            setErrorMessage(Messages.IpsObjectPage_msgRootRequired);
            }
            else {
                setErrorMessage(NLS.bind(Messages.IpsObjectPage_msgRootMissing, sourceFolderControl.getText())); 
            }
        }
	}
	
	/**
	 * The method validates the source folder.
	 */
	private void validatePackage() {
	    IIpsPackageFragment pack = packageControl.getIpsPackageFragment(); 
        if (pack!=null && !pack.exists()) {
            setErrorMessage(NLS.bind(Messages.IpsObjectPage_msgPackageMissing, pack.getName())); 
        }
	}
	
	/**
	 * The method validates the name.
	 * <p>
	 * Subclasses may extend this method to perform their own validation.
	 * </p>
	 * @throws CoreException 
	 */
	protected void validateName() throws CoreException {
        if(getIpsProject() == null){
            return;
        }
	    // validate naming conventions
        String name=nameField.getText();
        IIpsProjectNamingConventions namingConventions = getIpsProject().getNamingConventions();
        MessageList ml;
        try {
            ml = namingConventions.validateUnqualifiedIpsObjectName(getIpsObjectType(), name);
            if (ml.getNoOfMessages() > 0) {
                String msgText = ml.getFirstMessage(ml.getSeverity()).getText();
                if (ml.getSeverity() == Message.ERROR){
                    setErrorMessage(msgText);
                } else if (ml.getSeverity() == Message.WARNING){
                    setMessage(msgText, DialogPage.WARNING);
                } else if (ml.getSeverity() == Message.INFO){
                    setMessage(msgText, DialogPage.INFORMATION);
                } else {
                    setMessage(msgText, DialogPage.NONE);
                }
                return;
            }            
        }
        catch (CoreException e) {
            // an error occured while validating the name
            IpsPlugin.logAndShowErrorDialog(e);
        }
		
        // check if an ipsobject already exists that has the same name and generates a java class
        // to avoid conflicts with java classes that have the same name 
        IIpsSrcFile file = getIpsProject().findIpsSrcFile(IpsObjectType.POLICY_CMPT_TYPE, getIpsObjectName());
        if(file != null){
            setErrorMessage(Messages.IpsObjectPage_msgObjectAllreadyExists);
        }
        file = getIpsProject().findIpsSrcFile(IpsObjectType.PRODUCT_CMPT_TYPE_V2, getIpsObjectName());
        if(file != null){
            setErrorMessage(Messages.IpsObjectPage_msgObjectAllreadyExists);
        }
        file = getIpsProject().findIpsSrcFile(IpsObjectType.TABLE_STRUCTURE, getIpsObjectName());
        if(file != null){
            setErrorMessage(Messages.IpsObjectPage_msgObjectAllreadyExists);
        }
        file = getIpsProject().findIpsSrcFile(IpsObjectType.TEST_CASE_TYPE, getIpsObjectName());
        if(file != null){
            setErrorMessage(Messages.IpsObjectPage_msgObjectAllreadyExists);
        }
	}
    
    protected void updatePageComplete() {
        if (getErrorMessage()!=null) {
            setPageComplete(false);
            return;
        }
        boolean complete = !"".equals(sourceFolderControl.getText()) //$NON-NLS-1$
        	&& !"".equals(nameField.getText()); //$NON-NLS-1$
        setPageComplete(complete);
    }
    
    public boolean canCreateIpsSrcFile(){
        return true;
    }
    
    protected IIpsSrcFile createIpsSrcFile(IProgressMonitor monitor) throws CoreException{
        IIpsSrcFile srcFile = getIpsPackageFragment().createIpsFile(getIpsObjectType(), getIpsObjectName(), true, new SubProgressMonitor(monitor, 1));
        finishIpsObject(srcFile.getIpsObject());
        return srcFile;
    }
    
    /**
     * Returns <code>false</code> by default. Subclasses can override this method to indicate that the Wizard 
     * can finish without stepping to the next page. It only makes sense to use this method if this page is added to a
     * NewIpsObjectWizard.
     */
    public boolean finishWhenThisPageIsComplete(){
        return false;
    }
    
    /**
     * This method is empty by default and can be overridden by sub classes when they want
     * to change the new created IpsObject before it will be saved.
     * 
     * @param ipsObject
     * @throws CoreException
     */
    protected void finishIpsObject(IIpsObject ipsObject) throws CoreException{
        
    }
    
    /**
     * This method is called when the page is entered. By default the implementation of this method is empty.
     * 
     * @throws CoreException
     */
    public void pageEntered() throws CoreException {
        
    }
}
