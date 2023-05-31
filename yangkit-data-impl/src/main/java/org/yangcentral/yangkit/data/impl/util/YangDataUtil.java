package org.yangcentral.yangkit.data.impl.util;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.VirtualSchemaNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class YangDataUtil {
    public static Boolean getXPathBooleanValue(Object obj) {
        if(null == obj){
            return false;
        }
        if (obj instanceof List) {
            List list = (List)obj;
            if (list.size() == 0) {
                return Boolean.FALSE;
            }

            obj = list.get(0);
        }

        if (obj instanceof Boolean) {
            return (Boolean)obj;
        } else if (obj instanceof Number) {
            double d = ((Number)obj).doubleValue();
            return d != 0.0D && !Double.isNaN(d) ? Boolean.TRUE : Boolean.FALSE;
        } else if (obj instanceof String) {
            return ((String)obj).length() > 0 ? Boolean.TRUE : Boolean.FALSE;
        } else {
            return obj != null ? Boolean.TRUE : Boolean.FALSE;
        }
    }
    public static Object getXpathContextData(YangData<?> curData) {
        if (null == curData) {
            return null;
        }
        if(curData.getSchemaNode() instanceof DataNode){
            return curData;
        }
        if(curData.getContext().getParent() instanceof YangDataDocument){
            return curData.getContext().getDocument();
        }
        return getXpathContextData((YangData<?>) curData.getContext().getParent());

    }

    public static List<YangData<?>> search(YangDataContainer yangDataContainer,QName qName) {
        List<YangData<?>> matched = new ArrayList<>();
        for(YangData<?> child:yangDataContainer.getChildren()){
            if((child instanceof AugmentData) || (child instanceof AugmentStructureData)
                    || (child instanceof UsesData)){
                YangDataContainer childContainer = (YangDataContainer) child;
                matched.addAll(search(childContainer,qName));
            } else {
                if(child.getIdentifier().getQName().equals(qName)){
                    matched.add(child);
                }
            }
        }
        return matched;
    }

    public static List<YangData<?>> search(YangDataContainer yangDataContainer, SchemaPath.Descendant path){
        if(null == yangDataContainer || null == path){
            return null;
        }
        List<QName> steps = path.getPath();
        List<YangData<?>> matched = new ArrayList<>();
        for(QName step:steps){
            if(matched.isEmpty()){
                matched = search(yangDataContainer,step);
            }
            else {
                List<YangData<?>> nextMatched = new ArrayList<>();
                for(YangData<?> matchedItem:matched){
                    if(matchedItem instanceof YangDataContainer){
                        nextMatched.addAll(search((YangDataContainer) matchedItem,step));
                    }
                }
                matched = nextMatched;
            }

            if(matched.isEmpty()){
                //no matched data, return
                return matched;
            }
        }
        return matched;
    }

    public static boolean equals(List<YangData<?>> src, List<YangData<?>> desc){
        if(null == src || null == desc){
            return false;
        }
        if(src.size() != desc.size()){
            return false;
        }
        int length = src.size();
        for(int i =0; i< length;i++){
            YangData<?> srcEntry = src.get(i);
            YangData<?> descEntry = desc.get(i);
            if(!srcEntry.equals(descEntry)){
                return false;
            }
        }
        return true;
    }

}
