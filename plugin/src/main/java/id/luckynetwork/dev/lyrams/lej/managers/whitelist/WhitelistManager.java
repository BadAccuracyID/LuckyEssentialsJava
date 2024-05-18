package id.luckynetwork.dev.lyrams.lej.managers.whitelist;

import id.luckynetwork.dev.lyrams.lej.LuckyEssentials;
import id.luckynetwork.dev.lyrams.lej.config.ConfigFile;
import id.luckynetwork.dev.lyrams.lej.enums.settings.WhitelistCheckMode;
import id.luckynetwork.dev.lyrams.lej.utils.Utils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class WhitelistManager {

    private final LuckyEssentials plugin;

    private boolean enabled = false;
    private String denyMessage = "§cYou are not whitelisted!";
    private WhitelistCheckMode checkMode = WhitelistCheckMode.NAME;
    private List<WhitelistData> whitelistDataList = new ArrayList<>();

    public WhitelistManager(LuckyEssentials plugin) {
        this.plugin = plugin;
        this.reload();
    }

    public void reload() {
        plugin.setWhitelistConfig(new ConfigFile(plugin, "whitelist.yml"));

        this.enabled = plugin.getWhitelistConfig().getBoolean("toggled", false);
        this.denyMessage = Utils.colorize(plugin.getWhitelistConfig().getString("deny-message", "§cYou are not whitelisted!"));
        this.checkMode = WhitelistCheckMode.valueOf(plugin.getWhitelistConfig().getString("check-mode", "NAME").toUpperCase());

        this.whitelistDataList.clear();
        plugin.getWhitelistConfig().getConfigurationSection("whitelisted").getKeys(false).stream()
                .map(it -> plugin.getWhitelistConfig().getConfigurationSection("whitelisted." + it))
                .forEach(it -> {
                    if (it.getString("name") != null) {
                        try {
                            NameUUIDWhitelistData data = NameUUIDWhitelistData.newBuilder()
                                    .name(it.getString("name"))
                                    .uuid(it.getString("uuid"))
                                    .build();

                            whitelistDataList.add(data);
                        } catch (Exception ignored) {
                            plugin.getLogger().severe("There is something wrong in your whitelist.yml configuration!");
                        }
                    } else {
                        try {
                            PermissionWhitelistData data = PermissionWhitelistData.newBuilder()
                                    .permission(it.getString("permission"))
                                    .build();

                            whitelistDataList.add(data);
                        } catch (Exception ignored) {
                            plugin.getLogger().severe("There is something wrong in your whitelist.yml configuration!");
                        }
                    }
                });
    }

    public void save() {
        plugin.getWhitelistConfig().set("toggled", this.enabled);
        plugin.getWhitelistConfig().set("deny-message", this.denyMessage);
        plugin.getWhitelistConfig().set("check-mode", this.checkMode.toString());
        plugin.getWhitelistConfig().set("whitelisted", "");

        this.whitelistDataList.forEach(it -> {
            if (it instanceof NameUUIDWhitelistData) {
                NameUUIDWhitelistData data = (NameUUIDWhitelistData) it;
                StringBuilder name = new StringBuilder(data.getName());
                int i = 1;
                while (plugin.getWhitelistConfig().get("whitelisted." + name) != null) {
                    if (plugin.getWhitelistConfig().getString("whitelisted." + name + ".name").equals(data.getName()) && plugin.getWhitelistConfig().getString("whitelisted." + name + ".uuid").equals(data.getUuid())) {
                        break;
                    } else {
                        int number = i++;
                        name.append(number);
                    }
                }

                plugin.getWhitelistConfig().set("whitelisted." + name + ".name", data.getName());
                plugin.getWhitelistConfig().set("whitelisted." + name + ".uuid", data.getUuid());
            } else {
                PermissionWhitelistData data = (PermissionWhitelistData) it;
                StringBuilder name = new StringBuilder(data.getPermission().replace(".", "!"));
                int i = 1;
                while (plugin.getWhitelistConfig().get("whitelisted." + name) != null) {
                    if (plugin.getWhitelistConfig().getString("whitelisted." + name + ".permission").equals(data.getPermission())) {
                        break;
                    } else {
                        int number = i++;
                        name.append(number);
                    }
                }

                plugin.getWhitelistConfig().set("whitelisted." + name + ".permission", data.getPermission());
            }
        });

        plugin.getWhitelistConfig().save();
    }

    public boolean isWhitelisted(OfflinePlayer player) {
        return this.whitelistDataList.stream().anyMatch(it -> it.check(player, this.checkMode));
    }

    public boolean canJoin(OfflinePlayer player) {
        if (!this.enabled) {
            return true;
        }

        return this.whitelistDataList.stream().anyMatch(it -> it.check(player, this.checkMode));
    }
}
