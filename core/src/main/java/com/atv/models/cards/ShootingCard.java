package com.atv.models.cards;

import com.atv.models.Action;

import java.util.List;
import java.util.Stack;


public class ShootingCard extends PermanentCard{

   boolean isCostInAmmo;
   boolean multipleAmmo;
   Stack<Card> ammo = new Stack<>();

   public ShootingCard(String name, int explorationCost, int usageCost, String subtype, boolean isCostInAmmo, Action action) {
      super(name, explorationCost, usageCost, subtype, action);
      this.isCostInAmmo = isCostInAmmo;
   }

   public ShootingCard(String name, int explorationCost, int usageCost, String subtype, boolean isCostInAmmo, Action action, boolean oncePerTurn) {
      super(name, explorationCost, usageCost, subtype, oncePerTurn, action);
      this.isCostInAmmo = isCostInAmmo;
   }

   public ShootingCard(String name, int explorationCost, int usageCost, String subtype, boolean isCostInAmmo, boolean multipleAmmo, Action action) {
      super(name, explorationCost, usageCost, subtype, action);
      this.isCostInAmmo = isCostInAmmo;
      this.multipleAmmo = multipleAmmo;
   }

   public void prepare(List<Card> ammo) {
      this.ammo.addAll(ammo);
   }

   public void addAmmo(Card ammo) {
      this.ammo.add(ammo);
   }
   public void addAmmo(List<Card> ammo) {
      this.ammo.addAll(ammo);
   }

   public Card shoot() {
      return this.ammo.pop();
   }

   public Stack<Card> getAmmo() {
      return ammo;
   }

   @Override
   public String toStringInPlay() {
      return "[" + getName() + ", PC: " + getPrepareCost() + " prep: " + isPrepared() + " ammo: " + ammo.size() + "]";
   }
}
