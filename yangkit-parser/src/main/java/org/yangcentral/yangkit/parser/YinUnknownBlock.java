package org.yangcentral.yangkit.parser;

import org.dom4j.Comment;
import org.dom4j.Element;
import org.dom4j.Node;
import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.base.YangContext;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.base.YangUnknownBlock;
import org.yangcentral.yangkit.common.api.FName;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.Extension;
import org.yangcentral.yangkit.model.api.stmt.Module;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;
import org.yangcentral.yangkit.register.YangStatementParserRegister;
import org.yangcentral.yangkit.register.YangUnknownParserPolicy;
import org.yangcentral.yangkit.register.YangUnknownRegister;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.List;

class YinUnknownBlock extends YangUnknownBlock<Element> {
   private String fileName;

   public YinUnknownBlock(Element block, String fileName) {
      super(block);
      this.fileName = fileName;
   }

   public YangUnknown build(YangContext context) {
      String keyword = ((Element)this.getBlock()).getQualifiedName();
      String namespace = ((Element)this.getBlock()).getNamespaceURI();
      FName fName = new FName(keyword);
      List<Module> moduleList = context.getSchemaContext().getModule(URI.create(namespace));
      if (moduleList.isEmpty()) {
         throw new IllegalArgumentException("no module's namespace is " + namespace);
      } else {
         Extension extension = ((Module)moduleList.get(0)).getExtension(fName.getLocalName());
         if (null == extension) {
            throw new IllegalArgumentException("can not find a extension named:" + fName.getLocalName());
         } else {
            boolean isYinElement = false;
            YangUnknown yangUnknown = null;
            String argStr = null;
            if (extension.getArgument() != null) {
               isYinElement = extension.getArgument().isYinElement();
               if (isYinElement) {
                  argStr = ((Element)this.getBlock()).elementText(extension.getArgument().getArgStr());
               } else {
                  argStr = ((Element)this.getBlock()).attributeValue(extension.getArgument().getArgStr());
               }
            }

            YangUnknownParserPolicy parserPolicy = YangUnknownRegister.getInstance().getUnknownInfo(new QName(namespace, fName.getLocalName()));
            if (parserPolicy != null) {
               try {
                  Constructor<? extends YangStatement> constructor = parserPolicy.getClazz().getConstructor(String.class);
                  yangUnknown = (YangUnknown)constructor.newInstance(argStr);
                  yangUnknown.setElementPosition(new Position(this.fileName, new XPathLocation(((Element)this.getBlock()).getUniquePath())));
               } catch (InvocationTargetException | InstantiationException | IllegalAccessException | NoSuchMethodException var19) {
                  throw new IllegalArgumentException((new Position(this.fileName, new XPathLocation(((Element)this.getBlock()).getUniquePath()))).toString() + " can not create instance for this statement.");
               }
            } else {
               yangUnknown = YangStatementParserRegister.getInstance().getUnknownInstance(((Element)this.getBlock()).getQualifiedName(), argStr);
               yangUnknown.setElementPosition(new Position(this.fileName, new XPathLocation(((Element)this.getBlock()).getUniquePath())));
            }

            boolean hasChildren = false;
            Element root = (Element)this.getBlock();
            if (isYinElement) {
               if (root.nodeCount() > 1) {
                  hasChildren = true;
               }
            } else if (root.nodeCount() > 0) {
               hasChildren = true;
            }

            if (hasChildren) {
               for(int i = 0; i < root.nodeCount(); ++i) {
                  Node node = root.node(i);
                  if (node != null) {
                     if (node instanceof Comment) {
                        yangUnknown.addChild(YinParser.buildYangComment((Comment)node, this.fileName));
                     } else if (node instanceof Element) {
                        Element childElement = (Element)node;
                        if (!isYinElement || !childElement.getName().equals(fName.getLocalName())) {
                           try {
                              YangElement childYangElement = YinParser.buildYangElement(childElement, this.fileName);
                              if (childYangElement instanceof YinUnknownBlock) {
                                 YangUnknown childUnknown = ((YinUnknownBlock)childYangElement).build(new YangContext(context));
                                 yangUnknown.addChild(childUnknown);
                              } else {
                                 yangUnknown.addChild(childYangElement);
                              }
                           } catch (YangParserException var18) {
                              throw new IllegalArgumentException(var18.getMessage());
                           }
                        }
                     }
                  }
               }
            }

            return yangUnknown;
         }
      }
   }
}
