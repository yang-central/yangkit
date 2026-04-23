package org.yangcentral.yangkit.data.codec.json;

import org.yangcentral.yangkit.model.api.codec.StringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.restriction.Union;
import org.yangcentral.yangkit.model.api.stmt.Type;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.impl.codec.UnionStringValueCodecImpl;

import java.util.List;

/**
 * JSON-aware Union string value codec.
 * Uses {@link JsonStringValueCodecFactory} for sub-type codec resolution,
 * ensuring identityref sub-types use the JSON format (module-name:local-name)
 * rather than the XML format (prefix:local-name).
 */
public class UnionJsonStringValueCodecImpl extends UnionStringValueCodecImpl {

    public UnionJsonStringValueCodecImpl(TypedDataNode schemaNode) {
        super(schemaNode);
    }

    @Override
    public Object deserialize(Restriction<Object> restriction, String input) throws YangCodecException {
        Union union = (Union) restriction;
        if (union.getDerived() != null) {
            return this.deserialize((Restriction<Object>) union.getDerived().getType().getRestriction(), input);
        }
        List<Type> types = union.getTypes();
        Object result = null;
        for (Type type : types) {
            StringValueCodec<?> codec = JsonStringValueCodecFactory.getInstance()
                    .getStringValueCodec(getSchemaNode(), type.getRestriction());
            try {
                result = ((StringValueCodec<Object>) codec).deserialize(
                        (Restriction<Object>) type.getRestriction(), input);
                break;
            } catch (Exception ignored) {
                // try next sub-type
            }
        }
        if (result == null) {
            throw new YangCodecException("invalid value for union: " + input);
        }
        return result;
    }

    @Override
    public String serialize(Restriction<Object> restriction, Object output) throws YangCodecException {
        if (!restriction.evaluate(output)) {
            throw new YangCodecException("invalid value for union: " + output);
        }
        List<Type> types = ((Union) restriction).getActualTypes();
        for (Type type : types) {
            StringValueCodec<?> codec = JsonStringValueCodecFactory.getInstance()
                    .getStringValueCodec(getSchemaNode(), type.getRestriction());
            try {
                String s = ((StringValueCodec<Object>) codec).serialize(
                        (Restriction<Object>) type.getRestriction(), output);
                if (s != null) {
                    return s;
                }
            } catch (Exception ignored) {
                // try next sub-type
            }
        }
        throw new YangCodecException("cannot serialize union value: " + output);
    }
}

