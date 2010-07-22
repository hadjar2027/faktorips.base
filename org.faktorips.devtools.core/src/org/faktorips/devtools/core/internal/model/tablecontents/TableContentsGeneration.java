/*******************************************************************************
 * Copyright © 2005-2010 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen, 
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 ******************************************************************************/
package org.faktorips.devtools.core.internal.model.tablecontents;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectGeneration;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablecontents.IRow;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Element;

public class TableContentsGeneration extends IpsObjectGeneration implements ITableContentsGeneration {

    private List<Row> rows = new ArrayList<Row>(100);

    private UniqueKeyValidator uniqueKeyValidator;

    public TableContentsGeneration(TableContents parent, String id) {
        super(parent, id);
    }

    @Override
    public IIpsElement[] getChildren() {
        return getRows();
    }

    @Override
    protected void setValidFromInternal(GregorianCalendar validFrom) {
        super.setValidFromInternal(validFrom);
    }

    @Override
    public IRow[] getRows() {
        IRow[] r = new IRow[rows.size()];
        rows.toArray(r);
        return r;
    }

    @Override
    public IRow getRow(int rowIndex) {
        if (rowIndex < 0 || rowIndex >= getNumOfRows()) {
            return null;
        }
        return rows.get(rowIndex);
    }

    @Override
    public int getNumOfRows() {
        return rows.size();
    }

    @Override
    public IRow newRow() {
        IRow newRow = newRowInternal(getNextPartId());
        objectHasChanged();
        return newRow;
    }

    /**
     * This method is used by the table contents sax handler, after finishing a row node
     */
    Row newRow(List<String> columns) {
        Row newRow = newRowInternal(getNextPartId());
        int column = 0;
        for (String value : columns) {
            newRow.setValueInternal(column++, value);
        }

        updateUniqueKeyCacheFor(newRow);

        return newRow;
    }

    /**
     * Creates a new row and inserts it into the list of rows. The rownumber of the new row is its
     * index in the list (respectively the number of rows before the insertion).
     */
    private Row newRowInternal(String id) {
        int nextRowNumber = getNumOfRows();
        Row newRow = new Row(this, id);
        rows.add(newRow);
        newRow.setRowNumber(nextRowNumber);
        return newRow;
    }

    public void newColumn(int insertAt, String defaultValue) {
        for (Row row : rows) {
            row.newColumn(insertAt, defaultValue);
        }
        clearUniqueKeyValidator();
    }

    public void removeColumn(int column) {
        for (Row row : rows) {
            row.removeColumn(column);
        }
        clearUniqueKeyValidator();
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, String id) {
        String xmlTagName = xmlTag.getNodeName();
        if (xmlTagName.equals(Row.TAG_NAME)) {
            return newRowInternal(id);
        }
        throw new RuntimeException("Could not create part for tag name" + xmlTagName); //$NON-NLS-1$
    }

    @Override
    protected void addPart(IIpsObjectPart part) {
        if (part instanceof IRow) {
            rows.add((Row)part);
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    /**
     * Removes the given row from the list of rows and updates the rownumbers of all following rows.
     */
    @Override
    protected void removePart(IIpsObjectPart part) {
        if (part instanceof IRow) {
            Row row = (Row)part;
            int delIndex = rows.indexOf(row);
            if (delIndex != -1) {
                rows.remove(delIndex);
                // update rownumbers after delete
                for (int i = delIndex; i < rows.size(); i++) {
                    Row updateRow = rows.get(i);
                    updateRow.setRowNumber(i);
                }
                removeUniqueKeyCacheFor(row);
            }
            return;
        }
        throw new RuntimeException("Unknown part type" + part.getClass()); //$NON-NLS-1$
    }

    @Override
    protected void reinitPartCollections() {
        rows.clear();
    }

    @Override
    public IIpsObjectPart newPart(Class<?> partType) {
        if (partType.equals(IRow.class)) {
            return newRowInternal(getNextPartId());
        }

        throw new IllegalArgumentException("Unknown part type" + partType); //$NON-NLS-1$
    }

    @Override
    public void clear() {
        rows.clear();
        clearUniqueKeyValidator();
        objectHasChanged();
    }

    public ITableContents getTableContents() {
        return (ITableContents)parent;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        super.validateThis(list, ipsProject);
        if (!list.isEmpty()) {
            return;
        }
        ITableStructure structure = getTableContents().findTableStructure(ipsProject);
        if (structure == null) {
            return;
        }
    }

    @Override
    public IRow insertRowAfter(int rowIndex) {
        int newRowIndex = (rowIndex + 1) > getNumOfRows() ? getNumOfRows() : rowIndex + 1;
        Row newRow = new Row(this, getNextPartId());
        rows.add(newRowIndex, newRow);
        newRow.setRowNumber(newRowIndex);
        refreshRowNumbers();
        objectHasChanged();
        return newRow;
    }

    /**
     * Refreshs the row number of all rows.
     */
    private void refreshRowNumbers() {
        int index = 0;
        for (Row row : rows) {
            row.setRowNumber(index++);
        }
    }

    @Override
    protected void validateChildren(MessageList result, IIpsProject ipsProject) throws CoreException {
        ITableStructure tableStructure = getTableContents().findTableStructure(ipsProject);
        if (tableStructure == null) {
            return;
        }
        ValueDatatype[] datatypes = ((TableContents)getTableContents()).findColumnDatatypes(tableStructure, ipsProject);

        IIpsElement[] children = getChildren();
        for (IIpsElement element : children) {
            Row row = (Row)element;
            MessageList list = row.validateThis(tableStructure, datatypes, ipsProject);
            result.add(list);
        }

        validateUniqueKeys(result, tableStructure, datatypes);
    }

    //
    // Methods to return the xml representation constants for the extension properties
    // The constants are necessary because the TableContents supports the initialisation via SAX.
    // And the corresponding SAX handler uses this constants for node identification.
    //
    static String getXmlExtPropertiesElementName() {
        return XML_EXT_PROPERTIES_ELEMENT;
    }

    static String getXmlValueElement() {
        return XML_VALUE_ELEMENT;
    }

    static String getXmlAttributeExtpropertyid() {
        return XML_ATTRIBUTE_EXTPROPERTYID;
    }

    static String getXmlAttributeIsnull() {
        return XML_ATTRIBUTE_ISNULL;
    }

    /**
     * Updates the whole unique key cache. The unique key(s) of all rows will be updated.
     */
    private void updateUniqueKeyCache(ITableStructure tableStructure) {
        uniqueKeyValidator.clearUniqueKeyCache();
        IRow[] rows = getRows();
        for (IRow row : rows) {
            updateUniqueKeyCacheFor((Row)row);
        }
        uniqueKeyValidator.cacheTableStructureAndValueDatatypes(this);
        // store the last table structure modification time to detect changes of the table structure
        uniqueKeyValidator.setTableStructureModificationTimeStamp(tableStructure);
    }

    /**
     * Returns <code>true</code> if there was an unique key error state change, e.g. current state
     * is error previous state was error free
     */
    public boolean wasUniqueKeyErrorStateChange() {
        return uniqueKeyValidator.wasErrorStateChange();
    }

    /**
     * Validates the unique keys of all rows.
     */
    public void validateUniqueKeys(MessageList list, ITableStructure tableStructure, ValueDatatype[] datatypes) {
        if (isUniqueKeyValidationEnabled()) {
            // check if the unique key cache needs to be updated
            if (uniqueKeyValidator.isEmtpy() || uniqueKeyValidator.isInvalidUniqueKeyCache(tableStructure)) {
                // could be happen if a new column was added to the table generation
                // or the structure has changed, e.g. a new unique key was added
                updateUniqueKeyCache(tableStructure);
            }

            uniqueKeyValidator.validateAllUniqueKeys(list, tableStructure, datatypes);
        }
    }

    /**
     * returns <code>true</code> if the unique key validation is enabled
     */
    public boolean isUniqueKeyValidationEnabled() {
        return uniqueKeyValidator != null;
    }

    /**
     * Clears the unique key cache, e.g. if a column was added or removed or the table contents
     * generation will cleared
     */
    public void clearUniqueKeyValidator() {
        if (isUniqueKeyValidationEnabled()) {
            uniqueKeyValidator.clearUniqueKeyCache();
        }
    }

    /**
     * Updates the unique key cache for the given row
     */
    void updateUniqueKeyCacheFor(Row row) {
        if (isUniqueKeyValidationEnabled()) {
            uniqueKeyValidator.handleRowChanged(this, row);
        }
    }

    /**
     * Removes the row in the cached unique keys
     */
    private void removeUniqueKeyCacheFor(Row row) {
        if (isUniqueKeyValidationEnabled()) {
            uniqueKeyValidator.handleRowRemoved(this, row);
        }
    }

    /**
     * Initializes the unique key cache.
     */
    public void initUniqueKeyValidator(ITableStructure tableStructure, UniqueKeyValidator uniqueKeyValidator) {
        this.uniqueKeyValidator = uniqueKeyValidator;
        if (tableStructure != null && uniqueKeyValidator != null) {
            updateUniqueKeyCache(tableStructure);
        }
    }

}
