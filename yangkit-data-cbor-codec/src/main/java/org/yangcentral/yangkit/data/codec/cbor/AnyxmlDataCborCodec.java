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
import org.yangcentral.yangkit.data.api.model.AnyxmlData;
import org.yangcentral.yangkit.data.impl.model.AnyXmlDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Anyxml;

/**
 * CBOR codec for YANG anyxml data.
 * AnyXML contains XML content that needs to be serialized/deserialized.
 *
 * @author Yangkit Team
 */
public class AnyxmlDataCborCodec extends YangDataCborCodec<Anyxml, AnyxmlData> {
    
    public AnyxmlDataCborCodec(Anyxml schemaNode) {
        super(schemaNode);
    }
    
    @Override
    protected JsonNode buildJson(AnyxmlData yangData) throws YangDataCborCodecException {
        // For AnyXML, we need to serialize the XML content
        // This is a simplified implementation
        ObjectNode rootNode = JSON_MAPPER.createObjectNode();
        
        // TODO: Implement proper XML serialization
        // The XML content should be serialized according to RFC 9254
        
        return rootNode;
    }
    
    @Override
    protected AnyxmlData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException {
        AnyXmlDataImpl anyxmlData = new AnyXmlDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        anyxmlData.setQName(qName);
        
        // Process anyxml content from JSON
        // TODO: Implement proper XML deserialization
        
        return anyxmlData;
    }
}
