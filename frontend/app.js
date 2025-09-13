document.addEventListener('DOMContentLoaded', () => {
    const priceDisplay = document.getElementById('price-display');

    // 1. Create a new SockJS connection to our backend endpoint
    // This is the endpoint we defined in WebSocketConfig.java
    const socket = new SockJS('http://localhost:8081/ws');

    // 2. Create a STOMP client over the SockJS connection
    const stompClient = Stomp.over(socket);

    // 3. Connect the STOMP client
    stompClient.connect({}, (frame) => {
        console.log('Connected: ' + frame);
        priceDisplay.textContent = 'Waiting for data...';

        // 4. Subscribe to the "/topic/prices" destination
        // This is the destination we send messages to in PriceStreamService.java
        stompClient.subscribe('/topic/prices', (message) => {
            // When a message is received, parse the JSON and update the display
            const tick = JSON.parse(message.body);
            priceDisplay.textContent = `$${tick.price.toFixed(2)}`;
        });
    }, (error) => {
        console.error('Connection error: ' + error);
        priceDisplay.textContent = 'Connection failed!';
    });
});