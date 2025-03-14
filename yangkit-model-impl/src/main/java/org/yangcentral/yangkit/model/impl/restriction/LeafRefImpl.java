package org.yangcentral.yangkit.model.impl.restriction;

import org.apache.commons.lang3.ObjectUtils;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.restriction.*;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.model.api.stmt.Typedef;
import org.yangcentral.yangkit.model.api.stmt.type.Path;
import org.yangcentral.yangkit.model.api.stmt.type.RequireInstance;
import org.yangcentral.yangkit.model.impl.stmt.type.RequireInstanceImpl;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class LeafRefImpl extends RestrictionImpl<Object> implements LeafRef {
   private Path path;
   private RequireInstance requireInstance;
   private TypedDataNode referencedNode;

   public LeafRefImpl(YangContext context, Typedef derived) {
      super(context, derived);
   }

   public LeafRefImpl(YangContext context) {
      super(context);
   }

   public Path getPath() {
      return this.path;
   }

   public Path getEffectivePath() {
      return this.getDerived() != null ? ((LeafRef)this.getDerived().getType().getRestriction()).getEffectivePath() : this.getPath();
   }

   public RequireInstance getRequireInstance() {
      return this.requireInstance;
   }

   public RequireInstance getEffectiveRequireInstance() {
      RequireInstance requireInstance = this.getRequireInstance();
      if (requireInstance != null) {
         return requireInstance;
      } else if (this.getDerived() != null) {
         LeafRef instanceIdentifier = (LeafRef)this.getDerived().getType().getRestriction();
         return instanceIdentifier.getEffectiveRequireInstance();
      } else {
         RequireInstance newRequireInstance = new RequireInstanceImpl("true");
         newRequireInstance.setContext(new YangContext(this.getContext()));
         newRequireInstance.setElementPosition(this.getContext().getSelf().getElementPosition());
         newRequireInstance.setParentStatement(this.getContext().getSelf());
         newRequireInstance.init();
         newRequireInstance.build();
         return newRequireInstance;
      }
   }

   public void setPath(Path path) {
      this.path = path;
   }

   public void setRequireInstance(RequireInstance requireInstance) {
      this.requireInstance = requireInstance;
   }

   public boolean isRequireInstance() {
      return this.requireInstance == null ? true : this.requireInstance.value();
   }

   public TypedDataNode getReferencedNode() {
      return this.referencedNode;
   }

   public void setReferencedNode(TypedDataNode typedDataNode) {
      this.referencedNode = typedDataNode;
   }

   public boolean evaluate(Object value) {
      if(referencedNode != null){
         Restriction restriction = referencedNode.getType().getRestriction();
         if(restriction instanceof Binary){
            if(!(value instanceof byte[])){
               return false;
            }
         } else if (restriction instanceof Bits){
            if(!(value instanceof List)){
               return false;
            }
         } else if (restriction instanceof Decimal64) {
            if(!(value instanceof BigDecimal)){
               return false;
            }
         } else if (restriction instanceof Empty){
            if(!(value instanceof ObjectUtils.Null)){
               return false;
            }
         } else if (restriction instanceof Enumeration){
            if(!(value instanceof String)){
               return false;
            }
         } else if (restriction instanceof IdentityRef){
            if(!(value instanceof QName)){
               return false;
            }
         } else if (restriction instanceof InstanceIdentifier){
            if(!(value instanceof YangAbsoluteLocationPath)){
               return false;
            }
         } else if (restriction instanceof Int16){
            if(!(value instanceof Short)){
               return false;
            }
         } else if (restriction instanceof Int32){
            if(!(value instanceof Integer)){
               return false;
            }
         } else if (restriction instanceof Int64){
            if(!(value instanceof Long)){
               return false;
            }
         }  else if (restriction instanceof Int8){
            if(!(value instanceof Byte)){
               return false;
            }
         }  else if (restriction instanceof UInt16){
            if(!(value instanceof Integer)){
               return false;
            }
         }  else if (restriction instanceof UInt32){
            if(!(value instanceof Long)){
               return false;
            }
         } else if (restriction instanceof UInt64){
            if(!(value instanceof BigInteger)){
               return false;
            }
         } else if (restriction instanceof UInt8){
            if(!(value instanceof Short)){
               return false;
            }
         } else if (restriction instanceof YangBoolean){
            if(!(value instanceof Boolean)){
               return false;
            }
         } else if (restriction instanceof YangString){
            if(!(value instanceof String)){
               return false;
            }
         }
         return restriction.evaluate(value);
      }
      return false;
   }

   public boolean equals(Object obj) {
      if (!(obj instanceof LeafRef)) {
         return false;
      } else {
         LeafRef another = (LeafRef)obj;
         if (!this.getEffectivePath().equals(another.getEffectivePath())) {
            return false;
         } else {
            return this.isRequireInstance() == another.isRequireInstance();
         }
      }
   }
}
