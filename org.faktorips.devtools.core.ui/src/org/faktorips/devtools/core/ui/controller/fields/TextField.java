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

import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.faktorips.util.ArgumentCheck;

/**
 * Edit field for text controls.
 */
public class TextField extends DefaultEditField {

    private Text text;

    private boolean immediatelyNotifyListener = false;

    public TextField(Text text) {
        super();
        ArgumentCheck.notNull(text);
        this.text = text;
    }

    @Override
    public Control getControl() {
        return text;
    }

    /**
     * Returns the text control this is an edit field for.
     */
    public Text getTextControl() {
        return text;
    }

    @Override
    public Object parseContent() {
        return super.prepareObjectForGet(text.getText());
    }

    @Override
    public void setValue(Object newValue) {
        setText((String)super.prepareObjectForSet(newValue));
    }

    @Override
    public String getText() {
        return text.getText();
    }

    @Override
    public void setText(String newText) {
        immediatelyNotifyListener = true;
        try {
            text.setText(newText);
        } finally {
            immediatelyNotifyListener = false;
        }
    }

    @Override
    public void insertText(String insertText) {
        text.insert(insertText);
    }

    @Override
    public void selectAll() {
        text.selectAll();
    }

    @Override
    protected void addListenerToControl() {
        text.addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                notifyChangeListeners(new FieldValueChangedEvent(TextField.this), immediatelyNotifyListener);
            }
        });
    }

}
