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
    gameState = newState;
    updateGameDisplay();
    handleDrawnCard();
}



async function drawCard() {
    try {
        const response = await fetch('http://localhost:8080/api/game/playTurn', {
            method: 'GET'
        });
        gameState = await response.json();
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
    
    const currentPotentialSponsor = PLAYER_ORDER[currentSponsorshipIndex];
    localPlayerId = currentPotentialSponsor;

    // Check if we've gone full circle
    if (currentSponsorshipIndex === PLAYER_ORDER.indexOf(gameState.currentPlayerId)) {
        if (currentSponsorshipIndex !== 0) { // Not the first time asking
            handleNoSponsors();
            return;
        }
    }

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
    try {
        const response = await fetch('http://localhost:8080/api/game/endTurn', {
            method: 'POST'
        });
        const newState = await response.json();
        updateGameState(newState);
    } catch (error) {
        console.error('Error ending quest attempt:', error);
    }
}


async function respondToSponsorship(accepting) {
    if (accepting) {
        try {
            const response = await fetch(`http://localhost:8080/api/game/quest/sponsor?playerId=${localPlayerId}`, {
                method: 'POST'
            });
            
            const result = await response.json();
            if (!result.error) {
                currentSponsorshipIndex = 0;  // Reset for next quest
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
    // Hide the normal hand display
    document.getElementById('current-hand').style.display = 'none';
    
    actionArea.innerHTML = `
        <div class="quest-setup">
            <h3>Quest Stage ${currentStage}</h3>
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
    currentSponsorshipIndex = (currentSponsorshipIndex + 1) % PLAYER_ORDER.length;
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
        console.log("Stage Created with value: " + result.value);
        
        // Fetch current game state to get accurate quest information
        const gameStateResponse = await fetch('http://localhost:8080/api/game');
        const newState = await gameStateResponse.json();
        updateGameState(newState);
        
        // Check if we've completed all stages
        if (currentStage >= newState.currentQuest.stages) {
            // Quest setup complete
            currentStage = 1;
            currentQuest = null;
            selectedCards.clear();
            document.getElementById('current-hand').style.display = 'block';
            localPlayerId = PLAYER_ORDER[(PLAYER_ORDER.indexOf(newState.currentQuest.sponsorId) + 1) % PLAYER_ORDER.length];
            // Start participation phase for other players
            handleParticipationSequence();
            // updateGameState(newState);
        } else {
            // Setup next stage
            selectedCards.clear();
            currentStage++;
            handleQuestSetup();
        }
        
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
    console.log("Current stage being handled: " + currentStage); // Debug log
    console.log("Current player being asked: " + localPlayerId); // Debug log
    
    showParticipationPrompt();
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

        // Update participant's status based on attack result
        const participantStatus = questParticipants.get(localPlayerId);
        if (attackResult.stageCleared) {
            participantStatus.stagesCleared++;
            console.log(`${localPlayerId} cleared stage ${currentStage} (Total stages cleared: ${participantStatus.stagesCleared})`);
            
            if (participantStatus.stagesCleared === gameState.currentQuest.stages) {
                questWinners.push(localPlayerId);
                alert(`${localPlayerId} has completed all stages!`);
            }
        } else {
            participantStatus.active = false;
            console.log(`${localPlayerId} failed stage ${currentStage}`);
        }
        questParticipants.set(localPlayerId, participantStatus);

        // Get fresh game state
        const gameStateResponse = await fetch('http://localhost:8080/api/game');
        const newGameState = await gameStateResponse.json();
        gameState = newGameState;

        // Check if this was the final stage and player won
        if (attackResult.stageCleared && currentStage === gameState.currentQuest.stages) {
            questWinners.push(localPlayerId)
            alert(`${localPlayerId} has won the quest!`);
        }

        moveToNextActivePlayer();

    } catch (error) {
        console.error('Error submitting attack:', error);
        alert('Failed to submit attack. Please try again.');
    }
}

async function withdrawFromQuest() {
    try {
        const response = await fetch('http://localhost:8080/api/game/quest/withdraw', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                playerId: localPlayerId
            })
        });

        const result = await response.json();
        updateGameState(result);
    } catch (error) {
        console.error('Error withdrawing from quest:', error);
        alert('Failed to withdraw from quest. Please try again.');
    }
}


function moveToNextActivePlayer() {
    const sponsorId = gameState.currentQuest.sponsorId;
    const sponsorIndex = PLAYER_ORDER.indexOf(sponsorId);
    let currentIndex = PLAYER_ORDER.indexOf(localPlayerId);
    let nextIndex = (currentIndex + 1) % PLAYER_ORDER.length;

    console.log(`Moving from player ${localPlayerId} (index ${currentIndex}) to next player`);
    
    // Look for next active player or unasked player for current stage
    while (nextIndex !== sponsorIndex) {
        const nextPlayerId = PLAYER_ORDER[nextIndex];
        const participantStatus = questParticipants.get(nextPlayerId);
        
        // If this player hasn't been asked yet or is active and hasn't attempted current stage
        if (!participantStatus || (participantStatus.active && participantStatus.stagesCleared < currentStage)) {
            localPlayerId = nextPlayerId;
            handleParticipationSequence();
            return;
        }
        nextIndex = (nextIndex + 1) % PLAYER_ORDER.length;
    }
    
    // If we get here, we've asked all players for this stage
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
        // Start with first player after sponsor
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
            const shieldResponse = await fetch('http://localhost:8080/api/game/quest/addShield', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    playerIds: questWinners
                })
            });
            
            if (!shieldResponse.ok) {
                throw new Error('Failed to add shields to winners');
            }
            
            const updatedState = await shieldResponse.json();
            gameState = updatedState;
            alert(`Quest completed! Winners: ${questWinners.join(', ')}`);
        }

        // End the turn and get final state
        const endResponse = await fetch('http://localhost:8080/api/game/endTurn', {
            method: 'POST'
        });
        const finalState = await endResponse.json();
        
        // Reset quest-related variables
        currentStage = 1;
        questParticipants.clear();
        questWinners = [];
        
        // Update state and UI for next player's turn
        gameState = finalState;
        localPlayerId = finalState.currentPlayerId;
        drawButton.style.display = 'block';
        actionArea.innerHTML = '';
        
        // Update display with new state
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
        const response = await fetch('http://localhost:8080/api/game/handlePlague', {
            method: 'POST'
        });
        const newState = await response.json();
        updateGameState(newState);
    } catch (error) {
        console.error('Error handling plague:', error);
    }
}









/////////////////////////////////////HANDLE PROSPERITY AND QUEENS CARDS///////////////////////////////////////////////

async function handleQandP(){
    try {
        const reponse = await fetch('http://localhost:8080/api/game/drawCards', {
            method: 'POST',
            headers: {'Content-Type': 'application/json' }
        });
        const newState = await response.json();
        updateGameState(newState);
    } catch (error){
        console.error('Error handling Queens / prosperity card:', error);
    }
}









////////////////////////////////////////////Rendering Function///////////////////////////////////////////////////
function updateGameDisplay() {
    document.getElementById('adventure-deck-count').textContent = gameState.adventureDeckSize;
    document.getElementById('event-deck-count').textContent = gameState.eventDeckSize;
    
    // Always show current player
    drawnCard.innerHTML = `<h3>Current Player: ${gameState.currentPlayerId}</h3>`;
    
    const currentHandDisplay = document.getElementById('current-hand');
    
    // Show hand and draw button when there's no quest active
    if (!gameState.currentQuest && !gameState.pendingQuest) {
        currentHandDisplay.style.display = 'block';
        actionArea.innerHTML = `
            <button id="draw-button" onclick="drawCard()" class="button">Draw Card</button>
        `;  // Actually create the button element

        // Show the hand of the current player
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
    
    // Update turn/sponsor information
    if (gameState.pendingQuest) {
        let statusMessage = '';
        if (gameState.pendingQuest.type === 'QUEST') {
            if (gameState.currentQuest) {
                statusMessage = `Quest in progress - ${gameState.currentQuest.stages} stages`;
            } else {
                const currentPotentialSponsor = PLAYER_ORDER[currentSponsorshipIndex];
                statusMessage = `Waiting for Player ${currentPotentialSponsor} to decide on sponsorship`;
            }
        }
        gameStatus.textContent = statusMessage;
    } else {
        gameStatus.textContent = `Game Status: ${gameState.gameStatus}`;
    }
}

