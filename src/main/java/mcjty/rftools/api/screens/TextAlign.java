package mcjty.rftools.api.screens;

public enum TextAlign {
    ALIGN_LEFT,
    ALIGN_CENTER,
    ALIGN_RIGHT;

    public static TextAlign get(String alignment) {
        return "Left".equals(alignment) ? TextAlign.ALIGN_LEFT : ("Right".equals(alignment) ? TextAlign.ALIGN_RIGHT : TextAlign.ALIGN_CENTER);
    }
}
