package com.clotus.entity;

import android.content.*;
import android.os.*;
import org.json.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.text.*;
import java.util.*;

/** Core boundary: persistence, memory, integrity, snapshots and audit are not UI concerns. */
public final class ClotusCore {
    public static final String VERSION = "1.0.0";
    private final File root, stateFile, memoryFile, snapshotsDir, auditFile;
    private JSONObject identity, state;
    private JSONArray memories, audit;
    public ClotusCore(Context context) throws Exception {
        root = new File(context.getFilesDir(), "clotus"); if (!root.exists() && !root.mkdirs()) throw new IOException("Cannot create storage");
        stateFile = new File(root, "state.json"); memoryFile = new File(root, "memory.json"); snapshotsDir = new File(root, "snapshots"); auditFile = new File(root, "audit.json");
        load();
    }
    private void load() throws Exception {
        identity = readObject(stateFile); memories = readArray(memoryFile); audit = readArray(auditFile);
        if (identity == null) { identity = new JSONObject(); identity.put("id", UUID.randomUUID().toString()); identity.put("name", "CLOTUS"); identity.put("seed", UUID.randomUUID().toString()); identity.put("systemVersion", VERSION); identity.put("createdAt", now()); identity.put("status", "ACTIVE"); identity.put("protectedFields", new JSONArray(Arrays.asList("id","seed"))); state = new JSONObject(); state.put("mood", "observing"); state.put("goals", new JSONArray()); state.put("capabilities", new JSONObject()); atomicWrite(stateFile, identity); atomicWrite(new File(root,"runtime.json"), state); log("CORE", "IDENTITY_CREATED", "VERIFIED"); } else state = readObject(new File(root,"runtime.json"));
        if (state == null) state = new JSONObject();
        if (memories == null) memories = new JSONArray(); if (audit == null) audit = new JSONArray();
    }
    public synchronized JSONObject identity() { return identity; }
    public synchronized JSONArray memories() { return memories; }
    public synchronized JSONArray audit() { return audit; }
    public synchronized void remember(String text, String source) throws Exception { JSONObject m = new JSONObject(); m.put("id", UUID.randomUUID().toString()); m.put("text", text); m.put("source", source); m.put("createdAt", now()); m.put("updatedAt", now()); memories.put(m); atomicWrite(memoryFile, memories); log("MEMORY", "CREATED", "VERIFIED"); }
    public synchronized String respond(String input) throws Exception { if (input == null || input.trim().isEmpty()) return "No received input."; remember(input, "conversation"); String lower=input.toLowerCase(Locale.ROOT); String answer = lower.contains("hola") || lower.contains("hello") ? "Hola. Soy CLOTUS. Mi identidad y memoria están persistidas localmente." : lower.contains("quién") || lower.contains("quien") ? "Soy CLOTUS, una entidad local con estado auditable." : "He registrado tu mensaje en mi memoria. El procesamiento local está activo."; remember(answer, "core-response"); return answer; }
    public synchronized String createSnapshot(String reason) throws Exception { if (!snapshotsDir.exists()) snapshotsDir.mkdirs(); String id = "snapshot-" + System.currentTimeMillis(); JSONObject snap = new JSONObject(); snap.put("identity", new JSONObject(identity.toString())); snap.put("state", new JSONObject(state.toString())); snap.put("memories", new JSONArray(memories.toString())); snap.put("reason", reason); snap.put("createdAt", now()); atomicWrite(new File(snapshotsDir, id+".json"), snap); log("EVOLUTION", "SNAPSHOT_CREATED", "VERIFIED"); return id; }
    public synchronized boolean rollback(String id) throws Exception { File f=new File(snapshotsDir,id+".json"); JSONObject s=readObject(f); if(s==null) return false; identity=s.getJSONObject("identity"); state=s.getJSONObject("state"); memories=s.getJSONArray("memories"); persist(); log("EVOLUTION", "ROLLBACK_"+id, "VERIFIED"); return true; }
    public synchronized JSONObject capabilities(Context c) { JSONObject x=new JSONObject(); try { x.put("LOCAL_CORE", "VERIFIED"); x.put("IDENTITY_PERSISTENCE", stateFile.exists()?"VERIFIED":"FAILED"); x.put("MEMORY_PERSISTENCE", memoryFile.exists()?"VERIFIED":"FAILED"); x.put("CAMERA", "NOT_VERIFIED"); x.put("MICROPHONE", "NOT_VERIFIED"); x.put("STT", "NOT_VERIFIED"); x.put("TTS", "AVAILABLE"); x.put("NETWORK", "NOT_VERIFIED"); x.put("BACKGROUND", "UNAVAILABLE"); } catch(Exception ignored){} return x; }
    public synchronized void persist() throws Exception { atomicWrite(stateFile, identity); atomicWrite(new File(root,"runtime.json"),state); atomicWrite(memoryFile,memories); atomicWrite(auditFile,audit); }
    private void log(String module,String event,String result) throws Exception { JSONObject e=new JSONObject(); e.put("timestamp",now()); e.put("module",module); e.put("event",event); e.put("result",result); e.put("version",VERSION); e.put("operationId",UUID.randomUUID().toString()); audit.put(e); atomicWrite(auditFile,audit); }
    private static String now(){return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ",Locale.US).format(new Date());}
    private static JSONObject readObject(File f)throws Exception{if(!f.exists())return null;return new JSONObject(read(f));}
    private static JSONArray readArray(File f)throws Exception{if(!f.exists())return null;return new JSONArray(read(f));}
    private static String read(File f)throws Exception{return new String(java.nio.file.Files.readAllBytes(f.toPath()),StandardCharsets.UTF_8);}
    private static void atomicWrite(File f,Object value)throws Exception{File tmp=new File(f.getPath()+".tmp");try(FileOutputStream o=new FileOutputStream(tmp)){o.write(value.toString().getBytes(StandardCharsets.UTF_8));o.getFD().sync();}if(f.exists()&&!f.delete())throw new IOException("Cannot replace state");if(!tmp.renameTo(f))throw new IOException("Cannot commit state");}
}
