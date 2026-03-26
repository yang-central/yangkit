package org.yangcentral.yangkit.data.codec.proto;

import org.junit.Assert;
import org.junit.Test;

/**
 * Simple unit tests for ProtoCodecUtil.
 */
public class SimpleProtoCodecTest {

    @Test
    public void testConvertYangValueToProto() {
        // Test string conversion
        String stringValue = "test";
        Object protoValue = ProtoCodecUtil.convertYangValueToProto(stringValue, null);
        Assert.assertEquals("test", protoValue);

        // Test integer conversion (without type info, preserves Number type)
        Integer intValue = 42;
        Object protoIntValue = ProtoCodecUtil.convertYangValueToProto(intValue, null);
        Assert.assertEquals(Integer.valueOf(42), protoIntValue);

        // Test boolean conversion
        Boolean boolValue = true;
        Object protoBoolValue = ProtoCodecUtil.convertYangValueToProto(boolValue, null);
        Assert.assertEquals(Boolean.TRUE, protoBoolValue);
    }

    @Test
    public void testConvertProtoValueToYang() {
        // Test string conversion
        String protoValue = "test";
        Object yangValue = ProtoCodecUtil.convertProtoValueToYang(protoValue, null);
        Assert.assertEquals("test", yangValue);

        // Test integer conversion (without type info, preserves Number type)
        Integer protoIntValue = 42;
        Object yangIntValue = ProtoCodecUtil.convertProtoValueToYang(protoIntValue, null);
        Assert.assertEquals(Integer.valueOf(42), yangIntValue);

        // Test boolean conversion
        Boolean protoBoolValue = true;
        Object yangBoolValue = ProtoCodecUtil.convertProtoValueToYang(protoBoolValue, null);
        Assert.assertEquals(Boolean.TRUE, yangBoolValue);
    }

    @Test
    public void testCacheMechanism() {
        ProtoCache cache = ProtoCache.getInstance();
        
        // Test put and get
        cache.put("test-key", "test-value");
        String value = cache.get("test-key");
        Assert.assertEquals("test-value", value);
        
        // Test remove
        cache.remove("test-key");
        String removedValue = cache.get("test-key");
        Assert.assertNull(removedValue);
        
        // Test clear
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        cache.clear();
        Assert.assertEquals(0, cache.size());
    }

    @Test
    public void testCacheStats() {
        ProtoCache cache = ProtoCache.getInstance();
        cache.clear();
        
        cache.put("key1", "value1");
        cache.put("key2", "value2");
        
        int size = cache.size();
        Assert.assertEquals(2, size);
        
        String stats = cache.getStats();
        Assert.assertNotNull(stats);
        Assert.assertTrue(stats.contains("size="));
    }
}
