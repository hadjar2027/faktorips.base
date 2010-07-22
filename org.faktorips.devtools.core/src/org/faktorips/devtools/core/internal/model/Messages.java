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
package org.faktorips.devtools.core.internal.model;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.internal.model.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String TableContentsEnumDatatypeAdapter_1;
    public static String TableContentsEnumDatatypeAdapter_3;

    public static String ValidationUtils_msgObjectDoesNotExist;
    public static String ValidationUtils_msgDatatypeDoesNotExist;
    public static String ValidationUtils_msgVoidNotAllowed;
    public static String ValidationUtils_msgPropertyMissing;
    public static String ValidationUtils_VALUE_VALUEDATATYPE_NOT_FOUND;
    public static String ValidationUtils_VALUEDATATYPE_INVALID;
    public static String ValidationUtils_NO_INSTANCE_OF_VALUEDATATYPE;

}
