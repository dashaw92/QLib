package me.danny.qlib.testing;

import me.danny.qlib.PDCBridge;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.UUID;

public final class TestPlugin extends JavaPlugin {

    private final PersistentDataType<?, SimpleRecord> SIMPLE_RECORD = PDCBridge.defineType(this, SimpleRecord.class);

    @Override
    public void onEnable() {
        getCommand("testpdcbridge").setExecutor(new TestPDCBridgeCommand(this));
    }

    public final class TestPDCBridgeCommand implements CommandExecutor {

        private final TestPlugin inst;

        public TestPDCBridgeCommand(TestPlugin inst) {
            this.inst = inst;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            if(!(sender instanceof Player p)) return true;
            var key2 = new NamespacedKey(inst, "simple_record");

            if(p.getInventory().getItemInMainHand().getType() == Material.STICK) {
                var it = p.getInventory().getItemInMainHand();
                var meta = it.getItemMeta();
                var simpleRecordOut = meta.getPersistentDataContainer().get(key2, inst.SIMPLE_RECORD);
                p.sendMessage("Got : " + simpleRecordOut);
                return true;
            }

            var it = new ItemStack(Material.STICK);
            var meta = it.getItemMeta();
            meta.setDisplayName("Contains data");

            var simpleRecord = new SimpleRecord(
                    "danny was here",
                    22,
                    new byte[] { 1, 2, 3 },
                    p.getUniqueId()
            );

            meta.getPersistentDataContainer().set(key2, inst.SIMPLE_RECORD, simpleRecord);

            it.setItemMeta(meta);
            p.getInventory().addItem(it);
            return true;
        }
    }

    public static final class SimpleRecord {
        private final String stringField;
        private final int someInt;
        private final byte[] extraData;
        private final UUID test;

        public SimpleRecord(String stringField, int someInt, byte[] extraData, UUID test) {
            this.stringField = stringField;
            this.someInt = someInt;
            this.extraData = extraData;
            this.test = test;
        }

        @Override
        public String toString() {
            return "SimpleRecord{" +
                    "stringField='" + stringField + '\'' +
                    ", someInt=" + someInt +
                    ", extraData=" + Arrays.toString(extraData) +
                    ", test=" + test +
                    '}';
        }
    }
}
