package me.freesign.module.modules;

import me.freesign.module.BotModule;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

//zum testen im single player gedacht
public class SignSimulator extends BotModule {
    @Override
    public boolean onSendChatMessage(String message) {
        if(message.toLowerCase().startsWith("/sign")){
            //if singleplayer
            if(Minecraft.getMinecraft().isIntegratedServerRunning()) {
                Minecraft.getMinecraft().player.sendChatMessage("/replaceitem entity @p slot.container.0 minecraft:"+ Minecraft.getMinecraft().player.inventory.getCurrentItem().getDisplayName() +" "+ Minecraft.getMinecraft().player.inventory.getCurrentItem().getCount() +" 0 {display:{Lore:[\""+message.replace("/sign ","")+"\",\"Signiert von" +Minecraft.getMinecraft().player.getName()+  "am 02.10.2022\"]}}");
                LabyMod.getInstance().displayMessageInChat("Item Signiert!");
                return true;
            }
        }
        if(message.toLowerCase().startsWith("##getlore")){
            System.out.println(Minecraft.getMinecraft().player.inventory.getCurrentItem().getTagCompound());
        }
        return false;
    }
}
