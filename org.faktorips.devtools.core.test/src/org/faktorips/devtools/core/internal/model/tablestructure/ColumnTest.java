/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.tablestructure;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.datatype.Datatype;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.tablestructure.IColumn;
import org.faktorips.util.message.MessageList;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Element;

@RunWith(MockitoJUnitRunner.class)
public class ColumnTest extends AbstractIpsPluginTest {

    private IIpsSrcFile ipsSrcFile;
    private TableStructure table;
    private IColumn column;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        IIpsProject project = newIpsProject();
        table = (TableStructure)newIpsObject(project, IpsObjectType.TABLE_STRUCTURE, "TestTable");
        ipsSrcFile = table.getIpsSrcFile();
        column = table.newColumn();
        ipsSrcFile.save(true, null);
    }

    @Test
    public void testSetName() {
        column.setName("newName");
        assertEquals("newName", column.getName());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testSetDatatype() {
        column.setDatatype("newType");
        assertEquals("newType", column.getDatatype());
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testRemove() {
        IColumn c0 = column;
        IColumn c1 = table.newColumn();
        IColumn c2 = table.newColumn();

        assertSame(c0, table.getColumns()[0]);
        assertSame(c1, table.getColumns()[1]);
        assertSame(c2, table.getColumns()[2]);

        c1.delete();
        assertEquals(2, table.getNumOfColumns());
        assertEquals(c0, table.getColumns()[0]);
        assertEquals(c2, table.getColumns()[1]);
        assertTrue(ipsSrcFile.isDirty());
    }

    @Test
    public void testToXml() {
        column = table.newColumn();
        column.setName("premium");
        column.setDatatype("Money");
        Element element = column.toXml(newDocument());

        assertEquals(column.getId(), element.getAttribute(IIpsObjectPart.PROPERTY_ID));
        assertEquals("premium", element.getAttribute(IIpsElement.PROPERTY_NAME));
        assertEquals("Money", element.getAttribute(IColumn.PROPERTY_DATATYPE));
    }

    @Test
    public void testInitFromXml() {
        column.initFromXml(getTestDocument().getDocumentElement());
        assertEquals("42", column.getId());
        assertEquals("premium", column.getName());
        assertEquals("Money", column.getDatatype());
    }

    @Test
    public void testValidateName() throws Exception {
        column.setName("Boolean");
        column.setDatatype(Datatype.STRING.getQualifiedName());
        MessageList ml = column.validate(ipsSrcFile.getIpsProject());
        assertNotNull(ml.getMessageByCode(IColumn.MSGCODE_INVALID_NAME));

        column.setName("integer");
        ml = column.validate(ipsSrcFile.getIpsProject());
        assertNull(ml.getMessageByCode(IColumn.MSGCODE_INVALID_NAME));
    }

    @Test
    public void testFindValueDatatype() throws CoreException {
        column.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        IIpsProject spyProject = spy(column.getIpsProject());
        ValueDatatype validValueDatatype = column.findValueDatatype(spyProject);

        verify(spyProject).findValueDatatype(Datatype.BOOLEAN.getQualifiedName());
        assertEquals(Datatype.BOOLEAN, validValueDatatype);

        column.setDatatype("NotADatatype");
        ValueDatatype invalidValueDatatype = column.findValueDatatype(spyProject);

        verify(spyProject).findValueDatatype("NotADatatype");
        assertNull(invalidValueDatatype);
    }

    @Test
    public void testFindValueDatatype_UsesCache() throws CoreException {
        IIpsProject ipsProject = column.getIpsProject();
        IIpsProject spyProject = spy(ipsProject);

        column.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        column.findValueDatatype(spyProject);

        column.setDatatype(Datatype.BOOLEAN.getQualifiedName());
        column.findValueDatatype(spyProject);
        verify(spyProject, times(1)).findValueDatatype(Datatype.BOOLEAN.getQualifiedName());
    }
}
