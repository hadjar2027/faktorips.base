/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/

package org.faktorips.devtools.core.internal.model.ipsobject;

import java.util.Locale;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.osgi.util.NLS;
import org.faktorips.devtools.core.internal.model.IpsModel;
import org.faktorips.devtools.core.internal.model.ipsproject.IpsProject;
import org.faktorips.devtools.core.model.ipsobject.IDescription;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPartContainer;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.devtools.core.model.ipsproject.IIpsProjectProperties;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Implementation of {@link IDescription}.
 * 
 * @author Alexander Weickmann
 */
public class Description extends AtomicIpsObjectPart implements IDescription {

    private Locale locale;

    private String text = ""; //$NON-NLS-1$

    /**
     * @param ipsObjectPartContainer The parent {@link IIpsObjectPartContainer} this is a
     *            description for.
     * @param id A unique ID for this description.
     */
    public Description(IIpsObjectPartContainer ipsObjectPartContainer, String id) {
        super(ipsObjectPartContainer, id);
    }

    @Override
    public Locale getLocale() {
        return locale;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public void setLocale(Locale locale) {
        Locale oldValue = this.locale;
        this.locale = locale;
        valueChanged(oldValue, locale);
    }

    public void setLocaleWithoutChangeEvent(Locale locale) {
        this.locale = locale;
    }

    @Override
    public void setText(String text) {
        String oldValue = this.text;
        if (text == null) {
            this.text = ""; //$NON-NLS-1$
        } else {
            this.text = text;
        }
        valueChanged(oldValue, this.text);
    }

    public void setTextWithoutChangeEvent(String text) {
        if (text == null) {
            this.text = ""; //$NON-NLS-1$
        } else {
            this.text = text;
        }
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        if (locale == null) {
            validateLocaleMissing(list);
        } else {
            validateLocaleSupported(list);
        }
    }

    private void validateLocaleMissing(MessageList list) {
        String text = Messages.Description_msgLocaleMissing;
        Message msg = new Message(IDescription.MSGCODE_LOCALE_MISSING, text, Message.ERROR, this,
                IDescription.PROPERTY_LOCALE);
        list.add(msg);
    }

    private void validateLocaleSupported(MessageList list) {
        // Only the project of the label itself must support the language
        // Obtain the project properties via the IPS model as it provides caching
        IIpsProjectProperties properties = ((IpsModel)getIpsModel())
                .getIpsProjectProperties((IpsProject)getIpsProject());
        boolean localeSupported = properties.isSupportedLanguage(locale);
        if (!(localeSupported)) {
            String text = NLS.bind(Messages.Description_msgLocaleNotSupportedByProject, locale.getLanguage());
            Message msg = new Message(IDescription.MSGCODE_LOCALE_NOT_SUPPORTED_BY_IPS_PROJECT, text, Message.WARNING,
                    this, IDescription.PROPERTY_LOCALE);
            list.add(msg);
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, String id) {
        String localeCode = element.getAttribute(PROPERTY_LOCALE);
        locale = localeCode.equals("") ? null : new Locale(localeCode); //$NON-NLS-1$
        text = element.getTextContent();

        super.initPropertiesFromXml(element, id);
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);

        element.setAttribute(PROPERTY_LOCALE, (locale == null) ? "" : locale.getLanguage()); //$NON-NLS-1$
        element.setTextContent(text);
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(XML_TAG_NAME);
    }

}
