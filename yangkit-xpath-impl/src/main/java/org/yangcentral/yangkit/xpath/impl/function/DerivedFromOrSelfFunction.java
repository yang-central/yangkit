package org.yangcentral.yangkit.xpath.impl.function;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.data.api.model.TypedData;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.IdentityRef;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.Identity;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.xpath.impl.XPathUtil;
import org.yangcentral.yangkit.xpath.YangContextSupport;
import java.util.Iterator;
import java.util.List;
import org.jaxen.Context;
import org.jaxen.Function;
import org.jaxen.FunctionCallException;

public class DerivedFromOrSelfFunction implements Function {
   public Object call(Context context, List args) throws FunctionCallException {
      YangContextSupport yangContextSupport = (YangContextSupport)context.getContextSupport();
      if (null == yangContextSupport) {
         return false;
      } else if (args != null && args.size() == 2) {
         List<YangData<?>> sourceDataset = (List)args.get(0);
         String dest = (String)args.get(1);
         FName fName = new FName(dest);
         String uri = context.translateNamespacePrefixToUri(fName.getPrefix());
         YangSchemaContext schemaContext = XPathUtil.getSchemaContext(yangContextSupport.getContextData());
         if (null == schemaContext) {
            throw new FunctionCallException(ErrorCode.COMMON_ERROR.getFieldName());
         } else {
            Identity destIdentity = XPathUtil.getIdentity(schemaContext, uri, fName.getLocalName());
            if (null == destIdentity) {
               throw new FunctionCallException("argument error, the second argument:" + dest + " can't find corresponding identity");
            } else {
               Iterator iterator = sourceDataset.iterator();

               while(iterator.hasNext()) {
                  YangData yangData = (YangData)iterator.next();
                  if (yangData.getSchemaNode() instanceof TypedDataNode) {
                     TypedDataNode typedDataNode = (TypedDataNode)yangData.getSchemaNode();
                     if (typedDataNode.getType().getRestriction() instanceof IdentityRef) {
                        TypedData typedData = (TypedData)yangData;
                        QName qName = null;
                        try {
                           qName = (QName)(typedData.getValue().getValue());
                        } catch (YangCodecException e) {
                           throw new FunctionCallException(e.getMessage());
                        }
                        Identity sourceIdentity = XPathUtil.getIdentity(schemaContext, qName.getNamespace().toString(), qName.getLocalName());
                        if (sourceIdentity != null && sourceIdentity.isDerivedOrSelf(destIdentity)) {
                           return true;
                        }
                     }
                  }
               }

               return false;
            }
         }
      } else {
         throw new FunctionCallException("derived-from-or-self MUST have 2 arguments.");
      }
   }
}
