package org.faktorips.devtools.stdbuilder.pctype;

import java.lang.reflect.Modifier;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.builder.JavaSourceFileBuilder;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IRelation;

/**
 * helper class for ProductCmptInterfaceCuBuilder, responsible for code related to relation handling
 */
public class ProductCmptInterfaceRelationBuilder {

    private final static String RELATION_INTERFACE_GETALL_JAVADOC = "RELATION_INTERFACE_GETALL_JAVADOC";
    private final static String RELATION_INTERFACE_GETTER_JAVADOC = "RELATION_INTERFACE_GETTER_JAVADOC";
    private final static String RELATION_INTERFACE_NUMOF_JAVADOC = "RELATION_INTERFACE_NUMOF_JAVADOC";

    private ProductCmptInterfaceCuBuilder cmptInterfaceCuBuilder;
    

    public ProductCmptInterfaceRelationBuilder(ProductCmptInterfaceCuBuilder cuBuilder) {
        cmptInterfaceCuBuilder = cuBuilder;
    }

    private ProductCmptInterfaceCuBuilder getProductCmptInterfaceBuilder() {
        return cmptInterfaceCuBuilder;
    }

    private void build1To1Relation(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        createRelationGetterMethod1To1(methodsBuilder, relation, target);
        createRelationGetNumOfMethod(methodsBuilder, relation);
    }

    private void build1ToManyRelation(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        createRelationGetAllMethod(methodsBuilder, relation, target);
        createRelationGetterMethod1ToMany(methodsBuilder, relation, target);
        createRelationGetNumOfMethod(methodsBuilder, relation);
    }

    protected void buildRelation(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        build1ToManyRelation(methodsBuilder, relation);
        // folgende Zeilen auskommentiert bis genauer Umgang mit relationen gekl�rt ist. Jan
        // if (relation.is1ToMany()) {
        // build1ToManyRelation(relation);
        // } else {
        // build1To1Relation(relation);
        // }
    }

    /**
     * @param relation
     * @param target
     */
    private void createRelationGetAllMethod(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodName = getProductCmptGetAllMethodName(relation);
        String returnType = getProductCmptInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile())
                + "[]";
        String javaDoc = getProductCmptInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_GETALL_JAVADOC,
            relation.getTargetRoleSingular());
        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, returnType, methodName, new String[0],
            new String[0], javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.append(';');
    }

    private void createRelationGetNumOfMethod(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        String methodName = getProductCmptNumOfMethodName(relation);
        String javaDoc = getProductCmptInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_NUMOF_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, Datatype.PRIMITIVE_INT.getJavaClassName(),
            methodName, new String[0], new String[0], javaDoc,
            JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.append(';');
    }

    private void createRelationGetterMethod1To1(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodName = getProductCmptInterfaceGetMethodName(relation);
        String targetQualifiedName = getProductCmptInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile());
        String javaDoc = getProductCmptInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_GETTER_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.javaDoc(javaDoc);
        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, targetQualifiedName, methodName, new String[0],
            new String[0], javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.append(';');
    }

    private void createRelationGetterMethod1ToMany(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodName = getProductCmptInterfaceGetMethodName(relation);
        String targetQualifiedName = getProductCmptInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile());

        String javaDoc = getProductCmptInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_GETTER_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.javaDoc(javaDoc);
        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, targetQualifiedName, methodName,
            new String[] { "productCmptName" }, new String[] { String.class.getName() }, javaDoc,
            JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.append(';');
    }

    private String getProductCmptGetAllMethodName(IRelation relation) {
        return "get" + StringUtils.capitalise(relation.getTargetRolePlural()) + "Pk";
    }

    // duplicated in ProductCmptImplRelationBuilder
    private String getProductCmptNumOfMethodName(IRelation relation) {
        return "getAnzahl" + StringUtils.capitalise(relation.getTargetRolePlural()) + "Pk";
    }

    private String getProductCmptInterfaceGetMethodName(IRelation relation) {
        return "get" + StringUtils.capitalise(relation.getTargetRoleSingular()) + "Pk";
    }
}
