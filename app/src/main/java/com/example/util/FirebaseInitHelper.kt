package com.example.util

import android.content.Context
import android.util.Log
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseOptions

/**
 * Ensures FirebaseApp is safely initialized across normal runtime, preview, and test environments.
 */
object FirebaseInitHelper {
    private const val TAG = "FirebaseInitHelper"

    fun ensureInitialized(context: Context): FirebaseApp? {
        return try {
            val apps = FirebaseApp.getApps(context)
            if (apps.isNotEmpty()) {
                apps.first()
            } else {
                // Initialize default Firebase App
                val app = FirebaseApp.initializeApp(context.applicationContext)
                if (app != null) {
                    Log.d(TAG, "FirebaseApp initialized successfully: ${app.name}")
                    app
                } else {
                    // Fallback initialization with project options if needed
                    val options = FirebaseOptions.Builder()
                        .setApplicationId("1:469302415416:android:9ed008ababcf91fea516cd")
                        .setApiKey("AIzaSyDmrLjO1oIi_0FAaZw5LDF7l7zma9PpRl0")
                        .setProjectId("gen-lang-client-0254795298")
                        .setStorageBucket("gen-lang-client-0254795298.firebasestorage.app")
                        .build()
                    FirebaseApp.initializeApp(context.applicationContext, options)
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "FirebaseApp initialization handled: ${e.message}")
            try {
                val apps = FirebaseApp.getApps(context)
                if (apps.isNotEmpty()) apps.first() else null
            } catch (ignored: Exception) {
                null
            }
        }
    }
}
