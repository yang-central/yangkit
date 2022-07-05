package org.yangcentral.yangkit.writter;

public final class YangFormatter {
   private String Indentation = "\t";
   private String endWith = "\n";
   private int columnSize = 80;
   public static final String YANG_FORMAT_TAB = "\t";
   public static final String YANG_FORMAT_FOURSPACES = "    ";
   public static final String YANG_FORMAT_TWOSPACES = "  ";
   public static final String YANG_FORMAT_ONEPACE = " ";
   public static final String YANG_FORMAT_CRLF = "\n";

   private void setTraditionalFormatter() {
      this.Indentation = "    ";
      this.endWith = "\n";
   }

   private void setPrettyFormatter() {
      this.Indentation = "  ";
      this.endWith = "\n";
   }

   public void setIndentation(String indentation) {
      this.Indentation = indentation;
   }

   public int getColumnSize() {
      return this.columnSize;
   }

   public void setColumnSize(int columnSize) {
      this.columnSize = columnSize;
   }

   public String getIndentation() {
      return this.Indentation;
   }

   public String getEndWith() {
      return this.endWith;
   }

   public static YangFormatter getPrettyYangFormatter() {
      YangFormatter formatter = new YangFormatter();
      formatter.setPrettyFormatter();
      return formatter;
   }

   public static YangFormatter getTraditionalYangFormatter() {
      YangFormatter formatter = new YangFormatter();
      formatter.setTraditionalFormatter();
      return formatter;
   }
}
