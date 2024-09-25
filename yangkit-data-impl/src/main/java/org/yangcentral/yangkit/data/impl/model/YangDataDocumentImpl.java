package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.*;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import org.yangcentral.yangkit.model.api.stmt.MainModule;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.impl.stmt.MainModuleImpl;
import org.yangcentral.yangkit.model.impl.stmt.ModuleImpl;


import java.util.ArrayList;
import java.util.List;

public class YangDataDocumentImpl extends YangAbstractDataEntry<YangDataDocument> implements YangDataDocument {
    private YangAbstractDataContainer container;
    private YangSchemaContext schemaContext;
    private String docString;

    private String[] modulesString;

    private boolean onlyConfig;

    public YangDataDocumentImpl(QName qName,YangSchemaContext yangDataContainer) {
        super(qName);
        this.schemaContext = yangDataContainer;
        container = new YangAbstractDataContainer(this);
        this.docString = null;
    }
    public YangDataDocumentImpl(QName qName,YangSchemaContext yangDataContainer, String docString) {
        super(qName);
        this.schemaContext = yangDataContainer;
        container = new YangAbstractDataContainer(this);
        this.docString = docString;
    }

    @Override
    public YangSchemaContext getSchemaContext() {
        return schemaContext;
    }

    @Override
    public void setOnlyConfig(boolean onlyConfig) {
        this.onlyConfig = onlyConfig;
    }

    @Override
    public boolean onlyConfig() {
        return onlyConfig;
    }


    @Override
    public ValidatorResult validate() {
        return validateChildren();
    }

    @Override
    public List<YangDataCompareResult> compare(YangDataDocument another) {
        return container.compareChildren(another);
    }

    @Override
    public YangDataDocument clone() throws CloneNotSupportedException {
        YangDataDocument cloned = super.clone();

        for(YangData<?> child:this.getChildren()){
            YangData<?> clonedChild = child.clone();
            cloned.removeChild(child.getIdentifier());
            try {
                cloned.addChild(clonedChild);
            } catch (YangDataException e) {
                throw new RuntimeException(e);
            }
        }
        cloned.update();
        return cloned;
    }


    @Override
    public void update() {
        for(YangData<?> child:getChildren()){
            child.getContext().setDocument(this);
            child.update();
        }
    }

    @Override
    public List<YangData<?>> getChildren() {
        return container.getChildren();
    }

    @Override
    public YangData<?> getChild(DataIdentifier identifier) {
        return container.getChild(identifier);
    }

    @Override
    public List<YangData<?>> getChildren(QName qName) {
        return container.getChildren(qName);
    }

    @Override
    public List<YangData<?>> getDataChildren() {
        return container.getDataChildren();
    }

    @Override
    public YangData<?> getDataChild(DataIdentifier identifier) {
        return container.getDataChild(identifier);
    }

    @Override
    public List<YangData<?>> getDataChildren(QName qName) {
        return container.getDataChildren(qName);
    }

    @Override
    public List<YangData<?>> getDataChildren(String name) {
        return container.getDataChildren(name);
    }

    @Override
    public List<YangData<?>> getDataChildren(String name, String namespace) {
        return container.getDataChildren(name,namespace);
    }

    @Override
    public void addChild(YangData<?> child, boolean autoDelete) throws YangDataException {
        container.addChild(child,autoDelete);
    }

    @Override
    public YangData<?> removeChild(DataIdentifier identifier) {
        return container.removeChild(identifier);
    }

    @Override
    public void addDataChild(YangData<?> child, boolean autoDelete) throws YangDataException {
        container.addDataChild(child,autoDelete);
    }

    @Override
    public YangData<?> removeDataChild(DataIdentifier identifier) {
        return container.removeDataChild(identifier);
    }

    @Override
    public ValidatorResult validateChildren() {
        return container.validateChildren();
    }

    @Override
    public List<YangDataCompareResult> compareChildren(YangDataContainer another) {
        return container.compareChildren(another);
    }

    @Override
    public String getDocString() {
        return this.docString;
    }

    @Override
    public String[] getModulesStrings() {
        if(this.modulesString != null) return this.modulesString;
        List<Module> modules = this.getSchemaContext().getModules();
        this.modulesString = new String[modules.size()];
        for(int i = 0; i < this.modulesString.length ; i++){
            this.modulesString[i] = modules.get(i).getOriginalString();
        }
        return this.modulesString;
    }

}
