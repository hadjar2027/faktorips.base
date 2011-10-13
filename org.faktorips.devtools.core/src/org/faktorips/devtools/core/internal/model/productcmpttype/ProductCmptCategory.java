/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpttype;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyDirectReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyExternalReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyReference;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.model.type.TypeHierarchyVisitor;
import org.faktorips.devtools.core.util.ListElementMover;
import org.faktorips.runtime.internal.StringUtils;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IProductCmptProperty}, please see the interface for more details.
 * 
 * @author Alexander Weickmann
 */
public final class ProductCmptCategory extends IpsObjectPart implements IProductCmptCategory {

    /*
     * Cannot use IpsObjectPartCollection as there are multiple implementations that need to be
     * stored in one collection in order to preserve the insert ordering.
     */
    private final List<IProductCmptPropertyReference> propertyReferences = new ArrayList<IProductCmptPropertyReference>();

    private boolean inherited;

    private boolean defaultForFormulaSignatureDefinitions;

    private boolean defaultForValidationRules;

    private boolean defaultForTableStructureUsages;

    private boolean defaultForPolicyCmptTypeAttributes;

    private boolean defaultForProductCmptTypeAttributes;

    private Position position;

    public ProductCmptCategory(IProductCmptType parent, String id) {
        super(parent, id);
        position = Position.LEFT;
    }

    @Override
    public IProductCmptType getProductCmptType() {
        return (IProductCmptType)getParent();
    }

    @Override
    public List<IProductCmptProperty> findReferencedProductCmptProperties(IIpsProject ipsProject) throws CoreException {
        List<IProductCmptProperty> properties = new ArrayList<IProductCmptProperty>(propertyReferences.size());
        for (IProductCmptPropertyReference reference : propertyReferences) {
            properties.add(reference.findReferencedProductCmptProperty(ipsProject));
        }
        return properties;
    }

    @Override
    public boolean isReferencedProductCmptProperty(IProductCmptProperty property) {
        for (IProductCmptPropertyReference reference : propertyReferences) {
            if (reference.isReferencingProperty(property)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public List<IProductCmptProperty> findAllReferencedProductCmptProperties(IIpsProject ipsProject)
            throws CoreException {

        if (!isInherited()) {
            return findReferencedProductCmptProperties(ipsProject);
        }

        // Collect all assigned properties from the supertype hierarchy
        final Map<IProductCmptType, List<IProductCmptProperty>> typesToAssignedProperties = new LinkedHashMap<IProductCmptType, List<IProductCmptProperty>>();
        TypeHierarchyVisitor<IProductCmptType> visitor = new TypeHierarchyVisitor<IProductCmptType>(ipsProject) {
            @Override
            protected boolean visit(IProductCmptType currentType) throws CoreException {
                IProductCmptCategory category = currentType.getProductCmptCategoryIncludeSupertypeCopies(getName());
                typesToAssignedProperties.put(currentType, category.findReferencedProductCmptProperties(ipsProject));
                return true;
            }
        };
        visitor.start(getProductCmptType());
        // Sort so that properties that are farther up in the hierarchy are listed at the top
        List<IProductCmptProperty> sortedAssignedProperties = new ArrayList<IProductCmptProperty>();
        for (int i = visitor.getVisited().size() - 1; i >= 0; i--) {
            IType type = visitor.getVisited().get(i);
            sortedAssignedProperties.addAll(typesToAssignedProperties.get(type));
        }
        return Collections.unmodifiableList(sortedAssignedProperties);
    }

    @Override
    public boolean findIsReferencedProductCmptProperty(final IProductCmptProperty property, IIpsProject ipsProject)
            throws CoreException {

        if (!isInherited()) {
            return isReferencedProductCmptProperty(property);
        }

        class ProductCmptPropertyFinder extends TypeHierarchyVisitor<IProductCmptType> {

            public ProductCmptPropertyFinder(IIpsProject ipsProject) {
                super(ipsProject);
            }

            private boolean referenced;

            @Override
            protected boolean visit(IProductCmptType currentType) throws CoreException {
                IProductCmptCategory category = currentType.getProductCmptCategoryIncludeSupertypeCopies(getName());
                if (category != null && category.isReferencedProductCmptProperty(property)) {
                    referenced = true;
                    return false;
                }
                return true;
            }

        }

        ProductCmptPropertyFinder finder = new ProductCmptPropertyFinder(ipsProject);
        finder.start(getProductCmptType());
        return finder.referenced;
    }

    @Override
    public IProductCmptPropertyDirectReference newProductCmptPropertyReference(IProductCmptTypeAttribute productCmptTypeAttribute) {
        ArgumentCheck.equals(productCmptTypeAttribute.getProductCmptType(), getProductCmptType());
        return newProductCmptPropertyDirectReference(productCmptTypeAttribute);
    }

    @Override
    public IProductCmptPropertyExternalReference newProductCmptPropertyReference(IPolicyCmptTypeAttribute policyCmptTypeAttribute) {
        ArgumentCheck.equals(policyCmptTypeAttribute.getPolicyCmptType().getQualifiedName(), getProductCmptType()
                .getPolicyCmptType());
        return newProductCmptPropertyExternalReference(policyCmptTypeAttribute);
    }

    @Override
    public IProductCmptPropertyDirectReference newProductCmptPropertyReference(IProductCmptTypeMethod productCmptTypeMethod) {
        ArgumentCheck.equals(productCmptTypeMethod.getProductCmptType(), getProductCmptType());
        return newProductCmptPropertyDirectReference(productCmptTypeMethod);
    }

    @Override
    public IProductCmptPropertyDirectReference newProductCmptPropertyReference(ITableStructureUsage tableStructureUsage) {
        ArgumentCheck.equals(tableStructureUsage.getProductCmptType(), getProductCmptType());
        return newProductCmptPropertyDirectReference(tableStructureUsage);
    }

    @Override
    public IProductCmptPropertyExternalReference newProductCmptPropertyReference(IValidationRule validationRule) {
        ArgumentCheck.equals(validationRule.getType().getQualifiedName(), getProductCmptType().getPolicyCmptType());
        return newProductCmptPropertyExternalReference(validationRule);
    }

    private IProductCmptPropertyExternalReference newProductCmptPropertyExternalReference(IProductCmptProperty productCmptProperty) {
        IProductCmptPropertyExternalReference reference = (IProductCmptPropertyExternalReference)newPart(ProductCmptPropertyExternalReference.class);
        reference.setName(productCmptProperty.getPropertyName());
        reference.setProductCmptPropertyType(productCmptProperty.getProductCmptPropertyType());
        return reference;
    }

    private IProductCmptPropertyDirectReference newProductCmptPropertyDirectReference(IProductCmptProperty productCmptProperty) {
        IProductCmptPropertyDirectReference reference = (IProductCmptPropertyDirectReference)newPart(ProductCmptPropertyDirectReference.class);
        ((ProductCmptPropertyDirectReference)reference).setProductCmptProperty(productCmptProperty);
        return reference;
    }

    @Override
    public boolean deleteProductCmptPropertyReference(IProductCmptProperty productCmptProperty) {
        for (IProductCmptPropertyReference reference : propertyReferences) {
            if (reference.isReferencingProperty(productCmptProperty)) {
                reference.delete();
                return true;
            }
        }
        return false;
    }

    @Override
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        valueChanged(oldValue, name);
    }

    @Override
    public boolean isInherited() {
        return inherited;
    }

    @Override
    public void setInherited(boolean inherited) {
        boolean oldValue = this.inherited;
        this.inherited = inherited;
        valueChanged(oldValue, inherited);
    }

    @Override
    public boolean isDefaultForFormulaSignatureDefinitions() {
        return defaultForFormulaSignatureDefinitions;
    }

    @Override
    public void setDefaultForFormulaSignatureDefinitions(boolean defaultForFormulaSignatureDefinitions) {
        boolean oldValue = this.defaultForFormulaSignatureDefinitions;
        this.defaultForFormulaSignatureDefinitions = defaultForFormulaSignatureDefinitions;
        valueChanged(oldValue, defaultForFormulaSignatureDefinitions);
    }

    @Override
    public boolean isDefaultForPolicyCmptTypeAttributes() {
        return defaultForPolicyCmptTypeAttributes;
    }

    @Override
    public void setDefaultForPolicyCmptTypeAttributes(boolean defaultForPolicyCmptTypeAttributes) {
        boolean oldValue = this.defaultForPolicyCmptTypeAttributes;
        this.defaultForPolicyCmptTypeAttributes = defaultForPolicyCmptTypeAttributes;
        valueChanged(oldValue, defaultForPolicyCmptTypeAttributes);
    }

    @Override
    public boolean isDefaultForProductCmptTypeAttributes() {
        return defaultForProductCmptTypeAttributes;
    }

    @Override
    public void setDefaultForProductCmptTypeAttributes(boolean defaultForProductCmptTypeAttributes) {
        boolean oldValue = this.defaultForProductCmptTypeAttributes;
        this.defaultForProductCmptTypeAttributes = defaultForProductCmptTypeAttributes;
        valueChanged(oldValue, defaultForProductCmptTypeAttributes);
    }

    @Override
    public boolean isDefaultForTableStructureUsages() {
        return defaultForTableStructureUsages;
    }

    @Override
    public void setDefaultForTableStructureUsages(boolean defaultForTableStructureUsages) {
        boolean oldValue = this.defaultForTableStructureUsages;
        this.defaultForTableStructureUsages = defaultForTableStructureUsages;
        valueChanged(oldValue, defaultForTableStructureUsages);
    }

    @Override
    public boolean isDefaultForValidationRules() {
        return defaultForValidationRules;
    }

    @Override
    public void setDefaultForValidationRules(boolean defaultForValidationRules) {
        boolean oldValue = this.defaultForValidationRules;
        this.defaultForValidationRules = defaultForValidationRules;
        valueChanged(oldValue, defaultForValidationRules);
    }

    @Override
    public void setPosition(Position side) {
        ArgumentCheck.notNull(side);

        Position oldValue = this.position;
        this.position = side;
        valueChanged(oldValue, side);
    }

    @Override
    public Position getPosition() {
        return position;
    }

    @Override
    public boolean isAtLeftPosition() {
        return position == Position.LEFT;
    }

    @Override
    public boolean isAtRightPosition() {
        return position == Position.RIGHT;
    }

    @Override
    public int getNumberOfProductCmptPropertyReferences() {
        return propertyReferences.size();
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (!validateNameIsEmpty(list)) {
            return;
        }
        validateNameAlreadyUsedInTypeHierarchy(list, ipsProject);
        if (!validateInheritedButNoSupertype(list)) {
            return;
        }
        validateInheritedButNotFoundInSupertypeHierarchy(list, ipsProject);
        validateDuplicateDefaultsForFormulaSignatureDefinitions(list, ipsProject);
        validateDuplicateDefaultsForPolicyCmptTypeAttributes(list, ipsProject);
        validateDuplicateDefaultsForProductCmptTypeAttributes(list, ipsProject);
        validateDuplicateDefaultsForTableStructureUsages(list, ipsProject);
        validateDuplicateDefaultsForValidationRules(list, ipsProject);
    }

    private boolean validateNameIsEmpty(MessageList list) {
        if (StringUtils.isEmpty(name)) {
            addValidationError(list, MSGCODE_NAME_IS_EMPTY, Messages.ProductCmptCategory_msgNameIsEmpty, PROPERTY_NAME);
            return false;
        }
        return true;
    }

    private boolean validateNameAlreadyUsedInTypeHierarchy(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        boolean valid = !isNameAlreadyUsedInThisType();
        if (valid) {
            valid = !isNameAlreadyUsedInSupertypeHierarchy(ipsProject);
        }

        if (!valid) {
            String text = NLS.bind(Messages.ProductCmptCategory_msgNameAlreadyUsedInTypeHierarchy, name,
                    getProductCmptType().getName());
            addValidationError(list, MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY, text, PROPERTY_NAME);
        }

        return valid;
    }

    private boolean isNameAlreadyUsedInThisType() {
        for (IProductCmptCategory category : getProductCmptType().getProductCmptCategories()) {
            if (category == this) {
                continue;
            }
            if (category.getName().equals(name)) {
                return true;
            }
        }
        return false;
    }

    private boolean isNameAlreadyUsedInSupertypeHierarchy(IIpsProject ipsProject) throws CoreException {
        if (!inherited && getProductCmptType().hasSupertype()) {
            IProductCmptType superProductCmptType = getProductCmptType().findSuperProductCmptType(ipsProject);
            if (superProductCmptType != null) {
                if (superProductCmptType.findProductCmptCategory(name, ipsProject) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean validateInheritedButNoSupertype(MessageList list) {
        if (!getProductCmptType().hasSupertype()) {
            String text = NLS.bind(Messages.ProductCmptCategory_msgInheritedButNoSupertype, name, getProductCmptType()
                    .getName());
            addValidationError(list, MSGCODE_INHERITED_BUT_NO_SUPERTYPE, text, PROPERTY_INHERITED);
            return false;
        }
        return true;
    }

    private boolean validateInheritedButNotFoundInSupertypeHierarchy(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (inherited && getProductCmptType().findProductCmptCategory(name, ipsProject) == null) {
            IProductCmptType superProductCmptType = getProductCmptType().findSuperProductCmptType(ipsProject);
            if (superProductCmptType == null || superProductCmptType.findProductCmptCategory(name, ipsProject) == null) {
                String text = NLS.bind(Messages.ProductCmptCategory_msgInheritedButNotFoundInSupertypeHierarchy, name);
                addValidationError(list, MSGCODE_INHERITED_BUT_NOT_FOUND_IN_SUPERTYPE_HIERARCHY, text,
                        PROPERTY_INHERITED);
                return false;
            }
        }
        return true;
    }

    private void validateDuplicateDefaultsForFormulaSignatureDefinitions(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForFormulaSignatureDefinitions) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForFormulaSignatureDefinitions();
            }
        };
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationWarningIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_FORMULA_SIGNATURE_DEFINITIONS,
                Messages.ProductCmptCategory_DuplicateDefaultsForFormulaSignatureDefinitions,
                PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS);
    }

    private void validateDuplicateDefaultsForValidationRules(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForValidationRules) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForValidationRules();
            }
        };
        duplicateFinder.start(getProductCmptType());
        duplicateFinder
                .addValidationWarningIfDuplicateFound(list, MSGCODE_DUPLICATE_DEFAULTS_FOR_VALIDATION_RULES,
                        Messages.ProductCmptCategory_DuplicateDefaultsForValidationRules,
                        PROPERTY_DEFAULT_FOR_VALIDATION_RULES);
    }

    private void validateDuplicateDefaultsForTableStructureUsages(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForTableStructureUsages) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForTableStructureUsages();
            }
        };
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationWarningIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_TABLE_STRUCTURE_USAGES,
                Messages.ProductCmptCategory_DuplicateDefaultsForTableStructureUsages,
                PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES);
    }

    private void validateDuplicateDefaultsForPolicyCmptTypeAttributes(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForPolicyCmptTypeAttributes) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForPolicyCmptTypeAttributes();
            }
        };
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationWarningIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_POLICY_CMPT_TYPE_ATTRIBUTES,
                Messages.ProductCmptCategory_DuplicateDefaultsForPolicyCmptTypeAttributes,
                PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES);
    }

    private void validateDuplicateDefaultsForProductCmptTypeAttributes(MessageList list, IIpsProject ipsProject)
            throws CoreException {

        if (!defaultForProductCmptTypeAttributes) {
            return;
        }

        DuplicateDefaultFinder duplicateFinder = new DuplicateDefaultFinder(ipsProject) {
            @Override
            protected boolean isDefault(IProductCmptCategory category) {
                return category.isDefaultForProductCmptTypeAttributes();
            }
        };
        duplicateFinder.start(getProductCmptType());
        duplicateFinder.addValidationWarningIfDuplicateFound(list,
                MSGCODE_DUPLICATE_DEFAULTS_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                Messages.ProductCmptCategory_DuplicateDefaultsForProductCmptTypeAttributes,
                PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES);
    }

    @Override
    public int[] moveProductCmptPropertyReferences(int[] indexes, boolean up) {
        ListElementMover<IProductCmptPropertyReference> mover = new ListElementMover<IProductCmptPropertyReference>(
                propertyReferences);
        int[] newIndexes = mover.move(indexes, up);
        partsMoved(propertyReferences.toArray(new IIpsObjectPart[propertyReferences.size()]));
        return newIndexes;
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        inherited = Boolean.parseBoolean(element.getAttribute(PROPERTY_INHERITED));
        defaultForFormulaSignatureDefinitions = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS));
        defaultForPolicyCmptTypeAttributes = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES));
        defaultForProductCmptTypeAttributes = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES));
        defaultForTableStructureUsages = Boolean.parseBoolean(element
                .getAttribute(PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES));
        defaultForValidationRules = Boolean.parseBoolean(element.getAttribute(PROPERTY_DEFAULT_FOR_VALIDATION_RULES));
        position = Position.valueOf(element.getAttribute(PROPERTY_POSITION).toUpperCase());

        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_INHERITED, Boolean.toString(inherited));
        element.setAttribute(PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS,
                Boolean.toString(defaultForFormulaSignatureDefinitions));
        element.setAttribute(PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES,
                Boolean.toString(defaultForPolicyCmptTypeAttributes));
        element.setAttribute(PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES,
                Boolean.toString(defaultForProductCmptTypeAttributes));
        element.setAttribute(PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES,
                Boolean.toString(defaultForTableStructureUsages));
        element.setAttribute(PROPERTY_DEFAULT_FOR_VALIDATION_RULES, Boolean.toString(defaultForValidationRules));
        element.setAttribute(PROPERTY_POSITION, position.toString().toLowerCase());
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        IIpsElement[] children = new IIpsElement[propertyReferences.size()];
        for (int i = 0; i < children.length; i++) {
            children[i] = propertyReferences.get(i);
        }
        return children;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        propertyReferences.clear();
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        if (part instanceof IProductCmptPropertyReference) {
            propertyReferences.add((IProductCmptPropertyReference)part);
            return true;
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        if (part instanceof IProductCmptPropertyReference) {
            return propertyReferences.remove(part);
        }
        return false;
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        String nodeName = xmlTag.getNodeName();
        if (nodeName.equals(IProductCmptPropertyDirectReference.XML_TAG_NAME)) {
            return newPart(ProductCmptPropertyDirectReference.class);
        } else if (nodeName.equals(IProductCmptPropertyExternalReference.XML_TAG_NAME)) {
            return newPart(ProductCmptPropertyExternalReference.class);
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        IProductCmptPropertyReference reference = null;
        if (partType == ProductCmptPropertyDirectReference.class) {
            reference = new ProductCmptPropertyDirectReference(this, getNextPartId());
            propertyReferences.add(reference);
        } else if (partType == ProductCmptPropertyExternalReference.class) {
            reference = new ProductCmptPropertyExternalReference(this, getNextPartId());
            propertyReferences.add(reference);
        }
        return reference;
    }

    @Override
    protected void partsToXml(Document doc, Element element) {
        for (IIpsElement child : getChildren()) {
            IIpsObjectPart part = (IIpsObjectPart)child;
            if (child instanceof IProductCmptPropertyReference) {
                IProductCmptPropertyReference reference = (IProductCmptPropertyReference)part;
                if (!reference.isExternalReference()) {
                    IProductCmptProperty referencedProperty = null;
                    try {
                        referencedProperty = reference.findReferencedProductCmptProperty(getIpsProject());
                    } catch (CoreException e) {
                        // Property is not found due to exception, log the exception and continue
                        IpsPlugin.log(e);
                        continue;
                    }
                    if (referencedProperty == null) {
                        continue;
                    }
                }
            }
            Element newPartElement = part.toXml(doc);
            element.appendChild(newPartElement);
        }
    }

    private void addValidationError(MessageList list, String code, String text, String invalidProperty) {
        addValidationMessage(list, code, text, invalidProperty, Message.ERROR);
    }

    private void addValidationWarning(MessageList list, String code, String text, String invalidProperty) {
        addValidationMessage(list, code, text, invalidProperty, Message.WARNING);
    }

    private void addValidationMessage(MessageList list, String code, String text, String invalidProperty, int severity) {
        list.add(new Message(code, text, severity, this, invalidProperty));
    }

    private abstract class DuplicateDefaultFinder extends TypeHierarchyVisitor<IProductCmptType> {

        private boolean duplicateDefaultFound;

        protected DuplicateDefaultFinder(IIpsProject ipsProject) {
            super(ipsProject);
        }

        @Override
        protected boolean visit(IProductCmptType currentType) throws CoreException {
            for (IProductCmptCategory category : currentType.getProductCmptCategories()) {
                if (isDefault(category) && !category.getName().equals(name)) {
                    duplicateDefaultFound = true;
                    return false;
                }
            }
            return true;
        }

        protected abstract boolean isDefault(IProductCmptCategory category);

        private void addValidationWarningIfDuplicateFound(MessageList list,
                String code,
                String text,
                String invalidProperty) {

            if (duplicateDefaultFound) {
                addValidationWarning(list, code, text, invalidProperty);
            }
        }

    }

}