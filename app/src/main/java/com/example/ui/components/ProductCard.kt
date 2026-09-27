package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.LunchDining
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Sanitizer
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.Spa
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Product
import com.example.localization.AppLanguage
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.BlinkitGreenDark
import com.example.ui.theme.BlinkitGreenLight
import com.example.ui.theme.BlinkitGreenPrimary
import com.example.ui.theme.BlinkitYellowPrimary
import com.example.ui.theme.NonVegRed
import com.example.ui.theme.VegGreen

@Composable
fun ProductCard(
    product: Product,
    quantityInCart: Int,
    currentLanguage: AppLanguage,
    onAddToCart: (Product) -> Unit,
    onDecrement: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val isOutOfStock = product.stockQuantity <= 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("product_card_${product.id}"),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.4f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp)
        ) {
            // Visual header with category icon & badges
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // Category Visual Icon
                val categoryIcon: ImageVector = when (product.category) {
                    "Vegetables & Fruits" -> Icons.Default.Spa
                    "Dairy & Bread" -> Icons.Default.LunchDining
                    "Snacks & Munchies" -> Icons.Default.Restaurant
                    "Beverages & Cold Drinks" -> Icons.Default.LocalDrink
                    "Household Essentials" -> Icons.Default.Sanitizer
                    else -> Icons.Default.ShoppingBag
                }

                Icon(
                    imageVector = categoryIcon,
                    contentDescription = product.name,
                    tint = BlinkitGreenPrimary.copy(alpha = 0.7f),
                    modifier = Modifier.size(46.dp)
                )

                // Top-Left: Veg / Non-Veg indicator
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(6.dp)
                        .size(16.dp)
                        .border(
                            width = 1.5.dp,
                            color = if (product.isVeg) VegGreen else NonVegRed,
                            shape = RoundedCornerShape(3.dp)
                        )
                        .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(3.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .background(
                                color = if (product.isVeg) VegGreen else NonVegRed,
                                shape = CircleShape
                            )
                    )
                }

                // Top-Right: Discount Tag (if any)
                if (product.discountPercent > 0) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(6.dp),
                        color = BlinkitGreenPrimary,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "${product.discountPercent}% OFF",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }

                // Bottom-Left: 10 MINS tag
                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomStart)
                        .padding(6.dp),
                    color = BlinkitYellowPrimary,
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Bolt,
                            contentDescription = null,
                            tint = BlinkitGreenDark,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "${product.deliveryMinutes}m",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BlinkitGreenDark
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Unit/Weight
            Text(
                text = product.unit,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Product Name
            val displayName = if (currentLanguage != AppLanguage.ENGLISH && product.hindiName.isNotEmpty()) {
                "${product.hindiName} (${product.name})"
            } else {
                product.name
            }

            Text(
                text = displayName,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.height(40.dp)
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Rating snippet
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = BlinkitYellowPrimary,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(2.dp))
                Text(
                    text = "${product.rating}",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "(${product.ratingCount})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Price & Add to Cart button row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Price & Strike-through MRP
                Column {
                    Text(
                        text = "₹${product.price.toInt()}",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (product.originalPrice > product.price) {
                        Text(
                            text = "₹${product.originalPrice.toInt()}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textDecoration = TextDecoration.LineThrough
                        )
                    }
                }

                // Add to Cart / Stepper Button
                if (isOutOfStock) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Text(
                            text = "Out of Stock",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                        )
                    }
                } else if (quantityInCart == 0) {
                    OutlinedButton(
                        onClick = { onAddToCart(product) },
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, BlinkitGreenPrimary),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = BlinkitGreenLight,
                            contentColor = BlinkitGreenPrimary
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 14.dp,
                            vertical = 4.dp
                        ),
                        modifier = Modifier
                            .height(34.dp)
                            .testTag("add_to_cart_${product.id}")
                    ) {
                        Text(
                            text = "ADD",
                            fontWeight = FontWeight.ExtraBold,
                            fontSize = 13.sp
                        )
                    }
                } else {
                    // Stepper: [-] Qty [+]
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = BlinkitGreenPrimary,
                        modifier = Modifier.height(34.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp)
                        ) {
                            IconButton(
                                onClick = { onDecrement(product) },
                                modifier = Modifier
                                    .size(26.dp)
                                    .testTag("stepper_decrement_${product.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Remove,
                                    contentDescription = "Decrease quantity",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            Text(
                                text = "$quantityInCart",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                modifier = Modifier.padding(horizontal = 6.dp)
                            )

                            IconButton(
                                onClick = { onAddToCart(product) },
                                modifier = Modifier
                                    .size(26.dp)
                                    .testTag("stepper_increment_${product.id}")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Increase quantity",
                                    tint = Color.White,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
