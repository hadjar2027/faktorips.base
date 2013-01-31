/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.dialogs;

import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.productcmpt.SingleValueHolder;
import org.faktorips.devtools.core.model.IInternationalString;
import org.faktorips.devtools.core.model.ILocalizedString;
import org.faktorips.devtools.core.model.value.IValue;
import org.faktorips.devtools.core.model.value.ValueFactory;
import org.faktorips.devtools.core.model.value.ValueType;
import org.faktorips.devtools.core.ui.controls.tableedit.IElementModifier;
import org.faktorips.devtools.core.ui.dialogs.MultiValueTableModel.SingleValueViewItem;

/**
 * Allows to access and modify {@link SingleValueHolder} instances.
 * 
 * @author Stefan Widmaier
 */
public class MultiValueElementModifier implements IElementModifier {

    /**
     * Assumes the given element is a {@link SingleValueHolder}. Returns its string value.
     * {@inheritDoc}
     */
    @Override
    public String getValue(Object element) {
        SingleValueHolder item = ((SingleValueViewItem)element).getSingleValueHolder();
        if (item == null || item.getValue() == null) {
            return IpsPlugin.getDefault().getIpsPreferences().getNullPresentation();
        } else if (item.getValueType() == ValueType.STRING) {
            return (String)item.getValue().getContent();
        }
        ILocalizedString locString = ((IValue<IInternationalString>)item.getValue()).getContent().get(
                IpsPlugin.getDefault().getUsedLanguagePackLocale());
        return locString == null ? IpsPlugin.getDefault().getIpsPreferences().getNullPresentation() : locString
                .getValue();
    }

    /**
     * Assumes the given element is a {@link SingleValueHolder} and the given value is a
     * {@link String}. Sets the given string as new value of the given element. {@inheritDoc}
     */
    @Override
    public void setValue(Object element, Object value) {
        // TODO FIPS-1469
        SingleValueHolder singleValueHolder = ((SingleValueViewItem)element).getSingleValueHolder();
        if (value instanceof String) {
            singleValueHolder.setValue(ValueFactory.createStringValue((String)value));
        }
    }

}
