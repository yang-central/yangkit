package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.YangDataValue;
import org.yangcentral.yangkit.model.api.codec.StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.ValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.impl.codec.StringValueCodecFactory;

public class YangDataValueStringImpl<D> implements YangDataValue<D,String> {
    Restriction<D> restriction;
    String source;
    StringValueCodec<D> codec;

    public YangDataValueStringImpl(Restriction<D> restriction, String source) {
        this.restriction = restriction;
        this.source = source;
        codec = (StringValueCodec<D>) StringValueCodecFactory.getInstance().getStringValueCodec(restriction);

    }

    @Override
    public Restriction<D> getRestriction() {
        return restriction;
    }

    @Override
    public D getValue() throws YangCodecException {
        return codec.deserialize(restriction,source);
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getStringValue() {
        return source;
    }
}
