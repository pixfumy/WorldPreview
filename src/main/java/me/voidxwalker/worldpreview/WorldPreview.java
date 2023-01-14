package me.voidxwalker.worldpreview;

import net.fabricmc.api.ClientModInitializer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.options.KeyBinding;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class WorldPreview implements ClientModInitializer {
   public static World world;
   public static ClientPlayerEntity player;
   public static ClientWorld clientWorld;
   public static boolean inPreview;
   public static BlockPos spawnPos;
   public static int kill=0;
   public static WorldRenderer worldRenderer;
   public static boolean existingWorld;
   public static boolean loadedSpawn;
   public static boolean canReload;
   public static KeyBinding resetKey;
   public static KeyBinding freezeKey;
   public static boolean freezePreview;
   public static final Object lock= new Object();
   public static Logger LOGGER = LogManager.getLogger();
   public static void log(Level level, String message) {
      LOGGER.log(level, message);
   }
   public static Identifier WIDGETS_LOCATION = new Identifier("textures/gui/widgets.png");
   @Override
   public void onInitializeClient() {
      init();
   }

   public static void init() {
      WorldPreview.freezePreview = false;
      WorldPreview.loadedSpawn = false;
      WorldPreview.canReload = false;
      KeyBinding.unpressAll();
   }
}
