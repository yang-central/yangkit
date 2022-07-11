package org.yangcentral.yangkit.parser;

import org.yangcentral.yangkit.base.ErrorCode;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.Yang;
import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangComment;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.register.YangStatementParserPolicy;
import org.yangcentral.yangkit.register.YangStatementParserRegister;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.dom4j.Comment;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Namespace;
import org.dom4j.Node;

public class YinParser {
   private String fileName;
   public static final Namespace YIN_NS = Namespace.get("urn:ietf:params:xml:ns:yang:yin:1");

   public YinParser(String fileName) {
      this.fileName = fileName;
   }

   static YangComment buildYangComment(Comment comment, String fileName) {
      YangComment yangComment = new YangComment();
      yangComment.setComment(comment.getText());
      if (comment.getText().contains("\n")) {
         yangComment.setMultiComment(true);
      }

      yangComment.setElementPosition(new Position(fileName, new XPathLocation(comment.getUniquePath())));
      return yangComment;
   }

   static YangElement buildYangElement(Element element, String fileName) throws YangParserException {
      String keyword = element.getName();
      if (!element.getNamespaceURI().equals(YIN_NS.getURI())) {
         YinUnknownBlock yinUnknownBlock = new YinUnknownBlock(element, fileName);
         return yinUnknownBlock;
      } else if (!YangBuiltinKeyword.isKeyword(keyword)) {
         throw new YangParserException(Severity.ERROR, new Position(fileName, new XPathLocation(element.getUniquePath())), ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName());
      } else {
         String value = null;
         YangBuiltinKeyword yangBuiltinKeyword = YangBuiltinKeyword.from(new QName(Yang.NAMESPACE, keyword));
         if (yangBuiltinKeyword.isYinElement()) {
            value = element.element(yangBuiltinKeyword.getArgument()).getStringValue();
         } else {
            value = element.attributeValue(yangBuiltinKeyword.getArgument());
         }

         YangStatementParserPolicy parserPolicy = YangStatementParserRegister.getInstance().getStatementParserPolicy(new QName(Yang.NAMESPACE, keyword));
         if (null == parserPolicy) {
            throw new YangParserException(Severity.ERROR, new Position(fileName, new XPathLocation(element.getUniquePath())), "can not find the parser policy for statement:" + keyword + ". please check whether register parser policy for this statement.");
         } else {
            try {
               Constructor<? extends YangStatement> constructor = parserPolicy.getClazz().getConstructor(String.class);
               YangStatement statement = (YangStatement)constructor.newInstance(value);
               statement.setElementPosition(new Position(fileName, new XPathLocation(element.getUniquePath())));
               boolean hasChildren = false;
               if (yangBuiltinKeyword.isYinElement()) {
                  if (element.nodeCount() > 1) {
                     hasChildren = true;
                  }
               } else if (element.nodeCount() > 0) {
                  hasChildren = true;
               }

               if (hasChildren) {
                  for(int i = 0; i < element.nodeCount(); ++i) {
                     Node node = element.node(i);
                     if (node != null) {
                        if (node instanceof Comment) {
                           statement.addChild(buildYangComment((Comment)node, fileName));
                        } else if (node instanceof Element) {
                           Element childElement = (Element)node;
                           if (!yangBuiltinKeyword.isYinElement() || !childElement.getName().equals(yangBuiltinKeyword.getArgument())) {
                              statement.addChild(buildYangElement(childElement, fileName));
                           }
                        }
                     }
                  }
               }

               return statement;
            } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException var12) {
               throw new YangParserException(Severity.ERROR, new Position(fileName, new XPathLocation(element.getUniquePath())), "can not create instance for this statement.");
            }
         }
      }
   }

   public List<YangElement> parse(Document document) throws YangParserException {
      List<YangElement> elements = new ArrayList();
      Iterator<Node> nodes = document.nodeIterator();

      while(nodes.hasNext()) {
         Node node = (Node)nodes.next();
         if (node instanceof Comment) {
            elements.add(buildYangComment((Comment)node, this.fileName));
         } else if (node instanceof Element) {
            Element element = (Element)node;
            elements.add(buildYangElement(element, this.fileName));
         }
      }

      return elements;
   }
}
