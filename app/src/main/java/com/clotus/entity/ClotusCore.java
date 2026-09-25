package com.clotus.entity;

import android.content.Context;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.File;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public final class ClotusCore {
    public static final String VERSION = "1.0.0";

    private final File rootDir;
    private final File identityFile;
    private final File memoryFile;
    private final File snapshotDir;
    private final File auditFile;
    private final File runtimeFile;

    private JSONObject identity;
    private JSONObject runtimeState;
    private JSONArray memory;
    private JSONArray audit;

    public ClotusCore(Context context) throws Exception {
        rootDir = new File(context.getFilesDir(), "clotus");
        if (!rootDir.exists() && !rootDir.mkdirs()) {
            throw new IllegalStateException("Unable to create CLOTUS base directory");
        }

        identityFile = new File(rootDir, "identity.json");
        memoryFile = new File(rootDir, "memory.json");
        snapshotDir = new File(rootDir, "snapshots");
        auditFile = new File(rootDir, "audit.json");
        runtimeFile = new File(rootDir, "runtime.json");

        if (!snapshotDir.exists() && !snapshotDir.mkdirs()) {
            throw new IllegalStateException("Unable to create snapshot directory");
        }

        loadState();
    }

    public synchronized JSONObject getIdentity() {
        return identity;
    }

    public synchronized JSONObject getRuntimeState() {
        return runtimeState;
    }

    public synchronized JSONArray getMemory() {
        return memory;
    }

    public synchronized JSONArray getAudit() {
        return audit;
    }

    public synchronized JSONObject getCapabilities() {
        JSONObject capabilities = new JSONObject();
        capabilities.put("CORE_RUNTIME", "VERIFIED");
        capabilities.put("IDENTITY_PERSISTENCE", identityFile.exists() ? "VERIFIED" : "FAILED");
        capabilities.put("MEMORY_PERSISTENCE", memoryFile.exists() ? "VERIFIED" : "FAILED");
        capabilities.put("RUNTIME_PERSISTENCE", runtimeFile.exists() ? "VERIFIED" : "FAILED");
        capabilities.put("SNAPSHOT_SUPPORT", snapshotDir.exists() ? "VERIFIED" : "FAILED");
        capabilities.put("CAMERA", "NOT_VERIFIED");
        capabilities.put("MICROPHONE", "NOT_VERIFIED");
        capabilities.put("STT", "NOT_VERIFIED");
        capabilities.put("TTS", "NOT_VERIFIED");
        capabilities.put("NETWORK", "NOT_VERIFIED");
        capabilities.put("BACKGROUND", "UNAVAILABLE");
        capabilities.put("DEVICE_STORAGE", "VERIFIED");
        return capabilities;
    }

    public synchronized String handleMessage(String value) throws Exception {
        if (value == null || value.trim().isEmpty()) {
            return "No input received.";
        }

        String trimmed = value.trim();
        remember(trimmed, "user_message");

        String lower = trimmed.toLowerCase(Locale.ROOT);
        String response;
        if (lower.contains("hola") || lower.contains("hello")) {
            response = "Hola. Soy CLOTUS y mi identidad y memoria están persistidas localmente.";
        } else if (lower.contains("estado") || lower.contains("status")) {
            response = "Mi estado está cargado. La identidad, la memoria y los snapshots están activos.";
        } else if (lower.contains("memoria") || lower.contains("memory")) {
            response = "He registrado tu mensaje en la memoria local de CLOTUS.";
        } else {
            response = "He procesado tu mensaje y lo he registrado en el núcleo funcional de CLOTUS.";
        }

        remember(response, "clotus_response");
        logEvent("conversation", "message_processed", "VERIFIED");
        persist();
        return response;
    }

    public synchronized String createSnapshot(String reason) throws Exception {
        String snapshotId = "snapshot-" + System.currentTimeMillis();
        File snapshotFile = new File(snapshotDir, snapshotId + ".json");

        JSONObject snapshot = new JSONObject();
        snapshot.put("snapshotId", snapshotId);
        snapshot.put("reason", reason != null ? reason : "unspecified");
        snapshot.put("createdAt", now());
        snapshot.put("identity", new JSONObject(identity.toString()));
        snapshot.put("runtimeState", new JSONObject(runtimeState.toString()));
        snapshot.put("memory", new JSONArray(memory.toString()));

        atomicWrite(snapshotFile, snapshot);
        logEvent("evolution", "snapshot_created", "VERIFIED");
        return snapshotId;
    }

    public synchronized boolean rollbackSnapshot(String snapshotId) throws Exception {
        File snapshotFile = new File(snapshotDir, snapshotId + ".json");
        if (!snapshotFile.exists()) {
            return false;
        }

        JSONObject snapshot = readJsonObject(snapshotFile);
        if (snapshot == null) {
            return false;
        }

        identity = snapshot.getJSONObject("identity");
        runtimeState = snapshot.getJSONObject("runtimeState");
        memory = snapshot.getJSONArray("memory");

        persist();
        logEvent("evolution", "snapshot_restored", "VERIFIED");
        return true;
    }

    public synchronized void remember(String text, String source) throws Exception {
        JSONObject item = new JSONObject();
        item.put("id", UUID.randomUUID().toString());
        item.put("text", text);
        item.put("source", source);
        item.put("createdAt", now());
        memory.put(item);
        persist();
    }

    public synchronized void persist() throws Exception {
        atomicWrite(identityFile, identity);
        atomicWrite(runtimeFile, runtimeState);
        atomicWrite(memoryFile, memory);
        atomicWrite(auditFile, audit);
    }

    private void loadState() throws Exception {
        identity = readJsonObject(identityFile);
        runtimeState = readJsonObject(runtimeFile);
        memory = readJsonArray(memoryFile);
        audit = readJsonArray(auditFile);

        boolean created = false;
        if (identity == null) {
            identity = new JSONObject();
            identity.put("id", UUID.randomUUID().toString());
            identity.put("name", "CLOTUS");
            identity.put("seed", UUID.randomUUID().toString());
            identity.put("systemVersion", VERSION);
            identity.put("status", "ACTIVE");
            identity.put("createdAt", now());
            identity.put("protectedFields", new JSONArray().put("id").put("seed").put("systemVersion"));
            created = true;
        }

        if (runtimeState == null) {
            runtimeState = new JSONObject();
            runtimeState.put("appState", "READY");
            runtimeState.put("mood", "observing");
            runtimeState.put("autonomy", "ACTIVE_WHILE_APP_IS_RUNNING");
            runtimeState.put("memoryLoaded", true);
            runtimeState.put("identityLoaded", true);
        }

        if (memory == null) {
            memory = new JSONArray();
        }

        if (audit == null) {
            audit = new JSONArray();
        }

        if (created) {
            logEvent("identity", "created", "VERIFIED");
        } else {
            logEvent("identity", "loaded", "VERIFIED");
        }

        persist();
    }

    private void logEvent(String module, String event, String result) throws Exception {
        JSONObject entry = new JSONObject();
        entry.put("timestamp", now());
        entry.put("module", module);
        entry.put("event", event);
        entry.put("result", result);
        entry.put("version", VERSION);
        audit.put(entry);
    }

    private static String now() {
        return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ", Locale.US).format(new Date());
    }

    private static JSONObject readJsonObject(File file) throws Exception {
        if (!file.exists() || file.length() == 0) {
            return null;
        }
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        if (content.trim().isEmpty()) {
            return null;
        }
        return new JSONObject(content);
    }

    private static JSONArray readJsonArray(File file) throws Exception {
        if (!file.exists() || file.length() == 0) {
            return null;
        }
        String content = new String(java.nio.file.Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8);
        if (content.trim().isEmpty()) {
            return null;
        }
        return new JSONArray(content);
    }

    private static void atomicWrite(File file, Object value) throws Exception {
        File temp = new File(file.getAbsolutePath() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(temp)) {
            out.write(value.toString().getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        }

        if (file.exists() && !file.delete()) {
            throw new IllegalStateException("Unable to replace existing file: " + file.getAbsolutePath());
        }

        if (!temp.renameTo(file)) {
            throw new IllegalStateException("Unable to commit file: " + file.getAbsolutePath());
        }
    }
}
