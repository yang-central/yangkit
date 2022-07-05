package org.yangcentral.yangkit.data.api.operation;

import org.yangcentral.yangkit.common.api.AbsolutePath;
import org.yangcentral.yangkit.data.api.model.YangData;
import org.yangcentral.yangkit.data.api.model.YangDataDocument;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class DataChangeNotifier {
   private YangDataDocument document;
   private List<DataChangeListener> listeners;

   public DataChangeNotifier(YangDataDocument document) {
      this.document = document;
   }

   public void addListener(DataChangeListener listener) {
      if (null == this.listeners) {
         this.listeners = new ArrayList();
         this.listeners.add(listener);
      } else {
         Iterator var2 = this.listeners.iterator();

         DataChangeListener l;
         do {
            if (!var2.hasNext()) {
               this.listeners.add(listener);
               return;
            }

            l = (DataChangeListener)var2.next();
         } while(l != listener);

      }
   }

   public void notify(AbsolutePath path, DataChangeType dataChangeType, YangData<?> oldData, YangData<?> newData) {
      if (null != this.listeners) {
         Iterator var5 = this.listeners.iterator();

         while(var5.hasNext()) {
            DataChangeListener listener = (DataChangeListener)var5.next();
            if (null != listener) {
               listener.processDataChange(this.document, path, dataChangeType, oldData, newData);
            }
         }

      }
   }
}
