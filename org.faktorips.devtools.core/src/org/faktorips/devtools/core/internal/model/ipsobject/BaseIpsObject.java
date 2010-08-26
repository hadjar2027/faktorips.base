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

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.ArrayList;
import java.util.List;

import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.w3c.dom.Element;

/**
 * Base class that allows to implement IPS object subclasses in a simple way. The handling of parts
 * of this object is done using IpsObjectPartCollections.
 * 
 * @see IpsObjectPartCollection
 * 
 * @since 2.0
 * 
 * @author Jan Ortmann
 */
public abstract class BaseIpsObject extends IpsObject {

    private final List<IpsObjectPartCollection<?>> partCollections = new ArrayList<IpsObjectPartCollection<?>>(1);

    protected BaseIpsObject(IIpsSrcFile file) {
        super(file);
    }

    protected final void addPartCollection(IpsObjectPartCollection<?> container) {
        partCollections.add(container);
    }

    @Override
    protected IIpsElement[] getChildrenThis() {
        List<IIpsElement> children = new ArrayList<IIpsElement>();
        for (IpsObjectPartCollection<?> container : partCollections) {
            int size = container.size();
            for (int i = 0; i < size; i++) {
                children.add(container.getPart(i));
            }
        }
        return children.toArray(new IIpsElement[children.size()]);
    }

    @Override
    protected IIpsObjectPart newPartThis(Element xmlTag, String id) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            IIpsObjectPart part = container.newPart(xmlTag, id);
            if (part != null) {
                return part;
            }
        }
        return null;
    }

    @Override
    protected IIpsObjectPart newPartThis(Class<? extends IIpsObjectPart> partType) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            IIpsObjectPart part = container.newPart(partType);
            if (part != null) {
                return part;
            }
        }
        return null;
    }

    @Override
    protected boolean addPartThis(IIpsObjectPart part) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            if (container.addPart(part)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected boolean removePartThis(IIpsObjectPart part) {
        for (IpsObjectPartCollection<?> container : partCollections) {
            if (container.removePart(part)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void reinitPartCollectionsThis() {
        for (IpsObjectPartCollection<?> container : partCollections) {
            container.clear();
        }
    }

}
