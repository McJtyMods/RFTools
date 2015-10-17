package mcjty.rftools.blocks.special;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import mcjty.lib.entity.GenericTileEntity;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

public class VolcanicCoreTileEntity extends GenericTileEntity {
    // Client side only.
    private VolcanicRumbleSound sound = null;

    // Activity cycle.
    private int cycle = 500 + VolcanicEvents.random.nextInt(1500);

    @Override
    protected void checkStateClient() {
        if (sound == null) {
            playRumble();
        }
    }

    @SideOnly(Side.CLIENT)
    private void playRumble() {
        EntityClientPlayerMP player = Minecraft.getMinecraft().thePlayer;
        sound = new VolcanicRumbleSound(player, worldObj, xCoord, yCoord, zCoord);
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
//            RFTools.log("++++ Start rumble at " + xCoord + "," + yCoord + "," + zCoord);
    }

    @Override
    public void invalidate() {
        super.invalidate();
        if (worldObj.isRemote) {
            stopRumble();
        }
    }

    @SideOnly(Side.CLIENT)
    private void stopRumble() {
        if (sound != null) {
            //            RFTools.log("---- Stop rumble at " + xCoord + "," + yCoord + "," + zCoord);
            Minecraft.getMinecraft().getSoundHandler().stopSound(sound);
            sound = null;
        }
    }

    @Override
    protected void checkStateServer() {
        cycle++;
        markDirty();
        float activityChance;
        int c = cycle % 1500;
        if (c < 400) {
            activityChance = 0.0001f;
        } else if (c < 600) {
            activityChance = 0.01f;
        } else if (c < 800) {
            activityChance = 0.05f;
        } else if (c < 1200) {
            activityChance = 0.01f;
        } else {
            activityChance = 0.0001f;
        }

        if (VolcanicEvents.random.nextFloat() < activityChance) {
            switch (VolcanicEvents.random.nextInt(16)) {
                case 0:
                    VolcanicEvents.spawnVolcanicBlocks(worldObj, xCoord, yCoord, zCoord, 7);
                    break;
                case 1:
                case 2:
                    VolcanicEvents.explosion(worldObj, xCoord, yCoord, zCoord, 7, 5.0f);
                    break;
                case 10:
                case 11:
                case 12:
                    VolcanicEvents.randomFire(worldObj, xCoord, yCoord, zCoord, 12);
                    break;
                case 14:
                case 15:
                    VolcanicEvents.randomLava(worldObj, xCoord, yCoord, zCoord, 1);
                    break;
                default:
                    spawnVolcanicBlock();
                    break;
            }
        }
    }

    private void spawnVolcanicBlock() {
        int rx = VolcanicEvents.random.nextInt(3)-1;
        int ry = VolcanicEvents.random.nextInt(3)-1;
        int rz = VolcanicEvents.random.nextInt(3)-1;
        if (rx != 0 || ry != 0 || rz != 0) {
            int x = xCoord + rx;
            int y = yCoord + ry;
            int z = zCoord + rz;
            if (y < 0 || y >= worldObj.getHeight()) {
                return;
            }
            Block block = worldObj.getBlock(x, y, z);
            if (block == null || block.getMaterial() == Material.air) {
                worldObj.setBlock(x, y, z, SpecialSetup.volcanicBlock, 15, 2);
            }
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        cycle = tagCompound.getInteger("cycle");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setInteger("cycle", cycle);
    }
}
