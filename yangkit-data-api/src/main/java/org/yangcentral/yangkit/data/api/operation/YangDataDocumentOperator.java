package org.yangcentral.yangkit.data.api.operation;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import javax.annotation.Nonnull;

public interface YangDataDocumentOperator extends Cloneable {
   YangDataDocument getDocument();

   void create(AbsolutePath path, YangData<? extends DataNode> child,boolean autoDelete) throws YangDataException;
   default void create(AbsolutePath path, YangData<? extends DataNode> child) throws YangDataException{
      create(path,child,true);
   }
   void delete(AbsolutePath path) throws YangDataException;

   void merge(@Nonnull YangDataDocument doc, boolean autoDelete) throws YangDataException;
   default void merge(@Nonnull YangDataDocument doc) throws YangDataException {
      merge(doc,true);
   }

   void merge(AbsolutePath path, YangData<? extends DataNode> child,boolean autoDelete) throws YangDataException;

   default void merge(AbsolutePath path, YangData<? extends DataNode> child) throws YangDataException {
      merge(path,child,true);
   }
   void replace(YangDataDocument doc) throws YangDataException;

   void replace(AbsolutePath path, YangData<? extends DataNode> child, boolean autoDelete) throws YangDataException;

   default void replace(AbsolutePath path, YangData<? extends DataNode> child) throws YangDataException{
      replace(path,child,true);
   }

   YangData<?> get(AbsolutePath path);
}
