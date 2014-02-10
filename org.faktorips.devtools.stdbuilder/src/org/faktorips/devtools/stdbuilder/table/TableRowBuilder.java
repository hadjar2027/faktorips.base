/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.table;

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.JavaCodeFragmentBuilder;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.builder.DefaultBuilderSet;
import org.faktorips.devtools.core.builder.DefaultJavaSourceFileBuilder;
import org.faktorips.devtools.core.builder.TypeSection;
import org.faktorips.devtools.core.builder.naming.DefaultJavaClassNameProvider;
import org.faktorips.devtools.core.builder.naming.IJavaClassNameProvider;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.LocalizedStringsSet;
import org.faktorips.util.StringUtil;

public class TableRowBuilder extends DefaultJavaSourceFileBuilder {

    private final IJavaClassNameProvider javaClassNameProvider;

    private static final String KEY_CLASS_JAVADOC = "TABLE_ROW_BUILDER_CLASS_JAVADOC";

    private static final String KEY_CONSTRUCTOR_JAVADOC = "TABLE_ROW_BUILDER_CONSTRUCTOR_JAVADOC";

    public TableRowBuilder(DefaultBuilderSet builderSet) {
        super(builderSet, new LocalizedStringsSet(TableRowBuilder.class));
        setMergeEnabled(true);
        javaClassNameProvider = new DefaultJavaClassNameProvider(builderSet.isGeneratePublishedInterfaces()) {
            @Override
            public String getImplClassName(IIpsSrcFile ipsSrcFile) {
                return StringUtil.getFilenameWithoutExtension(ipsSrcFile.getName()) + "Row";
            }
        };
    }

    @Override
    public IJavaClassNameProvider getJavaClassNameProvider() {
        return javaClassNameProvider;
    }

    @Override
    protected void generateCodeForJavatype() throws CoreException {
        configureMainTypeSection();
        generateCodeForConstants();
        generateCodeForAttributes();
        generateCodeForConstructor();
        generateCodeForMethods();
    }

    private void configureMainTypeSection() throws CoreException {
        TypeSection mainSection = getMainTypeSection();
        mainSection.setClass(true);
        mainSection.setEnum(false);
        mainSection.setClassModifier(Modifier.PUBLIC);
        mainSection.setUnqualifiedName(getUnqualifiedClassName());
        mainSection.getJavaDocForTypeBuilder().javaDoc(getLocalizedText(getIpsSrcFile(), KEY_CLASS_JAVADOC),
                ANNOTATION_GENERATED);
    }

    private void generateCodeForConstants() throws CoreException {
        generateCodeForNullRowConstant();
    }

    private void generateCodeForNullRowConstant() throws CoreException {
        JavaCodeFragment initExpression = new JavaCodeFragment();
        initExpression.append("new ");
        initExpression.append(getUnqualifiedClassName());
        initExpression.append('(');
        for (int i = 0; i < getTableStructure().getNumOfColumns(); i++) {
            IColumn column = getTableStructure().getColumn(i);
            if (!column.isValid(getIpsProject())) {
                continue;
            }
            Datatype datatype = column.findValueDatatype(getIpsProject());
            DatatypeHelper datatypeHelper = getIpsProject().getDatatypeHelper(datatype);
            if (datatypeHelper != null) {
                initExpression.append(datatypeHelper.nullExpression());
                if (i < getTableStructure().getNumOfColumns() - 1) {
                    initExpression.append(", ");
                }
            }
        }
        initExpression.append(')');

        JavaCodeFragmentBuilder constantBuilder = getMainTypeSection().getConstantBuilder();
        constantBuilder.javaDoc("", ANNOTATION_GENERATED);
        constantBuilder.varDeclaration(Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL, getUnqualifiedClassName(),
                getFieldNameForNullRow(), initExpression);
        constantBuilder.appendln();
    }

    private void generateCodeForAttributes() throws CoreException {
        JavaCodeFragmentBuilder attributesBuilder = getMainTypeSection().getMemberVarBuilder();
        for (IColumn column : getTableStructure().getColumns()) {
            if (!column.isValid(getIpsProject())) {
                continue;
            }
            generateCodeForAttribute(attributesBuilder, column);
        }
    }

    private void generateCodeForAttribute(JavaCodeFragmentBuilder attributesBuilder, IColumn column)
            throws CoreException {

        ValueDatatype datatype = column.findValueDatatype(getIpsProject());
        DatatypeHelper datatypeHelper = getIpsProject().getDatatypeHelper(datatype);
        if (datatypeHelper != null) {
            attributesBuilder.javaDoc("", ANNOTATION_GENERATED);
            int modifiers = Modifier.PRIVATE | Modifier.FINAL;
            String datatypeName = datatypeHelper.getJavaClassName();
            String attributeName = getJavaNamingConvention().getMemberVarName(column.getName());
            attributesBuilder.varDeclaration(modifiers, datatypeName, attributeName);
            attributesBuilder.appendln();
        }
    }

    private void generateCodeForConstructor() throws CoreException {
        List<String> parameterNames = new ArrayList<String>(getTableStructure().getNumOfColumns());
        List<String> parameterClasses = new ArrayList<String>(getTableStructure().getNumOfColumns());
        JavaCodeFragment body = generateBodyForConstructor(parameterNames, parameterClasses);

        JavaCodeFragmentBuilder constructorBuilder = getMainTypeSection().getConstructorBuilder();
        constructorBuilder.javaDoc(getLocalizedText(getIpsSrcFile(), KEY_CONSTRUCTOR_JAVADOC), ANNOTATION_GENERATED);
        constructorBuilder.methodBegin(Modifier.PUBLIC, null, getUnqualifiedClassName(),
                parameterNames.toArray(new String[parameterNames.size()]),
                parameterClasses.toArray(new String[parameterClasses.size()]));
        constructorBuilder.append(body);
        constructorBuilder.methodEnd();
    }

    private JavaCodeFragment generateBodyForConstructor(List<String> parameterNames, List<String> parameterClasses)
            throws CoreException {

        JavaCodeFragment body = new JavaCodeFragment();
        for (IColumn column : getTableStructure().getColumns()) {
            if (!column.isValid(getIpsProject())) {
                continue;
            }
            Datatype datatype = column.findValueDatatype(getIpsProject());
            DatatypeHelper datatypeHelper = getIpsProject().getDatatypeHelper(datatype);
            if (datatype != null) {
                String attributeName = getJavaNamingConvention().getMemberVarName(column.getName());
                body.append("this."); //$NON-NLS-1$
                body.append(attributeName);
                body.append(" = "); //$NON-NLS-1$
                body.append(attributeName);
                body.append(';');
                body.appendln();
                parameterNames.add(attributeName);
                parameterClasses.add(datatypeHelper.getJavaClassName());
            }
        }
        return body;
    }

    private void generateCodeForMethods() throws CoreException {
        JavaCodeFragmentBuilder methodBuilder = getMainTypeSection().getMethodBuilder();
        generateCodeForGetterMethods(methodBuilder);
        generateCodeForToStringMethod(methodBuilder);
    }

    private void generateCodeForGetterMethods(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        for (IColumn column : getTableStructure().getColumns()) {
            if (!column.isValid(getIpsProject())) {
                continue;
            }
            Datatype datatype = column.findValueDatatype(getIpsProject());
            DatatypeHelper datatypeHelper = getIpsProject().getDatatypeHelper(datatype);
            if (datatypeHelper != null) {
                methodBuilder.javaDoc(column.getDescriptionText(getLanguageUsedInGeneratedSourceCode()),
                        ANNOTATION_GENERATED);
                String methodName = getJavaNamingConvention().getGetterMethodName(column.getName(), datatype);
                methodBuilder.methodBegin(Modifier.PUBLIC, datatypeHelper.getJavaClassName(), methodName,
                        new String[0], new String[0]);
                methodBuilder.append("return ");
                methodBuilder.append(getJavaNamingConvention().getMemberVarName(column.getName()));
                methodBuilder.append(';');
                methodBuilder.appendln();
                methodBuilder.methodEnd();
            }
        }
    }

    private void generateCodeForToStringMethod(JavaCodeFragmentBuilder methodBuilder) throws CoreException {
        methodBuilder.javaDoc("", ANNOTATION_GENERATED);
        appendOverrideAnnotation(methodBuilder, false);
        methodBuilder.methodBegin(Modifier.PUBLIC, String.class, "toString", new String[0], new Class[0]);
        methodBuilder.append("return \"\"");
        for (int i = 0; i < getTableStructure().getNumOfColumns(); i++) {
            IColumn column = getTableStructure().getColumn(i);
            if (!column.isValid(getIpsProject())) {
                continue;
            }
            methodBuilder.append(" + ");
            methodBuilder.append(getJavaNamingConvention().getMemberVarName(column.getName()));
            if (i < getTableStructure().getNumOfColumns() - 1) {
                methodBuilder.append(" + \"|\"");
            }
        }
        methodBuilder.append(';');
        methodBuilder.appendln();
        methodBuilder.methodEnd();
    }

    @Override
    public boolean isBuilderFor(IIpsSrcFile ipsSrcFile) throws CoreException {
        return IpsObjectType.TABLE_STRUCTURE.equals(ipsSrcFile.getIpsObjectType());
    }

    @Override
    protected void getGeneratedJavaElementsThis(List<IJavaElement> javaElements,
            IIpsObjectPartContainer ipsObjectPartContainer) {

        // not supported, yet
    }

    @Override
    public boolean isBuildingPublishedSourceFile() {
        return false;
    }

    public String getFieldNameForNullRow() {
        return "NULL_ROW";
    }

    private ITableStructure getTableStructure() {
        return (ITableStructure)getIpsObject();
    }

    @Override
    protected boolean generatesInterface() {
        return false;
    }

}
