package com.example.gridscrollsynchronizerexample

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.gridscrollsynchronizerexample.utils.GridScrollSynchronizer

@Composable
fun PagerGridSyncExample(
    modifier: Modifier = Modifier
) {
    val pageCount = 3
    val columns = 3
    val pagerState = rememberPagerState(initialPage = 0, pageCount = { pageCount })
    val scope = rememberCoroutineScope()

    val syncer = remember { GridScrollSynchronizer() }

    val items = remember { (0 until 50).toList() }

    Column(modifier.fillMaxSize()) {
        Text(
            text = "HorizontalPager + Grid scroll-sync",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(12.dp)
        )

        HorizontalPager(
            state = pagerState,
            pageSpacing = 8.dp,
            contentPadding = PaddingValues(horizontal = 12.dp),
            modifier = Modifier.fillMaxSize()
        ) { pageIndex ->
            val gridState = rememberLazyGridState()
            LaunchedEffect(Unit) { syncer.bind(gridState, scope) }
            LazyVerticalGrid(
                columns = GridCells.Fixed(columns),
                state = gridState,
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        when (pageIndex % 3) {
                            0 -> Color(0xFFF6F6F6)
                            1 -> Color(0xFFF9FAF9)
                            else -> Color(0xFFFFFBF4)
                        }
                    ),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                items(items, key = { it }) { i ->
                    GridBox(
                        label = "Page ${pageIndex + 1} • #$i",
                        tint = when (pageIndex % 3) {
                            0 -> Color(0xFFE3F2FD)
                            1 -> Color(0xFFE8F5E9)
                            else -> Color(0xFFFFF3E0)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun GridBox(
    label: String,
    tint: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .aspectRatio(0.6f)
            .clip(RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        colors = CardDefaults.cardColors(
            containerColor = tint
        ),
        border = BorderStroke(
            width = 1.dp,
            color = Color(0x2979747E)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(label)
        }
    }
}
