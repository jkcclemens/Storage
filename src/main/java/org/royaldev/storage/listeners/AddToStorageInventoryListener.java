package org.royaldev.storage.listeners;

import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.royaldev.storage.commands.impl.store.StoreCommandHolder;
import org.royaldev.storage.storage.Storage;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class AddToStorageInventoryListener implements Listener {

    @EventHandler
    public void closeThatInventory(final InventoryCloseEvent event) {
        final Inventory i = event.getInventory();
        final InventoryHolder ih = i.getHolder();
        if (!(ih instanceof StoreCommandHolder)) {
            return;
        }
        final StoreCommandHolder sch = (StoreCommandHolder) ih;
        final Storage<UUID, ItemStack> storage = sch.getStorage();
        final List<ItemStack> toAdd = Arrays.stream(i.getContents())
            .filter(item -> item != null)
            .collect(Collectors.toList());
        storage.addAll(toAdd);
        final int added = toAdd.stream()
            .mapToInt(ItemStack::getAmount)
            .sum();
        event.getPlayer().sendMessage(ChatColor.BLUE + "Added " + added + " item" + (added == 1 ? "" : "s") + " to storage.");
    }

}
