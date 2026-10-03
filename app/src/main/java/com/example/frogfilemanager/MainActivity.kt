package com.example.frogfilemanager

import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import rikka.shizuku.Shizuku
import java.io.BufferedReader
import java.io.InputStreamReader

class MainActivity : AppCompatActivity() {

    private lateinit var statusText: TextView
    private lateinit var logText: TextView
    private lateinit var pathInput: EditText
    private val SHIZUKU_CODE = 1001

    private val shizukuPermissionListener = Shizuku.OnRequestPermissionResultListener { requestCode, grantResult ->
        if (requestCode == SHIZUKU_CODE && grantResult == PackageManager.PERMISSION_GRANTED) {
            statusText.text = "Shizuku Permission: Granted"
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        statusText = findViewById(R.id.statusText)
        logText = findViewById(R.id.logText)
        pathInput = findViewById(R.id.pathInput)
        val btnListFiles: Button = findViewById(R.id.btnListFiles)
        val btnRequestShizuku: Button = findViewById(R.id.btnRequestShizuku)

        Shizuku.addRequestPermissionResultListener(shizukuPermissionListener)

        btnRequestShizuku.setOnClickListener {
            checkAndRequestShizuku()
        }

        btnListFiles.setOnClickListener {
            val targetPath = pathInput.text.toString().ifEmpty {
                "/storage/emulated/0/Android/data/com.BabTeam.Baboon"
            }
            listFilesWithShizuku(targetPath)
        }
    }

    private fun checkAndRequestShizuku() {
        if (Shizuku.pingBinder()) {
            if (Shizuku.checkSelfPermission() == PackageManager.PERMISSION_GRANTED) {
                statusText.text = "Shizuku Ready & Authorized"
            } else {
                Shizuku.requestPermission(SHIZUKU_CODE)
            }
        } else {
            statusText.text = "Shizuku Service Not Running on Quest"
        }
    }

    private fun listFilesWithShizuku(path: String) {
        if (!Shizuku.pingBinder()) {
            logText.text = "Error: Shizuku is not running on Quest!"
            return
        }

        try {
            val process = Shizuku.newProcess(arrayOf("ls", "-la", path), null, null)
            val reader = BufferedReader(InputStreamReader(process.inputStream))
            val output = StringBuilder()
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                output.append(line).append("\n")
            }

            process.waitFor()
            logText.text = if (output.isNotEmpty()) output.toString() else "Directory empty or inaccessible."
        } catch (e: Exception) {
            logText.text = "Execution Error: ${e.message}"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Shizuku.removeRequestPermissionResultListener(shizukuPermissionListener)
    }
}
