import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp

@Composable
fun SearchItemCategoryChips(
    selectedCategoryCode: String?,
    onCategorySelected: (String?) -> Unit
) {
    // 🔹 항상 고정된 4개의 카테고리만 표시
    val categories = listOf("관광지", "숙소", "맛집", "여행기")

    LazyRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        items(categories) { category ->
            val isSelected = selectedCategoryCode == category

            FilterChip(
                selected = isSelected,
                onClick = { onCategorySelected(if (isSelected) null else category) },
                label = {
                    Text(
                        text = category,
                        fontSize = 14.sp
                    )
                },
                modifier = Modifier.padding(horizontal = 4.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = Color(0xFF435C8F),
                    containerColor = Color.White,
                    labelColor = Color.Black,
                    selectedLabelColor = Color.White
                )
            )
        }
    }

}
