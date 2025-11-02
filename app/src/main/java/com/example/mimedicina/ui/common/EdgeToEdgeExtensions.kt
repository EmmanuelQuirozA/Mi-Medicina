package com.example.mimedicina.ui.common

import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlin.math.max

fun AppCompatActivity.applyEdgeToEdge(root: View) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    ViewCompat.setOnApplyWindowInsetsListener(root) { view, insets ->
        val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
        view.setPadding(
            systemBars.left,
            systemBars.top,
            systemBars.right,
            max(systemBars.bottom, ime.bottom)
        )
        insets
    }
    ViewCompat.requestApplyInsets(root)
}
