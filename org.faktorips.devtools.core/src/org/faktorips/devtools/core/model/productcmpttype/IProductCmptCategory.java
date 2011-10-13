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

package org.faktorips.devtools.core.model.productcmpttype;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptTypeAttribute;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.core.model.type.IProductCmptProperty;

/**
 * Used to arrange {@link IProductCmptProperty} into groups that make more sense to the insurance
 * department than a technical arrangement. For example, a category called
 * <em>premium computation</em> including a <em>premium table</em> might be created. Prior to this
 * feature, the <em>premium table</em> would be automatically assigned to the
 * <em>tables and formulas</em> section.
 * <p>
 * A category can be marked to be the <em>default category</em> for each type of
 * {@link IProductCmptProperty}. New parts of that property type will then automatically be
 * referenced by the corresponding default category. Of course, the parts can still be moved to
 * other categories by the user.
 * <p>
 * Furthermore, a category can be marked to be inherited from the supertype. In this case, a
 * category with the same name must be found in the supertype.
 * 
 * @since 3.6
 * 
 * @author Alexander Weickmann
 */
public interface IProductCmptCategory extends IIpsObjectPart {

    public final static String XML_TAG_NAME = "Category"; //$NON-NLS-1$

    public final static String PROPERTY_INHERITED = "inherited"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_FORMULA_SIGNATURE_DEFINITIONS = "defaultForMethods"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_VALIDATION_RULES = "defaultForValidationRules"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_TABLE_STRUCTURE_USAGES = "defaultForTableStructureUsages"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_POLICY_CMPT_TYPE_ATTRIBUTES = "defaultForPolicyCmptTypeAttributes"; //$NON-NLS-1$

    public final static String PROPERTY_DEFAULT_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES = "defaultForProductCmptTypeAttributes"; //$NON-NLS-1$

    public final static String PROPERTY_POSITION = "position"; //$NON-NLS-1$

    public final static String MSGCODE_PREFIX = "ProductCmptCategory-"; //$NON-NLS-1$

    public final static String MSGCODE_NAME_IS_EMPTY = MSGCODE_PREFIX + "NameIsEmpty"; //$NON-NLS-1$

    public final static String MSGCODE_NAME_ALREADY_USED_IN_TYPE_HIERARCHY = "NameAlreadyUsedInTypeHierarchy"; //$NON-NLS-1$

    public final static String MSGCODE_INHERITED_BUT_NOT_FOUND_IN_SUPERTYPE_HIERARCHY = MSGCODE_PREFIX
            + "InheritedButNotFoundInSupertypeHierarchy"; //$NON-NLS-1$

    public final static String MSGCODE_INHERITED_BUT_NO_SUPERTYPE = MSGCODE_PREFIX + "InheritedButNoSupertype"; //$NON-NLS-1$

    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_FORMULA_SIGNATURE_DEFINITIONS = MSGCODE_PREFIX
            + "DuplicateDefaultsForFormulaSignatureDefinitions"; //$NON-NLS-1$

    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_VALIDATION_RULES = MSGCODE_PREFIX
            + "DuplicateDefaultsForValidationRules"; //$NON-NLS-1$

    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_TABLE_STRUCTURE_USAGES = MSGCODE_PREFIX
            + "DuplicateDefaultsForTableStructureUsages"; //$NON-NLS-1$

    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_POLICY_CMPT_TYPE_ATTRIBUTES = MSGCODE_PREFIX
            + "DuplicateDefaultsForPolicyCmptTypeAttributes"; //$NON-NLS-1$

    public final static String MSGCODE_DUPLICATE_DEFAULTS_FOR_PRODUCT_CMPT_TYPE_ATTRIBUTES = MSGCODE_PREFIX
            + "DuplicateDefaultsForProductCmptTypeAttributes"; //$NON-NLS-1$

    /**
     * Returns the {@link IProductCmptType} this category belongs to.
     */
    public IProductCmptType getProductCmptType();

    /**
     * Sets the name of this category.
     * 
     * @param name The new name of this category
     */
    public void setName(String name);

    /**
     * Returns a list containing the {@link IProductCmptProperty} currently referenced by this
     * category.
     * <p>
     * If this category is <em>inherited</em>, this method does <strong>not</strong> return
     * {@link IProductCmptProperty}s referenced by the supertype hierarchy. The method
     * {@link #findAllReferencedProductCmptProperties(IIpsProject)} can be used to retrieve all
     * referenced {@link IProductCmptProperty}s.
     * 
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs during the search
     */
    public List<IProductCmptProperty> findReferencedProductCmptProperties(IIpsProject ipsProject) throws CoreException;

    /**
     * Returns whether the indicated {@link IProductCmptProperty} is referenced by this category.
     * <p>
     * This method does <strong>not</strong> consider {@link IProductCmptProperty}s referenced by
     * the supertype hierarchy. To achieve this, use
     * {@link #findIsReferencedProductCmptProperty(IProductCmptProperty, IIpsProject)}.
     * 
     * @param productCmptProperty The property to check for reference
     */
    public boolean isReferencedProductCmptProperty(IProductCmptProperty productCmptProperty);

    /**
     * Returns a list containing the {@link IProductCmptProperty} currently referenced by this
     * category.
     * <p>
     * In contrast to {@link #findReferencedProductCmptProperties(IIpsProject)}, the list returned
     * by this method <strong>does</strong> include {@link IProductCmptProperty}s referenced by the
     * supertype hierarchy if this category is marked as <em>inherited</em>.
     * <p>
     * The properties are sorted in such a way that references of supertypes are positioned at the
     * top of the list.
     * 
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy
     */
    public List<IProductCmptProperty> findAllReferencedProductCmptProperties(IIpsProject ipsProject)
            throws CoreException;

    /**
     * Returns whether the indicated {@link IProductCmptProperty} is referenced by this category.
     * <p>
     * This method <strong>does</strong> consider the supertype hierarchy if this category is marked
     * as <em>inherited</em>.
     * 
     * @param property The property to check for reference
     * @param ipsProject The project which IPS object path is used for the search
     * 
     * @throws CoreException If an error occurs while searching the supertype hierarchy
     */
    public boolean findIsReferencedProductCmptProperty(IProductCmptProperty property, IIpsProject ipsProject)
            throws CoreException;

    /**
     * Creates a reference to the given {@link IProductCmptTypeAttribute} in this category and
     * returns it.
     * 
     * @param productCmptTypeAttribute The {@link IProductCmptTypeAttribute} to reference in this
     *            category
     * 
     * @throws NullPointerException If the parameter is null
     * @throws IllegalArgumentException If the given {@link IProductCmptTypeAttribute} does not
     *             belong to the {@link IProductCmptType} this category belongs to
     */
    public IProductCmptPropertyDirectReference newProductCmptPropertyReference(IProductCmptTypeAttribute productCmptTypeAttribute);

    /**
     * Creates a reference to the given {@link IPolicyCmptTypeAttribute} in this category and
     * returns it.
     * 
     * @param policyCmptTypeAttribute The {@link IPolicyCmptTypeAttribute} to reference in this
     *            category
     * 
     * @throws NullPointerException If the parameter is null
     * @throws IllegalArgumentException If the given {@link IPolicyCmptTypeAttribute} does not
     *             belong to the {@link IPolicyCmptType} the {@link IProductCmptType} this category
     *             belongs to configures
     */
    public IProductCmptPropertyExternalReference newProductCmptPropertyReference(IPolicyCmptTypeAttribute policyCmptTypeAttribute);

    /**
     * Creates a reference to the given {@link IProductCmptTypeMethod} in this category and returns
     * it.
     * 
     * @param productCmptTypeMethod The {@link IProductCmptTypeMethod} to reference in this category
     * 
     * @throws NullPointerException If the parameter is null
     * @throws IllegalArgumentException If the given {@link IProductCmptTypeMethod} does not belong
     *             to the {@link IProductCmptType} this category belongs to
     */
    public IProductCmptPropertyDirectReference newProductCmptPropertyReference(IProductCmptTypeMethod productCmptTypeMethod);

    /**
     * Creates a reference to the given {@link ITableStructureUsage} in this category and returns
     * it.
     * 
     * @param tableStructureUsage The {@link ITableStructureUsage} to reference in this category
     * 
     * @throws NullPointerException If the parameter is null
     * @throws IllegalArgumentException If the given {@link ITableStructureUsage} does not belong to
     *             the {@link IProductCmptType} this category belongs to
     */
    public IProductCmptPropertyDirectReference newProductCmptPropertyReference(ITableStructureUsage tableStructureUsage);

    /**
     * Creates a reference to the given {@link IValidationRule} in this category and returns it.
     * 
     * @param validationRule The {@link IValidationRule} to reference in this category
     * 
     * @throws NullPointerException If the parameter is null
     * @throws IllegalArgumentException If the given {@link IValidationRule} does not belong to the
     *             {@link IPolicyCmptType} the {@link IProductCmptType} this category belongs to
     *             configures
     */
    public IProductCmptPropertyExternalReference newProductCmptPropertyReference(IValidationRule validationRule);

    /**
     * Deletes the first {@link IProductCmptPropertyReference} that references the indicated
     * {@link IProductCmptProperty} from this category.
     * <p>
     * Returns false if the {@link IProductCmptProperty} is not referenced by this category, true
     * otherwise.
     * 
     * @param productCmptProperty The {@link IProductCmptProperty} to no longer reference from this
     *            category
     * 
     * @throws NullPointerException If the parameter is null
     */
    public boolean deleteProductCmptPropertyReference(IProductCmptProperty productCmptProperty);

    /**
     * Returns whether this category is inherited from the supertype hierarchy.
     */
    public boolean isInherited();

    /**
     * Sets whether this category is inherited from the supertype hierarchy.
     * 
     * @param inherited Flag indicating whether this category is inherited from the supertype
     *            hierarchy
     */
    public void setInherited(boolean inherited);

    /**
     * Returns whether this category is the default category for {@link IProductCmptTypeMethod}s
     * defining formula signatures.
     */
    public boolean isDefaultForFormulaSignatureDefinitions();

    /**
     * Sets whether this category is the default category for {@link IProductCmptTypeMethod}s.
     * 
     * @param defaultForFormulaSignatureDefinitions Flag indicating whether this category shall be
     *            the default category for {@link IProductCmptTypeMethod}s defining formula
     *            signatures
     */
    public void setDefaultForFormulaSignatureDefinitions(boolean defaultForFormulaSignatureDefinitions);

    /**
     * Returns whether this category is the default category for product relevant
     * {@link IPolicyCmptTypeAttribute}s.
     */
    public boolean isDefaultForPolicyCmptTypeAttributes();

    /**
     * Sets whether this category is the default category for product relevant
     * {@link IPolicyCmptTypeAttribute}s.
     * 
     * @param defaultForPolicyCmptTypeAttributes Flag indicating whether this category shall be the
     *            default category for product relevant {@link IPolicyCmptTypeAttribute}s
     */
    public void setDefaultForPolicyCmptTypeAttributes(boolean defaultForPolicyCmptTypeAttributes);

    /**
     * Returns whether this category is the default category for {@link IProductCmptTypeAttribute}s.
     */
    public boolean isDefaultForProductCmptTypeAttributes();

    /**
     * Sets whether this category is the default category for {@link IProductCmptTypeAttribute}s.
     * 
     * @param defaultForProductCmptTypeAttributes Flag indicating whether this category shall be the
     *            default category for {@link IProductCmptTypeAttribute}s
     */
    public void setDefaultForProductCmptTypeAttributes(boolean defaultForProductCmptTypeAttributes);

    /**
     * Returns whether this category is the default category for {@link ITableStructureUsage}s.
     */
    public boolean isDefaultForTableStructureUsages();

    /**
     * Sets whether this category is the default category for {@link ITableStructureUsage}s.
     * 
     * @param defaultForTableStructureUsages Flag indicating whether this category shall be the
     *            default category for {@link ITableStructureUsage}s
     */
    public void setDefaultForTableStructureUsages(boolean defaultForTableStructureUsages);

    /**
     * Returns whether this category is the default category for product relevant
     * {@link IValidationRule}s.
     */
    public boolean isDefaultForValidationRules();

    /**
     * Sets whether this category is the default category for product relevant
     * {@link IValidationRule}s.
     * 
     * @param defaultForValidationRules Flag indicating whether this category shall be the default
     *            category for product relevant {@link IValidationRule}s
     */
    public void setDefaultForValidationRules(boolean defaultForValidationRules);

    /**
     * Sets the {@link Position} of this category.
     * 
     * @param side The {@link Position} to set this category to
     * 
     * @throws NullPointerException If the parameter is null
     */
    public void setPosition(Position side);

    /**
     * Returns the {@link Position} of this category.
     */
    public Position getPosition();

    /**
     * Returns whether this category is positioned at the left.
     */
    public boolean isAtLeftPosition();

    /**
     * Returns whether this category is positioned at the right.
     */
    public boolean isAtRightPosition();

    /**
     * Returns how many references to {@link IProductCmptProperty}s are contained in this category.
     * <p>
     * This method does <strong>not</strong> consider the number of references from the supertype
     * hierarchy.
     */
    public int getNumberOfProductCmptPropertyReferences();

    /**
     * Moves the {@link IProductCmptPropertyReference}s identified by the indexes up or down by one
     * position and returns the new indexes of the moved objects.
     * <p>
     * If one of the indexes is 0 (the first object), no object is moved up. If one of the indexes
     * is the number of objects - 1 (the last object) no object is moved down.
     * 
     * @param indexes The indexes identifying the {@link IProductCmptPropertyReference}s to be moved
     * @param up Flag indicating whether to move up or down
     * 
     * @throws NullPointerException If indexes is null
     * @throws IndexOutOfBoundsException If one of the indexes does not identify an
     *             {@link IProductCmptPropertyReference}
     */
    public int[] moveProductCmptPropertyReferences(int[] indexes, boolean up);

    /**
     * Defines the position of this category.
     */
    public static enum Position {

        LEFT,
        RIGHT;

    }

}