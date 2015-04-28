package mcjty.rftools.items;

import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import cpw.mods.fml.common.Optional;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

@Optional.InterfaceList({
        @Optional.Interface(iface = "buildcraft.api.tools.IToolWrench", modid = "BuildCraft|Core"),
        @Optional.Interface(iface = "cofh.api.item.IToolHammer", modid = "CoFHCore")})
public class SmartWrenchItem extends Item implements IToolWrench, IToolHammer {

    public SmartWrenchItem() {
        setMaxStackSize(1);
    }

    @Override
    @Optional.Method(modid = "CoFHCore")
    public boolean isUsable(ItemStack item, EntityLivingBase user, int x, int y, int z) {
        return true;
    }

    @Override
    @Optional.Method(modid = "CoFHCore")
    public void toolUsed(ItemStack item, EntityLivingBase user, int x, int y, int z) {
    }

    @Override
    @Optional.Method(modid = "BuildCraft|Core")
    public boolean canWrench(EntityPlayer player, int x, int y, int z) {
        return true;
    }

    @Override
    @Optional.Method(modid = "BuildCraft|Core")
    public void wrenchUsed(EntityPlayer player, int x, int y, int z) {
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack) {
        return 1;
    }

    @Override
    public boolean onItemUse(ItemStack stack, EntityPlayer player, World world, int x, int y, int z, int side, float sx, float sy, float sz) {
        return true;
    }
}