package org.yangcentral.yangkit.data.codec.json;

import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.QName;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AttributeCache {

    private  final Map<QName, List<Attribute>> cache = new HashMap<>();

    public  List<Attribute> getAttributeListByQName(QName qName) {
        return cache.get(qName);
    }

    public  void put(QName qName, List<Attribute> attributeList) {
        cache.put(qName, attributeList);
    }

    public void removeByQName(QName qName) {
        cache.remove(qName);
    }

}