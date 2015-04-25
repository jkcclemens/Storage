package org.royaldev.storage.commands.impl.retrieve.prompts;

import org.bukkit.ChatColor;
import org.bukkit.conversations.ConversationContext;
import org.bukkit.conversations.NumericPrompt;
import org.bukkit.conversations.Prompt;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class WithdrawalPrompt extends NumericPrompt implements StoragePrompt {

    private ItemStack getItemStack(final ConversationContext context) {
        return (ItemStack) context.getSessionData("itemStack");
    }

    @Override
    protected Prompt acceptValidatedInput(final ConversationContext context, final Number input) {
        if (!(context.getForWhom() instanceof Player)) {
            throw new IllegalStateException("Conversable was not a player.");
        }
        final Player p = (Player) context.getForWhom();
        final int amount = input.intValue();
        final ItemStack itemStack = this.getItemStack(context);
        if (amount < 1 || amount > itemStack.getAmount()) {
            return this;
        }
        final ItemStack toGive = itemStack.clone();
        toGive.setAmount(amount);
        this.getStorage(context).remove(toGive);
        p.getInventory().addItem(toGive);
        context.getForWhom().sendRawMessage(ChatColor.BLUE + "Item added to your inventory.");
        return null;
    }

    @Override
    public String getPromptText(final ConversationContext context) {
        final ItemStack stack = this.getItemStack(context);
        return ChatColor.BLUE + "How many of this item would you like to retrieve? (" + stack.getAmount() + " available)";
    }
}
