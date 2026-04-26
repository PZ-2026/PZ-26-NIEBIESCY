package com.vectorpeaks.edulink


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.vectorpeaks.edulink.navigation.AppNavGraph
import com.vectorpeaks.edulink.ui.theme.EduLinkTheme
import androidx.activity.viewModels
import android.util.Log


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EduLinkTheme {
                AppNavGraph()
            }
        }
    }

}

    /*
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.users.observe(this) { list ->
            Log.d("TEST", "Pobrano użytkowników: $list")
        }

        viewModel.loadUsers()
    }

     */
