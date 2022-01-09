package me.danny.qlib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.GsonBuildConfig;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class PDCBridge {
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();

    public static <T> PersistentDataType<PersistentDataContainer, T> defineType(Plugin plugin, Class<T> clazz) {
        if(clazz.isRecord()) throw new UnsupportedOperationException("Cannot define a record type. Support may be added with deserializers.");
        var key = new NamespacedKey(plugin, "json");

        return new PersistentDataType<>() {

            @Override
            public Class<PersistentDataContainer> getPrimitiveType() {
                return PersistentDataContainer.class;
            }

            @Override
            public Class<T> getComplexType() {
                return clazz;
            }

            @Override
            public PersistentDataContainer toPrimitive(T complex, PersistentDataAdapterContext context) {
                PersistentDataContainer data = context.newPersistentDataContainer();
                String json = gson.toJson(complex);
                data.set(key, PersistentDataType.STRING, json);
                return data;
            }

            @Override
            public T fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                String json = primitive.get(key, PersistentDataType.STRING);
                return gson.fromJson(json, getComplexType());
            }
        };
    }
}
