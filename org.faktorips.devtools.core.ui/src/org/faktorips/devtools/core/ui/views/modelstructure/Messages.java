/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modelstructure;

import org.eclipse.osgi.util.NLS;

public final class Messages extends NLS {

    private static final String BUNDLE_NAME = "org.faktorips.devtools.core.ui.views.modelstructure.messages"; //$NON-NLS-1$

    static {
        NLS.initializeMessages(BUNDLE_NAME, Messages.class);
    }

    private Messages() {
        // Messages bundles shall not be initialized.
    }

    public static String ModelStructure_tooltipToggleButton;
    public static String ModelStructure_menuShowCardinalities_name;
    public static String ModelStructure_menuShowCardinalities_tooltip;
    public static String ModelStructure_menuShowRoleName_name;
    public static String ModelStructure_menuShowRoleName_tooltip;
    public static String ModelStructure_contextMenuOpenAssociationTargetingTypeEditor;
    public static String ModelStructure_waitingLabel;
    public static String ModelStructure_emptyMessage;
    public static String ModelStructure_menuShowProjects_name;
    public static String ModelStructure_menuShowProjects_tooltip;
    public static String ModelStructure_NothingToShow_message;

    public static String ModelStructure_tooltipInheritedAssociations;
    public static String ModelStructure_tooltipHasInheritedAssociation;

    public static String ModelStructure_tooltipRefreshContents;
}