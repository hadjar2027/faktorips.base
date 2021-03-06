/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.codegen.conversion;

import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.datatype.Datatype;

public class IntegerToPrimitiveIntCg extends AbstractSingleConversionCg {

    public IntegerToPrimitiveIntCg() {
        super(Datatype.INTEGER, Datatype.PRIMITIVE_INT);
    }

    @Override
    public JavaCodeFragment getConversionCode(JavaCodeFragment fromValue) {
        fromValue.append(".intValue()"); //$NON-NLS-1$
        return fromValue;
    }

}
