package id.luckynetwork.dev.lyrams.lej.commands.essentials;

import id.luckynetwork.dev.lyrams.lej.commands.api.CommandClass;
import id.luckynetwork.dev.lyrams.lej.enums.ToggleType;
import id.luckynetwork.dev.lyrams.lej.enums.TrueFalseType;
import id.luckynetwork.dev.lyrams.lej.managers.whitelist.WhitelistData;
import id.luckynetwork.dev.lyrams.lej.utils.Utils;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WhitelistCommand extends CommandClass {

    public WhitelistCommand() {
        super("whitelist", Arrays.asList("ewl", "wl"));
    }

    public void infoCommand(CommandSender sender) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return;
        }

        sender.sendMessage("§eWhitelist system info:");
        sender.sendMessage("§8├─ §eState: " + Utils.colorizeTrueFalse(plugin.getWhitelistManager().isEnabled(), TrueFalseType.ON_OFF));

        ComponentBuilder textBuilder = new ComponentBuilder("").append("§8├─ §eWhitelisted Players: §a" + plugin.getWhitelistManager().getWhitelistedList().size()).event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click to run /whitelist list").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/whitelist list"));
        BaseComponent[] text = textBuilder.create();
        if (sender instanceof Player) {
            ((Player) sender).spigot().sendMessage(text);
        } else {
            sender.sendMessage(BaseComponent.toLegacyText(text));
        }

        sender.sendMessage("§8└─ §eCheck Mode: §a" + plugin.getWhitelistManager().getCheckMode().toString());
    }

    public void listCommand(CommandSender sender, Integer page) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return;
        }

        List<WhitelistData> whitelistedPlayers = plugin.getWhitelistManager().getWhitelistedList();
        if (whitelistedPlayers.size() > 5) {
            int maxPage = (int) Math.ceil(whitelistedPlayers.size() / 5.0);
            page = Math.min(Math.max(page, 1), maxPage);

            int from = page > 1 ? 5 * page - 5 : 0;
            int to = page > 0 ? 5 * page : 5;
            if (to > whitelistedPlayers.size()) {
                to -= (to - whitelistedPlayers.size());
            }

            sender.sendMessage("§6§m------------§a Whitelisted Players §e(§7" + page + "§e/§7" + maxPage + "§e) §6§m------------");

            List<WhitelistData> pagedWhitelistedPlayers = whitelistedPlayers.subList(from, to);
            int i = from;
            for (WhitelistData data : pagedWhitelistedPlayers) {
                sender.sendMessage("§7Player §a#" + ++i);
                sender.sendMessage("§8├─ §eUUID: §a" + data.getUuid());
                sender.sendMessage("§8└─ §eName: §a" + data.getName());
            }

            boolean lastPage = (page == maxPage);
            ComponentBuilder textBuilder = new ComponentBuilder("§6§m-----------------------§8 ");
            if (lastPage) {
                textBuilder.append("§8[§e←§8]").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click for previous page").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/whitelist list " + (page - 1)));
            } else {
                textBuilder.append("§8[§e→§8]").event(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("§7Click for next page").create())).event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/whitelist list " + (page + 1)));
            }
            textBuilder.append(" §6§m-----------------------");

            BaseComponent[] text = textBuilder.create();
            if (sender instanceof Player) {
                ((Player) sender).spigot().sendMessage(text);
            } else {
                sender.sendMessage(BaseComponent.toLegacyText(text));
            }
        } else {
            sender.sendMessage("§6§m------------§a Whitelisted Players §6§m------------");
            int i = 0;
            for (WhitelistData data : plugin.getWhitelistManager().getWhitelistedList()) {
                sender.sendMessage("§7Player §a#" + ++i);
                sender.sendMessage("§8├─ §eUUID: §a" + data.getUuid());
                sender.sendMessage("§8└─ §eName: §a" + data.getName());
            }
            sender.sendMessage("§6§m---------------------------------------------");
        }
    }

    public void addCommand(CommandSender sender, String targetName) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return;
        }

        OfflineTargetsCallback targets = this.getTargetsOffline(sender, targetName);
        if (targets.isEmpty()) {
            sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cNo targets found!");
            return;
        }

        targets.forEach(target -> {
            WhitelistData data = WhitelistData.newBuilder().uuid(target.getUniqueId().toString()).name(target.getName()).build();

            if (plugin.getWhitelistManager().getWhitelistedList().contains(data)) {
                sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§c§l" + data.getName() + " §cis already whitelisted.");
            } else {
                plugin.getWhitelistManager().getWhitelistedList().add(data);
                sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eAdded §d" + data.getName() + " §eto the whitelist.");
            }
        });

        plugin.getWhitelistManager().save();
    }

    public void removeCommand(CommandSender sender, String targetName) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return;
        }

        OfflineTargetsCallback targets = this.getTargetsOffline(sender, targetName);
        if (targets.isEmpty()) {
            sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cNo targets found!");
            return;
        }

        targets.forEach(target -> {
            WhitelistData data = WhitelistData.newBuilder().uuid(target.getUniqueId().toString()).name(target.getName()).build();

            if (plugin.getWhitelistManager().getWhitelistedList().contains(data)) {
                plugin.getWhitelistManager().getWhitelistedList().remove(data);
                sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eRemoved §d" + data.getName() + " §efrom the whitelist.");
            } else {
                boolean removed = plugin.getWhitelistManager().getWhitelistedList().removeIf(it -> it.getName().equals(target.getName()) || it.getUuid().equals(target.getUniqueId().toString()));
                if (removed) {
                    sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eRemoved §d" + data.getName() + " §efrom the whitelist.");
                } else {
                    sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§c§l" + data.getName() + " §cis already not whitelisted.");
                }
            }
        });

        plugin.getWhitelistManager().save();
    }

    public void checkCommand(CommandSender sender, String targetName) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return;
        }

        OfflineTargetsCallback targets = this.getTargetsOffline(sender, targetName);
        if (targets.isEmpty()) {
            sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cNo targets found!");
            return;
        }

        targets.forEach(target -> {
            boolean whitelsited = false;
            switch (plugin.getWhitelistManager().getCheckMode()) {
                case UUID: {
                    whitelsited = plugin.getWhitelistManager().getWhitelistedList().stream().map(WhitelistData::getUuid).anyMatch(it -> it.equals(target.getUniqueId().toString()));
                    break;
                }
                case NAME: {
                    whitelsited = plugin.getWhitelistManager().getWhitelistedList().stream().map(WhitelistData::getName).anyMatch(it -> it.equals(target.getName()));
                    break;
                }
                case BOTH: {
                    whitelsited = plugin.getWhitelistManager().getWhitelistedList().stream().anyMatch(it -> it.getUuid().equals(target.getUniqueId().toString()) && it.getName().equals(target.getName()));
                    break;
                }
            }

            if (whitelsited) {
                sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§d" + target.getName() + " §eis §awhitelisted.");
            } else {
                sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§d" + target.getName() + " §eis §cnot whitelisted.");
            }
        });
    }

    public void toggleCommand(CommandSender sender, String toggle) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return;
        }

        ToggleType toggleType = ToggleType.getToggle(toggle);
        if (toggleType.equals(ToggleType.UNKNOWN)) {
            sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cUnknown toggle type §l" + toggle + "§c!");
            return;
        }

        switch (toggleType) {
            case ON: {
                plugin.getWhitelistManager().setEnabled(true);
                break;
            }
            case OFF: {
                plugin.getWhitelistManager().setEnabled(false);
                break;
            }
            case TOGGLE: {
                plugin.getWhitelistManager().setEnabled(!plugin.getWhitelistManager().isEnabled());
                break;
            }
        }

        plugin.getWhitelistManager().save();
        sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eToggled whitelist system " + Utils.colorizeTrueFalse(plugin.getWhitelistManager().isEnabled(), TrueFalseType.ON_OFF) + "§e.");
    }

    public void onCommand(CommandSender sender) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return;
        }

        plugin.getWhitelistManager().setEnabled(true);
        plugin.getWhitelistManager().save();
        sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eToggled whitelist system " + Utils.colorizeTrueFalse(plugin.getWhitelistManager().isEnabled(), TrueFalseType.ON_OFF) + "§e.");
    }

    public void offCommand(CommandSender sender) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return;
        }

        plugin.getWhitelistManager().setEnabled(false);
        plugin.getWhitelistManager().save();
        sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eToggled whitelist system " + Utils.colorizeTrueFalse(plugin.getWhitelistManager().isEnabled(), TrueFalseType.ON_OFF) + "§e.");
    }

    public void reloadCommand(CommandSender sender) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return;
        }

        plugin.getWhitelistManager().reload();
        sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eReloaded the whitelist system!");
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return;
        }

        if (args.length == 0) {
            this.sendDefaultMessage(sender);
            return;
        }

        String arg = args[0];
        switch (arg.toLowerCase()) {
            case "info": {
                this.infoCommand(sender);
                break;
            }

            case "list": {
                int page = 1;
                if (args.length == 2) {
                    try {
                        page = Integer.parseInt(args[1]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cInvalid page number!");
                        return;
                    }
                }

                this.listCommand(sender, page);
                break;
            }

            case "add": {
                if (args.length == 1) {
                    sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cPlease specify a player!");
                    return;
                }

                this.addCommand(sender, args[1]);
                break;
            }

            case "remove": {
                if (args.length == 1) {
                    sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cPlease specify a player!");
                    return;
                }

                this.removeCommand(sender, args[1]);
                break;
            }

            case "check": {
                if (args.length == 1) {
                    sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cPlease specify a player!");
                    return;
                }

                this.checkCommand(sender, args[1]);
                break;
            }

            case "toggle": {
                if (args.length == 1) {
                    this.toggleCommand(sender, "toggle");
                    return;
                }

                this.toggleCommand(sender, args[1]);
                break;
            }

            case "on": {
                this.onCommand(sender);
                break;
            }

            case "off": {
                this.offCommand(sender);
                break;
            }

            case "reload": {
                this.reloadCommand(sender);
                break;
            }

            default: {
                this.sendDefaultMessage(sender);
                break;
            }
        }

    }

    @Override
    public void sendDefaultMessage(CommandSender sender) {
        sender.sendMessage("§eWhitelist command:");
        sender.sendMessage("§8└─ §e/whitelist §8- §7Shows this help message");
        sender.sendMessage("§8└─ §e/whitelist info §8- §7Shows information about the whitelist system");
        sender.sendMessage("§8└─ §e/whitelist list <page> §8- §7Shows a list of whitelisted players on a specific page");
        sender.sendMessage("§8└─ §e/whitelist add <player> §8- §7Adds a player to the whitelist");
        sender.sendMessage("§8└─ §e/whitelist remove <player> §8- §7Removes a player from the whitelist");
        sender.sendMessage("§8└─ §e/whitelist check <player> §8- §7Checks if a player is whitelisted");
        sender.sendMessage("§8└─ §e/whitelist toggle <on/off> §8- §7Toggles the whitelist system");
        sender.sendMessage("§8└─ §e/whitelist on §8- §7Enables the whitelist system");
        sender.sendMessage("§8└─ §e/whitelist off §8- §7Disables the whitelist system");
        sender.sendMessage("§8└─ §e/whitelist reload §8- §7Reloads the whitelist system");
    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender, String alias, String[] args) {
        if (!Utils.checkPermission(sender, "whitelist")) {
            return null;
        }

        if (args.length == 1) {
            return Stream.of("info", "list", "add", "remove", "check", "toggle", "on", "off", "reload")
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
