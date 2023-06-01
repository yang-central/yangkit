package org.yangcentral.yangkit.data.impl.operation;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.Attribute;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.api.operation.DataChangeNotifier;
import org.yangcentral.yangkit.data.api.operation.DataChangeType;
import org.yangcentral.yangkit.data.api.operation.YangDataOperator;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

public class YangDataOperatorImpl implements YangDataOperator {
    private YangDataContainer operatedData;

    private DataChangeNotifier dataChangeNotifier;

    public YangDataOperatorImpl(YangDataContainer operatedData) {
        this.operatedData = operatedData;
    }

    @Override
    public YangDataContainer getOperatedData() {
        return operatedData;
    }

    public DataChangeNotifier getDataChangeNotifier() {
        return dataChangeNotifier;
    }

    public void setDataChangeNotifier(DataChangeNotifier dataChangeNotifier) {
        this.dataChangeNotifier = dataChangeNotifier;
    }

    private AbsolutePath getOperateDataPath(){
        if(operatedData instanceof YangDataDocument){
            return new AbsolutePath();
        }
        YangData<?> yangData = (YangData<?>) operatedData;
        return yangData.getPath();
    }
    @Override
    public void create(YangData<? extends DataNode> node, boolean autoDelete) throws YangDataException {
        YangData<? extends DataNode> originalNode = (YangData<? extends DataNode>) operatedData.getDataChild(node.getIdentifier());
        if(originalNode != null){
            throw new YangDataException(ErrorTag.DATA_EXISTS,originalNode.getPath(),
                    new ErrorMessage("data exists"));
        }
        node.detach();
        operatedData.addDataChild(node,autoDelete);
        node.update();
        if(null != dataChangeNotifier){
            dataChangeNotifier.notify(getOperateDataPath(), DataChangeType.NEW,null,node);
        }
    }

    @Override
    public void merge(YangData<? extends DataNode> node, boolean autoDelete) throws YangDataException {
        if(node == null){
            return;
        }
        YangData<? extends DataNode> originalNode = (YangData<? extends DataNode>) operatedData.getDataChild(node.getIdentifier());
        if(null == originalNode){
            //new
            create(node,autoDelete);
            return;
        }
        //merge attributes
        for(Attribute attribute:node.getAttributes()){
            Attribute originalAttr = originalNode.getAttribute(attribute.getName());
            if(originalAttr == null){
                originalNode.addAttribute(attribute);
            }
            else {
                originalAttr.setValue(attribute.getValue());
            }
        }
        if(node instanceof LeafData){
            //if node is leaf, replace it's value with new
            LeafData originalLeaf = (LeafData) originalNode;
            originalLeaf.setValue(((LeafData) node).getValue());
            if(null != dataChangeNotifier){
                dataChangeNotifier.notify(originalLeaf.getPath(),DataChangeType.UPDATE,
                        originalLeaf,node);
            }
        } else if (node instanceof YangDataContainer){
            YangDataContainer originalContainer = (YangDataContainer) originalNode;
            YangDataContainer candidateContainer = (YangDataContainer) node;
            YangDataOperatorImpl yangDataOperator = new YangDataOperatorImpl(originalContainer);
            if(dataChangeNotifier != null){
                yangDataOperator.setDataChangeNotifier(dataChangeNotifier);
            }
            for(YangData<?> candidateChild:candidateContainer.getDataChildren()){
                yangDataOperator.merge((YangData<? extends DataNode>) candidateChild,autoDelete);
            }
        }
    }

    @Override
    public void replace(YangData<? extends DataNode> node, boolean autoDelete) throws YangDataException {
        if(null == node){
            return;
        }
        YangData<? extends DataNode> originalNode = (YangData<? extends DataNode>) operatedData.getDataChild(node.getIdentifier());
        if(originalNode != null){
            delete(originalNode.getIdentifier());
        }
        create(node, autoDelete);
    }

    @Override
    public void delete(DataIdentifier identifier) {
        YangData<? extends DataNode> originalNode = (YangData<? extends DataNode>) operatedData.getDataChild(identifier);
        if(null != originalNode){
            operatedData.removeDataChild(identifier);
            if(dataChangeNotifier != null){
                dataChangeNotifier.notify(getOperateDataPath(),DataChangeType.DELETE,originalNode,null);
            }
        }
    }
}
