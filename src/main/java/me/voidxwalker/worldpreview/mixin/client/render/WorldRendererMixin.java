package me.voidxwalker.worldpreview.mixin.client.render;


import me.voidxwalker.worldpreview.PreviewRenderer;
import me.voidxwalker.worldpreview.WorldPreview;
import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.render.BuiltChunkStorage;
import net.minecraft.client.render.WorldRenderer;
//import net.minecraft.client.render.world.AbstractChunkRenderManager;
//import net.minecraft.client.render.world.ChunkRenderFactory;
//import net.minecraft.client.world.BuiltChunk;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ControllablePlayerEntity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin implements PreviewRenderer {
    @Shadow private ClientWorld world;

    @Shadow private boolean needsTerrainUpdate;

    @Shadow @Final private MinecraftClient client;
    @Shadow private int totalEntityCount;
    public boolean previewRenderer;

    public void setPreviewRenderer(){
        this.previewRenderer=true;
    }

    @Redirect(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;world:Lnet/minecraft/client/world/ClientWorld;"))
    private ClientWorld getCorrectWorld(MinecraftClient instance) {
        if (this.previewRenderer) {
            return WorldPreview.clientWorld;
        }
        return instance.world;
    }

    @Redirect(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;world:Lnet/minecraft/client/world/ClientWorld;"))
    private ClientWorld getCorrectWorld2(WorldRenderer instance) {
        if (this.previewRenderer) {
            return WorldPreview.clientWorld;
        }
        return this.world;
    }

    @Redirect(method = "method_1377", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;world:Lnet/minecraft/client/world/ClientWorld;"))
    private ClientWorld getCorrectWorld3(WorldRenderer instance) {
        if (this.previewRenderer) {
            return WorldPreview.clientWorld;
        }
        return this.world;
    }

    @Redirect(method = "method_1377", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;world:Lnet/minecraft/client/world/ClientWorld;"))
    private ClientWorld getCorrectWorld4(MinecraftClient instance) {
        if (this.previewRenderer) {
            return WorldPreview.clientWorld;
        }
        return this.world;
    }

    @Redirect(method = "method_1382", at = @At(value = "FIELD", target = "Lnet/minecraft/client/render/WorldRenderer;world:Lnet/minecraft/client/world/ClientWorld;"))
    private ClientWorld getCorrectWorld5(WorldRenderer instance) {
        if (this.previewRenderer) {
            return WorldPreview.clientWorld;
        }
        return this.world;
    }

    @Redirect(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;field_3805:Lnet/minecraft/entity/player/ControllablePlayerEntity;"))
    private ControllablePlayerEntity getCorrectPlayer(MinecraftClient instance) {
        if (this.previewRenderer) {
            return (ControllablePlayerEntity) WorldPreview.player;
        }
        return this.client.field_3805;
    }

    @Redirect(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;field_6279:Lnet/minecraft/entity/LivingEntity;"))
    private LivingEntity getCorrectPlayer2(MinecraftClient instance) {
        if (this.previewRenderer) {
            return WorldPreview.player;
        }
        return this.client.field_6279;
    }

    @Redirect(method = "method_1377", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;field_6279:Lnet/minecraft/entity/LivingEntity;"))
    private LivingEntity getCorrectPlayer3(MinecraftClient instance) {
        if (this.previewRenderer) {
            return WorldPreview.player;
        }
        return this.client.field_6279;
    }

    @Redirect(method = "method_1382", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;field_6279:Lnet/minecraft/entity/LivingEntity;"))
    private LivingEntity getCorrectPlayer4(MinecraftClient instance) {
        if (this.previewRenderer) {
            return WorldPreview.player;
        }
        return this.client.field_6279;
    }

    @Redirect(method = "method_1368", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;field_6279:Lnet/minecraft/entity/LivingEntity;"))
    private LivingEntity getCorrectPlayer5(MinecraftClient instance) {
        if (this.previewRenderer) {
            return WorldPreview.player;
        }
        return this.client.field_6279;
    }

    @Redirect(method = "method_6887", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;field_6279:Lnet/minecraft/entity/LivingEntity;"))
    private LivingEntity getCorrectPlayer6(MinecraftClient instance) {
        if (this.previewRenderer) {
            return WorldPreview.player;
        }
        return this.client.field_6279;
    }

//    @Redirect(method = "renderEntities", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
//    public Entity worldpreview_getCameraEntity2(MinecraftClient instance){
//        if(instance.getCameraEntity()==null&&this.previewRenderer){
//            return WorldPreview.player;
//        }
//        return  instance.getCameraEntity();
//    }
//
//    @Redirect(method = "setupTerrain", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/render/world/AbstractChunkRenderManager;setViewPos(DDD)V"))
//    private void useCorrectYHeight(AbstractChunkRenderManager instance, double viewX, double viewY, double viewZ) {
//        if (this.previewRenderer && WorldPreview.player != null) {
//            instance.setViewPos(viewX, WorldPreview.player.y, viewZ);
//        } else {
//            instance.setViewPos(viewX, viewY, viewZ);
//        }
//    }
//    @Redirect(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;world:Lnet/minecraft/client/world/ClientWorld;", opcode = Opcodes.GETFIELD))
//    public ClientWorld worldpreview_getCorrectWorld(MinecraftClient instance){
//        if(instance.world==null&&this.previewRenderer){
//            return this.world;
//        }
//        return instance.world;
//
//    }
//    @Redirect(method = "renderSky", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
//    public Entity worldpreview_getCameraEntity3(MinecraftClient instance){
//        if(instance.getCameraEntity()==null&&this.previewRenderer){
//            return WorldPreview.player;
//        }
//        return  instance.getCameraEntity();
//    }
//    @Redirect(method = "renderSky", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;player:Lnet/minecraft/entity/player/ClientPlayerEntity;", opcode = Opcodes.GETFIELD))
//    public ClientPlayerEntity worldpreview_getCorrectPlayer2(MinecraftClient instance){
//        if(instance.player==null&&this.previewRenderer){
//            return WorldPreview.player;
//        }
//        return instance.player ;
//    }
//
//    @Redirect(method = "renderClouds", at = @At(value = "FIELD", target = "Lnet/minecraft/client/MinecraftClient;world:Lnet/minecraft/client/world/ClientWorld;", opcode = Opcodes.GETFIELD))
//    public ClientWorld worldpreview_getCorrectWorld2(MinecraftClient instance){
//        if(instance.world==null&&this.previewRenderer){
//            return this.world;
//        }
//        return instance.world;
//
//    }
//    @Redirect(method = "renderClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
//    public Entity worldpreview_getCameraEntity4(MinecraftClient instance){
//        if(instance.getCameraEntity()==null&&this.previewRenderer){
//            return WorldPreview.player;
//        }
//        return  instance.getCameraEntity();
//    }
//    @Redirect(method = "renderFancyClouds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
//    public Entity worldpreview_getCameraEntity5(MinecraftClient instance){
//        if(instance.getCameraEntity()==null&&this.previewRenderer){
//            return WorldPreview.player;
//        }
//        return  instance.getCameraEntity();
//    }
//    @Redirect(method = "addParticleInternal(IZDDDDDD[I)Lnet/minecraft/client/particle/Particle;", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
//    public Entity worldpreview_getCameraEntity6(MinecraftClient instance){
//        if(instance.getCameraEntity()==null&&this.previewRenderer){
//            return WorldPreview.player;
//        }
//        return  instance.getCameraEntity();
//    }
//    @Redirect(method = "processGlobalEvent", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/MinecraftClient;getCameraEntity()Lnet/minecraft/entity/Entity;"))
//    public Entity worldpreview_getCameraEntity7(MinecraftClient instance){
//        if(instance.getCameraEntity()==null&&this.previewRenderer){
//            return WorldPreview.player;
//        }
//        return  instance.getCameraEntity();
//    }
//    @Redirect(method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;)V",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/render/GameRenderer;enableLightmap()V"))
//    public void stopLightMap1(GameRenderer instance){
//        if(this.previewRenderer){
//            return;
//        }
//        instance.enableLightmap();
//    }
//    @Redirect(method = "renderLayer(Lnet/minecraft/client/render/RenderLayer;)V",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/render/GameRenderer;disableLightmap()V"))
//    public void stopLightMap2(GameRenderer instance){
//        if(this.previewRenderer){
//            return;
//        }
//        instance.disableLightmap();
//    }
//    @Redirect(method = "renderEntities",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/render/GameRenderer;enableLightmap()V"))
//    public void stopLightMap3(GameRenderer instance){
//        if(this.previewRenderer){
//            return;
//        }
//        instance.enableLightmap();
//    }
//    @Redirect(method = "renderEntities",at = @At(value = "INVOKE",target = "Lnet/minecraft/client/render/GameRenderer;disableLightmap()V"))
//    public void stopLightMap4(GameRenderer instance){
//        if(this.previewRenderer){
//            return;
//        }
//        instance.disableLightmap();
//    }
}
