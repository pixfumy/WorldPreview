package me.voidxwalker.worldpreview.mixin.server;

//import net.minecraft.client.gui.screen.LevelLoadingScreen;

import me.voidxwalker.worldpreview.WorldPreview;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(ServerPlayerEntity.class)
public abstract class ServerPlayerEntityMixin extends Entity {

    /**
     * All classes that extend Entity <b>must</b> have a constructor
     * that takes in one, and only one {@link World} parameter.
     * This is due to the fact that entity constructors are called reflectively
     *
     * @param world
     */
    public ServerPlayerEntityMixin(World world) {
        super(world);
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/world/ServerWorld;method_3708(II)I"))
    private int setSpawnPosY(ServerWorld instance, int i, int j) {
        if (WorldPreview.existingWorld) {
            return instance.method_3708(i, j);
        } else {
            return WorldPreview.spawnPos.y;
        }
    }

    @Redirect(method = "<init>", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/player/ServerPlayerEntity;refreshPositionAndAngles(DDDFF)V"))
    private void setSpawnPosXandZ(ServerPlayerEntity instance, double x, double y, double z, float yaw, float pitch) {
        if (WorldPreview.existingWorld) {
            instance.refreshPositionAndAngles(x, y, z, yaw, pitch);
        } else {
            instance.refreshPositionAndAngles(WorldPreview.spawnPos.x + 0.5, WorldPreview.spawnPos.y, WorldPreview.spawnPos.z + 0.5, 0.0F, 0.0F);
        }
    }
}