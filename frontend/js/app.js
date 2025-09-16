// Import from all our modules
import { initChart, updateChart, plotHistoricalData } from "./chart.js";
import {
  configureClient,
  login,
  logout,
  getAccessToken,
  handleRedirectCallback,
  isAuthenticated,
} from "./auth.js";

document.addEventListener("DOMContentLoaded", async () => {
  let stompClient = null;
  // --- UI Element References ---
  const priceDisplay = document.getElementById("price-display");
  const liveBtn = document.getElementById("liveBtn");
  const historyBtn = document.getElementById("historyBtn");
  const runBacktestBtn = document.getElementById("runBacktestBtn");
  const backtestResultDiv = document.getElementById("backtestResult");
  const loginBtn = document.getElementById("loginBtn");
  const logoutBtn = document.getElementById("logoutBtn");
  const mainContent = document.querySelector(".main-content"); // We will add this class in HTML

  // --- Auth0 Configuration & Initialization ---
  await configureClient();
  await handleRedirectCallback(); // Handle the redirect from Auth0
  updateUI(); // Update UI based on initial auth state

  // --- Event Listeners ---
  loginBtn.addEventListener("click", login);
  logoutBtn.addEventListener("click", logout);

  // --- UI Update Function ---
  async function updateUI() {
    const authenticated = await isAuthenticated();
    loginBtn.style.display = authenticated ? "none" : "block";
    logoutBtn.style.display = authenticated ? "block" : "none";

    // Hide or show the main application content
    const features = document.querySelectorAll(
      ".chart-controls, .price-display, .backtest-section, .chart-container"
    );
    features.forEach((el) => {
      el.style.display = authenticated ? "" : "none";
    });

    if (authenticated) {
      // Show all the features
      features.forEach((el) => (el.style.display = ""));

      // --- PASTE THE WEBSOCKET CODE HERE ---
      // And only connect if we haven't already
      if (!stompClient) {
        const socket = new SockJS("http://localhost:8081/ws");
        stompClient = Stomp.over(socket);
        stompClient.connect({}, (frame) => {
          stompClient.subscribe("/topic/prices", (message) => {
            const tick = JSON.parse(message.body);
            priceDisplay.textContent = `$${tick.price.toFixed(2)}`;
            updateChart(tick);
          });
        });
      }
    } else {
      // Hide all the features
      features.forEach((el) => (el.style.display = "none"));
      // And show the login message
      priceDisplay.style.display = ""; // Make sure the price display itself is visible
      priceDisplay.textContent = "Please log in to see the data.";
    }
  }

  // --- Secure Fetch Function ---
  async function fetchSecurely(url) {
    const token = await getAccessToken();
    const response = await fetch(url, {
      headers: {
        Authorization: `Bearer ${token}`,
      },
    });
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`);
    }
    return response.json();
  }

  // --- Existing Logic (Now using fetchSecurely) ---
  initChart();

  // In frontend/js/app.js

  historyBtn.addEventListener("click", async () => {
    // Update button styles
    historyBtn.classList.add("active");
    liveBtn.classList.remove("active");
    priceDisplay.textContent = "Loading history...";

    try {
      const historicalTicks = await fetchSecurely(
        "http://localhost:8081/history?symbol=BTC-USD&range=24h"
      );
      plotHistoricalData(historicalTicks);

      // Update the display with the latest historical price
      if (historicalTicks.length > 0) {
        const latestTick = historicalTicks[historicalTicks.length - 1];
        priceDisplay.textContent = `$${latestTick.price.toFixed(2)}`;
      } else {
        priceDisplay.textContent = "No historical data found.";
      }
    } catch (error) {
      priceDisplay.textContent = "Failed to load history.";
      console.error("Failed to fetch historical data:", error);
    }
  });

  // In frontend/js/app.js

  liveBtn.addEventListener("click", () => {
    // Update button styles to show "Live" is active
    liveBtn.classList.add("active");
    historyBtn.classList.remove("active");
    priceDisplay.textContent = "Waiting for live data...";

    // The WebSocket will automatically take over from here
  });

  // In frontend/js/app.js

  runBacktestBtn.addEventListener("click", async () => {
    // Show a loading message
    backtestResultDiv.style.display = "block";
    backtestResultDiv.innerHTML = "<p>Running backtest...</p>";

    // Get the actual values from the input fields
    const initialBalance = document.getElementById("initialBalance").value;
    const shortPeriod = document.getElementById("shortPeriod").value;
    const longPeriod = document.getElementById("longPeriod").value;

    try {
      // Construct the correct, dynamic URL
      const url = `http://localhost:8081/backtest/sma-crossover?initialBalance=${initialBalance}&shortPeriod=${shortPeriod}&longPeriod=${longPeriod}&range=24h`;
      const result = await fetchSecurely(url);

      // This is the logic to display the results in the UI
      const pnlClass = result.profitOrLoss >= 0 ? "profit" : "loss";
      backtestResultDiv.innerHTML = `
               <p><strong>Strategy:</strong> ${result.strategy}</p>
               <p><strong>Initial Balance:</strong> $${result.initialBalance.toLocaleString()}</p>
               <p><strong>Final Balance:</strong> $${result.finalBalance.toLocaleString()}</p>
               <p><strong>Total Trades:</strong> ${result.totalTrades}</p>
               <p class="${pnlClass}">
                   <strong>Profit/Loss:</strong> $${result.profitOrLoss.toLocaleString()} (${result.profitPercentage.toFixed(
        2
      )}%)
               </p>
           `;
    } catch (error) {
      console.error("Backtest failed:", error);
      backtestResultDiv.innerHTML =
        '<p class="loss">Error running backtest. See console for details.</p>';
    }
  });
});
