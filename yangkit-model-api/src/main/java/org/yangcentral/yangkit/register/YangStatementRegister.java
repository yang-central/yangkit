package org.yangcentral.yangkit.register;

import org.yangcentral.yangkit.common.api.QName;
import org.yangcentral.yangkit.model.api.schema.YangSchemaContext;
import org.yangcentral.yangkit.model.api.stmt.YangStatement;
import org.yangcentral.yangkit.model.api.stmt.YangUnknown;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author : frank feng
 * @date : 7/14/2022 8:04 PM
 */
public class YangStatementRegister {
    private static final YangStatementRegister ourInstance = new YangStatementRegister();
    private Map<QName, YangStatementParserPolicy> policyMap = new ConcurrentHashMap();
    private Class<? extends YangUnknown> defaultUnknownClass;
    private Class<? extends YangSchemaContext> yangSchemaContextClass;

    public static YangStatementRegister getInstance() {
        return ourInstance;
    }

    private YangStatementRegister() {

    }

    public YangStatementParserPolicy getStatementParserPolicy(QName keyword) {
        return null == keyword ? null : (YangStatementParserPolicy)this.policyMap.get(keyword);
    }

    public YangStatement getYangStatementInstance(QName keyword,String argStr){
        YangStatementParserPolicy yangStatementParserPolicy = getStatementParserPolicy(keyword);
        if(null == yangStatementParserPolicy){
            return null;
        }
        try {
            Constructor<? extends YangStatement> constructor = yangStatementParserPolicy.getClazz().getConstructor(String.class);
            return constructor.newInstance(argStr);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    public YangUnknown getDefaultUnknownInstance(String keyword, String argStr) {
        if(defaultUnknownClass == null){
            return null;
        }
        try {
            Constructor constructor = defaultUnknownClass.getConstructor(String.class,String.class);
            return (YangUnknown) constructor.newInstance(keyword,argStr);
        } catch (NoSuchMethodException | InvocationTargetException | InstantiationException
                | IllegalAccessException e) {
            return null;
        }
    }
    public void registerDefaultUnknown(Class<?extends YangUnknown> clazz){
        defaultUnknownClass = clazz;
    }

    public YangSchemaContext getSchemeContextInstance() {
        if(yangSchemaContextClass == null){
            return null;
        }
        try {
            Constructor<? extends YangSchemaContext> constructor = yangSchemaContextClass.getConstructor();
            return constructor.newInstance();
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
            return null;
        }
    }

    public void registerYangSchemaContext(Class<? extends YangSchemaContext> clazz){
        yangSchemaContextClass = clazz;
    }

    public Collection<YangStatementParserPolicy> getStatementParserPolicys() {
        return this.policyMap.values();
    }

    public void register(QName keyword, YangStatementParserPolicy statementPolicy) {
        if (this.policyMap.containsKey(keyword)) {
            this.policyMap.replace(keyword, statementPolicy);
        } else {
            this.policyMap.put(keyword, statementPolicy);
        }
    }

    public void unRegister(QName keyword) {
        this.policyMap.remove(keyword);
    }

}
