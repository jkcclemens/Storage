package org.royaldev.storage.commands.impl.store;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.royaldev.storage.StoragePlugin;
import org.royaldev.storage.commands.BaseCommand;
import org.royaldev.storage.commands.ReflectCommand;
import org.royaldev.storage.storage.impl.FilePlayerItemStackStorage;

@ReflectCommand(
    name = "store",
    description = "Stores items in an infinite storage space.",
    aliases = {"storage"}
)
public class StoreCommand extends BaseCommand<StoragePlugin> {

    public StoreCommand(final StoragePlugin instance, final String name) {
        super(instance, name, true);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(ChatColor.RED + "This command is only available to players.");
            return true;
        }
        final Player p = (Player) cs;
        final Inventory i = this.plugin.getServer().createInventory(new StoreCommandHolder(
            this.plugin.getStorages().getStorageOrAdd(p.getUniqueId(), FilePlayerItemStackStorage::new)
        ), 45, "Add to storage");
        p.openInventory(i);
        return true;
    }
}
