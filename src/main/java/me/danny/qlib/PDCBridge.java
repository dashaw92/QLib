package me.danny.qlib;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.util.NoSuchElementException;

public final class PDCBridge {

    public static <T extends Serializable> PersistentDataType<PersistentDataContainer, T> defineType(Plugin plugin, Class<T> clazz) {
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
                NamespacedKey key = new NamespacedKey(plugin, getComplexType().getSimpleName());
                try {
                    ByteArrayOutputStream bytes = new ByteArrayOutputStream();
                    ObjectOutputStream os = new ObjectOutputStream(bytes);
                    os.writeObject(complex);
                    os.flush();
                    byte[] serialized = bytes.toByteArray();
                    data.set(key, PersistentDataType.BYTE_ARRAY, serialized);
                } catch(IOException ex) {
                    plugin.getLogger().warning("[defineType::toPrimitive] Failed to serialize record to data container. Nothing has been set.");
                    ex.printStackTrace();
                }
                return data;
            }

            @SuppressWarnings("unchecked")
            @Override
            public T fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                NamespacedKey key = new NamespacedKey(plugin, getComplexType().getSimpleName());
                byte[] data = primitive.get(key, PersistentDataType.BYTE_ARRAY);
                if(data == null) throw new NoSuchElementException("Provided container does not contain a serialized " + getComplexType().getCanonicalName() + "!");
                try {
                    ByteArrayInputStream bytes = new ByteArrayInputStream(data);
                    ObjectInputStream reader = new ObjectInputStream(bytes);
                    return (T) reader.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    plugin.getLogger().warning("[defineType::fromPrimitive] Failed to deserialize record from data container. Must return null.");
                    e.printStackTrace();
                }

                throw new NoSuchElementException("Provided container does not contain a serialized " + getComplexType().getCanonicalName() + "!");
            }
        };
    }
}
