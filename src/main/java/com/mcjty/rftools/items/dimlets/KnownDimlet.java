package com.mcjty.rftools.items.dimlets;

import net.minecraft.item.Item;

public class KnownDimlet extends Item {

    // A number between 0 and 1 indicating the rarity of this dimlet. 1 prevents
    // further dimlets in the list from being selected while 0 will prevent a dimlet from
    // being selected at all.
    private float rarity;

    public KnownDimlet() {
        setMaxStackSize(16);
    }

    public float getRarity() {
        return rarity;
    }

    public void setRarity(float rarity) {
        this.rarity = rarity;
    }
}
