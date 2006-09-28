/*******************************************************************************
 * Copyright (c) 2005,2006 Faktor Zehn GmbH und andere.
 *
 * Alle Rechte vorbehalten.
 *
 * Dieses Programm und alle mitgelieferten Sachen (Dokumentationen, Beispiele,
 * Konfigurationen, etc.) duerfen nur unter den Bedingungen der 
 * Faktor-Zehn-Community Lizenzvereinbarung - Version 0.1 (vor Gruendung Community) 
 * genutzt werden, die Bestandteil der Auslieferung ist und auch unter
 *   http://www.faktorips.org/legal/cl-v01.html
 * eingesehen werden kann.
 *
 * Mitwirkende:
 *   Faktor Zehn GmbH - initial API and implementation - http://www.faktorzehn.de
 *
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model;

import java.util.Locale;
import java.util.StringTokenizer;

import org.apache.commons.lang.SystemUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.IpsPlugin;
import org.faktorips.devtools.core.internal.model.product.DateBasedProductCmptNamingStrategy;
import org.faktorips.devtools.core.internal.model.product.NoVersionIdProductCmptNamingStrategy;
import org.faktorips.devtools.core.model.IChangesOverTimeNamingConvention;
import org.faktorips.devtools.core.model.IIpsArtefactBuilderSet;
import org.faktorips.devtools.core.model.IIpsModel;
import org.faktorips.devtools.core.model.IIpsObjectPath;
import org.faktorips.devtools.core.model.IIpsProject;
import org.faktorips.devtools.core.model.IIpsProjectProperties;
import org.faktorips.devtools.core.model.product.IProductCmptNamingStrategy;
import org.faktorips.devtools.core.util.XmlUtil;
import org.faktorips.util.ArgumentCheck;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Comment;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * An ips project's properties. The project can't keep the properties on it's own, as it is
 * a handle.
 * 
 * @author Jan Ortmann
 */
public class IpsProjectProperties implements IIpsProjectProperties {

	public final static IpsProjectProperties createFromXml(IpsProject ipsProject, Element element) {
		IpsProjectProperties data = new IpsProjectProperties();
		data.initFromXml(ipsProject, element);
		return data;
	}
	
	final static String TAG_NAME = "IpsProject"; //$NON-NLS-1$
	final static String GENERATED_CODE_TAG_NAME = "GeneratedSourcecode";  //$NON-NLS-1$
		
	private boolean createdFromParsableFileContents = true;
	
	private boolean modelProject;
	private boolean productDefinitionProject;
	private Locale javaSrcLanguage = Locale.ENGLISH;
	private String changesInTimeConventionIdForGeneratedCode = IChangesOverTimeNamingConvention.VAA;
	private IProductCmptNamingStrategy productCmptNamingStrategy = new NoVersionIdProductCmptNamingStrategy();
	private String builderSetId = ""; //$NON-NLS-1$
	private IIpsObjectPath path = new IpsObjectPath();
	private String[] predefinedDatatypesUsed = new String[0];
    private DynamicValueDatatype[] definedDatatypes = new DynamicValueDatatype[0]; 
    private String runtimeIdPrefix = ""; //$NON-NLS-1$
    private boolean javaProjectContainsClassesForDynamicDatatypes = false;
    private boolean containerRelationIsImplementedRuleEnabled = true;

    /**
     * Default constructor.
     */
	public IpsProjectProperties() {
		super();
	}

    /**
     * Copy constructor.
     */
	public IpsProjectProperties(IIpsProject ipsProject, IpsProjectProperties props) {
		Document doc = IpsPlugin.getDefault().newDocumentBuilder().newDocument();
		Element el = props.toXml(doc);
		initFromXml(ipsProject, el);
		this.createdFromParsableFileContents = props.createdFromParsableFileContents;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public MessageList validate(IIpsProject ipsProject) throws CoreException {
		MessageList list = new MessageList();
		validateBuilderSetId(ipsProject, list);
		validateUsedPredefinedDatatype(ipsProject, list);
		for (int i = 0; i < definedDatatypes.length; i++) {
			list.add(definedDatatypes[i].validate());
		}
		return list;
	}
	
	private void validateUsedPredefinedDatatype(IIpsProject ipsProject, MessageList list) {
		IIpsModel model = ipsProject.getIpsModel();
		for (int i = 0; i < predefinedDatatypesUsed.length; i++) {
			if (!model.isPredefinedValueDatatype(predefinedDatatypesUsed[i])) {
				String text = NLS.bind(Messages.IpsProjectProperties_msgUnknownDatatype, predefinedDatatypesUsed[i]);
				Message msg = new Message(IIpsProjectProperties.MSGCODE_UNKNOWN_PREDEFINED_DATATYPE, text, Message.ERROR, this);
				list.add(msg);
			}
		}
	}
	
	private void validateBuilderSetId(IIpsProject ipsProject, MessageList list) {
		IIpsArtefactBuilderSet[] sets = ipsProject.getIpsModel().getAvailableArtefactBuilderSets();
		for (int i = 0; i < sets.length; i++) {
			if (sets[i].getId().equals(builderSetId)) {
				return;
			}
		}
		String text = Messages.IpsProjectProperties_msgUnknownBuilderSetId + builderSetId;
		Message msg = new Message(IIpsProjectProperties.MSGCODE_UNKNOWN_BUILDER_SET_ID, text, Message.ERROR, this, IIpsProjectProperties.PROPERTY_BUILDER_SET_ID);
		list.add(msg);
	}
	
	/**
	 * Returns <code>true</code> if this property object was created by reading a  
	 * .ipsproject file containg parsable xml data, otherwise <code>false</code>.
	 */
	public boolean isCreatedFromParsableFileContents() {
		return createdFromParsableFileContents;
	}

	/**
	 * Sets if if this property object was created by reading a .ipsproject file 
	 * containg parsable xml data, or not.
	 */
	public void setCreatedFromParsableFileContents(boolean flag) {
		this.createdFromParsableFileContents = flag;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getBuilderSetId() {
		return builderSetId;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setBuilderSetId(String id) {
		ArgumentCheck.notNull(id);
		builderSetId = id;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IIpsObjectPath getIpsObjectPath() {
		return path;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public boolean isModelProject() {
		return modelProject;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setModelProject(boolean modelProject) {
		this.modelProject = modelProject;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isProductDefinitionProject() {
		return productDefinitionProject;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setProductDefinitionProject(boolean productDefinitionProject) {
		this.productDefinitionProject = productDefinitionProject;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public Locale getJavaSrcLanguage() {
		return javaSrcLanguage;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setJavaSrcLanguage(Locale javaSrcLanguage) {
		this.javaSrcLanguage = javaSrcLanguage;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public IProductCmptNamingStrategy getProductCmptNamingStrategy() {
		return productCmptNamingStrategy;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setProductCmptNamingStrategy(IProductCmptNamingStrategy newStrategy) {
		ArgumentCheck.notNull(newStrategy);
		productCmptNamingStrategy = newStrategy;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public void setChangesOverTimeNamingConventionIdForGeneratedCode(
			String changesInTimeConventionIdForGeneratedCode) {
		this.changesInTimeConventionIdForGeneratedCode = changesInTimeConventionIdForGeneratedCode;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getChangesOverTimeNamingConventionIdForGeneratedCode() {
		return changesInTimeConventionIdForGeneratedCode;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIpsObjectPath(IIpsObjectPath path) {
		ArgumentCheck.notNull(path);
		this.path = path;
	}
	
    /**
	 * {@inheritDoc}
	 */
	public String[] getPredefinedDatatypesUsed() {
		return predefinedDatatypesUsed;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setPredefinedDatatypesUsed(String[] datatypes) {
		ArgumentCheck.notNull(datatypes);
		this.predefinedDatatypesUsed = datatypes;
	}
	
    /**
	 * {@inheritDoc}
	 */
	public void setPredefinedDatatypesUsed(ValueDatatype[] datatypes) {
		ArgumentCheck.notNull(datatypes);
		predefinedDatatypesUsed = new String[datatypes.length];
		for (int i = 0; i < datatypes.length; i++) {
			predefinedDatatypesUsed[i] = datatypes[i].getQualifiedName();
		}
	}

	/**
	 * {@inheritDoc}
	 */
    public DynamicValueDatatype[] getDefinedDatatypes() {
        return definedDatatypes;
    }
    
    /**
	 * {@inheritDoc}
	 */
    public void setDefinedDatatypes(DynamicValueDatatype[] datatypes) {
        definedDatatypes = datatypes;
    }

	public Element toXml(Document doc) {
        createIpsProjectDescriptionComment(doc);
		Element projectEl = doc.createElement(TAG_NAME);
		projectEl.setAttribute("modelProject", "" + modelProject); //$NON-NLS-1$ //$NON-NLS-2$
		projectEl.setAttribute("productDefinitionProject", "" + productDefinitionProject); //$NON-NLS-1$ //$NON-NLS-2$
		projectEl.setAttribute("runtimeIdPrefix", runtimeIdPrefix); //$NON-NLS-1$
		projectEl.setAttribute("javaProjectContainsClassesForDynamicDatatypes", "" + javaProjectContainsClassesForDynamicDatatypes); //$NON-NLS-1$ //$NON-NLS-2$
		projectEl.setAttribute("containerRelationIsImplementedRuleEnabled", "" + containerRelationIsImplementedRuleEnabled);
        
        // artefact builder set
        createIpsArtefactBuilderSetDescriptionComment(projectEl);
        Element builderSetEl = doc.createElement(IIpsArtefactBuilderSet.XML_ELEMENT);
        projectEl.appendChild(builderSetEl);
        builderSetEl.setAttribute("id", builderSetId); //$NON-NLS-1$
        
        // generated sourcecode
        createGeneratedSourcecodeDescriptionComment(projectEl);
        Element generatedCodeEl = doc.createElement(GENERATED_CODE_TAG_NAME);
		projectEl.appendChild(generatedCodeEl);
        generatedCodeEl.setAttribute("docLanguage", javaSrcLanguage.toString()); //$NON-NLS-1$
		generatedCodeEl.setAttribute("changesInTimeNamingConvention", changesInTimeConventionIdForGeneratedCode); //$NON-NLS-1$

		// naming strategy
        createProductCmptNamingStrategyDescriptionComment(projectEl);
        projectEl.appendChild(productCmptNamingStrategy.toXml(doc));
        
		// object path
        createDescriptionComment(IpsObjectPath.getXmlFormatDescription(), projectEl);
		projectEl.appendChild(((IpsObjectPath)path).toXml(doc));
		
		// datatypes
        createDatatypeDescriptionComment(projectEl);
		Element datatypesEl = doc.createElement("Datatypes"); //$NON-NLS-1$
		projectEl.appendChild(datatypesEl);
        Element predefinedTypesEl = doc.createElement("UsedPredefinedDatatypes"); //$NON-NLS-1$
        datatypesEl.appendChild(predefinedTypesEl);
		for (int i = 0;  i < predefinedDatatypesUsed.length; i++) {
			Element datatypeEl = doc.createElement("Datatype"); //$NON-NLS-1$
			datatypeEl.setAttribute("id", predefinedDatatypesUsed[i]); //$NON-NLS-1$
            predefinedTypesEl.appendChild(datatypeEl);
		}
		Element definedDatatypesEl = doc.createElement("DatatypeDefinitions"); //$NON-NLS-1$
		datatypesEl.appendChild(definedDatatypesEl);
		writeDefinedDataTypesToXML(doc, definedDatatypesEl);
		
		return projectEl;
	}
	
	public void initFromXml(IIpsProject ipsProject, Element element) {
        modelProject = Boolean.valueOf(element.getAttribute("modelProject")).booleanValue(); //$NON-NLS-1$
        productDefinitionProject = Boolean.valueOf(element.getAttribute("productDefinitionProject")).booleanValue(); //$NON-NLS-1$
        runtimeIdPrefix = element.getAttribute("runtimeIdPrefix"); //$NON-NLS-1$
        javaProjectContainsClassesForDynamicDatatypes = Boolean.valueOf(element.getAttribute("javaProjectContainsClassesForDynamicDatatypes")).booleanValue();  //$NON-NLS-1$
        containerRelationIsImplementedRuleEnabled = Boolean.valueOf(element.getAttribute("containerRelationIsImplementedRuleEnabled")).booleanValue();  //$NON-NLS-1$
        Element generatedCodeEl = XmlUtil.getFirstElement(element, GENERATED_CODE_TAG_NAME);
        if (generatedCodeEl!=null) {
    	    javaSrcLanguage = getLocale(generatedCodeEl.getAttribute("docLanguage")); //$NON-NLS-1$
    	    changesInTimeConventionIdForGeneratedCode = generatedCodeEl.getAttribute("changesInTimeNamingConvention"); //$NON-NLS-1$
        } else {
        	javaSrcLanguage = Locale.ENGLISH;
        	changesInTimeConventionIdForGeneratedCode = IChangesOverTimeNamingConvention.VAA;
        }
        Element artefactEl = XmlUtil.getFirstElement(element, IIpsArtefactBuilderSet.XML_ELEMENT);
        if(artefactEl != null) {
            builderSetId = artefactEl.getAttribute("id"); //$NON-NLS-1$
        } else {
        	builderSetId = ""; //$NON-NLS-1$
        }
        initProductCmptNamingStrategyFromXml(ipsProject, XmlUtil.getFirstElement(element, IProductCmptNamingStrategy.XML_TAG_NAME));
        if(artefactEl != null) {
            builderSetId = artefactEl.getAttribute("id"); //$NON-NLS-1$
        } else {
        	builderSetId = ""; //$NON-NLS-1$
        }
        Element pathEl = XmlUtil.getFirstElement(element, IpsObjectPath.XML_TAG_NAME);
        if (pathEl != null) {
            path = IpsObjectPath.createFromXml(ipsProject, pathEl);
        } else {
        	path = new IpsObjectPath();
        }
        Element datatypesEl = XmlUtil.getFirstElement(element, "Datatypes"); //$NON-NLS-1$
        if (datatypesEl==null) {
        	predefinedDatatypesUsed = new String[0];
            definedDatatypes = new DynamicValueDatatype[0];
        	return;
        }
        initUsedPredefinedDatatypesFromXml(XmlUtil.getFirstElement(datatypesEl, "UsedPredefinedDatatypes")); //$NON-NLS-1$
        initDefinedDatatypesFromXml(ipsProject, XmlUtil.getFirstElement(datatypesEl, "DatatypeDefinitions")); //$NON-NLS-1$
        
        
	}
	
	private void initProductCmptNamingStrategyFromXml(IIpsProject ipsProject, Element el) {
		productCmptNamingStrategy = new NoVersionIdProductCmptNamingStrategy();
		if (el!=null) {
        	String id = el.getAttribute("id"); //$NON-NLS-1$
        	if (id.equals(DateBasedProductCmptNamingStrategy.EXTENSION_ID)) {
        		productCmptNamingStrategy = new DateBasedProductCmptNamingStrategy();
        	}
        	productCmptNamingStrategy.setIpsProject(ipsProject);
    		productCmptNamingStrategy.initFromXml(el);
		}
	}

    private void initUsedPredefinedDatatypesFromXml(Element element) {
        if (element==null) {
            predefinedDatatypesUsed = new String[0];
            return;
        }
        NodeList nl = element.getElementsByTagName("Datatype"); //$NON-NLS-1$
        predefinedDatatypesUsed = new String[nl.getLength()];
        for (int i=0; i<nl.getLength(); i++) {
            predefinedDatatypesUsed[i] = ((Element)nl.item(i)).getAttribute("id"); //$NON-NLS-1$
        }
    }
    
    private void initDefinedDatatypesFromXml(IIpsProject ipsProject, Element element) {
        if (element==null) {
            definedDatatypes = new DynamicValueDatatype[0];
            return;
        }
        NodeList nl = element.getElementsByTagName("Datatype"); //$NON-NLS-1$
        definedDatatypes = new DynamicValueDatatype[nl.getLength()];
        for (int i=0; i<nl.getLength(); i++) {
            definedDatatypes[i] = DynamicValueDatatype.createFromXml(ipsProject, (Element)nl.item(i));
        }
    }
    
    private void writeDefinedDataTypesToXML(Document doc, Element parent) {
		for (int i=0; i<definedDatatypes.length; i++) {
			Element datatypeEl = doc.createElement("Datatype"); //$NON-NLS-1$
			definedDatatypes[i].writeToXml(datatypeEl);
			parent.appendChild(datatypeEl);
		}
	}

    public static Locale getLocale(String s) {
    	StringTokenizer tokenzier = new StringTokenizer(s, "_"); //$NON-NLS-1$
    	if (!tokenzier.hasMoreTokens()) {
    		return Locale.ENGLISH;
    	}
    	String language = tokenzier.nextToken();
    	if (!tokenzier.hasMoreTokens()) {
    		return new Locale(language);
    	}
    	String country = tokenzier.nextToken();
    	if (!tokenzier.hasMoreTokens()) {
    		return new Locale(language, country);
    	}
    	String variant = tokenzier.nextToken();
    	return new Locale(language, country, variant);
    }

    /**
     * {@inheritDoc}
     */
	public void addDefinedDatatype(DynamicValueDatatype newDatatype) {
		DynamicValueDatatype [] oldValue = definedDatatypes;
		int i;
		/* replace, if Datatype already registered */
		for (i = 0; i < definedDatatypes.length; i++) {
			if(definedDatatypes[i].getQualifiedName()!=null && 
					newDatatype.getQualifiedName()!=null && 
					definedDatatypes[i].getQualifiedName().equals(newDatatype.getQualifiedName())) {
				definedDatatypes[i] = newDatatype;
				return;
			}
		}
		definedDatatypes = new DynamicValueDatatype [oldValue.length + 1];
		for (i = 0; i < oldValue.length; i++) {
			definedDatatypes[i]=oldValue[i];
		}
		definedDatatypes[i]=newDatatype;
	}
	
	/**
	 * {@inheritDoc}
	 */
	public String getRuntimeIdPrefix() {
		return this.runtimeIdPrefix;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setRuntimeIdPrefix(String runtimeIdPrefix) {
		if (runtimeIdPrefix == null) {
			throw new NullPointerException("RuntimeIdPrefix can not be null"); //$NON-NLS-1$
		}
		this.runtimeIdPrefix = runtimeIdPrefix;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isJavaProjectContainsClassesForDynamicDatatypes() {
		return javaProjectContainsClassesForDynamicDatatypes;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setJavaProjectContainsClassesForDynamicDatatypes(
			boolean newValue) {
		this.javaProjectContainsClassesForDynamicDatatypes = newValue;
	}
    
    /**
     * {@inheritDoc}
     */
    public boolean isContainerRelationIsImplementedRuleEnabled() {
        return containerRelationIsImplementedRuleEnabled;
    }

    /**
     * {@inheritDoc}
     */
    public void setContainerRelationIsImplementedRuleEnabled(boolean enabled) {
        containerRelationIsImplementedRuleEnabled = enabled;
    }

    private void createIpsProjectDescriptionComment(Node parentEl) {
        String s = "This xml file contains the properties of the enclosing ips project. It contains the following information:" + SystemUtils.LINE_SEPARATOR 
        + " " + SystemUtils.LINE_SEPARATOR
        + "The generator used to transform the model to Java sourcecode and the product definition into the runtime format." + SystemUtils.LINE_SEPARATOR
        + "The path where to search for model and product definition files. This is basically the same concept as the  Java classpath." + SystemUtils.LINE_SEPARATOR
        + "A strategy that defines how to name product components and what names are valid." + SystemUtils.LINE_SEPARATOR
        + "The datatypes that can be used in the model. Datatypes used in the model fall into two categeories:" + SystemUtils.LINE_SEPARATOR
        + " * Predefined datatype" + SystemUtils.LINE_SEPARATOR
        + "   Predefined datatypes are defined by the datatype definition extension. FaktorIPS predefines datatypes for" + SystemUtils.LINE_SEPARATOR
        + "   the standard Java classes like Boolean, String, Integer, etc. and some additionals, for example Money." + SystemUtils.LINE_SEPARATOR
        + "   You can add you own datatype be providing an extension and then use it from every ips project." + SystemUtils.LINE_SEPARATOR
        + " * User defined datatype (or dynamic datatype)" + SystemUtils.LINE_SEPARATOR
        + "   If you want to use a Java class that represents a value as datatype, but do not want to provide an exension for it," + SystemUtils.LINE_SEPARATOR
        + "   you can register this class as datatype in this file. See the details in the description of the datatype section " + SystemUtils.LINE_SEPARATOR
        + "   below how to register the class. Naturallyt the class must be availabl via the project's Java classpath." + SystemUtils.LINE_SEPARATOR
        + "   There you have different options. It is strongly recommended to provide the class via a Jar file or in a separate Java" + SystemUtils.LINE_SEPARATOR
        + "   project. However cou can also implement the class in this project itself. In this case you have to set the " + SystemUtils.LINE_SEPARATOR
        + "   javaProjectContainsClassesForDynamicDatatypes property to true so that FaktorIPS also looks in this project " + SystemUtils.LINE_SEPARATOR
        + "   for the class. The disadvantage of this approach is that a clean build won't work properly. At the beginning" + SystemUtils.LINE_SEPARATOR
        + "   of the clean build the Java class is deleted, then FaktorIPS checks the model, doesn't find the class and reports" + SystemUtils.LINE_SEPARATOR
        + "   problems." + SystemUtils.LINE_SEPARATOR
        + " " + SystemUtils.LINE_SEPARATOR
        + "<IpsProject>" + SystemUtils.LINE_SEPARATOR
        + "    productDefinitionProject                           True if this project contains elements of the product definition." + SystemUtils.LINE_SEPARATOR
        + "    modelProject                                       True if this project contains the model or part of it." + SystemUtils.LINE_SEPARATOR
        + "    runtimeIdPrefix                                    " + SystemUtils.LINE_SEPARATOR
        + "    javaProjectContainsClassesForDynamicDatatypes      see discussion above" + SystemUtils.LINE_SEPARATOR
        + "    containerRelationIsImplementedRuleEnabled          True if FaktorIPS checks if all container relations are implemented in none abstract classes." + SystemUtils.LINE_SEPARATOR
        + "    <IpsArtefactBuilderSet/>                           The generator used. Details below." + SystemUtils.LINE_SEPARATOR
        + "    <GeneratedSourcecode/>                             See details below." + SystemUtils.LINE_SEPARATOR
        + "    <IpsObjectPath/>                                   The object path to search for model and product definition objects. Details below." + SystemUtils.LINE_SEPARATOR
        + "    <ProductCmptNamingStrategy/>                       The strategy used for product component names. Details below." + SystemUtils.LINE_SEPARATOR
        + "    </Datatypes>                                       The datatypes used in the model. Details below." + SystemUtils.LINE_SEPARATOR
        + "</IpsProject>" + SystemUtils.LINE_SEPARATOR;
        createDescriptionComment(s, parentEl, "    ");
    }
    
    private void createGeneratedSourcecodeDescriptionComment(Element parentEl) {
        String s = "GeneratedSourcecode" + SystemUtils.LINE_SEPARATOR 
        + " " + SystemUtils.LINE_SEPARATOR
        + "<GeneratedSourcecode>" + SystemUtils.LINE_SEPARATOR
        + "    docLanguage=\"en\"                      Language in that the sourcecode and the Javadoc is generated." + SystemUtils.LINE_SEPARATOR
        + "                                          Currently English (en) and German (de) are supported." + SystemUtils.LINE_SEPARATOR
        + "    changesInTimeNamingConvention=\"VAA\"   Naming convention used for product changes over time. " + SystemUtils.LINE_SEPARATOR
        + "                                          Currently we support the German VAA standard (Version, Generation)" + SystemUtils.LINE_SEPARATOR
        + "                                          and the Product-Manager convention (Generation, Anpassungsstufe)." + SystemUtils.LINE_SEPARATOR
        + "                                          Both naming conventions are available in English and German." + SystemUtils.LINE_SEPARATOR
        + "</GeneratedSourcecode>" + SystemUtils.LINE_SEPARATOR;
        createDescriptionComment(s, parentEl);
    }
    
    private void createProductCmptNamingStrategyDescriptionComment(Element parentEl) {
        String s = "Product Component Naming Strategy" + SystemUtils.LINE_SEPARATOR 
        + " " + SystemUtils.LINE_SEPARATOR
        + "The naming strategy defines the structure of product component names and how characters that are not allowed" + SystemUtils.LINE_SEPARATOR
        + "in Java identifiers are replaced by the code generator. In order to deal with different versions of " + SystemUtils.LINE_SEPARATOR
        + "a product you need a strategy to derive the version from the product component name. " + SystemUtils.LINE_SEPARATOR
        + " " + SystemUtils.LINE_SEPARATOR
        + "Currently FaktorIPS includes the following strategy:" + SystemUtils.LINE_SEPARATOR
        + " * DateBasedProductCmptNamingStrategy" + SystemUtils.LINE_SEPARATOR
        + "   The product component name is made up of a \"unversioned\" name and a date format for the version id." + SystemUtils.LINE_SEPARATOR
        + "   <ProductCmptNamingStrategy id=\"org.faktorips.devtools.core.DateBasedProductCmptNamingStrategy\">" + SystemUtils.LINE_SEPARATOR
        + "       <DateBasedProductCmptNamingStrategy " + SystemUtils.LINE_SEPARATOR
        + "           dateFormatPattern=\"yyyy-MM\"                           Format of the version id according to java.text.DateFormat" + SystemUtils.LINE_SEPARATOR
        + "           postfixAllowed=\"true\"                                 True if the date format can be followed by an optional postfix. " + SystemUtils.LINE_SEPARATOR
        + "           versionIdSeparator=\" \">                               The separator between \"unversioned name\" and version id." + SystemUtils.LINE_SEPARATOR
        + "           <JavaIdentifierCharReplacements>                      Definition replacements for charcacters invalid in Java identifiers." + SystemUtils.LINE_SEPARATOR
        + "               <Replacement replacedChar=\" \" replacement=\"___\"/> Example: Replace Blank with three underscores" + SystemUtils.LINE_SEPARATOR
        + "               <Replacement replacedChar=\"-\" replacement=\"__\"/>  Example: Replace Hyphen with two underscores" + SystemUtils.LINE_SEPARATOR
        + "           </JavaIdentifierCharReplacements>" + SystemUtils.LINE_SEPARATOR
        + "       </DateBasedProductCmptNamingStrategy>" + SystemUtils.LINE_SEPARATOR
        + "    </ProductCmptNamingStrategy>" + SystemUtils.LINE_SEPARATOR;
        createDescriptionComment(s, parentEl);
    }
    
    private void createDatatypeDescriptionComment(Node parentEl) {
        String s = "Datatypes" + SystemUtils.LINE_SEPARATOR 
        + " " + SystemUtils.LINE_SEPARATOR
        + "In the datatypes section the value datatypes allowed in the model are defined." + SystemUtils.LINE_SEPARATOR
        + "See also the discussion at the top this file." + SystemUtils.LINE_SEPARATOR
        + " " + SystemUtils.LINE_SEPARATOR
        + "<UsedPredefinedDatatypes>" + SystemUtils.LINE_SEPARATOR
        + "    <Datatype id=\"Money\"\\>                                 The id of the datatype that should be used." + SystemUtils.LINE_SEPARATOR
        + "</UsedPredefinedDatatypes>" + SystemUtils.LINE_SEPARATOR
        + " " + SystemUtils.LINE_SEPARATOR
        + "<DatatypeDefinitions>" + SystemUtils.LINE_SEPARATOR
        + "    <Datatype id=\"PaymentMode\"                             The datatype's id used in the model to refer to it." + SystemUtils.LINE_SEPARATOR
        + "        valueClass=\"org.faktorips.sample.PaymentMode\"      The Java class the datatype represents" + SystemUtils.LINE_SEPARATOR
        + "        isEnumType=\"true\"                                  True if this is an enumeration of values." + SystemUtils.LINE_SEPARATOR
        + "        valueOfMethod=\"getPaymentMode\"                     Name of the method that takes a String a returns an object instance." + SystemUtils.LINE_SEPARATOR
        + "        isParsableMethod=\"isPaymentMode\"                   Name of the method that evaluates if a given string can be parsed to an instance." + SystemUtils.LINE_SEPARATOR
        + "        valueToStringMethod=\"toString\"                     Name of the method that transforms an object instance to a String (that can be parsed via the valueOfMethod)" + SystemUtils.LINE_SEPARATOR
        + "        getAllValuesMethod=\"getAllPaymentModes\"            For enums only: The name of the method that returns all values" + SystemUtils.LINE_SEPARATOR
        + "        isSupportingNames=\"true\"                           For enums only: True indicates that a string representation for the user other than the one defined by the valueToStringMethod exists." + SystemUtils.LINE_SEPARATOR
        + "        getNameMethod=\"getName\">                           For enums only: The name of the method that returns the string representation for the user, if isSupportingNames=true" + SystemUtils.LINE_SEPARATOR
        + "        <NullObjectId isNull=\"false\">n</NullObjectId>      Marks a value as a NullObject. This has to be used, if the Java class implements the null object pattern, " + SystemUtils.LINE_SEPARATOR
        + "                                                           otherwise omitt this element. The element's text defines the null object's id. Calling the valueOfMethod " + SystemUtils.LINE_SEPARATOR
        + "                                                           with this name must return the null object instance. If the null object's id is null, leave the text empty" + SystemUtils.LINE_SEPARATOR
        + "                                                           and set the isNull attribute to true." + SystemUtils.LINE_SEPARATOR
        + "    </Datatype>" + SystemUtils.LINE_SEPARATOR
        + "</DatatypeDefinitions>" + SystemUtils.LINE_SEPARATOR;
        createDescriptionComment(s, parentEl);
    }
	
    private void createIpsArtefactBuilderSetDescriptionComment(Node parentEl) {
        String s = "Artefact builder set" + SystemUtils.LINE_SEPARATOR 
        + " " + SystemUtils.LINE_SEPARATOR
        + "In this section the artefact builder set (code generator) is defined." + SystemUtils.LINE_SEPARATOR
        + "FaktorIPS comes with a standard builder set. However the build / generator mechanism is completly decoupled " + SystemUtils.LINE_SEPARATOR
        + "from the modeling and product definition capabilities and you can write your own builder/generators." + SystemUtils.LINE_SEPARATOR
        + "A different builder set is defined by providing an extension for the extension point" + SystemUtils.LINE_SEPARATOR
        + "\"org.faktorips.devtools.core.artefactbuilderset\" defined by FaktorIPS" + SystemUtils.LINE_SEPARATOR
        + " " + SystemUtils.LINE_SEPARATOR
        + "<IpsArtefactBuilderSet id=\"org.faktorips.devtools.stdbuilder.ipsstdbuilderset\"/>" + SystemUtils.LINE_SEPARATOR;
        createDescriptionComment(s, parentEl);
    }
    
	private void createDescriptionComment(String text, Node parent) {
        createDescriptionComment(text, parent, "        ");
    }
    
    private void createDescriptionComment(String text, Node parent, String indentation) {
        StringBuffer indentedText = new StringBuffer();
        indentedText.append(SystemUtils.LINE_SEPARATOR);
        StringTokenizer tokenizer = new StringTokenizer(text, SystemUtils.LINE_SEPARATOR);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();
            indentedText.append(indentation);
            indentedText.append(token);
            indentedText.append(SystemUtils.LINE_SEPARATOR);
        }
        indentedText.append(indentation.substring(4));
        Document doc = parent.getOwnerDocument();
        if (doc==null) {
            doc = (Document)parent;
        }
        Comment comment = doc.createComment(indentedText.toString());
        parent.appendChild(comment);
    }
    
}
