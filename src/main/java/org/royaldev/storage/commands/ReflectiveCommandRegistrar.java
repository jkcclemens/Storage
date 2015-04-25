package org.royaldev.storage.commands;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandMap;
import org.bukkit.command.PluginCommand;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

public class ReflectiveCommandRegistrar<T extends Plugin> {

    @NotNull
    private final T plugin;
    private CommandMap cm;
    private FileConfiguration pluginYml;

    public ReflectiveCommandRegistrar(@NotNull final T plugin) {
        Preconditions.checkNotNull(plugin, "plugin was null");
        this.plugin = plugin;
        this.pluginYml = YamlConfiguration.loadConfiguration(new InputStreamReader(this.plugin.getResource("plugin.yml")));
    }

    @Nullable
    public static Object getPrivateField(@NotNull final Object object, @NotNull final String field) throws SecurityException, NoSuchFieldException, IllegalArgumentException, IllegalAccessException {
        Preconditions.checkNotNull(object, "object was null");
        Preconditions.checkNotNull(field, "field was null");
        final Class<?> clazz = object.getClass();
        final Field objectField = clazz.getDeclaredField(field);
        final boolean wasAccessible = objectField.isAccessible();
        objectField.setAccessible(true);
        final Object result = objectField.get(object);
        objectField.setAccessible(wasAccessible);
        return result;
    }

    @Nullable
    private Class<? extends BaseCommand> getClass(@NotNull final String className) throws ClassNotFoundException {
        Preconditions.checkNotNull(className, "className was null");
        final String[] parts = this.plugin.getClass().getName().split("\\.");
        final String classPath = Joiner.on('.').join(Arrays.copyOfRange(parts, 0, parts.length > 1 ? parts.length - 1 : parts.length));
        final Class<?> clazz = Class.forName(classPath + "." + className);
        if (!BaseCommand.class.isAssignableFrom(clazz)) return null;
        if (!clazz.isAnnotationPresent(ReflectCommand.class)) return null;
        //noinspection unchecked
        return (Class<BaseCommand>) clazz;
    }

    @Nullable
    private CommandMap getCommandMap() {
        if (this.cm != null) return this.cm;
        final Field map;
        try {
            map = this.plugin.getServer().getPluginManager().getClass().getDeclaredField("commandMap");
            map.setAccessible(true);
            this.cm = (CommandMap) map.get(this.plugin.getServer().getPluginManager());
            return this.cm;
        } catch (final Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @NotNull
    private List<String> getCommands() {
        return this.pluginYml.getStringList("reflectcommands");
    }

    @Nullable
    private ReflectCommand getReflectCommand(@NotNull final Class<? extends BaseCommand> baseCommandClass) {
        Preconditions.checkNotNull(baseCommandClass, "baseCommandClass was null");
        return baseCommandClass.getAnnotation(ReflectCommand.class);
    }

    @Nullable
    private ReflectCommand getReflectCommand(@NotNull final BaseCommand<? extends Plugin> baseCommand) {
        Preconditions.checkNotNull(baseCommand, "baseCommand was null");
        return this.getReflectCommand(baseCommand.getClass());
    }

    /**
     * Registers a command in the server's CommandMap.
     *
     * @param ce CommandExecutor to be registered
     * @param rc ReflectCommand the command was annotated with
     */
    public void registerCommand(@NotNull final CommandExecutor ce, @NotNull final ReflectCommand rc) {
        Preconditions.checkNotNull(ce, "ce was null");
        Preconditions.checkNotNull(rc, "rc was null");
        try {
            final Constructor c = PluginCommand.class.getDeclaredConstructor(String.class, Plugin.class);
            c.setAccessible(true);
            final PluginCommand pc = (PluginCommand) c.newInstance(rc.name(), this.plugin);
            pc.setExecutor(ce);
            pc.setAliases(Arrays.asList(rc.aliases()));
            pc.setDescription(rc.description());
            pc.setUsage(rc.usage());
            final CommandMap cm = this.getCommandMap();
            if (cm == null) {
                this.plugin.getLogger().warning("CommandMap was null. Command " + rc.name() + " not registered.");
                return;
            }
            cm.register(this.plugin.getDescription().getName(), pc);
        } catch (Exception e) {
            this.plugin.getLogger().warning("Could not register command \"" + rc.name() + "\" - an error occurred: " + e.getMessage() + ".");
        }
    }

    public void registerCommands() {
        for (final String className : this.getCommands()) {
            try {
                final Class<? extends BaseCommand> clazz = this.getClass(className);
                if (clazz == null) continue;
                final ReflectCommand rc = this.getReflectCommand(clazz);
                if (rc == null) continue;
                final Constructor c = clazz.getConstructor(this.plugin.getClass(), String.class);
                final Object o = c.newInstance(this.plugin, rc.name());
                if (!(o instanceof BaseCommand)) continue;
                this.registerCommand((CommandExecutor) o, rc);
            } catch (Exception e) {
                this.plugin.getLogger().warning("Could not register command \"" + className + "\" - an error occurred (" + e.getClass().getSimpleName() + "): " + e.getMessage() + ".");
            }
        }
    }

}
