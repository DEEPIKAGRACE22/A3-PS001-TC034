package com.duonexis.lab

import android.app.Activity
import android.graphics.Typeface
import android.os.Bundle
import android.os.FileObserver
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var labDir: File
    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private var fileObserver: FileObserver? = null
    private val timestampFormat =
        SimpleDateFormat("HH:mm:ss.SSS", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        title = "ThreatLens Early Warning"

        labDir = File(filesDir, "lab")
        labDir.mkdirs()

        buildScreen()
        startMonitoring()

        log("DuoNexis test harness started")
        log("Monitoring app-private lab directory")
        log("Directory: ${labDir.name}/")
    }

    private fun buildScreen() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 24, 24, 16)
        }

        val heading = TextView(this).apply {
            text = "ThreatLens Early Warning"
            textSize = 24f
            setTypeface(null, Typeface.BOLD)
        }
        root.addView(heading)

        statusText = TextView(this).apply {
            text = "Status: Monitoring"
            textSize = 16f
            setPadding(0, 12, 0, 16)
        }
        root.addView(statusText)

        addButton(root, "Generate 10 synthetic files") {
            generateSyntheticFiles()
        }

        addButton(root, "Modify one synthetic file") {
            modifySyntheticFile()
        }

        addButton(root, "Clear event display") {
            logText.text = ""
            log("Event display cleared")
        }

        val logHeading = TextView(this).apply {
            text = "Filesystem event log"
            textSize = 18f
            setTypeface(null, Typeface.BOLD)
            setPadding(0, 18, 0, 8)
        }
        root.addView(logHeading)

        logText = TextView(this).apply {
            textSize = 13f
            typeface = Typeface.MONOSPACE
            setTextIsSelectable(true)
        }

        val scroll = ScrollView(this)
        scroll.addView(logText)

        root.addView(
            scroll,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                0,
                1f
            )
        )

        setContentView(root)
    }

    private fun addButton(
        parent: LinearLayout,
        label: String,
        action: () -> Unit
    ) {
        val button = Button(this).apply {
            text = label
            setOnClickListener { action() }
        }

        parent.addView(
            button,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )
    }

    private fun startMonitoring() {
        val mask =
            FileObserver.CREATE or
            FileObserver.MODIFY or
            FileObserver.CLOSE_WRITE or
            FileObserver.MOVED_FROM or
            FileObserver.MOVED_TO or
            FileObserver.DELETE

        fileObserver = object : FileObserver(labDir.absolutePath, mask) {
            override fun onEvent(event: Int, path: String?) {
                val eventName = when {
                    event and FileObserver.CREATE != 0 -> "CREATE"
                    event and FileObserver.MODIFY != 0 -> "MODIFY"
                    event and FileObserver.CLOSE_WRITE != 0 -> "CLOSE_WRITE"
                    event and FileObserver.MOVED_FROM != 0 -> "MOVED_FROM"
                    event and FileObserver.MOVED_TO != 0 -> "MOVED_TO"
                    event and FileObserver.DELETE != 0 -> "DELETE"
                    else -> "OTHER"
                }

                runOnUiThread {
                    log("$eventName : ${path ?: "(no path)"}")
                }
            }
        }

        fileObserver?.startWatching()
        statusText.text = "Status: Monitoring ${labDir.name}/"
    }

    private fun generateSyntheticFiles() {
        try {
            repeat(10) { index ->
                val file = File(labDir, "sample_${index + 1}.txt")
                FileOutputStream(file).use { output ->
                    output.write(
                        "Harmless DuoNexis test file ${index + 1}\n"
                            .toByteArray(Charsets.UTF_8)
                    )
                }
            }

            log("TEST: Generated 10 synthetic text files")
        } catch (e: Exception) {
            log("ERROR generating files: ${e.message}")
        }
    }

    private fun modifySyntheticFile() {
        try {
            val file = File(labDir, "sample_1.txt")

            if (!file.exists()) {
                log("TEST: Generate synthetic files first")
                return
            }

            FileOutputStream(file, true).use { output ->
                output.write(
                    "Harmless test modification\n"
                        .toByteArray(Charsets.UTF_8)
                )
            }

            log("TEST: Modified sample_1.txt")
        } catch (e: Exception) {
            log("ERROR modifying file: ${e.message}")
        }
    }

    private fun log(message: String) {
        val time = timestampFormat.format(Date())
        logText.append("[$time] $message\n")
    }

    override fun onDestroy() {
        fileObserver?.stopWatching()
        fileObserver = null
        super.onDestroy()
    }
}
