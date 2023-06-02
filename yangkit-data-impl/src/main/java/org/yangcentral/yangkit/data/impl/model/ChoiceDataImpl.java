package org.yangcentral.yangkit.data.impl.model;

import org.yangcentral.yangkit.common.api.exception.ErrorMessage;
import org.yangcentral.yangkit.common.api.exception.ErrorTag;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.CaseData;
import org.yangcentral.yangkit.data.api.model.ChoiceData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataContainer;
import org.yangcentral.yangkit.model.api.stmt.Choice;

public class ChoiceDataImpl extends YangDataContainerImpl<Choice> implements ChoiceData {
    public ChoiceDataImpl(Choice schemaNode) {
        super(schemaNode);
        identifier = new SingleInstanceDataIdentifier(schemaNode.getIdentifier());
    }

    @Override
    public CaseData getCaseData() {
        YangData<?> child = this.getChildren().isEmpty()?null:this.getChildren().get(0);
        if(child == null){
            return null;
        }
        while(!(child instanceof CaseData)){
            YangDataContainer yangDataContainer = (YangDataContainer) child;
            child = yangDataContainer.getChildren().isEmpty()?
                    null:yangDataContainer.getChildren().get(0);
            if(child == null){
                return null;
            }
        }
        return (CaseData) child;
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public void addChild(YangData<?> child,boolean autoDelete) throws YangDataException {
        if(!this.getChildren().isEmpty()){
            if(autoDelete){
                this.removeChild(this.getChildren().get(0).getIdentifier());
            } else {
                throw new YangDataException(ErrorTag.BAD_ELEMENT,this.getPath(),
                        new ErrorMessage("conflict data exist."));
            }
        }
        super.addChild(child,autoDelete);
    }
}
