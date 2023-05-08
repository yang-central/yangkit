package org.yangcentral.yangkit.data.impl.model;

import com.google.common.collect.Lists;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.DataIdentifier;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.operation.YangDataOperator;
import org.yangcentral.yangkit.data.impl.operation.YangDataOperatorImpl;
import org.yangcentral.yangkit.data.impl.util.YangDataUtil;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNode;
import org.yangcentral.yangkit.model.api.stmt.SchemaNodeContainer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class YangAbstractDataContainer implements YangDataContainer {
    private YangDataContainer self;
    private SchemaNodeContainer schemaNodeContainer;
    private Map<DataIdentifier,YangData<?>> children = new ConcurrentHashMap<>();


    public YangAbstractDataContainer(YangDataContainer yangDataContainer) {
        this.self = yangDataContainer;
        if(self instanceof YangDataDocument){
            schemaNodeContainer = ((YangDataDocument) self).getSchemaContext();
        } else if ( self instanceof YangData){
            schemaNodeContainer = (SchemaNodeContainer) ((YangData<?>) self).getSchemaNode();
        }
    }

    @Override
    public List<YangData<?>> getChildren() {
        return Lists.newArrayList(children.values());
    }

    @Override
    public YangData<?> getChild(DataIdentifier identifier) {
        return children.get(identifier);
    }

    @Override
    public List<YangData<?>> getChildren(QName qName) {
        List<YangData<?>> childrenList = new ArrayList<>();
        Iterator<Map.Entry<DataIdentifier,YangData<?>>> entries = children.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<DataIdentifier,YangData<?>> entry = entries.next();
            if(entry.getKey().getQName().equals(qName)){
                childrenList.add(entry.getValue());
            }
        }
        return childrenList;
    }

    @Override
    public List<YangData<? extends DataNode>> getDataChildren() {
        List<YangData<? extends DataNode>> childrenList = new ArrayList<>();
        Iterator<Map.Entry<DataIdentifier,YangData<?>>> entries = children.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<DataIdentifier,YangData<?>> entry = entries.next();
            YangData<?> value = entry.getValue();
            if(value.isVirtual()){
                if(value instanceof YangDataContainer){
                    childrenList.addAll(((YangDataContainer) value).getDataChildren());
                }
            } else {
                childrenList.add((YangData<? extends DataNode>) value);
            }
        }
        return childrenList;
    }

    @Override
    public YangData<? extends DataNode> getDataChild(DataIdentifier identifier) {
        YangData<?> value = children.get(identifier);
        if(value != null && !value.isVirtual()){
            return (YangData<? extends DataNode>) value;
        }

        Iterator<Map.Entry<DataIdentifier,YangData<?>>> entries = children.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<DataIdentifier,YangData<?>> entry = entries.next();
            value = entry.getValue();
            if(value.isVirtual()){
                if(value instanceof YangDataContainer){
                    YangData<? extends DataNode> dataChild = ((YangDataContainer) value).getDataChild(identifier);
                    if(dataChild != null){
                        return dataChild;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public List<YangData<? extends DataNode>> getDataChildren(QName qName) {
        List<YangData<? extends DataNode>> childrenList = new ArrayList<>();
        Iterator<Map.Entry<DataIdentifier,YangData<?>>> entries = children.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<DataIdentifier,YangData<?>> entry = entries.next();
            YangData<?> value = entry.getValue();
            if(value.isVirtual()){
                if(value instanceof YangDataContainer){
                    childrenList.addAll(((YangDataContainer) value).getDataChildren(qName));
                }
            } else if (entry.getKey().getQName().equals(qName)){
                childrenList.add((YangData<? extends DataNode>) value);
            }
        }
        return childrenList;
    }

    @Override
    public List<YangData<? extends DataNode>> getDataChildren(String name) {
        List<YangData<? extends DataNode>> childrenList = new ArrayList<>();
        Iterator<Map.Entry<DataIdentifier,YangData<?>>> entries = children.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<DataIdentifier,YangData<?>> entry = entries.next();
            YangData<?> value = entry.getValue();
            if(value.isVirtual()){
                if(value instanceof YangDataContainer){
                    childrenList.addAll(((YangDataContainer) value).getDataChildren(name));
                }
            } else if (entry.getKey().getQName().getLocalName().equals(name)){
                childrenList.add((YangData<? extends DataNode>) value);
            }
        }
        return childrenList;
    }

    @Override
    public List<YangData<? extends DataNode>> getDataChildren(String name, String namespace) {
        return getDataChildren(new QName(namespace,name));
    }

    @Override
    public void addChild(YangData<?> child, boolean autoDelete) throws YangDataException {
        if( schemaNodeContainer == null){
            return;
        }
        SchemaNode childSchemaNode = schemaNodeContainer.getSchemaNodeChild(child.getSchemaNode().getIdentifier());
        if (childSchemaNode == null){
            AbsolutePath path = new AbsolutePath();
            if (self instanceof YangData){
                path = ((YangData<?>) self).getPath();
            }
            throw new YangDataException(ErrorTag.BAD_ELEMENT,path,
                    new ErrorMessage("Incompatible child occurs. The child's schema node:"
                            + child.getSchemaNode().toString()
                            + " is not the data child of this schema node:"
                            + ((schemaNodeContainer instanceof YangSchemaContext)?"root":schemaNodeContainer.toString())));
        }
        YangDataOperator yangDataOperator = new YangDataOperatorImpl(self);
        YangData<?> oldChild = getChild(child.getIdentifier());
        if(oldChild != null) {
            if(oldChild.isDummyNode()){
                yangDataOperator.merge(child,autoDelete);
                return;
            }
            throw new YangDataException(ErrorTag.DATA_EXISTS,oldChild.getPath(),
                    new ErrorMessage("the child:"+child.getIdentifier() + " is exists."));
        }
        yangDataOperator.create(child,autoDelete);
        children.put(child.getIdentifier(),child);
    }

    @Override
    public YangData<?> removeChild(DataIdentifier identifier) {
        return children.remove(identifier);
    }


    @Override
    public void addDataChild(YangData<? extends DataNode> child, boolean autoDelete) throws YangDataException {
        YangData<? extends DataNode> original = getDataChild(child.getIdentifier());
        if(original != null){
            if(!original.isDummyNode()){
                throw new YangDataException(ErrorTag.DATA_EXISTS,original.getPath(),
                        new ErrorMessage("the child:"+child.getIdentifier() + " is exists."));
            }
        }

        DataNode childSchema = schemaNodeContainer.getDataNodeChild(child.getSchemaNode().getIdentifier());
        if(null == childSchema){
            AbsolutePath errorPath = new AbsolutePath();
            if(self instanceof YangData){
                errorPath = ((YangData<?>) self).getPath();
            }
            throw new YangDataException(ErrorTag.BAD_ELEMENT, errorPath,
                    new ErrorMessage("unknown data child:"+ child.getSchemaNode().toString()));
        }
        //TODO  generate virtual data node

    }

    @Override
    public YangData<? extends DataNode> removeDataChild(DataIdentifier identifier) {
        return null;
    }
}
