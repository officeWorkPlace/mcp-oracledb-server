# 📊 Custom Dashboard Templates

A collection of professional, customizable dashboard templates for data visualization. These templates are built with modern HTML5, CSS3, and JavaScript, providing flexible solutions for different use cases.

## 🗂️ Directory Structure

```
dashboard-templates/
├── basic/                  # Simple, clean templates
│   └── dashboard-basic.html
├── advanced/              # Feature-rich templates
│   └── dashboard-advanced.html
├── configs/               # Configuration files
│   └── dashboard-config.json
├── assets/                # Static assets (images, icons)
└── README.md             # This file
```

## 🚀 Quick Start

### 1. Basic Template
**File:** `basic/dashboard-basic.html`

Perfect for simple data visualization needs with clean, professional styling.

**Features:**
- ✅ 3 Chart Types (Bar, Line, Pie)
- ✅ 4 Metric Cards
- ✅ Real-time Updates
- ✅ Responsive Design
- ✅ Export Functionality
- ✅ Easy Customization

**Usage:**
```bash
# Open in browser
.\dashboard-templates\basic\dashboard-basic.html
```

### 2. Advanced Template
**File:** `advanced/dashboard-advanced.html`

Enterprise-grade template with advanced features and stunning visuals.

**Features:**
- ✅ Real-time Time Series Charts
- ✅ D3.js Network Visualizations
- ✅ Interactive Heat Maps
- ✅ Animated Sparklines
- ✅ Live Data Streaming
- ✅ 3D Animations
- ✅ WebSocket Support
- ✅ Fullscreen Mode

**Usage:**
```bash
# Open in browser
.\dashboard-templates\advanced\dashboard-advanced.html
```

## ⚙️ Configuration

### Using the Configuration File
**File:** `configs/dashboard-config.json`

```javascript
// Load configuration in your template
function loadConfiguration(configUrl) {
    fetch(configUrl)
        .then(response => response.json())
        .then(config => {
            applyConfiguration(config);
        });
}

// Apply configuration
loadConfiguration('../configs/dashboard-config.json');
```

### Key Configuration Options

```json
{
  "dashboard": {
    "title": "Your Dashboard Title",
    "subtitle": "Your Subtitle",
    "theme": "dark",
    "refreshInterval": 30000
  },
  "colors": {
    "primary": "#6366f1",
    "success": "#10b981",
    "warning": "#f59e0b",
    "danger": "#ef4444"
  },
  "metrics": [
    {
      "id": "metric1",
      "title": "Total Users",
      "icon": "👥",
      "dataSource": "api",
      "endpoint": "/api/users/count"
    }
  ]
}
```

## 🎨 Customization Guide

### 1. Colors and Themes

**CSS Variables (Basic Template):**
```css
:root {
    --primary-color: #3b82f6;
    --success-color: #10b981;
    --warning-color: #f59e0b;
    --danger-color: #ef4444;
    --bg-color: #f8fafc;
    --card-bg: #ffffff;
    --text-color: #1f2937;
}
```

**CSS Variables (Advanced Template):**
```css
:root {
    --primary: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    --secondary: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
    --success: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}
```

### 2. Data Integration

**Sample Data Structure:**
```javascript
const sampleData = {
    bar: {
        labels: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun'],
        datasets: [{
            label: 'Sales Data',
            data: [65, 78, 90, 81, 56, 95],
            backgroundColor: '#3b82f6'
        }]
    }
};
```

**API Integration:**
```javascript
// Fetch data from your API
async function loadApiData(endpoint) {
    try {
        const response = await fetch(endpoint);
        const data = await response.json();
        updateCharts(data);
    } catch (error) {
        console.error('Error loading data:', error);
    }
}
```

**Real-time Updates:**
```javascript
// WebSocket connection
function connectWebSocket(url) {
    const ws = new WebSocket(url);
    
    ws.onmessage = function(event) {
        const data = JSON.parse(event.data);
        updateDashboard(data);
    };
}
```

### 3. Adding New Charts

**Chart.js Integration:**
```javascript
function createCustomChart(canvasId, type, data) {
    const ctx = document.getElementById(canvasId).getContext('2d');
    
    return new Chart(ctx, {
        type: type,
        data: data,
        options: {
            responsive: true,
            maintainAspectRatio: false,
            // Your custom options
        }
    });
}
```

**D3.js Integration:**
```javascript
function createD3Chart(containerId, data) {
    const svg = d3.select(`#${containerId}`)
        .append('svg')
        .attr('width', width)
        .attr('height', height);
    
    // Your D3.js visualization code
}
```

## 🔌 Data Source Integration

### 1. Oracle Database (via MCP)
```javascript
// Use with Oracle MCP tools
function loadOracleData() {
    // Call MCP Oracle tools
    const oracleData = callMcpTool('oracle_generate_loan_popularity_chart');
    updateDashboard(oracleData);
}
```

### 2. REST API
```javascript
// Generic API integration
const apiConfig = {
    baseUrl: 'https://api.yoursite.com',
    endpoints: {
        metrics: '/api/dashboard/metrics',
        charts: '/api/dashboard/charts'
    },
    headers: {
        'Authorization': 'Bearer YOUR_TOKEN'
    }
};
```

### 3. WebSocket Streaming
```javascript
// Real-time data streaming
function startRealTimeUpdates(wsUrl) {
    const socket = new WebSocket(wsUrl);
    
    socket.onmessage = (event) => {
        const data = JSON.parse(event.data);
        updateMetricsRealTime(data);
    };
}
```

### 4. CSV/File Upload
```javascript
// File upload handling
function handleFileUpload(file) {
    const reader = new FileReader();
    reader.onload = (e) => {
        const csv = e.target.result;
        const data = parseCSV(csv);
        updateDashboard(data);
    };
    reader.readAsText(file);
}
```

## 🎯 Use Cases

### 1. Business Analytics
- Sales performance tracking
- Customer behavior analysis
- Revenue monitoring
- KPI dashboards

### 2. System Monitoring
- Server performance metrics
- Application health monitoring
- Network topology visualization
- Error rate tracking

### 3. Financial Dashboards
- Trading platforms
- Portfolio management
- Risk assessment
- Market analysis

### 4. IoT & Sensor Data
- Real-time sensor monitoring
- Environmental tracking
- Industrial automation
- Smart city dashboards

## 🛠️ Development Tips

### 1. Performance Optimization
```javascript
// Efficient chart updates
chart.data.datasets[0].data = newData;
chart.update('none'); // Skip animations for better performance

// Limit data points for real-time charts
if (data.length > maxDataPoints) {
    data.shift(); // Remove oldest point
}
```

### 2. Responsive Design
```css
/* Mobile-first approach */
@media (max-width: 768px) {
    .chart-row {
        grid-template-columns: 1fr;
    }
    .metrics-grid {
        grid-template-columns: 1fr;
    }
}
```

### 3. Error Handling
```javascript
// Robust error handling
function safeChartUpdate(chartId, newData) {
    try {
        if (charts[chartId] && newData) {
            charts[chartId].data = newData;
            charts[chartId].update();
        }
    } catch (error) {
        console.error(`Error updating chart ${chartId}:`, error);
        showErrorMessage(`Failed to update ${chartId}`);
    }
}
```

## 🔧 Browser Support

| Browser | Basic Template | Advanced Template |
|---------|---------------|-------------------|
| Chrome 90+ | ✅ | ✅ |
| Firefox 88+ | ✅ | ✅ |
| Safari 14+ | ✅ | ✅ |
| Edge 90+ | ✅ | ✅ |

## 📦 Dependencies

### Basic Template
- [Chart.js](https://www.chartjs.org/) - Chart library
- [Inter Font](https://fonts.google.com/specimen/Inter) - Typography

### Advanced Template
- [Chart.js](https://www.chartjs.org/) - Chart library
- [D3.js](https://d3js.org/) - Data visualization
- [Inter Font](https://fonts.google.com/specimen/Inter) - Typography

## 🎨 Themes Available

### Light Theme
- Clean white background
- Professional blue accents
- High contrast text

### Dark Theme
- Dark gradient backgrounds
- Neon accents and glows
- Glassmorphism effects

## 📱 Mobile Support

Both templates are fully responsive and optimized for:
- 📱 Mobile phones (320px+)
- 📱 Tablets (768px+)
- 💻 Desktop (1024px+)
- 🖥️ Large screens (1440px+)

## 🚀 Deployment

### Local Development
```bash
# Simply open in browser
open dashboard-templates/basic/dashboard-basic.html
```

### Production Deployment
1. Upload templates to your web server
2. Configure data endpoints
3. Set up HTTPS for WebSocket connections
4. Enable caching for static assets

## 📝 License

These templates are provided for educational and development purposes. Feel free to modify and use them in your projects.

## 🤝 Contributing

To add new templates or improve existing ones:
1. Follow the established directory structure
2. Maintain responsive design principles
3. Include configuration examples
4. Test across different browsers

---

**Happy Dashboard Building! 🎉**
