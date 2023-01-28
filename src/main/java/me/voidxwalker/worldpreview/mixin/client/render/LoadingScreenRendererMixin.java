package me.voidxwalker.worldpreview.mixin.client.render;

import com.mojang.blaze3d.platform.GLX;
import me.voidxwalker.worldpreview.PreviewRenderer;
import me.voidxwalker.worldpreview.WorldPreview;

import me.voidxwalker.worldpreview.mixin.access.MinecraftClientMixin;
import me.voidxwalker.worldpreview.mixin.access.WorldRendererMixin;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.render.*;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.util.GlAllocationUtils;
import net.minecraft.client.util.Window;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.BossBar;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;
import org.lwjgl.util.glu.Project;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.FloatBuffer;
import java.util.ArrayList;

@Mixin(LoadingScreenRenderer.class)
public abstract class LoadingScreenRendererMixin {
    @Shadow private MinecraftClient client;
    
    private long lastRenderTime;
    private long lastInputPollTime;

    @Shadow private String field_1028;

    @Shadow private Window window;
    @Shadow private String title;
    private int frameCount = 0;
    float fogRed;
    float fogGreen;
    float fogBlue;
    private float prevFogColor;
    private float fogColor;
    private float field_1826;
    private float field_1827;

    private long nanoTime = 0;

    private ButtonWidget resetButton;
    private ArrayList<ButtonWidget> buttons = new ArrayList<ButtonWidget>();
    private float lastSkyDarkness;
    private float skyDarkness;
    FloatBuffer fogColorBuffer = GlAllocationUtils.allocateFloatBuffer(16);

    /**
     * @author Pixfumy
     * @reason This method is absolutely unrecognizable with all the changes. Any other mod targeting this method should know about this.
     * TODO: This implementation (custom WorldRenderer and ClientWorld) is causing GL issues and no preview is being shown. We need to rethink this.
     */
    @Overwrite
    public void setProgressPercentage(int percentage) {
        if(WorldPreview.worldRenderer==null){
            WorldPreview.worldRenderer=new WorldRenderer(MinecraftClient.getInstance());
            ((PreviewRenderer)WorldPreview.worldRenderer).setPreviewRenderer();
        }
        long l = MinecraftClient.getTime();
        Window window = this.window;
        int width = window.getWidth();
        int height = window.getHeight();
        int mouseX = Mouse.getX() * width / this.client.width;
        int mouseY = height - Mouse.getY() * height / this.client.height - 1;
        if (!WorldPreview.inPreview || Display.wasResized()) {
            WorldPreview.inPreview = true;
            worldpreview_setButtons(width, height);
        }
        if (((WorldRendererMixin) WorldPreview.worldRenderer).getWorld() != null) {
            int buttonHeight = 20;
            boolean resetHovered = this.resetButton != null && mouseX >= this.resetButton.x && mouseY >= this.resetButton.y && mouseX < this.resetButton.x + this.resetButton.getWidth() && mouseY < this.resetButton.y + buttonHeight;
            if (resetHovered && Mouse.isButtonDown(0)) {
                WorldPreview.kill = 1;
                return;
            }
        }
        if (WorldPreview.world != null && WorldPreview.clientWorld != null && WorldPreview.player != null && WorldPreview.inPreview && WorldPreview.loadedSpawn) {
            // render the world
            if (((WorldRendererMixin) WorldPreview.worldRenderer).getWorld() == null) {
                WorldPreview.worldRenderer.setWorld(WorldPreview.clientWorld);
                WorldPreview.log("Starting preview.");
                frameCount = 0;
                WorldPreview.canFreeze = true;
            }
            if (((WorldRendererMixin) WorldPreview.worldRenderer).getWorld() != null) {
                float h = WorldPreview.clientWorld.method_3780(MathHelper.floor(WorldPreview.player.x), MathHelper.floor(WorldPreview.player.y), MathHelper.floor(WorldPreview.player.z));
                float x = (float) this.client.options.viewDistance / 32.0F;
                float y = h * (1.0F - x) + x;
                this.fogColor += (y - this.fogColor) * 0.1F;
                this.lastSkyDarkness = this.skyDarkness;
                if (BossBar.darkenSky) {
                    this.skyDarkness += 0.05F;
                    if (this.skyDarkness > 1.0F) {
                        this.skyDarkness = 1.0F;
                    }
                    BossBar.darkenSky = false;
                } else if (this.skyDarkness > 0.0F) {
                    this.skyDarkness -= 0.0125F;
                }
                int p = this.client.options.maxFramerate;
                int q = Math.min(10, p);
                q = Math.max(q, 60);
                long r = System.nanoTime() - nanoTime;
                long s = Math.max((long) (1000000000 / q / 4) - r, 0L);
                if (l - this.lastRenderTime >= 1000L / WorldPreview.loadingScreenFPS) {
                    GL11.glPushMatrix();
                    GL11.glClear(16640);
                    GL11.glEnable(3553);
                    this.worldpreview_renderWorld(((MinecraftClientMixin) this.client).getTicker().tickDelta, (long)(1000000000 / this.client.options.maxFramerate));
//                    this.worldpreview_renderCenteredString(this.client.textRenderer, I18n.translate("menu.game"), width / 2, 40, 16777215);
//                    this.worldpreview_renderMenuButtons(width, height, mouseX, mouseY);
                }
            }
        } else { // usual loading screen
            if (l - this.lastRenderTime >= 1000L / WorldPreview.loadingScreenFPS) {
                GL11.glClear(256);
                GL11.glMatrixMode(5889);
                GL11.glLoadIdentity();
                GL11.glOrtho(0.0, window.getScaledWidth(), window.getScaledHeight(), 0.0, 100.0, 300.0);
                GL11.glMatrixMode(5888);
                GL11.glLoadIdentity();
                GL11.glTranslatef(0.0f, 0.0f, -200.0f);
                GL11.glClear(16640);
                Tessellator tessellator = Tessellator.INSTANCE;
                this.client.getTextureManager().bindTexture(DrawableHelper.OPTIONS_BACKGROUND_TEXTURE);
                float f = 32.0F;
                tessellator.begin();
                tessellator.color(0x404040);
                tessellator.vertex(0.0, height, 0.0, 0.0, (float)height / f);
                tessellator.vertex(width, height, 0.0, (float)width / f, (float)height / f);
                tessellator.vertex(width, 0.0, 0.0, (float)width / f, 0.0);
                tessellator.vertex(0.0, 0.0, 0.0, 0.0, 0.0);
                tessellator.end();
                if (percentage >= 0) {
                    int n4 = 100;
                    int n5 = 2;
                    int n6 = width / 2 - n4 / 2;
                    int n7 = height / 2 + 16;
                    GL11.glDisable(3553);
                    tessellator.begin();
                    tessellator.color(0x808080);
                    tessellator.vertex(n6, n7, 0.0);
                    tessellator.vertex(n6, n7 + n5, 0.0);
                    tessellator.vertex(n6 + n4, n7 + n5, 0.0);
                    tessellator.vertex(n6 + n4, n7, 0.0);
                    tessellator.color(0x80FF80);
                    tessellator.vertex(n6, n7, 0.0);
                    tessellator.vertex(n6, n7 + n5, 0.0);
                    tessellator.vertex(n6 + percentage, n7 + n5, 0.0);
                    tessellator.vertex(n6 + percentage, n7, 0.0);
                    tessellator.end();
                    GL11.glEnable(3553);
                }
            }
        }
        if (l - this.lastRenderTime >= 1000L / WorldPreview.loadingScreenFPS) {
            this.lastRenderTime = l;
            GL11.glEnable(3042);
            GLX.glBlendFuncSeparate(770, 771, 1, 0);
            this.client.textRenderer.method_956(this.title, (width - this.client.textRenderer.getStringWidth(this.title)) / 2, height / 2 - 4 - 16, 0xFFFFFF);
            this.client.textRenderer.method_956(this.field_1028, (width - this.client.textRenderer.getStringWidth(this.field_1028)) / 2, height / 2 - 4 + 8, 0xFFFFFF);
            this.client.method_6648();
        } else if (l - this.lastInputPollTime >= 1000L / WorldPreview.loadingScreenPollingRate){
            this.lastInputPollTime = l;
            Display.processMessages(); //updateDisplay() calls Display.processMessages() anyway, so no need to run both in one render.
        }
        this.nanoTime = System.nanoTime();
        try {
            Thread.yield();
        } catch (Exception var15) {
        }
    }

    private void worldpreview_setButtons(int width, int height) {
        buttons.add(this.resetButton = new ButtonWidget(1, width / 2 - 100, height / 4 + 120 - 16, I18n.translate("menu.returnToMenu")));
        buttons.add(new ButtonWidget(4, width / 2 - 100, height / 4 + 24 - 16, I18n.translate("menu.returnToGame")));
        buttons.add(new ButtonWidget(0, width / 2 - 100, height / 4 + 96 - 16, 98, 20, I18n.translate("menu.options")));
        buttons.add(new ButtonWidget(7, width / 2 + 2, height / 4 + 96 - 16, 98, 20, I18n.translate("menu.shareToLan")));
        buttons.add(new ButtonWidget(5, width / 2 - 100, height / 4 + 48 - 16, 98, 20, I18n.translate("gui.achievements")));
        buttons.add(new ButtonWidget(6, width / 2 + 2, height / 4 + 48 - 16, 98, 20, I18n.translate("gui.stats")));
    }

    public void worldpreview_renderWorld(float tickDelta, long limitTime) {
        GL11.glEnable(2884);
        GL11.glEnable(2929);
        GL11.glEnable(3008);
        GL11.glAlphaFunc(516, 0.5f);
        WorldRenderer worldRenderer =  WorldPreview.worldRenderer;
        ( (PreviewRenderer)worldRenderer).setPreviewRenderer();
        LivingEntity livingEntity = WorldPreview.player;
        double d = livingEntity.prevTickX + (livingEntity.x - livingEntity.prevTickX) * (double)tickDelta;
        double d2 = livingEntity.prevTickY + (livingEntity.y - livingEntity.prevTickY) * (double)tickDelta;
        double d3 = livingEntity.prevTickZ + (livingEntity.z - livingEntity.prevTickZ) * (double)tickDelta;
        for (int i = 0; i < 2; ++i) {
            this.client.profiler.swap("clear");
            GL11.glViewport(0, 0, this.client.width, this.client.height);
            this.worldpreview_updateFog(tickDelta);
            GL11.glClear(16640);
            GL11.glEnable(2884);
            this.client.profiler.swap("camera");
            this.worldpreview_setupCamera(tickDelta, i);
            Camera.update(WorldPreview.player, this.client.options.perspective == 2);
            this.client.profiler.swap("frustrum");
            Frustum.getInstance();
            if (this.client.options.viewDistance >= 4) {
                this.worldpreview_renderFog(-1, tickDelta);
                this.client.profiler.swap("sky");
                worldRenderer.renderSky(tickDelta);
            }
            GL11.glEnable(2912);
            this.worldpreview_renderFog(1, tickDelta);
            if (this.client.options.ao != 0) {
                GL11.glShadeModel(7425);
            }
            this.client.profiler.swap("culling");
            CullingCameraView cullingCameraView = new CullingCameraView();
            cullingCameraView.setPos(d, d2, d3);
            worldRenderer.method_1373(cullingCameraView, tickDelta);
            if (i == 0) {
                long l;
                this.client.profiler.swap("updatechunks");
                while (!worldRenderer.method_1375(livingEntity, false) && limitTime != 0L && (l = limitTime - System.nanoTime()) >= 0L && l <= 1000000000L) {
                }
            }
            if (livingEntity.y < 128.0) {
                this.worldpreview_renderClouds(worldRenderer, tickDelta);
            }
            this.client.profiler.swap("prepareterrain");
            this.worldpreview_renderFog(0, tickDelta);
            GL11.glEnable(2912);
            this.client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
            DiffuseLighting.disable();
            this.client.profiler.swap("terrain");
            GL11.glMatrixMode(5888);
            GL11.glPushMatrix();
            worldRenderer.method_1374(livingEntity, 0, tickDelta);
            GL11.glShadeModel(7424);
            GL11.glAlphaFunc(516, 0.1f);
            GL11.glMatrixMode(5888);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            DiffuseLighting.enableNormally();
            this.client.profiler.swap("entities");
            worldRenderer.method_6887(livingEntity, cullingCameraView, tickDelta);
            DiffuseLighting.disable();
            this.worldpreview_afterWorldRender(tickDelta);
            GL11.glMatrixMode(5888);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            if (this.client.result != null && livingEntity.isSubmergedIn(Material.WATER) && livingEntity instanceof PlayerEntity && !this.client.options.hudHidden) {
                PlayerEntity playerEntity = (PlayerEntity)livingEntity;
                GL11.glDisable(3008);
                this.client.profiler.swap("outline");
                worldRenderer.drawBlockOutline(playerEntity, this.client.result, 0, tickDelta);
                GL11.glEnable(3008);
            }
            GL11.glMatrixMode(5888);
            GL11.glPopMatrix();
            if (livingEntity instanceof PlayerEntity && !this.client.options.hudHidden && this.client.result != null && !livingEntity.isSubmergedIn(Material.WATER)) {
                PlayerEntity playerEntity = (PlayerEntity)livingEntity;
                GL11.glDisable(3008);
                this.client.profiler.swap("outline");
                worldRenderer.drawBlockOutline(playerEntity, this.client.result, 0, tickDelta);
                GL11.glEnable(3008);
            }
            this.client.profiler.swap("destroyProgress");
            GL11.glEnable(3042);
            GLX.glBlendFuncSeparate(770, 1, 1, 0);
            worldRenderer.method_1372(Tessellator.INSTANCE, (PlayerEntity)livingEntity, tickDelta);
            GL11.glDisable(3042);
            GL11.glDepthMask(false);
            GL11.glEnable(2884);
            GLX.glBlendFuncSeparate(770, 771, 1, 0);
            GL11.glAlphaFunc(516, 0.1f);
            this.worldpreview_renderFog(0, tickDelta);
            GL11.glEnable(3042);
            GL11.glDepthMask(false);
            this.client.getTextureManager().bindTexture(SpriteAtlasTexture.BLOCK_ATLAS_TEX);
            if (this.client.options.fancyGraphics) {
                this.client.profiler.swap("water");
                if (this.client.options.ao != 0) {
                    GL11.glShadeModel(7425);
                }
                GL11.glEnable(3042);
                GLX.glBlendFuncSeparate(770, 771, 1, 0);
                worldRenderer.method_1374(livingEntity, 1, tickDelta);
                GL11.glDisable(3042);
                GL11.glShadeModel(7424);
            } else {
                this.client.profiler.swap("water");
                worldRenderer.method_1374(livingEntity, 1, tickDelta);
            }
            GL11.glDepthMask(true);
            GL11.glEnable(2884);
            GL11.glDisable(3042);
            GL11.glDisable(2912);
            if (livingEntity.y >= 128.0) {
                this.client.profiler.swap("aboveClouds");
                this.worldpreview_renderClouds(worldRenderer, tickDelta);
            }
            GL11.glClear(256);
            if (this.client.options.anaglyph3d) continue;
            this.client.profiler.pop();
            return;
        }
        GL11.glColorMask(true, true, true, false);
        this.client.profiler.pop();
    }

    private void worldpreview_updateFog(float tickDelta) {
        ClientWorld var2 = WorldPreview.clientWorld;
        LivingEntity var3 = WorldPreview.player;
        float var4 = 0.25F + 0.75F * (float)this.client.options.viewDistance / 16.0F;
        var4 = 1.0F - (float)Math.pow((double)var4, 0.25D);
        Vec3d var5 = var2.method_3631(WorldPreview.player, tickDelta);
        float var6 = (float)var5.x;
        float var7 = (float)var5.y;
        float var8 = (float)var5.z;
        Vec3d var9 = var2.getFogColor(tickDelta);
        this.fogRed = (float)var9.x;
        this.fogGreen = (float)var9.y;
        this.fogBlue = (float)var9.z;
        float var11;
        if (this.client.options.viewDistance >= 4) {
            Vec3d var10 = MathHelper.sin(var2.getSkyAngleRadians(tickDelta)) > 0.0F ? Vec3d.of(-1.0D, 0.0D, 0.0D) : Vec3d.of(1.0D, 0.0D, 0.0D);
            var11 = (float)var3.method_6146(tickDelta).dotProduct(var10);
            if (var11 < 0.0F) {
                var11 = 0.0F;
            }
            if (var11 > 0.0F) {
                float[] var12 = var2.dimension.getBackgroundColor(var2.getSkyAngle(tickDelta), tickDelta);
                if (var12 != null) {
                    var11 *= var12[3];
                    this.fogRed = this.fogRed * (1.0F - var11) + var12[0] * var11;
                    this.fogGreen = this.fogGreen * (1.0F - var11) + var12[1] * var11;
                    this.fogBlue = this.fogBlue * (1.0F - var11) + var12[2] * var11;
                }
            }
        }
        this.fogRed += (var6 - this.fogRed) * var4;
        this.fogGreen += (var7 - this.fogGreen) * var4;
        this.fogBlue += (var8 - this.fogBlue) * var4;
        float var19 = var2.getRainGradient(tickDelta);
        float var20;
        if (var19 > 0.0F) {
            var11 = 1.0F - var19 * 0.5F;
            var20 = 1.0F - var19 * 0.4F;
            this.fogRed *= var11;
            this.fogGreen *= var11;
            this.fogBlue *= var20;
        }
        var11 = var2.getThunderGradient(tickDelta);
        if (var11 > 0.0F) {
            var20 = 1.0F - var11 * 0.5F;
            this.fogRed *= var20;
            this.fogGreen *= var20;
            this.fogBlue *= var20;
        }
        Block var21 = Camera.method_805(WorldPreview.clientWorld, var3, tickDelta);
        float var22;
        if (var21.getMaterial() == Material.WATER) {
            var22 = (float)EnchantmentHelper.getKnockback(var3) * 0.2F;
            this.fogRed = 0.02F + var22;
            this.fogGreen = 0.02F + var22;
            this.fogBlue = 0.2F + var22;
        } else if (var21.getMaterial() == Material.LAVA) {
            this.fogRed = 0.6F;
            this.fogGreen = 0.1F;
            this.fogBlue = 0.0F;
        }
        var22 = this.prevFogColor + (this.fogColor - this.prevFogColor) * tickDelta;
        this.fogRed *= var22;
        this.fogGreen *= var22;
        this.fogBlue *= var22;
        double var14 = (var3.prevTickY + (var3.y - var3.prevTickY) * (double)tickDelta) * var2.dimension.method_3994();
        if (var3.hasStatusEffect(StatusEffect.BLINDNESS)) {
            int var16 = var3.getEffectInstance(StatusEffect.BLINDNESS).getDuration();
            if (var16 < 20) {
                var14 *= (double)(1.0F - (float)var16 / 20.0F);
            } else {
                var14 = 0.0D;
            }
        }
        if (var14 < 1.0D) {
            if (var14 < 0.0D) {
                var14 = 0.0D;
            }
            var14 *= var14;
            this.fogRed = (float)((double)this.fogRed * var14);
            this.fogGreen = (float)((double)this.fogGreen * var14);
            this.fogBlue = (float)((double)this.fogBlue * var14);
        }
        if (this.skyDarkness > 0.0F) {
            float var23 = this.lastSkyDarkness + (this.skyDarkness - this.lastSkyDarkness) * tickDelta;
            this.fogRed = this.fogRed * (1.0F - var23) + this.fogRed * 0.7F * var23;
            this.fogGreen = this.fogGreen * (1.0F - var23) + this.fogGreen * 0.6F * var23;
            this.fogBlue = this.fogBlue * (1.0F - var23) + this.fogBlue * 0.6F * var23;
        }
        GL11.glClearColor(this.fogRed, this.fogGreen, this.fogBlue, 0.0F);
    }

    private void worldpreview_setupCamera(float tickDelta, int anaglyphFilter) {
        float viewDistance = (float)(this.client.options.viewDistance * 16);
        GL11.glMatrixMode(5889);
        GL11.glLoadIdentity();
        float var3 = 0.07F;
        if (this.client.options.anaglyph3d) {
            GL11.glTranslatef((float)(-(anaglyphFilter * 2 - 1)) * var3, 0.0F, 0.0F);
        }

        Project.gluPerspective(this.client.options.fov, (float)this.client.width / (float)this.client.height, 0.05F, viewDistance * 2.0F);
        float var4;

        GL11.glMatrixMode(5888);
        GL11.glLoadIdentity();
        if (this.client.options.anaglyph3d) {
            GL11.glTranslatef((float)(anaglyphFilter * 2 - 1) * 0.1F, 0.0F, 0.0F);
        }

        this.worldpreview_transformCamera(tickDelta);
    }

    private void worldpreview_renderClouds(WorldRenderer worldRenderer, float f) {
        if (this.client.options.method_876()) {
            this.client.profiler.swap("clouds");
            GL11.glPushMatrix();
            this.worldpreview_renderFog(0, f);
            GL11.glEnable(2912);
            worldRenderer.method_1377(f);
            GL11.glDisable(2912);
            this.worldpreview_renderFog(1, f);
            GL11.glPopMatrix();
        }
    }

    private void worldpreview_renderFog(int i, float tickDelta) {
        LivingEntity var3 = WorldPreview.player;
        if (i == 999) {
            GL11.glFog(2918, this.worldpreview_updateFogColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
            GL11.glFogi(2917, 9729);
            GL11.glFogf(2915, 0.0F);
            GL11.glFogf(2916, 8.0F);
            if (GLContext.getCapabilities().GL_NV_fog_distance) {
                GL11.glFogi(34138, 34139);
            }

            GL11.glFogf(2915, 0.0F);
        } else {
            GL11.glFog(2918, this.worldpreview_updateFogColorBuffer(this.fogRed, this.fogGreen, this.fogBlue, 1.0F));
            GL11.glNormal3f(0.0F, -1.0F, 0.0F);
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            Block var5 = Camera.method_805(WorldPreview.clientWorld, var3, tickDelta);
            float var6;
            if (var5.getMaterial() == Material.WATER) {
                GL11.glFogi(2917, 2048);
                GL11.glFogf(2914, 0.1F - (float)EnchantmentHelper.getKnockback(var3) * 0.03F);
            } else if (var5.getMaterial() == Material.LAVA) {
                GL11.glFogi(2917, 2048);
                GL11.glFogf(2914, 2.0F);
            } else {
                var6 = this.client.options.viewDistance * 16;
                GL11.glFogi(2917, 9729);
                if (i < 0) {
                    GL11.glFogf(2915, 0.0F);
                    GL11.glFogf(2916, var6);
                } else {
                    GL11.glFogf(2915, var6 * 0.75F);
                    GL11.glFogf(2916, var6);
                }

                if (GLContext.getCapabilities().GL_NV_fog_distance) {
                    GL11.glFogi(34138, 34139);
                }
            }

            GL11.glEnable(2903);
            GL11.glColorMaterial(1028, 4608);
        }
    }

    public void worldpreview_afterWorldRender(double tickDelta) {
        GLX.gl13ActiveTexture(GLX.lightmapTextureUnit);
        GL11.glDisable(3553);
        GLX.gl13ActiveTexture(GLX.textureUnit);
    }

    private void worldpreview_transformCamera(float tickDelta) {
        LivingEntity var2 = WorldPreview.player;
        float var3 = var2.heightOffset - 1.62F;
        double var4 = var2.prevX + (var2.x - var2.prevX) * (double)tickDelta;
        double var6 = var2.prevY + (var2.y - var2.prevY) * (double)tickDelta - (double)var3;
        double var8 = var2.prevZ + (var2.z - var2.prevZ) * (double)tickDelta;
        GL11.glRotatef(0, 0.0F, 0.0F, 1.0F);
        if (this.client.options.perspective > 0) {
            double var27 = 0;
            float var13;
            float var28;
            if (this.client.options.field_955) {
                var28 = 0;
                var13 = 0;
                GL11.glTranslatef(0.0F, 0.0F, (float)(-var27));
                GL11.glRotatef(var13, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(var28, 0.0F, 1.0F, 0.0F);
            } else {
                var28 = var2.yaw;
                var13 = var2.pitch;
                if (this.client.options.perspective == 2) {
                    var13 += 180.0F;
                }

                double var14 = (double)(-MathHelper.sin(var28 / 180.0F * 3.1415927F) * MathHelper.cos(var13 / 180.0F * 3.1415927F)) * var27;
                double var16 = (double)(MathHelper.cos(var28 / 180.0F * 3.1415927F) * MathHelper.cos(var13 / 180.0F * 3.1415927F)) * var27;
                double var18 = (double)(-MathHelper.sin(var13 / 180.0F * 3.1415927F)) * var27;

                if (this.client.options.perspective == 2) {
                    GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
                }

                GL11.glRotatef(var2.pitch - var13, 1.0F, 0.0F, 0.0F);
                GL11.glRotatef(var2.yaw - var28, 0.0F, 1.0F, 0.0F);
                GL11.glTranslatef(0.0F, 0.0F, (float)(-var27));
                GL11.glRotatef(var28 - var2.yaw, 0.0F, 1.0F, 0.0F);
                GL11.glRotatef(var13 - var2.pitch, 1.0F, 0.0F, 0.0F);
            }
        } else {
            GL11.glTranslatef(0.0F, 0.0F, -0.1F);
        }

        if (!this.client.options.field_955) {
            GL11.glRotatef(var2.prevPitch + (var2.pitch - var2.prevPitch) * tickDelta, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(var2.prevYaw + (var2.yaw - var2.prevYaw) * tickDelta + 180.0F, 0.0F, 1.0F, 0.0F);
        }

        GL11.glTranslatef(0.0F, var3, 0.0F);
        var4 = var2.prevX + (var2.x - var2.prevX) * (double)tickDelta;
        var6 = var2.prevY + (var2.y - var2.prevY) * (double)tickDelta - (double)var3;
        var8 = var2.prevZ + (var2.z - var2.prevZ) * (double)tickDelta;
    }

    private FloatBuffer worldpreview_updateFogColorBuffer(float red, float green, float blue, float alpha) {
        this.fogColorBuffer.clear();
        this.fogColorBuffer.put(red).put(green).put(blue).put(alpha);
        this.fogColorBuffer.flip();
        return this.fogColorBuffer;
    }

//    private FloatBuffer worldpreview_updateFogColorBuffer(float red, float green, float blue, float alpha) {
//        this.fogColorBuffer.clear();
//        this.fogColorBuffer.put(red).put(green).put(blue).put(alpha);
//        this.fogColorBuffer.flip();
//        return this.fogColorBuffer;
//    }
//
//    private void worldpreview_renderMenuButtons(int width, int height, int mouseX, int mouseY) {
//        TextRenderer textRenderer = this.client.textRenderer;
//        for (ButtonWidget button: this.buttons) {
//            int buttonWidth = button.getWidth();
//            int buttonHeight = 20;
//            this.client.getTextureManager().bindTexture(new Identifier("textures/gui/widgets.png"));
//            GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//            boolean hovered = mouseX >= button.x && mouseY >= button.y && mouseX < button.x + buttonWidth && mouseY < button.y + buttonHeight;
//            int i = hovered ? 2 : 1;
//            GlStateManager.enableBlend();
//            GlStateManager.blendFuncSeparate(770, 771, 1, 0);
//            GlStateManager.blendFunc(770, 771);
//            this.worldpreview_drawTexture(button.x, button.y, 0, 46 + i * 20, buttonWidth / 2, buttonHeight);
//            this.worldpreview_drawTexture(button.x + buttonWidth / 2, button.y, 200 - buttonWidth / 2, 46 + i * 20, buttonWidth / 2, buttonHeight);
//            int j = hovered ? 16777120 : 14737632;
//            this.worldpreview_renderCenteredString(textRenderer, button.message, button.x + buttonWidth / 2, button.y + (buttonHeight - 8) / 2, j);
//        }
//    }
//
//    private void worldpreview_renderCenteredString(TextRenderer textRenderer, String text, int centerX, int y, int color) {
//        textRenderer.drawWithShadow(text, (float)(centerX - textRenderer.getStringWidth(text) / 2), (float)y, color);
//    }
//
//    private void worldpreview_drawTexture(int x, int y, int u, int v, int width, int height) {
//        float f = 0.00390625F;
//        float g = 0.00390625F;
//        Tessellator tessellator = Tessellator.getInstance();
//        BufferBuilder bufferBuilder = tessellator.getBuffer();
//        bufferBuilder.begin(7, VertexFormats.POSITION_TEXTURE);
//        bufferBuilder.vertex((double)(x + 0), (double)(y + height), (double)0).texture((double)((float)(u + 0) * f), (double)((float)(v + height) * g)).next();
//        bufferBuilder.vertex((double)(x + width), (double)(y + height), (double)0).texture((double)((float)(u + width) * f), (double)((float)(v + height) * g)).next();
//        bufferBuilder.vertex((double)(x + width), (double)(y + 0), (double)0).texture((double)((float)(u + width) * f), (double)((float)(v + 0) * g)).next();
//        bufferBuilder.vertex((double)(x + 0), (double)(y + 0), (double)0).texture((double)((float)(u + 0) * f), (double)((float)(v + 0) * g)).next();
//        tessellator.draw();
//    }
}
