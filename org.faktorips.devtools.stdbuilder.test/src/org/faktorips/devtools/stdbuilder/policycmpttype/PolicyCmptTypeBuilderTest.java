/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
 * 
 * Alle Rechte vorbehalten.
 * 
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele, Konfigurationen,
 * etc.) duerfen nur unter den Bedingungen der Faktor-Zehn-Community Lizenzvereinbarung - Version
 * 0.1 (vor Gruendung Community) genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 * http://www.faktorzehn.org/fips:lizenz eingesehen werden kann.
 * 
 * Mitwirkende: Faktor Zehn AG - initial API and implementation - http://www.faktorzehn.de
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype;

import org.eclipse.jdt.core.IType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.stdbuilder.AbstractStdBuilderTest;
import org.junit.Before;

public abstract class PolicyCmptTypeBuilderTest extends AbstractStdBuilderTest {

    protected final static String POLICY_NAME = "PolicyCmptType";

    protected GenPolicyCmptType genPolicyCmptType;

    protected IPolicyCmptType policyCmptType;

    protected IType javaClass;

    protected IType javaInterface;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();

        policyCmptType = newPolicyCmptType(ipsProject, POLICY_NAME);
        genPolicyCmptType = new GenPolicyCmptType(policyCmptType, builderSet);

        javaClass = getGeneratedJavaClass(policyCmptType, false, POLICY_NAME);
        javaInterface = getGeneratedJavaInterface(policyCmptType, false, POLICY_NAME);
    }
}
