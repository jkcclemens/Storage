package org.royaldev.storage.storage.impl;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.royaldev.storage.storage.Storage;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class FilePlayerItemStackStorage implements Storage<UUID, ItemStack> {

    private final List<ItemStack> storedItems = Lists.newArrayList();
    private UUID owner;

    public FilePlayerItemStackStorage(@NotNull final UUID owner) {
        Preconditions.checkNotNull(owner, "owner was null");
        this.owner = owner;
    }

    @NotNull
    public static FilePlayerItemStackStorage deserialize(@NotNull final Map<String, Object> serialized) {
        Preconditions.checkNotNull(serialized, "serialized was null");
        Preconditions.checkArgument(
            serialized.containsKey("owner") &&
                serialized.containsKey("storedItems") &&
                serialized.get("storedItems") instanceof List,
            "The serialized object did not have all the required fields.");
        final Object owner = serialized.get("owner");
        final Object storedItems = serialized.get("storedItems");
        Preconditions.checkArgument(
            owner instanceof UUID &&
                storedItems instanceof List,
            "The required fields were not of the correct types."
        );
        @SuppressWarnings("ConstantConditions")
        final FilePlayerItemStackStorage fpiss = new FilePlayerItemStackStorage((UUID) owner);
        @SuppressWarnings({"unchecked", "ConstantConditions"})
        final List<ItemStack> deserializedItems = ((List<Map<String, Object>>) storedItems).stream()
            .map(ItemStack::deserialize)
            .collect(Collectors.toList());
        fpiss.addAll(deserializedItems);
        return fpiss;
    }

    @Nullable
    private ItemStack getSimilar(@NotNull final ItemStack item) {
        Preconditions.checkNotNull(item, "item was null");
        return this.storedItems.stream().filter(i -> i.isSimilar(item)).findFirst().orElse(null);
    }

    @Override
    public boolean add(@NotNull final ItemStack item) {
        Preconditions.checkNotNull(item, "item was null");
        if (item.getAmount() == 0) return true;
        final ItemStack stored = this.getSimilar(item);
        if (stored != null) {
            stored.setAmount(stored.getAmount() + item.getAmount());
            return true;
        }
        return this.storedItems.add(item);
    }

    @Override
    public boolean addAll(@NotNull final Collection<ItemStack> item) {
        Preconditions.checkNotNull(item, "item was null");
        if (item.size() == 0) return true;
        boolean result = true;
        for (final ItemStack i : item) {
            result &= this.add(i);
        }
        return result;
    }

    @Override
    public Collection<ItemStack> getAll() {
        return Collections.unmodifiableCollection(this.storedItems);
    }

    @Override
    public UUID getOwner() {
        return this.owner;
    }

    @Override
    public void setOwner(@NotNull final UUID owner) {
        Preconditions.checkNotNull(owner, "owner was null");
        this.owner = owner;
    }

    @Override
    public boolean remove(@NotNull final ItemStack item) {
        Preconditions.checkNotNull(item, "item was null");
        if (item.getAmount() == 0) return true;
        final ItemStack stored = this.getSimilar(item);
        if (stored == null) {
            return false;
        }
        stored.setAmount(Math.max(0, stored.getAmount() - item.getAmount()));
        if (stored.getAmount() == 0) {
            this.storedItems.remove(stored);
        }
        return true;
    }

    @Override
    public boolean removeAll(@NotNull final Collection<ItemStack> item) {
        Preconditions.checkNotNull(item, "item was null");
        if (item.size() == 0) return true;
        boolean result = true;
        for (final ItemStack i : item) {
            result &= this.remove(i);
        }
        return result;
    }

    @NotNull
    public Map<String, Object> serialize() {
        final Map<String, Object> serialized = Maps.newHashMap();
        serialized.put("owner", this.owner);
        final List<Map<String, Object>> serializedItems = this.storedItems.stream()
            .map(ItemStack::serialize)
            .collect(Collectors.toList());
        serialized.put("storedItems", serializedItems);
        return serialized;
    }
}
