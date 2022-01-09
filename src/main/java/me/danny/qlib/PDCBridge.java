package me.danny.qlib;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.*;

public final class PDCBridge {

    public static <T> PersistentDataType<PersistentDataContainer, T> defineType(Plugin plugin, Class<T> clazz) {
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
                Set<String> encountered = new HashSet<>();
                for (Field field : complex.getClass().getDeclaredFields()) {
                    if(!encountered.add(field.getName().toLowerCase())) {
                        plugin.getLogger().warning("[defineType::toPrimitive] Cannot store types with same-name fields (case insensitive!)! Aborting.");
                        return context.newPersistentDataContainer();
                    }

                    field.setAccessible(true);
                    var key = new NamespacedKey(plugin, field.getName());
                    try {
                        var type = field.getType();
                        var setter = setters.get(type);
                        if (setter == null) {
                            plugin.getLogger().warning("[defineType::toPrimitive] Encountered an invalid field type: field \"" + field.getName() + "\" is " + type.getCanonicalName() + ".");
                            plugin.getLogger().warning("[defineType::toPrimitive] Cannot store complex types recursively. Aborting.");
                            return null;
                        }

                        setter.set(data, key, field.get(complex));
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
                return data;
            }

            @Override
            public T fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
                try {
                    return getComplexType().getDeclaredConstructor().newInstance();
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };
    }

    private static final Map<Class<?>, Setter> setters = new HashMap<>();

    static {
        //Primitives
        setters.put(byte.class, (data, key, obj) -> data.set(key, PersistentDataType.BYTE, (byte) obj));
        setters.put(short.class, (data, key, obj) -> data.set(key, PersistentDataType.SHORT, (short) obj));
        setters.put(int.class, (data, key, obj) -> data.set(key, PersistentDataType.INTEGER, (int) obj));
        setters.put(long.class, (data, key, obj) -> data.set(key, PersistentDataType.LONG, (long) obj));
        setters.put(byte[].class, (data, key, obj) -> data.set(key, PersistentDataType.BYTE_ARRAY, (byte[]) obj));
        setters.put(int[].class, (data, key, obj) -> data.set(key, PersistentDataType.INTEGER_ARRAY, (int[]) obj));
        setters.put(long[].class, (data, key, obj) -> data.set(key, PersistentDataType.LONG_ARRAY, (long[]) obj));
        setters.put(float.class, (data, key, obj) -> data.set(key, PersistentDataType.FLOAT, (float) obj));
        setters.put(double.class, (data, key, obj) -> data.set(key, PersistentDataType.DOUBLE, (double) obj));

        //Boxed
        setters.put(Byte.class, setters.get(byte.class));
        setters.put(Short.class, setters.get(short.class));
        setters.put(Integer.class, setters.get(int.class));
        setters.put(Long.class, setters.get(long.class));
        setters.put(Float.class, setters.get(float.class));
        setters.put(Double.class, setters.get(double.class));

        //Complex
        setters.put(String.class, (data, key, obj) -> data.set(key, PersistentDataType.STRING, (String) obj));
        setters.put(UUID.class, (data, key, obj) -> data.set(key, Type.UUID, (UUID) obj));
    }

    private interface Setter {
        void set(PersistentDataContainer data, NamespacedKey key, Object obj);
    }
}
