package org.yangcentral.yangkit.data.api.builder;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.yangcentral.yangkit.data.api.model.YangData;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Scanner;

public class YangDataBuilderFactory {
    private static YangDataBuilder builder;
    public static YangDataBuilder getBuilder(){
        if(builder != null){
            return builder;
        }
        InputStream inputStream = YangDataBuilderFactory.class.getResourceAsStream("/builder.json");
        Scanner s = new Scanner(inputStream).useDelimiter("\\A");
        String result = s.hasNext()?s.next():"";
        result.hashCode();
        JsonElement builderElement = JsonParser.parseString(result);
        JsonObject jsonObject = builderElement.getAsJsonObject();
        String builderClassStr = jsonObject.get("builder").getAsString();
        try {
            Class<YangDataBuilder> builderClass = (Class<YangDataBuilder>) Class.forName(builderClassStr);
            builder = builderClass.getConstructor().newInstance();
            return builder;
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }
}
