let priceChart; // This variable will hold our chart instance

/**
 * Initializes the chart with a starting configuration.
 */
export function initChart() {
    const ctx = document.getElementById('priceChart').getContext('2d');
    priceChart = new Chart(ctx, {
        type: 'line', // We want a line chart
        data: {
            labels: [], // Timestamps will go here
            datasets: [{
                label: 'BTC-USD',
                data: [], // Prices will go here
                borderColor: 'rgba(255, 159, 64, 1)',
                backgroundColor: 'rgba(255, 159, 64, 0.2)',
                borderWidth: 2,
                fill: true,
                tension: 0.4 // This makes the line smooth
            }]
        },
        options: {
            scales: {
                x: {
                    type: 'time',
                    time: {
                        unit: 'second',
                        displayFormats: {
                            second: 'h:mm:ss a'
                        }
                    },
                    ticks: {
                        color: '#e0e0e0'
                    }
                },
                y: {
                    beginAtZero: false,
                    ticks: {
                        color: '#e0e0e0',
                        // Format the tick labels to look like currency
                        callback: function(value, index, values) {
                            return '$' + value.toLocaleString();
                        }
                    }
                }
            },
            plugins: {
                legend: {
                    display: false
                }
            }
        }
    });
}

/**
 * Updates the chart with a new price tick.
 * @param {object} tick - The price tick object { price, timestamp }.
 */
export function updateChart(tick) {
    // Add the new data to the chart
    priceChart.data.labels.push(tick.timestamp);
    priceChart.data.datasets[0].data.push(tick.price);

    // To prevent the chart from becoming cluttered, we'll limit it
    // to showing the last 60 data points (e.g., 60 seconds of data).
    const maxDataPoints = 60;
    if (priceChart.data.labels.length > maxDataPoints) {
        priceChart.data.labels.shift(); // Remove the oldest label
        priceChart.data.datasets[0].data.shift(); // Remove the oldest price data
    }

    // This command tells Chart.js to redraw the chart with the new data
    priceChart.update('none'); // 'none' for a smooth animation
}