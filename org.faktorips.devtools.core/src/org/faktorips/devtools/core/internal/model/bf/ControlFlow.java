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

package org.faktorips.devtools.core.internal.model.bf;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.draw2d.AbsoluteBendpoint;
import org.eclipse.draw2d.Bendpoint;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.faktorips.datatype.ValueDatatype;
import org.faktorips.devtools.core.internal.model.ipsobject.IpsObjectPart;
import org.faktorips.devtools.core.model.IIpsElement;
import org.faktorips.devtools.core.model.bf.BFElementType;
import org.faktorips.devtools.core.model.bf.IBFElement;
import org.faktorips.devtools.core.model.bf.IBusinessFunction;
import org.faktorips.devtools.core.model.bf.IControlFlow;
import org.faktorips.devtools.core.model.bf.IDecisionBFE;
import org.faktorips.devtools.core.model.ipsobject.IIpsObject;
import org.faktorips.devtools.core.model.ipsobject.IIpsObjectPart;
import org.faktorips.devtools.core.model.ipsproject.IIpsProject;
import org.faktorips.util.message.Message;
import org.faktorips.util.message.MessageList;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class ControlFlow extends IpsObjectPart implements IControlFlow {

    private String conditionValue = ""; //$NON-NLS-1$
    private Integer targetId;
    private Integer sourceId;
    private List<Bendpoint> bendpoints = new ArrayList<Bendpoint>();

    public ControlFlow(IIpsObject parent, int id) {
        super(parent, id);
    }

    public List<Bendpoint> getBendpoints() {
        return Collections.unmodifiableList(bendpoints);
    }

    public void setBendpoint(int index, Bendpoint bendpoint) {
        if (bendpoint == null || bendpoints.contains(bendpoint)) {
            return;
        }
        bendpoints.set(index, bendpoint);
        objectHasChanged();
    }

    public void addBendpoint(int index, Bendpoint bendpoint) {
        if (bendpoint == null || bendpoints.contains(bendpoint)) {
            return;
        }
        bendpoints.add(index, bendpoint);
        objectHasChanged();
    }

    public void removeBendpoint(int index) {
        if (bendpoints.remove(index) != null) {
            objectHasChanged();
        }
    }

    public String getConditionValue() {
        return conditionValue;
    }

    // TODO test
    public void setConditionValue(String value) {
        String old = conditionValue;
        this.conditionValue = value;
        valueChanged(old, conditionValue);
    }

    public IBusinessFunction getBusinessFunction() {
        return (IBusinessFunction)getParent();
    }

    public IBFElement getTarget() {
        return getBusinessFunction().getBFElement(targetId);
    }

    public void setTarget(IBFElement target) {
        if (this.targetId == null && target == null) {
            return;
        }
        if (this.targetId != null && target != null && this.targetId.equals(target.getId())) {
            return;
        }
        if (getTarget() != null) {
            getTarget().removeIncomingControlFlow(this);
        }
        this.targetId = (target == null) ? null : target.getId();
        objectHasChanged();
        if (getTarget() != null) {
            getTarget().addIncomingControlFlow(this);
        }
    }

    public IBFElement getSource() {
        return getBusinessFunction().getBFElement(sourceId);
    }

    public void setSource(IBFElement source) {
        if (this.sourceId == null && source == null) {
            return;
        }
        if (this.sourceId != null && source != null && this.sourceId.equals(source.getId())) {
            return;
        }
        if (getSource() != null) {
            getSource().removeOutgoingControlFlow(this);
        }
        this.sourceId = (source == null) ? null : source.getId();
        objectHasChanged();
        if (getSource() != null) {
            getSource().addOutgoingControlFlow(this);
        }
    }

    @Override
    protected void initPropertiesFromXml(Element element, Integer id) {
        super.initPropertiesFromXml(element, id);
        name = element.getAttribute(PROPERTY_NAME);
        String sourceValue = element.getAttribute(PROPERTY_SOURCE);
        sourceId = StringUtils.isEmpty(sourceValue) ? null : Integer.parseInt(sourceValue);
        String targetValue = element.getAttribute(PROPERTY_TARGET);
        targetId = StringUtils.isEmpty(targetValue) ? null : Integer.parseInt(targetValue);
        // TODO test
        conditionValue = element.getAttribute(PROPERTY_CONDITION_VALUE);
        NodeList nl = element.getElementsByTagName("Bendpoint"); //$NON-NLS-1$
        bendpoints.clear();
        for (int i = 0; i < nl.getLength(); i++) {
            Element bendpointEl = (Element)nl.item(i);
            int locationX = Integer.parseInt(bendpointEl.getAttribute("locationX")); //$NON-NLS-1$
            int locationY = Integer.parseInt(bendpointEl.getAttribute("locationY")); //$NON-NLS-1$
            bendpoints.add(new AbsoluteBendpoint(locationX, locationY));
        }
    }

    @Override
    protected void propertiesToXml(Element element) {
        super.propertiesToXml(element);
        element.setAttribute(PROPERTY_NAME, name);
        element.setAttribute(PROPERTY_SOURCE, sourceId == null ? "" : String.valueOf(sourceId)); //$NON-NLS-1$
        element.setAttribute(PROPERTY_TARGET, targetId == null ? "" : String.valueOf(targetId)); //$NON-NLS-1$
        // TODO test
        element.setAttribute(PROPERTY_CONDITION_VALUE, conditionValue);
        Document doc = element.getOwnerDocument();
        for (Bendpoint bendpoint : this.bendpoints) {
            Element bendpointEl = doc.createElement("Bendpoint"); //$NON-NLS-1$
            Point location = bendpoint.getLocation();
            bendpointEl.setAttribute("locationX", String.valueOf(location.x)); //$NON-NLS-1$
            bendpointEl.setAttribute("locationY", String.valueOf(location.y)); //$NON-NLS-1$
            element.appendChild(bendpointEl);
        }
    }

    @Override
    protected Element createElement(Document doc) {
        return doc.createElement(IControlFlow.XML_TAG);
    }

    @Override
    public IIpsElement[] getChildren() {
        return new IIpsElement[0];
    }

    @Override
    protected IIpsObjectPart newPart(Element xmlTag, int id) {
        return null;
    }

    @Override
    protected void reAddPart(IIpsObjectPart part) {
    }

    @Override
    protected void reinitPartCollections() {
    }

    @Override
    protected void removePart(IIpsObjectPart part) {
    }

    @SuppressWarnings("unchecked")
    public IIpsObjectPart newPart(Class partType) {
        return null;
    }

    // TODO image access
    public Image getImage() {
        return null;
    }

    @Override
    protected void validateThis(MessageList list, IIpsProject ipsProject) throws CoreException {
        IBFElement source = getSource();
        if (source != null && source.getType().equals(BFElementType.DECISION)) {
            if (StringUtils.isEmpty(getConditionValue())) {
                list.add(new Message(MSGCODE_VALUE_NOT_SPECIFIED, Messages
                        .getString("ControlFlow.valueMustBeSpecified"), //$NON-NLS-1$
                        Message.ERROR, this));
                return;
            }
            DecisionBFE decisionSource = (DecisionBFE)source;
            ValueDatatype datatype = decisionSource.findDatatype(ipsProject);
            if (datatype != null) {
                if (!datatype.isParsable(getConditionValue())) {
                    list.add(new Message(MSGCODE_VALUE_NOT_VALID, Messages.getString("ControlFlow.valueNotValid"), //$NON-NLS-1$
                            Message.ERROR, this));
                }
            }
            validateDublicateValues(decisionSource, list);
        }
    }

    private void validateDublicateValues(IDecisionBFE decision, MessageList msgList) {
        List<IControlFlow> cfs = decision.getOutgoingControlFlow();
        for (IControlFlow controlFlow : cfs) {
            if (controlFlow == this) {
                continue;
            }
            if (StringUtils.isEmpty(controlFlow.getConditionValue()) || StringUtils.isEmpty(getConditionValue())) {
                continue;
            }
            if (controlFlow.getConditionValue().equals(getConditionValue())) {
                String text = NLS.bind(Messages.getString("ControlFlow.duplicateControlFlowValue"), new String[]{decision.getName(), getConditionValue()}); //$NON-NLS-1$
                msgList.add(new Message(MSGCODE_DUBLICATE_VALUES, text, Message.ERROR, this));
            }
        }
    }

}
