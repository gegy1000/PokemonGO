package net.gegy1000.pokemon.client.event;

import net.gegy1000.pokemon.client.entity.PokemonEntity;
import net.gegy1000.pokemon.client.gui.LoginGUI;
import net.gegy1000.pokemon.client.gui.view.PokemonViewGUI;
import net.gegy1000.pokemon.client.key.PokemonKeyBinds;
import net.gegy1000.pokemon.client.renderer.PokemonObjectRenderer;
import net.gegy1000.pokemon.client.renderer.RenderHandler;
import net.gegy1000.pokemon.client.util.PokemonHandler;
import net.gegy1000.pokemon.client.util.PokemonMapHandler;
import net.gegy1000.pokemon.server.world.gen.WorldTypePokemonEarth;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClientEventHandler {
    private static final Minecraft MC = Minecraft.getMinecraft();

    @SubscribeEvent
    public void onKeyPress(InputEvent.KeyInputEvent event) {
        if (MC.theWorld.getWorldType() instanceof WorldTypePokemonEarth) {
            if (PokemonKeyBinds.KEY_POKEMON_VIEW.isPressed()) {
                MC.displayGuiScreen(new PokemonViewGUI());
            } else if (PokemonKeyBinds.KEY_LOGIN.isPressed()) {
                MC.displayGuiScreen(new LoginGUI());
            }
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        PokemonHandler.update(MC.thePlayer);
    }

    @SubscribeEvent
    public void onRightClickEmpty(PlayerInteractEvent.RightClickEmpty event) {
        if (event.getEntityPlayer() == MC.thePlayer) {
            this.interactMapObjects();
        }
    }

    @SubscribeEvent
    public void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getEntityPlayer() == MC.thePlayer) {
            this.interactMapObjects();
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getEntityPlayer() == MC.thePlayer) {
            this.interactMapObjects();
        }
    }

    private void interactMapObjects() {
        if (PokemonHandler.API != null && MC.currentScreen == null) {
            try {
                EntityPlayerSP player = MC.thePlayer;

                Map<PokemonEntity, AxisAlignedBB> entityBounds = new HashMap<>();
                for (PokemonEntity entity : PokemonMapHandler.getEntities()) {
                    entityBounds.put(entity, entity.getBounds());
                }

                PokemonEntity entity = this.getInteract(player, entityBounds);
                if (entity != null) {
                    entity.onInteract();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private <T> T getInteract(EntityPlayer player, Map<T, AxisAlignedBB> bounds) {
        Vec3d eyePosition = player.getPositionEyes(1.0F);
        double reach = 8.0;
        Vec3d look = player.getLook(1.0F);
        Vec3d farLook = eyePosition.addVector(look.xCoord * reach, look.yCoord * reach, look.zCoord * reach);
        double closestDistance = reach;
        T interact = null;
        for (Map.Entry<T, AxisAlignedBB> entry : bounds.entrySet()) {
            AxisAlignedBB bound = entry.getValue();
            RayTraceResult intercept = bound.calculateIntercept(eyePosition, farLook);
            if (bound.isVecInside(eyePosition)) {
                if (closestDistance >= 0.0) {
                    interact = entry.getKey();
                    closestDistance = 0.0;
                }
            } else if (intercept != null) {
                double distance = eyePosition.distanceTo(intercept.hitVec);
                if (distance < closestDistance || closestDistance == 0.0) {
                    interact = entry.getKey();
                    closestDistance = distance;
                }
            }
        }
        return interact;
    }

    @SubscribeEvent
    public void onWorldRender(RenderWorldLastEvent event) {
        if (PokemonHandler.API != null && MC.theWorld.getWorldType() instanceof WorldTypePokemonEarth) {
            this.renderPokemonFeatures(event.getPartialTicks());
        }
    }

    private void renderPokemonFeatures(float partialTicks) {
        try {
            EntityPlayerSP player = MC.thePlayer;
            double viewX = player.prevPosX + (player.posX - player.prevPosX) * partialTicks;
            double viewY = player.prevPosY + (player.posY - player.prevPosY) * partialTicks;
            double viewZ = player.prevPosZ + (player.posZ - player.prevPosZ) * partialTicks;
            ICamera frustum = new Frustum();
            frustum.setPosition(viewX, viewY, viewZ);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            RenderHelper.enableStandardItemLighting();
            GlStateManager.enableCull();
            GlStateManager.cullFace(GlStateManager.CullFace.FRONT);
            GlStateManager.enableBlend();
            GlStateManager.enableFog();
            synchronized (PokemonMapHandler.MAP_LOCK) {
                List<PokemonEntity> entities = PokemonMapHandler.getEntities();
                for (PokemonEntity entity : entities) {
                    if (frustum.isBoundingBoxInFrustum(entity.getBounds())) {
                        PokemonObjectRenderer<PokemonEntity> renderer = RenderHandler.getRenderer(entity);
                        renderer.render(entity, entity.getX() - viewX, entity.getY() - viewY, entity.getZ() - viewZ, partialTicks);
                    }
                }
                if (MC.getRenderManager().isDebugBoundingBox()) {
                    GlStateManager.disableCull();
                    GlStateManager.disableLighting();
                    GlStateManager.disableTexture2D();
                    for (PokemonEntity entity : entities) {
                        AxisAlignedBB bounds = entity.getBounds();
                        if (frustum.isBoundingBoxInFrustum(bounds)) {
                            RenderGlobal.drawBoundingBox(bounds.minX - viewX, bounds.minY - viewY, bounds.minZ - viewZ, bounds.maxX - viewX, bounds.maxY - viewY, bounds.maxZ - viewZ, 1.0F, 1.0F, 1.0F, 1.0F);
                        }
                    }
                    GlStateManager.enableLighting();
                }
            }
            GlStateManager.enableCull();
            GlStateManager.enableTexture2D();
            GlStateManager.cullFace(GlStateManager.CullFace.BACK);
            GlStateManager.disableFog();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
