package org.yangcentral.yangkit.model.impl.codec;

import org.yangcentral.yangkit.model.api.codec.InstanceIdentifierStringValueCodec;
import org.yangcentral.yangkit.model.api.codec.YangCodecException;
import org.yangcentral.yangkit.model.api.restriction.Restriction;
import org.yangcentral.yangkit.model.api.stmt.TypedDataNode;
import org.yangcentral.yangkit.xpath.YangAbsoluteLocationPath;
import org.yangcentral.yangkit.xpath.YangXPath;
import org.yangcentral.yangkit.xpath.impl.YangXPathImpl;
import org.jaxen.JaxenException;

public class InstanceIdentifierStringValueCodecImpl extends ComplexStringValueCodecImpl<YangAbsoluteLocationPath> implements InstanceIdentifierStringValueCodec {
   public InstanceIdentifierStringValueCodecImpl(TypedDataNode schemaNode) {
      super(schemaNode);
   }

   public YangAbsoluteLocationPath deserialize(Restriction<YangAbsoluteLocationPath> restriction, String input) throws YangCodecException {
      try {
         YangXPath yangXPath = new YangXPathImpl(input);
         if (yangXPath.getRootExpr() instanceof YangAbsoluteLocationPath) {
            return (YangAbsoluteLocationPath)yangXPath.getRootExpr();
         } else {
            throw new YangCodecException("the value:" + input + " is invalid instance-identifier.");
         }
      } catch (JaxenException var4) {
         throw new YangCodecException(var4.getMessage());
      }
   }

   public String serialize(Restriction<YangAbsoluteLocationPath> restriction, YangAbsoluteLocationPath output) throws YangCodecException {
      return output.getText();
   }
}
