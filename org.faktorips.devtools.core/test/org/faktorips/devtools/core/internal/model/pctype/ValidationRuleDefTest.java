package org.faktorips.devtools.core.internal.model.pctype;

import org.faktorips.devtools.core.internal.model.IpsObjectTestCase;
import org.faktorips.devtools.core.model.IpsObjectType;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRuleDef;
import org.faktorips.devtools.core.model.pctype.MessageSeverity;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;


/**
 *
 */
public class ValidationRuleDefTest extends IpsObjectTestCase {
    
    private PolicyCmptType pcType;
    private IValidationRuleDef rule;
    
    protected void setUp() throws Exception {
        super.setUp(IpsObjectType.POLICY_CMPT_TYPE);
    }
    
    protected void createObjectAndPart() {
        pcType = new PolicyCmptType(pdSrcFile);
        rule = pcType.newRule();
    }
    
    public void testRemove() {
        rule.delete();
        assertEquals(0, pcType.getAttributes().length);
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testSetName() {
        rule.setName("newName");
        assertEquals("newName", rule.getName());
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testSetMessageText() {
        rule.setMessageText("newText");
        assertEquals("newText", rule.getMessageText());
        assertTrue(pdSrcFile.isDirty());
    }
    
    public void testAddValidatedAttribute(){
    	rule.addValidatedAttribute("a");
    	rule.addValidatedAttribute("b");
    	assertEquals("a", rule.getValidatedAttributes()[0]);
    	assertEquals("b", rule.getValidatedAttributes()[1]);
    	assertTrue(pdSrcFile.isDirty());
    }
    
    public void testSetValidatedAttributeAt(){
    	rule.addValidatedAttribute("a");
    	rule.addValidatedAttribute("b");
    	rule.setValidatedAttributeAt(1, "c");
    	assertEquals("c", rule.getValidatedAttributes()[1]);
    	assertTrue(pdSrcFile.isDirty());
    }
    
    public void testGetValidatedAttributeAt(){
    	rule.addValidatedAttribute("a");
    	rule.addValidatedAttribute("b");
    	assertEquals("a", rule.getValidatedAttributeAt(0));
    	assertEquals("b", rule.getValidatedAttributeAt(1));

    }
    
    public void testRemoveValidatedAttribute(){
    	rule.addValidatedAttribute("a");
    	rule.addValidatedAttribute("b");
    	rule.removeValidatedAttribute(0);
    	assertEquals("b", rule.getValidatedAttributeAt(0));
    }
    
    public void testInitFromXml() {
        Document doc = this.getTestDocument();
        rule.setAppliedInAllBusinessFunctions(true);
        rule.initFromXml((Element)doc.getDocumentElement());
        assertEquals(42, rule.getId());
        assertEquals("checkAge", rule.getName());
        assertEquals("blabla", rule.getDescription());
        assertEquals("ageMissing", rule.getMessageCode());
        assertEquals("messageText", rule.getMessageText());
        assertEquals(MessageSeverity.WARNING, rule.getMessageSeverity());
        assertFalse(rule.isAppliedInAllBusinessFunctions());
        String[] functions = rule.getBusinessFunctions();
        assertEquals(2, functions.length);
        assertEquals("NewOffer", functions[0]);
        assertEquals("Renewal", functions[1]);
        String[] validatedAttributes = rule.getValidatedAttributes();
        assertEquals("a", validatedAttributes[0]);
        assertEquals("b", validatedAttributes[1]);
    }

    /*
     * Class under test for Element toXml(Document)
     */
    public void testToXmlDocument() {
        rule = pcType.newRule(); // => id=1 because it's the second rule
        rule.setName("checkAge");
        rule.setAppliedInAllBusinessFunctions(true);
        rule.setDescription("blabla");
        rule.setMessageCode("ageMissing");
        rule.setMessageText("messageText");
        rule.setMessageSeverity(MessageSeverity.WARNING);
        rule.setBusinessFunctions(new String[]{"NewOffer", "Renewal"});
        rule.addValidatedAttribute("a");
        
        Element element = rule.toXml(this.newDocument());
        
        ValidationRuleDef copy = new ValidationRuleDef();
        copy.initFromXml(element);
        assertEquals(1, copy.getId());
        assertEquals("checkAge", copy.getName());
        assertEquals("blabla", copy.getDescription());
        assertEquals("ageMissing", copy.getMessageCode());
        assertEquals("messageText", copy.getMessageText());
        assertEquals(MessageSeverity.WARNING, copy.getMessageSeverity());
        assertTrue(copy.isAppliedInAllBusinessFunctions());
        String[] functions = copy.getBusinessFunctions();
        assertEquals(2, functions.length);
        assertEquals("NewOffer", functions[0]);
        assertEquals("Renewal", functions[1]);
        String[] validationAttributes = copy.getValidatedAttributes();
        assertEquals("a", validationAttributes[0]);
    }
    
    public void testAddBusinessFunction() {
        rule.addBusinessFunction("f1");
        assertEquals(1, rule.getNumOfBusinessFunctions());
        assertEquals("f1", rule.getBusinessFunction(0));
        
        rule.addBusinessFunction("f2");
        assertEquals(2, rule.getNumOfBusinessFunctions());
        assertEquals("f2", rule.getBusinessFunction(1));
    }
    
    public void testSetBusinessFunction() {
        rule.addBusinessFunction("f1");
        rule.addBusinessFunction("f2");
        
        rule.setBusinessFunctions(1, "changed");
        assertEquals("changed", rule.getBusinessFunction(1));
    }

    public void testRemoveBusinessFunction() {
        rule.addBusinessFunction("f1");
        rule.addBusinessFunction("f2");
        rule.addBusinessFunction("f3");
        rule.addBusinessFunction("f4");
        
        rule.removeBusinessFunction(3);
        rule.removeBusinessFunction(1);
        assertEquals(2, rule.getNumOfBusinessFunctions());
        assertEquals("f1", rule.getBusinessFunction(0));
        assertEquals("f3", rule.getBusinessFunction(1));
    }
    
    public void testValidate() throws Exception{
    	rule.addValidatedAttribute("a");
    	
    	//validation is expected to fail because the specified attribute doesn't exist for the PolicyCmptType
    	MessageList messageList = rule.validate().getMessagesFor(rule, "validatedAttributes");
    	assertEquals(1, messageList.getNoOfMessages());
    	
    	IAttribute attr = pcType.newAttribute();
    	attr.setName("a");
    	attr.setAttributeType(AttributeType.CHANGEABLE);
    	attr.setDatatype("String");
    	
    	messageList = rule.validate().getMessagesFor(rule, "validatedAttributes");
    	assertEquals(0, messageList.getNoOfMessages());
    	
    	//validation is expected to fail because of duplicate attribute entries
    	rule.addValidatedAttribute("a");
    	messageList = rule.validate().getMessagesFor(rule, "validatedAttributes");
    	assertEquals(1, messageList.getNoOfMessages());

    }
}
