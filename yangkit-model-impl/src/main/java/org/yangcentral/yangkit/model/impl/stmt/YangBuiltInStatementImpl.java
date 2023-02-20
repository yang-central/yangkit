package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.*;
import org.yangcentral.yangkit.register.YangUnknownParserPolicy;
import org.yangcentral.yangkit.register.YangUnknownRegister;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.List;
import java.util.Map;

public abstract class YangBuiltInStatementImpl extends YangStatementImpl {
   public YangBuiltInStatementImpl(String argStr) {
      super(argStr);
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof YangBuiltinStatement)) {
         return false;
      } else if (!this.getYangKeyword().equals(((YangBuiltinStatement)obj).getYangKeyword())) {
         return false;
      } else if (this.getArgStr() != null && ((YangBuiltinStatement)obj).getArgStr() != null && this.getArgStr().equals(((YangBuiltinStatement)obj).getArgStr())) {
         return true;
      } else {
         return this.getArgStr() == null && ((YangBuiltinStatement)obj).getArgStr() == null;
      }
   }

   @Override
   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder(super.initSelf());

      YangSpecification yangSpecification = this.getContext().getYangSpecification();
      YangStatementDef statementDef = yangSpecification.getStatementDef(this.getYangKeyword());
      if (null == statementDef) {
         validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                 ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName() + " keyword:" + this.getYangKeyword().getLocalName() ));
         return validatorResultBuilder.build();
      }


      Map<QName, YangSubStatementInfo> subStatementInfos = statementDef.getSubStatementInfos();

      for (QName key : subStatementInfos.keySet()) {
         List<YangStatement> filteredStatements = this.getSubStatement(key);
         Cardinality cardinality = subStatementInfos.get(key).getCardinality();
         if (!cardinality.isValid(filteredStatements.size())) {
            validatorResultBuilder.addRecord(ModelUtil.reportError(this,
                ErrorCode.CARDINALITY_BROKEN.getFieldName() + " sub-statement:" + key.getLocalName() + "'s cardinality is:" + cardinality));
         }
      }

      for (YangElement subElement : this.getSubElements()) {
         if (!(subElement instanceof YangStatement)) {
            continue;
         }
         YangStatement yangStatement = (YangStatement) subElement;
         if (!subStatementInfos.containsKey(yangStatement.getYangKeyword())) {
            if (!(yangStatement instanceof YangUnknown)) {
               validatorResultBuilder.addRecord(ModelUtil.reportError(yangStatement,
                   ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
            } else {
               YangUnknownParserPolicy unknownParserPolicy = YangUnknownRegister.getInstance().getUnknownInfo(((YangUnknown) subElement).getYangKeyword());
               if (unknownParserPolicy == null || unknownParserPolicy.getParentStatements().size() <= 0) {
                  continue;
               }
               if (unknownParserPolicy.getParentStatement(this.getYangKeyword()) == null) {
                  validatorResultBuilder.addRecord(ModelUtil.reportError((YangStatement) subElement,
                      ErrorCode.INVALID_SUBSTATEMENT.getFieldName()));
               }
            }
         }
      }
      return validatorResultBuilder.build();
   }
}
