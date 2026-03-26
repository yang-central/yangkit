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

/**
 * CBOR codec for YANG data based on RFC 9254.
 * 
 * <p>This package provides serialization and deserialization of YANG data to/from CBOR format.
 * It follows the specification defined in RFC 9254 - "YANG Data Model in Concise Binary Object Representation (CBOR)".</p>
 * 
 * <h2>Main Components:</h2>
 * <ul>
 *   <li>{@link org.yangcentral.yangkit.data.codec.cbor.YangDataCborCodec} - Abstract base class for all CBOR codecs</li>
 *   <li>{@link org.yangcentral.yangkit.data.codec.cbor.ContainerDataCborCodec} - Codec for container nodes</li>
 *   <li>{@link org.yangcentral.yangkit.data.codec.cbor.LeafDataCborCodec} - Codec for leaf nodes</li>
 *   <li>{@link org.yangcentral.yangkit.data.codec.cbor.LeafListDataCborCodec} - Codec for leaf-list nodes</li>
 *   <li>{@link org.yangcentral.yangkit.data.codec.cbor.ListDataCborCodec} - Codec for list nodes</li>
 *   <li>{@link org.yangcentral.yangkit.data.codec.cbor.CborCodecUtil} - Utility methods for conversion</li>
 * </ul>
 * 
 * <h2>Usage Example:</h2>
 * <pre>
 * {@code
 * // Get schema context
 * YangSchemaContext schemaContext = YangYinParser.parse("module.yang");
 * 
 * // Get container schema
 * Container container = schemaContext.getModules().get(0).getDataNodeChildren().get(0);
 * 
 * // Create CBOR codec
 * ContainerDataCborCodec cborCodec = new ContainerDataCborCodec(container);
 * 
 * // Serialize YANG data to CBOR
 * byte[] cborBytes = cborCodec.serialize(containerData);
 * 
 * // Deserialize CBOR to YANG data
 * ContainerData data = cborCodec.deserialize(cborBytes, validatorResultBuilder);
 * }
 * </pre>
 * 
 * @author Yangkit Team
 * @version 1.5.0
 * @since 1.5.0
 */
package org.yangcentral.yangkit.data.codec.cbor;
