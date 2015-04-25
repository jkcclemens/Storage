package org.royaldev.storage.commands.impl.retrieve.prompts;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.Prompt;
import org.bukkit.conversations.StringPrompt;
import org.bukkit.inventory.ItemStack;
import org.royaldev.storage.storage.Storage;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class MaterialPrompt extends StringPrompt implements StoragePrompt {

    @Override
    public String getPromptText(final ConversationContext context) {
        return ChatColor.BLUE + "What material is the item you would like to retrieve?";
    }

    @Override
    public Prompt acceptInput(final ConversationContext context, final String input) {
        Material m;
        try {
            m = Material.valueOf(input.toUpperCase().replace(' ', '_').trim());
        } catch (final IllegalArgumentException ex) {
            return this;
        }
        context.setSessionData("material", m);
        final Storage<UUID, ItemStack> storage = this.getStorage(context);
        final List<ItemStack> possibleStacks = storage.getAll().stream()
            .filter(i -> i.getType() == m)
            .collect(Collectors.toList());
        context.setSessionData("possibleStacks", possibleStacks);
        final int amount = possibleStacks.size();
        if (amount <= 0) {
            context.getForWhom().sendRawMessage(ChatColor.RED + "You have no items of that type stored!");
            return null;
        } else if (amount == 1) {
            context.setSessionData("itemStack", possibleStacks.get(0));
            return new WithdrawalPrompt();
        } else {
            return new FilterPrompt();
        }
    }
}
