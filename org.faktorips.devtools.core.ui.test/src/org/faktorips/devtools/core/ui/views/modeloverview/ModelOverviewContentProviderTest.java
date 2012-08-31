/*******************************************************************************
 * Copyright (c) 2005-2012 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.ui.views.modeloverview;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.abstracttest.AbstractIpsPluginTest;
import org.faktorips.devtools.core.internal.model.pctype.PolicyCmptType;
import org.faktorips.devtools.core.internal.model.productcmpttype.ProductCmptType;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.type.AssociationType;
import org.faktorips.devtools.core.model.type.IAssociation;
import org.faktorips.devtools.core.model.type.IType;
import org.faktorips.devtools.core.ui.views.modeloverview.ModelOverviewContentProvider.ToChildAssociationType;
import org.junit.Test;

public class ModelOverviewContentProviderTest extends AbstractIpsPluginTest {

    @Test
    public void testHasChildren_NoChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        newProductCmptType(project, "TestProductComponentType");

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test
        for (Object object : elements) {
            assertFalse(contentProvider.hasChildren(object));
        }
    }

    @Test
    public void testGetChildren_ChildrenEmpty() throws CoreException {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        newProductCmptType(project, "TestProductComponentType");

        Object[] elements = contentProvider.getElements(project);

        // test
        assertNotNull(contentProvider.getChildren(elements[0]));
        assertNotNull(contentProvider.getChildren(elements[1]));
        assertEquals(0, contentProvider.getChildren(elements[0]).length);
        assertEquals(0, contentProvider.getChildren(elements[1]).length);
    }

    @Test
    public void testHasChildren_HasSubtypeChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");
        subCmptType.setSupertype(cmptType.getQualifiedName());

        IIpsProject project2 = newIpsProject();
        IType prodCmptType = newProductCmptType(project2, "TestProductComponentType");
        IType subProdCmptType = newProductCmptType(project2, "TestSubProductComponentType");

        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);
        Object[] elements2 = contentProvider.getElements(project2);

        // test
        assertTrue(contentProvider.hasChildren(elements[0]));
        assertTrue(contentProvider.hasChildren(elements2[0]));
    }

    @Test
    public void testGetElements_FindAssociationRootElements() throws CoreException {
        // setup
        // Status of root elements depends only on associations
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");

        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");

        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test the number of existing root elements
        assertEquals(2, elements.length);

        // test the identity of the root elements
        // project1
        List<IType> elementList = new ArrayList<IType>();
        elementList.add(((ComponentNode)elements[0]).getValue());
        elementList.add(((ComponentNode)elements[1]).getValue());
        assertTrue(elementList.contains(cmptType));
        assertTrue(elementList.contains(prodCmptType));
    }

    @Test
    public void testGetElements_FindSupertypeRootElements() throws CoreException {
        // setup
        // Status of root elements depends only on supertypes
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");

        subCmptType.setSupertype(cmptType.getQualifiedName());

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType subProdCmptType = newProductCmptType(project, "TestSubProductComponentType");

        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test the number of existing root elements
        assertEquals(2, elements.length);

        // test the identity of the root elements
        List<IType> elementList = new ArrayList<IType>();
        elementList.add(((ComponentNode)elements[0]).getValue());
        elementList.add(((ComponentNode)elements[1]).getValue());
        assertTrue(elementList.contains(cmptType));
        assertTrue(elementList.contains(prodCmptType));
    }

    @Test
    public void testHasChildren_HasAssociationChildren() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");

        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");

        IAssociation association2 = prodCmptType.newAssociation();
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // test
        for (Object element : elements) {
            assertTrue(contentProvider.hasChildren(element));
        }
    }

    @Test
    public void testHasChildren_HasAssociationAndSubtypeChildren() throws CoreException {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestPolicyComponentType2");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project, "TestSubPolicyComponentType");
        IType prodCmptType = newProductCmptType(project, "TestProductComponentType");
        IType associatedProdCmptType = newProductCmptType(project, "TestProductComponentType2");
        IType subProdCmptType = newProductCmptType(project, "TestSubProductComponentType");

        subCmptType.setSupertype(cmptType.getQualifiedName());
        subProdCmptType.setSupertype(prodCmptType.getQualifiedName());

        IAssociation association = cmptType.newAssociation();
        IAssociation association2 = prodCmptType.newAssociation();

        association.setTarget(associatedCmptType.getQualifiedName());
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association2.setTarget(associatedProdCmptType.getQualifiedName());

        Object[] elements = contentProvider.getElements(project);

        // test
        assertEquals(2, elements.length);
        assertTrue(contentProvider.hasChildren(elements[0]));
        assertTrue(contentProvider.hasChildren(elements[1]));
        assertEquals(2, contentProvider.getChildren(elements[0]).length);
        assertEquals(2, contentProvider.getChildren(elements[1]).length);
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * Tests if the root Elements have children and the returned lists are not null. Furthermore it
     * is checked that the correct {@link AbstractStructureNode AbstractStructureNodes} are
     * returned. At last the nodes under these structure nodes will be checked on identity.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * In the first {@link IIpsProject} a {@link CompositeNode} and a {@link SubtypeNode} are
     * expected as children of the root element. In the second project only a SubType node is
     * expected.
     * 
     */
    @Test
    public void testGetChildren_CorrectStructureAndComponentNodeHierarchy() throws CoreException {
        // setup
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();

        IIpsProject project1 = newIpsProject();
        IType cmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestPolicyComponentType");
        IType associatedCmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestPolicyComponentType2");
        IType subCmptType = newPolicyCmptTypeWithoutProductCmptType(project1, "TestSubPolicyComponentType");

        IAssociation association = cmptType.newAssociation();
        association.setTarget(associatedCmptType.getQualifiedName());
        subCmptType.setSupertype(cmptType.getQualifiedName());

        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        Object[] elements = contentProvider.getElements(project1);

        IModelOverviewNode subtypeNode = (IModelOverviewNode)contentProvider.getChildren(elements[0])[0];
        IModelOverviewNode compositeNode = (IModelOverviewNode)contentProvider.getChildren(elements[0])[1];

        // test
        // project1
        assertEquals(2, contentProvider.getChildren(elements[0]).length);

        assertTrue(compositeNode instanceof CompositeNode);
        assertTrue(subtypeNode instanceof SubtypeNode);

        List<ComponentNode> compositeChildren = ((CompositeNode)compositeNode).getChildren();
        assertEquals(1, compositeChildren.size());
        assertEquals(associatedCmptType, compositeChildren.get(0).getValue());

        List<ComponentNode> subtypeChildren = ((SubtypeNode)subtypeNode).getChildren();
        assertEquals(1, subtypeChildren.size());
        assertEquals(subCmptType, subtypeChildren.get(0).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * The associations which are derived via the supertype hierarchy should not be shown in the
     * ModelOverviewExplorer.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The elements in the supertype hierarchy should not be included in the construction of the
     * composite nodes.
     * 
     * 
     */
    @Test
    public void testGetChildren_NoSupertypeAssociations() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");
        PolicyCmptType hausratGrunddeckung = newPolicyCmptTypeWithoutProductCmptType(project, "HausratGrunddeckung");

        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        IAssociation associationVertrag2Deckung = vertrag.newAssociation();
        associationVertrag2Deckung.setTarget(deckung.getQualifiedName());
        associationVertrag2Deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IAssociation associationHausratVertrag2HausratGrunddeckung = hausratVertrag.newAssociation();
        associationHausratVertrag2HausratGrunddeckung.setTarget(hausratGrunddeckung.getQualifiedName());
        associationHausratVertrag2HausratGrunddeckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // tests
        ComponentNode vertragNode = (ComponentNode)elements[0];
        assertEquals(vertrag, vertragNode.getValue());
        assertEquals(1, elements.length);

        Object[] vertragStructureChildren = contentProvider.getChildren(elements[0]);

        // test that hausratvertrag is a subtype of vertrag
        SubtypeNode subtypeNode = (SubtypeNode)vertragStructureChildren[0];
        Object[] vertragSubtypeChildren = contentProvider.getChildren(subtypeNode);
        assertEquals(1, vertragSubtypeChildren.length);
        ComponentNode hausratVertragNode = (ComponentNode)vertragSubtypeChildren[0];
        assertEquals(hausratVertrag, hausratVertragNode.getValue());

        // test that only hausratgrunddeckung is a composite of hausratvertrag
        Object[] hausratVertragStructureChildren = contentProvider.getChildren(hausratVertragNode);
        CompositeNode hausratVertragCompositeNode = (CompositeNode)hausratVertragStructureChildren[0];
        Object[] hausratVertragCompositeChildren = contentProvider.getChildren(hausratVertragCompositeNode);
        assertEquals(1, hausratVertragCompositeChildren.length);
        assertEquals(hausratGrunddeckung, ((ComponentNode)hausratVertragCompositeChildren[0]).getValue());

        // test that deckung is a composite of vertrag
        CompositeNode compositeNode = (CompositeNode)vertragStructureChildren[1];
        Object[] vertragCompositeChildren = contentProvider.getChildren(compositeNode);
        assertEquals(1, vertragCompositeChildren.length);
        assertEquals(deckung, ((ComponentNode)vertragCompositeChildren[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * 
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The {@link SubtypeNode} should be before the {@link CompositeNode} in the list of
     * {@link ComponentNode} children
     * 
     */
    @Test
    public void testGetChildren_ComponentNodeChildrenOrder() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");

        hausratVertrag.setSupertype(vertrag.getQualifiedName());

        IAssociation associationVertrag2Deckung = vertrag.newAssociation();
        associationVertrag2Deckung.setTarget(deckung.getQualifiedName());
        associationVertrag2Deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // test
        ModelOverviewContentProvider contentProvider = new ModelOverviewContentProvider();
        Object[] elements = contentProvider.getElements(project);

        // tests
        ComponentNode vertragNode = (ComponentNode)elements[0];
        assertEquals(vertrag, vertragNode.getValue());
        assertEquals(1, elements.length);

        Object[] vertragChildren = contentProvider.getChildren(elements[0]);
        assertTrue(vertragChildren[0] instanceof SubtypeNode);
        assertTrue(vertragChildren[1] instanceof CompositeNode);
    }

    @Test
    public void testGetElements_InputInstanceofIType() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
        PolicyCmptType rootPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Root Node");
        PolicyCmptType superPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Super Node");

        // the Master-to-Detail association makes a root node out of rootPolicy
        IAssociation association = rootPolicy.newAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(leafPolicy.getQualifiedName());

        // the Supertype superPolicy is no root node of leafPolicy
        leafPolicy.setSupertype(superPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(leafPolicy);
        assertEquals(1, elements.length);
        assertEquals(rootPolicy, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * An {@link IType} is indirectly targeted by an {@link IAssociation} over the supertype
     * hierarchy: <br />
     * Example: if A supertype B and B -> C, then !A->C
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * B is the only root for C
     * 
     */
    @Test
    public void testGetElements_OmitSupertypeNode() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        ProductCmptType leafPolicy = newProductCmptType(project, "Leaf Node");
        ProductCmptType rootPolicy = newProductCmptType(project, "Root Node");
        ProductCmptType superPolicy = newProductCmptType(project, "Super Node");

        // the Master-to-Detail association makes an indirect root node out of rootPolicy
        IAssociation association = rootPolicy.newAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(leafPolicy.getQualifiedName());

        // the Supertype superPolicy is no root node of leafProduct
        rootPolicy.setSupertype(superPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(leafPolicy);
        assertEquals(1, elements.length);
        assertEquals(rootPolicy, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * An {@link IType} is indirectly targeted by an {@link IAssociation} over the supertype
     * hierarchy: <br />
     * Example: if A -> B and B supertype of C, then A->C
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * A is the root for object C
     * 
     */
    @Test
    public void testGetElements_ConsiderSupertypeAssociation() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
        PolicyCmptType rootPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Root Node");
        PolicyCmptType superPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Super Node");

        // the Master-to-Detail association makes an indirect root node out of rootPolicy
        IAssociation association = rootPolicy.newAssociation();
        association.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        association.setTarget(superPolicy.getQualifiedName());

        // the Supertype superPolicy is no root node of leafPolicy
        leafPolicy.setSupertype(superPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(leafPolicy);
        assertEquals(1, elements.length);
        assertEquals(rootPolicy, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * If the selected object is the only object in the hierarchy, it is the root element.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * The element is also the root element
     */
    @Test
    public void testGetElements_SingleElementIsItsOwnRoot() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(leafPolicy);
        assertEquals(1, elements.length);
        assertEquals(leafPolicy, ((ComponentNode)elements[0]).getValue());
    }

    /**
     * 
     * <strong>Scenario:</strong><br>
     * If a child is already a root node, none of its parent nodes should be root. The problem is,
     * that a node can be reached via different paths.
     * <p>
     * <strong>Expected Outcome:</strong><br>
     * Only the topmost element should be root.
     */
    @Test
    public void testGetElements_OnlyOneRootInASingleHierarchyPath() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        ProductCmptType produkt = newProductCmptType(project, "Produkt");
        ProductCmptType hausratProdukt = newProductCmptType(project, "HausratProdukt");
        ProductCmptType deckungstyp = newProductCmptType(project, "Deckungstyp");
        ProductCmptType hausratGrunddeckungstyp = newProductCmptType(project, "HausratGrunddeckungstyp");

        // set supertypes
        hausratProdukt.setSupertype(produkt.getQualifiedName());
        hausratGrunddeckungstyp.setSupertype(deckungstyp.getQualifiedName());

        // set associations
        IAssociation deckungstyp2produkt = produkt.newAssociation();
        deckungstyp2produkt.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        deckungstyp2produkt.setTarget(deckungstyp.getQualifiedName());

        IAssociation hausratGrunddeckungstyp2hausratProdukt = hausratProdukt.newAssociation();
        hausratGrunddeckungstyp2hausratProdukt.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        hausratGrunddeckungstyp2hausratProdukt.setTarget(hausratGrunddeckungstyp.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(hausratGrunddeckungstyp);

        assertEquals(1, elements.length);
        assertEquals(produkt, ((ComponentNode)elements[0]).getValue());

    }

    @Test
    public void testGetElements_ElementIsItsOwnRootInPureSupertypeHierarchy() throws CoreException {
        // setup
        IIpsProject project = newIpsProject();
        PolicyCmptType leafPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Leave Node");
        PolicyCmptType superSuperPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Root Node");
        PolicyCmptType superPolicy = newPolicyCmptTypeWithoutProductCmptType(project, "Super Node");

        // the Supertype superPolicy is no root node of leafPolicy
        leafPolicy.setSupertype(superPolicy.getQualifiedName());
        superPolicy.setSupertype(superSuperPolicy.getQualifiedName());

        // test
        ModelOverviewContentProvider provider = new ModelOverviewContentProvider();
        Object[] elements = provider.getElements(leafPolicy);
        assertEquals(1, elements.length);
        assertEquals(leafPolicy, ((ComponentNode)elements[0]).getValue());
    }

    @Test
    public void testGetElements_ComputeRootToLeafHierarchyPaths() throws CoreException {
        // setup
        ModelOverviewContentProvider contenProvider = new ModelOverviewContentProvider();

        IIpsProject project = newIpsProject();
        PolicyCmptType vertrag = newPolicyCmptTypeWithoutProductCmptType(project, "Vertrag");
        PolicyCmptType deckung = newPolicyCmptTypeWithoutProductCmptType(project, "Deckung");
        PolicyCmptType hausratVertrag = newPolicyCmptTypeWithoutProductCmptType(project, "HausratVertrag");
        PolicyCmptType hausratGrunddeckung = newPolicyCmptTypeWithoutProductCmptType(project, "HausratGrunddeckung");

        List<IType> componentList = new ArrayList<IType>();
        componentList.add(vertrag);
        componentList.add(deckung);
        componentList.add(hausratVertrag);
        componentList.add(hausratGrunddeckung);

        hausratVertrag.setSupertype(vertrag.getQualifiedName());
        hausratGrunddeckung.setSupertype(deckung.getQualifiedName());

        IAssociation associationVertrag2Deckung = vertrag.newAssociation();
        associationVertrag2Deckung.setTarget(deckung.getQualifiedName());
        associationVertrag2Deckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        IAssociation associationHausratVertrag2HausratGrunddeckung = hausratVertrag.newAssociation();
        associationHausratVertrag2HausratGrunddeckung.setTarget(hausratGrunddeckung.getQualifiedName());
        associationHausratVertrag2HausratGrunddeckung.setAssociationType(AssociationType.COMPOSITION_MASTER_TO_DETAIL);

        // get the rootCandidates
        List<AssociationType> associationTypeFilter = new ArrayList<AssociationType>();
        associationTypeFilter.add(AssociationType.COMPOSITION_MASTER_TO_DETAIL);
        associationTypeFilter.add(AssociationType.AGGREGATION);
        Collection<IType> rootCandidatesForIType = contenProvider.getRootElementsForIType(hausratGrunddeckung,
                componentList, ModelOverviewContentProvider.ToChildAssociationType.SELF, new ArrayList<IType>(),
                new ArrayList<Deque<PathElement>>(), new ArrayDeque<PathElement>());

        // compute the actual list of root elements and most importantly the list of paths from the
        // root elements to the selected element
        List<Deque<PathElement>> paths = new ArrayList<Deque<PathElement>>();
        contenProvider.getRootElementsForIType(hausratGrunddeckung, componentList,
                ModelOverviewContentProvider.ToChildAssociationType.SELF, rootCandidatesForIType, paths,
                new ArrayDeque<PathElement>());

        // expected paths
        Deque<PathElement> paths1 = new ArrayDeque<PathElement>();
        paths1.push(new PathElement(hausratGrunddeckung, ToChildAssociationType.SELF));
        paths1.push(new PathElement(hausratVertrag, ToChildAssociationType.ASSOCIATION));
        paths1.push(new PathElement(vertrag, ToChildAssociationType.SUPERTYPE));

        Deque<PathElement> paths2 = new ArrayDeque<PathElement>();
        paths1.push(new PathElement(hausratGrunddeckung, ToChildAssociationType.SELF));
        paths1.push(new PathElement(deckung, ToChildAssociationType.SUPERTYPE));
        paths1.push(new PathElement(vertrag, ToChildAssociationType.ASSOCIATION));

        // tests
        assertEquals(2, paths.size());

        paths.contains(paths1);
        paths.contains(paths2);
    }
}
