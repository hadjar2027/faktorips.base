/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.ui.controlfactories;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Display;
import org.faktorips.devtools.core.ui.controlfactories.BooleanComboField;

import junit.framework.TestCase;

/**
 * 
 * @author Jan Ortmann
 */
public class BooleanComboFieldTest extends TestCase {

    public void testSetText() {
        Combo c = new Combo(Display.getDefault().getActiveShell(), SWT.READ_ONLY);
        c.setItems(new String[]{"true", "false"});
        BooleanComboField field = new BooleanComboField(c, "true", "false");
        
        field.setText("false");
        assertEquals("false", field.getValue()); // in this case get value returns a string 
        assertTrue(field.isTextContentParsable());
        
        field.setText("true");
        assertEquals("true", field.getValue()); 
        assertTrue(field.isTextContentParsable());

        field.setText("unkown");
        assertEquals("false", field.getValue()); 
        assertTrue(field.isTextContentParsable());
    }
    
    public void testSetValue() {
        Combo c = new Combo(Display.getDefault().getActiveShell(), SWT.READ_ONLY);
        c.setItems(new String[]{"true", "false"});
        BooleanComboField field = new BooleanComboField(c, "true", "false");
        
        field.setValue("false");
        assertEquals("false", field.getValue()); // in this case get value returns a string 
        assertTrue(field.isTextContentParsable());
        
        field.setValue("true");
        assertEquals("true", field.getValue()); 
        assertTrue(field.isTextContentParsable());

        field.setValue("unkown");
        assertEquals("false", field.getValue()); 
        assertTrue(field.isTextContentParsable());
        
        
    }
    
}
