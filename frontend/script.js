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
let declinedSponsors = new Set();
let turnQuestWinners = new Set();


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
        turnQuestWinners.clear();
        
        const response = await fetch('http://localhost:8080/api/game/playTurn', {
            method: 'GET'
        });
        gameState = await response.json();
        
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
    // Reset quest-related state when starting a new quest
    if (!gameState.currentQuest) {
        currentStage = 1;
        questParticipants.clear();
        questWinners = [];
        declinedSponsors.clear();  // Clear declined sponsors for new quest
    }
    
    if (currentSponsorshipIndex === 0) {
        currentSponsorshipIndex = PLAYER_ORDER.indexOf(gameState.currentPlayerId);
    }
    
    const startingIndex = PLAYER_ORDER.indexOf(gameState.currentPlayerId);
    let currentIndex = currentSponsorshipIndex % PLAYER_ORDER.length;
    
    // Skip any declined sponsors
    while (declinedSponsors.has(PLAYER_ORDER[currentIndex])) {
        currentSponsorshipIndex++;
        currentIndex = currentSponsorshipIndex % PLAYER_ORDER.length;
        
        // If we've cycled back to start, no sponsors available
        if (currentIndex === startingIndex || currentSponsorshipIndex >= PLAYER_ORDER.length * 2) {
            console.log("No sponsors found (all declined), ending turn");
            await handleNoSponsors();
            return;
        }
    }
    
    // If we've made it back to the starting player
    if (currentIndex === startingIndex && currentSponsorshipIndex > startingIndex) {
        console.log("No sponsors found, ending turn");
        await handleNoSponsors();
        return;
    }

    // Get fresh game state
    try {
        const response = await fetch('http://localhost:8080/api/game');
        const freshState = await response.json();
        gameState = freshState;
    } catch (error) {
        console.error('Error getting fresh game state:', error);
    }

    const currentPotentialSponsor = PLAYER_ORDER[currentIndex];
    localPlayerId = currentPotentialSponsor;
    console.log(`Asking ${localPlayerId} to sponsor quest (declined sponsors: ${Array.from(declinedSponsors)})`);
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
                // Reset quest-related state
                currentStage = 1;
                currentSponsorshipIndex = 0;
                gameState = newState;
                
                console.log(`Starting new quest setup with ${gameState.currentQuest.stages} stages`);
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
    
    // Double check we have correct stage count
    if (currentStage > gameState.currentQuest.stages) {
        console.error("Invalid stage count detected");
        currentStage = 1;
    }
    
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
    console.log("WE ACTUALLY GOT INTO MOVE, DECLINE BUTTON WAS ACTUALLY CLICKED");
    
    // Add current player to declined sponsors
    declinedSponsors.add(localPlayerId);
    console.log(`Added ${localPlayerId} to declined sponsors`);
    
    currentSponsorshipIndex++;
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
            //alert('Only one foe card can be selected per stage');
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
        //alert('Please select at least one card for the stage');
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
        //alert('Failed to set up stage. Please try again.');
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
    drawnCard.innerHTML = '';
    
    const sponsorId = gameState.currentQuest.sponsorId;
    const participantStatus = questParticipants.get(localPlayerId);
    
    if (participantStatus && participantStatus.active) {
        // Always draw a card at the start of each stage for active participants
        await handleStageStart();
    } else {
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
        if (!joining) {
            const response = await fetch(`http://localhost:8080/api/game/quest/participate?playerId=${localPlayerId}`, {
                method: 'POST'
            });
            const result = await response.json();
            questParticipants.set(localPlayerId, {
                active: false,
                stagesCleared: 0
            });
            moveToNextActivePlayer();
            return;
        }

        const response = await fetch(`http://localhost:8080/api/game/quest/participate?playerId=${localPlayerId}`, {
            method: 'POST'
        });
        const result = await response.json();

        questParticipants.set(localPlayerId, {
            active: true,
            stagesCleared: 0
        });

        await handleStageStart();

    } catch (error) {
        console.error('Error in participation flow:', error);
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
        //alert('Only weapon cards can be used in an attack');
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
            return;
        }

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

        await handleStagesCleared(attackResult);

        const gameStateResponse = await fetch('http://localhost:8080/api/game');
        const newGameState = await gameStateResponse.json();
        gameState = newGameState;

        moveToNextActivePlayer();

    } catch (error) {
        console.error('Error submitting attack:', error);
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
        //alert('Failed to withdraw from quest. Please try again.');
    }
}


async function handleStageStart() {
    const drawResponse = await fetch(`http://localhost:8080/api/game/quest/drawParticipationCard?playerId=${localPlayerId}`, {
        method: 'POST'
    });
    const newState = await drawResponse.json();
    gameState = newState;

    if (gameState.players.find(p => p.id === localPlayerId).hand.length > 12) {
        handleHandTrimming(gameState.players.find(p => p.id === localPlayerId));
    } else {
        handleQuestAttack();
    }
}

async function handleStagesCleared(attackResult) {
    const participantStatus = questParticipants.get(localPlayerId);
    const currentStagesCleared = participantStatus ? participantStatus.stagesCleared : 0;

    if (attackResult.stageCleared) {
        const updatedStatus = {
            active: true,
            stagesCleared: currentStagesCleared + 1
        };
        questParticipants.set(localPlayerId, updatedStatus);
        
        if (updatedStatus.stagesCleared === gameState.currentQuest.stages && 
            updatedStatus.active && 
            localPlayerId !== gameState.currentQuest.sponsorId) {
            questWinners.push(localPlayerId);
            console.log(`${localPlayerId} completed quest and added to winners list`);
        }
    } else {
        questParticipants.set(localPlayerId, {
            active: false,
            stagesCleared: currentStagesCleared
        });
    }
}

async function moveToNextActivePlayer() {
    const sponsorId = gameState.currentQuest.sponsorId;
    const sponsorIndex = PLAYER_ORDER.indexOf(sponsorId);
    let currentIndex = PLAYER_ORDER.indexOf(localPlayerId);
    let nextIndex = (currentIndex + 1) % PLAYER_ORDER.length;
    
    let checkedAllPlayers = false;
    while (!checkedAllPlayers) {
        const nextPlayerId = PLAYER_ORDER[nextIndex];
        const participantStatus = questParticipants.get(nextPlayerId);

        if (nextIndex === sponsorIndex || (participantStatus && !participantStatus.active)) {
            nextIndex = (nextIndex + 1) % PLAYER_ORDER.length;
            if (nextIndex === currentIndex) checkedAllPlayers = true;
            continue;
        }

        if (!participantStatus || (participantStatus.active && participantStatus.stagesCleared < currentStage)) {
            localPlayerId = nextPlayerId;
            // Always call handleStageStart for active participants
            if (participantStatus && participantStatus.active) {
                await handleStageStart();
            } else {
                handleParticipationSequence();
            }
            return;
        }

        nextIndex = (nextIndex + 1) % PLAYER_ORDER.length;
        if (nextIndex === currentIndex) checkedAllPlayers = true;
    }
    
    let hasActivePlayers = false;
    questParticipants.forEach((status) => {
        if (status.active) hasActivePlayers = true;
    });

    if (!hasActivePlayers || currentStage >= gameState.currentQuest.stages) {
        console.log("Quest stage complete. Current shield counts:", 
            gameState.players.map(p => `${p.id}: ${p.shields}`));
    }

    if (!hasActivePlayers) {
        endQuest();
    } else if (currentStage < gameState.currentQuest.stages) {
        currentStage++;
        // Reset to first non-sponsor player for new stage
        localPlayerId = PLAYER_ORDER[(sponsorIndex + 1) % PLAYER_ORDER.length];
        handleParticipationSequence();
    } else {
        endQuest();
    }
}


async function endQuest() {
    try {        
        if (questWinners.length > 0) {
            console.log("Quest winners before shield award:", questWinners);
            const sponsorId = gameState.currentQuest.sponsorId;
            // Filter out sponsor from winners but don't modify original array
            const currentQuestWinners = questWinners.filter(winnerId => winnerId !== sponsorId);
            
            console.log("Processing winners for current quest:", currentQuestWinners);
            
            // Add shields just for this quest's winners
            const shieldResponse = await fetch('http://localhost:8080/api/game/quest/addShield', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    playerIds: currentQuestWinners
                })
            });
            
            if (!shieldResponse.ok) {
                throw new Error('Failed to add shields to winners');
            }
            
            // Get updated state after shields are added
            const updatedState = await shieldResponse.json();
            gameState = updatedState;
            
            // Add to cumulative winners after shields are awarded
            currentQuestWinners.forEach(winner => turnQuestWinners.add(winner));
            console.log("Cumulative winners this turn:", Array.from(turnQuestWinners));
            
            // Now check for any players who have reached winning condition
            const winners = gameState.players.filter(player => player.shields >= 7);
            console.log("Current shield counts:", gameState.players.map(p => `${p.id}: ${p.shields}`));
            
            if (winners.length > 0) {
                console.log("Game over! Winners:", winners.map(w => w.id).join(', '));
                handleGameOver();
                return;
            }
        }

        // Complete the quest and reward sponsor
        const completionResponse = await fetch('http://localhost:8080/api/game/quest/complete', {
            method: 'POST'
        });
        
        if (!completionResponse.ok) {
            throw new Error('Failed to process quest completion');
        }
        
        const newState = await completionResponse.json();
        gameState = newState;

        if (checkHandSizes()) {
            return;
        }

        // Explicitly end the turn
        const endResponse = await fetch('http://localhost:8080/api/game/endTurn', {
            method: 'POST'
        });

        if (!endResponse.ok) {
            throw new Error('Failed to end turn');
        }

        const finalState = await endResponse.json();
        
        // Reset quest-specific state
        currentStage = 1;
        questParticipants.clear();
        questWinners = [];  // Clear quest winners for next quest
        currentSponsorshipIndex = 0;
        currentQuest = null;
        declinedSponsors.clear();
        
        // Update game state
        gameState = finalState;
        localPlayerId = finalState.currentPlayerId;
        
        // Reset UI elements
        drawnCard.innerHTML = '';
        actionArea.innerHTML = '';
        document.getElementById('current-hand').style.display = 'block';
        drawButton.style.display = 'block';
        
        // Log final state
        console.log("Quest ended. Current player:", localPlayerId);
        console.log("Final shield counts:", gameState.players.map(p => `${p.id}: ${p.shields}`));
        
        updateGameDisplay();

        actionArea.innerHTML = `
            <button id="draw-button" onclick="drawCard()" class="button">Draw Card</button>
        `;

    } catch (error) {
        console.error('Error ending quest:', error);
    }
}

// Add this helper function to handle the draw card click event
function forceDrawCard() {
    console.log("Forcing draw card for player:", localPlayerId);
    drawButton.style.display = 'block';
    actionArea.innerHTML = `
        <button id="draw-button" onclick="drawCard()" class="button">Draw Card</button>
    `;
    const drawBtn = document.getElementById('draw-button');
    if (drawBtn) {
        drawBtn.click();
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
        //alert('Failed to handle plague card. Please try again.');
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
function toggleTrimCard(cardId) {
    const localPlayer = gameState.players.find(p => p.id === localPlayerId);
    const hand = localPlayer.hand;

    // Track the index of the card in the hand array
    const cardIndex = hand.findIndex((card, idx) => card.id === cardId && !selectedCards.has(`${cardId}-${idx}`));

    if (cardIndex === -1) return; // If no unselected duplicate found

    const uniqueCardKey = `${cardId}-${cardIndex}`;
    if (selectedCards.has(uniqueCardKey)) {
        selectedCards.delete(uniqueCardKey);
    } else if (selectedCards.size < hand.length - 12) {
        selectedCards.add(uniqueCardKey);
    }

    displayTrimHand();
}

// Function to render the hand trimming UI
function displayTrimHand() {
    const trimHandCards = document.getElementById('trim-hand-cards');
    const localPlayer = gameState.players.find(p => p.id === localPlayerId);

    if (localPlayer) {
        trimHandCards.innerHTML = localPlayer.hand.map((card, index) => `
            <div class="card ${selectedCards.has(`${card.id}-${index}`) ? 'selected' : ''}" 
                 onclick="toggleTrimCard('${card.id}')">
                <h4>${card.id}</h4>
                <p>Type: ${card.type}</p>
                <p>Value: ${card.value}</p>
            </div>
        `).join('');
    }

    updateSelectedCardsDisplay();
}

// Function to update the selected cards display
function updateSelectedCardsDisplay() {
    const selectedCardsDisplay = document.getElementById('selected-cards-display');
    const selectedCardsList = Array.from(selectedCards).map(uniqueKey => uniqueKey.split('-')[0]); // Extract base IDs

    selectedCardsDisplay.innerHTML = selectedCardsList.length > 0
        ? selectedCardsList.map(cardId => `<span class="selected-card">${cardId}</span>`).join('')
        : '<p>No cards selected</p>';
}

// Confirm the hand trimming and update the game state
async function confirmTrim() {
    const localPlayer = gameState.players.find(p => p.id === localPlayerId);
    const requiredDiscards = localPlayer.hand.length - 12;

    if (selectedCards.size !== requiredDiscards) return;

    // Map selected unique keys back to card objects
    const selectedCardIds = Array.from(selectedCards).map(uniqueKey => {
        const [cardId, index] = uniqueKey.split('-');
        return localPlayer.hand[index].id;
    });

    try {
        const response = await fetch('http://localhost:8080/api/game/discardCards', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                playerId: localPlayerId,
                cardIds: selectedCardIds
            })
        });

        const newState = await response.json();
        gameState = newState;
        selectedCards.clear();

        // Check if we're in quest context
        if (gameState.currentQuest) {
            handleQuestAttack();
        } else {
            if (!checkHandSizes()) {
                const endResponse = await fetch('http://localhost:8080/api/game/endTurn', { method: 'POST' });
                const finalState = await endResponse.json();
                gameState = finalState;
                localPlayerId = finalState.currentPlayerId;
                document.getElementById('current-hand').style.display = 'block';
                drawButton.style.display = 'block';
                actionArea.innerHTML = '';
            }
            updateGameDisplay();
        }
    } catch (error) {
        console.error('Error trimming hand:', error);
    }
}

// Function to handle hand trimming if necessary
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

// Function to check if any player needs to trim their hand
function checkHandSizes() {
    const playerWithLargeHand = gameState.players.find(player => player.hand.length > 12);
    if (playerWithLargeHand) {
        localPlayerId = playerWithLargeHand.id;
        handleHandTrimming(playerWithLargeHand);
        return true;
    }
    return false;
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
    drawButton.style.display = 'none';
    document.getElementById('current-hand').style.display = 'none';
    
    // Find all winners (anyone with 7+ shields)
    const winners = gameState.players.filter(player => player.shields >= 7);
    
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
    
    gameStatus.textContent = 'Game Over - Winners Found!';
    drawnCard.innerHTML = '';
}


function updateGameDisplay() {
    document.getElementById('adventure-deck-count').textContent = gameState.adventureDeckSize;
    document.getElementById('event-deck-count').textContent = gameState.eventDeckSize;
    
    updatePlayerStats();
    
    // Check for winners first
    const winners = gameState.players.filter(player => player.shields >= 7);
    if (winners.length > 0) {
        handleGameOver();
        return;
    }
    
    drawnCard.innerHTML = `<h3>Current Player: ${gameState.currentPlayerId}</h3>`;
    
    const currentHandDisplay = document.getElementById('current-hand');
    
    if (!gameState.currentQuest && !gameState.pendingQuest) {
        currentHandDisplay.style.display = 'block';
        actionArea.innerHTML = '';
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
        gameStatus.textContent = gameState.gameStatus === 'FINISHED' ? 
            'Game Over - Winners Found!' : 
            `Game Status: ${gameState.gameStatus}`;
    }
}