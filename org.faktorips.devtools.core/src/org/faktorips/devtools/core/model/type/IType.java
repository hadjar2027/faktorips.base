/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) dürfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung – Version 0.1 (vor Gründung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation 
 *
 *******************************************************************************/

package org.faktorips.devtools.core.model.type;

import org.eclipse.core.runtime.CoreException;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.IIpsObject;
import org.faktorips.devtools.core.model.IIpsProject;

/**
 * Common interface for types, policy component type and product component type.
 * 
 * @author Jan Ortmann
 */
public interface IType extends IIpsObject, Datatype {

    public final static String PROPERTY_SUPERTYPE = "supertype";
    public final static String PROPERTY_ABSTRACT= "abstract";

    /**
     * Prefix for all message codes of this class.
     */
    public final static String MSGCODE_PREFIX = "Type-";
    
    /**
     * Validation message code to indicate that the supertype hierarchy contains a cycle.
     */
    public final static String MSGCODE_CYCLE_IN_TYPE_HIERARCHY = MSGCODE_PREFIX + "CycleInSupertypeHierarchy"; //$NON-NLS-1$

    /**
     * Validation message code to indicate that there exists an error within the type hierarchy of this type.
     */
    public final static String MSGCODE_INCONSISTENT_TYPE_HIERARCHY = MSGCODE_PREFIX + "InconsistentTypeHierarchy"; //$NON-NLS-1$
    
    /**
     * Validation message code to indicate that the supertype can not be found.
     */
    public final static String MSGCODE_SUPERTYPE_NOT_FOUND = MSGCODE_PREFIX + "SupertypeNotFound"; //$NON-NLS-1$

    /**
     * Returns <code>true</code> if this is an abstract type, <code>false</code> otherwise.
     */
    public boolean isAbstract();

    /**
     * Sets wether this is an abstract type or not.
     */
    public void setAbstract(boolean newValue);
    
    /**
     * Returns the qualified name of the type's supertype. Returns an empty
     * string if this type has no supertype.
     */
    public String getSupertype();
    
    /**
     * Returns <code>true</code> if this type has a supertype, otherwise <code>false</code>.
     * This method also returns <code>true</code> if the type refers to a supertype but the 
     * supertype does not exist.
     */
    public boolean hasSupertype();
    
    /**
     * Returns the type's supertype if the type is derived from a supertype and the supertype can be found
     * on the project's ips object path. Returns <code>null</code> if either this type is not derived from 
     * a supertype or the supertype can't be found on the project's ips object path. 
     * 
     * @param ipsProject The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     *
     * @throws CoreException if an error occurs while searching for the supertype.
     */
    public IType findSupertype(IIpsProject ipsProject) throws CoreException;
    
    /**
     * Sets the type's supertype.
     * 
     * @throws IllegalArgumentException if newSupertype is null.
     */
    public void setSupertype(String newSupertype);
    
    /**
     * Returns <code>true</code> if this type is a subtype of the given supertype candidate,
     * returns <code>false</code> otherwise. Returns <code>false</code> if supertype candidate
     * is <code>null</code>.
     * 
     * @param supertypeCandidate The type which is the possibly a supertype of this type
     * @param ipsProject The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     * 
     * @throws CoreException if an error occurs while searching the type hierarchy.
     */
    public boolean isSubtypeOf(IType supertypeCandidate, IIpsProject ipsProject) throws CoreException;
    
    /**
     * Returns <code>true</code> if this type is a subtype of the given candidate, or if the
     * candidate is this same. Returns <code>false</code> otherwise. 
     * Returns <code>false</code> if candidate is <code>null</code>.
     * 
     * @param supertypeCandidate The type which is the possibly a supertype of this type
     * @param ipsProject The project which ips object path is used for the searched.
     * This is not neccessarily the project this type is part of. 
     * 
     * @throws CoreException if an error occurs while searching the type hierarchy.
     */
    public boolean isSubtypeOrSameType(IType candidate, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the type's attributes.
     */
    public IAttribute[] getAttributes();
    
    /**
     * Returns this type's attributes and the attributes within the supertype hierarchy. It considers overridden attributes. 
     * That means if an attribute has been overridden by a subtype this attribute instance will be in the returned array
     * all attributes in the supertype hierarchy with the same name will be neglected.
     *  
     * @throws CoreException if an exception occurs while collecting the attributes 
     */
    public IAttribute[] findAllAttributes() throws CoreException;

    /**
     * Returns the attribute with the given name defined in <strong>this</strong> type
     * (This method does not search the supertype hierarchy.)
     * If more than one attribute with the name exist, the first attribute with the name is returned.
     * Returns <code>null</code> if no attribute with the given name exists.
     */
    public IAttribute getAttribute(String name);

    /**
     * Searches an attribute with the given name in the type and its supertype hierarchy and returns it. 
     * Returns <code>null</code> if no such attribute exists.
     * 
     * @param name          The attribute's name.
     * @param ipsProject    The project which ips object path is used for the searched.
     *                      This is not neccessarily the project this type is part of. 
     * 
     * @throws NullPointerException if project is <code>null</code>.
     * @throws CoreException if an error occurs while searching.
     */
    public IAttribute findAttribute(String name, IIpsProject ipsProject) throws CoreException;
    
    /**
     * Creates a new attribute and returns it.
     */
    public IAttribute newAttribute();

    /**
     * Returns the number of attributes.
     */
    public int getNumOfAttributes();
    
    /**
     * Moves the attributes identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first attribute), no attribute is moved up. 
     * If one of the indexes is the number of attributes - 1 (the last attribute)
     * no attribute is moved down. 
     * 
     * @param indexes   The indexes identifying the attributes.
     * @param up        <code>true</code>, to move the attributes up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved attributes.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * an attribute.
     */
    public int[] moveAttributes(int[] indexes, boolean up);
    
    /**
     * Returns the assocation with the given name defined in <strong>this</strong> type.
     * (This method does not search the supertype hierarchy.)
     * If more than one association with the name exist, the first one is returned.
     * Returns <code>null</code> if no association with the given name exists or name is <code>null</code>.
     */
    public IAssociation getAssociation(String name);
    
    /**
     * Searches an association with the given name in the type and it's supertype hierarchy and returns it. 
     * Returns <code>null</code> if no such assoiation exists.
     * 
     * @param name          The association's name.
     * @param ipsProject    The project which ips object path is used for the searched.
     *                      This is not neccessarily the project this type is part of. 
     * 
     * @throws CoreException if an error occurs while searching.
     */
    public IAssociation findAssociation(String name, IIpsProject ipsProject) throws CoreException;

    /**
     * Returns the type's associations.
     */
    public IAssociation[] getAssociations();

    /**
     * Returns all associations that have the indicated target. Returns an empty array if no such assoiation exists
     * or target is <code>null</code>.
     * <p>
     * Note that this does NOT search the supertype hierarchy.
     * 
     * @param target The qualified name of the target type.
     */
    public IAssociation[] getAssociationsForTarget(String target);

    /**
     * Creates a new association and returns it.
     */
    public IAssociation newAssociation();
    
    /**
     * Returns the number of associations.
     */
    public int getNumOfAssociations();
    
    /**
     * Moves the associations identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first association), no association is moved up. 
     * If one of the indexes is the number of associations - 1 (the last association)
     * no association is moved down. 
     * 
     * @param indexes   The indexes identifying the associations.
     * @param up        <code>true</code>, to move the associations up, 
     *                  <false> to move them down.
     * 
     * @return The new indexes of the moved associations.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * an association.
     */
    public int[] moveAssociations(int[] indexes, boolean up);
    
    /**
     * Returns the type's methods. 
     */
    public IMethod[] getMethods();
    
    /**
     * Creates a new method and returns it.
     */
    public IMethod newMethod();

    /**
     * Returns the number of methods.
     */
    public int getNumOfMethods();
    
    /**
     * Moves the methods identified by the indexes up or down by one position.
     * If one of the indexes is 0 (the first method), no method is moved up. 
     * If one of the indexes is the number of methods - 1 (the last method)
     * no method is moved down. 
     * 
     * @param indexes   The indexes identifying the methods.
     * @param up        <code>true</code>, to move the methods up, 
     * <false> to move them down.
     * 
     * @return The new indexes of the moved methods.
     * 
     * @throws NullPointerException if indexes is null.
     * @throws IndexOutOfBoundsException if one of the indexes does not identify
     * a method.
     */
    public int[] moveMethods(int[] indexes, boolean up);
    
    /**
     * Returns a list of methods defined in any of the type's supertypes
     * that can be overriden (and isn't overriden yet).
     * 
     * @param onlyAbstractMethods if true only abstract methods are returned.
     * @param ipsProject          The project which ips object path is used for the searched.
     *                            This is not neccessarily the project this type is part of. 
     */
    public IMethod[] findOverrideMethodCandidates(boolean onlyNotImplementedAbstractMethods, IIpsProject ipsProject) throws CoreException;
    
    /**
     * Creates new methods in this type that overrides the given methods.
     * Note that it is not checked, if the methods really belong to one of
     * the type's supertypes.
     */
    public IMethod[] overrideMethods(IMethod[] methods);
    
    /**
     * Returns true if this type has a same method as the indicated one.
     * Two methods are considered to be same when they have the same name,
     * the same number of parameters and the parameter's datatypes are equal. 
     */
    public boolean hasSameMethod(IMethod method);
    
    /**
     * Returns the method that matches the indicated one regarding it's signature. Two methods match if they have 
     * the same name, the same number of parameters and the parameter's datatypes are equal.
     * Returns <code>null</code> if the type does not contain a matching method or the indicated method is 
     * <code>null</code>. 
     */
    public IMethod getMatchingMethod(IMethod method);

    
}
