package org.yangcentral.yangkit.data.impl.operation;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.data.api.operation.DataChangeNotifier;
import org.yangcentral.yangkit.data.api.operation.YangDataDocumentOperator;
import org.yangcentral.yangkit.data.api.operation.YangDataOperator;
import org.yangcentral.yangkit.data.impl.util.YangDataUtil;
import org.yangcentral.yangkit.model.api.stmt.DataNode;

import javax.annotation.Nonnull;

public class YangDataDocumentOperatorImpl implements YangDataDocumentOperator {
    private YangDataDocument document;
    private DataChangeNotifier dataChangeNotifier;

    public YangDataDocumentOperatorImpl(YangDataDocument document) {
        this.document = document;
    }

    @Override
    public YangDataDocument getDocument() {
        return document;
    }

    public DataChangeNotifier getDataChangeNotifier() {
        return dataChangeNotifier;
    }

    public void setDataChangeNotifier(DataChangeNotifier dataChangeNotifier) {
        this.dataChangeNotifier = dataChangeNotifier;
    }

    @Override
    public void create(AbsolutePath path, YangData<? extends DataNode> child, boolean autoDelete) throws YangDataException {
        YangData<? extends DataNode> parent = YangDataUtil.search(document,path);
        YangDataContainer operateData = null;
        if(parent == null){
            //not found, it may be document
            if(path.isRootPath()){
                operateData = document;
            } else {
                throw new YangDataException(ErrorTag.DATA_MISSING,path,
                        new ErrorMessage("invalid path, can not find any data node"));
            }
        } else {
            if(parent instanceof YangDataContainer){
                operateData = (YangDataContainer) parent;
            } else {
                throw new YangDataException(ErrorTag.BAD_ELEMENT,path,
                        new ErrorMessage("invalid path, it should point to data container"));
            }
        }
        YangDataOperatorImpl yangDataOperator = new YangDataOperatorImpl(operateData);
        yangDataOperator.setDataChangeNotifier(dataChangeNotifier);
        yangDataOperator.create(child,autoDelete);
    }

    @Override
    public void delete(AbsolutePath path) throws YangDataException {
        YangData<? extends DataNode> target = YangDataUtil.search(document,path);
        if(target == null){
            throw new YangDataException(ErrorTag.DATA_MISSING,path,new ErrorMessage("data missing"));
        }
        YangDataContainer parent = target.getContext().getParent();
        if(path.isRootPath()){
            parent = document;
        }
        YangDataOperatorImpl yangDataOperator = new YangDataOperatorImpl(parent);
        yangDataOperator.setDataChangeNotifier(dataChangeNotifier);
        yangDataOperator.delete(target.getIdentifier());
    }

    @Override
    public void merge(@Nonnull YangDataDocument doc, boolean autoDelete) throws YangDataException {
        YangDataOperatorImpl yangDataOperator = new YangDataOperatorImpl(document);
        yangDataOperator.setDataChangeNotifier(dataChangeNotifier);
        for(YangData<? extends DataNode> child:doc.getDataChildren()){
            yangDataOperator.merge(child,autoDelete);
        }
    }

    @Override
    public void merge(AbsolutePath path, YangData<? extends DataNode> child, boolean autoDelete) throws YangDataException {
        YangData<? extends DataNode> parent = YangDataUtil.search(document,path);
        YangDataContainer operateData = null;
        if(parent == null){
            //not found, it may be document
            if(path.isRootPath()){
                operateData = document;
            } else {
                throw new YangDataException(ErrorTag.DATA_MISSING,path,
                        new ErrorMessage("invalid path, can not find any data node"));
            }
        } else {
            if(parent instanceof YangDataContainer){
                operateData = (YangDataContainer) parent;
            } else {
                throw new YangDataException(ErrorTag.BAD_ELEMENT,path,
                        new ErrorMessage("invalid path, it should point to data container"));
            }
        }
        YangDataOperatorImpl yangDataOperator = new YangDataOperatorImpl(operateData);
        yangDataOperator.setDataChangeNotifier(dataChangeNotifier);
        yangDataOperator.merge(child,autoDelete);
    }

    @Override
    public void replace(YangDataDocument doc) throws YangDataException {
        YangDataOperatorImpl yangDataOperator = new YangDataOperatorImpl(document);
        yangDataOperator.setDataChangeNotifier(dataChangeNotifier);
        for(YangData<? extends DataNode> child:doc.getDataChildren()){
            yangDataOperator.replace(child);
        }
    }

    @Override
    public void replace(AbsolutePath path, YangData<? extends DataNode> child, boolean autoDelete) throws YangDataException {
        YangData<? extends DataNode> parent = YangDataUtil.search(document,path);
        YangDataContainer operateData = null;
        if(parent == null){
            //not found, it may be document
            if(path.isRootPath()){
                operateData = document;
            } else {
                throw new YangDataException(ErrorTag.DATA_MISSING,path,
                        new ErrorMessage("invalid path, can not find any data node"));
            }
        } else {
            if(parent instanceof YangDataContainer){
                operateData = (YangDataContainer) parent;
            } else {
                throw new YangDataException(ErrorTag.BAD_ELEMENT,path,
                        new ErrorMessage("invalid path, it should point to data container"));
            }
        }
        YangDataOperatorImpl yangDataOperator = new YangDataOperatorImpl(operateData);
        yangDataOperator.setDataChangeNotifier(dataChangeNotifier);
        yangDataOperator.replace(child,autoDelete);
    }

    @Override
    public YangData<? extends DataNode> get(AbsolutePath path) {
        return YangDataUtil.search(document,path);
    }
}
