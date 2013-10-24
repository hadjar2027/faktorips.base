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

package org.faktorips.devtools.core.ui.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.ui.dialogs.SearchPattern;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.exception.CoreRuntimeException;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.ui.controls.contentproposal.ICachedContentProposalProvider;

/**
 * An <tt>IContentProposalProvider</tt> for <tt>Datatype</tt> proposal.
 * 
 * @author hbaagil
 */

public class DatatypeContentProposalProvider implements ICachedContentProposalProvider {

    private IIpsProject ipsProject;
    private Datatype[] dataType;

    private boolean includeVoid;
    private boolean valuetypesOnly;
    private boolean includePrimitives;
    private boolean includeAbstract;
    private List<Datatype> excludedDatatypes;

    private SearchPattern searchPattern = new SearchPattern();

    public DatatypeContentProposalProvider(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;

        includeVoid = false;
        valuetypesOnly = false;
        includePrimitives = true;
        includeAbstract = false;
        excludedDatatypes = null;
    }

    public void setIncludeVoid(boolean value) {
        includeVoid = value;
    }

    public boolean isIncludeVoid() {
        return includeVoid;
    }

    public void setIncludeAbstract(boolean includeAbstract) {
        this.includeAbstract = includeAbstract;
    }

    public boolean isIncludeAbstract() {
        return includeAbstract;
    }

    public void setValueDatatypesOnly(boolean value) {
        valuetypesOnly = value;
    }

    public boolean isValueDatatypesOnly() {
        return valuetypesOnly;
    }

    public boolean isIncludePrimitives() {
        return includePrimitives;
    }

    public void setIncludePrimitives(boolean includePrimitives) {
        this.includePrimitives = includePrimitives;
    }

    public void setExcludedDatatypes(List<Datatype> excludedDatatypes) {
        this.excludedDatatypes = excludedDatatypes;
    }

    public List<Datatype> getExcludedDatatypes() {
        if (excludedDatatypes != null) {
            return Collections.unmodifiableList(excludedDatatypes);
        }
        return null;
    }

    private Datatype[] getDataType() {
        return dataType;
    }

    private void setDataType(Datatype[] dataType) {
        this.dataType = dataType;
    }

    public void setIpsProject(IIpsProject ipsProject) {
        this.ipsProject = ipsProject;
    }

    @Override
    public IContentProposal[] getProposals(String contents, int position) {
        checkDataType();

        String prefix = StringUtils.left(contents, position);
        searchPattern.setPattern(prefix);
        List<IContentProposal> result = new ArrayList<IContentProposal>();
        for (Datatype type : getDataType()) {
            String unqualifiedName = type.getName();
            if (searchPattern.matches(unqualifiedName)) {
                DatatypeContentProposal contentProposal = new DatatypeContentProposal(type);
                result.add(contentProposal);
            }
        }
        return result.toArray(new IContentProposal[result.size()]);
    }

    private void checkDataType() {
        if (getDataType() == null) {
            setDataType(findDataType());
        }
    }

    private Datatype[] findDataType() {
        try {
            return ipsProject.findDatatypes(isValueDatatypesOnly(), isIncludeVoid(), isIncludePrimitives(),
                    getExcludedDatatypes(), isIncludeAbstract());
        } catch (CoreException e) {
            throw new CoreRuntimeException(e);
        }
    }

    @Override
    public void clearCache() {
        setDataType(null);
    }

}