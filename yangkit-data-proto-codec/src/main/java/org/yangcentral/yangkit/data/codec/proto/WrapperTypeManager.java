package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.DescriptorProtos;
import com.google.protobuf.Descriptors;

/**
 * Manages the ywrapper protobuf types used in YGOT codec mode.
 *
 * <p>The ywrapper package provides wrapper messages for scalar types,
 * allowing them to be used where messages are required (e.g., in oneof
 * blocks or when explicit presence is needed).</p>
 *
 * <p>This class builds and caches the FileDescriptor for ywrapper.proto,
 * which defines the following wrapper messages:</p>
 * <ul>
 *   <li>{@code IntValue} - wrapper for signed integer scalars with {@code sint64 value = 1}</li>
 *   <li>{@code UintValue} - wrapper for unsigned integer scalars with {@code uint64 value = 1}</li>
 *   <li>{@code BoolValue} - wrapper for bool</li>
 *   <li>{@code StringValue} - wrapper for string</li>
 *   <li>{@code Decimal64Value} - wrapper for decimal64 with {@code uint64 digits = 1} and {@code uint32 precision = 2}</li>
 *   <li>{@code BytesValue} - wrapper for bytes</li>
 * </ul>
 */
public class WrapperTypeManager {

    /** The protobuf package name for ywrapper types. */
    public static final String YWRAPPER_PACKAGE = "ywrapper";

    /** The ywrapper.proto file descriptor path. */
    public static final String YWRAPPER_FILE = "ywrapper/ywrapper.proto";

    // Wrapper message type names
    /** Message type name for IntValue wrapper. */
    public static final String INT_VALUE = "IntValue";
    /** Message type name for UintValue wrapper. */
    public static final String UINT_VALUE = "UintValue";
    /** Message type name for BoolValue wrapper. */
    public static final String BOOL_VALUE = "BoolValue";
    /** Message type name for StringValue wrapper. */
    public static final String STRING_VALUE = "StringValue";
    /** Message type name for Decimal64Value wrapper. */
    public static final String DECIMAL64_VALUE = "Decimal64Value";
    /** Message type name for BytesValue wrapper. */
    public static final String BYTES_VALUE = "BytesValue";

    private static volatile WrapperTypeManager instance;

    private Descriptors.FileDescriptor fileDescriptor;

    private WrapperTypeManager() {
        buildFileDescriptor();
    }

    /**
     * Returns the singleton instance of WrapperTypeManager.
     *
     * @return the WrapperTypeManager instance
     */
    public static WrapperTypeManager getInstance() {
        if (instance == null) {
            synchronized (WrapperTypeManager.class) {
                if (instance == null) {
                    instance = new WrapperTypeManager();
                }
            }
        }
        return instance;
    }

    /**
     * Returns the FileDescriptor for ywrapper.proto.
     *
     * @return the file descriptor
     */
    public Descriptors.FileDescriptor getFileDescriptor() {
        return fileDescriptor;
    }

    /**
     * Returns the Descriptor for a wrapper message type.
     *
     * @param messageName the name of the wrapper message (e.g., "StringValue", "IntValue")
     * @return the Descriptor for the specified message type
     * @throws IllegalArgumentException if the message name is not found
     */
    public Descriptors.Descriptor getDescriptor(String messageName) {
        if (fileDescriptor == null) {
            throw new IllegalStateException("FileDescriptor not initialized");
        }
        Descriptors.Descriptor descriptor = fileDescriptor.findMessageTypeByName(messageName);
        if (descriptor == null) {
            throw new IllegalArgumentException("Unknown wrapper message type: " + messageName);
        }
        return descriptor;
    }

    /**
     * Builds the FileDescriptorProto for ywrapper.proto and compiles it.
     */
    private void buildFileDescriptor() {
        try {
            // Build ywrapper.proto file descriptor
            DescriptorProtos.FileDescriptorProto.Builder fileBuilder =
                    DescriptorProtos.FileDescriptorProto.newBuilder()
                            .setName("ywrapper/ywrapper.proto")
                            .setPackage(YWRAPPER_PACKAGE)
                            .setSyntax("proto3");

            // Add wrapper message types
            fileBuilder.addMessageType(buildMessage(INT_VALUE, "value", 
                    DescriptorProtos.FieldDescriptorProto.Type.TYPE_SINT64));
            fileBuilder.addMessageType(buildMessage(UINT_VALUE, "value",
                    DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64));
            fileBuilder.addMessageType(buildMessage(BOOL_VALUE, "value",
                    DescriptorProtos.FieldDescriptorProto.Type.TYPE_BOOL));
            fileBuilder.addMessageType(buildMessage(STRING_VALUE, "value", 
                    DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING));
            fileBuilder.addMessageType(buildMessage(BYTES_VALUE, "value", 
                    DescriptorProtos.FieldDescriptorProto.Type.TYPE_BYTES));
            
            // Decimal64Value has two fields: digits (uint64) and precision (uint32)
            fileBuilder.addMessageType(buildDecimal64Message());

            // Build the file descriptor
            fileDescriptor = Descriptors.FileDescriptor.buildFrom(
                    fileBuilder.build(),
                    new Descriptors.FileDescriptor[0]
            );
        } catch (Descriptors.DescriptorValidationException e) {
            throw new RuntimeException("Failed to build ywrapper file descriptor: " + e.getMessage(), e);
        }
    }

    /**
     * Builds a simple wrapper message with a single "value" field.
     *
     * @param messageName the name of the message
     * @param fieldName   the name of the value field
     * @param fieldType   the protobuf field type
     * @return the DescriptorProto for the wrapper message
     */
    private DescriptorProtos.DescriptorProto buildMessage(
            String messageName, String fieldName, 
            DescriptorProtos.FieldDescriptorProto.Type fieldType) {
        
        DescriptorProtos.DescriptorProto.Builder msgBuilder =
                DescriptorProtos.DescriptorProto.newBuilder()
                        .setName(messageName);

        DescriptorProtos.FieldDescriptorProto.Builder fieldBuilder =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName(fieldName)
                        .setNumber(1)
                        .setType(fieldType)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);

        msgBuilder.addField(fieldBuilder);
        return msgBuilder.build();
    }

    /**
     * Builds the Decimal64Value message with digits and precision fields.
     *
     * @return the DescriptorProto for Decimal64Value
     */
    private DescriptorProtos.DescriptorProto buildDecimal64Message() {
        DescriptorProtos.DescriptorProto.Builder msgBuilder =
                DescriptorProtos.DescriptorProto.newBuilder()
                        .setName(DECIMAL64_VALUE);

        // Field 1: digits (uint64)
        DescriptorProtos.FieldDescriptorProto.Builder digitsField =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName("digits")
                        .setNumber(1)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT64)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);

        // Field 2: precision (uint32)
        DescriptorProtos.FieldDescriptorProto.Builder precisionField =
                DescriptorProtos.FieldDescriptorProto.newBuilder()
                        .setName("precision")
                        .setNumber(2)
                        .setType(DescriptorProtos.FieldDescriptorProto.Type.TYPE_UINT32)
                        .setLabel(DescriptorProtos.FieldDescriptorProto.Label.LABEL_OPTIONAL);

        msgBuilder.addField(digitsField).addField(precisionField);
        return msgBuilder.build();
    }
}
