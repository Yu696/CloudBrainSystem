package com.cloudbrain.common.serializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;

/**
 * Jackson 反序列化器：将 JSON Array 或 String 统一转为 Java String
 * <p>
 * 用于实体中存储 JSON 文本的字段（如 PromptTemplate.variables、DiseaseKnowledge.symptoms），
 * 允许前端以数组或字符串形式提交，反序列化后统一存储为 JSON 字符串。
 */
public class JsonArrayOrStringDeserializer extends JsonDeserializer<String> {

    private static final ObjectMapper mapper = new ObjectMapper();

    @Override
    public String deserialize(JsonParser p, DeserializationContext ctx) throws IOException {
        if (p.currentToken().isScalarValue()) {
            // 已经是字符串，直接返回
            return p.getValueAsString();
        }
        // 数组或对象，转为 JSON 字符串
        return mapper.writeValueAsString(p.readValueAsTree());
    }
}
