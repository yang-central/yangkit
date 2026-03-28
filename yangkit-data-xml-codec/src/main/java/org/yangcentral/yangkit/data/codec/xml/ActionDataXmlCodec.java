package org.yangcentral.yangkit.data.codec.xml;

import org.dom4j.Element;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.ActionData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.Action;

/**
 * XML codec for YANG Action data nodes (RFC 7950 Section 7.15).
 * 
 * Action represents an operation that can be invoked on a specific node in the data tree.
 * This codec handles serialization and deserialization of Action input/output parameters.
 */
public class ActionDataXmlCodec extends YangDataXmlCodec<Action, ActionData> {
    
    protected ActionDataXmlCodec(Action schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ActionData buildData(Element element, ValidatorResultBuilder validatorResultBuilder) {
        // Action itself doesn't have direct value, only input/output
        ActionData actionData = (ActionData) YangDataBuilderFactory.getBuilder()
                .getYangData(getSchemaNode(), null);
        return actionData;
    }

    @Override
    protected void buildElement(Element element, YangData<?> yangData) {
        // Action element is just a container, no text content needed
    }

    /**
     * Serialize Action input parameters.
     * 
     * @param element the parent XML element
     * @param inputData the input data to serialize
     */
    public void serializeInput(Element element, YangData<?> inputData) {
        if (inputData != null) {
            YangDataXmlCodec<?, ?> inputCodec = getInstance(inputData.getSchemaNode());
            Element inputElement = inputCodec.serialize(inputData);
            element.add(inputElement);
        }
    }

    /**
     * Serialize Action output parameters.
     * 
     * @param element the parent XML element
     * @param outputData the output data to serialize
     */
    public void serializeOutput(Element element, YangData<?> outputData) {
        if (outputData != null) {
            YangDataXmlCodec<?, ?> outputCodec = getInstance(outputData.getSchemaNode());
            Element outputElement = outputCodec.serialize(outputData);
            element.add(outputElement);
        }
    }
}
