package org.yangcentral.yangkit.data.api.model;

import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.base.ValidatePhase;

import java.util.List;

public interface YangDataEntity<E extends YangDataEntity> extends Comparable<E>, Cloneable{
    QName getQName();
    void setQName(QName qName);

    void update();

    void addAttribute(Attribute attribute);

    Attribute getAttribute(QName qName);

    List<Attribute> getAttributes(String name);

    void deleteAttribute(QName qName);

    List<Attribute> getAttributes();

    void setAttributes(List<Attribute> attributes);


    ValidatorResult validate();

    E clone() throws CloneNotSupportedException;

    List<YangDataCompareResult> compare(E other);
}
