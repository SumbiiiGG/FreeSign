package me.freesign.module.modules;

import com.google.gson.JsonObject;
import me.freesign.module.BotModule;
import me.freesign.scheduleing.IMcTask;
import me.freesign.scheduleing.Scheduler;
import me.freesign.utils.RenderUtils;
import net.labymod.main.LabyMod;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityChest;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.Vec3i;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AutoSignerModule extends BotModule {
    static Minecraft mc = Minecraft.getMinecraft();
    static List<SigningChest> chests = new ArrayList<>();
    JsonObject signs = new JsonObject();
    private BlockPos currentChest;
    String chestCycleStep;

    @Override
    public void onEnable() {
        super.onEnable();
        LabyMod.getInstance().displayMessageInChat("Signieren Startet in 3 Sekunden! Sobald du in einer Kiste bist bewege deine Maus schnell aus dem Fenster Raus!");
    }

    @Override
    public boolean onSendChatMessage(String message) {
        if(message.toLowerCase().startsWith("##set sign ")){
            String sign = message.replace("##set sign ","");
            signs.addProperty(Minecraft.getMinecraft().player.inventory.getCurrentItem().getDisplayName(),sign);
            LabyMod.getInstance().displayMessageInChat("Das Sign für "+Minecraft.getMinecraft().player.inventory.getCurrentItem().getDisplayName()+" wurde auf "+sign.replace("&","§")+"§r gesetzt!");
            return true;
        }
        if(message.toLowerCase().startsWith("##aus")){
            onDisable();
            chests = new ArrayList<>();
            return true;
        }
        if(message.toLowerCase().startsWith("##sign")){
            //enable it
            this.onEnable();
            this.enabled = true;
            //find all the chests
            chests = new ArrayList<>();
            for(BlockPos pos : findChests()){
                SigningChest signingChest = new SigningChest(pos);
                chests.add(signingChest);
            }

            //scheduel the first opening Chest task
            Scheduler.scheduleTask(60, new IMcTask() {
                @Override
                public void execute() {
                    openNewChest();
                }
            });
            //correct the game settings
            Minecraft.getMinecraft().gameSettings.pauseOnLostFocus = false;
            Minecraft.getMinecraft().setIngameNotInFocus();
            Minecraft.getMinecraft().displayGuiScreen(null);
            Mouse.setGrabbed(false);
            Minecraft.getMinecraft().player.inventory.currentItem = 0;
            return true;
        }
        return super.onSendChatMessage(message);
    }


    //Used to ensure your mouse is not forced to remain in minecraft
    @Override
    public void onGameTick() {
        if(this.enabled){
            Mouse.setGrabbed(false);
            if(Minecraft.getMinecraft().player.inventory.currentItem != 0){
                Minecraft.getMinecraft().player.inventory.currentItem = 0;
            }
        }
        super.onGameTick();
    }

    @Override
    public void onRender() {
        super.onRender();
        for(SigningChest signingChest : chests){
            if(signingChest.done) {
                RenderUtils.renderBox(signingChest.pos, signingChest.pos, 0, 250, 0);
            }else {
                RenderUtils.renderBox(signingChest.pos, signingChest.pos, 250, 250, 0);
            }
        }
    }

    private boolean alreadySigned(ItemStack stack){
        if(stack.getTagCompound() == null || stack.getTagCompound().toString() == null){
            return false;
        }
        if((stack.getTagCompound().toString().contains("Signiert von"))){
            return true;
        }
        return false;
    }
    private void signInv(){
        Minecraft.getMinecraft().displayGuiScreen(new GuiInventory(Minecraft.getMinecraft().player));
        Scheduler.scheduleTask(5,new IMcTask() {
            @Override
            public void execute() {
                boolean itemsLeft = false;
                Container c = Minecraft.getMinecraft().player.openContainer;
                for (int i = 0; i < c.getInventory().size(); i++) { //searches for the first unsigned Item that is on ur list, if there are none left the itemsLeft boolean wil stay false, and it moves on to next step
                    if (signs.has(c.getInventory().get(i).getDisplayName()) && !alreadySigned(c.getInventory().get(i))) {
                        itemsLeft = true;
                        //click the item
                        Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.openContainer.windowId, i, 0, ClickType.PICKUP, Minecraft.getMinecraft().player);
                        Scheduler.scheduleTask(6, new IMcTask() {
                            @Override
                            public void execute() {
                                //click on slot 1 of you hotbar
                                Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.openContainer.windowId, 36, 0, ClickType.PICKUP, Minecraft.getMinecraft().player);
                                Scheduler.scheduleTask(6, new IMcTask() {
                                    @Override
                                    public void execute() {
                                        Container c = Minecraft.getMinecraft().player.openContainer;
                                        for (int i = c.getInventory().size() - 2; i >= c.getInventory().size() - 36; i--) {
                                            if (c.getInventory().get(i).isEmpty()) {
                                                //click on a free location in you inventory
                                                Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.openContainer.windowId, i, 0, ClickType.PICKUP, Minecraft.getMinecraft().player);
                                                Minecraft.getMinecraft().displayGuiScreen(null);
                                                Scheduler.scheduleTask(6, new IMcTask() {
                                                    @Override
                                                    public void execute() {
                                                      //Sign the item
                                                        Minecraft.getMinecraft().player.sendChatMessage("/sign " + signs.get(Minecraft.getMinecraft().player.inventory.getCurrentItem().getDisplayName()).getAsString());

                                                        //if single Player simulate sign
                                                        if(Minecraft.getMinecraft().isIntegratedServerRunning()) {
                                                            Minecraft.getMinecraft().player.sendChatMessage("/replaceitem entity @p slot.container.0 minecraft:"+ Minecraft.getMinecraft().player.inventory.getCurrentItem().getDisplayName() +" "+ Minecraft.getMinecraft().player.inventory.getCurrentItem().getCount() +" 0 {display:{Lore:[\""+signs.get(Minecraft.getMinecraft().player.inventory.getCurrentItem().getDisplayName()).getAsString()+"\",\"Signiert von" +Minecraft.getMinecraft().player.getName()+  "am 02.10.2022\"]}}");
                                                            LabyMod.getInstance().displayMessageInChat("Item Signiert!");
                                                        }

                                                        //sign the next item in 5 seconds
                                                        Scheduler.scheduleTask(100, new IMcTask() {
                                                            @Override
                                                            public void execute() {
                                                                signInv();
                                                            }
                                                        });
                                                    }
                                                });
                                                break;
                                            }
                                        }
                                    }
                                });
                            }
                        });
                        break;
                    }
                }
                if (!itemsLeft) {
                    //occurs when you are only half done with a chest
                    if (chestCycleStep.equals("TakeSecondHalf")) {
                        chestCycleStep = "layOfSecondHalf";
                        openCurrentChestSecondTime();
                        //occurs when you are done with this chest, it lays of the items and moves on to next one
                    } else if (chestCycleStep.equals("layOfSecondHalf")) {
                        chestCycleStep = "TakeSecondHalf";
                        openCurrentChestFinalTime();
                    }
                }
            }

            ;
        });
    }



    private void openCurrentChestSecondTime(){
        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode());
        Scheduler.scheduleTask(5, new IMcTask() {
            @Override
            public void execute() {
                layOfInv(0,true);
            }
        });
    }

    private void openCurrentChestFinalTime(){
        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode());
        Scheduler.scheduleTask(5, new IMcTask() {
            @Override
            public void execute() {
                layOfInv(0,false);
            }
        });
    }

    private void openNewChest(){
        if(enabled) {
            for (SigningChest chest : chests) {
                if (!chest.done) { //finds the next chest that has yet to get its items signed
                    currentChest = chest.pos;
                    chestCycleStep = "TakeSecondHalf";

                    //calculate how the player should turn and click the Chest
                    Vec3d playerPos = new Vec3d(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.getEyeHeight() + Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ);
                    Vec3d chestPos = new Vec3d(chest.pos.getX() + 0.5f, chest.pos.getY() + 0.5f, chest.pos.getZ() + 0.5f);
                    Vec3d chestDirection = new Vec3d(chestPos.x - playerPos.x, chestPos.y - playerPos.y, chestPos.z - playerPos.z).normalize();
                    Minecraft.getMinecraft().player.setPositionAndRotation(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ, -(float) Math.toDegrees(Math.atan2(chestDirection.x, chestDirection.z)), (float) Math.toDegrees(Math.asin(-chestDirection.y)));
                    Scheduler.scheduleTask(4, new IMcTask() {
                        @Override
                        public void execute() {
                            KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode());
                            Scheduler.scheduleTask(6, new IMcTask() {
                                @Override
                                public void execute() {
                                    takeHalfChest(0);
                                }
                            });
                        }
                    });
                    break;
                }
            }
        }
    }

    //put you signed items in a chest
    private void layOfInv(int current,boolean takeOtherHalf){
        if(current<27){
            Container c = Minecraft.getMinecraft().player.openContainer;
            boolean itemsLeft = false;
            for (int i = c.getInventory().size() - 36; i < c.getInventory().size(); i++) { // loops over the slots in the chest, this is why we need the -36 those slots are in the players inventory
                if(signs.has(c.getInventory().get(i).getDisplayName()) && alreadySigned(c.getInventory().get(i))){
                    itemsLeft = true;
                    //take from inventory
                    Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.openContainer.windowId,i,0,ClickType.PICKUP,Minecraft.getMinecraft().player);
                    Scheduler.scheduleTask(5, new IMcTask() {
                        @Override
                        public void execute() {
                            boolean chestFull = true;
                            for (int i = 0; i < c.getInventory().size() - 36; i++) {
                                if(c.getInventory().get(i).isEmpty()) {
                                    //place in chest
                                    chestFull = false;
                                    Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.openContainer.windowId, i, 0, ClickType.PICKUP, Minecraft.getMinecraft().player);
                                    Scheduler.scheduleTask(5, new IMcTask() {
                                        @Override
                                        //repeat for next item
                                        public void execute() {
                                            layOfInv(current+1,takeOtherHalf);
                                        }
                                    });
                                    break;
                                }
                            }
                            if(chestFull){//skip lets hope thers is space in some other chest
                                layOfInv(27,takeOtherHalf);
                            }
                        }
                    });
                    break;
                }
            }
            if(!itemsLeft){
                //skips to the end
                layOfInv(27,takeOtherHalf);
            }
        }else{
            if(takeOtherHalf){
                takeHalfChest(0);
            }else{
                Minecraft.getMinecraft().player.closeScreen();
                for(SigningChest chest : chests){
                    if(chest.pos.equals(currentChest)){
                        chest.setDone(true);
                        if(!anyChestsLeft()){
                            //when done
                            displayBotDoneMessage();
                            onDisable();
                        }
                    }
                }
                openNewChest();
            }
        }
    }


    //Sendet Windows Meldung
    public void displayBotDoneMessage(){
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("System tray icon demo");
            tray.add(trayIcon);

            trayIcon.displayMessage("Sign Bot", "fertig mit signen", TrayIcon.MessageType.INFO);
        }catch (Exception e){
            Toolkit.getDefaultToolkit().beep();
        }
    }


    private boolean anyChestsLeft(){
        for(SigningChest chest : chests){
            if(!chest.done){
                return true;
            }
        }
        return false;
    }

    private void takeHalfChest(int current){
        if(current<27) {
            Container c = Minecraft.getMinecraft().player.openContainer;
            boolean itemsLeft = false;
            for (int i = 0; i < c.getInventory().size() - 36; i++) {
                if(signs.has(c.getInventory().get(i).getDisplayName()) && !alreadySigned(c.getInventory().get(i))){ //finds the first item that need signing
                    itemsLeft = true;
                    Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.openContainer.windowId,i,0,ClickType.PICKUP,Minecraft.getMinecraft().player); //take the item from the chest
                    Scheduler.scheduleTask(5, new IMcTask() {
                        @Override
                        public void execute() {
                            Container c = Minecraft.getMinecraft().player.openContainer;
                            for(int i=c.getInventory().size()-36;i<c.getInventory().size();i++) {
                                if (c.getInventory().get(i).isEmpty()) { //find a free player inventory spot
                                    Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.openContainer.windowId, i, 0, ClickType.PICKUP, Minecraft.getMinecraft().player);//place item in player inventory
                                    Scheduler.scheduleTask(5, new IMcTask() {
                                        @Override
                                        public void execute() {
                                            takeHalfChest(current+1);
                                        } //repeat
                                    });
                                    break;
                                }
                            }
                        }
                    });
                    break;
                }
            }
            if(!itemsLeft){
                takeHalfChest(27);
            }
        }else{
            //when finished taking the items start signing
            Minecraft.getMinecraft().player.closeScreen();
            signInv();
        }
    }

    private java.util.List<BlockPos> findChests(){
        List<BlockPos> chests = new ArrayList<BlockPos>();
        JsonObject blackListed = new JsonObject();
        for(TileEntity te : mc.world.loadedTileEntityList){
            Vec3i playerPos = new Vec3i(mc.player.posX,mc.player.posY,mc.player.posZ);
            if(te.getPos().distanceSq(playerPos) <= 40){
                if(te instanceof TileEntityChest){
                    TileEntityChest cte = (TileEntityChest) te;

                    //when we encounter a double chest it should only use the one closest to the player
                    if(cte.adjacentChestXPos!=null || cte.adjacentChestXNeg!=null ||cte.adjacentChestZPos!=null || cte.adjacentChestZNeg!=null){
                        if(!blackListed.has(cte.getPos()+"")){
                            BlockPos pos = getLinkedChestPos(cte);
                            if(te.getPos().distanceSq(playerPos) < pos.distanceSq(playerPos)){
                                chests.add(cte.getPos());
                                blackListed.addProperty(pos+"",true);
                                blackListed.addProperty(te.getPos()+"",true);
                            }else{
                                chests.add(pos);
                                blackListed.addProperty(pos+"",true);
                                blackListed.addProperty(te.getPos()+"",true);
                            }
                        }
                    }else{
                        chests.add(cte.getPos());
                        blackListed.addProperty(cte.getPos()+"","true");
                    }
                }
            }
        }
        return chests;
    }

    //finds out what chests are linked to each other (Double Chests)
    private BlockPos getLinkedChestPos(TileEntityChest chest){
        BlockPos pos = chest.getPos();
        if(chest.adjacentChestXPos!=null){
            if(chest.adjacentChestXPos.getChestType().equals(chest.getChestType())){
                return chest.adjacentChestXPos.getPos();
            }
        }
        if(chest.adjacentChestZPos!=null){
            if(chest.adjacentChestZPos.getChestType().equals(chest.getChestType())){
                return chest.adjacentChestZPos.getPos();
            }
        }
        if(chest.adjacentChestXNeg!=null){
            if(chest.adjacentChestXNeg.getChestType().equals(chest.getChestType())){
                return chest.adjacentChestXNeg.getPos();
            }
        }
        if(chest.adjacentChestZNeg!=null){
            if(chest.adjacentChestZNeg.getChestType().equals(chest.getChestType())){
                return chest.adjacentChestZNeg.getPos();
            }
        }
        return pos;
    }


    class SigningChest{
        boolean done=false;
        BlockPos pos;
        public SigningChest(BlockPos pos){
            this.pos = pos;
        }
        public void setDone(boolean b){
            int baa = 2;
            switch (baa){
                case 0:
                    setDone(true);
                    break;
                case 1:

            }
            done=b;
        }

    }
}
