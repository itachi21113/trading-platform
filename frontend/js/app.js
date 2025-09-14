// Import the functions from our new chart module
import { initChart, updateChart } from './chart.js';

document.addEventListener('DOMContentLoaded', () => {
    const priceDisplay = document.getElementById('price-display');

    // 1. Initialize our chart as soon as the page loads
    initChart();

    // 2. Create the WebSocket connection (this logic remains the same)
    const socket = new SockJS('http://localhost:8081/ws');
    const stompClient = Stomp.over(socket);

    // 3. Connect and subscribe
    stompClient.connect({}, (frame) => {
        console.log('Connected: ' + frame);
        priceDisplay.textContent = 'Waiting for data...';

        stompClient.subscribe('/topic/prices', (message) => {
            const tick = JSON.parse(message.body);

            // 4. Update the text display and the chart with the new data
            priceDisplay.textContent = `$${tick.price.toFixed(2)}`;
            updateChart(tick);
        });
    }, (error) => {
        console.error('Connection error: ' + error);
        priceDisplay.textContent = 'Connection failed!';
    });
});