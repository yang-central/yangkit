/*
 * Copyright 2023 Yangkit Authors. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.yangcentral.yangkit.data.codec.cbor;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.builder.YangDataBuilderFactory;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.impl.model.RpcDataImpl;
import org.yangcentral.yangkit.model.api.stmt.*;

import java.util.List;

/**
 * CBOR codec for YANG RPC data (RFC 9254 §6).
 *
 * <p>An RPC envelope is encoded as a CBOR map with two optional keys:
 * {@code "input"} and {@code "output"}. Each is serialized using the
 * container-like codec pattern applied to the {@link Input} / {@link Output}
 * schema node's data children.
 *
 * @author Yangkit Team
 */
public class RpcDataCborCodec extends YangDataCborCodec<Rpc, RpcData> {

    public RpcDataCborCodec(Rpc schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ObjectNode buildJson(RpcData yangData) throws YangDataCborCodecException {
        // RpcData is a YangDataContainer; its children are InputData and/or OutputData
        return CborCodecUtil.serializeChildren(yangData);
    }

    @Override
    protected RpcData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        RpcDataImpl rpcData = new RpcDataImpl(getSchemaNode());
        QName qName = getSchemaNode().getIdentifier();
        rpcData.setQName(qName);

        if (jsonNode == null || !jsonNode.isObject()) {
            return rpcData;
        }

        // Deserialize "input" sub-tree
        Input inputSchema = getSchemaNode().getInput();
        try {
            if (inputSchema != null) {
                JsonNode inputNode = jsonNode.get("input");
                if (inputNode != null && inputNode.isObject()) {
                    InputData inputData = deserializeDataDefChildren(inputSchema, inputNode,
                            validatorResultBuilder);
                    if (inputData != null) {
                        rpcData.addChild(inputData);
                    }
                }
            }

            // Deserialize "output" sub-tree
            Output outputSchema = getSchemaNode().getOutput();
            if (outputSchema != null) {
                JsonNode outputNode = jsonNode.get("output");
                if (outputNode != null && outputNode.isObject()) {
                    OutPutData outputData = deserializeDataDefOutputChildren(outputSchema, outputNode,
                            validatorResultBuilder);
                    if (outputData != null) {
                        rpcData.addChild(outputData);
                    }
                }
            }
        } catch (org.yangcentral.yangkit.data.api.exception.YangDataException e) {
            throw new YangDataCborCodecException("Failed to add input/output to RPC data", e);
        }

        return rpcData;
    }

    @SuppressWarnings("unchecked")
    private InputData deserializeDataDefChildren(Input inputSchema, JsonNode jsonNode,
                                                  ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        InputData inputData = (InputData) YangDataBuilderFactory.getBuilder()
                .getYangData(inputSchema, null);
        if (inputData == null) return null;
        inputData.setQName(inputSchema.getIdentifier());
        deserializeChildren(inputData, inputSchema.getDataDefChildren(), jsonNode,
                validatorResultBuilder);
        return inputData;
    }

    @SuppressWarnings("unchecked")
    private OutPutData deserializeDataDefOutputChildren(Output outputSchema, JsonNode jsonNode,
                                                         ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        OutPutData outputData = (OutPutData) YangDataBuilderFactory.getBuilder()
                .getYangData(outputSchema, null);
        if (outputData == null) return null;
        outputData.setQName(outputSchema.getIdentifier());
        deserializeChildren(outputData, outputSchema.getDataDefChildren(), jsonNode,
                validatorResultBuilder);
        return outputData;
    }

    private void deserializeChildren(YangData<?> parent,
                                     List<DataDefinition> schemaDefs,
                                     JsonNode jsonNode,
                                     ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        if (schemaDefs == null || !(parent instanceof org.yangcentral.yangkit.data.api.model.YangDataContainer)) {
            return;
        }
        org.yangcentral.yangkit.data.api.model.YangDataContainer container =
                (org.yangcentral.yangkit.data.api.model.YangDataContainer) parent;
        for (DataDefinition def : schemaDefs) {
            if (!(def instanceof SchemaNode)) continue;
            SchemaNode childSchema = (SchemaNode) def;
            String childName = childSchema.getIdentifier().getLocalName();
            JsonNode childNode = jsonNode.get(childName);
            if (childNode == null) continue;
            try {
                if (childSchema instanceof Leaf) {
                    LeafDataCborCodec lc = new LeafDataCborCodec((Leaf) childSchema);
                    LeafData<?> ld = lc.buildData(childNode, validatorResultBuilder);
                    if (ld != null) container.addChild(ld);
                } else if (childSchema instanceof LeafList) {
                    LeafListDataCborCodec llc = new LeafListDataCborCodec((LeafList) childSchema);
                    if (childNode.isArray()) {
                        for (JsonNode el : childNode) {
                            LeafListData<?> lld = llc.buildData(el, validatorResultBuilder);
                            if (lld != null) container.addChild(lld);
                        }
                    }
                } else if (childSchema instanceof Container) {
                    ContainerDataCborCodec cc = new ContainerDataCborCodec((Container) childSchema);
                    ContainerData cd = cc.buildData(childNode, validatorResultBuilder);
                    if (cd != null) container.addChild(cd);
                } else if (childSchema instanceof YangList) {
                    ListDataCborCodec ldc = new ListDataCborCodec((YangList) childSchema);
                    if (childNode.isArray()) {
                        for (JsonNode entryNode : childNode) {
                            ListData entry = ldc.buildData(entryNode, validatorResultBuilder);
                            if (entry != null) container.addChild(entry);
                        }
                    }
                }
            } catch (Exception e) {
                throw new YangDataCborCodecException(
                        "Failed to deserialize RPC child '" + childName + "'", e);
            }
        }
    }
}
