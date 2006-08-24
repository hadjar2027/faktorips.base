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

package org.faktorips.devtools.extsystems;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.IPath;
import org.faktorips.datatype.Datatype;
import org.faktorips.devtools.core.model.tablecontents.ITableContents;
import org.faktorips.devtools.core.model.tablecontents.ITableContentsGeneration;
import org.faktorips.devtools.core.model.tablestructure.ITableStructure;
import org.faktorips.util.message.MessageList;

/**
 * @author Thorsten Guenther
 */
public abstract class AbstractExternalTableFormat {

	/**
	 * The human readable name of this external table format.
	 */
	private String name;

	/**
	 * The extension of the files this external table format usually uses. Can
	 * be the empty string, but not <code>null</code>.
	 */
	private String defaultExtension;

	/**
	 * Converter to be used if no other matches
	 */
	private final IValueConverter defaultValueConverter = new DefaultValueConverter();

	/**
	 * List of all converters this external table format is configured with.
	 */
	private List converter = new ArrayList();

	/**
	 * @return The human readable name of this external table format.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Set the (human readable) name of this external table format. This name
	 * might be used to identify this in the ui.
	 * 
	 * @param name
	 *            The name to use.
	 */
	public void setName(String name) {
		this.name = name;

		if (this.name == null) {
			this.name = "";
		}
	}

	/**
	 * Set the default extension to use if a proposal for the name of the file
	 * to export ist generated. If the file is, for example, an excel-file, the
	 * default-extension is ".xls" (note the included dot as first char).
	 * 
	 * @param extension
	 *            The new default-extension.
	 */
	public void setDefaultExtension(String extension) {
		this.defaultExtension = extension;

		if (this.defaultExtension == null) {
			defaultExtension = "";
		}
	}

	/**
	 * @return Returns the default extension used for the proposal of a filename
	 *         as export-target.
	 */
	public String getDefaultExtension() {
		return defaultExtension;
	}

	/**
	 * Add a converter to tranform external values to internal values (and vice
	 * versa).
	 * 
	 * @param converter
	 *            The additional converter.
	 */
	public void addValueConverter(IValueConverter converter) {
		this.converter.add(converter);
	}

	/**
	 * @param externalValue
	 *            The external representation of the value.
	 * @param datatype
	 *            The datatype for the given external value.
	 * @param messageList 
	 *            A list for messages to add if anything happens that should be 
	 *            reported to the user. If this list does not contains an error-message
	 *            before you call this method and do contain an error-message after the
	 *            call, the conversion failed.
	 *            
	 * @return A string representing the given external value which can be
	 *         parsed by the given datatype.
	 */
	public String getIpsValue(Object externalValue, Datatype datatype, MessageList messageList) {
		return getConverter(datatype).getIpsValue(externalValue, messageList);
	}

	/**
	 * @param ipsValue
	 *            The string-representation of a value.
	 * @param datatype
	 *            The datatype the given string is a value for.
	 * @param messageList 
	 *            A list for messages to add if anything happens that should be 
	 *            reported to the user. If this list does not contains an error-message
	 *            before you call this method and do contain an error-message after the
	 *            call, the conversion failed.
	 *            
	 * @return Returns the external representation for the given string
	 *         respecting the given datatype.
	 */
	public Object getExternalValue(String ipsValue, Datatype datatype, MessageList messageList) {
		return getConverter(datatype).getExternalDataValue(ipsValue, messageList);
	}

	private IValueConverter getConverter(Datatype datatype) {
		for (Iterator iter = converter.iterator(); iter.hasNext();) {
			IValueConverter valueConverter = (IValueConverter) iter.next();

			if (valueConverter.getSupportedDatatype().compareTo(datatype) == 0) {
				return valueConverter;
			}
		}
		return defaultValueConverter;
	}

	/**
	 * @param contents
	 *            The contents of the table to export.
	 * @param filename
	 *            The name of the file to export to. The file can exist allready
	 *            and might or might not be overwritten, the choice is up to the
	 *            runnable.
	 * @param nullRepresentationString
	 *            The string to use to replace <code>null</code>. This value
	 *            can be used for systems with no own <code>null</code>-representation
	 *            (MS-Excel, for example).
	 * @param list
	 *            A list for messages describing any problems occured during the
	 *            export. If no messages of severity ERROR are contained in this
	 *            list, the export is considered successfull.
	 *            
	 * @return Returns the runnable to use to export a table.
	 */
	public abstract IWorkspaceRunnable getExportTableOperation(
			ITableContents contents, IPath filename,
			String nullRepresentationString, MessageList list);

	/**
	 * @param structure
	 *            The structure for the imported table
	 * @param filename
	 *            The name of the file to import from. 
	 * @param targetGeneration
	 *            The generation to insert the data into.
	 * @param nullRepresentationString
	 *            The string to use to replace <code>null</code>. This value
	 *            can be used for systems with no own <code>null</code>-representation
	 *            (MS-Excel, for example).
	 * @param list
	 *            A list for messages describing any problems occured during the
	 *            import. If no messages of severity ERROR are contained in this
	 *            list, the import is considered successfull.
	 *            
	 * @return The runnable to use to import a table.
	 */
	public abstract IWorkspaceRunnable getImportTableOperation(
			ITableStructure structure, IPath filename,
			ITableContentsGeneration targetGeneration,
			String nullRepresentationString, MessageList list);

	/**
	 * @param source The identification of the resource to check (for example, a qualified filename).
	 * @return <code>true</code> if the given resource is a valid source for import, <code>false</code> otherwise.
	 */
	public abstract boolean isValidImportSource(String source);
	
	/**
	 * Implementation of IValueConverter to be used if no other converter was
	 * found.
	 * 
	 * @author Thorsten Guenther
	 */
	private class DefaultValueConverter implements IValueConverter {

		/**
		 * {@inheritDoc}
		 */
		public Class getSupportedClass() {
			return Object.class;
		}

		/**
		 * {@inheritDoc}
		 */
		public Datatype getSupportedDatatype() {
			return null;
		}

		/**
		 * {@inheritDoc}
		 */
		public String getIpsValue(Object externalDataValue, MessageList messageList) {
			return externalDataValue.toString();
		}

		/**
		 * {@inheritDoc}
		 */
		public Object getExternalDataValue(String ipsValue, MessageList messageList) {
			return ipsValue;
		}

	}
}
