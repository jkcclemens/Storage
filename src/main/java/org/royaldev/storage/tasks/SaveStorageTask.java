package org.royaldev.storage.tasks;

import org.royaldev.storage.StoragePlugin;

public class SaveStorageTask implements Runnable {

    private final StoragePlugin plugin;

    public SaveStorageTask(final StoragePlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        this.plugin.getStorages().saveAll();
    }
}
