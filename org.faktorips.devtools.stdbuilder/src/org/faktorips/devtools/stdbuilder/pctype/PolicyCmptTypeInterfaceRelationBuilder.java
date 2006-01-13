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
 * helper class for PolicyCmptTypeInterfaceCuBuilder, responsible for code related to relation
 * handling
 */
public class PolicyCmptTypeInterfaceRelationBuilder {

    private final static String RELATION_INTERFACE_ADD_JAVADOC = "RELATION_INTERFACE_ADD_JAVADOC";
    private final static String RELATION_INTERFACE_CONTAINS_JAVADOC = "RELATION_CONTAINS_JAVADOC";
    private final static String RELATION_INTERFACE_GETALL_JAVADOC = "RELATION_INTERFACE_GETALL_JAVADOC";
    private final static String RELATION_INTERFACE_GETTER_JAVADOC = "RELATION_INTERFACE_GETTER_JAVADOC";
    private final static String RELATION_INTERFACE_NUMOF_JAVADOC = "RELATION_INTERFACE_NUMOF_JAVADOC";
    private final static String RELATION_INTERFACE_REMOVE_JAVADOC = "RELATION_INTERFACE_REMOVE_JAVADOC";
    private final static String RELATION_INTERFACE_SETTER_JAVADOC = "RELATION_INTERFACE_SETTER_JAVADOC";

    private PolicyCmptTypeInterfaceCuBuilder cuBuilder;
    
    public PolicyCmptTypeInterfaceRelationBuilder(PolicyCmptTypeInterfaceCuBuilder cuBuilder) {
        this.cuBuilder = cuBuilder;
    }

    private PolicyCmptTypeInterfaceCuBuilder getPolicyCmptTypeInterfaceBuilder() {
        return cuBuilder;
    }

    private void build1To1Relation(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        createRelationGetterMethodDeclaration(methodsBuilder, relation, target);
        createRelationGetNumOfMethodDeclaration(methodsBuilder, relation);
        if (!relation.isReadOnlyContainer()) {
            createRelationSetterMethodDeclaration(methodsBuilder, relation, target);
        }
    }

    private void build1ToManyRelation(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        IPolicyCmptType target = relation.getIpsProject().findPolicyCmptType(relation.getTarget());
        createRelationGetNumOfMethodDeclaration(methodsBuilder, relation);
        createRelationGetAllMethodDeclaration(methodsBuilder, relation, target);
        createRelationContainsMethodDeclaration(methodsBuilder, relation, target);
        if (!relation.isReadOnlyContainer()) {
            createRelationAddMethodDeclaration(methodsBuilder, relation, target);
            createRelationRemoveMethodDeclaration(methodsBuilder, relation, target);
        }
    }

    protected void buildRelation(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        if (relation.is1ToMany()) {
            build1ToManyRelation(methodsBuilder, relation);
        } else {
            build1To1Relation(methodsBuilder, relation);
        }
    }

    // duplicate in PolicyCmptTypeImplRelationBuilder
    private String getPolicyCmptInterfaceAddPolicyCmptTypeMethod(IRelation r) {
        return "add" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationAddMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodName = getPolicyCmptInterfaceAddPolicyCmptTypeMethod(relation);
        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_ADD_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.VOID.getJavaClassName(),
            methodName,
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private void createRelationContainsMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String methodname = "contains" + relation.getTargetRoleSingular(); // TODO von IRelation
        // abfragen
        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_CONTAINS_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.PRIMITIVE_BOOLEAN.getJavaClassName(),
            methodname,
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptTypeInterfaceGetAllPcTypeMethodName(IRelation r) {
        return "get" + StringUtils.capitalise(r.getTargetRolePlural());
    }

    private void createRelationGetAllMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {

        String methodName = getPolicyCmptTypeInterfaceGetAllPcTypeMethodName(relation);
        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_GETALL_JAVADOC,
            relation.getTargetRoleSingular());
        String returnType = getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile())
                + "[]";
        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, returnType, methodName, new String[0],
            new String[0], javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptTypeInterfaceGetNumOfMethodName(IRelation r) {
        return "getAnzahl" + StringUtils.capitalise(r.getTargetRolePlural());
    }

    private void createRelationGetNumOfMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation) throws CoreException {
        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_NUMOF_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, Integer.TYPE,
            getPolicyCmptTypeInterfaceGetNumOfMethodName(relation), new String[0], new Class[0],
            javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptTypeInterfaceGetMethod(IRelation r) {
        return "get" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationGetterMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {

        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_GETTER_JAVADOC,
            relation.getTargetRoleSingular());
        String returnType = getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
            target.getIpsSrcFile());
        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT, returnType,
            getPolicyCmptTypeInterfaceGetMethod(relation), new String[0], new String[0], javaDoc,
            JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptTypeInterfaceRemoveMethodName(IRelation r) {
        return "remove" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationRemoveMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {
        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_REMOVE_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.VOID.getJavaClassName(),
            getPolicyCmptTypeInterfaceRemoveMethodName(relation),
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

    private String getPolicyCmptTypeInterfaceSetMethodName(IRelation r) {
        return "set" + StringUtils.capitalise(r.getTargetRoleSingular());
    }

    private void createRelationSetterMethodDeclaration(JavaCodeFragmentBuilder methodsBuilder, IRelation relation, IPolicyCmptType target)
            throws CoreException {

        String javaDoc = getPolicyCmptTypeInterfaceBuilder().getLocalizedText(RELATION_INTERFACE_SETTER_JAVADOC,
            relation.getTargetRoleSingular());

        methodsBuilder.methodBegin(
            Modifier.PUBLIC | Modifier.ABSTRACT,
            Datatype.VOID.getJavaClassName(),
            getPolicyCmptTypeInterfaceSetMethodName(relation),
            new String[] { "refObject" },
            new String[] { getPolicyCmptTypeInterfaceBuilder().getQualifiedClassName(
                target.getIpsSrcFile()) }, javaDoc, JavaSourceFileBuilder.ANNOTATION_GENERATED);
        methodsBuilder.appendln(";");
    }

}
