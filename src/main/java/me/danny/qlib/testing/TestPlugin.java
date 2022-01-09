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

import java.util.UUID;

public final class TestPlugin extends JavaPlugin {

    private final PersistentDataType<?, SimpleRecord> SIMPLE_RECORD = PDCBridge.defineType(this, SimpleRecord.class);
    private final PersistentDataType<?, TestSameName> TEST_SAME_NAME = PDCBridge.defineType(this, TestSameName.class);

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

            var it = new ItemStack(Material.STICK);
            var meta = it.getItemMeta();
            meta.setDisplayName("Contains data");

//            var simple = new SimpleRecord(Math.random() * 100 + " . random", (int) (Math.random() * 10), new byte[] {1, 2, 3}, p.getUniqueId());
//            var key = new NamespacedKey(inst, "simple_record");
//            meta.getPersistentDataContainer().set(key, inst.SIMPLE_RECORD, simple);

            var sameName = new TestSameName("string field", 20);
            var key2 = new NamespacedKey(inst, "same_name");
            meta.getPersistentDataContainer().set(key2, inst.TEST_SAME_NAME, sameName);

            it.setItemMeta(meta);
            p.getInventory().addItem(it);
            return true;
        }
    }

    public record SimpleRecord(String stringField, int someInt, byte[] extraData, UUID test) {}
    public record TestSameName(String field, int Field) {}
}
