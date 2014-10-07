/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.productcmpttype;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.ipsobject.Modifier;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.method.IParameter;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpt.IPropertyValue;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptTypeMethod;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

public class ProductCmptTypeMethodTest extends AbstractIpsPluginTest {

    private IProductCmptType productCmptType;

    private IProductCmptTypeMethod method;

    private IIpsProject ipsProject;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject("TestProject");
        productCmptType = newProductCmptType(ipsProject, "Type");
        method = productCmptType.newProductCmptTypeMethod();
    }

    @Test
    public void testMandatoryFormula() {
        method.setFormulaSignatureDefinition(false);
        method.setFormulaMandatory(true);

        assertFalse(method.isFormulaOptionalSupported());
        assertTrue(method.isFormulaMandatory());

        method.setFormulaMandatory(false);

        assertFalse(method.isFormulaOptionalSupported());
        assertTrue(method.isFormulaMandatory());

        method.setFormulaSignatureDefinition(true);
        method.setFormulaMandatory(true);

        assertTrue(method.isFormulaOptionalSupported());
        assertTrue(method.isFormulaMandatory());

        method.setFormulaMandatory(false);

        assertTrue(method.isFormulaOptionalSupported());
        assertFalse(method.isFormulaMandatory());
    }

    @Test
    public void testValidate_FormulaMustntBeAbstract() throws CoreException {
        method.setFormulaSignatureDefinition(true);
        method.setAbstract(true);
        MessageList result = method.validate(method.getIpsProject());
        assertNotNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));

        method.setAbstract(false);
        result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));

        method.setFormulaSignatureDefinition(false);
        method.setAbstract(true);
        result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));

        method.setAbstract(false);
        result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTNT_BE_ABSTRACT));
    }

    @Test
    public void testValidate_FormulaNameIsMissing() throws CoreException {
        method.setFormulaSignatureDefinition(false);
        method.setFormulaName("someName");
        MessageList result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));

        method.setFormulaSignatureDefinition(true);
        result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));

        method.setFormulaName("");
        result = method.validate(method.getIpsProject());
        assertNotNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));

        method.setFormulaSignatureDefinition(false);
        result = method.validate(method.getIpsProject());
        assertNull(result.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_NAME_IS_EMPTY));
    }

    @Test
    public void testIsChangingOverTime_NoFormulaSignatureDefinition() {
        method.setChangingOverTime(false);
        method.setFormulaSignatureDefinition(false);
        assertFalse(method.isChangingOverTime());
        assertFalse(method.isFormulaSignatureDefinition());
    }

    @Test
    public void testValidate_DatatypeMustBeAValueDatatypeForFormulaSignature() throws CoreException {
        method.setDatatype("void");
        method.setFormulaSignatureDefinition(false);
        MessageList result = method.validate(method.getIpsProject());
        assertNull(result
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
        method.setFormulaSignatureDefinition(true);
        result = method.validate(method.getIpsProject());
        assertNotNull(result
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));

        method.setFormulaSignatureDefinition(false);
        method.setDatatype(productCmptType.getQualifiedName());
        result = method.validate(method.getIpsProject());
        assertNull(result
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
        method.setFormulaSignatureDefinition(true);
        result = method.validate(method.getIpsProject());
        assertNotNull(result
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));

        method.setDatatype("Integer");
        result = method.validate(method.getIpsProject());
        assertNull(result
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_DATATYPE_MUST_BE_A_VALUEDATATYPE_FOR_FORMULA_SIGNATURES));
    }

    @Test
    public void testInitFromXml() {
        Element docElement = getTestDocument().getDocumentElement();
        method.setFormulaSignatureDefinition(false);
        method.initFromXml(XmlUtil.getElement(docElement, "Method", 0));
        assertTrue(method.isFormulaSignatureDefinition());
        assertEquals("Premium", method.getFormulaName());
        assertEquals("42", method.getId());
        assertEquals("calcPremium", method.getName());
        assertEquals("Money", method.getDatatype());
        assertEquals(Modifier.PUBLIC, method.getModifier());
        assertTrue(method.isAbstract());
        assertTrue(method.isOverloadsFormula());
        assertFalse(method.isFormulaMandatory());
        assertFalse(method.isChangingOverTime());
    }

    @Test
    public void testInitFromXml_formulaSignature() {
        Element docElement = getTestDocument().getDocumentElement();
        method.initFromXml(XmlUtil.getElement(docElement, "Method", 2));
        assertEquals("44", method.getId());
        assertFalse(method.isFormulaSignatureDefinition());
    }

    @Test
    public void testInitFromXml_defaultValues() {
        Element docElement = getTestDocument().getDocumentElement();
        method.initFromXml(XmlUtil.getElement(docElement, "Method", 1));
        assertTrue(method.isFormulaSignatureDefinition());
        assertEquals(StringUtils.EMPTY, method.getFormulaName());
        assertEquals(StringUtils.EMPTY, method.getDatatype());
        assertEquals(Modifier.PUBLISHED, method.getModifier());
        assertFalse(method.isAbstract());
        assertFalse(method.isOverloadsFormula());
        assertTrue(method.isFormulaMandatory());
        assertTrue(method.isChangingOverTime());
    }

    @Test
    public void testToXmlDocument() {
        method = productCmptType.newProductCmptTypeMethod(); // => id=1, because it's the second
        // method
        method.setName("getAge");
        method.setModifier(Modifier.PUBLIC);
        method.setDatatype("Decimal");
        method.setFormulaSignatureDefinition(true);
        method.setFormulaMandatory(false);
        method.setAbstract(true);
        method.setFormulaName("Premium");
        IParameter param0 = method.newParameter();
        param0.setName("p0");
        param0.setDatatype("Decimal");
        IParameter param1 = method.newParameter();
        param1.setName("p1");
        param1.setDatatype("Money");
        method.setOverloadsFormula(true);
        method.setCategory("foo");
        method.setChangingOverTime(false);

        Element element = method.toXml(newDocument());

        IProductCmptTypeMethod copy = productCmptType.newProductCmptTypeMethod();
        copy.initFromXml(element);
        assertTrue(copy.isFormulaSignatureDefinition());
        assertFalse(copy.isFormulaMandatory());
        assertEquals("Premium", copy.getFormulaName());
        IParameter[] copyParams = copy.getParameters();
        assertEquals(method.getId(), copy.getId());
        assertEquals("getAge", copy.getName());
        assertEquals("Decimal", copy.getDatatype());
        assertEquals(Modifier.PUBLIC, copy.getModifier());
        assertTrue(copy.isAbstract());
        assertEquals(2, copyParams.length);
        assertEquals("p0", copyParams[0].getName());
        assertEquals("Decimal", copyParams[0].getDatatype());
        assertEquals("p1", copyParams[1].getName());
        assertEquals("Money", copyParams[1].getDatatype());
        assertTrue(copy.isOverloadsFormula());
        assertEquals("foo", copy.getCategory());
        assertFalse(copy.isChangingOverTime());
    }

    @Test
    public void testFindOverloadedFormulaMethod() throws CoreException {
        IProductCmptType aType = newProductCmptType(ipsProject, "AType");
        IProductCmptTypeMethod aMethod = aType.newProductCmptTypeMethod();
        aMethod.setName("calculate");
        aMethod.setDatatype(Datatype.STRING.toString());
        aMethod.setFormulaName("formula");
        aMethod.setFormulaSignatureDefinition(true);
        aMethod.setModifier(Modifier.PUBLIC);
        aMethod.newParameter(Datatype.STRING.toString(), "param1");
        aMethod.newParameter(Datatype.INTEGER.toString(), "param2");

        IProductCmptType bType = newProductCmptType(ipsProject, "BType");
        bType.setSupertype(aType.getQualifiedName());
        IProductCmptTypeMethod bMethod = bType.newProductCmptTypeMethod();
        bMethod.setName("calculate");
        bMethod.setDatatype(Datatype.STRING.toString());
        bMethod.setFormulaName("formula");
        bMethod.setFormulaSignatureDefinition(true);
        bMethod.setModifier(Modifier.PUBLIC);
        bMethod.newParameter(Datatype.STRING.toString(), "param1");
        bMethod.newParameter(Datatype.INTEGER.toString(), "param2");

        IProductCmptTypeMethod overloadedFormulaMethod = bMethod.findOverloadedFormulaMethod(ipsProject);
        assertNull(overloadedFormulaMethod);

        bMethod.setOverloadsFormula(true);
        overloadedFormulaMethod = bMethod.findOverloadedFormulaMethod(ipsProject);
        assertEquals(aMethod, overloadedFormulaMethod);

        bType.setSupertype(null);
        overloadedFormulaMethod = bMethod.findOverloadedFormulaMethod(ipsProject);
        assertNull(overloadedFormulaMethod);

        bType.setSupertype(aType.getQualifiedName());
        bMethod.setFormulaSignatureDefinition(false);
        assertNull(overloadedFormulaMethod);
    }

    @Test
    public void testOverloadsFormula() throws CoreException {
        IProductCmptType bType = newProductCmptType(ipsProject, "BType");
        IProductCmptTypeMethod bMethod = bType.newProductCmptTypeMethod();
        bMethod.setName("calculate");
        bMethod.setDatatype(Datatype.STRING.toString());
        bMethod.setFormulaName("formula");
        bMethod.setFormulaSignatureDefinition(true);
        bMethod.setModifier(Modifier.PUBLIC);
        bMethod.newParameter(Datatype.STRING.toString(), "param1");
        bMethod.newParameter(Datatype.INTEGER.toString(), "param2");

        assertFalse(bMethod.isOverloadsFormula());

        bMethod.setOverloadsFormula(true);
        assertTrue(bMethod.isOverloadsFormula());
    }

    @Test
    public void testValidate_OverLoaded_Mandatory() throws CoreException {
        IProductCmptType aType = newProductCmptType(ipsProject, "AType");
        IProductCmptTypeMethod aMethod = aType.newProductCmptTypeMethod();
        aMethod.setName("calculate");
        aMethod.setDatatype(Datatype.STRING.toString());
        aMethod.setFormulaName("formula");
        aMethod.setFormulaSignatureDefinition(true);
        aMethod.setModifier(Modifier.PUBLIC);
        aMethod.newParameter(Datatype.STRING.toString(), "param1");
        aMethod.newParameter(Datatype.INTEGER.toString(), "param2");
        aMethod.setFormulaMandatory(false);

        IProductCmptType bType = newProductCmptType(ipsProject, "BType");
        bType.setSupertype(aType.getQualifiedName());
        IProductCmptTypeMethod bMethod = bType.newProductCmptTypeMethod();
        bMethod.setName("calculate");
        bMethod.setDatatype(Datatype.STRING.toString());
        bMethod.setFormulaName("formula");
        bMethod.setFormulaSignatureDefinition(true);
        bMethod.setModifier(Modifier.PUBLIC);
        bMethod.newParameter(Datatype.STRING.toString(), "param1");
        bMethod.newParameter(Datatype.INTEGER.toString(), "param2");
        bMethod.setOverloadsFormula(true);

        bMethod.setFormulaMandatory(false);
        MessageList msgList = bMethod.validate(ipsProject);
        assertTrue(msgList.isEmpty());

        bMethod.setFormulaMandatory(true);
        msgList = bMethod.validate(ipsProject);
        assertTrue(msgList.isEmpty());

        aMethod.setFormulaMandatory(true);
        msgList = bMethod.validate(ipsProject);
        assertTrue(msgList.isEmpty());

        bMethod.setFormulaMandatory(false);
        msgList = bMethod.validate(ipsProject);
        Message messageByCode = msgList.getMessageByCode(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTBE_MANDATORY);
        assertNotNull(messageByCode);
    }

    @Test
    public void testValidateOverLoadedFormulaSignatureNotInSupertypeHierarchy() throws CoreException {
        IProductCmptType aType = newProductCmptType(ipsProject, "AType");
        IProductCmptTypeMethod aMethod = aType.newProductCmptTypeMethod();
        aMethod.setName("calculate");
        aMethod.setDatatype(Datatype.STRING.toString());
        aMethod.setFormulaName("formula");
        aMethod.setFormulaSignatureDefinition(true);
        aMethod.setModifier(Modifier.PUBLIC);
        aMethod.newParameter(Datatype.STRING.toString(), "param1");
        aMethod.newParameter(Datatype.INTEGER.toString(), "param2");

        IProductCmptType bType = newProductCmptType(ipsProject, "BType");
        bType.setSupertype(aType.getQualifiedName());
        IProductCmptTypeMethod bMethod = bType.newProductCmptTypeMethod();
        bMethod.setName("calculate");
        bMethod.setDatatype(Datatype.STRING.toString());
        bMethod.setFormulaName("formula");
        bMethod.setFormulaSignatureDefinition(true);
        bMethod.setModifier(Modifier.PUBLIC);
        bMethod.newParameter(Datatype.STRING.toString(), "param1");
        bMethod.newParameter(Datatype.INTEGER.toString(), "param2");
        bMethod.setOverloadsFormula(true);

        MessageList msgList = bMethod.validate(ipsProject);
        Message msg = msgList
                .getMessageByCode(IProductCmptTypeMethod.MSGCODE_NO_FORMULA_WITH_SAME_NAME_IN_TYPE_HIERARCHY);
        assertNull(msg);

        aMethod.setFormulaName("formula2");
        msgList = bMethod.validate(ipsProject);
        msg = msgList.getMessageByCode(IProductCmptTypeMethod.MSGCODE_NO_FORMULA_WITH_SAME_NAME_IN_TYPE_HIERARCHY);
        assertNotNull(msg);
    }

    @Test
    public void testIsPolicyCmptTypeProperty() {
        assertFalse(method.isPolicyCmptTypeProperty());
    }

    @Test
    public void testIsPropertyFor() throws CoreException {
        IProductCmpt productCmpt = newProductCmpt(productCmptType, "Product");
        IProductCmptGeneration generation = (IProductCmptGeneration)productCmpt.newGeneration();
        IPropertyValue propertyValue = generation.newFormula(method);

        assertTrue(method.isPropertyFor(propertyValue));
    }

    @Test
    public void testValidateOverloadedFormulaSignature_overloadedNoName() throws CoreException {
        MessageList list = new MessageList();
        method.setOverloadsFormula(true);

        ((ProductCmptTypeMethod)method).validateOverloadedFormulaSignature(list, ipsProject);

        assertTrue(list.isEmpty());
    }

    @Test
    public void testValidateOverloadedFormulaSignature_overloadedNotFound() throws CoreException {
        MessageList list = new MessageList();
        method.setOverloadsFormula(true);
        method.setFormulaName("testName");

        ((ProductCmptTypeMethod)method).validateOverloadedFormulaSignature(list, ipsProject);

        assertEquals(1, list.size());
        assertEquals(IProductCmptTypeMethod.MSGCODE_NO_FORMULA_WITH_SAME_NAME_IN_TYPE_HIERARCHY, list.getMessage(0)
                .getCode());
    }

    @Test
    public void testValidateOverloadedFormulaSignature_overloadedMendatory() throws CoreException {
        MessageList list = new MessageList();
        method.setOverloadsFormula(true);
        method.setFormulaName("testName");
        method.setFormulaMandatory(false);
        ProductCmptType superType = newProductCmptType(ipsProject, "SuperType");
        productCmptType.setSupertype("SuperType");
        IProductCmptTypeMethod superMethod = superType.newFormulaSignature("testName");
        superMethod.setFormulaMandatory(true);

        ((ProductCmptTypeMethod)method).validateOverloadedFormulaSignature(list, ipsProject);

        assertEquals(1, list.size());
        assertEquals(IProductCmptTypeMethod.MSGCODE_FORMULA_MUSTBE_MANDATORY, list.getMessage(0).getCode());
    }

}