package net.katch0420.macebot.client.gui.themes;

public class Themes {

    public static Theme DEFAULT = new Theme(
            true,
            0xFFEEEEEE, 0xFF202020,
            0xFFEEEEEE, 0xFF252525,
            0xFFEEEEEE, 0xFF252525,
            0xFFEEEEEE, 0xFF303030,
            0xFF101010,
            0xFF202020, 0xFFEEEEEE, 0xFF303030, 0xFF101010,
            0xFFDEDEDE, 0xFFDDDDDD, 0xFFBBBBBB, 0xFF909090,
            0xFF606060, 0xFFCCCCCC, 0xFF707070,
            0xFF707070, 0xFF808080,
            0xFF3CB371, 0xFFD9A441, 0xFFD9534F,
            0x80000000, 0xFFFFFFFF
    );

    public static Theme PROFESSIONAL_DARK = new Theme(
            true,
            0xFFE7E9EC, 0xFF1B1D21,
            0xFFFFFFFF, 0xFF202329,
            0xFFB6BAC2, 0xFF1F2126,
            0xFFB6BAC2, 0xFF202329,
            0xFF34373D,
            0xFF22252B, 0xFFE7E9EC, 0xFF34373D, 0xFF15171B,
            0xFF9AA0AA, 0xFFE7E9EC, 0xFFB6BAC2, 0xFFD4D7DC,
            0xFF2E5C8A, 0xFFFFFFFF, 0xFF3D7BB8,
            0xFF3D7BB8, 0xFF4A8FD1,
            0xFF3CB371, 0xFFD9A441, 0xFFD9534F,
            0x99000000, 0xFF63A6E0
    );

    public static Theme PROFESSIONAL_LIGHT = new Theme(
            true,
            0xFF202329, 0xFFF4F5F7,
            0xFF202329, 0xFFFFFFFF,
            0xFF55585F, 0xFFEDEEF0,
            0xFF55585F, 0xFFFFFFFF,
            0xFFDCDEE2,
            0xFFFFFFFF, 0xFF202329, 0xFFDCDEE2, 0xFFE9EAEC,
            0xFF6A6E76, 0xFF202329, 0xFF55585F, 0xFF36383D,
            0xFF3D7BB8, 0xFFFFFFFF, 0xFF2E5C8A,
            0xFF2E5C8A, 0xFF3D7BB8,
            0xFF2E9E5C, 0xFFB8841E, 0xFFC9433D,
            0x99FFFFFF, 0xFF2E5C8A
    );

    public static Theme CURRENT = ThemeManager.load();
}