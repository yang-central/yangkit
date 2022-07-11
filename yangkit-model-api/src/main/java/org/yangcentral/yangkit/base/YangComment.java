package org.yangcentral.yangkit.base;
/**
 * the class for comment of YANG file, support sing line comment and multiline comment
 * @version 1.0.
 * @author frank feng
 * @since 7/8/2022
 */
public class YangComment implements YangElement {
   private boolean isMultiComment;
   private String comment;
   private Position pos;

   public boolean isMultiComment() {
      return this.isMultiComment;
   }

   public void setMultiComment(boolean isMultiComment) {
      this.isMultiComment = isMultiComment;
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
