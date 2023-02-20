package org.yangcentral.yangkit.parser;

import org.apache.commons.lang3.StringUtils;
import org.yangcentral.yangkit.base.*;
import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.common.api.exception.Severity;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.register.YangStatementRegister;
import org.yangcentral.yangkit.util.ModelUtil;

import java.util.ArrayList;
import java.util.List;

public class YangParser {
   private int getFirstPosNoformat(String str) {
      if (null == str) {
         return -1;
      } else {
         int length = str.length();

         for(int i = 0; i < length; ++i) {
            char c = str.charAt(i);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
               return i;
            }
         }

         return -1;
      }
   }

   private int getEndPosNoformat(String str) {
      if (null == str) {
         return -1;
      } else {
         int length = str.length();

         for(int i = length - 1; i >= 0; --i) {
            char c = str.charAt(i);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
               return i;
            }
         }

         return -1;
      }
   }

   private String getStringEndSkipformat(String str) {
      if (null == str) {
         return null;
      } else {
         int length = str.length();

         for(int i = length - 1; i >= 0; --i) {
            char c = str.charAt(i);
            if (c != ' ' && c != '\t' && c != '\n' && c != '\r') {
               return str.substring(0, i + 1);
            }
         }

         return null;
      }
   }

   private int getSepBeginPos(String str, int beginPos, int endPos) {
      if (null == str) {
         return -1;
      } else if (beginPos >= endPos) {
         return -1;
      } else if (beginPos >= 0 && endPos >= 0) {
         for(int i = beginPos; i < endPos; ++i) {
            char c = str.charAt(i);
            if (' ' == c || '\t' == c || '\r' == c && '\n' == str.charAt(i + 1) || '\n' == c) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private int getSepEndPos(String str, int beginPos, int endPos) {
      if (null == str) {
         return -1;
      } else if (beginPos >= endPos) {
         return -1;
      } else if (beginPos >= 0 && endPos >= 0) {
         for(int i = beginPos; i <= endPos; ++i) {
            char c = str.charAt(i);
            if (' ' != c && '\t' != c && ('\r' != c || '\n' != str.charAt(i + 1)) && '\n' != c) {
               return i;
            }
         }

         return -1;
      } else {
         return -1;
      }
   }

   private String skipComments(String str) {
      if (null == str) {
         return null;
      } else if (0 == str.length()) {
         return null;
      } else {
         int size = str.length();
         boolean isInSingleComments = false;
         boolean isInMultiComments = false;
         boolean isInDQuotes = false;
         boolean isInSQuotes = false;
         StringBuilder sb = new StringBuilder();

         for(int i = 0; i < size; ++i) {
            char c = str.charAt(i);
            switch (c) {
               case '\n':
                  if (isInSingleComments) {
                     isInSingleComments = false;
                  }

                  if (!isInMultiComments && !isInSingleComments) {
                     sb.append(c);
                  }
                  break;
               case '"':
                  if (isInDQuotes) {
                     if (str.charAt(i - 1) != '\\') {
                        isInDQuotes = false;
                     }
                  } else if (!isInSQuotes) {
                     isInDQuotes = true;
                  }

                  if (!isInSingleComments && !isInMultiComments) {
                     sb.append(c);
                  }
                  break;
               case '\'':
                  if (isInSQuotes) {
                     isInSQuotes = false;
                  } else if (!isInDQuotes) {
                     isInSQuotes = true;
                  }

                  if (!isInSingleComments && !isInMultiComments) {
                     sb.append(c);
                  }
                  break;
               case '*':
                  if (!isInDQuotes && !isInSQuotes) {
                     if (isInMultiComments) {
                        if ('/' == str.charAt(i + 1)) {
                           ++i;
                           isInMultiComments = false;
                        }
                     } else if (!isInSingleComments) {
                        sb.append(c);
                     }
                     break;
                  }

                  sb.append(c);
                  break;
               case '/':
                  if (!isInDQuotes && !isInSQuotes) {
                     if ('/' == str.charAt(i + 1)) {
                        isInSingleComments = true;
                     } else if ('*' == str.charAt(i + 1)) {
                        isInMultiComments = true;
                     }

                     if (!isInSingleComments && !isInMultiComments) {
                        sb.append(c);
                     }
                     break;
                  }

                  sb.append(c);
                  break;
               default:
                  if (!isInMultiComments && !isInSingleComments) {
                     sb.append(c);
                  }
            }
         }

         return sb.toString();
      }
   }

   private YangStatement parseSimpleStatement(String str, YangParserEnv env) throws YangParserException {
      String keyword = null;
      String value = null;
      int curPos = 0;
      YangStatement statement = null;
      if (null == str) {
         return null;
      } else if (null == env) {
         return null;
      } else {
         curPos = env.getCurPos();
         int keywordBeginPos = this.getFirstPosNoformat(str);
         if (-1 == keywordBeginPos) {
            return null;
         } else {
            int noFormatEndPos = this.getEndPosNoformat(str);
            if (-1 == noFormatEndPos) {
               return null;
            } else {
               int keywordEndPos = -1;
               int valueBeginPos = -1;
               int valueEndPos = -1;
               keywordEndPos = this.getSepBeginPos(str, keywordBeginPos, noFormatEndPos);
               if (-1 == keywordEndPos) {
                  keyword = str.substring(keywordBeginPos, noFormatEndPos + 1);
                  value = null;
               } else {
                  keyword = str.substring(keywordBeginPos, keywordEndPos);
                  valueBeginPos = this.getSepEndPos(str, keywordEndPos, noFormatEndPos);
                  if (-1 == valueBeginPos) {
                     env.setCurPos(curPos + keywordEndPos);
                     throw new YangParserException(Severity.ERROR, new Position(env.getFilename(), new LineColumnLocation(env.getCurLine(), env.getCurColumn())), "wrong format, the statement value should be in here.");
                  }

                  value = str.substring(valueBeginPos, noFormatEndPos + 1);
               }

               if (null != value) {
                  if (0 == value.length()) {
                     value = null;
                  } else {
                     value = this.skipComments(value);
                     int newvalueBeginPos = this.getFirstPosNoformat(value);
                     if (-1 == newvalueBeginPos) {
                        newvalueBeginPos = 0;
                     }

                     int newvalueEndPos = this.getEndPosNoformat(value);
                     if (-1 == newvalueEndPos) {
                        newvalueEndPos = value.length() - 1;
                     }

                     value = value.substring(newvalueBeginPos, newvalueEndPos + 1);
                     if (StringUtils.isNotBlank(value) && ('"' == value.charAt(0) || '\'' == value.charAt(0))) {
                        env.setCurPos(curPos + valueBeginPos + newvalueBeginPos);
                        value = this.parseQuotesValue(value, env);
                        env.setCurPos(curPos);
                     }
                  }
               }

               if (ModelUtil.isExtensionKeyword(keyword)) {
                  statement = YangStatementRegister.getInstance().getDefaultUnknownInstance(keyword, value);
                  statement.setElementPosition(new Position(env.getFilename(), new LineColumnLocation(env.getCurLine(), env.getCurColumn())));
                  return statement;
               } else if (!YangBuiltinKeyword.isKeyword(keyword)) {
                  throw new YangParserException(Severity.ERROR, new Position(env.getFilename(), new LineColumnLocation(env.getCurLine(), env.getCurColumn())), ErrorCode.UNRECOGNIZED_KEYWORD.getFieldName());
               } else {
                  statement = YangStatementRegister.getInstance().getYangStatementInstance(new QName(Yang.NAMESPACE, keyword),value);
                  if(statement == null){
                     throw new YangParserException(Severity.ERROR, new Position(env.getFilename(), new LineColumnLocation(env.getCurLine(), env.getCurColumn())), "can not create instance for this statement.");
                  }
                  statement.setElementPosition(new Position(env.getFilename(), new LineColumnLocation(env.getCurLine(), env.getCurColumn())));
                  return statement;
               }
            }
         }
      }
   }

   private String escapeProcess(String value) {
      if (null == value) {
         return null;
      } else {
         StringBuilder sb = new StringBuilder();
         int length = value.length();

         for(int i = 0; i < length; ++i) {
            char c = value.charAt(i);
            if ('\\' == c) {
               if (i + 1 == length) {
                  sb.append(c);
               } else {
                  char nc = value.charAt(i + 1);
                  if ('n' == nc) {
                     sb.append('\n');
                     ++i;
                  } else if ('t' == nc) {
                     sb.append("        ");
                     ++i;
                  } else if ('\\' == nc) {
                     sb.append('\\');
                     ++i;
                  } else if ('"' == nc) {
                     sb.append('"');
                     ++i;
                  } else {
                     sb.append(c);
                  }
               }
            } else {
               sb.append(c);
            }
         }

         return sb.toString();
      }
   }

   private static String trimRight(String str){
      int rightIndex = str.length();
      for(int i = str.length() -1; i >=0;i--){
         char ch = str.charAt(i);
         if(ch == ' '|| ch == '\t'){
            rightIndex--;
         } else {
            break;
         }
      }
      return str.substring(0,rightIndex);
   }

   private String interpretDoubleQuotesValue(String value, YangParserEnv env) {
      if (null == value) {
         return null;
      } else {
         StringBuilder sb = new StringBuilder();
         int valueBeginColumn = env.getCurColumn();
         String[] lineValues = value.split("\n");
         int size = lineValues.length;

         for(int i = 0; i < size; ++i) {
            String lineValue = lineValues[i];
            if(i != (size-1)){
               lineValue = trimRight(lineValue);
            }
            lineValue = this.escapeProcess(lineValue);
            if (null != lineValue) {
               if (0 != i) {
                  int noFormatPos = this.getFirstPosNoformat(lineValue);
                  if (-1 != noFormatPos) {
                     if (noFormatPos > valueBeginColumn) {
                        lineValue = lineValue.substring(valueBeginColumn);
                     } else {
                        lineValue = lineValue.substring(noFormatPos);
                        valueBeginColumn = noFormatPos;
                     }
                  } else {
                     if(lineValue.length() <= valueBeginColumn){
                        lineValue = "";
                     }
                     else {
                        lineValue = lineValue.substring(valueBeginColumn);
                     }
                  }
               }

               if (null != lineValue) {
                  sb.append(lineValue);
               }
            }

            if (i != size - 1) {
               sb.append("\n");
            }
         }

         return sb.toString();
      }
   }

   private String parseQuotesValue(String value, YangParserEnv env) throws YangParserException {
      if (null == value) {
         return null;
      } else if (!value.startsWith("\"") && !value.startsWith("'")) {
         return null;
      } else {
         int doubleQuoteBeginPos = -1;
         int doubleQuoteEndPos = -1;
         int singleQuoteBeginPos = -1;
         int singleQuoteEndPos = -1;
         boolean plusFlag = false;
         ArrayList<String> values = new ArrayList<>(1);
         int curPos = env.getCurPos();

         String str;
         for(int i = 0; i < value.length(); ++i) {
            char c = value.charAt(i);
            if ('"' == c){
               if (-1 == doubleQuoteBeginPos && -1 == singleQuoteBeginPos) {
                  if (values.size() != 0) {
                     if (!plusFlag) {
                        env.calculateCurLineColumn(curPos + i);
                        throw new YangParserException(Severity.ERROR, new Position(env.getFilename(), new LineColumnLocation(env.getCurLine(), env.getCurColumn())), "'+' must be used to concatenat strings.");
                     }

                     plusFlag = false;
                  }

                  doubleQuoteBeginPos = i;
               } else if (-1 != doubleQuoteBeginPos && value.charAt(i - 1) != '\\') {
                  env.setCurPos(curPos + doubleQuoteBeginPos);
                  str = this.interpretDoubleQuotesValue(value.substring(doubleQuoteBeginPos + 1, i), env);
                  env.setCurPos(curPos);
                  values.add(str);
                  doubleQuoteBeginPos = -1;
                  doubleQuoteEndPos = -1;
               }
            } else if ('\'' == c){
               if (-1 == doubleQuoteBeginPos && -1 == singleQuoteBeginPos) {
                  if (values.size() != 0) {
                     if (!plusFlag) {
                        env.calculateCurLineColumn(curPos + i);
                        throw new YangParserException(Severity.ERROR, new Position(env.getFilename(), new LineColumnLocation(env.getCurLine(), env.getCurColumn())), "'+' must be used to concatenat strings.");
                     }

                     plusFlag = false;
                  }

                  singleQuoteBeginPos = i;
               } else if (-1 != singleQuoteBeginPos) {
                  values.add(value.substring(singleQuoteBeginPos + 1, i));
                  singleQuoteBeginPos = -1;
                  singleQuoteEndPos = -1;
               }
            } else if ('+' == c){
               if (-1 == doubleQuoteBeginPos && -1 == singleQuoteBeginPos) {
                  plusFlag = true;
               }
            } else if (-1 == doubleQuoteBeginPos && -1 == singleQuoteBeginPos && (' ' != c)
                    && ('\t' != c)
                    && ('\n' != c)
                    && ('\r' != c)) {
               values.clear();
               env.calculateCurLineColumn(curPos + i);
               throw new YangParserException(Severity.ERROR, new Position(env.getFilename(), new LineColumnLocation(env.getCurLine(), env.getCurColumn())), "invalid char, only '+' can be occured in here.");
            }
         }

         if (-1 == doubleQuoteBeginPos && -1 == doubleQuoteEndPos && -1 == singleQuoteBeginPos && -1 == singleQuoteEndPos) {
            if (0 == values.size()) {
               return null;
            } else {
               StringBuilder sb = new StringBuilder();

               for (String s : values) {
                  str = s;
                  if (null != str) {
                     sb.append(str);
                  }
               }

               return sb.toString();
            }
         } else {
            values.clear();
            return null;
         }
      }
   }

   public List<YangElement> parseYang(String YangStr, YangParserEnv env) throws YangParserException {
      if (null == YangStr){
         return null;
      }
      if (null == env){
         return null;
      }
      boolean statementEnd = true;
      boolean isInSingleComments= false;
      boolean isInMultiComments = false;
      boolean isInDQuotes = false;
      boolean isInSQuotes = false;
      String statementString = null;
      YangStatement rootStatement = null;
      ArrayList<YangElement> elements = null;
      int beginPos = 0;
      int endPos = 0;
      int commentBeginPos = 0;
      //int curPos = env.getCurPos();
      for (int i = 0; i < YangStr.length();i++){
         char c = YangStr.charAt(i);
         switch (c) {
            //simple statement end flag
            case ';' : {
               //if this character is in comments or in quotes, it will be treated as common character
               if (isInSingleComments || isInMultiComments || isInDQuotes || isInSQuotes){
                  continue;
               }

               endPos = i;
               env.setCurPos(beginPos);
               statementString = YangStr.substring(beginPos, endPos);
               YangStatement statement = parseSimpleStatement(statementString,env);
               if (null != statement){
                  //if there is no statement in stack, it's wrong.
                  // a simple statement MUST have parent statement.
                  if (env.getStatements().isEmpty()){
                     throw new YangParserException(Severity.ERROR,
                             new Position(env.getFilename(),new LineColumnLocation(env.getCurLine(),env.getCurColumn())),
                             "a simple statement MUST have parent statement.");
                  }
                  else {
                     YangStatement parentStatement = env.getStatements().peek();
                     parentStatement.addChild(statement);
                  }
               }
               statementEnd = true;
               break;
            }
            //composite statement. This character indicates main statement end.
            case '{': {
               //if this character is in comments or in quotes, it will be treated as common character
               if (isInSingleComments || isInMultiComments || isInDQuotes || isInSQuotes){
                  continue;
               }

               endPos = i;
               env.setCurPos(beginPos);
               statementString = YangStr.substring(beginPos, endPos);

               YangStatement statement = parseSimpleStatement(statementString,env);
               if (null != statement){
                  if (!env.getStatements().isEmpty()){
                     YangStatement parentStatement = env.getStatements().peek();
                     parentStatement.addChild(statement);
                  }
                  else {
                     if (null == rootStatement){
                        rootStatement = statement;
                        if (null == elements){
                           elements = new ArrayList<YangElement>();
                        }
                        elements.add(rootStatement);
                     }
                  }

                  env.getStatements().push(statement);
               }
               statementEnd = true;
               break;
            }
            //composite statement. This character indicates whole statement end.
            case '}': {
               //if this character is in comments or in quotes, it will be treated as common character
               if (isInSingleComments || isInMultiComments || isInDQuotes || isInSQuotes){
                  continue;
               }
               //It's impossible, there must be at least one statement in stack
               if (env.getStatements().isEmpty()){
                  env.setCurPos(i);
                  throw new YangParserException(Severity.ERROR,
                          new Position(env.getFilename(),new LineColumnLocation(env.getCurLine(),env.getCurColumn())),
                          "wrong format,} maybe is more than {.");
               }

               if (!statementEnd){
                  endPos = i;
                  env.setCurPos(beginPos);
                  if (endPos > beginPos){
                     statementString = YangStr.substring(beginPos, endPos);
                     if (statementString.trim().length() > 0){
                        throw new YangParserException(Severity.ERROR,
                                new Position(env.getFilename(),new LineColumnLocation(env.getCurLine(),env.getCurColumn())),
                                "wrong format, missing end character of statement:" + statementString);
                     }
                  }
               }


               //composite statement ends, pop up the statement,
               //because the next statement will be processed should
               //be brother statement of current statement.
               env.getStatements().pop();
               statementEnd = true;
               break;
            }
            //double quote
            case '"':{
               //if this character is in comments, it will be treated as common character
               if (isInSingleComments || isInMultiComments ){
                  continue;
               }
               if (isInDQuotes){
                  //if it's not escape character, it means double quotes end.
                  if (YangStr.charAt(i -1) != '\\'){
                     isInDQuotes = false;
                  }
               }
               else {
                  if (isInSQuotes){
                     //it will be treated as common character
                  }
                  else {
                     //if it's not in double quotes and not in single quotes, it means
                     //double quotes start.
                     isInDQuotes = true;
                  }
               }

               break;

            }

            case '\'': {
               //if this character is in comments, it will be treated as common character
               if (isInSingleComments || isInMultiComments ){
                  continue;
               }
               if (isInSQuotes){
                  //it means single quotes end
                  isInSQuotes = false;
               }
               else {
                  if (isInDQuotes){
                     //it will be treated as common character
                  }
                  else {
                     //if it's not in double quotes and not in single quotes, it means
                     //single quotes start.
                     isInSQuotes = true;
                  }
               }

               break;
            }

            case '/': {
               //if this character is in quotes or in comments, it will be treated as common character
               if (isInDQuotes || isInSQuotes || isInSingleComments || isInMultiComments){
                  continue;
               }
               if ( i == YangStr.length() - 1){

               }
               //single line comment
               else if ('/' ==  YangStr.charAt(i+1)){
                  commentBeginPos = i+2;
                  isInSingleComments = true;
               }
               //multi-line comment
               else if ('*' == YangStr.charAt(i+1)){
                  commentBeginPos = i+2;
                  isInMultiComments = true;
               }

               break;
            }
            case '*': {
               //if this character is in quotes or single comment, it will be treated as common character
               if (isInDQuotes || isInSQuotes || isInSingleComments){
                  continue;
               }
               if (isInMultiComments){

                  if ('/'== YangStr.charAt(i+1)){
                     env.setCurPos(commentBeginPos);
                     YangComment comment = new YangComment();
                     comment.setMultiComment(true);
                     comment.setComment(YangStr.substring(commentBeginPos, i));
                     comment.setElementPosition(new Position(env.getFilename(),
                             new LineColumnLocation(env.getCurLine(),env.getCurColumn())));
                     if (!env.getStatements().isEmpty()){
                        YangStatement parentStatement = env.getStatements().peek();
                        parentStatement.addChild(comment);
                     }
                     else {

                        if (null == elements) {
                           elements = new ArrayList<>();

                        }
                        elements.add(comment);

                     }

                     isInMultiComments = false;

                  }
               }

               break;

            }
            case '\n' : {
               if (true == isInSingleComments) {
                  env.setCurPos(commentBeginPos);
                  YangComment comment = new YangComment();
                  comment.setMultiComment(false);
                  comment.setElementPosition(new Position(env.getFilename(),
                          new LineColumnLocation(env.getCurLine(),env.getCurColumn())));
                  comment.setComment(YangStr.substring(commentBeginPos, i));
                  comment.setElementPosition(new Position(env.getFilename(),
                          new LineColumnLocation(env.getCurLine(),env.getCurColumn())));
                  if (!env.getStatements().isEmpty()){
                     YangStatement parentStatement = env.getStatements().peek();
                     parentStatement.addChild(comment);
                  }
                  else {

                     if (null == elements) {
                        elements = new ArrayList<>();

                     }
                     elements.add(comment);

                  }

                  isInSingleComments = false;
               }
               break;
            }

            case '\t':
            case ' ':
            case '\r':{
               break;
            }
            default: {
               if (isInSingleComments || isInMultiComments || isInDQuotes || isInSQuotes){
                  continue;
               }
               //if statement end, any not blank character means statement start
               if (statementEnd){
                  beginPos = i;
                  statementEnd = false;
               }

            }

         }


      }
      if(!env.getStatements().isEmpty()){
         YangStatement stackedStatement = env.getStatements().peek();
         throw new YangParserException(Severity.ERROR,
                 stackedStatement.getElementPosition(),
                 "wrong format, missing end character of statement:" + stackedStatement);

      }

      if (null != elements){
         boolean findStatement = false;
         for(YangElement element:elements){
            if (null == element){
               continue;
            }

            if (element instanceof YangStatement){
               YangStatement statement = (YangStatement) element;
               if (findStatement){
                  throw new YangParserException(Severity.ERROR,statement.getElementPosition(),
                          "a yang file should only have one root statement.");
               }
               findStatement = true;

            }

         }
      }

      return elements;
   }

}
