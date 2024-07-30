package org.yangcentral.yangkit.data.impl.util;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.Predict;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.XPathStep;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.impl.model.YangCompareResultImpl;
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

    public static XPathStep translate2Step(YangData<?> yangData){
        XPathStep step = new XPathStep(yangData.getQName());
        if(yangData.getIdentifier()  instanceof ListIdentifier){
            ListIdentifier listIdentifier = (ListIdentifier) yangData.getIdentifier();
            List<LeafData> keys = listIdentifier.getKeys();
            for(LeafData key: keys){
                Predict predict = new Predict(key.getQName(),key.getStringValue());
                step.addPredict(predict);
            }
        } else if( yangData.getIdentifier() instanceof LeafListIdentifier){
            LeafListIdentifier leafListIdentifier = (LeafListIdentifier) yangData.getIdentifier();
            Predict predict = new Predict(leafListIdentifier.getQName(), leafListIdentifier.getValue());
            step.addPredict(predict);
        }
        return step;
    }

    public static YangData<?> search(YangDataDocument yangDataDocument, AbsolutePath path){
        List<XPathStep> steps = path.getSteps();
        if(steps.isEmpty()){
            return null;
        }
        YangDataContainer parent = yangDataDocument;
        YangData<?> matched = null;
        for(XPathStep step: steps){
            if(null == parent){
                matched = null;
                break;
            }
            QName stepName = step.getStep();
            List<YangData<?>> children = parent.getDataChildren(stepName);
            if(children.isEmpty()){
                return null;
            }
            matched = null;
            for(YangData<?> child:children){
                if(translate2Step(child).equals(step)){
                    matched = child;
                    break;
                }
            }
            if(null == matched){
                return null;
            }
            if(matched instanceof YangDataContainer){
                parent = (YangDataContainer) matched;
            } else {
                parent = null;
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

    public static boolean onlyConfig(YangData<?> yangData){
        if(null == yangData){
            return false;
        }
        if(null == yangData.getContext().getDocument()){
            return false;
        }
        return yangData.getContext().getDocument().onlyConfig();
    }

    public static boolean onlyConfig(YangDataContainer yangDataContainer){
        if(null == yangDataContainer){
            return false;
        }
        if(yangDataContainer instanceof YangDataDocument){
            YangDataDocument yangDataDocument = (YangDataDocument) yangDataContainer;
            return yangDataDocument.onlyConfig();
        } else {
            YangData yangData = (YangData) yangDataContainer;
            return onlyConfig(yangData);
        }
    }
    public static List<YangDataCompareResult> compare(YangData base, YangData another){
        List<YangDataCompareResult> results = new ArrayList<>();
        if((base == null) && (another == null)) {
            return results;
        }
        if(base == null) {
            results.add(new YangCompareResultImpl(another.getPath(),DifferenceType.NEW,another));
            return results;
        }
        if(another == null) {
            results.add(new YangCompareResultImpl(base.getPath(),DifferenceType.NONE,base));
            return results;
        }
        return base.compare(another);
    }
}
