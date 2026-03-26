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
import org.yangcentral.yangkit.data.api.model.NotificationData;
import org.yangcentral.yangkit.data.impl.model.NotificationDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Notification;

/**
 * CBOR codec for YANG notification data.
 *
 * @author Yangkit Team
 */
public class NotificationDataCborCodec extends YangDataCborCodec<Notification, NotificationData> {
    
    public NotificationDataCborCodec(Notification schemaNode) {
        super(schemaNode);
    }
    
    @Override
    protected ObjectNode buildJson(NotificationData yangData) throws YangDataCborCodecException {
        ObjectNode rootNode = JSON_MAPPER.createObjectNode();
        
        // Serialize notification content
        // TODO: Implement proper serialization of notification data
        
        return rootNode;
    }
    
    @Override
    protected NotificationData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException {
        NotificationDataImpl notificationData = new NotificationDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        notificationData.setQName(qName);
        
        // Process notification content from JSON
        // TODO: Implement proper deserialization of notification data
        
        return notificationData;
    }
}
