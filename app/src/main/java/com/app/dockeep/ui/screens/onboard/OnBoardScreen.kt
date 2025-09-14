package com.app.dockeep.ui.screens.onboard

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.app.dockeep.model.OnBoardModel
import com.app.dockeep.ui.screens.onboard.components.OnBoardItem
import kotlinx.coroutines.launch


@Composable
fun OnboardingScreen(
    onDone: () -> Unit
) {
    val pages = listOf(
        OnBoardModel(
            title = "Welcome to Dockeep",
            description = "Save what matters. Keep what's yours!",
            icon = Icons.Default.Star
        ),
        OnBoardModel(
            title = "Share to Import",
            description = "Import files instantly — just share from any app. Dockeep makes it easy.",
            icon = Icons.Default.Share
        ),
        OnBoardModel(
            title = "Set Dockeep Location",
            description = "Pick your Dockeep storage location. Change it anytime.",
            icon = Icons.Default.Folder
        )
    )

    val pagerState = rememberPagerState(pageCount = { pages.size })
    val coroutineScope = rememberCoroutineScope()

    Scaffold {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(it)
        ) {

            HorizontalPager(
                state = pagerState, modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) { page ->
                OnBoardItem(pages[page])
            }

            Row(
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(10.dp)

            ) {


                TextButton(
                    onClick = {
                        val skipPage = pagerState.pageCount - 1
                        coroutineScope.launch { pagerState.animateScrollToPage(skipPage) }
                    }
                ) {
                    Text(
                        "Skip",
                        style = TextStyle(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Normal,
                        ),
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.Center, modifier = Modifier.weight(1f)
                ) {
                    repeat(pages.size) { index ->
                        val isSelected = pagerState.currentPage == index
                        Box(
                            modifier = Modifier
                                .padding(10.dp)
                                .width(if (isSelected) 18.dp else 8.dp)
                                .height(if (isSelected) 8.dp else 8.dp)
                                .border(
                                    width = 1.dp,
                                    color = Color(0xFF707784),
                                    shape = RoundedCornerShape(10.dp)
                                )
                                .background(
                                    color = if (isSelected) Color(0xFF3B6C64) else Color(0xFFFFFFFF),
                                    shape = CircleShape
                                )
                        )
                    }
                }

                if (pagerState.currentPage == pagerState.pageCount - 1) {
                    OutlinedButton(
                        onClick = onDone
                    ) {
                        Text(
                            "Start",
                            style = TextStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                        )
                    }
                } else {
                    TextButton(
                        onClick = {
                            if (pagerState.currentPage < 2) {
                                val nextPage = pagerState.currentPage + 1
                                coroutineScope.launch { pagerState.animateScrollToPage(nextPage) }
                            }
                        }
                    ) {
                        Text(
                            "Next",
                            style = TextStyle(
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Normal,
                            ),
                        )
                    }
                }

            }
        }
    }
}