package org.yangcentral.yangkit.base;
/**
 * the interface for yang element including comment,yang statement
 * @version 1.0.0
 * @author frank feng
 * @since 7/8/2022
 */
public interface YangElement {
   Position getElementPosition();

   void setElementPosition(Position var1);
}
