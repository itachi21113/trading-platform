import { initChart, updateChart, plotHistoricalData } from './chart.js';

document.addEventListener('DOMContentLoaded', () => {
    const priceDisplay = document.getElementById('price-display');
    const liveBtn = document.getElementById('liveBtn');
    const historyBtn = document.getElementById('historyBtn');

    let isLive = true; // State to track if we are in live mode

    initChart();

    // --- WebSocket Connection ---
    const socket = new SockJS('http://localhost:8081/ws');
    const stompClient = Stomp.over(socket);

    stompClient.connect({}, (frame) => {
        console.log('Connected: ' + frame);
        priceDisplay.textContent = 'Waiting for data...';

        stompClient.subscribe('/topic/prices', (message) => {
            // Only update the chart if we are in live mode
            if (isLive) {
                const tick = JSON.parse(message.body);
                priceDisplay.textContent = `$${tick.price.toFixed(2)}`;
                updateChart(tick);
            }
        });
    });

    // --- Button Event Listeners ---
    liveBtn.addEventListener('click', () => {
        isLive = true;
        liveBtn.classList.add('active');
        historyBtn.classList.remove('active');
        // The chart will automatically resume updating via the WebSocket subscription
    });

    historyBtn.addEventListener('click', async () => {
        isLive = false;
        historyBtn.classList.add('active');
        liveBtn.classList.remove('active');
        priceDisplay.textContent = 'Loading history...';

        try {
            // Fetch the 24h history from our new API endpoint
            const response = await fetch('http://localhost:8081/history?symbol=BTC-USD&range=24h');
            const historicalTicks = await response.json();

            // Plot the data on the chart
            plotHistoricalData(historicalTicks);

            // Update the display with the latest historical price
            const latestTick = historicalTicks[historicalTicks.length - 1];
            if (latestTick) {
                priceDisplay.textContent = `$${latestTick.price.toFixed(2)}`;
            } else {
                priceDisplay.textContent = 'No historical data available.';
            }
        } catch (error) {
            console.error('Failed to fetch historical data:', error);
            priceDisplay.textContent = 'Failed to load history.';
        }
    });
});