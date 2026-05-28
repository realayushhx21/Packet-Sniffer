# 🛡️ Packet Sniffer — Network Security Intelligence Platform

A real-time network traffic analysis and threat detection tool built with **Java Swing**. Features a SOC-inspired cybersecurity dashboard with AI-powered packet analysis (Google Gemini), live traffic visualization, multi-vector intrusion detection, SQLite persistence, and multi-format export.

---

## 📋 Table of Contents

- [Features](#-features)
- [Screenshots](#-screenshots)
- [Architecture](#-architecture)
- [Project Structure](#-project-structure)
- [Prerequisites](#-prerequisites)
- [Installation](#-installation)
- [Configuration](#-configuration)
- [Running](#-running)
- [Usage Guide](#-usage-guide)
- [Threat Detection Engine](#-threat-detection-engine)
- [Dependencies](#-dependencies)
- [License](#-license)

---

## ✨ Features

### Core Packet Capture
- **Network Interface Selection** — Dynamically enumerate and select available network interfaces for monitoring
- **Real-Time Packet Capture** — Capture live network traffic using pcap4j (libpcap/Npcap wrapper)
- **Detailed Packet Inspection** — Drill into headers, payloads, raw hex data, and packet length
- **BPF Packet Filtering** — Apply Berkeley Packet Filter expressions during live capture (e.g. `tcp port 80`, `host 192.168.1.1`)
- **PCAP File Import** — Open and replay saved `.pcap` files at 1x, 5x, or instant speed

### 🤖 AI-Powered Analysis
- **Gemini AI Packet Explanation** — Send any captured packet to Google Gemini for a plain-English explanation of the protocol, source/destination, and any suspicious activity
- Uses the Gemini REST API via `HttpURLConnection` — no external HTTP client dependency

### 📊 Live Analytics Dashboard
- **Packets/sec Line Chart** — Real-time traffic rate visualization (JFreeChart)
- **Protocol Distribution Pie Chart** — Live breakdown of captured protocol types (TCP, UDP, ICMP, ARP, DNS, etc.)
- **Top Source IPs** — Dynamic pie chart of the most active source IP addresses
- **Packet Size Distribution** — Line chart tracking packet sizes across the capture session
- All charts auto-refresh every 2 seconds

### 🛡️ Multi-Vector Intrusion Detection
The threat detection engine analyzes every captured packet in real-time and raises alerts for **12 distinct attack types**:

| # | Threat | Severity | Detection Method |
|---|--------|----------|-----------------|
| 1 | Port Scan | HIGH | SYN-only packets to many unique destination ports within a time window |
| 2 | ARP Spoofing | HIGH | Multiple MAC addresses mapping to a single IP |
| 3 | ARP Flood | HIGH | Excessive ARP packets from a single source MAC |
| 4 | DoS / High Volume | HIGH | Packet rate exceeding threshold from a single source IP |
| 5 | SYN Flood | HIGH | High volume of SYN packets from one source to one destination |
| 6 | DNS Amplification | HIGH | High volume of DNS (port 53) traffic from a single source |
| 7 | ICMP Flood | HIGH | Excessive ICMP Echo Request packets from a single source |
| 8 | FIN Scan | HIGH | FIN-only packets to many ports (Nmap `-sF` stealth scan) |
| 9 | NULL Scan | HIGH | Zero-flag TCP packets to many ports (Nmap `-sN` stealth scan) |
| 10 | Suspicious Port Access | MEDIUM | Traffic on known malware/backdoor ports (4444, 31337, 12345, etc.) |
| 11 | Large Packet | MEDIUM | Packets exceeding 1400 bytes (potential exfiltration/tunneling) |
| 12 | TTL Anomaly | MEDIUM | Unusually low TTL values (≤5), indicating traceroute or evasion |

Each alert includes:
- **Mathematical Detection Logic** — The exact algorithm and formula that triggered the alert
- **Remediation Steps** — Immediate and long-term mitigation actions

### 💾 Data Persistence
- **SQLite Database** — All captured packets are automatically persisted with full-text search capability
- **Database Logs Panel** — Browse and search historical captures directly from the UI

### 📤 Multi-Format Export
- **CSV** — Tabular export with timestamp, source/destination IPs, protocol, length, and summary
- **JSON** — Structured export for programmatic consumption
- **PCAP** — Native pcap format for replay in Wireshark or other tools

### 🎨 UI / UX
- **SOC-Inspired Dark Theme** — Custom glassmorphism-styled cybersecurity dashboard with FlatLaf
- **Sidebar Navigation** — 9 dedicated panels accessible via an animated sidebar
- **Animated Splash Screen** — Loading sequence with progress bar and animated grid
- **Toast Notifications** — Real-time pop-up alerts for detected threats
- **Animated Counters & Sparklines** — Smooth metric card animations on the dashboard
- **Hex Viewer Component** — Packet raw data viewer with formatted hex display

---

## 🏗️ Architecture

```
┌────────────────────────────────────────────────────────────┐
│                     Main.java (Entry Point)                │
│                  UITheme.initTheme() → SplashScreen         │
│                         → DashboardFrame                    │
├──────────┬─────────────────────────────────────────────────┤
│ Sidebar  │              Content Panels                     │
│ Nav      │  ┌──────────────────────────────────────────┐   │
│          │  │ DashboardPanel   - Metrics, sparklines    │   │
│  🏠 Dash │  │ LiveCapturePanel - Interface select,     │   │
│  📡 Cap  │  │                    BPF filter, table      │   │
│  🛡 Thr  │  │ ThreatPanel      - Alert table, logic,   │   │
│  🤖 AI   │  │                    live feed              │   │
│  📋 Exp  │  │ AIAnalysisPanel  - Gemini integration     │   │
│  💾 DB   │  │ PacketExplorerPanel - Deep inspection     │   │
│  📤 Exp  │  │ DatabasePanel    - SQLite browser         │   │
│  📊 Ana  │  │ ExportPanel      - CSV/JSON/PCAP export   │   │
│  ⚙ Set   │  │ AnalyticsPanel   - Charts & graphs        │   │
│          │  │ SettingsPanel    - Configuration           │   │
│          │  └──────────────────────────────────────────┘   │
├──────────┴─────────────────────────────────────────────────┤
│                        StatusBar                           │
└────────────────────────────────────────────────────────────┘

Backend Services:
  PacketUtils        →  Protocol parsing & packet info extraction
  ThreatDetector     →  12-vector real-time intrusion detection
  GeminiExplainer    →  Google Gemini REST API integration
  DatabaseManager    →  SQLite CRUD & full-text search
  ExportManager      →  CSV / JSON / PCAP serialization
  ConfigManager      →  config.properties reader
```

---

## 📁 Project Structure

```
packet-sniffer/
├── src/
│   ├── Main.java                  # Application entry point
│   ├── DashboardFrame.java        # Main frame, panel wiring, chart timer
│   ├── UITheme.java               # SOC dark theme, glassmorphism, animations
│   ├── SplashScreen.java          # Animated loading splash
│   ├── ThreatDetector.java        # 12-vector intrusion detection engine
│   ├── GeminiExplainer.java       # Google Gemini AI REST integration
│   ├── DatabaseManager.java       # SQLite packet persistence & search
│   ├── ExportManager.java         # CSV, JSON, PCAP export
│   ├── ConfigManager.java         # config.properties loader
│   ├── PacketUtils.java           # Protocol parsing utilities
│   ├── PacketDetails.java         # Detailed packet dissection
│   ├── ViewPackets.java           # Packet table/viewer logic
│   ├── CheckStats.java            # Capture statistics tracker
│   ├── Header.java                # Packet header model
│   ├── Length.java                 # Packet length model
│   ├── Payload.java               # Packet payload model
│   ├── RawData.java               # Raw hex data model
│   ├── components/
│   │   ├── GlassCard.java         # Glassmorphism card component
│   │   ├── MetricCard.java        # Animated metric display card
│   │   ├── SparklinePanel.java    # Mini sparkline chart widget
│   │   ├── HexViewer.java         # Hex dump viewer component
│   │   ├── SidebarNav.java        # Collapsible sidebar navigation
│   │   ├── StatusBar.java         # Bottom status bar (packets, rate, DB)
│   │   └── NotificationManager.java # Toast notification system
│   └── panels/
│       ├── DashboardPanel.java     # Overview with metric cards & charts
│       ├── LiveCapturePanel.java   # Interface select, BPF, live table
│       ├── ThreatPanel.java        # Threat alerts, math logic, remediation
│       ├── AIAnalysisPanel.java    # Gemini AI packet explanation
│       ├── PacketExplorerPanel.java # Deep packet inspector
│       ├── DatabasePanel.java      # SQLite database browser
│       ├── ExportPanel.java        # Multi-format export UI
│       ├── AnalyticsPanel.java     # Traffic charts & analytics
│       └── SettingsPanel.java      # App configuration UI
├── lib/                           # All dependency JARs (see below)
├── config.properties              # Runtime configuration
├── packet_sniffer.db              # SQLite database (auto-created)
├── DEPENDENCIES.txt               # Dependency details & setup
├── CODE_OF_CONDUCT.md
├── CONTRIBUTING.md
├── SECURITY.md
├── LICENSE                        # Apache License 2.0
└── README.md
```

---

## 📌 Prerequisites

| Requirement | Details |
|-------------|---------|
| **Java** | JDK 11 or higher |
| **Packet Capture Driver** | **Npcap** (Windows) or **libpcap** (Linux/macOS) |
| **IDE (recommended)** | IntelliJ IDEA |
| **Internet** | Required only for the Gemini AI feature |

> **Windows Users:** Download and install [Npcap](https://npcap.com/) with the "WinPcap API-compatible Mode" option enabled.

---

## 🚀 Installation

1. **Clone the repository**
   ```bash
   git clone https://github.com/arya2004/packet-sniffer.git
   cd packet-sniffer
   ```

2. **Open in IntelliJ IDEA**
   - `File` → `Open` → select the `packet-sniffer` folder
   - All dependencies are pre-included in the `lib/` directory

3. **Verify library setup** (if needed)
   - `File` → `Project Structure` → `Modules` → `Dependencies`
   - Ensure all JARs from `lib/` are listed. If not, click `+` → `JARs or Directories` and add them.

---

## ⚙️ Configuration

Edit `config.properties` in the project root:

```properties
# Gemini AI API Key
# Get yours at https://aistudio.google.com/app/apikey
gemini.api.key=YOUR_KEY_HERE

# SQLite Database Path
db.path=packet_sniffer.db

# Default capture settings
capture.snaplen=65536
capture.timeout=150

# Chart settings
chart.history.seconds=30
chart.update.interval.ms=1000

# Threat detection thresholds
threat.portscan.threshold=5
threat.portscan.window.seconds=10
threat.dos.threshold=100
threat.dos.window.seconds=3
```

> **Tip:** Adjust `threat.portscan.threshold` and `threat.dos.threshold` to fine-tune sensitivity for your network environment.

---

## ▶️ Running

### From IntelliJ IDEA
1. Navigate to `src/Main.java`
2. Click **Run** ▶️

### From Command Line
```bash
# Compile
javac -d out/production -cp "lib/*" src/*.java src/components/*.java src/panels/*.java

# Run (Windows)
java -cp "out/production;lib/*" com.first.src.Main

# Run (Linux / macOS)
java -cp "out/production:lib/*" com.first.src.Main
```

> **Note:** On Linux/macOS, use `:` instead of `;` as the classpath separator. You may also need to run with `sudo` for packet capture permissions.

---

## 📖 Usage Guide

### 1. Dashboard
The home screen displays live metric cards (total packets, packets/sec, threats detected, protocols seen) with animated counters and sparkline graphs. Traffic rate and protocol distribution charts update in real-time.

### 2. Live Capture
- Select a network interface from the dropdown
- Optionally enter a BPF filter expression (e.g. `tcp`, `udp port 53`, `host 10.0.0.1`)
- Click **Start Capture** to begin sniffing
- Packets populate the table in real-time — double-click any row to open it in the Packet Explorer

### 3. Threat Monitor
- Alerts appear automatically as threats are detected during capture
- Click any alert row to view its **mathematical detection logic** and **remediation steps**
- The live alert stream scrolls at the bottom for continuous monitoring

### 4. AI Analysis
- Select a captured packet and click **Analyze with AI**
- The packet data is sent to Google Gemini, which returns a plain-English explanation of:
  - Protocol identification
  - Source and destination analysis
  - Suspicious activity flagging

### 5. Packet Explorer
- Deep inspection of individual packets
- View parsed headers, payload content, raw hex data, and packet length
- Hex viewer with formatted address/hex/ASCII columns

### 6. Database Logs
- Browse all historically captured packets stored in SQLite
- Full-text search across packet data

### 7. Export Center
- Export captured packets in **CSV**, **JSON**, or **PCAP** format
- PCAP files can be opened in Wireshark for further analysis

### 8. Analytics
- Four auto-refreshing charts:
  - 📈 **Traffic Rate** — Packets/sec over time
  - 🔗 **Protocol Distribution** — Pie chart of protocol types
  - 🌐 **Top Source IPs** — Most active source addresses
  - 📏 **Packet Sizes** — Size distribution across captured packets

### 9. Settings
- Configure application settings from the UI

---

## 🔍 Threat Detection Engine

The `ThreatDetector` uses **sliding window algorithms** and **threshold-based heuristics** to detect threats in real-time. Each detection runs per-packet with O(1) amortized complexity.

### Detection Architecture

```
Incoming Packet
      │
      ├── Layer 2 ─── ARP Spoofing (IP-to-MAC consistency)
      │            └── ARP Flood (rate per source MAC)
      │
      ├── Layer 3 ─── DoS / High Volume (rate per source IP)
      │            ├── TTL Anomaly (TTL ≤ 5)
      │            └── Large Packet (size > 1400 bytes)
      │
      ├── ICMP ────── ICMP Flood (rate per source)
      │
      ├── TCP ─────── Port Scan (SYN to unique ports)
      │            ├── SYN Flood (SYN rate per src→dst pair)
      │            ├── FIN Scan (FIN-only to unique ports)
      │            ├── NULL Scan (zero-flag to unique ports)
      │            └── Suspicious Port (known malware ports)
      │
      └── UDP ─────── DNS Amplification (port 53 rate)
                   └── Suspicious Port (known malware ports)
```

### Example Detection Formula

**Port Scan:**
```
UniqueDestPorts(srcIP, W) > T
  where W = 10 seconds, T = 5 ports (configurable)
```

**SYN Flood:**
```
SYNCount(srcIP → dstIP, W) > T
  where W = 5 seconds, T = 50 SYN packets
```

### Monitored Suspicious Ports

| Port | Known Association |
|------|-------------------|
| 4444 | Metasploit default listener |
| 5555 | Android ADB remote |
| 6666 | IRC backdoor |
| 6667 | IRC C&C channel |
| 12345 | NetBus trojan |
| 27374 | SubSeven trojan |
| 31337 | Back Orifice trojan |
| 65535 | Max port (scanning indicator) |

---

## 📦 Dependencies

All JARs are included in the `lib/` directory — no build tool required.

| Library | Version | Purpose | License |
|---------|---------|---------|---------|
| [pcap4j](https://github.com/kaitoy/pcap4j) | 1.0 | Packet capture (wraps libpcap/Npcap) | MIT |
| [FlatLaf](https://www.formdev.com/flatlaf/) | 3.5.2 | Modern Flat Look and Feel for Swing | Apache 2.0 |
| [JFreeChart](https://github.com/jfree/jfreechart) | 1.5.4 | Live traffic charts & analytics | LGPL |
| [SQLite JDBC](https://github.com/xerial/sqlite-jdbc) | 3.45.1.0 | Packet database persistence | Apache 2.0 |
| [JSON-java](https://github.com/stleary/JSON-java) | 20240303 | JSON export support | JSON License |

---

## 📜 License

This project is licensed under the [Apache License 2.0](LICENSE).
