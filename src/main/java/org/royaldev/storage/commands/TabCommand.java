package org.royaldev.storage.commands;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.command.SimpleCommandMap;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class TabCommand<T extends Plugin> extends CACommand<T> implements TabCompleter {

    private final List<Short> completionTypes = new ArrayList<>();
    private CompletionType alwaysUse = null;

    protected TabCommand(final T instance, final String name, final boolean checkPermissions, final Short[] cts) {
        super(instance, name, checkPermissions);
        for (Short s : cts) {
            if (s == null) s = 0;
            this.completionTypes.add(s);
        }
    }

    /**
     * An array of all Enum values used by {@link org.royaldev.storage.commands.TabCommand.CompletionType#ENUM}.
     *
     * @param cs    CommandSender completing
     * @param cmd   Command being completed
     * @param label Label of command being completed
     * @param args  All arguments
     * @param arg   Argument to complete
     * @return Array of Enums to be checked
     */
    @SuppressWarnings("UnusedParameters")
    protected Enum[] customEnum(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new Enum[0];
    }

    /**
     * A list of values used by {@link org.royaldev.storage.commands.TabCommand.CompletionType#LIST}.
     *
     * @param cs    CommandSender completing
     * @param cmd   Command being completed
     * @param label Label of command being completed
     * @param args  All arguments
     * @param arg   Argument to complete
     * @return List of values to be checked
     */
    @SuppressWarnings("UnusedParameters")
    protected List<String> customList(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>();
    }

    /**
     * Filters all completions just before they are returned. This should be overridden by any class extending
     * TabCommand if necessary.
     *
     * @param completions Completions before filtering
     * @param cs          CommandSender completing
     * @param cmd         Command being completed
     * @param label       Label of command being completed
     * @param args        All arguments
     * @param arg         Argument to complete
     * @return Filtered completions to return
     */
    @SuppressWarnings("UnusedParameters")
    protected List<String> filterCompletions(final List<String> completions, final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return completions;
    }

    protected List<String> getCompletionsFor(final CommandSender cs, final Command cmd, final String label, final String[] args, final CompletionType ct) {
        final List<String> possibilities = new ArrayList<>();
        if (args.length < 1) return possibilities;
        final String arg = args[args.length - 1].toLowerCase();
        switch (ct) {
            case ONLINE_PLAYER:
                for (final Player p : this.plugin.getServer().getOnlinePlayers()) {
                    final String name = p.getName();
                    final String lowerCaseName = name.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
                }
                break;
            case ITEM:
                for (final Material m : Material.values()) {
                    final String name = m.name();
                    final String lowerCaseName = name.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
                }
                break;
            case CUSTOM:
                possibilities.addAll(this.getCustomCompletions(cs, cmd, label, args, arg));
                break;
            case PLUGIN:
                possibilities.addAll(this.getPluginCompletions(arg));
                break;
            case WORLD:
                for (final World w : this.plugin.getServer().getWorlds()) {
                    final String name = w.getName();
                    final String lowerCaseName = name.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
                }
                break;
            case LIST:
                final List<String> custom = this.customList(cs, cmd, label, args, arg);
                if (custom == null) break;
                for (final String s : custom) {
                    final String lowerCaseName = s.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), s);
                }
                break;
            case ENUM:
                final Enum[] enums = this.customEnum(cs, cmd, label, args, arg);
                if (enums == null) break;
                for (final Enum e : enums) {
                    final String name = e.name();
                    final String lowerCaseName = name.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
                }
                break;
            case PLUGIN_COMMAND:
            case ANY_COMMAND:
                final SimpleCommandMap commandMap;
                try {
                    final Object result = ReflectiveCommandRegistrar.getPrivateField(this.plugin.getServer().getPluginManager(), "commandMap");
                    commandMap = (SimpleCommandMap) result;
                } catch (Exception e) {
                    break;
                }
                for (final Command c : commandMap.getCommands()) {
                    if (ct == CompletionType.PLUGIN_COMMAND && (!(c instanceof PluginCommand) || !this.plugin.getClass().getName().equals(((PluginCommand) c).getPlugin().getClass().getName()))) {
                        continue;
                    }
                    final String name = c.getName();
                    if (possibilities.contains(name)) continue;
                    final String lowerCaseName = name.toLowerCase();
                    if (!lowerCaseName.startsWith(arg)) continue;
                    possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
                    for (final String alias : c.getAliases()) {
                        if (possibilities.contains(alias)) continue;
                        final String lowerCaseAlias = alias.toLowerCase();
                        if (!lowerCaseAlias.startsWith(arg)) continue;
                        possibilities.add(lowerCaseAlias.equals(arg) ? 0 : possibilities.size(), alias);
                    }
                }
                break;
        }
        return this.filterCompletions(possibilities, cs, cmd, label, args, arg);
    }

    /**
     * Gets the custom completions for an argument. This should be overridden by any class extending TabCommand. This
     * will be called on any argument with a CompletionType of CUSTOM.
     *
     * @param cs    CommandSender completing
     * @param cmd   Command being completed
     * @param label Label of command being completed
     * @param args  All arguments
     * @param arg   Argument to complete
     * @return List of possible completions (not null)
     */
    @SuppressWarnings("UnusedParameters")
    protected List<String> getCustomCompletions(final CommandSender cs, final Command cmd, final String label, final String[] args, final String arg) {
        return new ArrayList<>();
    }

    protected List<String> getPluginCompletions(final String arg) {
        final List<String> possibilities = new ArrayList<>();
        for (final Plugin p : this.plugin.getServer().getPluginManager().getPlugins()) {
            final String name = p.getName();
            final String lowerCaseName = name.toLowerCase();
            if (!lowerCaseName.startsWith(arg)) continue;
            possibilities.add(lowerCaseName.equals(arg) ? 0 : possibilities.size(), name);
        }
        return possibilities;
    }

    @Override
    public List<String> onTabComplete(final CommandSender cs, final Command cmd, final String label, final String[] args) {
        try {
            final ArrayList<String> possibilities = new ArrayList<>();
            final String[] eargs = this.getCommandArguments(args).getExtraParameters();
            final int lastArgIndex = eargs.length - 1;
            final List<CompletionType> cts;
            if (lastArgIndex < 0 || lastArgIndex >= this.completionTypes.size()) {
                if (this.alwaysUse == null) return possibilities;
                else cts = Collections.singletonList(this.alwaysUse);
            } else cts = CompletionType.getCompletionTypes(this.completionTypes.get(lastArgIndex));
            for (final CompletionType ct : cts) {
                possibilities.addAll(this.getCompletionsFor(cs, cmd, label, eargs, ct));
            }
            return possibilities;
        } catch (final Throwable t) {
            this.handleException(cs, cmd, label, args, t, "An exception occurred while tab-completing that command.");
            return null;
        }
    }

    protected void setAlwaysUse(final CompletionType alwaysUse) {
        this.alwaysUse = alwaysUse;
    }

    protected enum CompletionType {
        /**
         * Completes for any online player.
         */
        ONLINE_PLAYER((short) 1),
        /**
         * Completes for any item alias in items.csv.
         */
        ITEM_ALIAS((short) 2),
        /**
         * Completes for any material name (see {@link Material}).
         */
        ITEM((short) 4),
        /**
         * Completes based on
         * {@link #getCustomCompletions(CommandSender, Command, String, String[], String)},
         * which can be overridden.
         */
        CUSTOM((short) 8),
        /**
         * Completes for any plugin loaded.
         */
        PLUGIN((short) 16),
        /**
         * Completes for any world loaded.
         */
        WORLD((short) 32),
        /**
         * Completes for a list specified by
         * {@link #customList(CommandSender, Command, String, String[], String)}.
         */
        LIST((short) 64),
        /**
         * Completes for an enum specified by
         * {@link #customEnum(CommandSender, Command, String, String[], String)}.
         */
        ENUM((short) 128),
        /**
         * Completes for any command from the plugin this command is registered under.
         */
        PLUGIN_COMMAND((short) 256),
        /**
         * Completes for any command.
         */
        ANY_COMMAND((short) 512);

        private final short s;

        CompletionType(final short s) {
            this.s = s;
        }

        public static List<CompletionType> getCompletionTypes(final int i) {
            final List<CompletionType> cts = new ArrayList<>();
            for (final CompletionType ct : CompletionType.values()) {
                if ((i & ct.getShort()) <= 0) continue;
                cts.add(ct);
            }
            return cts;
        }

        public short getShort() {
            return this.s;
        }
    }
}
