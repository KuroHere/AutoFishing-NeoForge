package me.kurohere.autofish.scheduler;

import net.minecraft.Util;

public class Action {
    private final ActionType actionType;
    private final long delay;
    private long timeToComplete;
    private final Runnable runnable;

    public Action(ActionType actionType, long delay, Runnable runnable) {
        this.actionType = actionType;
        this.delay = delay;
        this.timeToComplete = Util.getMillis() + delay;
        this.runnable = runnable;

//        System.out.println("regd " + actionType + ". Complete in " + delay + " at " + timeToComplete);
    }

    /**
     * @return true if the action was completed
     */
    public boolean tick(){

        if(Util.getMillis() >= timeToComplete){

            runnable.run();

            //If this is a repeating action, we need to reset the timer
            if(actionType == ActionType.REPEATING_ACTION){
                this.timeToComplete = Util.getMillis() + delay;
            }

            return true;
        }
        return false;
    }

    public ActionType getActionType() {
        return actionType;
    }

    public void resetTimer(){
        this.timeToComplete = Util.getMillis() + delay;
    }
}
