/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.Locale;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.ipsobject.AtomicIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValueContainer;
import org.faktorips.devtools.core.model.productcmpt.ITableContentUsage;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.ITableStructureUsage;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;
import org.faktorips.devtools.core.model.type.ProductCmptPropertyType;
import org.faktorips.runtime.internal.ValueToXmlHelper;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * 
 * @author Thorsten Guenther
 */
public class TableContentUsage extends AtomicIpsObjectPart implements ITableContentUsage {

    public static final String TAG_NAME = "TableContentUsage"; //$NON-NLS-1$
    /**
     * The full qualified name of the table content this usage defines.
     */
    private String tableContentName = ""; //$NON-NLS-1$

    /**
     * The role-name of the structure usage this content usage is based on.
     */
    private String structureUsage = ""; //$NON-NLS-1$

    public TableContentUsage(IPropertyValueContainer generation, String id) {
        super(generation, id);
    }

    public TableContentUsage(IPropertyValueContainer generation, String id, String structureUsage) {
        super(generation, id);
        this.structureUsage = structureUsage;
    }

    @Override
    public final IPropertyValueContainer getPropertyValueContainer() {
        return (IPropertyValueContainer)getParent();
    }

    public TableContentUsage() {
        super();
    }

    @Override
    public String getPropertyName() {
        return structureUsage;
    }

    @Override
    public IProductCmptProperty findProperty(IIpsProject ipsProject) throws CoreException {
        return findTableStructureUsage(ipsProject);
    }

    @Override
    public ProductCmptPropertyType getPropertyType() {
        return ProductCmptPropertyType.TABLE_STRUCTURE_USAGE;
    }

    @Override
    public String getPropertyValue() {
        return tableContentName;
    }

    @Override
    public IProductCmptGeneration getProductCmptGeneration() {
        return (ProductCmptGeneration)getParent();
    }

    private IProductCmptType getProductCmptType(IIpsProject ipsProject) throws CoreException {
        return getProductCmptGeneration().findProductCmptType(ipsProject);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(TAG_NAME);
    }

    @Override
    public void setStructureUsage(String structureUsage) {
        this.structureUsage = structureUsage;
        objectHasChanged();
    }

    @Override
    public String getStructureUsage() {
        return structureUsage;
    }

    @Override
    public void setTableContentName(String tableContentName) {
        this.tableContentName = tableContentName;
        objectHasChanged();
    }

    @Override
    public String getName() {
        return getTableContentName();
    }

    @Override
    public String getTableContentName() {
        return tableContentName;
    }

    @Override
    public ITableContents findTableContents(IIpsProject ipsProject) throws CoreException {
        return (ITableContents)ipsProject.findIpsObject(IpsObjectType.TABLE_CONTENTS, tableContentName);
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        IProductCmptType type = getProductCmptType(ipsProject);
        if (type == null) {
            list.add(new Message(MSGCODE_NO_TYPE, Messages.TableContentUsage_msgNoType, Message.WARNING, this));
            return;
        }

        ITableStructureUsage tsu = type.findTableStructureUsage(structureUsage, ipsProject);
        if (tsu == null) {
            String text = NLS.bind(Messages.TableContentUsage_msgUnknownStructureUsage, structureUsage);
            list.add(new Message(MSGCODE_UNKNOWN_STRUCTURE_USAGE, text, Message.ERROR, this, PROPERTY_STRUCTURE_USAGE));
            return;
        }

        ITableContents content = null;
        if (tableContentName != null) {
            content = findTableContents(ipsProject);
        }
        if (content == null) {
            if (StringUtils.isNotEmpty(tableContentName) || (tsu.isMandatoryTableContent())) {
                String text = NLS.bind(Messages.TableContentUsage_msgUnknownTableContent, tableContentName);
                list.add(new Message(MSGCODE_UNKNOWN_TABLE_CONTENT, text, Message.ERROR, this, PROPERTY_TABLE_CONTENT));
            }
            return;
        }
        String usedStructure = content.getTableStructure();
        if (!tsu.isUsed(usedStructure)) {
            String[] params = { tableContentName, usedStructure, structureUsage };
            String text = NLS.bind(Messages.TableContentUsage_msgInvalidTableContent, params);
            list.add(new Message(MSGCODE_INVALID_TABLE_CONTENT, text, Message.ERROR, this, PROPERTY_TABLE_CONTENT));
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        super.initPropertiesFromXml(element, id);
        structureUsage = element.getAttribute(PROPERTY_STRUCTURE_USAGE);
        tableContentName = ValueToXmlHelper.getValueFromElement(element, "TableContentName"); //$NON-NLS-1$
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_STRUCTURE_USAGE, structureUsage);
        ValueToXmlHelper.addValueToElement(tableContentName, element, "TableContentName"); //$NON-NLS-1$
    }

    @Override
    public ITableStructureUsage findTableStructureUsage(IIpsProject ipsProject) throws CoreException {
        IProductCmptType type = getProductCmptType(ipsProject);
        if (type == null) {
            return null;
        }
        return type.findTableStructureUsage(structureUsage, ipsProject);
    }

    @Override
    public String getCaption(Locale locale) throws CoreException {
        ArgumentCheck.notNull(locale);

        String caption = null;
        ITableStructureUsage structureUsage = findTableStructureUsage(getIpsProject());
        if (structureUsage != null) {
            caption = structureUsage.getLabelValue(locale);
        }
        return caption;
    }

    @Override
    public String getLastResortCaption() {
        return StringUtils.capitalize(structureUsage);
    }

}
