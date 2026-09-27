package com.example.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BlinkitGreenLight
import com.example.ui.theme.BlinkitGreenPrimary
import com.example.ui.viewmodel.Screen

@Composable
fun AppBottomNavigation(
    currentScreen: Screen,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    NavigationBar(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 6.dp
    ) {
        NavigationBarItem(
            selected = currentScreen == Screen.HOME,
            onClick = { onNavigate(Screen.HOME) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Store",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Screen.HOME) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BlinkitGreenPrimary,
                selectedTextColor = BlinkitGreenPrimary,
                indicatorColor = BlinkitGreenLight
            ),
            modifier = Modifier.testTag("nav_home_tab")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.SEARCH_FILTER,
            onClick = { onNavigate(Screen.SEARCH_FILTER) },
            icon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search & Categories",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Explore",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Screen.SEARCH_FILTER) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BlinkitGreenPrimary,
                selectedTextColor = BlinkitGreenPrimary,
                indicatorColor = BlinkitGreenLight
            ),
            modifier = Modifier.testTag("nav_search_tab")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.LOYALTY_CLUB,
            onClick = { onNavigate(Screen.LOYALTY_CLUB) },
            icon = {
                Icon(
                    imageVector = Icons.Default.MilitaryTech,
                    contentDescription = "Loyalty Club",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Club",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Screen.LOYALTY_CLUB) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BlinkitGreenPrimary,
                selectedTextColor = BlinkitGreenPrimary,
                indicatorColor = BlinkitGreenLight
            ),
            modifier = Modifier.testTag("nav_loyalty_tab")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.ORDER_TRACKING,
            onClick = { onNavigate(Screen.ORDER_TRACKING) },
            icon = {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = "Track Orders",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Track",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Screen.ORDER_TRACKING) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BlinkitGreenPrimary,
                selectedTextColor = BlinkitGreenPrimary,
                indicatorColor = BlinkitGreenLight
            ),
            modifier = Modifier.testTag("nav_orders_tab")
        )

        NavigationBarItem(
            selected = currentScreen == Screen.SUPPORT_CHAT,
            onClick = { onNavigate(Screen.SUPPORT_CHAT) },
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Chat,
                    contentDescription = "Live Support",
                    modifier = Modifier.size(22.dp)
                )
            },
            label = {
                Text(
                    text = "Support",
                    fontSize = 11.sp,
                    fontWeight = if (currentScreen == Screen.SUPPORT_CHAT) FontWeight.Bold else FontWeight.Normal
                )
            },
            colors = NavigationBarItemDefaults.colors(
                selectedIconColor = BlinkitGreenPrimary,
                selectedTextColor = BlinkitGreenPrimary,
                indicatorColor = BlinkitGreenLight
            ),
            modifier = Modifier.testTag("nav_support_tab")
        )
    }
}
