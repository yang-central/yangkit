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
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.LeafData;
import org.yangcentral.yangkit.data.api.model.LeafListData;
import org.yangcentral.yangkit.data.api.model.ListData;
import org.yangcentral.yangkit.data.impl.model.ContainerDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.YangList;

import java.util.List;

/**
 * CBOR codec for YANG container data (RFC 9254 §5.1).
 *
 * <p>Serialization delegates to {@link CborCodecUtil#serializeChildren}, which groups
 * list/leaf-list entries into arrays automatically. Deserialization handles all
 * standard child node types: Leaf, LeafList, Container, and YangList.
 *
 * @author Yangkit Team
 */
public class ContainerDataCborCodec extends YangDataCborCodec<Container, ContainerData> {

    public ContainerDataCborCodec(Container schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ObjectNode buildJson(ContainerData yangData) throws YangDataCborCodecException {
        return CborCodecUtil.serializeChildren(yangData);
    }

    @Override
    protected ContainerData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        ContainerDataImpl containerData = new ContainerDataImpl(getSchemaNode());
        QName qName = getSchemaNode().getIdentifier();
        containerData.setQName(qName);

        if (jsonNode == null || !jsonNode.isObject()) {
            return containerData;
        }

        List<DataDefinition> schemaDefs = getSchemaNode().getDataDefChildren();
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
                    if (ld != null) containerData.addChild(ld);

                } else if (childSchema instanceof LeafList) {
                    // leaf-list is encoded as a JSON array; each element is one entry
                    LeafList ll = (LeafList) childSchema;
                    LeafListDataCborCodec llc = new LeafListDataCborCodec(ll);
                    if (childNode.isArray()) {
                        for (JsonNode element : childNode) {
                            LeafListData<?> lld = llc.buildData(element, validatorResultBuilder);
                            if (lld != null) containerData.addChild(lld);
                        }
                    }

                } else if (childSchema instanceof Container) {
                    ContainerDataCborCodec cc = new ContainerDataCborCodec((Container) childSchema);
                    ContainerData cd = cc.buildData(childNode, validatorResultBuilder);
                    if (cd != null) containerData.addChild(cd);

                } else if (childSchema instanceof YangList) {
                    // list is encoded as a JSON array; each element is one list entry
                    YangList listSchema = (YangList) childSchema;
                    ListDataCborCodec ldc = new ListDataCborCodec(listSchema);
                    if (childNode.isArray()) {
                        for (JsonNode entryNode : childNode) {
                            ListData entry = ldc.buildData(entryNode, validatorResultBuilder);
                            if (entry != null) containerData.addChild(entry);
                        }
                    }
                }
                // choice/case and other complex nodes are out of scope for this implementation
            } catch (YangDataCborCodecException e) {
                throw e;
            } catch (Exception e) {
                throw new YangDataCborCodecException(
                        "Failed to deserialize child '" + childName + "'", e);
            }
        }

        return containerData;
    }
}
