package com.example.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Order
import com.example.data.model.OrderStatus
import com.example.localization.LocalizedStrings
import com.example.ui.theme.BlinkitGreenDark
import com.example.ui.theme.BlinkitGreenLight
import com.example.ui.theme.BlinkitGreenPrimary
import com.example.ui.theme.BlinkitYellowDark
import com.example.ui.theme.BlinkitYellowPrimary
import com.example.ui.viewmodel.Screen

@Composable
fun OrderTrackingScreen(
    order: Order?,
    allOrders: List<Order>,
    strings: LocalizedStrings,
    onNavigate: (Screen) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentOrder = order ?: allOrders.firstOrNull()

    if (currentOrder == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalShipping,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "No active orders yet",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Place your first grocery order and track your delivery in 10 minutes!",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Button(
                    onClick = { onNavigate(Screen.HOME) },
                    colors = ButtonDefaults.buttonColors(containerColor = BlinkitGreenPrimary)
                ) {
                    Text("Explore Groceries")
                }
            }
        }
        return
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseFraction by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rider_pulse"
    )

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("order_tracking_screen"),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ETA & Header Card
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (currentOrder.status == OrderStatus.DELIVERED) BlinkitGreenLight else BlinkitYellowPrimary.copy(alpha = 0.2f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = if (currentOrder.status == OrderStatus.DELIVERED) BlinkitGreenPrimary else BlinkitYellowPrimary,
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                text = if (currentOrder.status == OrderStatus.DELIVERED) "DELIVERED" else "FAST 10 MINS",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = if (currentOrder.status == OrderStatus.DELIVERED) Color.White else BlinkitGreenDark,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }

                        Text(
                            text = "Order #${currentOrder.orderId}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = when (currentOrder.status) {
                            OrderStatus.PLACED -> "Order placed! Preparing at Dark Store"
                            OrderStatus.PACKED -> "Groceries packed with hygienic seal"
                            OrderStatus.ASSIGNED -> "Delivery rider assigned & on way"
                            OrderStatus.OUT_FOR_DELIVERY -> "Arriving in 3 minutes! Rider is nearby"
                            OrderStatus.DELIVERED -> "Delivered on time! Enjoy your order"
                        },
                        fontSize = 17.sp,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Delivering to: ${currentOrder.deliveryAddress}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Live Route Visual Canvas
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val canvasWidth = size.width
                        val canvasHeight = size.height

                        val storePoint = Offset(canvasWidth * 0.15f, canvasHeight * 0.5f)
                        val homePoint = Offset(canvasWidth * 0.85f, canvasHeight * 0.5f)

                        // Path calculation based on order status
                        val riderProgress = when (currentOrder.status) {
                            OrderStatus.PLACED -> 0.05f
                            OrderStatus.PACKED -> 0.25f
                            OrderStatus.ASSIGNED -> 0.45f
                            OrderStatus.OUT_FOR_DELIVERY -> 0.80f
                            OrderStatus.DELIVERED -> 1.0f
                        }

                        val riderPos = Offset(
                            storePoint.x + (homePoint.x - storePoint.x) * riderProgress,
                            storePoint.y - kotlin.math.sin(riderProgress * Math.PI.toFloat()) * 30f
                        )

                        // Draw Road background line
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.6f),
                            start = storePoint,
                            end = homePoint,
                            strokeWidth = 10f,
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 15f), 0f)
                        )

                        // Draw Completed route line
                        drawLine(
                            color = Color(0xFF0C831F),
                            start = storePoint,
                            end = riderPos,
                            strokeWidth = 10f
                        )

                        // Pulse animation around rider if on the move
                        if (currentOrder.status == OrderStatus.OUT_FOR_DELIVERY || currentOrder.status == OrderStatus.ASSIGNED) {
                            drawCircle(
                                color = Color(0xFF0C831F).copy(alpha = (1f - pulseFraction) * 0.4f),
                                radius = 24f + pulseFraction * 30f,
                                center = riderPos
                            )
                        }

                        // Store Pin
                        drawCircle(
                            color = Color(0xFF1E2429),
                            radius = 16f,
                            center = storePoint
                        )

                        // Rider Indicator
                        drawCircle(
                            color = Color(0xFFF8CB46),
                            radius = 18f,
                            center = riderPos
                        )
                        drawCircle(
                            color = Color(0xFF0C831F),
                            radius = 12f,
                            center = riderPos
                        )

                        // User Home Pin
                        drawCircle(
                            color = Color(0xFFFF523B),
                            radius = 16f,
                            center = homePoint
                        )
                    }

                    // Labels for Map
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp)
                            .align(Alignment.BottomCenter),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Storefront, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Dark Store #45", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Home, contentDescription = null, modifier = Modifier.size(12.dp))
                                Spacer(modifier = Modifier.width(3.dp))
                                Text("Your House", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Stepper Progress Indicator
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Live Order Journey",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))

                    val steps = listOf(
                        Triple(OrderStatus.PLACED, strings.orderStatusPlaced, "Dark store received order"),
                        Triple(OrderStatus.PACKED, strings.orderStatusPacked, "Packed in sanitized tamper-proof bag"),
                        Triple(OrderStatus.ASSIGNED, strings.orderStatusAssigned, "${currentOrder.riderName} is assigned"),
                        Triple(OrderStatus.OUT_FOR_DELIVERY, strings.orderStatusOutForDelivery, "On the way on e-scooter"),
                        Triple(OrderStatus.DELIVERED, strings.orderStatusDelivered, "Delivered to your doorstep")
                    )

                    steps.forEachIndexed { index, (stepStatus, stepTitle, stepSub) ->
                        val isDone = currentOrder.status.ordinal >= stepStatus.ordinal
                        val isCurrent = currentOrder.status == stepStatus

                        Row(modifier = Modifier.fillMaxWidth()) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Box(
                                    modifier = Modifier
                                        .size(24.dp)
                                        .background(
                                            color = when {
                                                isDone -> BlinkitGreenPrimary
                                                else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                                            },
                                            shape = CircleShape
                                        ),
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (isDone) {
                                        Icon(
                                            imageVector = Icons.Default.CheckCircle,
                                            contentDescription = null,
                                            tint = Color.White,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                }

                                if (index < steps.size - 1) {
                                    Box(
                                        modifier = Modifier
                                            .width(2.dp)
                                            .height(28.dp)
                                            .background(
                                                if (currentOrder.status.ordinal > stepStatus.ordinal) BlinkitGreenPrimary
                                                else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
                                            )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = stepTitle,
                                    fontSize = 13.sp,
                                    fontWeight = if (isCurrent || isDone) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isDone) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = stepSub,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Rider Profile Card with Call & Delivery PIN
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(
                                shape = CircleShape,
                                color = BlinkitGreenLight,
                                modifier = Modifier.size(46.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.DeliveryDining,
                                        contentDescription = null,
                                        tint = BlinkitGreenPrimary,
                                        modifier = Modifier.size(28.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = currentOrder.riderName,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "4.9★ • ${currentOrder.riderVehicleNumber}",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        // Call & Chat buttons
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            IconButton(
                                onClick = { /* Rider call */ },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(BlinkitGreenLight, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Call,
                                    contentDescription = "Call Rider",
                                    tint = BlinkitGreenDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }

                            IconButton(
                                onClick = { onNavigate(Screen.SUPPORT_CHAT) },
                                modifier = Modifier
                                    .size(38.dp)
                                    .background(BlinkitGreenLight, CircleShape)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Chat,
                                    contentDescription = "Chat",
                                    tint = BlinkitGreenDark,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Contactless delivery OTP
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Delivery Verification PIN",
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = "Share with rider at doorstep",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = BlinkitYellowPrimary,
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = currentOrder.deliveryOtp,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = BlinkitGreenDark,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

        // Order Summary & Items
        item {
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Items in this Delivery (${currentOrder.itemCount})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentOrder.itemsSummary,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Paid via ${currentOrder.paymentMethod}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = BlinkitGreenPrimary
                        )
                        Text(
                            text = "₹${currentOrder.totalPaid.toInt()}",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
