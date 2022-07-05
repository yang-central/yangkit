package org.yangcentral.yangkit.data.api.operation;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.data.api.exception.YangDataException;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import org.yangcentral.yangkit.model.api.stmt.DataNode;
import javax.annotation.Nonnull;

public interface YangDataDocumentOperator extends Cloneable {
   YangDataDocument getDocument();

   void create(AbsolutePath var1, YangData<? extends DataNode> var2) throws YangDataException;

   void delete(AbsolutePath var1) throws YangDataException;

   void merge(@Nonnull YangDataDocument var1) throws YangDataException;

   void merge(AbsolutePath var1, YangData<? extends DataNode> var2) throws YangDataException;

   void replace(YangDataDocument var1) throws YangDataException;

   void replace(AbsolutePath var1, YangData<? extends DataNode> var2) throws YangDataException;
}
