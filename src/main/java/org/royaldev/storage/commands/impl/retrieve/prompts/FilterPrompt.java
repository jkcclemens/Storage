package org.royaldev.storage.commands.impl.retrieve.prompts;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.royaldev.storage.shaded.mkremins.fanciful.FancyMessage;

import java.util.List;

public class FilterPrompt extends NumericPrompt implements StoragePrompt {

    @SuppressWarnings("unchecked")
    private List<ItemStack> getPossibleStacks(final ConversationContext context) {
        return (List<ItemStack>) context.getSessionData("possibleStacks");
    }

    private void sendItems(final ConversationContext context) {
        if (!(context.getForWhom() instanceof Player)) {
            throw new IllegalStateException("Conversable was not player.");
        }
        final Player p = (Player) context.getForWhom();
        final List<ItemStack> possibleStacks = this.getPossibleStacks(context);
        for (int i = 0; i < possibleStacks.size(); i++) {
            new FancyMessage()
                .text(String.valueOf(i + 1))
                .color(ChatColor.GRAY)
                .then(". ")
                .color(ChatColor.BLUE)
                .then("Hover here.")
                .color(ChatColor.GRAY)
                .itemTooltip(possibleStacks.get(i))
                .send(p);
        }
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        final int index = input.intValue() - 1;
        final List<ItemStack> possibleStacks = this.getPossibleStacks(context);
        if (index < 0 || index >= possibleStacks.size()) {
            return this;
        }
        context.setSessionData("itemStack", possibleStacks.get(index));
        return new WithdrawalPrompt();
    }

    @Override
    public String getPromptText(final ConversationContext context) {
        context.getForWhom().sendRawMessage(ChatColor.BLUE + "There is more than one item with that type in your storage. Which do you want?");
        context.getForWhom().sendRawMessage(ChatColor.GRAY + "Hover over each item to see its description.");
        this.sendItems(context);
        return ChatColor.GRAY + "Type the number of the item you want.";
    }
}
