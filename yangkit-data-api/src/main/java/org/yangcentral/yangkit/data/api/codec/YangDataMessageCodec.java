package org.yangcentral.yangkit.data.api.codec;

import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.model.YangDataMessage;

public interface YangDataMessageCodec<T,M extends YangDataMessage<M>> {
    M deserialize(T document, ValidatorResultBuilder builder);

    T serialize(M yangDataMessage);
}
