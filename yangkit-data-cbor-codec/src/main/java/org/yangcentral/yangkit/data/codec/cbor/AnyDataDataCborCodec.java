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
import org.yangcentral.yangkit.data.api.model.AnyDataData;
import org.yangcentral.yangkit.data.impl.model.AnyDataDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Anydata;

/**
 * CBOR codec for YANG {@code anydata} (RFC 9254 §4.14).
 *
 * <p>{@code anydata} holds an arbitrary {@code YangDataDocument} value. Full
 * round-trip encoding requires converting between {@code YangDataDocument} and
 * CBOR, which in turn requires a YANG schema context. This implementation
 * provides a best-effort serialization:
 * <ul>
 *   <li>Serialize: emits an empty CBOR map when no richer representation is
 *       available (the value is a {@code YangDataDocument} that cannot easily
 *       be decomposed here).</li>
 *   <li>Deserialize: creates the {@code anydata} node without populating its
 *       value (schema-context-aware population is out of scope).</li>
 * </ul>
 *
 * @author Yangkit Team
 */
public class AnyDataDataCborCodec extends YangDataCborCodec<Anydata, AnyDataData> {

    public AnyDataDataCborCodec(Anydata schemaNode) {
        super(schemaNode);
    }

    @Override
    protected JsonNode buildJson(AnyDataData yangData) throws YangDataCborCodecException {
        // anydata value is a YangDataDocument; without a full YANG schema context
        // we cannot traverse it generically. Return an empty map as placeholder.
        ObjectNode rootNode = JSON_MAPPER.createObjectNode();
        return rootNode;
    }

    @Override
    protected AnyDataData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        AnyDataDataImpl anyDataData = new AnyDataDataImpl(getSchemaNode());
        QName qName = getSchemaNode().getIdentifier();
        anyDataData.setQName(qName);
        // Value population requires schema context - left as null
        return anyDataData;
    }
}
