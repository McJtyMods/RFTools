package mcjty.rftools.blocks.elevator;

import mcjty.lib.varia.GlobalCoordinate;
import mcjty.rftools.RFTools;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.MovingSound;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class ElevatorSounds {

    private static final Map<GlobalCoordinate, MovingSound> sounds = new HashMap<>();

    public static final String[] REGISTER_SOUND = { "registerSound", "func_187502_a", "a" };

    public static void init() {
        try {
            Method m = ReflectionHelper.findMethod(SoundEvent.class, null, REGISTER_SOUND, String.class);
            m.invoke(null, RFTools.MODID + ":elevator_start");
            m.invoke(null, RFTools.MODID + ":elevator_loop");
            m.invoke(null, RFTools.MODID + ":elevator_stop");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    public static void moveSound(World world, BlockPos pos, float y) {
        GlobalCoordinate g = new GlobalCoordinate(pos, world.provider.getDimension());
        if (sounds.containsKey(g)) {
            MovingSound movingSound = sounds.get(g);
            // @todo slightly dirty. A superclass would be nice. Move to mcjtylib perhaps?
            if (movingSound instanceof ElevatorLoopSound) {
                ((ElevatorLoopSound) movingSound).move(movingSound.getXPosF(), y, movingSound.getZPosF());
            } else if (movingSound instanceof ElevatorStartupSound) {
                ((ElevatorStartupSound) movingSound).move(movingSound.getXPosF(), y, movingSound.getZPosF());
            } else if (movingSound instanceof ElevatorStopSound) {
                ((ElevatorStopSound) movingSound).move(movingSound.getXPosF(), y, movingSound.getZPosF());
            }
        }
    }


    public static void stopSound(World worldObj, BlockPos pos) {
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.provider.getDimension());
        if (sounds.containsKey(g)) {
            MovingSound movingSound = sounds.get(g);
            Minecraft.getMinecraft().getSoundHandler().stopSound(movingSound);
            sounds.remove(g);
        }
    }

    private static void playSound(World worldObj, BlockPos pos, MovingSound sound) {
        stopSound(worldObj, pos);
        Minecraft.getMinecraft().getSoundHandler().playSound(sound);
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.provider.getDimension());
        sounds.put(g, sound);
    }


    public static void playStartup(World worldObj, BlockPos pos) {
        MovingSound sound = new ElevatorStartupSound(worldObj, pos.getX(), pos.getY(), pos.getZ());
        playSound(worldObj, pos, sound);
    }

    public static void playLoop(World worldObj, BlockPos pos) {
        MovingSound sound = new ElevatorLoopSound(worldObj, pos.getX(), pos.getY(), pos.getZ());
        playSound(worldObj, pos, sound);
    }

    public static void playStop(World worldObj, BlockPos pos) {
        MovingSound sound = new ElevatorStopSound(worldObj, pos.getX(), pos.getY(), pos.getZ());
        playSound(worldObj, pos, sound);
    }

    public static boolean isStartupPlaying(World worldObj, BlockPos pos) {
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.provider.getDimension());
        MovingSound movingSound = sounds.get(g);
        return movingSound instanceof ElevatorStartupSound;
    }

    public static boolean isLoopPlaying(World worldObj, BlockPos pos) {
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.provider.getDimension());
        MovingSound movingSound = sounds.get(g);
        return movingSound instanceof ElevatorLoopSound;
    }

    public static boolean isStopPlaying(World worldObj, BlockPos pos) {
        GlobalCoordinate g = new GlobalCoordinate(pos, worldObj.provider.getDimension());
        MovingSound movingSound = sounds.get(g);
        return movingSound instanceof ElevatorStopSound;
    }
}
