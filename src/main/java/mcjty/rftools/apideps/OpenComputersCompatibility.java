package mcjty.rftools.apideps;

import li.cil.oc.api.Driver;
import li.cil.oc.api.driver.EnvironmentAware;
import li.cil.oc.api.network.Environment;
import li.cil.oc.api.network.ManagedEnvironment;
import li.cil.oc.api.prefab.DriverBlock;
import mcjty.rftools.blocks.blockprotector.BlockProtectorSetup;
import mcjty.rftools.blocks.blockprotector.BlockProtectorTileEntity;
import mcjty.rftools.blocks.dimlets.DimensionBuilderTileEntity;
import mcjty.rftools.blocks.dimlets.DimletSetup;
import mcjty.rftools.blocks.environmental.EnvironmentalControllerTileEntity;
import mcjty.rftools.blocks.environmental.EnvironmentalSetup;
import mcjty.rftools.blocks.screens.ScreenControllerTileEntity;
import mcjty.rftools.blocks.screens.ScreenSetup;
import mcjty.rftools.blocks.shield.ShieldSetup;
import mcjty.rftools.blocks.shield.ShieldTEBase;
import mcjty.rftools.blocks.spaceprojector.BuilderTileEntity;
import mcjty.rftools.blocks.spaceprojector.SpaceProjectorSetup;
import mcjty.rftools.blocks.spaceprojector.SpaceProjectorTileEntity;
import mcjty.rftools.blocks.teleporter.DialingDeviceTileEntity;
import mcjty.rftools.blocks.teleporter.TeleporterSetup;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class OpenComputersCompatibility {

    public static void registerOC() {
        Driver.add(new RFToolsEnvironmentAware());
    }

    public static class RFToolsEnvironmentAware extends DriverBlock implements EnvironmentAware {
        @Override
        public Class<? extends Environment> providedEnvironment(ItemStack stack) {
            if (stack.getItem() instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) stack.getItem();
                Block b = itemBlock.field_150939_a;
                if (b == ShieldSetup.shieldBlock || b == ShieldSetup.shieldBlock2 || b == ShieldSetup.shieldBlock3 || b == ShieldSetup.shieldBlock4) {
                    return (Class<? extends Environment>) (Object) ShieldTEBase.class;
                } else if (b == SpaceProjectorSetup.builderBlock) {
                    return (Class<? extends Environment>) (Object) BuilderTileEntity.class;
                } else if (b == TeleporterSetup.dialingDeviceBlock) {
                    return (Class<? extends Environment>) (Object) DialingDeviceTileEntity.class;
                } else if (b == DimletSetup.dimensionBuilderBlock || b == DimletSetup.creativeDimensionBuilderBlock) {
                    return (Class<? extends Environment>) (Object) DimensionBuilderTileEntity.class;
                } else if (b == ScreenSetup.screenControllerBlock) {
                    return (Class<? extends Environment>) (Object) ScreenControllerTileEntity.class;
                } else if (b == SpaceProjectorSetup.spaceProjectorBlock) {
                    return (Class<? extends Environment>) (Object) SpaceProjectorTileEntity.class;
                } else if (b == BlockProtectorSetup.blockProtectorBlock) {
                    return (Class<? extends Environment>) (Object) BlockProtectorTileEntity.class;
                } else if (b == EnvironmentalSetup.environmentalControllerBlock) {
                    return (Class<? extends Environment>) (Object) EnvironmentalControllerTileEntity.class;
                }
            }
            return null;
        }

        @Override
        public ManagedEnvironment createEnvironment(World world, int x, int y, int z) {
            return null;
        }
    }
}
