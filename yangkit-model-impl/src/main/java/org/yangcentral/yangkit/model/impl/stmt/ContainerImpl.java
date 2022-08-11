package org.yangcentral.yangkit.model.impl.stmt;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.validate.ValidatorResult;
import org.yangcentral.yangkit.common.api.validate.ValidatorResultBuilder;
import org.yangcentral.yangkit.model.api.stmt.Container;
import org.yangcentral.yangkit.model.api.stmt.Presence;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.ArrayList;
import java.util.List;

public class ContainerImpl extends ContainerDataNodeImpl implements Container {
   private Presence presence;

   public ContainerImpl(String argStr) {
      super(argStr);
   }

   public Presence getPresence() {
      return this.presence;
   }

   public void setPresence(Presence presence) {
      this.presence = presence;
   }

   public boolean isPresence() {
      return this.presence != null;
   }

   public QName getYangKeyword() {
      return YangBuiltinKeyword.CONTAINER.getQName();
   }

   @Override
   protected void clear() {
      this.presence = null;
      super.clear();
   }

   protected ValidatorResult initSelf() {
      ValidatorResultBuilder validatorResultBuilder = new ValidatorResultBuilder();
      validatorResultBuilder.merge(super.initSelf());

      List<YangStatement> matched = this.getSubStatement(YangBuiltinKeyword.PRESENCE.getQName());
      if (matched.size() != 0) {
         this.presence = (Presence)matched.get(0);
      }

      return validatorResultBuilder.build();
   }

   public List<YangStatement> getEffectiveSubStatements() {
      List<YangStatement> statements = new ArrayList();
      if (this.presence != null) {
         statements.add(this.presence);
      }

      statements.addAll(super.getEffectiveSubStatements());
      return statements;
   }
}
