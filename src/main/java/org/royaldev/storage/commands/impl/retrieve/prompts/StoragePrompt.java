package org.royaldev.storage.commands.impl.retrieve.prompts;

import org.bukkit.conversations.ConversationContext;
import org.bukkit.inventory.ItemStack;
import org.royaldev.storage.storage.Storage;

import java.util.UUID;

public interface StoragePrompt {

    @SuppressWarnings("unchecked")
    default Storage<UUID, ItemStack> getStorage(final ConversationContext context) {
        return (Storage<UUID, ItemStack>) context.getSessionData("storage");
    }

}
