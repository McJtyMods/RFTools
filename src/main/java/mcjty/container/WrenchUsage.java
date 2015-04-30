package mcjty.container;

public enum WrenchUsage {
    NOT,                // Not a wrench
    NORMAL,             // Normal wrench usage
    SNEAKING,           // Sneaking mode with wrench
    DISABLED,           // It is a wrench but it is disabled
    SELECT,             // In select mode (smart wrench only)
    SNEAK_SELECT,       // Sneak select mode (smart wrench only)
}
