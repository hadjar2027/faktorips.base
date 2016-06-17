package org.faktorips.devtools.stdbuilder.util;

/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.enums.EnumTypeDatatypeAdapter;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.core.model.enums.IEnumType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.util.DatatypeHelperUtil;
import org.junit.Test;

public class DatatypeHelperUtilTest extends AbstractStdBuilderTest {

    @Test
    public void testNewInstanceFromExpression_ExtensibleEnum() throws Exception {
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setExtensible(true);
        paymentMode.newEnumLiteralNameAttribute();

        Datatype datatype = ipsProject.findDatatype("PaymentMode");
        DatatypeHelper datatypeHelper = ipsProject.getDatatypeHelper(datatype);

        assertTrue(datatypeHelper instanceof EnumTypeDatatypeHelper);

        EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)datatypeHelper;

        JavaCodeFragment fragment = enumHelper.newInstanceFromExpression("getValue()");

        String repoExpression = "customRepo";

        assertEquals(".getEnumValue(PaymentMode.class, getValue())", fragment.getSourcecode());
        String newInstanceFromExpression = repoExpression + fragment.getSourcecode();
        assertEquals(newInstanceFromExpression,
                DatatypeHelperUtil.getNewInstanceFromExpression(datatypeHelper, "getValue()", repoExpression)
                        .getSourcecode());
    }

    @Test
    public void testNewInstanceFromExpression_NonExtensibleEnum() throws Exception {
        IEnumType paymentMode = newEnumType(ipsProject, "PaymentMode");
        paymentMode.setAbstract(false);
        paymentMode.setExtensible(false);
        paymentMode.newEnumLiteralNameAttribute();

        IEnumAttribute id = paymentMode.newEnumAttribute();
        id.setDatatype(Datatype.STRING.getQualifiedName());
        id.setInherited(false);
        id.setIdentifier(true);
        id.setUnique(true);
        id.setName("id");

        Datatype datatype = ipsProject.findDatatype("PaymentMode");
        DatatypeHelper datatypeHelper = ipsProject.getDatatypeHelper(datatype);

        JavaCodeFragment newInstanceFromExpression = datatypeHelper.newInstanceFromExpression("getValue()");
        assertEquals(newInstanceFromExpression,
                DatatypeHelperUtil.getNewInstanceFromExpression(datatypeHelper, "getValue()", "repo"));
    }

    @Test
    public void testNewInstanceFromExpression_OtherDatatypes() throws Exception {
        DatatypeHelper booleanTypeHelper = ipsProject.getDatatypeHelper(Datatype.BOOLEAN);

        JavaCodeFragment newInstanceFromExpression = booleanTypeHelper.newInstanceFromExpression("getValue()");
        assertEquals(newInstanceFromExpression,
                DatatypeHelperUtil.getNewInstanceFromExpression(booleanTypeHelper, "getValue()", "repo"));
    }

    @Test(expected = CoreRuntimeException.class)
    public void testNewInstanceFromExpression_RethrowsCoreExceptionAsCoreRuntimeException() throws Exception {
        IEnumType extensibleEnumType = mock(IEnumType.class);
        when(extensibleEnumType.isExtensible()).thenReturn(true);
        EnumTypeDatatypeAdapter enumTypeDatatypeAdapter = mock(EnumTypeDatatypeAdapter.class);
        when(enumTypeDatatypeAdapter.getEnumType()).thenReturn(extensibleEnumType);
        EnumTypeDatatypeHelper badHelper = mock(EnumTypeDatatypeHelper.class);
        when(badHelper.getDatatype()).thenReturn(enumTypeDatatypeAdapter);
        doThrow(CoreException.class).when(badHelper).getEnumTypeBuilder();

        DatatypeHelperUtil.getNewInstanceFromExpression(badHelper, "foo", "bar");
    }
}