package org.royaldev.storage;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;

/**
 * A class that builds ItemStacks in an easy-to-use, simple way.
 * <br/>
 * To start, use either a new instance of ItemStackBuilder or {@link #newBuilder()} to get a fresh builder. Then, simply
 * chain methods together to produce the desired ItemStack. Once finished, use the {@link #build()} method to construct
 * the ItemStack.
 * <br/>
 * The only required call is to {@link #type(Material)}. The rest of the information has a default.
 */
public class ItemStackBuilder {

    private final List<String> lore = new ArrayList<>();
    private Material material;
    private int amount = 1;
    private short damage = 0;
    private String customName = null;

    /**
     * A convenience method that simply returns a new instance of ItemStackBuilder. Using
     * <code>new ItemStackBuilder()</code> is just as acceptable.
     *
     * @return A new instance of ItemStackBuilder
     */
    public static ItemStackBuilder newBuilder() {
        return new ItemStackBuilder();
    }

    /**
     * Sets the amount of this ItemStack. Will default to 1 if not called.
     *
     * @param amount New amount
     * @return The builder
     */
    public ItemStackBuilder amount(final int amount) {
        this.amount = amount;
        return this;
    }

    /**
     * Builds the ItemStack with any previous calls accounted for. Future calls will not change this ItemStack.
     *
     * @return ItemStack
     */
    public ItemStack build() {
        final ItemStack is = new ItemStack(this.material, this.amount, this.damage);
        final ItemMeta im = is.getItemMeta();
        im.setDisplayName(this.customName);
        im.setLore(this.lore);
        is.setItemMeta(im);
        // TODO: Other types of meta
        return is;
    }

    /**
     * Removes all lore from this ItemStack.
     *
     * @return The builder
     */
    public ItemStackBuilder clearLore() {
        this.lore.clear();
        return this;
    }

    /**
     * Sets the custom name, or display name, of this ItemStack. null will remove it.
     *
     * @param customName New custom name
     * @return The builder
     */
    public ItemStackBuilder customName(final String customName) {
        this.customName = customName;
        return this;
    }

    /**
     * Sets the damage, or durability, of this ItemStack. Will default to 0 if not called.
     *
     * @param damage New damage
     * @return The builder
     */
    public ItemStackBuilder damage(final short damage) {
        this.damage = damage;
        return this;
    }

    /**
     * Adds a string of lore to this ItemStack.
     *
     * @param lore Next string of lore
     * @return The builder
     * @see #lore(String... lore)
     */
    public ItemStackBuilder lore(final String lore) {
        this.lore.add(lore);
        return this;
    }

    /**
     * Adds multiple strings of lore to this ItemStack.
     *
     * @param lore Next strings of lore
     * @return The builder
     * @see #lore(String)
     */
    public ItemStackBuilder lore(final String... lore) {
        for (final String s : lore) {
            this.lore(s);
        }
        return this;
    }

    /**
     * Adds multiple strings of lore to this ItemStack.
     *
     * @param lore Next strings of lore
     * @return The builder
     * @see #lore(String)
     */
    public ItemStackBuilder lore(final List<String> lore) {
        this.lore(lore.toArray(new String[lore.size()]));
        return this;
    }

    /**
     * Sets the type of ItemStack this is. This is required to be called, or an exception will be thrown on
     * {@link #build()}.
     *
     * @param material New type
     * @return The builder
     */
    public ItemStackBuilder type(final Material material) {
        this.material = material;
        return this;
    }

}
