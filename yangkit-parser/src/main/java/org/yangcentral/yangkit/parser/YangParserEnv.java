package org.yangcentral.yangkit.parser;

import org.yangcentral.yangkit.base.Position;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import java.util.Map;
import java.util.Stack;
import java.util.concurrent.ConcurrentHashMap;

public class YangParserEnv implements Cloneable {
   private Stack<YangStatement> statements = new Stack<>();
   private String filename;
   private String yangStr;
   private ParseStatus status;
   private String errorMsg;
   private int curLine;
   private int curColumn;
   private int curPos;
   private Map<Integer, Position> positionMap;

   public YangParserEnv() {
      this.status = ParseStatus.UNPARSE;
      this.curLine = 1;
      this.curColumn = 1;
      this.curPos = -1;
      this.positionMap = new ConcurrentHashMap<>();
   }

   public String getYangStr() {
      return this.yangStr;
   }

   public void setYangStr(String yangStr) {
      this.yangStr = yangStr;
   }

   public ParseStatus getStatus() {
      return this.status;
   }

   public void setStatus(ParseStatus status) {
      this.status = status;
   }

   public String getErrorMsg() {
      return this.errorMsg;
   }

   public void setErrorMsg(String errorMsg) {
      this.errorMsg = errorMsg;
   }

   public String getFilename() {
      return this.filename;
   }

   public void setFilename(String filename) {
      this.filename = filename;
   }

   public Stack<YangStatement> getStatements() {
      return this.statements;
   }

   public int getCurPos() {
      return this.curPos;
   }

   public void setCurPos(int curPos) {
      if (this.positionMap.containsKey(curPos)) {
         this.curPos = curPos;
         Position curPosition = (Position)this.positionMap.get(curPos);
         LineColumnLocation location = (LineColumnLocation)curPosition.getLocation();
         this.curLine = location.getLine();
         this.curColumn = location.getColumn();
      } else {
         this.calculateCurLineColumn(curPos);
         this.curPos = curPos;
         this.positionMap.put(curPos, new Position(this.filename, new LineColumnLocation(this.getCurLine(), this.getCurColumn())));
      }
   }

   public int getCurLine() {
      return this.curLine;
   }

   public int getCurColumn() {
      return this.curColumn;
   }

   public void calculateCurLineColumn(int curPos) {
      if (null != this.yangStr) {
         if (curPos >= 0) {
            int curLine = this.getCurLine();
            int curColumn = this.getCurColumn();
            int preCurPos = this.curPos;
            int tempPos;
            char tempChar;
            if (curPos > preCurPos) {
               for(tempPos = preCurPos + 1; tempPos <= curPos; ++tempPos) {
                  tempChar = this.yangStr.charAt(tempPos);
                  if ('\n' == tempChar) {
                     ++curLine;
                     curColumn = 0;
                  } else if ('\t' == tempChar) {
                     curColumn += 8;
                  } else if ('\r' == tempChar) {
                     curColumn = 0;
                  } else {
                     ++curColumn;
                  }
               }
            } else if (curPos < preCurPos) {
               for(tempPos = preCurPos; tempPos >= curPos; --tempPos) {
                  tempChar = this.yangStr.charAt(tempPos);
                  if ('\n' == tempChar) {
                     --curLine;
                  }
               }

               for(tempPos = curPos; tempPos >= 0; --tempPos) {
                  tempChar = this.yangStr.charAt(tempPos);
                  if (tempChar == '\n') {
                     break;
                  }

                  if ('\t' == tempChar) {
                     curColumn += 8;
                  } else if ('\r' == tempChar) {
                     curColumn = 0;
                  } else {
                     ++curColumn;
                  }
               }
            }

            this.curLine = curLine;
            this.curColumn = curColumn;
         }
      }
   }

   public YangParserEnv clone() {
      YangParserEnv env = new YangParserEnv();
      env.curColumn = this.curColumn;
      env.curLine = this.curLine;
      env.curPos = this.curPos;
      env.filename = this.filename;
      env.status = this.status;
      return env;
   }

   public static enum ParseStatus {
      UNPARSE,
      PARSING,
      PARSED,
      FAILED;
   }
}
