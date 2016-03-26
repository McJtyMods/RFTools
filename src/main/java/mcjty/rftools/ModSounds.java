package mcjty.rftools;

import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.relauncher.ReflectionHelper;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public class ModSounds {

    public static final String[] REGISTER_SOUND = { "registerSound", "func_187502_a", "a" };

    public static void init() {
        try {
            Method m = ReflectionHelper.findMethod(SoundEvent.class, null, REGISTER_SOUND, String.class);
            m.invoke(null, RFTools.MODID + ":teleport_whoosh");
            m.invoke(null, RFTools.MODID + ":teleport_error");
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

}
