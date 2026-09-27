package com.example.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Inventory
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Store
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.data.model.Product
import com.example.localization.LocalizedStrings
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.BlinkitGreenDark
import com.example.ui.theme.BlinkitGreenLight
import com.example.ui.theme.BlinkitGreenPrimary
import com.example.ui.theme.BlinkitYellowDark
import com.example.ui.theme.BlinkitYellowPrimary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminInventoryScreen(
    products: List<Product>,
    orders: List<Order>,
    strings: LocalizedStrings,
    onStockDelta: (String, Int) -> Unit,
    onToggleStock: (String) -> Unit,
    onUpdateOrderStatus: (String, OrderStatus) -> Unit,
    onBackToStore: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableStateOf(0) } // 0: Inventory, 1: Live Orders
    var searchKeyword by remember { mutableStateOf("") }

    val lowStockCount = products.count { it.stockQuantity <= 10 }
    val outOfStockCount = products.count { it.stockQuantity == 0 }
    val activeOrdersCount = orders.count { it.status != OrderStatus.DELIVERED }
    val totalRevenue = orders.sumOf { it.totalPaid }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "Dark Store Admin Hub",
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Sector 45 Micro-Fulfillment Center",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBackToStore) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back to Store"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        modifier = modifier.testTag("admin_inventory_screen")
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Metrics Overview Cards Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Total SKUs
                MetricCard(
                    title = "Total SKUs",
                    value = "${products.size}",
                    subtitle = "$outOfStockCount OOS",
                    icon = Icons.Default.Inventory,
                    color = BlinkitGreenPrimary,
                    modifier = Modifier.weight(1f)
                )

                // Active Orders
                MetricCard(
                    title = "Live Queue",
                    value = "$activeOrdersCount",
                    subtitle = "Orders",
                    icon = Icons.Default.LocalShipping,
                    color = BlinkitYellowDark,
                    modifier = Modifier.weight(1f)
                )

                // Today's Sales
                MetricCard(
                    title = "Gross Revenue",
                    value = "₹${totalRevenue.toInt()}",
                    subtitle = "Dispatched",
                    icon = Icons.Default.AttachMoney,
                    color = BlinkitGreenDark,
                    modifier = Modifier.weight(1f)
                )
            }

            // Tab Row
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = BlinkitGreenPrimary
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = "Inventory Stock (${products.size})",
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = "Live Orders Queue (${orders.size})",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 13.sp
                        )
                    }
                )
            }

            if (selectedTab == 0) {
                // Search inventory bar
                OutlinedTextField(
                    value = searchKeyword,
                    onValueChange = { searchKeyword = it },
                    placeholder = { Text("Filter SKU by name or category...", fontSize = 12.sp) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    shape = RoundedCornerShape(10.dp),
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    singleLine = true
                )

                val displayProducts = products.filter {
                    searchKeyword.isBlank() ||
                            it.name.contains(searchKeyword, ignoreCase = true) ||
                            it.category.contains(searchKeyword, ignoreCase = true)
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(displayProducts) { prod ->
                        Card(
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = prod.name,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 13.sp
                                    )
                                    Text(
                                        text = "${prod.category} • ${prod.unit} • ₹${prod.price.toInt()}",
                                        fontSize = 11.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(2.dp))
                                    Text(
                                        text = if (prod.stockQuantity > 0) "Current Stock: ${prod.stockQuantity} units" else "OUT OF STOCK",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (prod.stockQuantity > 10) BlinkitGreenPrimary else if (prod.stockQuantity > 0) AccentOrange else Color.Red
                                    )
                                }

                                // Quick Stock Increment / Decrement
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    IconButton(
                                        onClick = { onStockDelta(prod.id, -10) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Remove,
                                            contentDescription = "Reduce stock",
                                            tint = MaterialTheme.colorScheme.error
                                        )
                                    }

                                    Surface(
                                        color = MaterialTheme.colorScheme.surfaceVariant,
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = "${prod.stockQuantity}",
                                            fontSize = 13.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                        )
                                    }

                                    IconButton(
                                        onClick = { onStockDelta(prod.id, 10) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Add,
                                            contentDescription = "Add stock",
                                            tint = BlinkitGreenPrimary
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(4.dp))

                                    Switch(
                                        checked = prod.stockQuantity > 0,
                                        onCheckedChange = { onToggleStock(prod.id) },
                                        colors = SwitchDefaults.colors(
                                            checkedThumbColor = BlinkitGreenPrimary,
                                            checkedTrackColor = BlinkitGreenLight
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            } else {
                // Live Orders Queue
                if (orders.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No customer orders received yet.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(orders) { ord ->
                            Card(
                                shape = RoundedCornerShape(12.dp),
                                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            text = "Order #${ord.orderId}",
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.sp
                                        )
                                        Surface(
                                            color = when (ord.status) {
                                                OrderStatus.DELIVERED -> BlinkitGreenLight
                                                OrderStatus.OUT_FOR_DELIVERY -> BlinkitYellowPrimary.copy(alpha = 0.2f)
                                                else -> MaterialTheme.colorScheme.surfaceVariant
                                            },
                                            shape = RoundedCornerShape(4.dp)
                                        ) {
                                            Text(
                                                text = ord.status.name,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = when (ord.status) {
                                                    OrderStatus.DELIVERED -> BlinkitGreenDark
                                                    OrderStatus.OUT_FOR_DELIVERY -> BlinkitYellowDark
                                                    else -> MaterialTheme.colorScheme.onSurface
                                                },
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = ord.itemsSummary,
                                        fontSize = 12.sp,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Address: ${ord.deliveryAddress} • Total: ₹${ord.totalPaid.toInt()}",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Medium
                                    )

                                    Spacer(modifier = Modifier.height(10.dp))

                                    // Action status buttons
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                                    ) {
                                        if (ord.status == OrderStatus.PLACED) {
                                            Button(
                                                onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.PACKED) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BlinkitYellowPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Pack Order", fontSize = 11.sp, color = BlinkitGreenDark, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (ord.status == OrderStatus.PACKED) {
                                            Button(
                                                onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.ASSIGNED) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BlinkitGreenPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Assign Rider", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (ord.status == OrderStatus.ASSIGNED) {
                                            Button(
                                                onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.OUT_FOR_DELIVERY) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BlinkitGreenPrimary),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Dispatch Out", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        } else if (ord.status == OrderStatus.OUT_FOR_DELIVERY) {
                                            Button(
                                                onClick = { onUpdateOrderStatus(ord.orderId, OrderStatus.DELIVERED) },
                                                colors = ButtonDefaults.buttonColors(containerColor = BlinkitGreenDark),
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.weight(1f)
                                            ) {
                                                Text("Mark Delivered", fontSize = 11.sp, color = Color.White, fontWeight = FontWeight.Bold)
                                            }
                                        } else {
                                            Surface(
                                                color = BlinkitGreenLight,
                                                shape = RoundedCornerShape(8.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    text = "✓ Order Fulfilled Successfully",
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = BlinkitGreenDark,
                                                    modifier = Modifier.padding(8.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 1.dp,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                fontSize = 16.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = title,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
