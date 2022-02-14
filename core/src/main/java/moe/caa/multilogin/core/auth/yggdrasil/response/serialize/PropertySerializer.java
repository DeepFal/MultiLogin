package moe.caa.multilogin.core.auth.yggdrasil.response.serialize;

import com.google.gson.*;
import lombok.NoArgsConstructor;
import moe.caa.multilogin.core.auth.yggdrasil.response.Property;

import java.lang.reflect.Type;

/**
 * Property 的 GSON 序列化程序
 *
 * @see moe.caa.multilogin.core.auth.yggdrasil.response.Property
 */
@NoArgsConstructor
public class PropertySerializer implements JsonSerializer<Property>, JsonDeserializer<Property> {

    @Override
    public Property deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Property ret = Property.builder().build();
        if (json.isJsonObject()) {
            JsonObject root = json.getAsJsonObject();
            ret.setName(root.get("name").getAsString());
            ret.setValue(root.get("value").getAsString());
            if (root.has("signature")) ret.setSignature(root.get("signature").getAsString());
        }
        return ret;
    }

    @Override
    public JsonElement serialize(Property src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject ret = new JsonObject();
        ret.addProperty("name", src.getName());
        ret.addProperty("value", src.getValue());
        if (src.getSignature() != null) ret.addProperty("signature", src.getSignature());
        return ret;
    }
}
