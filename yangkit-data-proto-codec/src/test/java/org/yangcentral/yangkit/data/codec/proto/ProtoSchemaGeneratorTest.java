package org.yangcentral.yangkit.data.codec.proto;

import com.google.protobuf.DescriptorProtos;
import org.junit.Assert;
import org.junit.Test;
import org.yangcentral.yangkit.model.api.stmt.Type;

/**
 * Integration tests for ProtoSchemaGenerator.
 */
public class ProtoSchemaGeneratorTest {

    @Test
    public void testProtoSchemaGeneratorCreation() {
        // Test that we can create a ProtoSchemaGenerator instance
        ProtoSchemaGenerator generator = new ProtoSchemaGenerator();
        Assert.assertNotNull(generator);
    }

    @Test
    public void testGenerateFileDescriptorForNullModule() {
        ProtoSchemaGenerator generator = new ProtoSchemaGenerator();
        
        // Test with null module
        DescriptorProtos.FileDescriptorProto fileDescriptor = 
            generator.generateFileDescriptor(null);
        
        Assert.assertNull(fileDescriptor);
    }

    @Test
    public void testGenerateMessageForNullNode() {
        ProtoSchemaGenerator generator = new ProtoSchemaGenerator();
        
        // Test with null data node
        DescriptorProtos.DescriptorProto message =
            generator.generateMessage(null, "");

        Assert.assertNull(message);
    }

    @Test
    public void testGeneratorCanBeCreatedTwice() {
        // ProtoSchemaGenerator is stateless per-mode; creating multiple instances is fine
        ProtoSchemaGenerator gen1 = new ProtoSchemaGenerator();
        ProtoSchemaGenerator gen2 = new ProtoSchemaGenerator();
        Assert.assertNotNull(gen1);
        Assert.assertNotNull(gen2);
    }

    @Test
    public void testTypeMapperWithNullType() {
        // Test null type mapping
        DescriptorProtos.FieldDescriptorProto.Type type = 
            YangProtoTypeMapper.getProtoType(null);
        Assert.assertEquals(DescriptorProtos.FieldDescriptorProto.Type.TYPE_STRING, type);
    }

    @Test
    public void testValueConversionWithNulls() {
        // Test null value conversion
        Object nullResult = YangProtoTypeMapper.convertToProtoValue(null, null);
        Assert.assertNull(nullResult);

        nullResult = YangProtoTypeMapper.convertToYangValue(null, null);
        Assert.assertNull(nullResult);
    }

    @Test
    public void testValueConversionWithPrimitives() {
        // Test integer conversion
        Integer intValue = 42;
        Object protoInt = YangProtoTypeMapper.convertToProtoValue(intValue, null);
        Assert.assertEquals(Integer.valueOf(42), protoInt);

        Object yangInt = YangProtoTypeMapper.convertToYangValue(protoInt, null);
        Assert.assertEquals(Integer.valueOf(42), yangInt);

        // Test boolean conversion
        Boolean boolValue = true;
        Object protoBool = YangProtoTypeMapper.convertToProtoValue(boolValue, null);
        Assert.assertEquals(Boolean.TRUE, protoBool);

        Object yangBool = YangProtoTypeMapper.convertToYangValue(protoBool, null);
        Assert.assertEquals(Boolean.TRUE, yangBool);

        // Test string conversion
        String strValue = "test";
        Object protoStr = YangProtoTypeMapper.convertToProtoValue(strValue, null);
        Assert.assertEquals("test", protoStr);

        Object yangStr = YangProtoTypeMapper.convertToYangValue(protoStr, null);
        Assert.assertEquals("test", yangStr);
    }

    @Test
    public void testValueConversionWithNumbers() {
        // Test long conversion
        Long longValue = 1234567890L;
        Object protoLong = YangProtoTypeMapper.convertToProtoValue(longValue, null);
        Assert.assertEquals(Long.valueOf(1234567890L), protoLong);

        // Test double conversion
        Double doubleValue = 3.14159;
        Object protoDouble = YangProtoTypeMapper.convertToProtoValue(doubleValue, null);
        Assert.assertEquals(Double.valueOf(3.14159), protoDouble);
    }

    @Test
    public void testProtoCacheOperations() {
        ProtoCache cache = ProtoCache.getInstance();
        cache.clear();

        // Test basic put/get
        String key = "test-key-1";
        String value = "test-value-1";
        cache.put(key, value);
        
        String retrieved = cache.get(key);
        Assert.assertEquals(value, retrieved);

        // Test get non-existent key
        String nonExistent = cache.get("non-existent-key");
        Assert.assertNull(nonExistent);

        // Test remove
        cache.remove(key);
        String afterRemove = cache.get(key);
        Assert.assertNull(afterRemove);

        // Test size
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        Assert.assertEquals(2, cache.size());

        // Test clear
        cache.clear();
        Assert.assertEquals(0, cache.size());
    }

    @Test
    public void testProtoCacheStats() {
        ProtoCache cache = ProtoCache.getInstance();
        cache.clear();

        // Add some entries
        cache.put("stat-key-1", "value1");
        cache.put("stat-key-2", "value2");

        // Get stats
        String stats = cache.getStats();
        Assert.assertNotNull(stats);
        Assert.assertTrue(stats.contains("size="));
        
        // Clean up
        cache.clear();
    }
}
