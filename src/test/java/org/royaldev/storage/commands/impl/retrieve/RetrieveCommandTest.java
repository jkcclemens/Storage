package org.royaldev.storage.commands.impl.retrieve;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.Conversation.ConversationState;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.royaldev.storage.ItemStackBuilder;
import org.royaldev.storage.commands.BaseCommandTest;
import org.royaldev.storage.storage.Storage;

import java.util.UUID;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.hasItems;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.when;

public class RetrieveCommandTest extends BaseCommandTest {

    private RetrieveCommand rc;
    private Conversation c;

    @Override
    protected Player makePlayer() {
        final Player p = super.makePlayer();
        // Set the Conversation field when we begin one
        when(p.beginConversation(any(Conversation.class))).then(invocation -> {
            this.c = (Conversation) invocation.getArguments()[0];
            this.c.begin();
            this.c.outputNextPrompt();
            return true;
        });
        return p;
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        this.rc = new RetrieveCommand(this.sp, "retrieve");
    }

    @After
    public void tearDown() throws Exception {
        super.tearDown();
        this.rc = null;
        this.c = null;
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testMultipleItems() throws Exception {
        // Create a storage for the player
        final Storage<UUID, ItemStack> storage = this.getStorage(
            this.uuid,
            new ItemStack(Material.COAL, 64),
            ItemStackBuilder.newBuilder()
                .type(Material.COAL)
                .amount(16)
                .customName("Cool Coal")
                .build(),
            new ItemStack(Material.WOOD, 5)
        );
        // Create player
        final Player p = this.makePlayer();
        // Mock a command that returns "retrieve" as its name
        final Command c = mock(Command.class);
        when(c.getName()).thenReturn("retrieve");
        // Run the command
        this.rc.runCommand(p, c, "retrieve", new String[0]);
        // Ensure we have a conversation
        assertNotNull(this.c);
        // Ensure the conversation is started
        assertSame(ConversationState.STARTED, this.c.getState());
        // Input "coal" to the first prompt
        this.c.acceptInput("coal");
        // Ensure that the player has been sent at least one message
        verify(p, atLeastOnce()).sendRawMessage(anyString());
        // Check for the filter prompt message
        assertThat(this.pastMessages, hasItems(
            containsString("Type the number of the item you want.")
        ));
        this.pastMessages.clear();
        // Choose the cool coal, since it is the second item added
        this.c.acceptInput("2");
        // Ensure that the player has been sent at least one message
        verify(p, atLeastOnce()).sendRawMessage(anyString());
        // A message stating that 16 coal is available should be sent
        assertThat(this.pastMessages, hasItems(
            containsString("16")
        ));
        // Clear out past messages
        this.pastMessages.clear();
        // Send that we want 12 coal
        this.c.acceptInput("12");
        // Should have had one call to addItem
        verify(p.getInventory(), times(1)).addItem(any(ItemStack.class));
        final ItemStack is = storage.getAll().stream()
            .filter(i -> i.hasItemMeta() && i.getItemMeta().getDisplayName().equals("Cool Coal"))
            .findFirst()
            .get();
        // We should now only have 4 cool coal
        assertSame(4, is.getAmount());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testOneStack() throws Exception {
        // Create a storage for the player
        final Storage<UUID, ItemStack> storage = this.getStorage(
            this.uuid,
            new ItemStack(Material.COAL, 84),
            new ItemStack(Material.WOOD, 5)
        );
        // Create player
        final Player p = this.makePlayer();
        // Mock a command that returns "retrieve" as its name
        final Command c = mock(Command.class);
        when(c.getName()).thenReturn("retrieve");
        // Run the command
        this.rc.runCommand(p, c, "retrieve", new String[0]);
        // Ensure we have a conversation
        assertNotNull(this.c);
        // Ensure the conversation is started
        assertSame(ConversationState.STARTED, this.c.getState());
        // Input "coal" to the first prompt
        this.c.acceptInput("coal");
        // Ensure that the player has been sent at least one message
        verify(p, atLeastOnce()).sendRawMessage(anyString());
        // A message stating that 84 coal is available should be sent
        assertThat(this.pastMessages, hasItems(
            containsString("84")
        ));
        // Clear out past messages
        this.pastMessages.clear();
        // Send that we want 12 coal
        this.c.acceptInput("12");
        // Should have had one call to addItem
        verify(p.getInventory(), times(1)).addItem(any(ItemStack.class));
        final ItemStack is = storage.getAll().stream()
            .filter(i -> i.getType() == Material.COAL)
            .findFirst()
            .get();
        // We should now only have 72 cool coal
        assertSame(72, is.getAmount());
    }
}
