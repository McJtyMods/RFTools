package mcjty.rftools.setup;


import mcjty.lib.McJtyRegister;
import mcjty.lib.datafix.fixes.TileEntityNamespace;
import mcjty.rftools.ModSounds;
import mcjty.rftools.RFTools;
import mcjty.rftools.blocks.ModBlocks;
import mcjty.rftools.blocks.screens.ScreenSetup;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.datafix.FixTypes;
import net.minecraftforge.common.util.ModFixs;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.oredict.OreDictionary;

import java.util.HashMap;
import java.util.Map;

@Mod.EventBusSubscriber
public class Registration {

    @SubscribeEvent
    public static void registerBlocks(RegistryEvent.Register<Block> event) {
        ModFixs modFixs = FMLCommonHandler.instance().getDataFixer().init(RFTools.MODID, 2);
        McJtyRegister.registerBlocks(RFTools.instance, event.getRegistry(), modFixs, 1);

        // We used to accidentally register TEs with names like "minecraft:rftools_solid_shield_block" instead of "rftools:solid_shield_block".
        // Set up a DataFixer to map these incorrect names to the correct ones, so that we don't break old saved games.
        // @todo Remove all this if we ever break saved-game compatibility.
        Map<String, String> oldToNewIdMap = new HashMap<>();
        oldToNewIdMap.put(RFTools.MODID + "_invisible_shield_block", RFTools.MODID + ":invisible_shield_block");
        oldToNewIdMap.put("minecraft:" + RFTools.MODID + "_invisible_shield_block", RFTools.MODID + ":invisible_shield_block");
        oldToNewIdMap.put(RFTools.MODID + "_notick_invisible_shield_block", RFTools.MODID + ":notick_invisible_shield_block");
        oldToNewIdMap.put("minecraft:" + RFTools.MODID + "_notick_invisible_shield_block", RFTools.MODID + ":notick_invisible_shield_block");
        oldToNewIdMap.put(RFTools.MODID + "_solid_shield_block", RFTools.MODID + ":solid_shield_block");
        oldToNewIdMap.put("minecraft:" + RFTools.MODID + "_solid_shield_block", RFTools.MODID + ":solid_shield_block");
        oldToNewIdMap.put(RFTools.MODID + "_notick_solid_shield_block", RFTools.MODID + ":notick_solid_shield_block");
        oldToNewIdMap.put("minecraft:" + RFTools.MODID + "_notick_solid_shield_block", RFTools.MODID + ":notick_solid_shield_block");
        modFixs.registerFix(FixTypes.BLOCK_ENTITY, new TileEntityNamespace(oldToNewIdMap, 2));
    }

    @SubscribeEvent
    public static void registerItems(RegistryEvent.Register<Item> event) {
        McJtyRegister.registerItems(RFTools.instance, event.getRegistry());
        OreDictionary.registerOre("oreDimensionalShard", new ItemStack(ModBlocks.dimensionalShardBlock, 1, OreDictionary.WILDCARD_VALUE));
    }

    @SubscribeEvent
    public static void registerSounds(RegistryEvent.Register<SoundEvent> sounds) {
        ModSounds.init(sounds.getRegistry());
    }


    @SubscribeEvent
    public static void onMissingItemMappings(RegistryEvent.MissingMappings<Item> event) {
        ResourceLocation screenHit = ScreenSetup.screenHitBlock.getRegistryName();
        for(RegistryEvent.MissingMappings.Mapping<Item> mapping : event.getAllMappings()) {
            if(screenHit.equals(mapping.key)) {
                // The screen hit block used to (incorrectly) have an ItemBlock.
                // Don't bother warning the player that this is gone.
                mapping.ignore();
                break;
            }
        }
    }
}
