package org.royaldev.storage;

import com.google.common.collect.Lists;
import org.bukkit.Bukkit;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftItemFactory;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.PluginDescriptionFile;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import org.royaldev.storage.storage.Storage;
import org.royaldev.storage.storage.Storages;
import org.royaldev.storage.storage.impl.FilePlayerItemStackStorage;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.powermock.api.mockito.PowerMockito.doAnswer;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.mockStatic;
import static org.powermock.api.mockito.PowerMockito.when;

@RunWith(PowerMockRunner.class)
@PrepareForTest({PluginDescriptionFile.class, StoragePlugin.class, Bukkit.class})
@PowerMockIgnore("javax.management.*")
public abstract class BaseTest {

    protected final UUID uuid = UUID.randomUUID();
    protected StoragePlugin sp;
    protected Storages<UUID, ItemStack> storages;
    protected List<String> pastMessages;

    protected Storage<UUID, ItemStack> getStorage(final UUID uuid, final ItemStack... items) {
        // Create a storage for the player
        final Storage<UUID, ItemStack> storage = this.storages.getStorageOrAdd(uuid, FilePlayerItemStackStorage::new);
        // Add some items to it
        storage.addAll(Arrays.asList(items));
        return storage;
    }

    protected Player makePlayer() {
        // Create player
        final Player p = mock(Player.class);
        // When sent a message, add it to the past messages
        doAnswer(invocation -> this.pastMessages.add((String) invocation.getArguments()[0])).when(p).sendMessage(anyString());
        doAnswer(invocation -> this.pastMessages.add((String) invocation.getArguments()[0])).when(p).sendRawMessage(anyString());
        // Make an inventory
        final PlayerInventory pi = mock(PlayerInventory.class);
        when(pi.addItem(anyVararg())).thenReturn(null);
        when(p.getInventory()).thenReturn(pi);
        // Do nothing for inventory additions
        // Give the player a UUID
        when(p.getUniqueId()).thenReturn(this.uuid);
        return p;
    }

    @Before
    public void setUp() throws Exception {
        this.pastMessages = Lists.newArrayList();
        this.sp = mock(StoragePlugin.class);
        final PluginDescriptionFile pdf = mock(PluginDescriptionFile.class);
        when(pdf.getName()).thenReturn("Storage");
        when(this.sp.getDescription()).thenReturn(pdf);
        this.storages = new Storages<>(this.sp);
        when(this.sp.getStorages()).thenReturn(this.storages);
        mockStatic(Bukkit.class);
        when(Bukkit.getItemFactory()).thenReturn(CraftItemFactory.instance());
    }

    @After
    public void tearDown() throws Exception {
        this.sp = null;
        this.storages = null;
        this.pastMessages = null;
    }

}
