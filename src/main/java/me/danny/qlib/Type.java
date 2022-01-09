package me.danny.qlib;

import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataAdapterContext;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

public final class Type {

    public static final PersistentDataType<PersistentDataContainer, UUID> UUID = new UUIDType();

    private static final class UUIDType implements PersistentDataType<PersistentDataContainer, UUID> {
        private static final NamespacedKey key = NamespacedKey.fromString("qlib:uuid");

        @Override
        public Class<PersistentDataContainer> getPrimitiveType() {
            return PersistentDataContainer.class;
        }

        @Override
        public Class<UUID> getComplexType() {
            return UUID.class;
        }

        @Override
        public PersistentDataContainer toPrimitive(UUID complex, PersistentDataAdapterContext context) {
            PersistentDataContainer pdc = context.newPersistentDataContainer();
            var lsb = complex.getLeastSignificantBits();
            var msb = complex.getMostSignificantBits();

            pdc.set(key, PersistentDataType.LONG_ARRAY, new long[] { lsb, msb });
            return pdc;
        }

        @Override
        public UUID fromPrimitive(PersistentDataContainer primitive, PersistentDataAdapterContext context) {
            if(!primitive.has(key, PersistentDataType.LONG_ARRAY)) return null;

            long[] bits = primitive.get(key, PersistentDataType.LONG_ARRAY);
            if(bits == null || bits.length < 2) return null;

            var lsb = bits[0];
            var msb = bits[1];

            return new UUID(lsb, msb);
        }
    }
}