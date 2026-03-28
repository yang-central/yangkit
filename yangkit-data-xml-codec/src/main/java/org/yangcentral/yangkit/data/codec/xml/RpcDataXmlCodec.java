package org.yangcentral.yangkit.data.codec.xml;

import org.dom4j.Element;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.RpcData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.stmt.Input;
import org.yangcentral.yangkit.model.api.stmt.Output;
import org.yangcentral.yangkit.model.api.stmt.Rpc;

/**
 * XML codec for YANG RPC data nodes (RFC 7950 Section 7.14).
 * 
 * RPC represents a remote procedure call operation.
 * This codec handles serialization and deserialization of RPC input/output messages.
 */
public class RpcDataXmlCodec extends YangDataXmlCodec<Rpc, RpcData> {
    
    protected RpcDataXmlCodec(Rpc schemaNode) {
        super(schemaNode);
    }

    @Override
    protected RpcData buildData(Element element, ValidatorResultBuilder validatorResultBuilder) {
        // RPC itself doesn't have direct value, only input/output
        RpcData rpcData = (RpcData) YangDataBuilderFactory.getBuilder()
                .getYangData(getSchemaNode(), null);
        return rpcData;
    }

    @Override
    protected void buildElement(Element element, YangData<?> yangData) {
        // RPC element is just a container, no text content needed
    }

    /**
     * Deserialize RPC input message.
     * 
     * @param element the input XML element
     * @param validatorResultBuilder validation result builder
     * @return deserialized input data
     */
    public RpcData deserializeInput(Element element, ValidatorResultBuilder validatorResultBuilder) {
        Input input = getSchemaNode().getInput();
        if (input == null) {
            return null;
        }
        
        RpcData rpcData = (RpcData) YangDataBuilderFactory.getBuilder()
                .getYangData(getSchemaNode(), null);
        
        // Process input children
        if (element != null) {
            YangDataDocumentXmlCodec documentCodec = new YangDataDocumentXmlCodec(getSchemaContext());
            ValidatorResultBuilder localValidatorBuilder = new ValidatorResultBuilder();
            documentCodec.buildChildrenData(rpcData, element);
            validatorResultBuilder.merge(localValidatorBuilder.build());
        }
        
        return rpcData;
    }

    /**
     * Serialize RPC input message.
     * 
     * @param element the parent XML element
     * @param inputData the input data to serialize
     */
    public void serializeInput(Element element, YangData<?> inputData) {
        if (inputData != null && inputData instanceof RpcData) {
            serializeChildren(element, (RpcData) inputData);
        }
    }

    /**
     * Deserialize RPC output message.
     * 
     * @param element the output XML element
     * @param validatorResultBuilder validation result builder
     * @return deserialized output data
     */
    public RpcData deserializeOutput(Element element, ValidatorResultBuilder validatorResultBuilder) {
        Output output = getSchemaNode().getOutput();
        if (output == null) {
            return null;
        }
        
        RpcData rpcData = (RpcData) YangDataBuilderFactory.getBuilder()
                .getYangData(getSchemaNode(), null);
        
        // Process output children
        if (element != null) {
            YangDataDocumentXmlCodec documentCodec = new YangDataDocumentXmlCodec(getSchemaContext());
            ValidatorResultBuilder localValidatorBuilder = new ValidatorResultBuilder();
            documentCodec.buildChildrenData(rpcData, element);
            validatorResultBuilder.merge(localValidatorBuilder.build());
        }
        
        return rpcData;
    }

    /**
     * Serialize RPC output message.
     * 
     * @param element the parent XML element
     * @param outputData the output data to serialize
     */
    public void serializeOutput(Element element, YangData<?> outputData) {
        if (outputData != null && outputData instanceof RpcData) {
            serializeChildren(element, (RpcData) outputData);
        }
    }
}
