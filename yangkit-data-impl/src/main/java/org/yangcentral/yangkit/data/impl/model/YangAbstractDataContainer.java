package org.yangcentral.yangkit.data.impl.model;

import com.google.common.collect.Lists;
import org.jaxen.JaxenException;
import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.ErrorAppTag;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.common.api.validate.ValidatorRecordBuilder;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.data.impl.builder.YangDataBuilder;
import org.yangcentral.yangkit.data.impl.util.YangDataUtil;
import org.yangcentral.yangkit.model.api.schema.SchemaPath;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.model.impl.schema.DescendantSchemaPath;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class YangAbstractDataContainer implements YangDataContainer {
    private YangDataContainer self;
    private SchemaNodeContainer schemaNodeContainer;
    private Map<DataIdentifier,YangData<?>> children = new ConcurrentHashMap<>();

    private List<YangData<?>> childrenList = new ArrayList<>();


    public YangAbstractDataContainer(YangDataContainer yangDataContainer) {
        this.self = yangDataContainer;
        if(self instanceof YangDataDocument){
            schemaNodeContainer = ((YangDataDocument) self).getSchemaContext();
        } else if ( self instanceof YangData){
            schemaNodeContainer = (SchemaNodeContainer) ((YangData<?>) self).getSchemaNode();
        }
    }

    public YangAbstractDataContainer(SchemaNodeContainer schemaNodeContainer) {
        this.schemaNodeContainer = schemaNodeContainer;
    }

    public void setSelf(YangDataContainer self) {
        this.self = self;
    }

    @Override
    public List<YangData<?>> getChildren() {
        return Lists.newArrayList(childrenList);
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
    public List<YangData<?>> getDataChildren() {
        List<YangData<?>> list = new ArrayList<>();
        for(YangData<?> value: childrenList){
            if(value.isVirtual()){
                if(value instanceof YangDataContainer){
                    list.addAll(((YangDataContainer) value).getDataChildren());
                }
            } else {
                list.add(value);
            }
        }
        return list;
    }

    @Override
    public YangData<?> getDataChild(DataIdentifier identifier) {
        YangData<?> value = children.get(identifier);
        if(value != null && !value.isVirtual()){
            return value;
        }

        Iterator<Map.Entry<DataIdentifier,YangData<?>>> entries = children.entrySet().iterator();
        while(entries.hasNext()){
            Map.Entry<DataIdentifier,YangData<?>> entry = entries.next();
            value = entry.getValue();
            if(value.isVirtual()){
                if(value instanceof YangDataContainer){
                    YangData<?> dataChild = ((YangDataContainer) value).getDataChild(identifier);
                    if(dataChild != null){
                        return dataChild;
                    }
                }
            }
        }

        return null;
    }

    @Override
    public List<YangData<?>> getDataChildren(QName qName) {
        List<YangData<?>> list = new ArrayList<>();
        for(YangData<?> child:childrenList){
            if(child.isVirtual()){
                if(child instanceof YangDataContainer){
                    list.addAll(((YangDataContainer) child).getDataChildren(qName));
                }
            } else if (child.getIdentifier().getQName().equals(qName)){
                list.add(child);
            }
        }
        return list;
    }

    @Override
    public List<YangData<?>> getDataChildren(String name) {
        List<YangData<?>> list = new ArrayList<>();
        for(YangData<?> value:childrenList){
            if(value.isVirtual()){
                if(value instanceof YangDataContainer){
                    list.addAll(((YangDataContainer) value).getDataChildren(name));
                }
            } else if (value.getIdentifier().getQName().getLocalName().equals(name)){
                list.add(value);
            }
        }

        return list;
    }

    @Override
    public List<YangData<?>> getDataChildren(String name, String namespace) {
        return getDataChildren(new QName(namespace,name));
    }

    @Override
    public YangData<?> removeChild(DataIdentifier identifier) {
        YangData<?> child = children.remove(identifier);
        childrenList.remove(child);
        return child;
    }

    @Override
    public void addDataChild(YangData child, boolean autoDelete) throws YangDataException {
        YangData<?> original = getDataChild(child.getIdentifier());
        if(original != null){
            if(!original.isDummyNode()){
                throw new YangDataException(ErrorTag.DATA_EXISTS,original.getPath(),
                        new ErrorMessage("the child:"+child.getIdentifier() + " is exists."));
            }
        }

        SchemaNode childSchema = schemaNodeContainer.getTreeNodeChild(child.getSchemaNode().getIdentifier());
        if(null == childSchema){
            AbsolutePath errorPath = new AbsolutePath();
            if(self instanceof YangData){
                errorPath = ((YangData<?>) self).getPath();
            }
            throw new YangDataException(ErrorTag.BAD_ELEMENT, errorPath,
                    new ErrorMessage("unknown data child:"+ child.getSchemaNode().toString()));
        }

        Stack<SchemaNode> descendants = new Stack();
        SchemaNode childParentSchemaNode = childSchema;
        while(childParentSchemaNode != schemaNodeContainer){
            descendants.push(childParentSchemaNode);
            SchemaNodeContainer parentContainer = childParentSchemaNode.getParentSchemaNode();
            if(!(parentContainer instanceof SchemaNode)){
                break;
            }
            childParentSchemaNode = (SchemaNode) parentContainer;
        }

        YangDataContainer yangDataContainer = this.self;

        while( !descendants.isEmpty()) {
            SchemaNode descendant = descendants.pop();
            YangData<?> descendantData = null;
            if(descendant.equals(child.getSchemaNode())){
                yangDataContainer.addChild(child,autoDelete);
                break;
            } else {
                descendantData = yangDataContainer.getChild(
                        new SingleInstanceDataIdentifier(descendant.getIdentifier()));
                if(null == descendantData){
                    descendantData = new YangDataBuilder().getYangData(descendant,null);
                    yangDataContainer.addChild(descendantData,autoDelete);
                }
            }
            yangDataContainer = (YangDataContainer) descendantData;

        }

    }

    @Override
    public void addChild(YangData child, boolean autoDelete) throws YangDataException {
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

        YangData<?> oldChild = getChild(child.getIdentifier());
        if(oldChild != null) {
//            if(oldChild.isDummyNode()){
//                self.removeChild(child.getIdentifier());
//                children.put(child.getIdentifier(),child);
//                return;
//            }
            throw new YangDataException(ErrorTag.DATA_EXISTS,oldChild.getPath(),
                    new ErrorMessage("the child:"+child.getIdentifier() + " is exists."));
        }
        childrenList.add(child);
        children.put(child.getIdentifier(),child);
        child.getContext().setParent(self);
        if(self instanceof YangDataDocument){
            child.getContext().setDocument((YangDataDocument) self);
        } else {
            YangData<?> yangData = (YangData<?>) self;
            child.getContext().setDocument(yangData.getContext().getDocument());
        }
    }


    @Override
    public YangData<?> removeDataChild(DataIdentifier identifier) {

        YangData<?> dataChild = getDataChild(identifier);
        if(dataChild == null){
            return null;
        }
        YangDataContainer parent = dataChild.getContext().getParent();
        parent.removeChild(identifier);
        return dataChild;
    }

    private boolean matchUnique(Unique unique,List<YangData<?>> uniqueData,ListData listData){
        List<YangData<?>> matchedUniqueData = new ArrayList<>();
        for(Leaf leaf:unique.getUniqueNodes()){
            List<QName> steps = listData.getSchemaNode().getSchemaPath().getRelativeSchemaPath(leaf.getSchemaPath());
            SchemaPath.Descendant descendant = new DescendantSchemaPath(steps,listData.getSchemaNode());
            List<YangData<?>> matched = YangDataUtil.search(listData,descendant);
            if(matched.isEmpty()){
                return false;
            }
            if(matched.size() > 1){
                return false;
            }
            matchedUniqueData.add(matched.get(0));
        }
        if(uniqueData.isEmpty()){
            uniqueData.addAll(matchedUniqueData);
        } else {
            if(!YangDataUtil.equals(uniqueData,matchedUniqueData)){
                return false;
            }
        }
        return true;
    }

    private ValidatorResult checkUniques(YangList list, List<YangData<?>> matchedData){
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        if(list.getUniques().isEmpty() || matchedData.isEmpty()){
            return validatorResultBuilder.build();
        }
        List<Unique> uniques = list.getUniques();
        for(Unique unique:uniques){
            List<YangData<?>> uniqueData = new ArrayList<>();
            int matchCount = 0;
            YangData<?> previous = null;
            for( YangData<?> dataItem:matchedData){
                if(dataItem instanceof ListData){
                    if(matchUnique(unique,uniqueData, (ListData) dataItem)){
                        matchCount++;
                    }
                    if(matchCount == 1){
                        previous = dataItem;
                    }
                    if(matchCount >1){
                        ValidatorRecordBuilder<AbsolutePath,YangData> validatorRecordBuilder =
                                new ValidatorRecordBuilder<>();
                        validatorRecordBuilder.setErrorTag(ErrorTag.OPERATION_FAILED);
                        validatorRecordBuilder.setErrorAppTag(ErrorAppTag.DATA_NOT_UNIQUE.getName());
                        validatorRecordBuilder.setErrorPath(dataItem.getPath());
                        validatorRecordBuilder.setErrorMessage(new ErrorMessage("data is not unique."+
                                " previous:"+ previous.getPath()));
                        validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                        break;
                    }
                }
            }
            if(matchCount > 1){
                break;
            }
        }
        return validatorResultBuilder.build();
    }

    private ValidatorResult checkMandatory(SchemaNode schemaNode, List<YangData<?>> matchedData) {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        if(schemaNode.isMandatory() && schemaNode.isActive() && schemaNode.isConfig()){
            if(matchedData.isEmpty()){
                //if have when condition, valuate this when condition,if true, report error
                YangData<?> dummyNode = new YangDataBuilder().getYangData(schemaNode,null);
                dummyNode.setDummyNode(true);
                try {
                    self.addChild(dummyNode);
                    boolean result = dummyNode.checkWhen();
                    if(result){
                        ValidatorRecordBuilder<AbsolutePath,YangData<?>> validatorRecordBuilder =
                                new ValidatorRecordBuilder<>();
                        validatorRecordBuilder.setErrorTag(ErrorTag.DATA_MISSING);
                        validatorRecordBuilder.setErrorPath((self instanceof YangDataDocument)?new AbsolutePath():
                                ((YangData)self).getPath());
                        validatorRecordBuilder.setErrorMessage(new ErrorMessage("missing mandatory schema node:"
                                + schemaNode.getIdentifier().getQualifiedName()));
                        validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                    }
                    self.removeChild(dummyNode.getIdentifier());
                } catch (YangDataException | JaxenException e) {
                    self.removeChild(dummyNode.getIdentifier());
                    e.printStackTrace();
                }
            }
            else {
                //check whether match the min-elements and max-elements for multi-instance schema node
                if(schemaNode instanceof MultiInstancesDataNode){
                    MultiInstancesDataNode multiInstancesDataNode = (MultiInstancesDataNode) schemaNode;
                    int minElements =0;
                    int maxElements = Integer.MAX_VALUE;
                    if(multiInstancesDataNode.getMinElements() != null){
                        minElements = multiInstancesDataNode.getMinElements().getValue();
                    }

                    if(multiInstancesDataNode.getMaxElements() != null
                            && !multiInstancesDataNode.getMaxElements().isUnbounded()){
                        maxElements = multiInstancesDataNode.getMaxElements().getValue();
                    }

                    int size = matchedData.size();
                    if(size < minElements || size > maxElements){
                        ValidatorRecordBuilder<AbsolutePath,YangData<?>> validatorRecordBuilder =
                                new ValidatorRecordBuilder<>();
                        validatorRecordBuilder.setErrorTag(ErrorTag.OPERATION_FAILED);
                        validatorRecordBuilder.setErrorPath((self instanceof YangDataDocument)?new AbsolutePath():
                                ((YangData)self).getPath());
                        if(size < minElements){
                            validatorRecordBuilder.setErrorAppTag(ErrorAppTag.TOO_FEW_ELEMENTS.getName());
                            validatorRecordBuilder.setErrorMessage(new ErrorMessage("too few elements for node:"
                            + schemaNode.getIdentifier().getQualifiedName() + " min-elements:"+ minElements));
                        } else {
                            validatorRecordBuilder.setErrorAppTag(ErrorAppTag.TOO_MANY_ELEMENTS.getName());
                            validatorRecordBuilder.setErrorMessage(new ErrorMessage("too many elements for node:"
                                    + schemaNode.getIdentifier().getQualifiedName() + " max-elements:"+ maxElements));
                        }
                        validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                    }

                }
            }
        }
        return validatorResultBuilder.build();
    }

    @Override
    public ValidatorResult validateChildren() {
        ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
        //build schema children match record map
        Map<QName,List<YangData<?>>> matchRecord = new ConcurrentHashMap<>();
        for(SchemaNode schemaNode: schemaNodeContainer.getSchemaNodeChildren()){
            if(YangDataUtil.onlyConfig(self)){
                if(!schemaNode.isConfig()){
                    continue;
                }
            }
            matchRecord.put(schemaNode.getIdentifier(),new ArrayList<YangData<?>>());
        }
        for(YangData<?> child:self.getChildren()){
            SchemaNode schemaNode = child.getSchemaNode();
            if(!matchRecord.containsKey(schemaNode.getIdentifier()) || !schemaNode.isActive()){
                //inactive or unknown schema node, report error
                ValidatorRecordBuilder<AbsolutePath,YangData<?>> validatorRecordBuilder =
                        new ValidatorRecordBuilder<>();
                validatorRecordBuilder.setErrorTag(ErrorTag.UNKNOWN_ELEMENT);
                validatorRecordBuilder.setErrorPath((self instanceof YangDataDocument)?new AbsolutePath():
                        ((YangData)self).getPath());
                validatorRecordBuilder.setErrorMessage(new ErrorMessage("unknown schema node:"
                        + schemaNode.getArgStr()));
                validatorResultBuilder.addRecord(validatorRecordBuilder.build());
                continue;
            }
            List<YangData<?>> matchedData = matchRecord.get(schemaNode.getIdentifier());
            matchedData.add(child);
        }

        for(Map.Entry<QName,List<YangData<?>>> entry :matchRecord.entrySet()){
            SchemaNode schemaNode = schemaNodeContainer.getSchemaNodeChild(entry.getKey());
            //check mandatory
            validatorResultBuilder.merge(checkMandatory(schemaNode,entry.getValue()));
            //check unique
            if(schemaNode instanceof YangList){
                validatorResultBuilder.merge(checkUniques((YangList) schemaNode,entry.getValue()));
            }
        }
        for(YangData<?> child:self.getChildren()){
            validatorResultBuilder.merge(child.validate());
        }
        return validatorResultBuilder.build();
    }

    @Override
    public List<YangDataCompareResult> compareChildren(YangDataContainer another) {
        List<YangDataCompareResult> results = new ArrayList<>();
        List<YangData<?>> children = self.getDataChildren();
        List<YangData<?>> oChildren = another.getDataChildren();
        Map<DataIdentifier,YangData<?>> map = new ConcurrentHashMap<>();
        AbsolutePath path = new AbsolutePath();
        if(!(self instanceof YangDataDocument)){
            path = ((YangData)self).getPath();
        }
        for(YangData<?> child: children){
            YangData<?> oChild = another.getDataChild(child.getIdentifier());
            if(oChild == null){
                //delete
                results.add(new YangCompareResultImpl(path,DifferenceType.NONE,null,child));
            } else {
                //change?
                results.addAll(child.compare(oChild));
                map.put(child.getIdentifier(),oChild);
            }
        }
        for(YangData<?> oChild:another.getDataChildren()){
            if(map.get(oChild.getIdentifier())== null){
                //new
                results.add(new YangCompareResultImpl(path,DifferenceType.NEW,oChild,null));
            }
        }
        return results;
    }


}
