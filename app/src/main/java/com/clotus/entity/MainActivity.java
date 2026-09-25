package com.clotus.entity;

import android.graphics.Color;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private ClotusCore core;
    private LinearLayout content;
    private TextView statusText;
    private EditText inputField;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            core = new ClotusCore(this);
            buildUi();
            showConversation();
        } catch (Exception e) {
            setContentView(buildErrorView(e));
        }
    }

    private void buildUi() {
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setBackgroundColor(Color.parseColor("#070A12"));
        root.setPadding(24, 32, 24, 24);

        TextView title = new TextView(this);
        title.setText("CLOTUS");
        title.setTextColor(Color.parseColor("#65E6D3"));
        title.setTextSize(28f);
        title.setPadding(0, 0, 0, 12);
        root.addView(title);

        LinearLayout nav = new LinearLayout(this);
        nav.setOrientation(LinearLayout.HORIZONTAL);
        nav.setPadding(0, 0, 0, 12);

        String[] tabs = {"Conversation", "State", "Memory", "Audit"};
        for (String tab : tabs) {
            Button btn = new Button(this);
            btn.setText(tab);
            btn.setTextAllCaps(false);
            btn.setOnClickListener(v -> {
                if ("Conversation".equals(tab)) showConversation();
                else if ("State".equals(tab)) showState();
                else if ("Memory".equals(tab)) showMemory();
                else showAudit();
            });
            nav.addView(btn, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));
        }

        root.addView(nav);

        content = new LinearLayout(this);
        content.setOrientation(LinearLayout.VERTICAL);
        root.addView(content, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT,
                1f
        ));

        setContentView(root);
    }

    private void showConversation() {
        content.removeAllViews();
        content.addView(label("ENTITY RUNTIME", 18, "#65E6D3"));

        statusText = textView("CLOTUS: sistema inicializado. La identidad y la memoria están cargadas.", 18, "#F4F7FB");
        statusText.setBackgroundColor(Color.parseColor("#101A28"));
        statusText.setPadding(18, 18, 18, 18);
        content.addView(statusText);

        inputField = new EditText(this);
        inputField.setHint("Escribe a CLOTUS...");
        inputField.setTextColor(Color.WHITE);
        inputField.setHintTextColor(Color.parseColor("#B6C7D7"));
        content.addView(inputField, new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));

        Button send = new Button(this);
        send.setText("Enviar");
        send.setOnClickListener(v -> {
            try {
                String result = core.handleMessage(inputField.getText().toString());
                statusText.setText("CLOTUS: " + result);
                inputField.setText("");
            } catch (Exception e) {
                statusText.setText("ERROR: " + e.getMessage());
            }
        });
        content.addView(send);

        Button snapshot = new Button(this);
        snapshot.setText("Crear snapshot");
        snapshot.setOnClickListener(v -> {
            try {
                String id = core.createSnapshot("user_action");
                statusText.setText("SNAPSHOT: " + id);
            } catch (Exception e) {
                statusText.setText("SNAPSHOT ERROR: " + e.getMessage());
            }
        });
        content.addView(snapshot);
    }

    private void showState() {
        content.removeAllViews();
        content.addView(label("IDENTITY + RUNTIME", 18, "#65E6D3"));
        try {
            JSONObject identity = core.getIdentity();
            content.addView(textView(identity.toString(), 14, "#F4F7FB"));
            JSONObject runtime = core.getRuntimeState();
            content.addView(textView(runtime.toString(), 14, "#F4F7FB"));
            JSONObject caps = core.getCapabilities();
            content.addView(textView(caps.toString(), 14, "#F4F7FB"));
        } catch (Exception e) {
            content.addView(textView(e.toString(), 14, "#F4F7FB"));
        }
    }

    private void showMemory() {
        content.removeAllViews();
        content.addView(label("MEMORY", 18, "#65E6D3"));
        try {
            JSONArray memory = core.getMemory();
            content.addView(textView(memory.toString(), 14, "#F4F7FB"));
        } catch (Exception e) {
            content.addView(textView(e.toString(), 14, "#F4F7FB"));
        }
    }

    private void showAudit() {
        content.removeAllViews();
        content.addView(label("AUDIT", 18, "#65E6D3"));
        try {
            JSONArray audit = core.getAudit();
            content.addView(textView(audit.toString(), 14, "#F4F7FB"));
        } catch (Exception e) {
            content.addView(textView(e.toString(), 14, "#F4F7FB"));
        }
    }

    private TextView label(String text, int size, String colorHex) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextColor(Color.parseColor(colorHex));
        label.setTextSize(size);
        label.setPadding(0, 12, 0, 12);
        return label;
    }

    private TextView textView(String text, int size, String colorHex) {
        TextView tv = new TextView(this);
        tv.setText(text);
        tv.setTextColor(Color.parseColor(colorHex));
        tv.setTextSize(size);
        tv.setPadding(8, 8, 8, 8);
        return tv;
    }

    private ScrollView buildErrorView(Exception e) {
        ScrollView scroll = new ScrollView(this);
        LinearLayout container = new LinearLayout(this);
        container.setOrientation(LinearLayout.VERTICAL);
        container.setPadding(24, 32, 24, 24);

        TextView title = new TextView(this);
        title.setText("CLOTUS CORE FAILED");
        title.setTextColor(Color.RED);
        title.setTextSize(20f);
        container.addView(title);

        TextView msg = new TextView(this);
        msg.setText(e.toString());
        msg.setTextColor(Color.WHITE);
        container.addView(msg);

        scroll.addView(container);
        return scroll;
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (core != null) {
            try {
                core.persist();
            } catch (Exception ignored) {
            }
        }
    }
}
