package org.yangcentral.yangkit.model.api.restriction;

public interface UInt8 extends YangInteger<Short> {
   Short MAX = 255;
   Short MIN = Short.valueOf((short)0);
}
