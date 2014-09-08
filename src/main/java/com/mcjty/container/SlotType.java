package com.mcjty.container;

public enum SlotType {
    SLOT_UNKNOWN,
    SLOT_GHOST,             // Ghost slot as used by crafting grids
    SLOT_INPUT,             // Inventory slot that can accept items in sided inventories
    SLOT_OUTPUT,            // Inventory slot that can output items in sided inventories
    SLOT_CONTAINER,         // Inventory slot that cannot accept nor output items in sided inventories
    SLOT_PLAYERINV,         // Player inventory slot
    SLOT_PLAYERHOTBAR,      // Player hotbar slot
}
