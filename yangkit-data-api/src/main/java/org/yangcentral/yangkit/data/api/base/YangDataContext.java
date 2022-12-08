package org.yangcentral.yangkit.data.api.base;

import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;

public class YangDataContext {
    private YangDataContainer parent;
    private YangDataDocument document;

    public YangDataContext() {
    }

    public YangDataContainer getParent() {
        return parent;
    }

    public YangDataContainer getDataParent(){
        if(parent == null){
            return null;
        }
        if(!(parent instanceof YangData)){
            return null;
        }
        YangData yangData = (YangData) parent;
        if(yangData.isVirtual()){
            return yangData.getContext().getDataParent();
        }
        return parent;
    }

    public void setParent(YangDataContainer parent) {
        this.parent = parent;
    }

    public YangDataDocument getDocument() {
        if(document != null){
            return document;
        }
        YangDataContainer parentData = this.getParent();
        if(parentData == null){
            return null;
        }
        if(parentData instanceof YangDataDocument){
            return (YangDataDocument) parentData;
        }

        return ((YangData)parentData).getContext().getDocument();
    }

    public void setDocument(YangDataDocument document) {
        this.document = document;
    }
}
