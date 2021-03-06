/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.builder;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.faktorips.codegen.JavaCodeFragment;
import org.faktorips.devtools.core.builder.naming.JavaPackageStructure;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.ipsproject.IIpsSrcFolderEntry;
import org.faktorips.devtools.core.model.productcmpt.IExpression;
import org.faktorips.devtools.core.model.tablestructure.ITableAccessFunction;
import org.faktorips.devtools.core.util.QNameUtil;
import org.faktorips.fl.CompilationResult;
import org.faktorips.fl.ExprCompiler;
import org.faktorips.fl.IdentifierResolver;
import org.faktorips.runtime.internal.IpsStringUtils;

/**
 * A default implementation that extends the AbstractBuilderSet and implements the
 * IJavaPackageStructure interface. The getPackage() method provides package names for the kind
 * constants defined in this DefaultBuilderSet. This implementation uses the base package name for
 * generated java classes as the root of the package structure. The base package name can be
 * configure for an IPS project within the ipsproject.xml file. On top of the base package name it
 * adds the IPS package fragment name of the IpsSrcFile in question. Internal packages are
 * distinguished from packages that contain published interfaces and classes. It depends on the kind
 * constant if an internal or published package name is returned.
 */
public abstract class DefaultBuilderSet extends AbstractBuilderSet implements IJavaPackageStructure {

    private static final String PARENTHESIS_CHARACTER = "("; //$NON-NLS-1$

    private static final String SEMI_COLON_CHARACTER = ";"; //$NON-NLS-1$

    private JavaPackageStructure javaPackageStructure = new JavaPackageStructure();

    private List<String> additionalAnnotations = new ArrayList<String>();

    private List<String> additionalImports = new ArrayList<String>();

    private List<String> retainedAnnotations = new ArrayList<String>();

    @Override
    public IFile getRuntimeRepositoryTocFile(IIpsPackageFragmentRoot root) {
        if (root == null) {
            return null;
        }
        if (!root.isBasedOnSourceFolder()) {
            return null;
        }
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        String basePackInternal = javaPackageStructure.getBasePackageName(entry, true, false);
        IPath path = QNameUtil.toPath(basePackInternal);
        path = path.append(entry.getBasePackageRelativeTocPath());
        IFolder tocFileLocation = getTocFileLocation(root);
        if (tocFileLocation == null) {
            return null;
        }
        return tocFileLocation.getFile(path);
    }

    private IFolder getTocFileLocation(IIpsPackageFragmentRoot root) {
        IIpsSrcFolderEntry entry = (IIpsSrcFolderEntry)root.getIpsObjectPathEntry();
        return entry.getOutputFolderForDerivedJavaFiles();
    }

    @Override
    public String getRuntimeRepositoryTocResourceName(IIpsPackageFragmentRoot root) {
        IFile tocFile = getRuntimeRepositoryTocFile(root);
        if (tocFile == null) {
            return null;
        }
        IFolder tocFileLocation = getTocFileLocation(root);
        return tocFile.getFullPath().removeFirstSegments(tocFileLocation.getFullPath().segmentCount()).toString();
    }

    @Override
    public String getPackageName(IIpsSrcFile ipsSrcFile, boolean internalArtifacts, boolean mergableArtifacts) {
        return javaPackageStructure.getPackageName(ipsSrcFile, internalArtifacts, mergableArtifacts);
    }

    @Override
    public String getBasePackageName(IIpsSrcFolderEntry entry, boolean internalArtifacts, boolean mergableArtifacts) {
        return javaPackageStructure.getBasePackageName(entry, internalArtifacts, mergableArtifacts);
    }

    public abstract boolean isGeneratePublishedInterfaces();

    @Override
    public boolean isSupportTableAccess() {
        return false;
    }

    /**
     * Empty implementation. Might be overridden by subclasses that support the formula language.
     */
    @Override
    public CompilationResult<JavaCodeFragment> getTableAccessCode(String tableContentsQualifiedName,
            ITableAccessFunction fct,
            CompilationResult<JavaCodeFragment>[] argResults) throws CoreException {

        return null;
    }

    @Override
    public boolean isSupportFlIdentifierResolver() {
        return false;
    }

    @Override
    public IdentifierResolver<JavaCodeFragment> createFlIdentifierResolver(IExpression formula,
            ExprCompiler<JavaCodeFragment> exprCompiler) throws CoreException {
        return null;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Returns an empty string. This method is supposed to be overridden by subclasses.
     */
    @Override
    public String getVersion() {
        return ""; //$NON-NLS-1$
    }

    @Override
    public IPersistenceProvider getPersistenceProvider() {
        return null;
    }

    @Override
    public void beforeBuildProcess(int buildKind) throws CoreException {
        super.beforeBuildProcess(buildKind);
        initAdditionalImports();
        additionalAnnotations = initAdditionalAnnotations();
        retainedAnnotations = initRetainedAnnotations();
    }

    protected String getConfiguredAdditionalAnnotations() {
        return IpsStringUtils.EMPTY;
    }

    protected String getConfiguredRetainedAnnotations() {
        return IpsStringUtils.EMPTY;
    }

    private void initAdditionalImports() {
        additionalImports = new ArrayList<String>();
        // we only need the additional imports for additional annotations. The imports for retained
        // annotations will have been added by whomever created those annotations and are not removed by
        // jMerge.
        List<String> splitInput = splitString(getConfiguredAdditionalAnnotations());
        for (String splitString : splitInput) {
            String importStatement = removeParenthesis(splitString);
            if (!importStatement.equals(QNameUtil.getUnqualifiedName(importStatement))) {
                additionalImports.add(importStatement);
            }
        }
    }

    private List<String> splitString(String input) {
        List<String> splitInput = new ArrayList<String>();
        String[] split = input.split(SEMI_COLON_CHARACTER);
        for (String string : split) {
            splitInput.add(string.trim());
        }
        return splitInput;
    }

    private String removeParenthesis(String importWithParenthesis) {
        if (importWithParenthesis.contains(PARENTHESIS_CHARACTER)) {
            int bracket = importWithParenthesis.indexOf(PARENTHESIS_CHARACTER);
            return importWithParenthesis.substring(0, bracket);
        }
        return importWithParenthesis;
    }

    private List<String> initAdditionalAnnotations() {
        List<String> annotations = new LinkedList<String>();
        List<String> splitInput = splitString(getConfiguredAdditionalAnnotations());
        for (String splitString : splitInput) {
            int i = splitString.indexOf(PARENTHESIS_CHARACTER);
            if (i < 0) {
                i = splitString.length();
            }
            String unqualifiedName = QNameUtil.getUnqualifiedName(splitString.substring(0, i));
            annotations.add(unqualifiedName + splitString.substring(i));
        }
        return annotations;
    }

    private List<String> initRetainedAnnotations() {
        List<String> annotations = splitString(getConfiguredRetainedAnnotations());
        for (ListIterator<String> iterator = annotations.listIterator(); iterator.hasNext();) {
            String annotation = iterator.next();
            if (!annotation.startsWith("@")) { //$NON-NLS-1$
                iterator.set('@' + annotation);
            }
        }
        return annotations;
    }

    public List<String> getAdditionalImports() {
        return additionalImports;
    }

    public List<String> getAdditionalAnnotations() {
        return additionalAnnotations;
    }

    public List<String> getRetainedAnnotations() {
        return retainedAnnotations;
    }
}
