/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.internal.model.productcmpt;

import java.util.ArrayList;
import java.util.Arrays;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.AbstractDatatypeHelper;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.AbstractIpsPluginTest;
import org.faktorips.devtools.core.TestEnumType;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.internal.model.valueset.EnumValueSet;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpt.ConfigElementType;
import org.faktorips.devtools.core.model.productcmpt.IConfigElement;
import org.faktorips.devtools.core.model.productcmpt.IProductCmpt;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.valueset.IEnumValueSet;
import org.faktorips.devtools.core.model.valueset.IRangeValueSet;
import org.faktorips.devtools.core.model.valueset.ValueSetType;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 *
 */
public class ConfigElementTest extends AbstractIpsPluginTest {

    private IIpsProject ipsProject;
	private IPolicyCmptType policyCmptType;
    private IProductCmptType productCmptType;
    private IProductCmpt productCmpt;
    private IProductCmptGeneration generation;
    private IConfigElement configElement;

    protected void setUp() throws Exception {
        super.setUp();
        ipsProject = newIpsProject();
        policyCmptType = newPolicyAndProductCmptType(ipsProject, "TestPolicy", "TestProduct");
        productCmptType = policyCmptType.findProductCmptType(ipsProject);
        productCmpt = newProductCmpt(productCmptType, "TestProduct");
        generation = productCmpt.getProductCmptGeneration(0);
        configElement = generation.newConfigElement();
        productCmpt.getIpsSrcFile().save(true, null);
        newDefinedEnumDatatype((IpsProject)ipsProject, new Class[]{TestEnumType.class});
    }
    
    public void testFindPcTypeAttribute() throws CoreException {
        IPolicyCmptType policyCmptSupertype = newPolicyCmptType(ipsProject, "SuperPolicy");
        policyCmptType.setSupertype(policyCmptSupertype.getQualifiedName());
        
        IPolicyCmptTypeAttribute a1 = policyCmptType.newPolicyCmptTypeAttribute();
        a1.setName("a1");
        IPolicyCmptTypeAttribute a2 = policyCmptSupertype.newPolicyCmptTypeAttribute();
        a2.setName("a2");
        
        generation = productCmpt.getProductCmptGeneration(0);
        IConfigElement ce = generation.newConfigElement();
        ce.setPolicyCmptTypeAttribute("a1");
        assertEquals(a1, ce.findPcTypeAttribute(ipsProject));
        ce.setPolicyCmptTypeAttribute("a2");
        assertEquals(a2, ce.findPcTypeAttribute(ipsProject));
        ce.setPolicyCmptTypeAttribute("unkown");
        assertNull(ce.findPcTypeAttribute(ipsProject));
    }
    
    public void testValidate_UnknownAttribute() throws CoreException {
    	configElement.setPolicyCmptTypeAttribute("a");
    	MessageList ml = configElement.validate(ipsProject);
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
    	
    	policyCmptType.newPolicyCmptTypeAttribute().setName("a");
        policyCmptType.getIpsSrcFile().save(true, null);
    	
    	ml = configElement.validate(ipsProject);
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNWON_ATTRIBUTE));
    }
    
    public void testValidate_UnknownDatatypeValue() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.POLICY_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPolicyCmptTypeAttribute("valueTest");
    	IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
    	attr.setName("valueTest");
    	attr.setAttributeType(AttributeType.CHANGEABLE);
    	
    	MessageList ml = ce.validate(ipsProject);
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_VALUE));
    	
    	attr.setDatatype("Decimal");
    	
    	policyCmptType.getIpsSrcFile().save(true, null);

    	ml = ce.validate(ipsProject);
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_UNKNOWN_DATATYPE_VALUE));
    }

    public void testValidate_ValueNotParsable() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.POLICY_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPolicyCmptTypeAttribute("valueTest");
    	IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
    	attr.setName("valueTest");
    	attr.setAttributeType(AttributeType.CHANGEABLE);
    	attr.setDatatype("Money");

    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate(ipsProject);
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE));
    	
    	attr.setDatatype("Decimal");
    	policyCmptType.getIpsSrcFile().save(true, null);

    	ml = ce.validate(ipsProject);
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_PARSABLE));
    }
    
    public void testValidate_InvalidValueset() throws CoreException {
    	IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
    	attr.setName("valueTest");
    	attr.setAttributeType(AttributeType.CHANGEABLE);
    	attr.setDatatype("Decimal");
    	attr.setValueSetType(ValueSetType.RANGE);
    	IRangeValueSet valueSet = (IRangeValueSet)attr.getValueSet();
    	valueSet.setLowerBound("a");
    	valueSet.setUpperBound("b");

    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.POLICY_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPolicyCmptTypeAttribute("valueTest");
    	ce.setValueSetCopy(valueSet);
    	

    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate(ce.getIpsProject());
    	
    	// no test for specific message codes because the codes are under controll
    	// of the value set.
    	assertTrue(ml.getNoOfMessages() > 0); 
    	
    	valueSet = (IRangeValueSet)ce.getValueSet();
    	valueSet.setLowerBound("0");
    	valueSet.setUpperBound("100");
    	
    	valueSet = (IRangeValueSet)attr.getValueSet();
    	valueSet.setLowerBound("0");
    	valueSet.setUpperBound("100");
    	
    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);

    	ml = ce.validate(ipsProject);
    	assertEquals(0, ml.getNoOfMessages());
    }
    
    public void testValidate_InvalidDatatype() throws Exception {
    	IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
    	attr.setName("test");
    	InvalidDatatype datatype = new InvalidDatatype();
    	attr.setDatatype(datatype.getQualifiedName());

		ValueDatatype[] vds = ipsProject.getValueDatatypes(false);
		ArrayList vdlist = new ArrayList();
		vdlist.addAll(Arrays.asList(vds));
		vdlist.add(datatype);

        IIpsProjectProperties properties = ipsProject.getProperties();
        properties.setPredefinedDatatypesUsed((ValueDatatype[])vdlist.toArray(new ValueDatatype[vdlist.size()]));
        ipsProject.setProperties(properties);
		
		InvalidDatatypeHelper idh = new InvalidDatatypeHelper();
    	idh.setDatatype(datatype);
    	((IpsModel)ipsProject.getIpsModel()).addDatatypeHelper(idh);

    	IConfigElement ce = generation.newConfigElement();
    	ce.setPolicyCmptTypeAttribute("test");
    	MessageList ml = ce.validate(ce.getIpsProject());
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_INVALID_DATATYPE));
    }
    
    public void testValidate_ValueNotInValueset() throws CoreException {
    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.POLICY_ATTRIBUTE);
    	ce.setValue("1");
    	ce.setPolicyCmptTypeAttribute("valueTest");
    	ce.setValueSetType(ValueSetType.RANGE);
    	IRangeValueSet valueSet = (IRangeValueSet)ce.getValueSet();
    	valueSet.setLowerBound("10");
    	valueSet.setUpperBound("20");

    	IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
    	attr.setName("valueTest");
    	
    	attr.setAttributeType(AttributeType.CONSTANT);
        attr.setValueSetType(ValueSetType.RANGE);
        attr.setDatatype("Decimal");
        IRangeValueSet valueSetAttr = (IRangeValueSet)attr.getValueSet();
        valueSetAttr.setLowerBound(null);
        valueSetAttr.setUpperBound(null);
        
    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate(ipsProject);
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET)); 
    	
    	ce.setValue("15");

    	ml = ce.validate(ipsProject);
    	assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUE_NOT_IN_VALUESET)); 
    }
    
    public void testValidate_ValueSetNotASubset() throws CoreException {
    	IPolicyCmptTypeAttribute attr = policyCmptType.newPolicyCmptTypeAttribute();
    	attr.setName("valueTest");
    	attr.setValueSetType(ValueSetType.RANGE);
    	IRangeValueSet valueSet = (IRangeValueSet)attr.getValueSet();
    	valueSet.setLowerBound("10");
    	valueSet.setUpperBound("15");
    	attr.setAttributeType(AttributeType.CONSTANT);
    	attr.setDatatype("Decimal");

    	IConfigElement ce = generation.newConfigElement();
    	ce.setType(ConfigElementType.POLICY_ATTRIBUTE);
    	ce.setValue("12");
    	ce.setPolicyCmptTypeAttribute("valueTest");
    	ce.setValueSetCopy(valueSet);
    	IRangeValueSet valueSet2 = (IRangeValueSet)ce.getValueSet();
    	valueSet2.setUpperBound("20");

    	policyCmptType.getIpsSrcFile().save(true, null);
    	productCmpt.getIpsSrcFile().save(true, null);
    	
    	MessageList ml = ce.validate(ipsProject);
    	// no test for specific message codes because the codes are under controll
    	// of the value set.
    	assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 
    	
    	valueSet.setUpperBound("20");
    	policyCmptType.getIpsSrcFile().save(true, null);

    	ml = ce.validate(ipsProject);
    	assertEquals(0, ml.getNoOfMessages());
        
        // check lower unbound values
        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 
        
        valueSet.setLowerBound("10");
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = ce.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 

        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound("10");
        valueSet2.setUpperBound(null);
        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 
        
        // check upper unbound values
        valueSet.setLowerBound(null);
        valueSet.setUpperBound("10");
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound(null);
        ml = ce.validate(ipsProject);
        assertNotNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 

        valueSet.setLowerBound(null);
        valueSet.setUpperBound(null);
        valueSet2.setLowerBound(null);
        valueSet2.setUpperBound("10");
        ml = ce.validate(ipsProject);
        assertNull(ml.getMessageByCode(IConfigElement.MSGCODE_VALUESET_IS_NOT_A_SUBSET)); 
    }

    public void testSetValue() {
        configElement.setValue("newValue");
        assertEquals("newValue", configElement.getValue());
        assertTrue(configElement.getIpsSrcFile().isDirty());
    }

    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        configElement.initFromXml((Element)doc.getDocumentElement());
        assertEquals(42, configElement.getId());
        assertEquals("sumInsured", configElement.getPolicyCmptTypeAttribute());
        assertEquals("10", configElement.getValue());
        IRangeValueSet range = (IRangeValueSet)configElement.getValueSet();
        assertEquals("22", range.getLowerBound());
        assertEquals("33", range.getUpperBound());
        assertEquals("4", range.getStep());
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        IConfigElement cfgElement = generation.newConfigElement();
        cfgElement.setType(ConfigElementType.POLICY_ATTRIBUTE);
        cfgElement.setValue("value");
        cfgElement.setValueSetType(ValueSetType.RANGE);
        IRangeValueSet valueSet = (IRangeValueSet)cfgElement.getValueSet(); 
        valueSet.setLowerBound("22");
        valueSet.setUpperBound("33");
        valueSet.setStep("4");
        Element xmlElement = cfgElement.toXml(getTestDocument());

        IConfigElement newCfgElement = generation.newConfigElement();
        newCfgElement.initFromXml(xmlElement);
        assertEquals("value", newCfgElement.getValue());
        assertEquals("22", ((IRangeValueSet)newCfgElement.getValueSet()).getLowerBound());
        assertEquals("33", ((IRangeValueSet)newCfgElement.getValueSet()).getUpperBound());
        assertEquals("4", ((IRangeValueSet)newCfgElement.getValueSet()).getStep());
        
        cfgElement.setValueSetType(ValueSetType.ENUM);
        EnumValueSet enumValueSet = (EnumValueSet)cfgElement.getValueSet();
        enumValueSet.addValue("one");
        enumValueSet.addValue("two");
        enumValueSet.addValue("three");
        enumValueSet.addValue("four");

        xmlElement = cfgElement.toXml(getTestDocument());
        assertEquals(4, ((IEnumValueSet)cfgElement.getValueSet()).getValues().length);
        assertEquals("one", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[0]);
        assertEquals("two", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[1]);
        assertEquals("three", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[2]);
        assertEquals("four", ((IEnumValueSet)cfgElement.getValueSet()).getValues()[3]);
        
        cfgElement.setValue(null);
        xmlElement = cfgElement.toXml(getTestDocument());
        newCfgElement.initFromXml(xmlElement);
        
        assertNull(newCfgElement.getValue());
    }

    /**
     * Tests for the correct type of exception to be thrown - no part of any type could ever be created.
     */
    public void testNewPart() {
    	try {
			configElement.newPart(IPolicyCmptTypeAttribute.class);
			fail();
		} catch (IllegalArgumentException e) {
			//nothing to do :-)
		}
    }
    
    private class InvalidDatatype implements ValueDatatype {

		public ValueDatatype getWrapperType() {
			return null;
		}

		public boolean isParsable(String value) {
			return true;
		}

		public String getName() {
			return getQualifiedName();
		}

		public String getQualifiedName() {
			return "InvalidDatatype";
		}

		public String getDefaultValue() {
            return null;
        }

        public boolean isVoid() {
			return false;
		}

		public boolean isPrimitive() {
			return false;
		}

		public boolean isValueDatatype() {
			return true;
		}

		public String getJavaClassName() {
			return null;
		}

		public MessageList checkReadyToUse() {
			MessageList ml = new MessageList();
			
			ml.add(new Message("", "", Message.ERROR));
			
			return ml;
		}

		public int compareTo(Object o) {
			return -1;
		}

        /**
         * {@inheritDoc}
         */
        public boolean hasNullObject() {
            return false;
        }

        public boolean isNull(String value) {
            return false;
        }

        public boolean supportsCompare() {
            return false;
        }

        public int compare(String valueA, String valueB) throws UnsupportedOperationException {
            return 0;
        }

        public boolean areValuesEqual(String valueA, String valueB) {
            return false;
        }

		public boolean isMutable() {
			return true;
		}
    	
		public boolean isImmutable() {
			return false;
		}
    }
    
    private class InvalidDatatypeHelper extends AbstractDatatypeHelper {

		protected JavaCodeFragment valueOfExpression(String expression) {
			return null;
		}

		public JavaCodeFragment nullExpression() {
			return null;
		}

		public JavaCodeFragment newInstance(String value) {
			return null;
		}
    	
    }
}
