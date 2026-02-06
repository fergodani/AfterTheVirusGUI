package com.atv.models.views;

import com.atv.models.cards.Card;

import java.util.List;

public interface PlayerInteraction {

   Card selectCard(List<Card> cardsAvailable);
   List<Card> selectCards(List<Card> cardsAvailable);
   int selectOption(String message, int numberOfOptions);
   void killZombies(List<Card> zombies, int quantity);
   void discardZombies(List<Card> zombies, int quantity);
   void showMessage(String message);

}
