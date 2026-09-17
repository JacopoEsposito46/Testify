package com.example.tastify.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SharedBottomNavBar(
    selectedRoute: String = "profile",
    onRouteSelected: (String) -> Unit = {},
    unreadNotificationCount: Int = 0
) {
    NavigationBar(
        modifier = Modifier.shadow(
            elevation = 2.dp,
            shape = RectangleShape,
            clip = false
        ),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp) {
        val items = listOf(
            Triple("Updates", Icons.Default.Notifications, "updates"),
            Triple("Cookbook", Icons.AutoMirrored.Filled.MenuBook, "cookbook"),
            Triple("Home", Icons.Default.Home, "home"),
            Triple("Profile", Icons.Default.Person, "profile"),
            Triple("Chat", Icons.AutoMirrored.Filled.Chat, "chat")
        )

        items.forEach { (label, icon, route) ->
            NavigationBarItem(
                icon = {
                    if (route == "updates" && unreadNotificationCount > 0) {
                        BadgedBox(
                            badge = {
                                Badge {
                                    Text(
                                        text = if (unreadNotificationCount > 99) "99+" else unreadNotificationCount.toString(),
                                        fontSize = 10.sp
                                    )
                                }
                            }
                        ) {
                            Icon(icon, contentDescription = label)
                        }
                    } else {
                        Icon(icon, contentDescription = label)
                    }
                },
                label = { Text(label, fontSize = 10.sp) },
                selected = selectedRoute == route,
                onClick = { onRouteSelected(route) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = MaterialTheme.colorScheme.secondary,
                    unselectedIconColor = MaterialTheme.colorScheme.onSurface,
                )
            )
        }
    }
}