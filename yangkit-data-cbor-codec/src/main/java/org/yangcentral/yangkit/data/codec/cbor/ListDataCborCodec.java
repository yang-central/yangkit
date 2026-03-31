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
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.data.api.model.ListData;
import org.yangcentral.yangkit.model.api.stmt.Anydata;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.YangList;

import java.util.ArrayList;
import java.util.List;

/**
 * CBOR codec for a single YANG list entry.
 *
 * <p>A list entry is serialized as a CBOR map (JSON object) containing all
 * data children (key leaves + non-key fields). The parent container codec is
 * responsible for wrapping multiple entries into a CBOR array.
 *
 * @author Yangkit Team
 */
public class ListDataCborCodec extends YangDataCborCodec<YangList, ListData> {

    public ListDataCborCodec(YangList schemaNode) {
        super(schemaNode);
    }

    /**
     * Serializes a single list entry as a JSON object containing all its children
     * (key leaves + non-key fields).
     */
    @Override
    protected ObjectNode buildJson(ListData yangData) throws YangDataCborCodecException {
        return CborCodecUtil.serializeListEntry(yangData);
    }

    /**
     * Deserializes a JSON object as a single list entry.
     *
     * <p>Key leaves are extracted first (required by {@link YangDataBuilderFactory}),
     * then non-key children are deserialized and added to the entry.
     */
    @Override
    protected ListData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        if (jsonNode == null || !jsonNode.isObject()) {
            throw new YangDataCborCodecException("Expected a JSON object for list entry, got: "
                    + (jsonNode == null ? "null" : jsonNode.getNodeType()));
        }

        // --- Step 1: extract key leaf data ---
        List<LeafData> keyDataList = new ArrayList<>();
        List<Leaf> keyNodes = getSchemaNode().getKey().getkeyNodes();
        for (Leaf keyLeaf : keyNodes) {
            String keyName = keyLeaf.getArgStr();
            JsonNode keyElement = jsonNode.get(keyName);
            if (keyElement == null) {
                // Key absent in payload – skip (e.g. list was encoded without keys)
                continue;
            }
            LeafDataCborCodec keyCodec = new LeafDataCborCodec(keyLeaf);
            LeafData<?> keyData = keyCodec.buildData(keyElement, validatorResultBuilder);
            if (keyData != null) {
                keyDataList.add(keyData);
            }
        }

        // --- Step 2: create the list entry via builder ---
        @SuppressWarnings("unchecked")
        ListData listData = (ListData) YangDataBuilderFactory.getBuilder()
                .getYangData(getSchemaNode(), keyDataList);
        if (listData == null) {
            throw new YangDataCborCodecException(
                    "Builder returned null for list entry: " + getSchemaNode().getArgStr());
        }
        listData.setQName(getSchemaNode().getIdentifier());

        // --- Step 3: deserialize non-key children ---
        List<DataDefinition> schemaDefs = getSchemaNode().getDataDefChildren();
        for (DataDefinition def : schemaDefs) {
            if (!(def instanceof org.yangcentral.yangkit.model.api.stmt.SchemaNode)) continue;
            org.yangcentral.yangkit.model.api.stmt.SchemaNode childSchema =
                    (org.yangcentral.yangkit.model.api.stmt.SchemaNode) def;
            String childName = childSchema.getIdentifier().getLocalName();

            // Skip keys – they are already part of the identifier
            boolean isKey = false;
            for (Leaf key : keyNodes) {
                if (key.getArgStr().equals(childName)) { isKey = true; break; }
            }
            if (isKey) continue;

            JsonNode childNode = jsonNode.get(childName);
            if (childNode == null) continue;

            try {
                if (childSchema instanceof Leaf) {
                    LeafDataCborCodec lc = new LeafDataCborCodec((Leaf) childSchema);
                    LeafData<?> ld = lc.buildData(childNode, validatorResultBuilder);
                    if (ld != null) listData.addChild(ld);

                } else if (childSchema instanceof LeafList) {
                    LeafList ll = (LeafList) childSchema;
                    if (childNode.isArray()) {
                        LeafListDataCborCodec llc = new LeafListDataCborCodec(ll);
                        for (JsonNode element : childNode) {
                            LeafListData<?> lld = llc.buildData(element, validatorResultBuilder);
                            if (lld != null) listData.addChild(lld);
                        }
                    }

                } else if (childSchema instanceof Container) {
                    ContainerDataCborCodec cc = new ContainerDataCborCodec((Container) childSchema);
                    cc.setAnydataValidationContextResolver(getAnydataValidationContextResolver());
                    cc.setSourcePath(resolveChildSourcePath(childName));
                    ContainerData cd = cc.buildData(childNode, validatorResultBuilder);
                    if (cd != null) listData.addChild(cd);

                } else if (childSchema instanceof YangList) {
                    YangList nestedList = (YangList) childSchema;
                    ListDataCborCodec ldc = new ListDataCborCodec(nestedList);
                    ldc.setAnydataValidationContextResolver(getAnydataValidationContextResolver());
                    if (childNode.isArray()) {
                        int index = 0;
                        for (JsonNode entryNode : childNode) {
                            ldc.setSourcePath(resolveChildSourcePath(childName) + "[" + index + "]");
                            ListData entry = ldc.buildData(entryNode, validatorResultBuilder);
                            if (entry != null) listData.addChild(entry);
                            index++;
                        }
                    }

                } else if (childSchema instanceof Anydata) {
                    AnyDataDataCborCodec adc = new AnyDataDataCborCodec((Anydata) childSchema);
                    adc.setAnydataValidationContextResolver(getAnydataValidationContextResolver());
                    adc.setSourcePath(resolveChildSourcePath(childName));
                    AnyDataData anyData = adc.buildData(childNode, validatorResultBuilder);
                    if (anyData != null) listData.addChild(anyData);
                }
            } catch (Exception e) {
                throw new YangDataCborCodecException(
                        "Failed to deserialize child '" + childName + "' in list entry", e);
            }
        }

        return listData;
    }

    private String resolveChildSourcePath(String childName) {
        if (getSourcePath() == null || getSourcePath().isEmpty()) {
            return "/" + childName;
        }
        return getSourcePath() + "/" + childName;
    }
}
