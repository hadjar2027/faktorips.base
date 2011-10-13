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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptCategory;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptPropertyExternalReference;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IProductCmptPropertyExternalReference}, please see the interface for
 * more details.
 * 
 * @author Alexander Weickmann
 */
public final class ProductCmptPropertyExternalReference extends ProductCmptPropertyReference implements
        IProductCmptPropertyExternalReference {

    private ProductCmptPropertyType propertyType;

    public ProductCmptPropertyExternalReference(IProductCmptCategory parentCategory, String id) {
        super(parentCategory, id);
    }

    @Override
    public void setName(String name) {
        String oldValue = this.name;
        this.name = name;
        valueChanged(oldValue, name);
    }

    @Override
    public void setProductCmptPropertyType(ProductCmptPropertyType propertyType) {
        ArgumentCheck.isTrue(propertyType.equals(ProductCmptPropertyType.POLICY_CMPT_TYPE_ATTRIBUTE)
                || propertyType.equals(ProductCmptPropertyType.VALIDATION_RULE));

        ProductCmptPropertyType oldValue = this.propertyType;
        this.propertyType = propertyType;
        valueChanged(oldValue, propertyType);
    }

    @Override
    public ProductCmptPropertyType getProductCmptPropertyType() {
        return propertyType;
    }

    @Override
    public IProductCmptProperty findReferencedProductCmptProperty(IIpsProject ipsProject) throws CoreException {
        IProductCmptProperty referencedProperty = null;

        IPolicyCmptType policyCmptType = getProductCmptType().findPolicyCmptType(ipsProject);
        if (policyCmptType != null) {
            switch (propertyType) {
                case POLICY_CMPT_TYPE_ATTRIBUTE:
                    referencedProperty = policyCmptType.getPolicyCmptTypeAttribute(name);
                    break;
                case VALIDATION_RULE:
                    referencedProperty = policyCmptType.getValidationRule(name);
                    break;
                default:
                    break;
            }
        }

        return referencedProperty;
    }

    @Override
    public boolean isReferencingProperty(IProductCmptProperty property) {
        return getName().equals(property.getName()) && propertyType == property.getProductCmptPropertyType();
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        IProductCmptProperty referencedProperty = findReferencedProductCmptProperty(ipsProject);
        validateReferencedPropertyCouldNotBeFound(list, referencedProperty);
    }

    private void validateReferencedPropertyCouldNotBeFound(MessageList list, IProductCmptProperty referencedProperty) {
        if (referencedProperty == null) {
            String text = NLS.bind(Messages.ProductCmptPropertyExternalReference_msgReferencedPropertyCouldNotBeFound,
                    getProductCmptPropertyType().getName(), getName());
            Message msg = new Message(MSGCODE_REFERENCED_PROPERTY_COULD_NOT_BE_FOUND, text, Message.ERROR);
            list.add(msg);
        }
    }

    @Override
    public boolean isExternalReference() {
        return true;
    }

    @Override
    protected void initFromXml(Element element, String id) {
        name = element.getAttribute(PROPERTY_NAME);
        propertyType = ProductCmptPropertyType.getValueById(element.getAttribute(PROPERTY_PROPERTY_TYPE));

        super.initFromXml(element, id);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

}