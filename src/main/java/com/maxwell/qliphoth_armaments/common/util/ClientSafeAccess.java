package com.maxwell.qliphoth_armaments.common.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.loading.FMLEnvironment;

/**
 * Utility class for safe access to client-side only methods and classes.
 */
public class ClientSafeAccess {

    public static boolean isClient() {
        return FMLEnvironment.dist == Dist.CLIENT;
    }

    public static Player getClientPlayer() {
        if (isClient()) {
            return Minecraft.getInstance().player;
        }
        return null;
    }

    public static boolean hasShiftDown() {
        if (isClient()) {
            return Screen.hasShiftDown();
        }
        return false;
    }

    public static boolean hasControlDown() {
        if (isClient()) {
            return Screen.hasControlDown();
        }
        return false;
    }
}
