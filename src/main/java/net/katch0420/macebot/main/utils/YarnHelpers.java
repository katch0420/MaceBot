package net.katch0420.macebot.main.utils;

import net.minecraft.client.gui.*;
import org.jetbrains.annotations.UnknownNullability;

public class YarnHelpers {
    //? if >1.21.11 {
    /*public static void pushMatrix(@UnknownNullability GuiGraphicsExtractor c){
        //? if >=1.21.6 {
        /^c.pose().pushMatrix();
        ^///?} else
        c.pose().pushPose();
    }

    public static void popMatrix(@UnknownNullability GuiGraphicsExtractor c){
        //? if >=1.21.6 {
        /^c.pose().popMatrix();
        ^///?} else
        c.pose().popPose();
    }
    *///?}
    //? if <=1.21.11 {
    public static void pushMatrix(@UnknownNullability GuiGraphics c){
        //? if >=1.21.6 {
        c.pose().pushMatrix();
        //?} else
        //c.pose().pushPose();
    }

    public static void popMatrix(@UnknownNullability GuiGraphics c){
        //? if >=1.21.6 {
        c.pose().popMatrix();
        //?} else
        //c.pose().popPose();
    }
    //?}
}
