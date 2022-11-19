package me.freesign;

import me.freesign.events.GameTickEvent;
import me.freesign.events.RenderEvent;
import me.freesign.module.BotModule;
import me.freesign.module.modules.AutoRenamerModule;
import me.freesign.module.modules.AutoSignerModule;
import me.freesign.module.modules.SignSimulator;
import net.labymod.api.LabyModAddon;
import net.labymod.api.events.MessageSendEvent;
import net.labymod.settings.elements.SettingsElement;

import java.util.ArrayList;
import java.util.List;

public class FreeSignAddon extends LabyModAddon {
    private ArrayList<BotModule>  botModules = new ArrayList<>();
    private static FreeSignAddon instance;

    public static FreeSignAddon getInstance(){
        return instance;
    }

    public ArrayList<BotModule> getBotModules(){return botModules;}

    @Override
    public void onEnable() {
        instance = this;
        registerBotModules();
        registerEvents();
    }

    @Override
    public void loadConfig() {

    }

    @Override
    protected void fillSettings(List<SettingsElement> list) {

    }

    public void registerBotModules(){
        botModules.add(new SignSimulator());
        botModules.add(new AutoSignerModule());
        botModules.add(new AutoRenamerModule());
    }
    public void registerEvents(){
        //Register Forge Events
        this.api.registerForgeListener(new GameTickEvent());
        this.api.registerForgeListener(new RenderEvent());

        //Register Labymod Events
        this.getApi().getEventManager().register(new MessageSendEvent() {
            @Override
            public boolean onSend(String s) {
                for(BotModule botModule : botModules){
                    if(botModule.onSendChatMessage(s)){
                        return true;
                    }
                }
                return false;
            }
        });
    }
}
