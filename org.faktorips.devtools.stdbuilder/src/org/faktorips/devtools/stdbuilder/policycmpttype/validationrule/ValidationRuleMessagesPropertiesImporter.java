/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.stdbuilder.policycmpttype.validationrule;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.faktorips.devtools.core.model.ipsobject.IIpsSrcFile;
import org.faktorips.devtools.core.model.ipsobject.IpsObjectType;
import org.faktorips.devtools.core.model.ipsproject.IIpsPackageFragmentRoot;
import org.faktorips.devtools.core.model.pctype.IPolicyCmptType;
import org.faktorips.devtools.core.model.pctype.IValidationRule;
import org.faktorips.devtools.stdbuilder.StdBuilderPlugin;
import org.faktorips.util.IoUtil;
import org.faktorips.values.LocalizedString;

/**
 * This class implements an algorithm to import a properties file containing the validation messages
 * for {@link IValidationRule}s. The keys have to be in the format as they are generated by
 * {@link ValidationRuleMessagesGenerator}.
 * 
 * @author dirmeier
 */
public class ValidationRuleMessagesPropertiesImporter implements IWorkspaceRunnable {

    public static final int MSG_CODE_MISSING_MESSAGE = 1;

    public static final int MSG_CODE_ILLEGAL_MESSAGE = 2;

    private InputStream contents;

    private IIpsPackageFragmentRoot root;

    private Locale locale;

    private IStatus resultStatus = new Status(IStatus.OK, StdBuilderPlugin.PLUGIN_ID, "");

    public ValidationRuleMessagesPropertiesImporter(InputStream contents, IIpsPackageFragmentRoot root, Locale locale) {
        this.contents = contents;
        this.root = root;
        this.locale = locale;
    }

    @Override
    public void run(IProgressMonitor monitor) throws CoreException {

        setResultStatus(importPropertyFile(monitor));

    }

    /**
     * This method imports the messages in the give file into the objects found in the specified
     * {@link IIpsPackageFragmentRoot}. The messages are set for the specified locale.
     */
    IStatus importPropertyFile(IProgressMonitor monitor) {
        try {
            Properties properties = new Properties();
            properties.load(contents);

            return importProperties(properties, monitor);
        } catch (CoreException e) {
            return e.getStatus();
        } catch (IOException e) {
            return new Status(IStatus.ERROR, StdBuilderPlugin.PLUGIN_ID, "Error while loading property file", e);
        } finally {
            IoUtil.close(contents);
            monitor.done();
        }

    }

    IStatus importProperties(Properties properties, IProgressMonitor monitor) throws CoreException {
        MultiStatus illegalMessages = new MultiStatus(StdBuilderPlugin.PLUGIN_ID, MSG_CODE_ILLEGAL_MESSAGE,
                "Found illegal messages", null);
        MultiStatus missingMessages = new MultiStatus(StdBuilderPlugin.PLUGIN_ID, MSG_CODE_MISSING_MESSAGE,
                "Missing messages for validation rules", null);

        List<IIpsSrcFile> findAllIpsSrcFiled;
        findAllIpsSrcFiled = root.findAllIpsSrcFiles(IpsObjectType.POLICY_CMPT_TYPE);
        List<String> readMessageKeys = new ArrayList<String>();
        monitor.beginTask("Updating Policy Component Types", findAllIpsSrcFiled.size() * 2 + 1);
        for (IIpsSrcFile ipsSrcFile : findAllIpsSrcFiled) {
            IPolicyCmptType pcType;
            pcType = (IPolicyCmptType)ipsSrcFile.getIpsObject();
            if (!ipsSrcFile.isMutable()) {
                continue;
            }
            boolean dirtyState = ipsSrcFile.isDirty();
            List<IValidationRule> validationRules = pcType.getValidationRules();
            for (IValidationRule validationRule : validationRules) {
                String messageKey = ValidationRuleMessagesGenerator.getMessageKey(validationRule);
                String message = properties.getProperty(messageKey);
                if (message == null) {
                    missingMessages.add(new Status(IStatus.WARNING, StdBuilderPlugin.PLUGIN_ID,
                            "Did not found a message for Rule " + validationRule.getName()
                                    + " in Policy Component Type" + pcType.getQualifiedName() + ". Message key is: "
                                    + messageKey));
                    continue;
                }
                validationRule.getMessageText().add(new LocalizedString(locale, message));
                readMessageKeys.add(messageKey);
            }
            monitor.worked(1);
            if (!dirtyState && ipsSrcFile.isDirty()) {
                ipsSrcFile.save(false, new SubProgressMonitor(monitor, 1));
            }
        }

        if (readMessageKeys.size() < properties.size()) {
            for (Object key : properties.keySet()) {
                if (!readMessageKeys.contains(key)) {
                    illegalMessages.add(new Status(IStatus.WARNING, StdBuilderPlugin.PLUGIN_ID, "Messagekey " + key
                            + " is not valid. No rule was found for the specified name."));
                }
            }
        }
        monitor.worked(1);

        MultiStatus result = new MultiStatus(StdBuilderPlugin.PLUGIN_ID, 0, "Problems during importing messages", null);
        if (!illegalMessages.isOK()) {
            result.add(illegalMessages);
        }
        if (!missingMessages.isOK()) {
            result.add(missingMessages);
        }
        if (result.isOK()) {
            return new Status(IStatus.OK, StdBuilderPlugin.PLUGIN_ID, "");
        } else {
            return result;
        }
    }

    /**
     * @param resultStatus The resultStatus to set.
     */
    public void setResultStatus(IStatus resultStatus) {
        this.resultStatus = resultStatus;
    }

    /**
     * @return Returns the resultStatus.
     */
    public IStatus getResultStatus() {
        return resultStatus;
    }
}
