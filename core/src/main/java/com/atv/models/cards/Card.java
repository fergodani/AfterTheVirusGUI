package com.atv.models.cards;

import com.atv.models.Game;
import com.atv.models.Action;

public class Card {

   private String name;
   private String subType;
   private int explorationCost;
   private Action action;

   public Card(String name, String subType, int explorationCost, Action action) {
      this.name = name;
      this.explorationCost = explorationCost;
      this.action = action;
      this.subType = subType;
   }

   public Card(String name, String subType, int explorationCost) {
      this.name = name;
      this.subType = subType;
      this.explorationCost = explorationCost;
   }

   public void play(Game game) {
      this.action.execute(game, this);
   }

   public String getName() {
      return name;
   }

   public void setName(String name) {
      this.name = name;
   }

   public int getExplorationCost() {
      return explorationCost;
   }

   public void setExplorationCost(int explorationCost) {
      this.explorationCost = explorationCost;
   }

   public Action getAction() {
      return action;
   }

   public void setAction(Action action) {
      this.action = action;
   }


   public String getSubType() {
      return subType;
   }

   public void setSubType(String subType) {
      this.subType = subType;
   }

   @Override
   public String toString() {
      return "[" + getName() + "]";
   }

   public String toStringHand() {
      return this.toString();
   }

   public String toStringExploration() {
      return "[" + getName() + ", EC: " + getExplorationCost() + "]";
   }

   public String toStringInPlay() {
      return toString();
   }
}
