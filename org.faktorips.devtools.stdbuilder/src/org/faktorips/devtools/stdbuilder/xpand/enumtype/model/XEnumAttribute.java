/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.devtools.stdbuilder.xpand.enumtype.model;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.faktorips.codegen.DatatypeHelper;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.codegen.dthelpers.InternationalStringDatatypeHelper;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.InternationalStringDatatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.enums.IEnumAttribute;
import org.faktorips.devtools.stdbuilder.EnumTypeDatatypeHelper;
import org.faktorips.devtools.stdbuilder.xpand.GeneratorModelContext;
import org.faktorips.devtools.stdbuilder.xpand.model.AbstractGeneratorModelNode;
import org.faktorips.devtools.stdbuilder.xpand.model.ModelService;
import org.faktorips.runtime.IRuntimeRepository;
import org.faktorips.values.InternationalString;

public class XEnumAttribute extends AbstractGeneratorModelNode {

    public XEnumAttribute(IEnumAttribute enumAttribute, GeneratorModelContext context, ModelService modelService) {
        super(enumAttribute, context, modelService);
    }

    protected IEnumAttribute getEnumAttribute() {
        return (IEnumAttribute)getIpsObjectPartContainer();
    }

    public boolean isUnique() {
        try {
            return getEnumAttribute().findIsUnique(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isDisplayName() {
        return getEnumAttribute().findIsUsedAsNameInFaktorIpsUi(getIpsProject());
    }

    public boolean isInherited() {
        return getEnumAttribute().isInherited();
    }

    public boolean isIdentifier() {
        return getEnumAttribute().findIsIdentifier(getIpsProject());
    }

    public boolean isLiteralName() {
        return getEnumAttribute().isEnumLiteralNameAttribute();
    }

    public boolean isMultilingualSupported() {
        return getEnumAttribute().isMultilingualSupported();
    }

    public boolean isMultilingual() {
        return getEnumAttribute().isMultilingual();
    }

    public boolean isDeclaredIn(XEnumType xEnumType) {
        return !xEnumType.hasSuperEnumType() || getEnumAttribute().getEnumType() == xEnumType.getEnumType();
    }

    public XEnumType getEnumType() {
        return getModelNode(getEnumAttribute().getEnumType(), XEnumType.class);
    }

    protected DatatypeHelper getDatatypeHelper(boolean mapMultilingual) {
        IEnumAttribute enumAttribute = getEnumAttribute();
        if (enumAttribute == null) {
            return getIpsProject().getDatatypeHelper(Datatype.STRING);
        } else if (mapMultilingual && enumAttribute.isMultilingual()) {
            return new InternationalStringDatatypeHelper(true);
        } else {
            return getDatatypeHelper(getDatatype());
        }
    }

    public ValueDatatype getDatatype() {
        try {
            return getEnumAttribute().findDatatype(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    /**
     * Returns the name of the Java class for the datatype used in the constructor and add's a
     * matching import. May differ from the datatype returned by the corresponding getter (for
     * example {@link InternationalString} vs. {@link String}).
     */
    public String getDatatypeNameForConstructor() {
        return addImport(getDatatypeHelper(true));
    }

    private Datatype getDatatypeUseWrappers() {
        ValueDatatype datatype = getDatatype();
        if (datatype.isPrimitive()) {
            datatype = datatype.getWrapperType();
        }
        DatatypeHelper datatypeHelper = getDatatypeHelper(datatype);
        return datatypeHelper.getDatatype();
    }

    public String getDatatypeNameUseWrappers() {
        return addImport(getDatatypeUseWrappers());
    }

    private String addImport(DatatypeHelper datatypeHelper) {
        return addImport(datatypeHelper.getJavaClassName());
    }

    public String getDatatypeName() {
        return addImport(getDatatype());
    }

    private String addImport(Datatype datatype) {
        return addImport(getDatatypeHelper(datatype));
    }

    /**
     * @see IEnumAttribute#findDatatypeIgnoreEnumContents(org.faktorips.devtools.core.model.ipsproject.IIpsProject)
     */
    protected ValueDatatype getDatatypeIgnoreEnumContents() {
        try {
            return getEnumAttribute().findDatatypeIgnoreEnumContents(getIpsProject());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    public boolean isGenerateField() {
        return (getEnumAttribute().getEnumType().isExtensible() || !isMultilingual()) && !isLiteralName();
    }

    public String getMemberVarName() {
        return getJavaNamingConvention().getMemberVarName(getName());
    }

    /**
     * Returns the assignment from a (International)String parameter (as used in the constructor
     * used by the {@link IRuntimeRepository} to create extended enum contents) to the member
     * variable.
     */
    public String getMemberVarAssignmentFromStringParameter() {
        ValueDatatype datatype = getDatatype();
        String paramName = getStringConstructorParamName();
        if (Datatype.STRING.equals(datatype) || datatype instanceof InternationalStringDatatype) {
            return paramName;
        }
        DatatypeHelper datatypeHelper = getDatatypeHelper(datatype);
        JavaCodeFragment newInstanceFromExpression;
        if (isExtensibleEnumDatatype(datatypeHelper)) {
            newInstanceFromExpression = ((EnumTypeDatatypeHelper)datatypeHelper)
                    .getCallGetValueByIdentifierCodeFragment(paramName, new JavaCodeFragment(
                            XEnumType.VAR_NAME_PRODUCT_REPOSITORY));
        } else {
            newInstanceFromExpression = datatypeHelper.newInstanceFromExpression(paramName);
        }
        addImport(newInstanceFromExpression.getImportDeclaration());
        return newInstanceFromExpression.getSourcecode();
    }

    public String getStringConstructorParamName() {
        return getMemberVarName() + "String";
    }

    private boolean isExtensibleEnumDatatype(DatatypeHelper helper) {
        if (helper instanceof EnumTypeDatatypeHelper) {
            EnumTypeDatatypeHelper enumHelper = (EnumTypeDatatypeHelper)helper;
            if (enumHelper.getEnumType().isExtensible()) {
                return true;
            }
        }
        return false;
    }

    public String getMethodNameGetter() {
        Datatype datatype = getDatatypeHelper(false).getDatatype();
        return getJavaNamingConvention().getGetterMethodName(getName(), datatype);
    }

    public String getMethodNameGetValueBy() {
        return "getValueBy" + StringUtils.capitalize(getName());
    }

    public String getMethodNameIsValueBy() {
        return "isValueBy" + StringUtils.capitalize(getName());
    }
}
