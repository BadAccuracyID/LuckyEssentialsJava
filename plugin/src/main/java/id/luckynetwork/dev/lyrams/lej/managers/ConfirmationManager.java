package id.luckynetwork.dev.lyrams.lej.managers;

import id.luckynetwork.dev.lyrams.lej.LuckyEssentials;
import id.luckynetwork.dev.lyrams.lej.callbacks.CanSkipCallback;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class ConfirmationManager {

    private final LuckyEssentials plugin;
    private final Map<Player, ConfirmationData> confirmationMap = new HashMap<>();

    /**
     * Requests a confirmation before executing something
     *
     * @param callable the code to execute if confirmed
     * @param sender   the executor
     * @param skip     the required condition to skip the confirmation
     * @param warnings warnings to be sent to the player
     */
    public void requestConfirmation(Callable callable, CommandSender sender, boolean skip, @Nullable List<String> warnings) {
        if (skip || sender instanceof ConsoleCommandSender) {
            callable.call();
            return;
        }

        Player player = (Player) sender;
        if (warnings != null && !warnings.isEmpty()) {
            warnings.forEach(player::sendMessage);
        }

        player.sendMessage(plugin.getMainConfigManager().getPrefix() + "§ePlease type §a/confirm §eto confirm your action or &c/cancel &eto cancel your action.");
        this.confirmationMap.put(player, new ConfirmationData(callable));
    }

    /**
     * @see ConfirmationManager#requestConfirmation(Callable, CommandSender, boolean, List)
     */
    public void requestConfirmation(Callable callable, CommandSender sender, boolean skip) {
        this.requestConfirmation(callable, sender, skip, null);
    }

    /**
     * @see ConfirmationManager#requestConfirmation(Callable, CommandSender, boolean, List)
     */
    public void requestConfirmation(Callable callable, CommandSender sender, @Nullable List<String> warnings) {
        this.requestConfirmation(callable, sender, false, warnings);
    }

    /**
     * @see ConfirmationManager#requestConfirmation(Callable, CommandSender, boolean, List)
     */
    public void requestConfirmation(Callable callable, CommandSender sender) {
        this.requestConfirmation(callable, sender, false, null);
    }

    /**
     * @see ConfirmationManager#requestConfirmation(Callable, CommandSender, boolean, List)
     */
    public void requestConfirmation(Callable callable, CanSkipCallback skipCallback) {
        this.requestConfirmation(callable, skipCallback.getSender(), skipCallback.isCanSkip(), skipCallback.getReason());
    }

    /**
     * Confirms the execution of a pending code
     *
     * @param player the player
     */
    public void confirm(Player player) {
        if (!this.confirmationMap.containsKey(player)) {
            player.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cYou don't have any pending action!");
            return;
        }

        ConfirmationData confirmationData = this.confirmationMap.get(player);
        if (confirmationData.getTime() + 5000L < System.currentTimeMillis()) {
            player.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cYou have waited too long to confirm your action!");
            this.confirmationMap.remove(player);
            return;
        }

        player.sendMessage(plugin.getMainConfigManager().getPrefix() + "§aAction confirmed.");
        confirmationData.getCallable().call();
        this.confirmationMap.remove(player);
    }


    /**
     * Deletes the queued pending code from {@link ConfirmationManager#confirmationMap}
     *
     * @param player the player who has the code pending
     */
    public void deleteConfirmation(Player player) {
        if (!this.confirmationMap.containsKey(player)) {
            if (player != null && player.isOnline()) {
                player.sendMessage(plugin.getMainConfigManager().getPrefix() + "§cYou don't have any pending action!");
            }
            return;
        }

        this.confirmationMap.remove(player);
    }


    public interface Callable {
        void call();
    }

    @Getter
    @RequiredArgsConstructor
    private static class ConfirmationData {
        private final long time = System.currentTimeMillis();
        private final Callable callable;
    }
}
