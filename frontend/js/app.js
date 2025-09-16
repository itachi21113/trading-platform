// Import from all our modules
import { initChart, updateChart, plotHistoricalData } from './chart.js';
import { configureClient, login, logout, getAccessToken, handleRedirectCallback, isAuthenticated } from './auth.js';

document.addEventListener('DOMContentLoaded', async () => {
    // --- UI Element References ---
    const priceDisplay = document.getElementById('price-display');
    const liveBtn = document.getElementById('liveBtn');
    const historyBtn = document.getElementById('historyBtn');
    const runBacktestBtn = document.getElementById('runBacktestBtn');
    const backtestResultDiv = document.getElementById('backtestResult');
    const loginBtn = document.getElementById('loginBtn');
    const logoutBtn = document.getElementById('logoutBtn');
    const mainContent = document.querySelector('.main-content'); // We will add this class in HTML

    // --- Auth0 Configuration & Initialization ---
    await configureClient();
    await handleRedirectCallback(); // Handle the redirect from Auth0
    updateUI(); // Update UI based on initial auth state

    // --- Event Listeners ---
    loginBtn.addEventListener('click', login);
    logoutBtn.addEventListener('click', logout);

    // --- UI Update Function ---
    async function updateUI() {
        const authenticated = await isAuthenticated();
        loginBtn.style.display = authenticated ? 'none' : 'block';
        logoutBtn.style.display = authenticated ? 'block' : 'none';

        // Hide or show the main application content
        const features = document.querySelectorAll('.chart-controls, .price-display, .backtest-section, .chart-container');
        features.forEach(el => {
            el.style.display = authenticated ? '' : 'none';
        });

        if (!authenticated) {
            priceDisplay.textContent = 'Please log in to see the data.';
        }
    }

    // --- Secure Fetch Function ---
    async function fetchSecurely(url) {
        const token = await getAccessToken();
        const response = await fetch(url, {
            headers: {
                Authorization: `Bearer ${token}`
            }
        });
        if (!response.ok) {
            throw new Error(`HTTP error! status: ${response.status}`);
        }
        return response.json();
    }

    // --- Existing Logic (Now using fetchSecurely) ---
    initChart();

    // WebSocket connection remains the same as it's public
    const socket = new SockJS('http://localhost:8081/ws');
    const stompClient = Stomp.over(socket);
    stompClient.connect({}, (frame) => {
        stompClient.subscribe('/topic/prices', (message) => {
            const tick = JSON.parse(message.body);
            updateChart(tick);
        });
    });

    historyBtn.addEventListener('click', async () => {
        try {
            const historicalTicks = await fetchSecurely('http://localhost:8081/history?symbol=BTC-USD&range=24h');
            plotHistoricalData(historicalTicks);
        } catch (error) {
            console.error('Failed to fetch historical data:', error);
        }
    });

    runBacktestBtn.addEventListener('click', async () => {
        try {
            const url = `http://localhost:8081/backtest/sma-crossover?initialBalance=...`; // simplified for brevity
            const result = await fetchSecurely(url);
            // ... (display result logic remains the same) ...
        } catch (error) {
            console.error('Backtest failed:', error);
        }
    });
});