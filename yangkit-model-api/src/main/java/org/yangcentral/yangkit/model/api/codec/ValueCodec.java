package org.yangcentral.yangkit.model.api.codec;

import org.yangcentral.yangkit.model.api.restriction.Restriction;

public interface ValueCodec<D, T> {
   D deserialize(Restriction<D> restriction, T input) throws YangCodecException;

   T serialize(Restriction<D> restriction, D output) throws YangCodecException;
}
