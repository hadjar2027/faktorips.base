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

package org.faktorips.devtools.formulalibrary.internal.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
    private static final String BUNDLE_NAME = "org.faktorips.devtools.formulalibrary.internal.model.messages"; //$NON-NLS-1$
    static {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
    }

    public static String FormulaLibraryIpsObjectType_nameFormulaLibrary;
    public static String FormulaLibraryIpsObjectType_nameFormulaLibraryPlural;

    public static String FormulaLibrary_msgDuplicateFormulaName;
    public static String FormulaLibrary_msgDuplicateSignature;
    public static String FormulaFunction_expression;
    public static String FormulaMethod_FormulaNameIsMissing;
    public static String FormulaMethod_FormulaSignatureDatatypeMustBeAValueDatatype;
    public static String FormulaMethod_msgInvalidFormulaName;
}