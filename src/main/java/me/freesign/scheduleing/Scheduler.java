package me.freesign.scheduleing;

import java.util.ArrayList;
import java.util.List;

public class Scheduler {
    //Stores All the tasks that the scheduler should do, and how long till it executes them
    public static List<MCTask> tasks = new ArrayList<>();

    //Delay is in Minecraft ticks, Used to schedule a Task
    public static void scheduleTask(int delay,IMcTask task){
        tasks.add(new MCTask(delay, task));
    }

    //called once every minecraft tick
    public static void tick(){
        for(int i=0;i<tasks.size();i++){ // Go through every task
            if(tasks.get(i).ready()){ //if the delay is over
                tasks.remove(i); //remove the now finished task
                i--; //reduce i by 1 so that no tasks are skipped
            }
        }
    }

    //A subclass used to store a Task and its delay together
    static class MCTask{
        int delay;
        IMcTask task;
        MCTask(int delay,IMcTask task){
            this.delay = delay;
            this.task = task;
        }

        /*
        if the delay has hit 0 it executes the task and returns true, else it reduces the delay by 1,
        this is done so that the delay value will be decreased every tick, thus reaching 0 after the initial delay is over.
        */
        public boolean ready(){
            if(delay==0){
                task.execute();
                return true;
            }
            delay--;
            return false;
        }
    }
}
