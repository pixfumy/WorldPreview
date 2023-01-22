package me.voidxwalker.worldpreview.mixin.client.render;

import me.voidxwalker.worldpreview.WorldPreview;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(BufferBuilder.class)
public class BufferBuilderMixin {
    @Redirect(method = "method_6874", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;field_6279:Lnet/minecraft/entity/LivingEntity;"))
    private LivingEntity getCorrectPlayer(MinecraftClient instance) {
        if (WorldPreview.inPreview && instance.field_6279 == null) {
            return WorldPreview.player;
        }
        return instance.field_6279;
    }
}
