package me.freesign.events;

import me.freesign.FreeSignAddon;
import me.freesign.module.BotModule;
import me.freesign.scheduleing.Scheduler;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

public class GameTickEvent {
    @SubscribeEvent
    public void onGameTick(TickEvent.ClientTickEvent event){
        Scheduler.tick(); // Tick the scheduler
        for(BotModule botModule : FreeSignAddon.getInstance().getBotModules()){
            botModule.onGameTick();
        }
    }
}
