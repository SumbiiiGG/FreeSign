package me.freesign.module.modules;

import com.google.gson.JsonObject;
import me.freesign.module.BotModule;
import me.freesign.scheduleing.IMcTask;
import me.freesign.scheduleing.Scheduler;
import me.freesign.utils.RenderUtils;
import net.labymod.main.LabyMod;
import net.minecraft.block.BlockAnvil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiRepair;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.Container;
import net.minecraft.item.Item;
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

public class AutoRenamerModule extends BotModule {
    static Minecraft mc = Minecraft.getMinecraft();
    static List<SigningChest> chests = new ArrayList<>();
    JsonObject names = new JsonObject();
    private BlockPos currentChest;
    String chestCycleStep;

    JsonObject newNamesToOrg = new JsonObject();
    private BlockPos anvilPos = new BlockPos(0,0,0);

    @Override
    public void onEnable() {
        super.onEnable();
        LabyMod.getInstance().displayMessageInChat("Signieren Startet in 3 Sekunden! Sobald du in einer Kiste bist bewege deine Maus schnell aus dem Fenster Raus!");
    }

    @Override
    public boolean onSendChatMessage(String message) {
        if (message.startsWith("##test")) {
            openAnvil();
            enabled = true;
        }
        if(message.toLowerCase().startsWith("##set name ")){
            String name = message.replace("##set name ","");
            names.addProperty(Minecraft.getMinecraft().player.inventory.getCurrentItem().getDisplayName(),name);
            newNamesToOrg.addProperty(name,Minecraft.getMinecraft().player.inventory.getCurrentItem().getDisplayName());
            LabyMod.getInstance().displayMessageInChat("Der Name für "+ Minecraft.getMinecraft().player.inventory.getCurrentItem().getDisplayName()+" wurde auf "+name.replace("&","§")+"§r gesetzt!");
            return true;
        }
        if(message.toLowerCase().startsWith("##aus")){
            onDisable();
            chests = new ArrayList<>();
        }
        if(message.toLowerCase().startsWith("##rename")){
            //enable it
            this.onEnable();
            this.enabled = true;
            //find all the chests
            chests = new ArrayList<>();
            for(BlockPos pos : findChests()){
                AutoRenamerModule.SigningChest signingChest = new SigningChest(pos);
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


    private void openCurrentChestSecondTime(){
        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode());
        System.out.println("214!");
        Scheduler.scheduleTask(5, new IMcTask() {
            @Override
            public void execute() {
                layOfInv(0,true);
            }
        });
    }

    private void openCurrentChestFinalTime(){
        KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode());
        System.out.println("od2");
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
                            System.out.println("o1!");
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
                if(newNamesToOrg.has(c.getInventory().get(i).getDisplayName())){
                    itemsLeft = true;
                    //take from inventory
                    Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.openContainer.windowId,i,0,ClickType.PICKUP, Minecraft.getMinecraft().player);
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
        }else if(takeOtherHalf){
                takeHalfChest(0);
            }else{
                Minecraft.getMinecraft().player.closeScreen();
                for(SigningChest chest : chests){
                    if(chest.pos.equals(currentChest)){
                        chest.setDone(true);
                        if (this.anyChestsLeft()) {
                            continue;
                        }
                            displayMessage("Umbennen Bot Fertig!");
                            onDisable();
                    }
                }
                openNewChest();
            }
    }








    private BlockPos getAnvilPos() {
        if (mc.world.getBlockState(anvilPos).getBlock() instanceof BlockAnvil) {
            return this.anvilPos;
        }
        for (int x = (int)mc.player.posX - 5; x <= (int)mc.player.posX + 5; x++) {
            for (int y = (int)mc.player.posY - 5; y <= (int)mc.player.posY + 5; y++) {
                for (int z = (int)mc.player.posZ - 5; z <= (int)mc.player.posZ + 5; z++) {
                    final BlockPos pos = new BlockPos(x, y, z);
                    if (mc.world.getBlockState(pos).getBlock() instanceof BlockAnvil) {
                        return this.anvilPos = pos;
                    }
                }
            }
        }
        return null;
    }

    private int nextRenameItem() {
        final Container c = mc.player.openContainer;
        for (int i = 3; i < c.getInventory().size(); ++i) {
            if (names.has(c.getInventory().get(i).getDisplayName())) {
                return i;
            }
        }
        return -1;
    }

    private void openAnvil() {
        if (this.getAnvilPos() == null) {
            System.out.println("Kein Amboss!");
            LabyMod.getInstance().displayMessageInChat("Kein Amboss in der n\u00e4he!!!");
            displayMessage("Ambosse sind Aus!");
            return;
        }
        Vec3d playerPos = new Vec3d(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.getEyeHeight() + Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ);
        Vec3d anvilDirection = new Vec3d(anvilPos.getX() - playerPos.x + 0.5, anvilPos.getY() - playerPos.y + 0.5, anvilPos.getZ() - playerPos.z + 0.5).normalize();
        Minecraft.getMinecraft().player.setPositionAndRotation(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ, -(float)Math.toDegrees(Math.atan2(anvilDirection.x, anvilDirection.z)), (float)Math.toDegrees(Math.asin(-anvilDirection.y)));
        Scheduler.scheduleTask(5, new IMcTask() {
            @Override
            public void execute() {
                KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode());
                System.out.println("open anvil!");
                Scheduler.scheduleTask(5, new IMcTask() {
                    @Override
                    public void execute() {
                        try {
                              renameInv();
                        }
                        catch (Exception e) {
                            LabyMod.getInstance().displayMessageInChat("Ein Fehler ist aufgetreten!");
                        }
                    }
                });
            }
        });
    }

    private void renameInv() throws Exception {
        if(enabled) {
            Container c = mc.player.openContainer;
            if (mc.currentScreen instanceof GuiRepair) {
                GuiRepair anvilGui = (GuiRepair) mc.currentScreen;
                Scheduler.scheduleTask(8, new IMcTask() {
                    @Override
                    public void execute() {
                        final int next = nextRenameItem();
                        System.out.println(next);
                        if (next < 0) {
                            if (c.getInventory().get(0).isEmpty()) {
                                final Vec3d playerPos = new Vec3d(mc.player.posX, mc.player.getEyeHeight() + mc.player.posY, mc.player.posZ);
                                final Vec3d chestPos = new Vec3d((double) (currentChest.getX() + 0.5f), (double) (currentChest.getY() + 0.5f), (double) (currentChest.getZ() + 0.5f));
                                final Vec3d chestDirection = new Vec3d(chestPos.x - playerPos.x, chestPos.y - playerPos.y, chestPos.z - playerPos.z).normalize();
                                Minecraft.getMinecraft().player.setPositionAndRotation(Minecraft.getMinecraft().player.posX, Minecraft.getMinecraft().player.posY, Minecraft.getMinecraft().player.posZ, -(float) Math.toDegrees(Math.atan2(chestDirection.x, chestDirection.z)), (float) Math.toDegrees(Math.asin(-chestDirection.y)));
                                KeyBinding.onTick(Minecraft.getMinecraft().gameSettings.keyBindUseItem.getKeyCode());
                                System.out.println("ope311l!");
                                Minecraft.getMinecraft().displayGuiScreen(null);
                                Scheduler.scheduleTask(6, new IMcTask() {
                                    @Override
                                    public void execute() {
                                        if (chestCycleStep.equals("TakeSecondHalf")) {
                                            chestCycleStep = "layOfSecondHalf";
                                            openCurrentChestSecondTime();
                                        } else if (chestCycleStep.equals("layOfSecondHalf")) {
                                            chestCycleStep = "TakeSecondHalf";
                                            openCurrentChestFinalTime();
                                        }
                                    }
                                });
                            } else {
                                Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.openContainer.windowId, 2, 0, ClickType.PICKUP, mc.player);
                                try {
                                    Scheduler.scheduleTask(4, new IMcTask() {
                                        @Override
                                        public void execute() {
                                            try {
                                                renameInv();
                                            }catch (Exception e){}
                                        }
                                    });
                                } catch (Exception e) {
                                    LabyMod.getInstance().displayMessageInChat("Ein fehler ist aufgetreten!");
                                    mc.player.sendChatMessage("##aus");
                                }
                            }
                        } else {
                            if (c.getInventory().get(0).isEmpty()) {
                                mc.playerController.windowClick(c.windowId, nextRenameItem(), 0, ClickType.PICKUP, mc.player);
                            }
                            Scheduler.scheduleTask(8, new IMcTask() {
                                @Override
                                public void execute() {
                                    if (c.getInventory().get(0).isEmpty()) {
                                        mc.playerController.windowClick(c.windowId, 0, 0, ClickType.PICKUP, mc.player);
                                    }
                                    Scheduler.scheduleTask(10, new IMcTask() {
                                        @Override
                                        public void execute() {
                                            ItemStack stack = new ItemStack(new Item());
                                            stack.setStackDisplayName(names.get(c.getInventory().get(0).getDisplayName()).getAsString());
                                            anvilGui.sendSlotContents(c,0,stack);
                                            if (c.getSlot(0).getStack().getRepairCost() < mc.player.experienceLevel) {
                                                Scheduler.scheduleTask(9, new IMcTask() {
                                                    @Override
                                                    public void execute() {
                                                        mc.playerController.windowClick(c.windowId, 2, 0, ClickType.QUICK_MOVE, mc.player);
                                                        System.out.println("yey");
                                                        Scheduler.scheduleTask(9, new IMcTask() {
                                                            @Override
                                                            public void execute() {
                                                                try {
                                                                    renameInv();
                                                                } catch (Exception e) {
                                                                    LabyMod.getInstance().displayMessageInChat("Ein fehler ist aufgetreten!");
                                                                    mc.player.sendChatMessage("##aus");
                                                                }
                                                            }
                                                        });
                                                    }
                                                });
                                                return;
                                            } else {
                                                try {
                                                    renameInv();
                                                } catch (Exception e) {
                                                    LabyMod.getInstance().displayMessageInChat("Ein fehler ist aufgetreten!");
                                                    mc.player.sendChatMessage("##aus");
                                                }
                                            }
                                        }
                                    });
                                }
                            });
                        }
                    }
                });
            } else {
                Scheduler.scheduleTask(20, new IMcTask() {
                    @Override
                    public void execute() {
                        openAnvil();
                    }
                });
            }
        }
    }

    //Sendet Windows Meldung
    public void displayMessage(String msg){
        try {
            SystemTray tray = SystemTray.getSystemTray();
            Image image = Toolkit.getDefaultToolkit().createImage("icon.png");
            TrayIcon trayIcon = new TrayIcon(image, "Tray Demo");
            trayIcon.setImageAutoSize(true);
            trayIcon.setToolTip("System tray icon demo");
            tray.add(trayIcon);

            trayIcon.displayMessage("Umbennen Bot", msg, TrayIcon.MessageType.INFO);
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
                if(names.has(c.getInventory().get(i).getDisplayName())){ //finds the first item that need renaming
                    itemsLeft = true;
                    Minecraft.getMinecraft().playerController.windowClick(Minecraft.getMinecraft().player.openContainer.windowId,i,0,ClickType.PICKUP, Minecraft.getMinecraft().player); //take the item from the chest
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
            //when finished taking the items start Renaming
            Minecraft.getMinecraft().player.closeScreen();
            openAnvil();
        }
    }

    private List<BlockPos> findChests(){
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
