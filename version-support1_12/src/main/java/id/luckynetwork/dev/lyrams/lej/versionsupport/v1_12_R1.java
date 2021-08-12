package id.luckynetwork.dev.lyrams.lej.versionsupport;

import id.luckynetwork.dev.lyrams.lej.versionsupport.enums.LEnchants;
import id.luckynetwork.dev.lyrams.lej.versionsupport.enums.LItemStack;
import net.minecraft.server.v1_12_R1.DamageSource;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.craftbukkit.v1_12_R1.entity.CraftPlayer;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

public class v1_12_R1 extends VersionSupport {

    public v1_12_R1(Plugin plugin) {
        super(plugin);
    }

    @Override
    public ItemStack getItemInHand(Player player) {
        return player.getInventory().getItemInMainHand();
    }

    @Override
    public void kill(Player player) {
        ((CraftPlayer) player).getHandle().damageEntity(DamageSource.OUT_OF_WORLD, 1000);
    }

    @Override
    public double getMaxHealth(Player player) {
        return player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
    }

    @Override
    public ItemStack getItemByName(String name, int amount, int damage) {
        name = name.toUpperCase();
        ItemStack cachedItem = this.getMaterialCache().getIfPresent(name);
        if (cachedItem != null) {
            return cachedItem;
        }

        ItemStack itemStack = null;
        try {
            itemStack = LItemStack.valueOf(name).getItemStack();
        } catch (Exception ignored) {
        }

        if (itemStack == null) {
            Material material = Material.getMaterial(name);
            if (material != null) {
                itemStack = new ItemStack(material);
            }
        }

        if (itemStack != null) {
            if (amount == -1) {
                if (itemStack.getMaxStackSize() > 1) {
                    itemStack.setAmount(itemStack.getMaxStackSize());
                } else {
                    itemStack.setAmount(1);
                }
            } else {
                itemStack.setAmount(amount);
            }

            if (itemStack.getDurability() != (short) 0) {
                itemStack.setDurability((short) damage);
            }

            this.getMaterialCache().put(name, itemStack);
        }

        return itemStack;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public Enchantment getEnchantName(String name) {
        name = name.toUpperCase();
        Enchantment cachedEnchant = this.getEnchantmentCache().getIfPresent(name);
        if (cachedEnchant != null) {
            return cachedEnchant;
        }

        Enchantment enchantment = null;
        try {
            enchantment = LEnchants.valueOf(name).getEnchantment();
        } catch (Exception ignored) {
        }

        if (enchantment == null) {
            if (Enchantment.getByName(name) != null) {
                enchantment = Enchantment.getByName(name);
            }
        }

        if (enchantment != null) {
            this.getEnchantmentCache().put(name, enchantment);
        }

        return enchantment;
    }

}
