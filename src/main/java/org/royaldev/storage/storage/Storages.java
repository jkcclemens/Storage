package org.royaldev.storage.storage;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.royaldev.storage.StoragePlugin;
import org.royaldev.storage.storage.impl.FilePlayerItemStackStorage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

public class Storages<OWNER, CONTAINED> {

    private final StoragePlugin plugin;

    @NotNull
    private final List<Storage<OWNER, CONTAINED>> storages = Lists.newArrayList();

    public Storages(final StoragePlugin plugin) {
        this.plugin = plugin;
    }

    public boolean addStorage(@NotNull final Storage<OWNER, CONTAINED> storage) {
        Preconditions.checkNotNull(storage, "storage was null");
        return this.storages.add(storage);
    }

    @Nullable
    public Storage<OWNER, CONTAINED> getStorage(@NotNull final OWNER owner) {
        Preconditions.checkNotNull(owner, "owner was null");
        return this.storages.stream().filter(s -> Objects.equals(s.getOwner(), owner)).findAny().orElse(null);
    }

    @NotNull
    public Storage<OWNER, CONTAINED> getStorageOrAdd(@NotNull final OWNER owner, @NotNull final Function<OWNER, Storage<OWNER, CONTAINED>> function) {
        Preconditions.checkNotNull(owner, "owner was null");
        Preconditions.checkNotNull(function, "function was null");
        final Storage<OWNER, CONTAINED> storage = this.getStorage(owner);
        if (storage != null) {
            return storage;
        }
        final Storage<OWNER, CONTAINED> newStorage = function.apply(owner);
        Preconditions.checkNotNull(newStorage, "The storage that function produced was null");
        this.addStorage(newStorage);
        return newStorage;
    }

    @NotNull
    public List<Storage<OWNER, CONTAINED>> getStorages() {
        return Collections.unmodifiableList(this.storages);
    }

    public void loadAll() {
        final File file = new File(this.plugin.getDataFolder(), "storages.dat");
        if (!file.exists()) {
            return;
        }
        try (final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            @SuppressWarnings("unchecked")
            final List<Map<String, Object>> serialize = (List<Map<String, Object>>) ois.readObject();
            final List<Storage> storages = serialize.stream().map(FilePlayerItemStackStorage::deserialize).collect(Collectors.toList());
            storages.forEach(this.storages::add);
        } catch (final IOException | ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public boolean remove(@NotNull final OWNER owner) {
        Preconditions.checkNotNull(owner, "owner was null");
        final Storage<OWNER, CONTAINED> storage = this.getStorage(owner);
        return storage != null && this.storages.remove(storage);
    }

    public void saveAll() {
        final File file = new File(this.plugin.getDataFolder(), "storages.dat");
        if (!file.exists()) {
            try {
                //noinspection ResultOfMethodCallIgnored
                file.getParentFile().mkdirs();
                //noinspection ResultOfMethodCallIgnored
                file.createNewFile();
            } catch (final IOException ex) {
                ex.printStackTrace();
                return;
            }
        }
        final List<Map<String, Object>> serialize = Lists.newArrayList();
        for (final Storage<OWNER, CONTAINED> storage : this.storages) {
            if (!(storage instanceof FilePlayerItemStackStorage)) {
                continue;
            }
            final FilePlayerItemStackStorage fpiss = (FilePlayerItemStackStorage) storage;
            serialize.add(fpiss.serialize());
        }
        try (final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(file))) {
            oos.writeObject(serialize);
            oos.flush();
        } catch (final IOException ex) {
            ex.printStackTrace();
        }
    }

}
