/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.nebula.jface.gridviewer.GridTableViewer;
import org.eclipse.nebula.jface.gridviewer.GridTreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.valueset.IValueSet;
import org.faktorips.devtools.core.ui.UIToolkit;
import org.faktorips.devtools.core.ui.ValueDatatypeControlFactory;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controller.fields.FormattingTextField;
import org.faktorips.devtools.core.ui.controller.fields.IntegerFormat;
import org.faktorips.devtools.core.ui.table.FormattingTextCellEditor;
import org.faktorips.devtools.core.ui.table.GridTableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.IpsCellEditor;
import org.faktorips.devtools.core.ui.table.TableViewerTraversalStrategy;
import org.faktorips.devtools.core.ui.table.TextCellEditor;

/**
 * A factory for edit fields/controls for the data type Integer and Long. Creates a common text
 * control for editing the value but configures it with a {@link VerifyListener} that prevents
 * illegal characters from being entered. Only digits and "-" are valid for integer and long.
 * 
 * @author Stefan Widmaier
 * @since 3.2
 */
public class IntegerLongControlFactory extends ValueDatatypeControlFactory {

    public IntegerLongControlFactory() {
        super();
    }

    @Override
    public boolean isFactoryFor(ValueDatatype datatype) {
        return Datatype.INTEGER.equals(datatype) | Datatype.LONG.equals(datatype);
    }

    @Override
    public EditField createEditField(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {

        FormattingTextField formatField = new FormattingTextField((Text)createControl(toolkit, parent, datatype,
                valueSet, ipsProject), new IntegerFormat());
        return formatField;
    }

    @Override
    public Control createControl(UIToolkit toolkit,
            Composite parent,
            ValueDatatype datatype,
            IValueSet valueSet,
            IIpsProject ipsProject) {

        Text text = toolkit.createText(parent, SWT.NONE);
        return text;
    }

    /**
     * @deprecated use
     *             {@link #createTableCellEditor(UIToolkit, ValueDatatype, IValueSet, TableViewer, int, IIpsProject)}
     *             instead.
     */
    @Deprecated
    @Override
    public IpsCellEditor createCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        return createTableCellEditor(toolkit, dataType, valueSet, tableViewer, columnIndex, ipsProject);
    }

    /**
     * Creates a {@link TextCellEditor} containing a {@link Text} control and configures it with a
     * {@link TableViewerTraversalStrategy}.
     */
    @Override
    public IpsCellEditor createTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            TableViewer tableViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        IpsCellEditor cellEditor = createTextCellEditor(toolkit, dataType, valueSet, tableViewer.getTable(), ipsProject);
        TableViewerTraversalStrategy strat = new TableViewerTraversalStrategy(cellEditor, tableViewer, columnIndex);
        strat.setRowCreating(true);
        cellEditor.setTraversalStrategy(strat);
        return cellEditor;
    }

    /**
     * Creates a {@link TextCellEditor} containing a {@link Text} control and configures it with a
     * {@link GridTableViewerTraversalStrategy}.
     */
    @Override
    public IpsCellEditor createGridTableCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            GridTableViewer gridViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        IpsCellEditor cellEditor = createTextCellEditor(toolkit, dataType, valueSet, gridViewer.getGrid(), ipsProject);
        cellEditor.setTraversalStrategy(new GridTableViewerTraversalStrategy(cellEditor, gridViewer, columnIndex));
        return cellEditor;
    }

    private IpsCellEditor createTextCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            Composite parent,
            IIpsProject ipsProject) {

        Text textControl = (Text)createControl(toolkit, parent, dataType, valueSet, ipsProject);
        IntegerFormat format = new IntegerFormat();
        IpsCellEditor tableCellEditor = new FormattingTextCellEditor(textControl, format);
        return tableCellEditor;
    }

    @Override
    public IpsCellEditor createGridTreeCellEditor(UIToolkit toolkit,
            ValueDatatype dataType,
            IValueSet valueSet,
            GridTreeViewer gridViewer,
            int columnIndex,
            IIpsProject ipsProject) {

        IpsCellEditor cellEditor = createTextCellEditor(toolkit, dataType, valueSet, gridViewer.getGrid(), ipsProject);
        return cellEditor;
    }
}