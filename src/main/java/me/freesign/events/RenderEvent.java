package me.freesign.events;

import me.freesign.FreeSignAddon;
import me.freesign.module.BotModule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class RenderEvent {
    @SubscribeEvent
    public void onRenderLast(RenderWorldLastEvent event){
        // Handel all the GL stuff here

        double doubleX = Minecraft.getMinecraft().getRenderViewEntity().posX + (Minecraft.getMinecraft().getRenderViewEntity().posX - Minecraft.getMinecraft().getRenderViewEntity().lastTickPosX);
        double doubleY = Minecraft.getMinecraft().getRenderViewEntity().posY + (Minecraft.getMinecraft().getRenderViewEntity().posY - Minecraft.getMinecraft().getRenderViewEntity().lastTickPosY) + 0.1;
        double doubleZ = Minecraft.getMinecraft().getRenderViewEntity().posZ + (Minecraft.getMinecraft().getRenderViewEntity().posZ - Minecraft.getMinecraft().getRenderViewEntity().lastTickPosZ);

        GlStateManager.pushMatrix();
        GlStateManager.translate(-doubleX, -doubleY, -doubleZ);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GlStateManager.disableTexture2D();
        GlStateManager.disableCull();

        for(BotModule botModule : FreeSignAddon.getInstance().getBotModules()){
            botModule.onRender();
        }

        GlStateManager.popMatrix();
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GlStateManager.resetColor();
        GlStateManager.enableTexture2D();
    }
}
