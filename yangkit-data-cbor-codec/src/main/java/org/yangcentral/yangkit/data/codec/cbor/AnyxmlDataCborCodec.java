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
import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.AnyxmlData;
import org.yangcentral.yangkit.data.impl.model.AnyXmlDataImpl;
import org.yangcentral.yangkit.model.api.stmt.Anyxml;

/**
 * CBOR codec for YANG {@code anyxml} (RFC 9254 §4.15).
 *
 * <p>The XML value is serialized as a CBOR text string (the document's XML
 * representation) and deserialized back from a text string.
 *
 * @author Yangkit Team
 */
public class AnyxmlDataCborCodec extends YangDataCborCodec<Anyxml, AnyxmlData> {

    public AnyxmlDataCborCodec(Anyxml schemaNode) {
        super(schemaNode);
    }

    @Override
    protected JsonNode buildJson(AnyxmlData yangData) throws YangDataCborCodecException {
        Document doc = yangData.getValue();
        if (doc == null) {
            return JSON_MAPPER.createObjectNode();
        }
        return JSON_MAPPER.getNodeFactory().textNode(doc.asXML());
    }

    @Override
    protected AnyxmlData buildData(JsonNode jsonNode, ValidatorResultBuilder validatorResultBuilder)
            throws YangDataCborCodecException {
        AnyXmlDataImpl anyxmlData = new AnyXmlDataImpl(getSchemaNode());
        QName qName = getSchemaNode().getIdentifier();
        anyxmlData.setQName(qName);

        if (jsonNode != null && jsonNode.isTextual()) {
            try {
                Document doc = DocumentHelper.parseText(jsonNode.asText());
                anyxmlData.setValue(doc);
            } catch (Exception e) {
                // Malformed XML - leave value as null (soft failure)
            }
        }
        return anyxmlData;
    }
}
