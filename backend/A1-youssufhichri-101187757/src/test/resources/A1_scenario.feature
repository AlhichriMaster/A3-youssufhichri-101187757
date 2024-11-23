Feature: A1 Quest Scenario

  Scenario: A1_scenario
    Given a new game is initialized with 4 players
    And player "P1"'s hand is initialized 1
    And player "P2"'s hand is initialized 1
    And player "P3"'s hand is initialized 1
    And player "P4"'s hand is initialized 1
    And the following cards are rigged to be drawn in order:
      | cardId |
      | F30   |
      | S10   |
      | B15   |
      | F10   |
      | L20   |
      | L20   |
      | B15   |
      | S10   |
      | F30   |
      | L20   |

    When P1 draws a quest card "Q4"
    And P1 declines to sponsor the quest
    And P2 accepts to sponsor the quest
    And P2 sets up the quest stages:
      | stage | cards        |
      | 1     | F5, H10      |
      | 2     | F15, S10     |
      | 3     | F15, D5, B15 |
      | 4     | F40, B15     |


    And P1 agrees to participate in the quest
    And P3 agrees to participate in the quest
    And P4 agrees to participate in the quest



    And P1 draws a card for stage 1
#    And P1 discards "F5" for stage 1

    And P3 draws a card for stage 1
#    And P3 discards "F5" for stage 1

    And P4 draws a card for stage 1
#    And P4 discards "F5" for stage 1

    And P1 continues and attacks stage 1 with "D5, S10"
    And P3 continues and attacks stage 1 with "S10, D5"
    And P4 continues and attacks stage 1 with "D5, H10"


    And P1 draws a card for stage 2
    And P3 draws a card for stage 2
    And P4 draws a card for stage 2

    And P1 continues and attacks stage 2 with "H10, S10"
    And P3 continues and attacks stage 2 with "B15, S10"
    And P4 continues and attacks stage 2 with "H10, B15"


    And P3 draws a card for stage 3
    And P4 draws a card for stage 3

    And P3 continues and attacks stage 3 with "L20, H10, S10"
    And P4 continues and attacks stage 3 with "B15, S10, L20"


    And P3 draws a card for stage 4
    And P4 draws a card for stage 4

    And P3 continues and attacks stage 4 with "B15, H10, L20"
    And P4 continues and attacks stage 4 with "D5, S10, L20, E30"


    #We need P2 to discard his extra cards now
    And P2 draws 13 cards
    And P2 discards all extra cards

    And all quest responses are processed 1



    Then P1 should have 0 shields
    And P1's hand should contain
      |cardId |
      | F5    |
      | F10   |
      | F15   |
      | F15   |
      | F30   |
      | H10   |
      | B15   |
      | B15   |
      | L20   |

    And P2 should have 0 shields
    And P2's hand should have 12 cards

    And P3 should have 0 shields
    And P3's hand should contain
      |cardId |
      | F5    |
      | F5    |
      | F15   |
      | F30   |
      | S10   |
    And P4 should have 4 shields
    And P4's hand should contain
      | cardId |
      | F15    |
      | F15    |
      | F40    |
      | L20    |




  Scenario: 2winner_game_2winner_quest
    Given a new game is initialized with 4 players
    And player "P1"'s hand is initialized 2
    And player "P2"'s hand is initialized 2
    And player "P3"'s hand is initialized 2
    And player "P4"'s hand is initialized 2

    And the following cards are rigged to be drawn in order:
      | cardId |
      #stage 1
      | B15   |
      | D5    |
      | B15   |

      #stage 2
      | B15   |
      | B15   |

      #stage 3
      | L20   |
      | L20   |

      #stage 4
      | E30   |
      | E30   |


      | D5    |
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      | L20   |


    When P1 draws a quest card "Q4"
    And P1 accepts to sponsor the quest
    And P1 sets up the quest stages:
      | stage | cards        |
      | 1     | F15          |
      | 2     | F5, B15      |
      | 3     | F10, D5, H10 |
      | 4     | F25, D5      |


    And P2 agrees to participate in the quest
    And P3 agrees to participate in the quest
    And P4 agrees to participate in the quest


    And P2 draws a card for stage 1
    And P3 draws a card for stage 1
    And P4 draws a card for stage 1
#
#    #P2 and P4 win since their attack is 15
#    #P3 loses
    And P2 continues and attacks stage 1 with "B15"
    And P3 continues and attacks stage 1 with "D5"
    And P4 continues and attacks stage 1 with "B15"


    #Both P2 and P4 play out the next full quest and both win

    And P2 draws a card for stage 2
    And P4 draws a card for stage 2

    And P2 continues and attacks stage 2 with "D5, B15"
    And P4 continues and attacks stage 2 with "D5, B15"


    And P2 draws a card for stage 3
    And P4 draws a card for stage 3

    And P2 continues and attacks stage 3 with "L20, D5"
    And P4 continues and attacks stage 3 with "L20, D5"


    And P2 draws a card for stage 4
    And P4 draws a card for stage 4

    And P2 continues and attacks stage 4 with "E30"
    And P4 continues and attacks stage 4 with "E30"

    And all quest responses are processed 2
    And the following cards are rigged to be drawn in order:
      | cardId |
      #stage 1
      | B15   |
      | D5    |
      | B15   |

      #stage 2
      | B15   |
      | B15   |

      #stage 3
      | L20   |
      | L20   |

      #stage 4
      | E30   |
      | E30   |


      | D5    |
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      | L20   |



    And P2 draws a quest card "Q3"
    And P3 accepts to sponsor the quest

    And P3 sets up the quest stages:
      | stage | cards |
      | 1     | F5    |
      | 2     | F10   |
      | 3     | F15   |

    And P1 declines to participate in the quest
    And P2 agrees to participate in the quest
    And P4 agrees to participate in the quest


    And P2 draws a card for stage 1
    And P4 draws a card for stage 1

    And P2 continues and attacks stage 1 with "D5"
    And P4 continues and attacks stage 1 with "D5"


    And P2 draws a card for stage 2
    And P4 draws a card for stage 2

    And P2 continues and attacks stage 2 with "H10"
    And P4 continues and attacks stage 2 with "H10"


    And P2 draws a card for stage 3
    And P4 draws a card for stage 3

    And P2 continues and attacks stage 3 with "B15"
    And P4 continues and attacks stage 3 with "B15"

    And all quest responses are processed 2

    Then P2 should have 7 shields
    And P4 should have 7 shields




  Scenario: 1winner_game_with_events
    Given a new game is initialized with 4 players
    And player "P1"'s hand is initialized 3
    And player "P2"'s hand is initialized 3
    And player "P3"'s hand is initialized 3
    And player "P4"'s hand is initialized 3

    And the following cards are rigged to be drawn in order:
      | cardId |
      # First quest stage draws (P2, P3, P4)

      | B15   |
      | B15   |
      | B15   |


      | L20   |
      | L20   |
      | L20   |
      | E30   |
      | E30   |
      | E30   |
      | E30   |
      | E30   |
      | E30   |
      # Event cards
      | D5    |
      | D5    |
      # Prosperity draws (all players)
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      # Queen's favor draws (P4)
      | L20   |
      | L20   |
      # Second quest stage draws (P2, P3, P4)
      | B15   |
      | B15   |
      | D5    |
      | L20   |
      | L20   |
      | E30   |
      | E30   |


    When P1 draws a quest card "Q4"
    And P1 accepts to sponsor the quest

    And P1 sets up the quest stages:
      | stage | cards        |
      | 1     | F5           |
      | 2     | F10          |
      | 3     | F15          |
      | 4     | F20          |


    And P2 agrees to participate in the quest
    And P3 agrees to participate in the quest
    And P4 agrees to participate in the quest

    And P2 draws a card for stage 1
    And P3 draws a card for stage 1
    And P4 draws a card for stage 1

    And P2 continues and attacks stage 1 with "D5"
    And P3 continues and attacks stage 1 with "D5"
    And P4 continues and attacks stage 1 with "D5"


    And P2 draws a card for stage 2
    And P3 draws a card for stage 2
    And P4 draws a card for stage 2

    And P2 continues and attacks stage 2 with "S10"
    And P3 continues and attacks stage 2 with "S10"
    And P4 continues and attacks stage 2 with "S10"



    And P2 draws a card for stage 3
    And P3 draws a card for stage 3
    And P4 draws a card for stage 3

    And P2 continues and attacks stage 3 with "B15"
    And P3 continues and attacks stage 3 with "B15"
    And P4 continues and attacks stage 3 with "B15"



    And P2 draws a card for stage 4
    And P3 draws a card for stage 4
    And P4 draws a card for stage 4

    And P2 continues and attacks stage 3 with "L20"
    And P3 continues and attacks stage 3 with "L20"
    And P4 continues and attacks stage 3 with "L20"
    And all quest responses are processed 3

    And P2 should have 4 shields
    And P2 should have 4 shields
    And P2 should have 4 shields




    And P2 draws an event card "plague"
    And Event card is handled
    And P2 should have 2 shields



    And P3 draws an event card "prosperity"
    And P1 presses enter
    And P2 presses enter
    And P3 presses enter
    And P4 presses enter
    And Event card is handled

    #they all draw 2 cards
    And P1's hand should have 7 cards
    And P2's hand should have 7 cards
    And P3's hand should have 7 cards
    And P4's hand should have 5 cards




    And P4 draws an event card "Qfavor"
    And P4 presses enter
    And Event card is handled
    And P4's hand should have 5 cards


    And the following cards are rigged to be drawn in order:
      | cardId |
      # First quest stage draws (P2, P3, P4)

      | B15   |
      | B15   |
      | B15   |


      | L20   |
      | L20   |
      | L20   |
      | E30   |
      | E30   |
      | E30   |
      | E30   |
      | E30   |
      | E30   |
      # Event cards
      | D5    |
      | D5    |
      # Prosperity draws (all players)
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      | B15   |
      # Queen's favor draws (P4)
      | L20   |
      | L20   |
      # Second quest stage draws (P2, P3, P4)
      | B15   |
      | B15   |
      | D5    |
      | L20   |
      | L20   |
      | E30   |
      | E30   |


    And P1 draws a quest card "Q3"
    And P1 accepts to sponsor the quest
    And P1 sets up the quest stages:
      | stage | cards |
      | 1     | F10   |
      | 2     | F15   |
      | 3     | F20   |

    And P2 agrees to participate in the quest
    And P3 agrees to participate in the quest
    And P4 agrees to participate in the quest

    And P2 draws a card for stage 1
    And P3 draws a card for stage 1
    And P4 draws a card for stage 1

    And P2 continues and attacks stage 1 with "S10"
    And P3 continues and attacks stage 1 with "S10"
    And P4 continues and attacks stage 1 with "D5"


    And P2 draws a card for stage 2
    And P3 draws a card for stage 2

    And P2 continues and attacks stage 2 with "B15"
    And P3 continues and attacks stage 2 with "B15"



    And P2 draws a card for stage 3
    And P3 draws a card for stage 3

    And P2 continues and attacks stage 3 with "L20"
    And P3 continues and attacks stage 3 with "L20"

    And all quest responses are processed 3


    Then P2 should have 5 shields
    And P3 should have 7 shields
    And P4 should have 4 shields



  Scenario: 0_winner_quest
    Given a new game is initialized with 4 players
    And player "P1"'s hand is initialized 4
    And player "P2"'s hand is initialized 4
    And player "P3"'s hand is initialized 4
    And player "P4"'s hand is initialized 4

    And the following cards are rigged to be drawn in order:
      | cardId |
      | F5     |
      | F5     |
      | F5     |
      | F5     |
      | F10    |
      | F15    |
      | F20    |


    And P1 draws a quest card "Q2"
    And P1 accepts to sponsor the quest

    And P1 sets up the quest stages:
      | stage | cards |
      | 1     | F10   |
      | 2     | F15   |


    And P2 agrees to participate in the quest
    And P3 agrees to participate in the quest
    And P4 agrees to participate in the quest

    And P2 draws a card for stage 1
    And P3 draws a card for stage 1
    And P4 draws a card for stage 1

    And P2 continues and attacks stage 1 with "D5"
    And P3 continues and attacks stage 1 with "D5"
    And P4 continues and attacks stage 1 with "D5"

    And P1 draws 4 cards
    And all quest responses are processed 4

    And the following cards are rigged to be drawn in order:
      | cardId |
      | F5     |
      | F5     |
      | F5     |
      | F5     |
      | F10    |
      | F15    |
      | F20    |

    Then P1 should have 0 shields
    And P1's hand should contain
      | cardId |
      | F5     |
      | F10    |
      | F15    |
      | F20    |

    And P2 should have 0 shields
    And P2's hand should contain
      | cardId |
      | F5     |
      | D5     |

    And P3 should have 0 shields
    And P3's hand should contain
      | cardId |
      | F5     |
      | D5     |


    And P4 should have 0 shields
    And P4's hand should contain
      | cardId |
      | F5     |
      | D5     |