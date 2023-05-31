package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.*;

import java.util.ArrayList;
import java.util.List;

public abstract class YangAbstractDataEntry<T extends YangDataEntity> implements YangDataEntity<T> {
    private QName qName;
    private List<Attribute> attributes = new ArrayList<>();

    public YangAbstractDataEntry(QName qName) {
        this.qName = qName;
    }
    @Override
    public QName getQName() {
        return qName;
    }

    @Override
    public void setQName(QName qName) {
        this.qName = qName;
    }

    @Override
    public void update() {
        if(this instanceof YangDataContainer){
            YangDataContainer yangDataContainer = (YangDataContainer) this;
            List<YangData<?>> children = yangDataContainer.getChildren();
            if(children != null){
                for(YangData<?> child: children){
                    if(null == child){
                        continue;
                    }
                    if(this instanceof YangDataDocument){
                        child.getContext().setDocument((YangDataDocument) this);
                    }
                    else {
                        child.getContext().setDocument(((YangData)this).getContext().getDocument());
                    }

                    child.update();
                }
            }
        }
    }

    @Override
    public void addAttribute(Attribute attribute) {
        if (null != getAttribute(attribute.getName())) {
            return;
        }

        attributes.add(attribute);
    }

    @Override
    public Attribute getAttribute(QName qName) {
        for (Attribute attribute : attributes) {
            if (null == attribute) {
                continue;
            }
            if (attribute.getName().equals(qName)) {
                return attribute;
            }
        }
        return null;
    }

    @Override
    public List<Attribute> getAttributes(String name) {
        List<Attribute> candidate = null;
        for (Attribute attribute : attributes) {
            if (null == attribute) {
                continue;
            }
            if (attribute.getName().getLocalName().equals(name)) {
                if (null == candidate) {
                    candidate = new ArrayList<>();
                }
                candidate.add(attribute);
            }
        }
        return candidate;
    }

    @Override
    public void deleteAttribute(QName qName) {
        for (Attribute attribute : attributes) {
            if (null == attribute) {
                continue;
            }
            if (attribute.getName().equals(qName)) {
                attributes.remove(attribute);
                return;
            }
        }
    }

    @Override
    public List<Attribute> getAttributes() {
        return attributes;
    }

    @Override
    public void setAttributes(List list) {
        this.attributes = list;
    }

    @Override
    public T clone() throws CloneNotSupportedException {
        T cloned = (T) super.clone();
        if (null != getAttributes()) {
            for (Attribute attribute : getAttributes()) {
                if (null == attribute) {
                    continue;
                }
                cloned.deleteAttribute(attribute.getName());
                cloned.addAttribute(attribute.clone());
            }
        }
        cloned.update();
        return cloned;
    }

    @Override
    public int compareTo(T o) {
        int result = getQName().compareTo(o.getQName());
        return result;
    }
}
