package com.atv.models.views;

import com.atv.models.Character;

public interface UserInterface {

   void updateInfo();
   void showMessage(String message);

   PlayerInteraction getPlayerInteraction();
   void playAction();
}
