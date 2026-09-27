package com.example.ui.screens

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedFilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CartItem
import com.example.data.model.Product
import com.example.localization.AppLanguage
import com.example.localization.LocalizedStrings
import com.example.ui.components.ProductCard
import com.example.ui.components.SearchBarWithFilter
import com.example.ui.theme.BlinkitGreenLight
import com.example.ui.theme.BlinkitGreenPrimary
import com.example.ui.viewmodel.SortOption

@Composable
fun SearchFilterScreen(
    products: List<Product>,
    cartItems: List<CartItem>,
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit,
    selectedCategory: String?,
    onCategorySelect: (String?) -> Unit,
    isVegOnly: Boolean,
    onVegOnlyToggle: (Boolean) -> Unit,
    currentSort: SortOption,
    onSortChange: (SortOption) -> Unit,
    strings: LocalizedStrings,
    currentLanguage: AppLanguage,
    onProductAdd: (Product) -> Unit,
    onProductDecrement: (Product) -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryList = listOf(
        null to "All Essentials",
        "Vegetables & Fruits" to "Veggies & Fruits",
        "Dairy & Bread" to "Dairy & Bread",
        "Snacks & Munchies" to "Snacks",
        "Instant Food" to "Instant Food",
        "Beverages & Cold Drinks" to "Drinks & Tea",
        "Household Essentials" to "Household"
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .testTag("search_filter_screen")
    ) {
        // Search & Filter controls
        SearchBarWithFilter(
            query = searchQuery,
            onQueryChange = onSearchQueryChange,
            placeholder = strings.searchPlaceholder,
            strings = strings,
            isVegOnly = isVegOnly,
            onVegOnlyToggle = onVegOnlyToggle,
            currentSort = currentSort,
            onSortChange = onSortChange
        )

        // Category Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            categoryList.forEach { (catKey, catLabel) ->
                val isSelected = selectedCategory == catKey
                ElevatedFilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelect(catKey) },
                    label = {
                        Text(
                            text = catLabel,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.elevatedFilterChipColors(
                        selectedContainerColor = BlinkitGreenPrimary,
                        selectedLabelColor = androidx.compose.ui.graphics.Color.White
                    ),
                    modifier = Modifier.testTag("category_chip_${catKey ?: "all"}")
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Product Grid or Empty State
        if (products.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.SearchOff,
                        contentDescription = "No products found",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "No groceries match your search",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Try clearing filters or search for milk, atta, or vegetables",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedButton(
                        onClick = {
                            onSearchQueryChange("")
                            onCategorySelect(null)
                            onVegOnlyToggle(false)
                        },
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = BlinkitGreenPrimary
                        )
                    ) {
                        Text("Reset All Filters")
                    }
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("filtered_products_grid"),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 90.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(products) { prod ->
                    val qty = cartItems.find { it.productId == prod.id }?.quantity ?: 0
                    ProductCard(
                        product = prod,
                        quantityInCart = qty,
                        currentLanguage = currentLanguage,
                        onAddToCart = onProductAdd,
                        onDecrement = onProductDecrement
                    )
                }
            }
        }
    }
}
