package org.yangcentral.yangkit.model.api.codec;

import org.yangcentral.yangkit.model.api.restriction.Restriction;

public interface ValueCodec<D, T> {
   D deserialize(Restriction<D> var1, T var2) throws YangCodecException;

   T serialize(Restriction<D> var1, D var2) throws YangCodecException;
}
