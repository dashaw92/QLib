package me.danny.qlib;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.NoSuchElementException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public final class PDCBridge {
    private static final Gson gson = new GsonBuilder()
            .disableHtmlEscaping()
            .serializeNulls()
            .create();



    public static <T> PersistentDataType<PersistentDataContainer, T> defineType(Plugin plugin, Class<T> clazz) {
        if(clazz.isRecord()) return defineTypeRecord(plugin, clazz);
        return defineTypeClass(plugin, clazz);
    }

    private static <T> PersistentDataType<PersistentDataContainer, T> defineTypeClass(Plugin plugin, Class<T> clazz) {
        if(clazz.isRecord()) throw new UnsupportedOperationException("Cannot define a record type.");
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

    private static <T> PersistentDataType<PersistentDataContainer, T> defineTypeRecord(Plugin plugin, Class<T> clazz) {
        if(!Serializable.class.isAssignableFrom(clazz)) throw new UnsupportedOperationException("Cannot define a record type that does not implement %s.".formatted(Serializable.class.getCanonicalName()));
        var key = new NamespacedKey(plugin, "bytes");

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
                try(var bytes = new ByteArrayOutputStream(); var gzip = new GZIPOutputStream(bytes); var os = new ObjectOutputStream(gzip)) {
                    os.writeObject(complex);
                    os.flush();
                    gzip.close();
                    var written = bytes.toByteArray();
                    data.set(key, PersistentDataType.BYTE_ARRAY, written);
                    return data;
                } catch(IOException ex) {
                    plugin.getLogger().warning("[defineTypeRecord::toPrimitive] Failed to serialize record to data container. Nothing has been set.");
                    return context.newPersistentDataContainer();
                }
            }

            @SuppressWarnings("unchecked")
            @Override
            public T fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                 var read = primitive.get(key, PersistentDataType.BYTE_ARRAY);
                 if(read == null) throw new NoSuchElementException("[defineTypeRecord::fromPrimitive] Provided container does not contain a serialized " + getComplexType().getCanonicalName() + "!");
                 try(var bytes = new ByteArrayInputStream(read); var gzip = new GZIPInputStream(bytes); var is = new ObjectInputStream(gzip)) {
                    return (T) is.readObject();
                 } catch(IOException | ClassNotFoundException ex) {
                     ex.printStackTrace();
                     plugin.getLogger().warning("[defineTypeRecord::fromPrimitive] Failed to deserialize record from data container. Must return null.");
                     return null;
                 }
            }
        };
    }
}
