package org.yangcentral.yangkit.data.codec.json;

import org.yangcentral.yangkit.model.api.codec.StringValueCodec;
import org.yangcentral.yangkit.model.api.restriction.*;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.impl.codec.*;

public class JsonStringValueCodecFactory extends StringValueCodecFactory{
    private static final JsonStringValueCodecFactory ourInstance = new JsonStringValueCodecFactory();

    public static JsonStringValueCodecFactory getInstance() {
        return ourInstance;
    }

    private JsonStringValueCodecFactory() {
        super();
    }

    public StringValueCodec<?> getStringValueCodec(TypedDataNode dataNode) {

        Restriction restriction = dataNode.getType().getRestriction();
        return this.getStringValueCodec(dataNode, restriction);

    }

    public StringValueCodec<?> getStringValueCodec(TypedDataNode dataNode, Restriction restriction) {
        if (restriction instanceof LeafRef) {
            return new LeafRefJsonStringValueCodecImpl(dataNode);
        } else if (restriction instanceof IdentityRef) {
            return new IdentityRefJsonCodec(dataNode);
        } else if (restriction instanceof InstanceIdentifier) {
            return new InstanceIdentifierStringValueCodecImpl(dataNode);
        } else if (restriction instanceof Union) {
            return new UnionJsonStringValueCodecImpl(dataNode);
        } else {
            return this.getStringValueCodec(restriction);
        }
    }


}