package com.zelgius.openplayer

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.Icon
import androidx.compose.foundation.Text
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.setContent
import androidx.compose.ui.unit.dp
import com.zelgius.openplayer.navigation.NavigationHost
import com.zelgius.openplayer.ui.OpenPlayerTheme

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            OpenPlayerTheme {
                AppBar {
                    // A surface container using the 'background' color from the theme
                    Surface(color = MaterialTheme.colors.background) {
                        NavigationHost(onBackPressedDispatcher)
                    }
                }
            }
        }
    }
}

@Composable
fun AppBar(body: @Composable () -> Unit) {
    val state = rememberScaffoldState()
    Scaffold(
        scaffoldState = state,
        topBar = {
            TopAppBar(
                title = { Text(text = "AppBar") },
                navigationIcon = {
                    Icon(
                        Icons.Default.Menu,
                        modifier = Modifier.clickable(onClick = {
                            state.drawerState.open() // or toggle
                        })
                    )
                }
            )
        },
        drawerShape = RoundedCornerShape(topRight = 10.dp, bottomRight = 10.dp),
        drawerContent = {
            Text(text = "Drawer")
        }
    ) {
        body()
    }
}