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
import org.yangcentral.yangkit.data.api.model.RpcData;
import org.yangcentral.yangkit.data.impl.model.RpcDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Rpc;

/**
 * CBOR codec for YANG RPC data.
 * Handles both RPC input and output.
 *
 * @author Yangkit Team
 */
public class RpcDataCborCodec extends YangDataCborCodec<Rpc, RpcData> {
    
    public RpcDataCborCodec(Rpc schemaNode) {
        super(schemaNode);
    }
    
    @Override
    protected ObjectNode buildJson(RpcData yangData) throws YangDataCborCodecException {
        ObjectNode rootNode = JSON_MAPPER.createObjectNode();
        
        // Serialize RPC content (input/output parameters)
        // TODO: Implement proper serialization of RPC parameters
        
        return rootNode;
    }
    
    @Override
    protected RpcData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder) 
            throws YangDataCborCodecException {
        RpcDataImpl rpcData = new RpcDataImpl(getSchemaNode());
        
        QName qName = getSchemaNode().getIdentifier();
        rpcData.setQName(qName);
        
        // Process RPC content from JSON
        // TODO: Implement proper deserialization of RPC parameters
        
        return rpcData;
    }
}
