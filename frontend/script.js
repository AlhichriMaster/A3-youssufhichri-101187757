let gameState = {};

function updateGameState() {
    fetch('/api/game/state')
        .then(response => response.json())
        .then(data => {
            gameState = data;
            renderGame();
        });
}

function renderGame() {
    // Render current quest
    const questDiv = document.getElementById('current-quest');
    questDiv.innerHTML = gameState.currentQuest ? 
        `Current Quest: ${gameState.currentQuest.name}` : 'No active quest';

    // Render players
    const playersDiv = document.getElementById('players');
    playersDiv.innerHTML = gameState.players.map(player => `
        <div class="player ${player === gameState.currentPlayer ? 'current-player' : ''}">
            <h3>Player ${player.id}</h3>
            <div>Shields: ${player.shields}</div>
            <div>Hand: ${renderCards(player.hand)}</div>
        </div>
    `).join('');

    // Render decks
    document.getElementById('adventure-deck').textContent = 
        `Cards remaining: ${gameState.adventureDeck.cards.length}`;
    document.getElementById('event-deck').textContent = 
        `Cards remaining: ${gameState.eventDeck.cards.length}`;
}

function renderCards(cards) {
    return cards.map(card => 
        `<div class="card">${card.name}</div>`
    ).join('');
}

// Initial load
updateGameState();
// Update every 5 seconds
setInterval(updateGameState, 5000);