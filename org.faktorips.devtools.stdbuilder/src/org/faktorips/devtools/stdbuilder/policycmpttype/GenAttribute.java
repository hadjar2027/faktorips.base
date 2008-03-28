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

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.DefaultJavaGeneratorForIpsPart;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.AttributeType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.runtime.internal.MethodNames;
import org.faktorips.util.LocalizedStringsSet;

/**
 * Abstract code generator for an attribute.
 * 
 * @author Jan Ortmann
 */
public abstract class GenAttribute extends DefaultJavaGeneratorForIpsPart {

    protected static final String[] EMPTY_STRING_ARRAY = new String[0];
    
    protected IPolicyCmptTypeAttribute attribute;
    protected String attributeName;
    protected DatatypeHelper datatypeHelper;
    protected String staticConstantPropertyName;
    protected String memberVarName;
    private IProductCmptType productCmptType;
    
    public GenAttribute(IPolicyCmptTypeAttribute a, DefaultJavaSourceFileBuilder builder, LocalizedStringsSet stringsSet, boolean generateImplementation) throws CoreException {
        super(a, builder, stringsSet, generateImplementation);
        this.attribute = a;
        attributeName = a.getName();
        datatypeHelper = builder.getIpsProject().findDatatypeHelper(a.getDatatype());
        if (datatypeHelper==null) {
            throw new NullPointerException("No datatype helper found for " + a);
        }
        staticConstantPropertyName = getLocalizedText("FIELD_PROPERTY_NAME", StringUtils.upperCase(a.getName()));
        memberVarName = getJavaNamingConvention().getMemberVarName(attributeName);
    }
    
    /**
     * Returns the policy component implementation class builder.
     */
    private PolicyCmptImplClassBuilder getImplClassBuilder() {
        if (getJavaSourceFileBuilder() instanceof PolicyCmptImplClassBuilder) {
            return (PolicyCmptImplClassBuilder)getJavaSourceFileBuilder();
        }
        return null;
    }
    
    /**
     * Returns the policy component interface builder.
     */
    private PolicyCmptInterfaceBuilder getInterfaceBuilder() {
        if (getJavaSourceFileBuilder() instanceof PolicyCmptInterfaceBuilder) {
            return (PolicyCmptInterfaceBuilder)getJavaSourceFileBuilder();
        }
        return getImplClassBuilder().getInterfaceBuilder();
    }
    
    // TODO refactor
    protected boolean isGenerateChangeListenerSupport() {
        return true;
    }

    public IPolicyCmptTypeAttribute getPolicyCmptTypeAttribute() {
        return attribute;
    }
    
    protected IProductCmptType getProductCmptType() throws CoreException {
        if (productCmptType==null) {
            productCmptType = attribute.getPolicyCmptType().findProductCmptType(getIpsProject());
        }
        return productCmptType;
    }
    
    public DatatypeHelper getDatatypeHelper() {
        return datatypeHelper;
    }
    
    public ValueDatatype getDatatype() {
        return (ValueDatatype)datatypeHelper.getDatatype();
    }
    
    public String getJavaClassName() {
        return datatypeHelper.getJavaClassName();
    }
    
    public boolean isPublished() {
        return attribute.getModifier().isPublished();        
    }
    
    public boolean isNotPublished() {
        return !isPublished();
    }
    
    public boolean isOverwritten() {
        return attribute.isOverwrite();
    }
    
    public boolean isConfigurableByProduct() {
        return attribute.isProductRelevant();
    }
    
    public boolean isDerivedOnTheFly() {
        return attribute.getAttributeType()==AttributeType.DERIVED_ON_THE_FLY;
    }
    
    public boolean isDerivedByExplicitMethodCall() {
        return attribute.getAttributeType()==AttributeType.DERIVED_BY_EXPLICIT_METHOD_CALL;
    }

    public String getGetterMethodName() {
        return getJavaNamingConvention().getGetterMethodName(attributeName, getDatatype());
    }

    public String getSetterMethodName() {
        return getJavaNamingConvention().getSetterMethodName(attributeName, getDatatype());
    }
    
    public String getStaticConstantPropertyName() {
        return this.staticConstantPropertyName;
    }

    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public final static String PROPERTY_PREMIUM = "premium";
     * </pre>
     */
    protected void generateAttributeNameConstant(JavaCodeFragmentBuilder builder) throws CoreException {
        appendLocalizedJavaDoc("FIELD_PROPERTY_NAME", attributeName, builder);
        builder.append("public final static ");
        builder.appendClassName(String.class);
        builder.append(' ');
        builder.append(staticConstantPropertyName);
        builder.append(" = ");
        builder.appendQuoted(attributeName);
        builder.appendln(";");
    }
    
    /**
     * Code sample:
     * <pre>
     * [Javadoc]
     * public Money getPremium();
     * </pre>
     */
    protected void generateGetterInterface(JavaCodeFragmentBuilder builder) throws CoreException {
        String description = StringUtils.isEmpty(attribute.getDescription()) ? "" : SystemUtils.LINE_SEPARATOR + "<p>" + SystemUtils.LINE_SEPARATOR + attribute.getDescription();
        String[] replacements = new String[]{attributeName, description};
        appendLocalizedJavaDoc("METHOD_GETVALUE", replacements, builder);
        generateGetterSignature(builder);
        builder.appendln(";");
    }
        
    /**
     * Code sample:
     * <pre>
     * public Money getPremium() {
     *     return premium;
     * }
     * </pre>
     */
    protected void generateGetterImplementation(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        methodsBuilder.javaDoc(getJavaDocCommentForOverriddenMethod(), JavaSourceFileBuilder.ANNOTATION_GENERATED);
        generateGetterSignature(methodsBuilder);
        methodsBuilder.openBracket();
        methodsBuilder.append("return ");
        methodsBuilder.append(memberVarName);
        methodsBuilder.append(";");
        methodsBuilder.closeBracket();
    }
    
    /**
     * Returns <code>true</code> if a member variable is required to the type of attribute.
     * This is currently the case for changeable attributes and attributes that are derived by an explicit
     * method call.
     */
    protected boolean isMemberVariableRequired() {
        return (attribute.isChangeable() || isDerivedByExplicitMethodCall()) && !isOverwritten();
    }
    
    protected boolean needsToBeConsideredInDeltaComputation() {
        return isPublished() && isMemberVariableRequired() && !isOverwritten();
    }
    
    protected void generateDeltaComputation(JavaCodeFragmentBuilder methodsBuilder, String deltaVar, String otherVar) throws CoreException {
        methodsBuilder.append(deltaVar);
        methodsBuilder.append('.');
        methodsBuilder.append(MethodNames.MODELOBJECTDELTA_CHECK_PROPERTY_CHANGE);
        methodsBuilder.append("(");
        methodsBuilder.appendClassName(getInterfaceBuilder().getQualifiedClassName(attribute.getIpsSrcFile()));
        methodsBuilder.append(".");
        methodsBuilder.append(staticConstantPropertyName);
        methodsBuilder.append(", ");
        methodsBuilder.append(memberVarName);
        methodsBuilder.append(", ");
        methodsBuilder.append(otherVar);
        methodsBuilder.append(".");
        methodsBuilder.append(memberVarName);
        methodsBuilder.appendln(", options);");
    }
    
    protected boolean needsToBeConsideredInInitPropertiesFromXml() throws CoreException {
        return isMemberVariableRequired();        
    }
    
    protected void generateInitPropertiesFromXml(JavaCodeFragmentBuilder builder) throws CoreException {
        builder.append("if (propMap.containsKey(");
        builder.appendQuoted(attributeName);
        builder.appendln(")) {");
        String expr = "(String)propMap.get(\"" + attributeName + "\")";
        builder.append(getMemberVarName() + " = ");
        builder.append(datatypeHelper.newInstanceFromExpression(expr));
        builder.appendln(";");
        builder.appendln("}");
    }
    
    /**
     * Code sample:
     * <pre>
     * public Money getPremium()
     * </pre>
     */
    protected void generateGetterSignature(JavaCodeFragmentBuilder methodsBuilder) throws CoreException {
        int modifier = java.lang.reflect.Modifier.PUBLIC;
        String methodName = getMethodNameGetPropertyValue(attributeName, datatypeHelper.getDatatype());
        methodsBuilder.signature(modifier, getJavaClassName(), methodName, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
    }
    
    protected void generateField(JavaCodeFragmentBuilder memberVarsBuilders) throws CoreException {
        JavaCodeFragment initialValueExpression = datatypeHelper.newInstance(attribute.getDefaultValue());
        String comment = getLocalizedText("FIELD_ATTRIBUTE_VALUE_JAVADOC", attributeName);
        String fieldName = getMemberVarName();

        memberVarsBuilders.javaDoc(comment, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        memberVarsBuilders.varDeclaration(java.lang.reflect.Modifier.PRIVATE, getJavaClassName(), fieldName,
            initialValueExpression);
    }
    
    /**
     * Returns the name of the field/member variable that stores the values
     * for the property/attribute.
     */
    public String getMemberVarName() throws CoreException {
        return memberVarName;
    }
    
}
