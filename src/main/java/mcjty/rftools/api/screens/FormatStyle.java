package mcjty.rftools.api.screens;

public enum FormatStyle {
    MODE_FULL("Full"),
    MODE_COMPACT("Compact"),
    MODE_COMMAS("Commas");

    private final String name;

    FormatStyle(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static FormatStyle getStyle(String name) {
        for (FormatStyle style : values()) {
            if (name.equals(style.getName())) {
                return style;
            }
        }
        return MODE_FULL;
    }
}
