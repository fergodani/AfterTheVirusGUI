package com.atv.models;

public enum Character {

    RUTH("Ruth", "Pub"),
    ADAM("Adam", "Machete"),
    ROBERT("Robert", "Leather Jacket"),
    JENNIE("Jennie", "Weapon Skill"),
    ;

    private final String name;
    private final String cardInPlay;

    Character(String name, String cardInPlay) {
        this.name = name;
        this.cardInPlay = cardInPlay;
    }

    public String getName() {
        return name;
    }

    public String getCardInPlay() {
        return cardInPlay;
    }
}
