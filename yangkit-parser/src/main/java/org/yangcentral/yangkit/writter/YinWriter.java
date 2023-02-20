package org.yangcentral.yangkit.writter;

import org.dom4j.*;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangComment;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.model.api.stmt.Extension;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;
import org.yangcentral.yangkit.parser.YinParser;

import javax.annotation.Nonnull;
import java.net.URI;
import java.util.List;

public class YinWriter {
   public static Node serialize(@Nonnull YangElement yangElement) {
      if (yangElement instanceof YangComment) {
         return null;
      } else if (yangElement instanceof YangStatement) {
         YangStatement yangStatement = (YangStatement)yangElement;
         Element element = null;
         if (yangStatement instanceof YangBuiltinStatement) {
            YangBuiltinStatement builtinStatement = (YangBuiltinStatement)yangStatement;
            element = DocumentHelper.createElement(QName.get(builtinStatement.getYangKeyword().getLocalName(), YinParser.YIN_NS));
            if (builtinStatement.getArgStr() != null && !builtinStatement.getArgStr().isEmpty()) {
               YangBuiltinKeyword yangBuiltinKeyword = YangBuiltinKeyword.from(builtinStatement.getYangKeyword());
               if (yangBuiltinKeyword.getArgument() != null && !yangBuiltinKeyword.getArgument().isEmpty()) {
                  if (yangBuiltinKeyword.isYinElement()) {
                     Element argElement = DocumentHelper.createElement(QName.get(yangBuiltinKeyword.getArgument(), YinParser.YIN_NS));
                     argElement.setText(builtinStatement.getArgStr());
                     element.add(argElement);
                  } else {
                     Attribute argAttr = DocumentHelper.createAttribute(element, QName.get(yangBuiltinKeyword.getArgument(), YinParser.YIN_NS), builtinStatement.getArgStr());
                     element.add(argAttr);
                  }
               }
            }
         } else {
            YangUnknown yangUnknown = (YangUnknown)yangStatement;
            Extension extension = yangUnknown.getExtension();
            URI uri = extension.getContext().getCurModule().getMainModule().getNamespace().getUri();
            element = DocumentHelper.createElement(QName.get(yangUnknown.getKeyword(), uri.toString()));
            boolean isYinElement = false;
            if (extension.getArgument() != null) {
               isYinElement = extension.getArgument().isYinElement();
               if (isYinElement) {
                  Element argElement = DocumentHelper.createElement(QName.get(extension.getArgument().getArgStr(), Namespace.get(uri.toString())));
                  argElement.setText(yangUnknown.getArgStr());
                  element.add(argElement);
               } else {
                  Attribute argAttr = DocumentHelper.createAttribute(element, QName.get(extension.getArgument().getArgStr(), uri.toString()), yangUnknown.getArgStr());
                  element.add(argAttr);
               }
            }
         }

         for (YangElement childElement : yangStatement.getSubElements()) {
            Node childNode = serialize(childElement);
            if (childNode != null) {
               element.add(childNode);
            }
         }

         return element;
      } else {
         return null;
      }
   }

   public static Document serialize(List<YangElement> elements) {
      Document document = DocumentHelper.createDocument();

      for (YangElement yangElement : elements) {
         Node node = serialize(yangElement);
         if (node != null) {
            document.add(node);
         }
      }

      return document;
   }
}
