package org.yangcentral.yangkit.data.codec.json;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.model.api.codec.LeafRefStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.LeafRef;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.impl.codec.ComplexStringValueCodecImpl;

/**
 * JSON-aware LeafRef string value codec.
 * Delegates to the referenced node's codec resolved via {@link JsonStringValueCodecFactory},
 * so that if the referenced node is identityref (or union containing identityref),
 * the JSON format (module-name:local-name) is used instead of the XML format.
 */
public class LeafRefJsonStringValueCodecImpl extends ComplexStringValueCodecImpl<Object>
        implements LeafRefStringValueCodec {

    public LeafRefJsonStringValueCodecImpl(TypedDataNode schemaNode) {
        super(schemaNode);
    }

    @Override
    public Object deserialize(Restriction<Object> restriction, String input) throws YangCodecException {
        TypedDataNode referencedNode = ((LeafRef) restriction).getReferencedNode();
        if (referencedNode == null) {
            throw new YangCodecException(ErrorCode.REFERENCE_NODE_NOT_FOUND.getFieldName());
        }
        StringValueCodec<Object> codec = (StringValueCodec<Object>)
                JsonStringValueCodecFactory.getInstance().getStringValueCodec(referencedNode);
        return codec.deserialize(
                (Restriction<Object>) referencedNode.getType().getRestriction(), input);
    }

    @Override
    public String serialize(Restriction<Object> restriction, Object output) throws YangCodecException {
        TypedDataNode referencedNode = ((LeafRef) restriction).getReferencedNode();
        if (referencedNode == null) {
            throw new YangCodecException(ErrorCode.REFERENCE_NODE_NOT_FOUND.getFieldName());
        }
        StringValueCodec<Object> codec = (StringValueCodec<Object>)
                JsonStringValueCodecFactory.getInstance().getStringValueCodec(referencedNode);
        return codec.serialize(
                (Restriction<Object>) referencedNode.getType().getRestriction(), output);
    }
}

