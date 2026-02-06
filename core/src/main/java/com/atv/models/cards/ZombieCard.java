package com.atv.models.cards;

import com.atv.models.Action;

public class ZombieCard extends Card {

   private int value;

   private int zombiesDiscarded = 0;
   private int zombiesLeft;

   // Para la carta de cura
   private boolean isSurvivor;

   public ZombieCard(int value, Action action) {
      super(value == 1 ? "1 zombie" : value + " zombies", "Zombie", 0, action);
      this.value = value;
      this.zombiesLeft = value;
   }

   @Override
   public String toString() {
      return "[Zombie: " + value + "]";
   }

   public int getValue() {
      return value;
   }

   public void setValue(int value) {
      this.value = value;
   }

   public boolean isSurvivor() {
      return isSurvivor;
   }

   public void setSurvivor(boolean survivor) {
      isSurvivor = survivor;
   }

   public int getZombiesDiscarded() {
      return zombiesDiscarded;
   }

   public void setZombiesDiscarded(int zombiesDiscarded) {
      this.zombiesDiscarded = zombiesDiscarded;
   }

   public int getZombiesLeft() {
      return zombiesLeft;
   }

   public void setZombiesLeft(int zombiesLeft) {
      this.zombiesLeft = zombiesLeft;
   }
}
