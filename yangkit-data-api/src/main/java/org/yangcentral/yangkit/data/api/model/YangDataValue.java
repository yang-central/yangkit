package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;

public interface YangDataValue<D,S> {
    Restriction<D> getRestriction();
    D getValue() throws YangCodecException;
    S getSource();
    String getStringValue();

}
