/*******************************************************************************
 * Copyright (c) 2005-2009 Faktor Zehn AG und andere.
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

package org.faktorips.devtools.core.model;

import java.io.Serializable;

import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsobject.QualifiedNameType;
import org.faktorips.util.ArgumentCheck;

/**
 * An implementation of the {@link IDependency} interface that describes a dependency between two
 * IpsObjects.
 * 
 * @author Peter Erzberger
 */
public class IpsObjectDependency implements IDependency, Serializable {

    private static final long serialVersionUID = -4763466997240470890L;

    private QualifiedNameType source;
    private QualifiedNameType target;
    private int hashCode;
    private DependencyType dependencyType;
    private transient IIpsObjectPartContainer part;
    private transient String propertyName;

    private IpsObjectDependency(QualifiedNameType source, IIpsObjectPartContainer part, String propertyName,
            QualifiedNameType target, DependencyType dependencyType) {
        super();
        ArgumentCheck.notNull(source, this);
        ArgumentCheck.notNull(target, this);
        ArgumentCheck.notNull(dependencyType, this);
        this.source = source;
        this.target = target;
        this.dependencyType = dependencyType;
        this.part = part;
        this.propertyName = propertyName;
        calculateHashCode();
    }

    /**
     * Creates a new Dependency between the specified source and target objects and defines if it is
     * a transitive dependency.
     */
    public final static IpsObjectDependency create(QualifiedNameType source,
            IIpsObjectPartContainer part,
            String propertyName,
            QualifiedNameType target,
            DependencyType dependencyType) {
        return new IpsObjectDependency(source, part, propertyName, target, dependencyType);
    }

    /**
     * Creates a new Dependency instance indicating an instance of dependency between the specified
     * source and target objects. A Dependency instance indicates that the source is subtype of the
     * target and hence the source depends on the target.
     */
    public final static IpsObjectDependency createSubtypeDependency(QualifiedNameType source,
            IIpsObjectPartContainer part,
            String propertyName,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, part, propertyName, target, DependencyType.SUBTYPE);
    }

    /**
     * Creates a new Dependency instance indicating referencing dependency between the specified
     * source and target objects. A Dependency instance indicates that the source references the
     * target and hence the source depends on the target.
     */
    public final static IpsObjectDependency createReferenceDependency(QualifiedNameType source,
            IIpsObjectPartContainer part,
            String propertyName,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, part, propertyName, target, DependencyType.REFERENCE);
    }

    /**
     * Creates a new Dependency instance indicating special referencing dependency of the kind
     * compostion master to detail between the specified source and target objects. A Dependency
     * instance indicates that the source references the target and hence the source depends on the
     * target.
     */
    public final static IpsObjectDependency createCompostionMasterDetailDependency(QualifiedNameType source,
            IIpsObjectPartContainer part,
            String propertyName,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, part, propertyName, target,
                DependencyType.REFERENCE_COMPOSITION_MASTER_DETAIL);
    }

    /**
     * Creates a new Dependency instance indicating an instance of dependency between the specified
     * source and target objects. A Dependency instance indicates that the source is an instance of
     * the target and hence the source depends on the target.
     */
    public final static IpsObjectDependency createInstanceOfDependency(QualifiedNameType source,
            IIpsObjectPartContainer part,
            String propertyName,
            QualifiedNameType target) {
        return new IpsObjectDependency(source, part, propertyName, target, DependencyType.INSTANCEOF);
    }

    /**
     * The source object
     */
    public QualifiedNameType getSource() {
        return source;
    }

    /**
     * The target object
     */
    public QualifiedNameType getTargetAsQNameType() {
        return target;
    }

    /**
     * The target object
     */
    public Object getTarget() {
        return target;
    }

    /**
     * {@inheritDoc}
     */
    public DependencyType getType() {
        return dependencyType;
    }

    public IIpsObjectPartContainer getPart() {
        return part;
    }

    public String getProperty() {
        return propertyName;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (!(o instanceof IpsObjectDependency)) {
            return false;
        }
        IpsObjectDependency other = (IpsObjectDependency)o;
        if (part != null) {
            if (!part.equals(other.part)) {
                return false;
            }
        } else if (other.part != null) {
            return false;
        }

        if (propertyName != null) {
            if (!propertyName.equals(other.propertyName)) {
                return false;
            }
        } else if (other.propertyName != null) {
            return false;
        }
        return dependencyType.equals(other.getType()) && target.equals(other.getTarget())
                && source.equals(other.getSource());
    }

    private void calculateHashCode() {
        int result = 17;
        result = result * 37 + target.hashCode();
        result = result * 37 + source.hashCode();
        result = result * 37 + dependencyType.hashCode();
        if (part != null) {
            result = result * 37 + part.hashCode();
        }
        if (propertyName != null) {
            result = result * 37 + propertyName.hashCode();
        }
        hashCode = result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        return hashCode;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "(" + source.toString() + " -> " + target.toString() + ", type: " + dependencyType + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

}
