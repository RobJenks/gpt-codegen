package org.rj.modelgen.bpmn.intrep.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.DatabindContext;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.jsontype.impl.TypeIdResolverBase;

public class ElementNodeTypeIdResolver extends TypeIdResolverBase {

    private JavaType baseType;

    @Override
    public void init(JavaType baseType) {
        this.baseType = baseType;
    }

    @Override
    public String idFromValue(Object value) {
        return idFromValueAndType(value, value.getClass());
    }

    @Override
    public String idFromValueAndType(Object value, Class<?> suggestedType) {
        if (value instanceof ElementNode node) {
            return node.getElementType();
        }
        return null;
    }

    @Override
    public JavaType typeFromId(DatabindContext context, String id) {
        Class<? extends ElementNode> clazz = ElementNodeTypeRegistry.getNodeClass(id);
        return context.constructType(clazz);
    }

    @Override
    public JsonTypeInfo.Id getMechanism() {
        return JsonTypeInfo.Id.CUSTOM;
    }
}
