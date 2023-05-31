package org.yangcentral.yangkit.data.impl.builder;

import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.impl.model.*;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.api.stmt.ext.AugmentStructure;

import java.util.List;

public class YangDataBuilder {
    public static <T> YangData<?> getYangData(SchemaNode schemaNode, T  value) {
        if (schemaNode instanceof Anydata){
            return new AnyDataDataImpl((Anydata) schemaNode);
        } else if (schemaNode instanceof Anyxml){
            return new AnyXmlDataImpl((Anyxml) schemaNode);
        } else if (schemaNode instanceof Augment){
            return new AugmentDataImpl((Augment) schemaNode);
        } else if (schemaNode instanceof AugmentStructure){
            return new AugmentStructureDataImpl((AugmentStructure) schemaNode);
        } else if (schemaNode instanceof Case){
            return new CaseDataImpl((Case) schemaNode);
        } else if (schemaNode instanceof Choice){
            return new ChoiceDataImpl((Choice) schemaNode);
        } else if(schemaNode instanceof Container){
            return new ContainerDataImpl((Container) schemaNode);
        } else if (schemaNode instanceof Leaf){
            LeafData leafData = new LeafDataImpl((Leaf) schemaNode);
            if(value != null && (value instanceof YangDataValue<?,?>)){
                leafData.setValue((YangDataValue<?, ?>) value);
            }
            return leafData;
        } else if (schemaNode instanceof LeafList){
            if(value instanceof YangDataValue<?,?>){
                LeafListData leafListData = new LeafListDataImpl((LeafList) schemaNode,
                        (YangDataValue<?, ?>) value);
                return leafListData;
            }
            return new LeafListDataImpl((LeafList) schemaNode,null);
        } else if (schemaNode instanceof YangList){
            ListData listData = new ListDataImpl((YangList) schemaNode, (List<LeafData>) value);
            return listData;
        } else if (schemaNode instanceof Uses){
            return new UsesDataImpl((Uses) schemaNode);
        }
        return null;
    }
}
