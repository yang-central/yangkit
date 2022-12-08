package org.yangcentral.yangkit.data.impl.util;

import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

import java.util.List;

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
}
