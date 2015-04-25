package org.royaldev.storage.commands.impl.store;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.craftbukkit.v1_7_R4.inventory.CraftInventoryCustom;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.royaldev.storage.commands.BaseCommandTest;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class StoreCommandTest extends BaseCommandTest {

    private StoreCommand sc;

    @Override
    @Before
    public void setUp() throws Exception {
        super.setUp();
        final Server s = mock(Server.class);
        when(s.createInventory(any(InventoryHolder.class), anyInt(), anyString())).then(invocation -> new CraftInventoryCustom((InventoryHolder) invocation.getArguments()[0], (int) invocation.getArguments()[1], (String) invocation.getArguments()[2]));
        when(this.sp.getServer()).thenReturn(s);
        this.sc = new StoreCommand(this.sp, "store");
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        this.sc = null;
    }

    @Test
    public void testRunCommand() throws Exception {
        // Get a player
        final Player p = this.makePlayer();
        // Check that any inventory opened is a StoreCommandHolder and save it
        when(p.openInventory(any(Inventory.class))).thenAnswer(invocation -> {
            final Inventory i = ((Inventory) invocation.getArguments()[0]);
            assertThat(i.getHolder(), instanceOf(StoreCommandHolder.class));
            return null;
        });
        final Command c = mock(Command.class);
        when(c.getName()).thenReturn("store");
        // Run the command
        this.sc.runCommand(p, c, "store", new String[0]);
        // Ensure an inventory was opened
        verify(p, times(1)).openInventory(any(Inventory.class));
    }
}
