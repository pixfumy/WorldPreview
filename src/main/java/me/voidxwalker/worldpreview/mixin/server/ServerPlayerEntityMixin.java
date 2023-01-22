package me.voidxwalker.worldpreview.mixin.server;

//import net.minecraft.client.gui.screen.LevelLoadingScreen;

import me.voidxwalker.worldpreview.WorldPreview;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin  {
    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;method_3708(II)I"))
    private int setSpawnPos(ServerWorld instance, int i, int j) {
        return WorldPreview.existingWorld ? instance.method_3708(i, j) : WorldPreview.spawnPos.y ;
    }
}