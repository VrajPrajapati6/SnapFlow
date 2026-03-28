const API = "http://localhost:8084/api/workspaces";

// Load the current list of snaps from the backend
async function load() {
    try {
        const res = await fetch(API);
        if (!res.ok) throw new Error("Server not reached");
        const list = await res.json();
        const select = document.getElementById("ws-select");
        
        if (list.length === 0) {
            select.innerHTML = "<option value='none'>No existing snaps</option>";
        } else {
            select.innerHTML = list.map(w => `<option value="${w.id}">${w.title || 'Untitled'}</option>`).join("");
        }
        document.getElementById("snap-btn").disabled = false;
    } catch (e) {
        document.getElementById("ws-select").innerHTML = `<option>Error: Start Java App</option>`;
        const msg = document.getElementById("msg");
        msg.textContent = "Please start SnapFlow on port 8084";
        msg.style.color = "#ef4444";
    }
}

document.getElementById("snap-btn").onclick = async () => {
    const btn = document.getElementById("snap-btn");
    const wsId = document.getElementById("ws-select").value;
    const newTitle = document.getElementById("new-title").value.trim();
    const delay = parseInt(document.getElementById("delay-ms").value || "0");
    const msg = document.getElementById("msg");
    
    btn.disabled = true; 
    btn.textContent = "Processing...";

    try {
        // Get the active tab details
        const tabs = await chrome.tabs.query({ active: true, currentWindow: true });
        if (!tabs || tabs.length === 0) throw new Error("No active tab found");
        const tab = tabs[0];
        
        // Item to be added
        const newItem = { 
            path: tab.url, 
            delay: delay, 
            status: "checking..." 
        };

        if (newTitle !== "") {
            // OPTION A: Create a brand new workspace
            const newWorkspace = {
                title: newTitle,
                notes: "Captured via browser extension",
                items: [newItem]
            };

            const postRes = await fetch(API, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(newWorkspace)
            });

            if (postRes.ok) {
                msg.textContent = "Created new snap: " + newTitle;
                msg.style.color = "#22c55e";
                setTimeout(() => window.close(), 1500);
            } else {
                throw new Error("Failed to create new snap");
            }
        } else if (wsId !== 'none') {
            // OPTION B: Add to an existing workspace
            const res = await fetch(API);
            const list = await res.json();
            const ws = list.find(w => w.id === wsId);
            
            if (ws) {
                if (!ws.items) ws.items = [];
                ws.items.push(newItem);
                
                const putRes = await fetch(`${API}/${wsId}`, {
                    method: "PUT",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(ws)
                });
                
                if (putRes.ok) {
                    msg.textContent = "Snapped to: " + (ws.title || wsId);
                    msg.style.color = "#22c55e";
                    setTimeout(() => window.close(), 1500);
                } else {
                    throw new Error("Failed to update existing snap");
                }
            } else {
                throw new Error("Could not find the selected snap");
            }
        } else {
            throw new Error("Enter a new title or select existing");
        }
    } catch (e) {
        msg.textContent = "Error: " + e.message;
        msg.style.color = "#ef4444";
        btn.disabled = false; 
        btn.textContent = "Snap This Tab";
    }
};

load();
