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
import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.data.impl.model.NotificationDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.DataDefinition;
import org.yangcentral.yangkit.model.api.stmt.Leaf;
import org.yangcentral.yangkit.model.api.stmt.LeafList;
import org.yangcentral.yangkit.model.api.stmt.Notification;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.YangList;

import java.util.List;

/**
 * CBOR codec for YANG notification data (RFC 9254 §6.2).
 *
 * <p>A notification message is treated as a container-like structure: its
 * parameters are encoded as a CBOR map with leaf/container children.
 *
 * @author Yangkit Team
 */
public class NotificationDataCborCodec extends YangDataCborCodec<Notification, NotificationData> {

    public NotificationDataCborCodec(Notification schemaNode) {
        super(schemaNode);
    }

    @Override
    protected ObjectNode buildJson(NotificationData yangData) throws YangDataCborCodecException {
        return CborCodecUtil.serializeChildren(yangData);
    }

    @Override
    protected NotificationData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        NotificationDataImpl notificationData = new NotificationDataImpl(getSchemaNode());
        QName qName = getSchemaNode().getIdentifier();
        notificationData.setQName(qName);

        if (jsonNode == null || !jsonNode.isObject()) {
            return notificationData;
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
                    if (ld != null) notificationData.addChild(ld);

                } else if (childSchema instanceof LeafList) {
                    LeafList ll = (LeafList) childSchema;
                    LeafListDataCborCodec llc = new LeafListDataCborCodec(ll);
                    if (childNode.isArray()) {
                        for (JsonNode element : childNode) {
                            LeafListData<?> lld = llc.buildData(element, validatorResultBuilder);
                            if (lld != null) notificationData.addChild(lld);
                        }
                    }

                } else if (childSchema instanceof Container) {
                    ContainerDataCborCodec cc = new ContainerDataCborCodec((Container) childSchema);
                    ContainerData cd = cc.buildData(childNode, validatorResultBuilder);
                    if (cd != null) notificationData.addChild(cd);

                } else if (childSchema instanceof YangList) {
                    YangList listSchema = (YangList) childSchema;
                    ListDataCborCodec ldc = new ListDataCborCodec(listSchema);
                    if (childNode.isArray()) {
                        for (JsonNode entryNode : childNode) {
                            ListData entry = ldc.buildData(entryNode, validatorResultBuilder);
                            if (entry != null) notificationData.addChild(entry);
                        }
                    }
                }
            } catch (Exception e) {
                throw new YangDataCborCodecException(
                        "Failed to deserialize notification child '" + childName + "'", e);
            }
        }

        return notificationData;
    }
}
