/*******************************************************************************
 * Copyright (c) Faktor Zehn AG. <http://www.faktorzehn.org>
 * 
 * This source code is available under the terms of the AGPL Affero General Public License version
 * 3.
 * 
 * Please see LICENSE.txt for full license terms, including the additional permissions and
 * restrictions as well as the possibility of alternative license terms.
 *******************************************************************************/
package org.faktorips.runtime.modeltype.internal.read;

import java.lang.reflect.AnnotatedElement;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;

import org.faktorips.runtime.model.annotation.AnnotatedDeclaration;
import org.faktorips.runtime.modeltype.IModelElement;
import org.faktorips.runtime.modeltype.internal.ModelType;

public abstract class ModelPartCollector<T extends IModelElement, D extends PartDescriptor<T>> {

    protected static final String[] NO_NAMES = new String[0];

    private final LinkedHashMap<String, D> descriptors = new LinkedHashMap<String, D>();

    private List<AnnotationProcessor<?, D>> annotationProcessors;

    public ModelPartCollector(List<AnnotationProcessor<?, D>> annotationAccessors) {
        this.annotationProcessors = annotationAccessors;
    }

    public void initDescriptors(AnnotatedDeclaration annotatedDeclaration) {
        for (String name : getNames(annotatedDeclaration)) {
            D descriptor = createDescriptor();
            descriptor.setName(name);
            descriptors.put(name, descriptor);
        }
    }

    public LinkedHashMap<String, T> createParts(ModelType modelType) {
        LinkedHashMap<String, T> result = new LinkedHashMap<String, T>();
        for (Entry<String, D> descriptorEntry : descriptors.entrySet()) {
            D descriptor = descriptorEntry.getValue();
            T part = descriptor.create(modelType);
            addPart(result, part);
        }
        return result;
    }

    protected void addPart(LinkedHashMap<String, T> result, T part) {
        result.put(part.getName(), part);
    }

    protected abstract String[] getNames(AnnotatedDeclaration annotatedDeclaration);

    protected abstract D createDescriptor();

    public void readAnnotations(AnnotatedDeclaration annotatedDeclaration, AnnotatedElement annotatedElement) {
        for (AnnotationProcessor<?, D> annotationProcessor : annotationProcessors) {
            readAnnotationsInternal(annotationProcessor, annotatedDeclaration, annotatedElement);
        }
    }

    private void readAnnotationsInternal(AnnotationProcessor<?, D> annotationProcessor,
            AnnotatedDeclaration annotatedDeclaration,
            AnnotatedElement annotatedElement) {
        if (annotationProcessor.accept(annotatedElement)) {
            String name = annotationProcessor.getName(annotatedElement);
            D descriptor = getDescriptor(name, annotatedDeclaration.getDeclarationClassName());
            annotationProcessor.process(descriptor, annotatedDeclaration, annotatedElement);
        }
    }

    protected D getDescriptor(String name, String typeName) {
        D descriptor = descriptors.get(name);
        if (descriptor != null) {
            return descriptor;
        } else {
            throw new IllegalArgumentException("Cannot find part " + name + " in " + typeName);
        }
    }

    protected LinkedHashMap<String, D> getDescriptors() {
        return descriptors;
    }

}
