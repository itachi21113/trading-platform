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
  const livePriceSpan = document.getElementById("livePrice");
  const aiSignalSpan = document.getElementById("aiSignal");
  const liveBtn = document.getElementById("liveBtn");
  const historyBtn = document.getElementById("historyBtn");
  const runBacktestBtn = document.getElementById("runBacktestBtn");
  const backtestResultDiv = document.getElementById("backtestResult");
  const loginBtn = document.getElementById("loginBtn");
  const logoutBtn = document.getElementById("logoutBtn");
  const createAlertBtn = document.getElementById("createAlertBtn");
  const alertConditionSelect = document.getElementById("alertCondition");
  const alertPriceInput = document.getElementById("alertPrice");
  const activeAlertsList = document.getElementById("activeAlertsList");
  const notificationToast = document.getElementById("notificationToast");

  // --- Auth0 Configuration & Initialization ---
  await configureClient();
  await handleRedirectCallback();
  await updateUI();

  // --- Event Listeners ---
  loginBtn.addEventListener("click", login);
  logoutBtn.addEventListener("click", logout);

  // --- UI Update Function ---
  async function updateUI() {
    const authenticated = await isAuthenticated();
    loginBtn.style.display = authenticated ? "none" : "block";
    logoutBtn.style.display = authenticated ? "block" : "none";

    const features = document.querySelectorAll(
      ".chart-controls, #price-display, .backtest-section, .chart-container, .alerts-section"
    );

    if (authenticated) {
      features.forEach((el) => (el.style.display = ""));
      fetchAndDisplayAlerts();

      if (!stompClient) {
        const token = await getAccessToken();
        const socket = new SockJS("http://localhost:8081/ws");
        stompClient = Stomp.over(socket);
        const headers = { Authorization: `Bearer ${token}` };

        stompClient.connect(
          headers,
          (frame) => {
            console.log("WebSocket Connected with Auth:", frame);

            stompClient.subscribe("/topic/prices", (message) => {
              const tick = JSON.parse(message.body);
              // **CORRECTION HERE**
              livePriceSpan.textContent = `$${tick.price.toFixed(2)}`;
              updateChart(tick);
            });

            stompClient.subscribe("/user/queue/notifications", (message) => {
              console.log("Received private notification:", message.body);
              showToastNotification(message.body);
              fetchAndDisplayAlerts();
            });

            stompClient.subscribe("/topic/predictions", (message) => {
              const prediction = message.body;
              aiSignalSpan.textContent = `AI Signal: ${prediction}`;
              aiSignalSpan.classList.remove(
                "signal-up",
                "signal-down",
                "signal-error"
              );
              if (prediction === "UP") {
                aiSignalSpan.textContent += " ▲";
                aiSignalSpan.classList.add("signal-up");
              } else if (prediction === "DOWN") {
                aiSignalSpan.textContent += " ▼";
                aiSignalSpan.classList.add("signal-down");
              } else {
                aiSignalSpan.classList.add("signal-error");
              }
            });
          },
          (error) => {
            console.error("WebSocket connection error:", error);
          }
        );
      }
    } else {
      features.forEach((el) => (el.style.display = "none"));
      // **CORRECTION HERE**
      const priceDisplayDiv = document.getElementById("price-display");
      if (priceDisplayDiv) priceDisplayDiv.style.display = "";
      livePriceSpan.textContent = "Please log in to see the data.";
    }
  }

  // --- Secure Fetch Function ---
  async function fetchSecurely(url, options = {}) {
    const token = await getAccessToken();
    const headers = { ...options.headers, Authorization: `Bearer ${token}` };
    const response = await fetch(url, { ...options, headers });
    if (!response.ok) throw new Error(`HTTP error! status: ${response.status}`);
    const contentType = response.headers.get("content-type");
    if (contentType && contentType.indexOf("application/json") !== -1)
      return response.json();
    return;
  }

  // --- Chart and Backtesting Logic ---
  initChart();

  historyBtn.addEventListener("click", async () => {
    historyBtn.classList.add("active");
    liveBtn.classList.remove("active");
    livePriceSpan.textContent = "Loading history...";
    try {
      const historicalTicks = await fetchSecurely(
        "http://localhost:8081/history?symbol=BTC-USD&range=24h"
      );
      plotHistoricalData(historicalTicks);
      if (historicalTicks.length > 0) {
        const latestTick = historicalTicks[historicalTicks.length - 1];
        livePriceSpan.textContent = `$${latestTick.price.toFixed(2)}`;
      } else {
        livePriceSpan.textContent = "No historical data found.";
      }
    } catch (error) {
      livePriceSpan.textContent = "Failed to load history.";
      console.error("Failed to fetch historical data:", error);
    }
  });

  liveBtn.addEventListener("click", () => {
    liveBtn.classList.add("active");
    historyBtn.classList.remove("active");
    // **CORRECTION HERE**
    livePriceSpan.textContent = "Waiting for live data...";
  });

  runBacktestBtn.addEventListener("click", async () => {
    backtestResultDiv.style.display = "block";
    backtestResultDiv.innerHTML = "<p>Running backtest...</p>";
    const initialBalance = document.getElementById("initialBalance").value;
    const shortPeriod = document.getElementById("shortPeriod").value;
    const longPeriod = document.getElementById("longPeriod").value;
    try {
      const url = `http://localhost:8081/backtest/sma-crossover?initialBalance=${initialBalance}&shortPeriod=${shortPeriod}&longPeriod=${longPeriod}&range=24h`;
      const result = await fetchSecurely(url);
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
        </p>`;
    } catch (error) {
      console.error("Backtest failed:", error);
      backtestResultDiv.innerHTML =
        '<p class="loss">Error running backtest. See console for details.</p>';
    }
  });

  // --- Alerts Logic ---
  async function fetchAndDisplayAlerts() {
    try {
      const alerts = await fetchSecurely("http://localhost:8081/api/alerts");
      activeAlertsList.innerHTML = "";
      if (alerts.length === 0) {
        activeAlertsList.innerHTML = "<li>No active alerts.</li>";
      } else {
        alerts.forEach((alert) => {
          const li = document.createElement("li");
          li.textContent = `Notify when ${
            alert.symbol
          } is ${alert.alertCondition.toLowerCase()} $${alert.targetPrice.toLocaleString()}`;
          activeAlertsList.appendChild(li);
        });
      }
    } catch (error) {
      console.error("Failed to fetch alerts:", error);
      activeAlertsList.innerHTML = "<li>Error loading alerts.</li>";
    }
  }

  createAlertBtn.addEventListener("click", async () => {
    const symbol = "BTC-USD";
    const condition = alertConditionSelect.value;
    const targetPrice = alertPriceInput.value;
    if (!targetPrice) {
      alert("Please enter a target price.");
      return;
    }
    try {
      await fetchSecurely("http://localhost:8081/api/alerts", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ symbol, condition, targetPrice }),
      });
      alertPriceInput.value = "";
      fetchAndDisplayAlerts();
    } catch (error) {
      console.error("Failed to create alert:", error);
      alert("Error creating alert.");
    }
  });

  function showToastNotification(message) {
    notificationToast.textContent = message;
    notificationToast.classList.add("show");
    setTimeout(() => {
      notificationToast.classList.remove("show");
    }, 5000);
  }
});