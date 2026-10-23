package com.atv.models.views;

import com.atv.models.cards.Card;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public interface PlayerInteraction {

   Card selectCard(List<Card> cardsAvailable);
   List<Card> selectCards(List<Card> cardsAvailable);
   default void selectCards(List<Card> cardsAvailable, int quantity, Consumer<List<Card>> selectionConsumer) {
      List<Card> selectedCards = new ArrayList<>();
      for (int i = 0; i < quantity; i++) {
         Card selectedCard = selectCard(cardsAvailable);
         if (selectedCard == null || selectedCards.contains(selectedCard)) {
            return;
         }
         selectedCards.add(selectedCard);
      }
      selectionConsumer.accept(selectedCards);
   }
   default void selectDamage(Runnable armsAction, Runnable legsAction) {
      if (selectOption("¿Dónde quieres recibir el daño?\n1. Brazos\n2. Piernas", 2) == 1) {
         armsAction.run();
      } else {
         legsAction.run();
      }
   }
   int selectOption(String message, int numberOfOptions);
   void killZombies(List<Card> zombies, int quantity);
   void discardZombies(List<Card> zombies, int quantity);
   void showMessage(String message);

}
