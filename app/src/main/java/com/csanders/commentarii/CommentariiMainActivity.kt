package com.csanders.commentarii

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.csanders.commentarii.ui.shared.MenuBarScaffold
import com.csanders.commentarii.ui.theme.CommentariiTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CommentariiMainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CommentariiTheme {
              StartNav()
            }
        }
    }
}

@Composable
fun StartNav() {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        MenuBarScaffold()
    }
}
