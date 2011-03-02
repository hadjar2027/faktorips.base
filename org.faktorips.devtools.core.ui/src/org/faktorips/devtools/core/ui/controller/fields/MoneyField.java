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

package org.faktorips.devtools.core.ui.controller.fields;

import java.util.ArrayList;
import java.util.Currency;
import java.util.List;

import org.eclipse.swt.widgets.Control;
import org.faktorips.devtools.core.ui.IpsUIPlugin;
import org.faktorips.devtools.core.ui.controller.EditField;
import org.faktorips.devtools.core.ui.controls.TextComboControl;
import org.faktorips.util.message.MessageList;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;

/**
 * This class is a {@link EditField} for money values. The combo control is populated with the
 * currency objects returned by IpsUIPlugin#getCurrencies(). The text control is managed by a
 * FormattedTextField with decimal format.
 * <p/>
 * This field in essence is a "composite" field. It uses a field for each the text and the combo
 * control and forwards events sent by them to its own listeners.
 * 
 * @author Stefan Widmaier, FaktorZehn AG
 */
public class MoneyField extends DefaultEditField {

    private TextComboControl control;
    private ComboField currencyField;
    private FormattingTextField valueField;

    protected boolean immediatelyNotifyListener = false;

    public MoneyField(TextComboControl control) {
        super();
        this.control = control;
        valueField = new FormattingTextField(control.getTextControl(), new DoubleFormat());
        currencyField = new ComboField(control.getComboControl());

        List<Currency> currencies = IpsUIPlugin.getDefault().getCurrencies();
        initComboWithCurrencies(currencies);
    }

    private void initComboWithCurrencies(List<Currency> currencies) {
        List<String> currencyNames = new ArrayList<String>();
        for (Currency currency : currencies) {
            currencyNames.add(currency.getCurrencyCode());
        }
        control.getComboControl().setItems(currencyNames.toArray(new String[currencyNames.size()]));
    }

    @Override
    public Control getControl() {
        return control;
    }

    public TextComboControl getTextComboControl() {
        return control;
    }

    @Override
    public void setMessages(MessageList list) {
        MessageCueController.setMessageCue(control.getTextControl(), list);
    }

    @Override
    public Object parseContent() {
        Money money = Money.valueOf(Decimal.valueOf((String)valueField.getValue()),
                Currency.getInstance((String)currencyField.getValue()));
        if (money != Money.NULL) {
            return money.toString();
        } else {
            return null;
        }
    }

    @Override
    public void setValue(Object newValue) {
        setText((String)newValue);
    }

    /**
     * {@inheritDoc} Returns (<value>+" "+<currency>).
     */
    @Override
    public String getText() {
        return valueField.getValue() + " " + currencyField.getValue(); //$NON-NLS-1$
    }

    @Override
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            setTextInternal(newText);
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    private void setTextInternal(String newText) {
        try {
            Money money = Money.valueOf(newText);
            if (money != Money.NULL) {
                valueField.setValue(money.getAmount().toString());
                currencyField.setValue(money.getCurrency().toString());
                return;
            }
        } catch (IllegalArgumentException e) {
            // fall through
        }
        valueField.setText(newText);
        // TODO setting default currency in project properties
        currencyField.setText("EUR"); //$NON-NLS-1$
    }

    @Override
    public void insertText(String text) {
        control.getTextControl().insert(text);
    }

    @Override
    public void selectAll() {
        control.getTextControl().selectAll();
    }

    @Override
    protected void addListenerToControl() {
        FieldValueChangeEventForwarder forwarder = new FieldValueChangeEventForwarder();
        valueField.addChangeListener(forwarder);
        currencyField.addChangeListener(forwarder);
    }

    public class FieldValueChangeEventForwarder implements ValueChangeListener {
        @Override
        public void valueChanged(FieldValueChangedEvent e) {
            /*
             * Always send events immediately as this is only a forwarding mechanism. Events are
             * delayed as usual in the called notifyChangeListeners() method.
             */
            MoneyField.this.notifyChangeListeners(new FieldValueChangedEvent(MoneyField.this), true);
        }

    }

}
