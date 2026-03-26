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
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.ContainerData;
import org.yangcentral.yangkit.data.api.model.ListData;
import org.yangcentral.yangkit.data.impl.model.ListDataImpl;
import org.yangcentral.yangkit.model.api.stmt.YangList;

/**
 * CBOR codec for YANG list data.
 *
 * @author Yangkit Team
 */
public class ListDataCborCodec extends YangDataCborCodec<YangList, ListData> {
    
    public ListDataCborCodec(YangList schemaNode) {
        super(schemaNode);
    }
    
    @Override
    protected ArrayNode buildJson(ListData yangData) throws YangDataCborCodecException {
        ArrayNode arrayNode = JSON_MAPPER.createArrayNode();
        
        // Serialize each list entry - simplified implementation
        // Full implementation would need to get actual entries from the list data
        // This requires proper API support for iterating through list entries
        
        return arrayNode;
    }
    
    @Override
    protected ListData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException {
        // Create with empty keys list as required by constructor
        ListDataImpl listData = new ListDataImpl(getSchemaNode(), java.util.Collections.emptyList());
        
        QName qName = getSchemaNode().getIdentifier();
        listData.setQName(qName);
        
        // Process list entries from JSON array
        if (jsonNode != null && jsonNode.isArray()) {
            ArrayNode arrayNode = (ArrayNode) jsonNode;
            for (JsonNode entryNode : arrayNode) {
                // TODO: Implement entry deserialization based on list schema
                // This requires creating container data for each entry
            }
        }
        
        return listData;
    }
}
