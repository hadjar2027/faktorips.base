/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.enums.refactor;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.faktorips.abstracttest.AbstractIpsRefactoringTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumLiteralNameAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.core.refactor.IpsRefactoringProcessor;
import org.faktorips.devtools.core.refactor.IpsRenameProcessor;
import org.junit.Test;

public class RenameEnumAttributeProcessorTest extends AbstractIpsRefactoringTest {

    @Test
    public void testCheckInitialConditionsValid() throws OperationCanceledException, CoreException {
        IpsRefactoringProcessor ipsRefactoringProcessor = new RenameEnumAttributeProcessor(enumAttribute);
        RefactoringStatus status = ipsRefactoringProcessor.checkInitialConditions(new NullProgressMonitor());
        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsValid() throws OperationCanceledException, CoreException {
        IpsRenameProcessor ipsRenameProcessor = new RenameEnumAttributeProcessor(enumAttribute);
        ipsRenameProcessor.setNewName("test");
        RefactoringStatus status = ipsRenameProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertFalse(status.hasError());
    }

    @Test
    public void testCheckFinalConditionsInvalidAttributeName() throws OperationCanceledException, CoreException {
        // Create another enumeration attribute to test against
        IEnumAttribute otherEnumAttribute = enumType.newEnumAttribute();
        otherEnumAttribute.setName("otherEnumAttribute");

        IpsRenameProcessor ipsRenameProcessor = new RenameEnumAttributeProcessor(enumAttribute);
        ipsRenameProcessor.setNewName("otherEnumAttribute");
        RefactoringStatus status = ipsRenameProcessor.checkFinalConditions(new NullProgressMonitor(),
                new CheckConditionsContext());
        assertTrue(status.hasError());
    }

    @Test
    public void testRenameEnumAttribute() throws CoreException {
        enumType.setAbstract(true);

        IEnumType subEnumType = newEnumType(ipsProject, "SubEnumType");
        subEnumType.setEnumContentName(enumType.getEnumContentName());
        subEnumType.setSuperEnumType(enumType.getQualifiedName());

        IEnumAttribute inheritedEnumAttribute = subEnumType.newEnumAttribute();
        inheritedEnumAttribute.setName(enumAttribute.getName());
        inheritedEnumAttribute.setInherited(true);

        String newAttributeName = "newAttributeName";
        performRenameRefactoring(enumAttribute, newAttributeName);

        // Check for changed attribute name.
        assertNull(enumType.getEnumAttribute(ENUM_ATTRIBUTE_NAME));
        assertNotNull(enumType.getEnumAttribute(newAttributeName));
        assertTrue(enumAttribute.getName().equals(newAttributeName));

        // TODO AW: Write extra test for enumeration content reference.

        // Check for inherited attribute update.
        assertNull(subEnumType.getEnumAttributeIncludeSupertypeCopies(ENUM_ATTRIBUTE_NAME));
        assertNotNull(subEnumType.getEnumAttributeIncludeSupertypeCopies(newAttributeName));
        assertEquals(newAttributeName, inheritedEnumAttribute.getName());
    }

    @Test
    public void testRenameEnumAttributeUsedInLiteralName() throws CoreException {
        IEnumType modelEnumType = newEnumType(ipsProject, "ModelEnumType");
        modelEnumType.setContainingValues(true);

        IEnumAttribute enumAttribute = modelEnumType.newEnumAttribute();
        enumAttribute.setName("id");
        enumAttribute.setDatatype(Datatype.STRING.getQualifiedName());
        enumAttribute.setIdentifier(true);
        enumAttribute.setUnique(true);
        enumAttribute.setUsedAsNameInFaktorIpsUi(true);

        IEnumLiteralNameAttribute literalNameAttribute = modelEnumType.newEnumLiteralNameAttribute();
        literalNameAttribute.setDefaultValueProviderAttribute(enumAttribute.getName());

        String newAttributeName = "newAttributeName";
        performRenameRefactoring(enumAttribute, newAttributeName);

        assertEquals(newAttributeName, literalNameAttribute.getDefaultValueProviderAttribute());
    }

}
