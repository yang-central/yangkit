package org.yangcentral.yangkit.writter;

import org.yangcentral.yangkit.base.YangBuiltinKeyword;
import org.yangcentral.yangkit.base.YangComment;
import org.yangcentral.yangkit.base.YangElement;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.stmt.Extension;
import org.yangcentral.yangkit.model.api.stmt.Identifiable;
import org.yangcentral.yangkit.model.api.stmt.YangBuiltinStatement;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;

public class YangWriter {
   private static String convert2DQValue(String value) {
      if (null == value) {
         return null;
      } else {

         StringBuilder sb = new StringBuilder();
         sb.append("\"");
         int length = value.length();

         for(int i = 0; i < length; ++i) {
            char c = value.charAt(i);
            if ('"' == c) {
               sb.append("\\\"");
            } else if ('\\' == c) {
               sb.append("\\\\\\\\");
//            } else if ('\n' == c) {
//               sb.append("\\n");
            } else {
               sb.append(c);
            }
         }

         sb.append("\"");
         return sb.toString();
      }
   }

   private static String convert2QuotesValue(String value) {
      boolean useSingleQuote = false;
      return null == value ? null : convert2DQValue(value);
   }

   private static String buildLinePrefix(int size) {
      StringBuilder sBuffer = new StringBuilder();

      for(int i = 0; i < size; ++i) {
         sBuffer.append(" ");
      }

      return sBuffer.toString();
   }

   private static int getLinebreakerPos(int beginPos, String inStr, int columnSize) {
      if (null == inStr) {
         return -1;
      } else {
         int linebreakerPos = -1;
         int inlength = inStr.length();
         int lfPos = inStr.indexOf("\n");
         int maxlineSize = columnSize - beginPos;

         if (-1 == lfPos) {
            linebreakerPos = Math.min(inlength, maxlineSize);
         } else {
            linebreakerPos = Math.min(lfPos, maxlineSize);
         }

         if (linebreakerPos == inlength) {
            return linebreakerPos;
         } else {
            char clinebreaker = inStr.charAt(linebreakerPos);
            if (' ' != clinebreaker && '\t' != clinebreaker && '\n' != clinebreaker) {
               int tempLinebreakerPos = linebreakerPos;

               for(int i = linebreakerPos; i >= linebreakerPos - 10; --i) {
                  char temp = inStr.charAt(i);
                  if (' ' == temp || '\t' == temp || '\n' == temp) {
                     tempLinebreakerPos = i + 1;
                     break;
                  }
               }

               if (tempLinebreakerPos != linebreakerPos) {
                  linebreakerPos = tempLinebreakerPos;
               }

               for(char temp = inStr.charAt(linebreakerPos - 1); temp == '\\'; temp = inStr.charAt(linebreakerPos - 1)) {
                  --linebreakerPos;
               }
            } else if ('\n' == clinebreaker && linebreakerPos >= 1 && '\r' == inStr.charAt(linebreakerPos - 1)) {
               --linebreakerPos;
            }

            return linebreakerPos;
         }
      }
   }

   private static boolean needLineBreaker(int beginPos, String inStr, int columnSize) {
      if (null == inStr) {
         return false;
      } else if (!inStr.contains(" ") && !inStr.contains("\t")) {
         if (0 > beginPos) {
            return false;
         } else {
            if (columnSize <= beginPos) {
               columnSize = beginPos + 40;
            }

            if (beginPos >= 40) {
               columnSize += 40;
            }

            int linebreakerPos = getLinebreakerPos(beginPos, inStr, columnSize);
            return linebreakerPos != inStr.length();
         }
      } else {
         return true;
      }
   }

   private static String formatString(int beginPos, String inStr, int columnSize, String startWith) {
      StringBuilder sb = new StringBuilder();
      if (null == inStr) {
         return null;
      }
      if (0 > beginPos) {
         return null;
      }
      if (columnSize <= beginPos) {
         columnSize = beginPos + 40;
      }

      if (beginPos >= 40) {
         columnSize += 40;
      }

      int linebreakerPos = getLinebreakerPos(beginPos, inStr, columnSize);
      if (linebreakerPos == inStr.length()) {
         return inStr;
      }
      sb.append(inStr.substring(0, linebreakerPos));//first line
      String nextStr = null;
      int nextBeginPos = linebreakerPos;
      boolean linebreakerIscrlf = false;
      if ('\n' != inStr.charAt(linebreakerPos)) {
         if ('\r' == inStr.charAt(linebreakerPos) && '\n' == inStr.charAt(linebreakerPos + 1)) {
            sb.append("\r\n");
            sb.append(buildLinePrefix(beginPos));
            nextStr = inStr.substring(linebreakerPos + 2);
         } else {
            sb.append(startWith);
            sb.append("+");
            sb.append("\n");
            nextStr = startWith.concat(inStr.substring(linebreakerPos));
            sb.append(buildLinePrefix(beginPos -1));
         }
      } else {
         sb.append("\n");
         sb.append(buildLinePrefix(beginPos));
         nextStr = inStr.substring(linebreakerPos +1);
      }


      sb.append(formatString(beginPos, nextStr, columnSize, startWith));
      return sb.toString();


   }

   public static String toYangString(YangElement element, YangFormatter format, String curIndentation) {
      StringBuilder sb = new StringBuilder();
      if (null == element) {
         return null;
      } else {
         if (null == format) {
            format = YangFormatter.getTraditionalYangFormatter();
         }

         if (null != curIndentation) {
            sb.append(curIndentation);
         }

         if (element instanceof YangComment) {
            YangComment comment = (YangComment)element;
            if (comment.isMultiComment()) {
               sb.append("/*");
               sb.append(comment.getComment());
               sb.append("*/");
            } else {
               sb.append("//");
               sb.append(comment.getComment());
               sb.append("\n");
            }
         } else if (element instanceof YangStatement) {
            YangStatement statement = (YangStatement)element;
            boolean isYinElement = false;
            boolean hasAgrument = true;
            if (statement instanceof YangBuiltinStatement) {
               QName keyword = statement.getYangKeyword();

               assert null != keyword;

               sb.append(keyword.getLocalName());
               YangBuiltinKeyword builtinKeyword = YangBuiltinKeyword.from(keyword);
               if (builtinKeyword.getArgument() == null) {
                  hasAgrument = false;
               }

               isYinElement = builtinKeyword.isYinElement();
            } else {
               YangUnknown unknown = (YangUnknown)statement;
               Extension extension = unknown.getExtension();
               sb.append(unknown.getKeyword());
               if (extension != null) {
                  if (extension.getArgument() != null) {
                     isYinElement = extension.getArgument().isYinElement();
                  } else {
                     hasAgrument = false;
                  }
               }
            }

            if (hasAgrument) {
               int argPos = 0;
               if (isYinElement) {
                  sb.append("\n");
                  sb.append(curIndentation);
                  sb.append(format.getIndentation());
                  argPos = curIndentation.length() + format.getIndentation().length();
               } else {
                  sb.append(" ");
                  argPos = sb.length();
               }

               String argString = statement.getArgStr();
               if (null != argString && argString.length() > 0) {
                  if (!(statement instanceof Identifiable)) {
                     argString = convert2QuotesValue(statement.getArgStr());
                  } else if (needLineBreaker(argPos, argString, format.getColumnSize())) {
                     argString = convert2QuotesValue(statement.getArgStr());
                  }

                  String adjustArgString = formatString(argPos+1, argString, format.getColumnSize(), String.valueOf(argString.charAt(0)));
                  sb.append(adjustArgString);
               } else {
                  argString = "\"\"";
                  sb.append(argString);
               }
            }

            if (0 == statement.getSubElements().size()) {
               sb.append(";");
            } else {
               sb.append(" {");
               sb.append(format.getEndWith());
               String nextIndentation;
               if (null == curIndentation) {
                  nextIndentation = format.getIndentation();
               } else {
                  nextIndentation = curIndentation + format.getIndentation();
               }

               for (YangElement subElement : statement.getSubElements()) {
                  sb.append(toYangString(subElement, format, nextIndentation));
               }

               if (null != curIndentation) {
                  sb.append(curIndentation);
               }

               sb.append("}");
            }
         }

         sb.append(format.getEndWith());
         return sb.toString();
      }
   }
}
