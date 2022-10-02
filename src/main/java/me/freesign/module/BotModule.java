package me.freesign.module;

public class BotModule {
    public boolean enabled = false;

    public void onEnable(){
        this.enabled = true;
    }

    public void onDisable(){
        this.enabled = false;
    }
    //if it is enabled disable it else enable it
    public void onToggle(){
        if(this.enabled){
            onDisable();
        }else if(!this.enabled){
            onEnable();
        }
    }

    /*
    used when the player sends a Chat message
    returns false by default, if it returns true the message is not sent to the Minecraft Server and won't appear in the public chat
     */
    public boolean onSendChatMessage(String message){
        return false;
    }

    //used when the game ticks
    public void onGameTick(){

    }

    //used on render, this is the last thing that renders in the world
    public void onRender(){

    }
}
