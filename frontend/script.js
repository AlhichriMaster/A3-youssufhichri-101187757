// DOM Elements
const drawButton = document.getElementById('draw-button');
const drawnCard = document.getElementById('drawn-card');
const actionArea = document.getElementById('action-area');
const handCards = document.getElementById('hand-cards');
const trimHandModal = document.getElementById('trim-hand-modal');
const gameStatus = document.getElementById('game-status');

// Event Listeners
drawButton.addEventListener('click', drawCard);


let gameState = {}

let currentQuest = null;
let currentStage = 1;
let selectedCards = new Set();

let localPlayerId = null;
let currentSponsorshipIndex = 0;
const PLAYER_ORDER = ['P1', 'P2', 'P3', 'P4'];
let questParticipants = new Map();
let questWinners = []


// Initial setup
document.addEventListener('DOMContentLoaded', async () => {
    try {
        const response = await fetch('http://localhost:8080/api/game');
        const data = await response.json();
        gameState = data;
        localPlayerId = gameState.currentPlayerId;
        drawButton.style.display = 'block';  // Explicitly show draw button on load
        updateGameDisplay();
    } catch (error) {
        console.error('Error initializing game:', error);
    }
});



function updateGameState(newState) {
    const previousQuest = gameState.currentQuest;
    gameState = newState;
    
    // If we're in the middle of quest setup, don't change the localPlayerId
    if (previousQuest && previousQuest.sponsorId && 
        gameState.currentQuest && 
        currentStage > 0 && currentStage <= gameState.currentQuest.stages) {
        // Keep localPlayerId as the sponsor
        localPlayerId = gameState.currentQuest.sponsorId;
    }
    
    if (!checkHandSizes()) {
        updateGameDisplay();
        handleDrawnCard();
    }
}



async function drawCard() {
    try {
        const response = await fetch('http://localhost:8080/api/game/playTurn', {
            method: 'GET'
        });
        gameState = await response.json();
        
        // Check for game over before proceeding
        if (gameState.gameStatus === 'FINISHED') {
            updateGameDisplay();
            return;
        }
        
        handleDrawnCard();
    } catch (error) {
        console.error('Error drawing card:', error);
    }
}


async function handleDrawnCard() {
    if (!gameState.pendingQuest) return;

    drawButton.style.display = 'none';
    drawnCard.innerHTML = `
        <h3>Drawn Card:</h3>
        <p>Type: ${gameState.pendingQuest.type}</p>
        <p>Stages: ${gameState.pendingQuest.id}</p>
    `;

    switch (gameState.pendingQuest.type) {
        case 'QUEST':
            handleQuestCard();
            break;
        case 'PLAGUE':
            handlePlagueCard();
            break;
        case 'QUEENS_FAVOR':
            handleQandP();
            break;
        case 'PROSPERITY':
            handleQandP();
            break;
    }
}



///////////////////////////////////////////////// HANDLING QUEST CARDS //////////////////////////////////////////////////////
async function handleQuestCard() {
    if (currentSponsorshipIndex === 0) {
        currentSponsorshipIndex = PLAYER_ORDER.indexOf(gameState.currentPlayerId);
    }
    
    const startingIndex = PLAYER_ORDER.indexOf(gameState.currentPlayerId);
    const currentIndex = currentSponsorshipIndex % PLAYER_ORDER.length;
    
    // If we've made it back to the starting player
    if (currentIndex === startingIndex && currentSponsorshipIndex > startingIndex) {
        console.log("No sponsors found, ending turn");
        await handleNoSponsors();
        return;
    }

    const currentPotentialSponsor = PLAYER_ORDER[currentIndex];
    localPlayerId = currentPotentialSponsor;
    displaySponsorshipPrompt(currentPotentialSponsor);
}

function displaySponsorshipPrompt(playerId) {
    actionArea.innerHTML = `
        <div class="alert alert-info">
            Player ${playerId}, would you like to sponsor this quest?
        </div>
        <button onclick="respondToSponsorship(true)" class="button">Accept</button>
        <button onclick="respondToSponsorship(false)" class="button secondary">Decline</button>
    `;
}

async function handleNoSponsors() {
    currentSponsorshipIndex = 0; // Reset for next quest
    console.log("We should be calling end turn here")
    try {
        const response = await fetch('http://localhost:8080/api/game/endTurn', {
            method: 'POST'
        });
        const newState = await response.json();
        // Update game state and move to next player
        updateGameState(newState);
        localPlayerId = newState.currentPlayerId;
        // Show draw button for next player
        drawButton.style.display = 'block';
        actionArea.innerHTML = `
            <button id="draw-button" onclick="drawCard()" class="button">Draw Card</button>
        `;
    } catch (error) {
        console.error('Error ending quest attempt:', error);
    }
}


async function respondToSponsorship(accepting) {
    if (accepting) {
        try {
            console.log("Attempting to sponsor quest with player:", localPlayerId);
            
            const response = await fetch(`http://localhost:8080/api/game/quest/sponsor?playerId=${localPlayerId}`, {
                method: 'POST'
            });
            
            if (!response.ok) {
                throw new Error(`HTTP error! status: ${response.status}`);
            }
            
            const result = await response.json();
            console.log("Sponsorship response:", result);

            // Get updated game state after sponsorship
            const gameStateResponse = await fetch('http://localhost:8080/api/game');
            const newState = await gameStateResponse.json();
            console.log("Game state after sponsorship:", newState);
            
            if (!result.error) {
                currentSponsorshipIndex = 0;  // Reset for next quest
                gameState = newState;  // Update game state before setup
                handleQuestSetup();
            } else {
                console.error('Error in sponsorship:', result.error);
                moveToNextSponsor();
            }
        } catch (error) {
            console.error('Error in sponsorship response:', error);
            moveToNextSponsor();
        }
    } else {
        moveToNextSponsor();
    }
}


async function handleQuestSetup() {
    console.log(`Setting up quest stage ${currentStage} of ${gameState.currentQuest.stages}`);
    
    // Hide the normal hand display
    document.getElementById('current-hand').style.display = 'none';
    
    actionArea.innerHTML = `
        <div class="quest-setup">
            <h3>Quest Stage ${currentStage} of ${gameState.currentQuest.stages}</h3>
            <div class="selected-cards">
                <h4>Selected Cards:</h4>
                <div id="selected-cards-display"></div>
            </div>
            <div class="card-selection">
                <h4>Your Hand:</h4>
                <div id="quest-hand-cards" class="cards"></div>
            </div>
            <button onclick="confirmStage()" class="button">Confirm Stage</button>
            <button onclick="cancelStage()" class="button secondary">Cancel</button>
        </div>
    `;
    
    displayQuestHand();
}


function moveToNextSponsor() {
    currentSponsorshipIndex++;  // Just increment without modulo
    handleQuestCard();
}


function displayQuestHand() {
    const questHandCards = document.getElementById('quest-hand-cards');
    const localPlayer = gameState.players.find(p => p.id === localPlayerId);
    
    if (localPlayer) {
        questHandCards.innerHTML = localPlayer.hand.map(card => `
            <div class="card ${selectedCards.has(card.id) ? 'selected' : ''}" 
                 onclick="toggleCardSelection('${card.id}')">
                <h4>${card.id}</h4>
                <p>Type: ${card.type}</p>
                <p>Value: ${card.value}</p>
            </div>
        `).join('');
    }
    
    updateSelectedCardsDisplay();
}


function updateSelectedCardsDisplay() {
    const selectedCardsDisplay = document.getElementById('selected-cards-display');
    const selectedCardsList = Array.from(selectedCards);
    
    selectedCardsDisplay.innerHTML = selectedCardsList.length > 0 
        ? selectedCardsList.map(cardId => `<span class="selected-card">${cardId}</span>`).join('')
        : '<p>No cards selected</p>';
}


function toggleCardSelection(cardId) {
    const localPlayer = gameState.players.find(p => p.id === localPlayerId);
    const card = localPlayer.hand.find(c => c.id === cardId);
    
    // If trying to select a foe card
    if (card.type === 'FOE') {
        // Check if we already have a foe card selected
        const hasFoeCard = Array.from(selectedCards).some(selectedId => {
            const selectedCard = localPlayer.hand.find(c => c.id === selectedId);
            return selectedCard.type === 'FOE';
        });

        if (hasFoeCard && !selectedCards.has(cardId)) {
            alert('Only one foe card can be selected per stage');
            return;
        }
    }

    // Toggle selection as normal
    if (selectedCards.has(cardId)) {
        selectedCards.delete(cardId);
    } else {
        selectedCards.add(cardId);
    }
    
    displayQuestHand();
}


async function confirmStage() {
    console.log("Current Stage:", currentStage);
    console.log("Total Quest Stages:", gameState.currentQuest.stages);

    if (selectedCards.size === 0) {
        alert('Please select at least one card for the stage');
        return;
    }

    try {
        const response = await fetch('http://localhost:8080/api/game/quest/setup-stage', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                cardIds: Array.from(selectedCards),
                stageNumber: currentStage
            })
        });

        const result = await response.json();
        console.log("Stage Setup Result:", result);
        
        // Fetch current game state to get accurate quest information
        const gameStateResponse = await fetch('http://localhost:8080/api/game');
        const newState = await gameStateResponse.json();
        gameState = newState;  // Update the game state

        // Check if we've completed all stages
        if (currentStage >= gameState.currentQuest.stages) {
            // Quest setup complete
            console.log("Quest setup complete, moving to participation phase");
            currentStage = 1;
            selectedCards.clear();
            document.getElementById('current-hand').style.display = 'block';
            localPlayerId = PLAYER_ORDER[(PLAYER_ORDER.indexOf(gameState.currentQuest.sponsorId) + 1) % PLAYER_ORDER.length];
            handleParticipationSequence();
        } else {
            // Setup next stage
            console.log(`Moving to stage ${currentStage + 1} of ${gameState.currentQuest.stages}`);
            selectedCards.clear();
            currentStage++;
            handleQuestSetup();
        }
        
        updateGameDisplay();
    } catch (error) {
        console.error('Error setting up quest stage:', error);
        alert('Failed to set up stage. Please try again.');
    }
}

function cancelStage() {
    selectedCards.clear();
    currentStage = 1;
    currentQuest = null;
    updateGameDisplay();
}






////////////////////////////////////////////HANDLING QUEST PARTICIPATION////////////////////////////////////////////////
async function handleParticipationSequence() {
    // Clear previous UI
    drawnCard.innerHTML = '';
    
    // Get next non-sponsor player
    const sponsorId = gameState.currentQuest.sponsorId;
    console.log("Current stage being handled: " + currentStage);
    console.log("Current player being asked: " + localPlayerId);
    
    // Check if player has already participated
    const participantStatus = questParticipants.get(localPlayerId);
    if (participantStatus && participantStatus.active) {
        // If they're an active participant, go straight to attack
        handleQuestAttack();
    } else {
        // If they haven't participated yet, show the prompt
        showParticipationPrompt();
    }
}

function showParticipationPrompt() {
    actionArea.innerHTML = `
        <div class="quest-participation">
            <h3>Quest Participation</h3>
            <div class="quest-info">
                <p>Player ${localPlayerId}, would you like to participate in this quest?</p>
            </div>
            <button onclick="respondToParticipation(true)" class="button">Join Quest</button>
            <button onclick="respondToParticipation(false)" class="button secondary">Decline</button>
        </div>
    `;
}

async function respondToParticipation(joining) {
    try {
        const response = await fetch(`http://localhost:8080/api/game/quest/participate?playerId=${localPlayerId}`, {
            method: 'POST'
        });

        const result = await response.json();
        
        if (joining) {
            // Initialize participant tracking when they join
            questParticipants.set(localPlayerId, {
                active: true,
                stagesCleared: 0
            });
            selectedCards.clear();
            handleQuestAttack();
        } else {
            // Mark player as inactive when they decline to participate
            questParticipants.set(localPlayerId, {
                active: false,
                stagesCleared: 0
            });
            moveToNextActivePlayer();
        }
    } catch (error) {
        console.error('Error responding to quest participation:', error);
        alert('Failed to respond to quest participation. Please try again.');
    }
}


function handleQuestAttack() {
    actionArea.innerHTML = `
        <div class="quest-attack">
            <h3>Stage ${currentStage} Attack</h3>
            <div class="quest-info">
                <p>Current Player: ${localPlayerId}</p>
            </div>
            <div class="selected-cards">
                <h4>Selected Weapons:</h4>
                <div id="selected-attack-cards"></div>
            </div>
            <div class="card-selection">
                <h4>Your Hand:</h4>
                <div id="attack-hand-cards" class="cards"></div>
            </div>
            <button onclick="confirmAttack()" class="button">Confirm Attack</button>
            <button onclick="withdrawFromQuest()" class="button secondary">Withdraw</button>
        </div>
    `;

    displayAttackHand();
}



function displayAttackHand() {
    const attackHandCards = document.getElementById('attack-hand-cards');
    const localPlayer = gameState.players.find(p => p.id === localPlayerId);
    
    if (localPlayer) {
        attackHandCards.innerHTML = localPlayer.hand.map(card => `
            <div class="card ${selectedCards.has(card.id) ? 'selected' : ''}" 
                 onclick="toggleAttackCard('${card.id}')">
                <h4>${card.id}</h4>
                <p>Type: ${card.type}</p>
                <p>Value: ${card.value}</p>
            </div>
        `).join('');
    }
    
    updateSelectedAttackCards();
}



function toggleAttackCard(cardId) {
    const localPlayer = gameState.players.find(p => p.id === localPlayerId);
    const card = localPlayer.hand.find(c => c.id === cardId);
    
    // Only allow weapon cards for attack
    if (card.type !== 'WEAPON') {
        alert('Only weapon cards can be used in an attack');
        return;
    }

    if (selectedCards.has(cardId)) {
        selectedCards.delete(cardId);
    } else {
        selectedCards.add(cardId);
    }
    
    displayAttackHand();
}

function updateSelectedAttackCards() {
    const selectedCardsDisplay = document.getElementById('selected-attack-cards');
    const selectedCardsList = Array.from(selectedCards);
    
    selectedCardsDisplay.innerHTML = selectedCardsList.length > 0 
        ? selectedCardsList.map(cardId => `<span class="selected-card">${cardId}</span>`).join('')
        : '<p>No cards selected</p>';
}

async function confirmAttack() {
    try {
        let participantStatus = questParticipants.get(localPlayerId);
        if (!participantStatus || !participantStatus.active) {
            alert('You must be a quest participant to attack');
            return;
        }

        console.log("Player attacking stage " + currentStage + ": " + localPlayerId);
        const response = await fetch('http://localhost:8080/api/game/quest/attack', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                playerId: localPlayerId,
                cardIds: Array.from(selectedCards),
                stageNumber: currentStage
            })
        });

        const attackResult = await response.json();
        selectedCards.clear();

        // Get the existing participant status, maintaining its stages cleared count
        participantStatus = questParticipants.get(localPlayerId);
        const currentStagesCleared = participantStatus ? participantStatus.stagesCleared : 0;
        console.log(`Before attack - ${localPlayerId} has cleared ${currentStagesCleared} stages`);

        if (attackResult.stageCleared) {
            participantStatus = {
                active: true,
                stagesCleared: currentStagesCleared + 1  // Increment from existing count
            };
            console.log(`${localPlayerId} cleared stage ${currentStage} (Total stages cleared: ${participantStatus.stagesCleared})`);
            
            // Check if player has completed all stages
            if (participantStatus.stagesCleared === gameState.currentQuest.stages && participantStatus.active) {
                console.log(`${localPlayerId} has completed all stages!`);
                if (localPlayerId !== gameState.currentQuest.sponsorId) {
                    questWinners.push(localPlayerId);
                    alert(`${localPlayerId} has completed all stages!`);
                }
            }
        } else {
            participantStatus = {
                active: false,
                stagesCleared: currentStagesCleared  // Maintain existing count even on failure
            };
            console.log(`${localPlayerId} failed stage ${currentStage}`);
        }
        
        questParticipants.set(localPlayerId, participantStatus);
        console.log(`After attack - ${localPlayerId} updated status:`, questParticipants.get(localPlayerId));

        // Get fresh game state
        const gameStateResponse = await fetch('http://localhost:8080/api/game');
        const newGameState = await gameStateResponse.json();
        gameState = newGameState;

        moveToNextActivePlayer();

    } catch (error) {
        console.error('Error submitting attack:', error);
        alert('Failed to submit attack. Please try again.');
    }
}

async function withdrawFromQuest() {
    try {
        // Mark player as inactive when they withdraw
        questParticipants.set(localPlayerId, {
            active: false,
            stagesCleared: questParticipants.get(localPlayerId)?.stagesCleared || 0
        });
        
        const response = await fetch(`http://localhost:8080/api/game/quest/withdraw?playerId=${localPlayerId}`, {
            method: 'POST'
        });

        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }

        const result = await response.json();
        
        // Only update game state if we got a valid response
        if (result) {
            gameState = result;
            moveToNextActivePlayer();
        }
    } catch (error) {
        console.error('Error withdrawing from quest:', error);
        alert('Failed to withdraw from quest. Please try again.');
    }
}


async function moveToNextActivePlayer() {
    const sponsorId = gameState.currentQuest.sponsorId;
    const sponsorIndex = PLAYER_ORDER.indexOf(sponsorId);
    let currentIndex = PLAYER_ORDER.indexOf(localPlayerId);
    let nextIndex = (currentIndex + 1) % PLAYER_ORDER.length;

    console.log(`Moving from player ${localPlayerId} (index ${currentIndex}) to next player`);
    console.log("Current participant statuses:", Object.fromEntries(questParticipants));
    
    let checkedAllPlayers = false;
    while (!checkedAllPlayers) {
        const nextPlayerId = PLAYER_ORDER[nextIndex];
        const participantStatus = questParticipants.get(nextPlayerId);

        // Skip if sponsor or if player has already declined/failed/withdrawn
        if (nextIndex === sponsorIndex || (participantStatus && !participantStatus.active)) {
            nextIndex = (nextIndex + 1) % PLAYER_ORDER.length;
            if (nextIndex === currentIndex) checkedAllPlayers = true;
            continue;
        }

        // If player hasn't been asked yet or is active and hasn't attempted current stage
        if (!participantStatus || (participantStatus.active && participantStatus.stagesCleared < currentStage)) {
            localPlayerId = nextPlayerId;
            handleParticipationSequence();
            return;
        }

        nextIndex = (nextIndex + 1) % PLAYER_ORDER.length;
        if (nextIndex === currentIndex) checkedAllPlayers = true;
    }
    
    // If we get here, we've checked all players
    // Check if any players are still active
    let hasActivePlayers = false;
    questParticipants.forEach((status, playerId) => {
        if (status.active) {
            hasActivePlayers = true;
        }
    });

    if (!hasActivePlayers) {
        // No active players left, end the quest
        console.log('No active players remaining, ending quest');
        endQuest();
    } else if (currentStage < gameState.currentQuest.stages) {
        // Still have active players and more stages to go
        currentStage++;
        console.log(`Moving to stage ${currentStage}`);
        // Start with first player after sponsor for next stage
        localPlayerId = PLAYER_ORDER[(sponsorIndex + 1) % PLAYER_ORDER.length];
        handleParticipationSequence();
    } else {
        // All stages complete
        console.log('All stages complete, ending quest');
        endQuest();
    }
}


async function endQuest() {
    try {        
        if (questWinners.length > 0) {
            console.log("Quest winners before shield award:", questWinners);
            const sponsorId = gameState.currentQuest.sponsorId;
            const finalWinners = questWinners.filter(winnerId => winnerId !== sponsorId);
            
            const shieldResponse = await fetch('http://localhost:8080/api/game/quest/addShield', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    playerIds: finalWinners
                })
            });
            
            if (!shieldResponse.ok) {
                throw new Error('Failed to add shields to winners');
            }
            
            const updatedState = await shieldResponse.json();
            gameState = updatedState;
            
            // Check if game is over after adding shields
            if (gameState.gameStatus === 'FINISHED') {
                updateGameDisplay();
                return;
            }
            
            alert(`Quest completed! Winners: ${finalWinners.join(', ')}`);
        }

        // Add completion call to reward sponsor
        const completionResponse = await fetch('http://localhost:8080/api/game/quest/complete', {
            method: 'POST'
        });
        
        if (!completionResponse.ok) {
            throw new Error('Failed to process quest completion');
        }
        
        newState = await completionResponse.json();
        updateGameState(newState);


        // Continue with regular end quest logic
        const endResponse = await fetch('http://localhost:8080/api/game/endTurn', {
            method: 'POST'
        });
        const finalState = await endResponse.json();
        
        currentStage = 1;
        questParticipants.clear();
        questWinners = [];
        
        gameState = finalState;
        localPlayerId = finalState.currentPlayerId;
        drawButton.style.display = 'block';
        actionArea.innerHTML = '';
        
        updateGameDisplay();
    } catch (error) {
        console.error('Error ending quest:', error);
        alert('Failed to properly end quest. Please try again.');
    }
}







///////////////////////////////////////////////// HANDLING PLAGUE CARDS //////////////////////////////////////////////////////
async function handlePlagueCard() {
    actionArea.innerHTML = `
        <div class="alert alert-warning">
            Plague card drawn! You lose 2 shields.
        </div>
        <button onclick="handlePlague()" class="button">Continue</button>
    `;
}

async function handlePlague() {
    try {
        // Handle the plague effect
        const response = await fetch('http://localhost:8080/api/game/handlePlague', {
            method: 'POST'
        });
        const newState = await response.json();
        
        // End the turn
        const endResponse = await fetch('http://localhost:8080/api/game/endTurn', {
            method: 'POST'
        });
        const finalState = await endResponse.json();
        
        // Update game state and move to next player
        gameState = finalState;
        localPlayerId = finalState.currentPlayerId;
        
        // Reset UI
        drawButton.style.display = 'block';
        actionArea.innerHTML = `
            <button id="draw-button" onclick="drawCard()" class="button">Draw Card</button>
        `;
        drawnCard.innerHTML = '';
        
        // Update display with new state
        updateGameDisplay();
    } catch (error) {
        console.error('Error handling plague:', error);
        alert('Failed to handle plague card. Please try again.');
    }
}









/////////////////////////////////////HANDLE PROSPERITY AND QUEENS CARDS///////////////////////////////////////////////
async function handleQandP() {
    try {
        const response = await fetch('http://localhost:8080/api/game/handleQandP', {
            method: 'POST',
            headers: {'Content-Type': 'application/json'}
        });
        const newState = await response.json();
        updateGameState(newState);
    } catch (error) {
        console.error('Error handling Queens / prosperity card:', error);
    }
}





/////////////////////////////////////////////////Hand Trimming///////////////////////////////////////////////
function checkHandSizes() {
    const playerWithLargeHand = gameState.players.find(player => player.hand.length > 12);
    if (playerWithLargeHand) {
        localPlayerId = playerWithLargeHand.id;
        handleHandTrimming(playerWithLargeHand);
        return true;
    }
    return false;
}

function handleHandTrimming(player) {
    // Hide normal game elements
    drawButton.style.display = 'none';
    document.getElementById('current-hand').style.display = 'none';
    
    // Show trimming interface
    actionArea.innerHTML = `
        <div class="hand-trimming">
            <h3>Trim Hand - Player ${player.id}</h3>
            <p>Select ${player.hand.length - 12} cards to discard</p>
            <div class="selected-cards">
                <h4>Selected to Discard:</h4>
                <div id="selected-cards-display"></div>
            </div>
            <div class="card-selection">
                <h4>Your Hand:</h4>
                <div id="trim-hand-cards" class="cards"></div>
            </div>
            <button onclick="confirmTrim()" class="button">Confirm Discard</button>
        </div>
    `;
    
    displayTrimHand();
}

function displayTrimHand() {
    const trimHandCards = document.getElementById('trim-hand-cards');
    const localPlayer = gameState.players.find(p => p.id === localPlayerId);
    
    if (localPlayer) {
        trimHandCards.innerHTML = localPlayer.hand.map(card => `
            <div class="card ${selectedCards.has(card.id) ? 'selected' : ''}" 
                 onclick="toggleTrimCard('${card.id}')">
                <h4>${card.id}</h4>
                <p>Type: ${card.type}</p>
                <p>Value: ${card.value}</p>
            </div>
        `).join('');
    }
    
    updateSelectedCardsDisplay();
}

function toggleTrimCard(cardId) {
    const localPlayer = gameState.players.find(p => p.id === localPlayerId);
    const requiredDiscards = localPlayer.hand.length - 12;
    
    if (selectedCards.has(cardId)) {
        selectedCards.delete(cardId);
    } else if (selectedCards.size < requiredDiscards) {
        selectedCards.add(cardId);
    } else {
        alert(`You must discard exactly ${requiredDiscards} cards`);
        return;
    }
    
    displayTrimHand();
}

async function confirmTrim() {
    const localPlayer = gameState.players.find(p => p.id === localPlayerId);
    const requiredDiscards = localPlayer.hand.length - 12;
    
    if (selectedCards.size !== requiredDiscards) {
        alert(`You must select exactly ${requiredDiscards} cards to discard`);
        return;
    }

    try {
        // First discard the cards
        const response = await fetch('http://localhost:8080/api/game/discardCards', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                playerId: localPlayerId,
                cardIds: Array.from(selectedCards)
            })
        });

        let newState = await response.json();
        selectedCards.clear();
        
        // Update game state and check if any other players need to trim
        gameState = newState;
        if (!checkHandSizes()) {
            // If no more trimming needed, end the turn and move to next player
            const endResponse = await fetch('http://localhost:8080/api/game/endTurn', {
                method: 'POST'
            });
            const finalState = await endResponse.json();
            
            // Update state and UI for next player's turn
            gameState = finalState;
            localPlayerId = finalState.currentPlayerId;
            document.getElementById('current-hand').style.display = 'block';
            drawButton.style.display = 'block';
            actionArea.innerHTML = '';
            
            // Create new draw button for next player
            const newDrawButton = document.createElement('button');
            newDrawButton.id = 'draw-button';
            newDrawButton.className = 'button';
            newDrawButton.textContent = 'Draw Card';
            newDrawButton.addEventListener('click', drawCard);
            actionArea.appendChild(newDrawButton);
        }
        updateGameDisplay();
    } catch (error) {
        console.error('Error trimming hand:', error);
        alert('Failed to discard cards. Please try again.');
    }
}




////////////////////////////////////////////Rendering Function///////////////////////////////////////////////////
function updatePlayerStats() {
    const statsContainer = document.getElementById('player-stats-container');
    statsContainer.innerHTML = '';

    gameState.players.forEach(player => {
        const isCurrentPlayer = player.id === gameState.currentPlayerId;
        const playerCard = document.createElement('div');
        playerCard.className = `player-stat-card ${isCurrentPlayer ? 'current-player' : ''}`;
        
        playerCard.innerHTML = `
            <div class="stat-row">
                <strong>Player ${player.id}</strong>
                ${isCurrentPlayer ? ' (Current Turn)' : ''}
            </div>
            <div class="stat-row">
                <span>Shields:</span>
                <span>${player.shields}</span>
            </div>
            <div class="stat-row">
                <span>Hand Size:</span>
                <span>${player.hand.length} cards</span>
            </div>
        `;
        
        statsContainer.appendChild(playerCard);
    });
}

function handleGameOver() {
    // Hide regular game elements
    drawButton.style.display = 'none';
    document.getElementById('current-hand').style.display = 'none';
    
    // Find winner(s)
    const winners = gameState.players.filter(player => player.shields >= 7);
    
    // Create game over display
    actionArea.innerHTML = `
        <div class="game-over">
            <h2>Game Over!</h2>
            ${winners.map(winner => `
                <div class="winner">
                    <h3>Winner: Player ${winner.id}</h3>
                    <p>Final Shield Count: ${winner.shields}</p>
                </div>
            `).join('')}
            <div class="final-standings">
                <h3>Final Standings</h3>
                ${gameState.players
                    .sort((a, b) => b.shields - a.shields)
                    .map(player => `
                        <div class="player-standing">
                            <span>Player ${player.id}:</span>
                            <span>${player.shields} shields</span>
                        </div>
                    `).join('')}
            </div>
        </div>
    `;
    
    // Update status message
    gameStatus.textContent = 'Game Over - Winner Found!';
    
    // Clear drawn card area
    drawnCard.innerHTML = '';
}


function updateGameDisplay() {
    document.getElementById('adventure-deck-count').textContent = gameState.adventureDeckSize;
    document.getElementById('event-deck-count').textContent = gameState.eventDeckSize;
    
    updatePlayerStats();
    
    // Check for game over state first
    if (gameState.gameStatus === 'FINISHED') {
        handleGameOver();
        return;  // Don't proceed with normal game display
    }
    
    drawnCard.innerHTML = `<h3>Current Player: ${gameState.currentPlayerId}</h3>`;
    
    const currentHandDisplay = document.getElementById('current-hand');
    
    if (!gameState.currentQuest && !gameState.pendingQuest) {
        currentHandDisplay.style.display = 'block';
        actionArea.innerHTML = '';  // Clear existing content
        const newDrawButton = document.createElement('button');
        newDrawButton.id = 'draw-button';
        newDrawButton.className = 'button';
        newDrawButton.textContent = 'Draw Card';
        newDrawButton.addEventListener('click', drawCard);
        actionArea.appendChild(newDrawButton);

        const localPlayer = gameState.players.find(p => p.id === localPlayerId);
        if (localPlayer) {
            handCards.innerHTML = localPlayer.hand.map(card => `
                <div class="card">
                    <h4>${card.id}</h4>
                    <p>Type: ${card.type}</p>
                    <p>Value: ${card.value}</p>
                </div>
            `).join('');
        }
    } else {
        currentHandDisplay.style.display = 'none';
    }
    
    // Update game status message
    if (gameState.pendingQuest) {
        let statusMessage = '';
        if (gameState.pendingQuest.type === 'QUEST') {
            if (gameState.currentQuest) {
                statusMessage = `Quest in progress - Stage ${currentStage} of ${gameState.currentQuest.stages} stages`;
            } else {
                const currentPotentialSponsor = PLAYER_ORDER[currentSponsorshipIndex % PLAYER_ORDER.length];
                statusMessage = `Waiting for Player ${currentPotentialSponsor} to decide on sponsorship`;
            }
        }
        gameStatus.textContent = statusMessage;
    } else {
        gameStatus.textContent = `Game Status: ${gameState.gameStatus}`;
    }
}