package org.royaldev.storage;

import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.royaldev.storage.commands.ReflectiveCommandRegistrar;
import org.royaldev.storage.listeners.AddToStorageInventoryListener;
import org.royaldev.storage.storage.Storages;
import org.royaldev.storage.tasks.SaveStorageTask;

import java.util.UUID;

public class StoragePlugin extends JavaPlugin {

    private final Storages<UUID, ItemStack> storages = new Storages<>(this);

    public Storages<UUID, ItemStack> getStorages() {
        return this.storages;
    }

    @Override
    public void onDisable() {
        this.getStorages().saveAll();
    }

    @Override
    public void onEnable() {
        final ReflectiveCommandRegistrar<StoragePlugin> rcr = new ReflectiveCommandRegistrar<>(this);
        rcr.registerCommands();
        final PluginManager pm = this.getServer().getPluginManager();
        pm.registerEvents(new AddToStorageInventoryListener(), this);
        this.getStorages().loadAll();
        final BukkitScheduler bs = this.getServer().getScheduler();
        bs.runTaskTimer(this, new SaveStorageTask(this), 6000L, 6000L);
    }
}
