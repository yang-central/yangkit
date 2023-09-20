package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.data.api.model.YangDataValue;
import org.yangcentral.yangkit.model.api.codec.StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.impl.codec.StringValueCodecFactory;

public class YangDataValueStringImpl<D> implements YangDataValue<D,String> {
    Restriction<D> restriction;
    String source;
    D value;
    StringValueCodec<D> codec;

    public YangDataValueStringImpl(TypedDataNode node, String source) {
        this.restriction = node.getType().getRestriction();
        this.source = source;
        codec = (StringValueCodec<D>) StringValueCodecFactory.getInstance().getStringValueCodec(node,restriction);

    }
    public YangDataValueStringImpl(TypedDataNode node, String source, StringValueCodec<D> codec) {
        this.restriction = node.getType().getRestriction();
        this.source = source;
        this.codec = codec;
    }

    @Override
    public Restriction<D> getRestriction() {
        return restriction;
    }

    @Override
    public D getValue() throws YangCodecException {
        if(value == null) {
            value = codec.deserialize(restriction,source);
        }

        return value;
    }

    @Override
    public String getSource() {
        return source;
    }

    @Override
    public String getStringValue(StringValueCodec<D> codec) throws YangCodecException {
        return codec.serialize(restriction,getValue());
    }

    @Override
    public String getStringValue() throws YangCodecException {
        return codec.serialize(restriction,getValue());
    }


    @Override
    public boolean equals(Object oValue){
        if(oValue == null){
            return false;
        }
        if(this.getClass() != oValue.getClass()){
            return false;
        }
        YangDataValue oDataValue = (YangDataValue) oValue;

        try {
            D val =  getValue();
            Object oVal = oDataValue.getValue();
            return val.equals(oVal);
        } catch (YangCodecException e) {
            throw new RuntimeException(e);
        }
    }
}
