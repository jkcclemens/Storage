package org.royaldev.storage.commands.impl.retrieve;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.conversations.Conversation;
import org.bukkit.conversations.ConversationFactory;
import org.bukkit.entity.Player;
import org.royaldev.storage.StoragePlugin;
import org.royaldev.storage.commands.BaseCommand;
import org.royaldev.storage.commands.ReflectCommand;
import org.royaldev.storage.commands.impl.retrieve.prompts.MaterialPrompt;
import org.royaldev.storage.storage.impl.FilePlayerItemStackStorage;

@ReflectCommand(
    name = "retrieve",
    description = "Retrieves items from an infinite storage area."
)
public class RetrieveCommand extends BaseCommand<StoragePlugin> {

    public RetrieveCommand(final StoragePlugin instance, final String name) {
        super(instance, name, true);
    }

    @Override
    public boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
        if (!(cs instanceof Player)) {
            cs.sendMessage(ChatColor.RED + "This command may only be used by players.");
            return true;
        }
        final Player p = (Player) cs;
        final Conversation c = new ConversationFactory(this.plugin)
            .withLocalEcho(false)
            .withModality(false)
            .withEscapeSequence("stop")
            .withFirstPrompt(new MaterialPrompt())
            .buildConversation(p);
        c.getContext().setSessionData("storage", this.plugin.getStorages().getStorageOrAdd(
            p.getUniqueId(),
            FilePlayerItemStackStorage::new
        ));
        p.beginConversation(c);
        return true;
    }
}
