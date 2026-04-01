/**
 * Protocol Buffers codec for YANG data.
 * 
 * <p>This package provides bidirectional conversion between YANG data and Protocol Buffers messages.
 * It follows the same architecture as the JSON and XML codecs in yangkit-data-json-codec and 
 * yangkit-data-xml-codec modules.</p>
 * 
 * <h2>Key Components:</h2>
 * <ul>
 *   <li>{@link org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodec} - Abstract base class for all proto codecs</li>
 *   <li>{@link org.yangcentral.yangkit.data.codec.proto.ProtoCodecUtil} - Utility methods for type conversion</li>
 *   <li>Concrete codec implementations for each YANG data node type</li>
 *   <li>{@link org.yangcentral.yangkit.data.codec.proto.YangDataProtoCodecException} - Exception handling</li>
 * </ul>
 * 
 * <h2>Supported YANG Data Types:</h2>
 * <ul>
 *   <li>Container - {@link org.yangcentral.yangkit.data.codec.proto.ContainerDataProtoCodec}</li>
 *   <li>List - {@link org.yangcentral.yangkit.data.codec.proto.ListDataProtoCodec}</li>
 *   <li>Leaf - {@link org.yangcentral.yangkit.data.codec.proto.LeafDataProtoCodec}</li>
 *   <li>Leaf-List - {@link org.yangcentral.yangkit.data.codec.proto.LeafListDataProtoCodec}</li>
 *   <li>Anydata - {@link org.yangcentral.yangkit.data.codec.proto.AnyDataDataProtoCodec}</li>
 *   <li>Anyxml - {@link org.yangcentral.yangkit.data.codec.proto.AnyxmlDataProtoCodec}</li>
 *   <li>Notification - {@link org.yangcentral.yangkit.data.codec.proto.NotificationDataProtoCodec}</li>
 *   <li>RPC - {@link org.yangcentral.yangkit.data.codec.proto.RpcDataProtoCodec}</li>
 *   <li>Action - {@link org.yangcentral.yangkit.data.codec.proto.ActionDataProtoCodec}</li>
 *   <li>YangStructure - {@link org.yangcentral.yangkit.data.codec.proto.YangStructureDataProtoCodec}</li>
 * </ul>
 *
 * <h2>Codec Modes:</h2>
 * <ul>
 *   <li>{@link org.yangcentral.yangkit.data.codec.proto.ProtoCodecMode#SIMPLE} uses proto3 primitive fields where possible.
 *       For example, {@code decimal64} is emitted as its lexical decimal {@code string} form.</li>
 *   <li>{@link org.yangcentral.yangkit.data.codec.proto.ProtoCodecMode#YGOT} uses the ywrapper messages implemented by this module.
 *       In this mode {@code decimal64} uses {@code .ywrapper.Decimal64Value} with {@code digits} and {@code precision} fields.</li>
 * </ul>
 *
 * <h2>Current anydata Representation:</h2>
 * <p>{@code anydata} payloads are currently carried as JSON text in a generated wrapper message's
 * {@code value} field. During deserialization, payload schema resolution is delegated through the
 * anydata validation context APIs rather than protobuf-native {@code Any} handling.</p>
 * 
 * <h2>Usage Example:</h2>
 * <pre>
 * {@code
 * // Serialize YANG data to protobuf
 * SchemaNode schemaNode = ...;
 * YangData<?> yangData = ...;
 * DynamicMessage protoMessage = YangDataProtoCodec
 *     .getInstance(schemaNode)
 *     .serialize(yangData);
 * 
 * // Deserialize protobuf to YANG data
 * ValidatorResultBuilder builder = new ValidatorResultBuilder();
 * YangData<?> restoredData = YangDataProtoCodec
 *     .getInstance(schemaNode)
 *     .deserialize(protoMessage, builder);
 * }
 * </pre>
 * 
 * @since 1.5.0
 */
package org.yangcentral.yangkit.data.codec.proto;
