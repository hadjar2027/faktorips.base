/**
 * <copyright>
 *
 * Copyright (c) 2006 IBM Corporation and others. All rights reserved. This program and the
 * accompanying materials are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors: IBM - Initial API and implementation
 *
 * </copyright>
 *
 * $Id: JDOMJNode.java,v 1.5 2007/06/12 20:56:06 emerks Exp $
 */

package org.eclipse.emf.codegen.merge.java.facade.jdom;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;

import org.eclipse.emf.codegen.merge.java.facade.AbstractJNode;
import org.eclipse.emf.codegen.merge.java.facade.FacadeFlags;
import org.eclipse.emf.codegen.merge.java.facade.FacadeHelper;
import org.eclipse.emf.codegen.merge.java.facade.JNode;
import org.eclipse.jdt.core.jdom.IDOMNode;

/**
 * @since 2.2.0
 */
@SuppressWarnings({ "deprecation" })
public abstract class JDOMJNode extends AbstractJNode {
    private JDOMFacadeHelper facadeHelper;
    private IDOMNode wrappedObject;

    protected JDOMJNode(IDOMNode idomNode) {
        wrappedObject = idomNode;
    }

    @Override
    public void dispose() {
        facadeHelper = null;
        wrappedObject = null;
    }

    @Override
    public boolean isDisposed() {
        return wrappedObject == null;
    }

    @Override
    public JDOMFacadeHelper getFacadeHelper() {
        return facadeHelper;
    }

    @Override
    public void setFacadeHelper(FacadeHelper facadeHelper) {
        this.facadeHelper = (JDOMFacadeHelper)facadeHelper;
    }

    @Override
    protected IDOMNode getWrappedObject() {
        return wrappedObject;
    }

    @Override
    public String getName() {
        return getWrappedObject().getName();
    }

    @Override
    public void setName(String name) {
        getWrappedObject().setName(name);
    }

    @Override
    public int getFlags() {
        return FacadeFlags.DEFAULT;
    }

    @Override
    public void setFlags(int flags) {
        // Ignore.
    }

    @Override
    public String getContents() {
        return getWrappedObject().getContents();
    }

    @Override
    public JNode getParent() {
        return getFacadeHelper().convertToNode(getWrappedObject().getParent());
    }

    @Override
    public List<JNode> getChildren() {
        if (!isDisposed()) {
            List<JNode> children = new ArrayList<JNode>();
            for (Enumeration<?> e = getWrappedObject().getChildren(); e.hasMoreElements();) {
                IDOMNode node = (IDOMNode)e.nextElement();
                JNode jNode = getFacadeHelper().convertToNode(node);
                children.add(jNode);
            }
            return Collections.unmodifiableList(children);
        }
        return Collections.emptyList();
    }
}
