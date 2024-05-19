package id.luckynetwork.dev.lyrams.lej.commands.essentials;

import id.luckynetwork.dev.lyrams.lej.commands.api.CommandClass;
import id.luckynetwork.dev.lyrams.lej.utils.Utils;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

enum ListScope {
    ALL,
    NEARBY,
    WORLD
}

enum ShowType {
    ALL,
    DISPLAY,
    REAL
}

public class ListCommand extends CommandClass {

    public ListCommand() {
        super("list", Arrays.asList("who", "online"));
        this.registerCommandInfo("list", "Shows the list of online players");
    }

    private String formatPlayerName(Player player, ShowType showType) {
        switch (showType) {
            case ALL:
                return "§8└─ §a" + player.getDisplayName() + " §a(" + player.getName() + ")";
            case DISPLAY:
                return "§8└─ §a" + player.getDisplayName();
            case REAL:
                return "§8└─ §a" + player.getName();
        }

        return null;

    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        boolean showVanished = Utils.checkPermission(sender, "list.vanished", true);

        ListScope listScope = ListScope.ALL;
        if (args.length > 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cYou can only use this option in-game!");
                return;
            }

            switch (args[0].toLowerCase()) {
                case "all":
                    break;
                case "n":
                case "near":
                case "nearby":
                    listScope = ListScope.NEARBY;
                    break;
                case "w":
                case "world":
                    listScope = ListScope.WORLD;
                    break;
                default:
                    sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cUnknown list scope §l" + args[0] + "§c!");
                    sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eAvailable scopes: §dall, nearby[r={RANGE}], world");
                    return;
            }
        }

        ShowType showType = ShowType.ALL;
        switch (args[args.length - 1].toLowerCase()) {
            case "-a":
                break;
            case "-d":
                showType = ShowType.DISPLAY;
                break;
            case "-r":
                showType = ShowType.REAL;
                break;
            default:
                sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cUnknown show type §l" + args[args.length - 1] + "§c!");
                sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§eAvailable show types: §d-a, -d, -r");
                return;
        }

        ShowType finalShowType = showType;
        switch (listScope) {
            case ALL: {
                sender.sendMessage("§8├─ §eOnline players: §d" + plugin.getServer().getOnlinePlayers().size());
                plugin.getServer().getOnlinePlayers().forEach(player -> {
                    if (showVanished || !player.hasMetadata("vanished")) {
                        sender.sendMessage(this.formatPlayerName(player, finalShowType));
                    }
                });
                break;
            }
            case NEARBY: {
                Player senderPlayer = (Player) sender;

                int range = 50;
                if (args.length > 1) {
                    String rangeArg = args[0].split("r=")[1];
                    String rangeArgClean = rangeArg.substring(0, rangeArg.length() - 1);
                    try {
                        range = Integer.parseInt(rangeArgClean);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cInvalid range §l" + rangeArgClean + "§c!");
                        return;
                    }
                }

                int finalRange = range;
                List<? extends Player> filteredPlayers = plugin.getServer().getOnlinePlayers()
                        .stream()
                        .filter(player -> player.getLocation().distanceSquared(senderPlayer.getLocation()) <= finalRange * finalRange && (showVanished || !player.hasMetadata("vanished")))
                        .collect(Collectors.toList());

                sender.sendMessage("§8├─ §eNearby players: §d" + filteredPlayers.size());
                filteredPlayers.forEach(player -> sender.sendMessage(this.formatPlayerName(player, finalShowType)));
                break;
            }
            case WORLD: {
                Player senderPlayer = (Player) sender;

                List<? extends Player> filteredPlayers = plugin.getServer().getOnlinePlayers()
                        .stream()
                        .filter(player -> player.getWorld().equals(senderPlayer.getWorld()) && (showVanished || !player.hasMetadata("vanished")))
                        .collect(Collectors.toList());

                sender.sendMessage("§8├─ §ePlayers in world §d" + senderPlayer.getWorld().getName() + "§e: §d" + filteredPlayers.size());
                filteredPlayers.forEach(player -> sender.sendMessage(this.formatPlayerName(player, finalShowType)));
                break;
            }
        }
    }

    @Override
    public void sendDefaultMessage(CommandSender sender) {
        sender.sendMessage("§eList command:");
        sender.sendMessage("§8└─ §e/list §8- §7Shows the list of online players");
        sender.sendMessage("§8└─ §e/list all §8- §7Shows the list of all online players");
        sender.sendMessage("§8└─ §e/list nearby[r={RANGE}] §8- §7Shows the list of nearby players");
        sender.sendMessage("§8└─ §e/list world §8- §7Shows the list of players in the same world");
        sender.sendMessage("§8└─ §e/list [all|nearby|world] -a §8- §7Shows the list of online players with their display name and username");
        sender.sendMessage("§8└─ §e/list [all|nearby|world] -d §8- §7Shows the list of online players with their display name");
        sender.sendMessage("§8└─ §e/list [all|nearby|world] -r §8- §7Shows the list of online players with their username");
    }

    @Override
    public List<String> getTabSuggestions(CommandSender sender, String alias, String[] args) {
        if (!Utils.checkPermission(sender, "list", false)) {
            return null;
        }

        if (args.length == 1) {
            return Stream.of("all", "nearby", "world", "-a", "-d", "-r")
                    .filter(s -> s.toLowerCase().startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2) {
            return Stream.of("-a", "-d", "-r")
                    .filter(s -> s.toLowerCase().startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return null;
    }
}
