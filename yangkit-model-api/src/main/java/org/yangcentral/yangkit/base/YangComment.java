package org.yangcentral.yangkit.base;

public class YangComment implements YangElement {
   private boolean isMulitiComment;
   private String comment;
   private Position pos;

   public boolean isMulitiComment() {
      return this.isMulitiComment;
   }

   public void setMulitiComment(boolean isMulitiComment) {
      this.isMulitiComment = isMulitiComment;
   }

   public String getComment() {
      return this.comment;
   }

   public void setComment(String comment) {
      this.comment = comment;
   }

   public Position getElementPosition() {
      return this.pos;
   }

   public void setElementPosition(Position position) {
      this.pos = position;
   }
}
