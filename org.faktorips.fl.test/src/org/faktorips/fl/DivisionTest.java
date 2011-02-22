/*******************************************************************************
 * Copyright (c) 2005-2010 Faktor Zehn AG und andere.
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

package org.faktorips.fl;

import java.util.Locale;

import org.faktorips.datatype.Datatype;
import org.faktorips.values.Decimal;
import org.faktorips.values.Money;
import org.junit.Test;

/**
 *
 */
public class DivisionTest extends CompilerAbstractTest {
    @Test
    public void testDecimalDecimal() throws Exception {
        execAndTestSuccessfull("10.8 / 4.2", Decimal.valueOf("2.5714285714"), Datatype.DECIMAL);
    }

    @Test
    public void testDecimalInt() throws Exception {
        execAndTestSuccessfull("10.8 / 4", Decimal.valueOf("2.7000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntDecimal() throws Exception {
        execAndTestSuccessfull("10 / 2.5", Decimal.valueOf("4.0000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testDecimalInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("10.8 / WHOLENUMBER(4.1)", Decimal.valueOf("2.7000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerDecimal() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10.2) / 2.5", Decimal.valueOf("4.0000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntInt() throws Exception {
        execAndTestSuccessfull("10 / 4", Decimal.valueOf("2.5000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("10 / WHOLENUMBER(4)", Decimal.valueOf("2.5000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerInt() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10) / WHOLENUMBER(4)", Decimal.valueOf("2.5000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testIntegerInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("WHOLENUMBER(10) / WHOLENUMBER(4)", Decimal.valueOf("2.5000000000"), Datatype.DECIMAL);
    }

    @Test
    public void testMoneyDecimal() throws Exception {
        execAndTestSuccessfull("10.80EUR / 4.2", Money.valueOf("2.57EUR"), Datatype.MONEY);
    }

    @Test
    public void testMoneyInt() throws Exception {
        execAndTestSuccessfull("10.00EUR / 4", Money.valueOf("2.50EUR"), Datatype.MONEY);
    }

    @Test
    public void testMoneyInteger() throws Exception {
        compiler.add(new ExcelFunctionsResolver(Locale.ENGLISH));
        execAndTestSuccessfull("10.00EUR / WHOLENUMBER(4)", Money.valueOf("2.50EUR"), Datatype.MONEY);
    }

}