package com.snapflow;

import com.fasterxml.jackson.core.type.*;
import com.fasterxml.jackson.databind.*;
import com.sun.management.OperatingSystemMXBean;
import org.springframework.boot.*;
import org.springframework.boot.autoconfigure.*;
import org.springframework.scheduling.annotation.*;
import org.springframework.stereotype.*;
import org.springframework.web.bind.annotation.*;

import java.awt.Desktop;
import java.io.*;
import java.lang.management.*;
import java.net.*;
import java.util.*;
import java.util.List;

@SpringBootApplication
@EnableScheduling
@RestController
@CrossOrigin("*")
@RequestMapping("/api")
public class SnapFlowApplication {

    private CoreService myService;

    public SnapFlowApplication(CoreService coreService) {
        this.myService = coreService;
    }

    public static void main(String[] args) {
        System.setProperty("java.awt.headless", "false");
        SpringApplication.run(SnapFlowApplication.class, args);
    }

    @GetMapping("/workspaces")
    public List<Workspace> getLinks() {
        return myService.getAllWorkspaces();
    }

    @PostMapping("/workspaces")
    public Workspace saveLink(@RequestBody Workspace workspace) {
        return myService.saveWorkspace(workspace);
    }

    @PutMapping("/workspaces/{id}")
    public Workspace updateLink(@PathVariable String id, @RequestBody Workspace workspace) {
        workspace.setId(id);
        return myService.saveWorkspace(workspace);
    }

    @DeleteMapping("/workspaces/{id}")
    public void deleteLink(@PathVariable String id) {
        myService.deleteWorkspace(id);
    }

    @PostMapping("/workspaces/{id}/deploy")
    public Map<String, Boolean> deploy(@PathVariable String id) {
        boolean success = myService.launchWorkspace(id);
        return Map.of("success", success);
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("cpuLoadPercent", myService.getSystemHealth());
    }

    public static class SnapItem {
        private String path;
        private int delay = 0;
        private String status = "checking...";

        public SnapItem() {
        }

        public SnapItem(String path, int delay) {
            this.path = path;
            this.delay = delay;
        }

        public String getPath() {
            return path;
        }

        public void setPath(String path) {
            this.path = path;
        }

        public int getDelay() {
            return delay;
        }

        public void setDelay(int delay) {
            this.delay = delay;
        }

        public String getStatus() {
            return status;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }

    public static class Workspace {
        private String id;
        private String title;
        private String notes = "";
        private List<SnapItem> items = new ArrayList<>();

        public Workspace() {
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getNotes() {
            return notes;
        }

        public void setNotes(String notes) {
            this.notes = notes;
        }

        public List<SnapItem> getItems() {
            return items;
        }

        public void setItems(List<SnapItem> items) {
            this.items = items;
        }
    }

    public interface MyMonitor {
        String captureStats();
    }

    public static class WindowsMonitor implements MyMonitor {
        private OperatingSystemMXBean bean = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();

        @Override
        public String captureStats() {
            double load = bean.getCpuLoad();
            return String.format("%.1f%%", Math.max(0, load * 100));
        }
    }

    @Service
    public static class CoreService {
        private File myDataFile = new File("snapshots.json");
        private ObjectMapper jsonMapper = new ObjectMapper();
        private MyMonitor monitor = new WindowsMonitor();
        private String currentCpuLoad = "0.0%";

        @Scheduled(fixedRate = 7000)
        public void runBackgroundTasks() {
            currentCpuLoad = monitor.captureStats();
            checkAllResources();
        }

        private void checkAllResources() {
            List<Workspace> workspaces = getAllWorkspaces();
            boolean changed = false;

            for (Workspace ws : workspaces) {
                for (SnapItem item : ws.getItems()) {
                    String oldStatus = item.getStatus();
                    String path = item.getPath().trim();

                    if (path.toLowerCase().startsWith("http")) {
                        item.setStatus(checkUrl(path) ? "online" : "offline");
                    } else {
                        File f = new File(path);
                        item.setStatus(f.exists() ? "ready" : "missing");
                    }
                    if (!item.getStatus().equals(oldStatus))
                        changed = true;
                }
            }
            if (changed) {
                try {
                    jsonMapper.writeValue(myDataFile, workspaces);
                } catch (Exception e) {
                }
            }
        }

        private boolean checkUrl(String urlString) {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setConnectTimeout(4000);
                conn.setReadTimeout(4000);
                conn.setRequestProperty("User-Agent", "Mozilla/5.0");

                int code = conn.getResponseCode();
                return (code > 0);
            } catch (Exception e) {
                return false;
            }
        }

        public String getSystemHealth() {
            return currentCpuLoad;
        }

        public List<Workspace> getAllWorkspaces() {
            try {
                if (!myDataFile.exists())
                    return new ArrayList<>();
                return jsonMapper.readValue(myDataFile, new TypeReference<List<Workspace>>() {
                });
            } catch (Exception e) {
                return new ArrayList<>();
            }
        }

        public Workspace saveWorkspace(Workspace ws) {
            List<Workspace> all = getAllWorkspaces();
            if (ws.getId() == null || ws.getId().isEmpty())
                ws.setId(UUID.randomUUID().toString());

            for (int i = 0; i < all.size(); i++) {
                if (all.get(i).getId().equals(ws.getId())) {
                    all.remove(i);
                    break;
                }
            }
            all.add(ws);
            try {
                jsonMapper.writeValue(myDataFile, all);
            } catch (Exception e) {
            }
            return ws;
        }

        public void deleteWorkspace(String id) {
            List<Workspace> all = getAllWorkspaces();
            all.removeIf(w -> w.getId().equals(id));
            try {
                jsonMapper.writeValue(myDataFile, all);
            } catch (Exception e) {
            }
        }

        public boolean launchWorkspace(String id) {
            Workspace found = getAllWorkspaces().stream().filter(w -> w.getId().equals(id)).findFirst().orElse(null);
            if (found == null || !Desktop.isDesktopSupported())
                return false;

            for (SnapItem item : found.getItems()) {
                new Thread(() -> {
                    try {
                        if (item.getDelay() > 0)
                            Thread.sleep(item.getDelay());

                        Desktop d = Desktop.getDesktop();
                        String path = item.getPath().trim();
                        if (path.toLowerCase().startsWith("http")) {
                            d.browse(new URI(path));
                        } else {
                            File f = new File(path);
                            if (f.exists())
                                d.open(f);
                        }
                    } catch (Exception e) {
                    }
                }).start();
            }
            return true;
        }
    }
}
