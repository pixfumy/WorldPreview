package me.voidxwalker.worldpreview.mixin;

import net.minecraft.realms.RealmsBridge;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(RealmsBridge.class)
/**
 * to get rid of those annoying logs that clog up the console
 */
public class RealmsBridgeMixin {
}
