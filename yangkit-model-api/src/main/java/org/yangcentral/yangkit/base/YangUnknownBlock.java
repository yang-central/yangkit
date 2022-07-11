package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.model.api.stmt.YangUnknown;
/**
 * define yang unknown block for YIN
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public abstract class YangUnknownBlock<S> implements YangElement {
   private S block;
   private Position position;

   public YangUnknownBlock(S block) {
      this.block = block;
   }

   protected S getBlock() {
      return this.block;
   }

   public Position getElementPosition() {
      return this.position;
   }

   public void setElementPosition(Position position) {
      this.position = position;
   }

   public abstract YangUnknown build(YangContext context);
}
