package org.royaldev.storage.listeners;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.royaldev.storage.BaseTest;
import org.royaldev.storage.ItemStackBuilder;
import org.royaldev.storage.commands.impl.store.StoreCommandHolder;
import org.royaldev.storage.storage.Storage;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.anyVararg;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

@PrepareForTest({InventoryCloseEvent.class})
public class AddToStorageInventoryListenerTest extends BaseTest {

    private AddToStorageInventoryListener atsil;

    private Inventory createInventory(final InventoryHolder ih, final int size, final String name) {
        final List<ItemStack> items = Lists.newArrayList();
        final Inventory i = mock(Inventory.class);
        when(i.getHolder()).thenReturn(ih);
        when(i.getSize()).thenReturn(size);
        when(i.getName()).thenReturn(name);
        when(i.getContents()).then(invocation -> items.toArray(new ItemStack[size]));
        when(i.addItem(anyVararg())).then(invocation -> {
            Arrays.asList(invocation.getArguments()).stream()
                .filter(obj -> obj != null)
                .map(obj -> (ItemStack) obj)
                .forEach(items::add);
            return Maps.newHashMap();
        });
        return i;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.atsil = new AddToStorageInventoryListener();
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        this.atsil = null;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testCloseThatInventory() throws Exception {
        // Make a player
        final Player p = this.makePlayer();
        // Make a storage for that player
        final Storage<UUID, ItemStack> storage = this.getStorage(p.getUniqueId());
        // Make an inventory for that player
        final Inventory i = this.createInventory(new StoreCommandHolder(storage), 54, "Add to storage");
        // Add some items to it
        i.addItem(
            ItemStackBuilder.newBuilder()
                .type(Material.COAL)
                .amount(35)
                .build(),
            ItemStackBuilder.newBuilder()
                .type(Material.WOOD)
                .amount(17)
                .customName("Awesome wood")
                .lore("Really, though.", "It's great.")
                .damage((short) 1)
                .build()
        );
        // Ensure that they were added
        assertSame(2L, Arrays.stream(i.getContents()).filter(item -> item != null).count());
        // Make an inventory close event to give to the listener
        final InventoryCloseEvent ice = mock(InventoryCloseEvent.class);
        when(ice.getInventory()).thenReturn(i);
        when(ice.getPlayer()).thenReturn(p);
        // Pass the event to the listener
        this.atsil.closeThatInventory(ice);
        // One message should have been sent
        verify(p, times(1)).sendMessage(anyString());
        // Message should have said that 52 items were added
        assertThat(this.pastMessages, hasItems(containsString("52")));
        // Grab the items in storage
        final Collection<ItemStack> storedItems = storage.getAll();
        // Ensure that there are only two stacks
        assertSame(2, storedItems.size());
        // The total amounts should add up to 52
        assertSame(52, storedItems.stream().mapToInt(ItemStack::getAmount).sum());
        // Get the stored wood
        final ItemStack wood = storedItems.stream().filter(item -> item.getType() == Material.WOOD).findFirst().get();
        // It should have meta
        assertNotNull(wood.getItemMeta());
        // The name should be what we set
        assertEquals("Awesome wood", wood.getItemMeta().getDisplayName());
        // The lore should contain these two
        assertThat(wood.getItemMeta().getLore(), hasItems(
            "Really, though.",
            "It's great."
        ));
    }
}
