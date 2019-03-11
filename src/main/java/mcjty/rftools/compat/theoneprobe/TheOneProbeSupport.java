package mcjty.rftools.compat.theoneprobe;

import mcjty.lib.varia.Logging;
import mcjty.theoneprobe.api.IProbeInfo;
import mcjty.theoneprobe.api.ITheOneProbe;

import javax.annotation.Nullable;

public class TheOneProbeSupport implements com.google.common.base.Function<ITheOneProbe, Void> {

    public static ITheOneProbe probe;

    public static int ELEMENT_SEQUNCER;

    @Nullable
    @Override
    public Void apply(ITheOneProbe theOneProbe) {
        probe = theOneProbe;
        Logging.log("Enabled support for The One Probe");
        ELEMENT_SEQUNCER = probe.registerElementFactory(ElementSequencer::new);
        return null;
    }

    public static IProbeInfo addSequenceElement(IProbeInfo probeInfo, long bits, int current, boolean large) {
        return probeInfo.element(new ElementSequencer(bits, current, large));
    }
}