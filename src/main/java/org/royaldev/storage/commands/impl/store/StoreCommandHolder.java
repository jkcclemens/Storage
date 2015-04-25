package org.royaldev.storage.commands.impl.store;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.royaldev.storage.storage.Storage;

import java.util.UUID;

public class StoreCommandHolder implements InventoryHolder {

    private final Storage<UUID, ItemStack> storage;

    public StoreCommandHolder(final Storage<UUID, ItemStack> storage) {
        this.storage = storage;
    }

    @Override
    public Inventory getInventory() {
        return null;
    }

    public Storage<UUID, ItemStack> getStorage() {
        return this.storage;
    }
}
