package org.royaldev.storage.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;

public abstract class CACommand<T extends Plugin> extends BaseCommand<T> {

    public CACommand(final T instance, final String name, final boolean checkPermissions) {
        super(instance, name, checkPermissions);
    }

    protected abstract boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] eargs, final CommandArguments ca);

    @Override
    protected boolean runCommand(final CommandSender cs, final Command cmd, final String label, final String[] args) {
        final CommandArguments ca = this.getCommandArguments(args);
        return this.runCommand(cs, cmd, label, ca.getExtraParameters(), ca);
    }
}
