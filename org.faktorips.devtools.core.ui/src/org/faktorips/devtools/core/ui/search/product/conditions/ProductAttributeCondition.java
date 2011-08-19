/*******************************************************************************
 * Copyright (c) 2005-2011 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.search.product.conditions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.productcmpt.IAttributeValue;
import org.faktorips.devtools.core.model.productcmpt.IProductCmptGeneration;
import org.faktorips.devtools.core.model.productcmpttype.IProductCmptType;
import org.faktorips.devtools.core.model.type.IAttribute;

public class ProductAttributeCondition implements ICondition {

    private final class ProductAttributeArgumentProvider implements OperandProvider {
        private final IAttribute attribute;

        public ProductAttributeArgumentProvider(IAttribute attribute) {
            this.attribute = attribute;
        }

        @Override
        public String getSearchOperand(IProductCmptGeneration productComponentGeneration) {
            IAttributeValue attributeValue = productComponentGeneration.getAttributeValue(attribute.getName());
            // TODO bei nicht-migrierten produktbausteinen droht NPE
            if (attributeValue == null) {
                return null;
            }
            return attributeValue.getValue();
        }
    }

    @Override
    public List<? extends IIpsElement> getSearchableElements(IProductCmptType element) throws CoreException {
        return element.findAllAttributes(element.getIpsProject());
    }

    @Override
    public List<ISearchOperatorType> getSearchOperatorTypes(IIpsElement elementPart) {
        List<ISearchOperatorType> searchOperatorTypes = new ArrayList<ISearchOperatorType>();

        searchOperatorTypes.addAll(Arrays.asList(EqualitySearchOperatorType.values()));

        ValueDatatype valueDatatype = getValueDatatype(elementPart);

        if (valueDatatype.supportsCompare()) {
            searchOperatorTypes.addAll(Arrays.asList(ComparableSearchOperatorType.values()));
        }

        return searchOperatorTypes;
    }

    @Override
    public ValueDatatype getValueDatatype(IIpsElement elementPart) {
        IAttribute attribute = (IAttribute)elementPart;
        try {
            return attribute.findDatatype(attribute.getIpsProject());
        } catch (CoreException e) {
            // TODO Auto-generated catch block
            throw new RuntimeException(e);
        }
    }

    @Override
    public OperandProvider createOperandProvider(IIpsElement elementPart) {
        return new ProductAttributeArgumentProvider((IAttribute)elementPart);
    }

}
