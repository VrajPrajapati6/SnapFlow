# SnapFlow

### Java-Based Workflow Automation Tool

**Project Title:** SnapFlow  
**Developer:** Vraj Prajapati, Prince Patel  

---

## Overview

SnapFlow is a Java-based automation tool designed to save time by automating repetitive daily tasks.

It allows users to create **workflows (called workspaces)** that include multiple steps such as:
- Opening websites
- Launching applications
- Accessing files or folders

Instead of manually performing each action, SnapFlow executes everything automatically with a single click.

Additionally, the system:
- Monitors system health  
- Verifies links and file availability before execution  
- Provides a Chrome extension for quick URL capture  

---

## ⚙️ Working Functionality

SnapFlow works like a **setup assistant for your daily routine**.

### 🧩 Step-by-Step Working

1. User creates a workspace  
   Example: **"Morning Setup"**

2. User adds steps:
   - Website URLs  
   - File/Folder paths  
   - Delay for each step  

3. Data is stored in: 
   - snapshots.json


4. When user clicks **Deploy**:

- SnapFlow reads all steps  
- Executes each step automatically  
- Applies delay between steps  
- Opens websites in browser  
- Opens files/folders using system  

5. Background Processes:

- Checks if links are working  
- Verifies file/folder existence  
- Monitors CPU usage  

---

## 📊 Example

### 📝 Input (Workspace)

**Workspace Name:** Morning Setup  

| Step | Action | Delay |
|------|--------|------|
| 1 | https://mail.google.com | 0 ms |
| 2 | https://github.com | 500 ms |
| 3 | C:\Users\Documents | 1000 ms |

---

### ▶️ Execution Output

- Gmail opens immediately  
- After **0.5 seconds**, GitHub opens  
- After **1 second**, Documents folder opens  

✅ Everything runs automatically — no manual work needed.

---

## 🌐 Chrome Extension (Quick URL Add)

SnapFlow includes a Chrome extension to quickly add current webpages into workflows.

### 🔧 How it Works

1. Open any website in Chrome  
2. Click the **SnapFlow Extension**  
3. Extension captures current tab URL  
4. Sends it to backend  
5. URL is added as a step in workspace  

➡️ Makes workflow creation faster and easier

---

## ✨ Key Features

- 📁 Create and manage workflows (workspaces)  
- ⚡ Execute multiple steps automatically  
- 🌍 Support for websites, files, and folders  
- ⏱️ Delay control between steps  
- 🧠 Background system monitoring  
- 🔍 Automatic resource validation  
- 💾 Local storage using JSON file  
- 🔗 Chrome extension for quick URL capture  

---

## 🚀 Getting Started

### ✅ Prerequisites

- Java 17 or higher  
- Maven 3.6 or higher  

---

### ▶️ Run Locally

```bash
mvn spring-boot:run

Open browser: http://localhost:8084

🧩 Install Chrome Extension
Open Google Chrome

Go to:

chrome://extensions/
Enable Developer Mode (top right)
Click Load unpacked
Select the extension folder from the project

SnapFlow extension will be ready to use

Project Structure
src/main/java/com/snapflow/controller   → REST APIs  
src/main/java/com/snapflow/service      → Core logic  
src/main/java/com/snapflow/model        → Data models  
src/main/resources/static               → Frontend UI  
snapshots.json                          → Workflow storage  

🎯Conclusion

SnapFlow is a simple yet powerful automation tool that eliminates repetitive digital tasks.

It boosts productivity by allowing users to:

Set up their environment once
Reuse it anytime with one click

With system monitoring and Chrome extension support, SnapFlow becomes a complete and practical automation solution.
