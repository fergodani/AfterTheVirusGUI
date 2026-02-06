package com.atv.models.cards;

import com.atv.models.Game;
import com.atv.models.Action;

public class PermanentCard extends Card{

   private int prepareCost;
   private Action enterAction;
   private Action prepareAction;
   private Action exitAction;
   private boolean oncePerTurn = false;
   private boolean used = false;
   private boolean prepared = false;

   public PermanentCard(String name, int explorationCost, int prepareCost, String subType, Action action) {
      super(name, subType, explorationCost, action);
      this.prepareCost = prepareCost;
   }

   public PermanentCard(String name, int explorationCost, int prepareCost, String subType, boolean oncePerTurn, Action action) {
      super(name, subType, explorationCost, action);
      this.prepareCost = prepareCost;
      this.oncePerTurn = oncePerTurn;
   }

   public PermanentCard(String name, int explorationCost, int usageCost, String subType) {
      super(name, subType, explorationCost);
      this.prepareCost = usageCost;
   }
   public void prepare(Game game) {
      if (this.prepareAction != null) {
         this.prepareAction.execute(game, this);
      } else {
         if (game.discard(this.prepareCost)) {
            this.prepared = true;
         }
      }

   }

   public void setPrepared(boolean prepared) {
      this.prepared = prepared;
   }

   public boolean isPrepared() {
      return prepared;
   }

   public int getPrepareCost() {
      return prepareCost;
   }

   public void setPrepareCost(int prepareCost) {
      this.prepareCost = prepareCost;
   }

   public Action getPrepareAction() {
      return prepareAction;
   }

   public void setPrepareAction(Action prepareAction) {
      this.prepareAction = prepareAction;
   }

   public Action getExitAction() {
      return exitAction;
   }

   public void setExitAction(Action exitAction) {
      this.exitAction = exitAction;
   }

   public boolean isOncePerTurn() {
      return oncePerTurn;
   }

   public void setOncePerTurn(boolean oncePerTurn) {
      this.oncePerTurn = oncePerTurn;
   }

   public boolean isUsed() {
      return used;
   }

   public void setUsed(boolean used) {
      this.used = used;
   }

   @Override
   public String toString() {
      return "[" + getName() + ", PC: " + getPrepareCost() + ", EC: " + getExplorationCost() + "]";
   }

   @Override
   public String toStringHand() {
      return "[" + getName() + ", PC: " + getPrepareCost() + "]";
   }

   @Override
   public String toStringInPlay() {
      return "[" + getName() + ", PC: " + getPrepareCost() + " prep: " + isPrepared() + "]";
   }

    public Action getEnterAction() {
        return enterAction;
    }

    public void setEnterAction(Action enterAction) {
        this.enterAction = enterAction;
    }
}
