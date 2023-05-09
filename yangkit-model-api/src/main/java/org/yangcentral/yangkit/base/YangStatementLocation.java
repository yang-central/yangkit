package org.yangcentral.yangkit.base;

import org.yangcentral.yangkit.model.api.stmt.YangStatement;

import java.util.Objects;
import java.util.Stack;

/**
 * @author : frank feng
 * @since : 7/15/2022 2:45 PM
 */
public class YangStatementLocation implements Location<String>{
    private YangStatement yangStatement;

    public YangStatementLocation(YangStatement yangStatement) {
        this.yangStatement = yangStatement;
    }

    @Override
    public String getLocation() {
        Stack<YangStatementIdentifier> stack = new Stack<>();
        YangStatement cur = yangStatement;
        while(cur.getParentStatement() != null){
            stack.push(new YangStatementIdentifier(cur));
            cur = cur.getParentStatement();
        }
        StringBuilder sb = new StringBuilder();
        while(!stack.empty()){
            YangStatementIdentifier identifier = stack.pop();
            sb.append(identifier.getIdentifier());
            if(!stack.empty()){
                sb.append("/");
            }
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        YangStatementLocation that = (YangStatementLocation) o;
        return yangStatement.equals(that.yangStatement);
    }

    @Override
    public int hashCode() {
        return Objects.hash(yangStatement);
    }
}
