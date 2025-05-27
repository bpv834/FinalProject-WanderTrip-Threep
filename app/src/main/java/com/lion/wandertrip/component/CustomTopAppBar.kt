package com.lion.a02_boardcloneproject.component

import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme // MaterialTheme 임포트
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.lion.wandertrip.ui.theme.NanumSquareRound

// import com.lion.wandertrip.ui.theme.NanumSquareRound // NanumSquareRound 직접 사용 대신 MaterialTheme 활용 권장

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomTopAppBar(
    modifier: Modifier = Modifier, // ✅ 외부에서 Modifier를 받을 수 있도록 추가
    title: String? = null, // ✅ 타이틀을 옵션으로 만듭니다 (Text 컴포저블을 렌더링하지 않을 수 있음)
    navigationIconImage: ImageVector? = null,
    navigationIconOnClick: () -> Unit = {},
    menuItems: @Composable RowScope.() -> Unit = {}, // actions로 이름 유지, menuItems에서 변경
    // scrollBehavior: TopAppBarScrollBehavior? = null // ✅ 스크롤 동작을 추가할 수 있습니다 (선택 사항)
) {
    TopAppBar(
        modifier = modifier.fillMaxWidth(), // ✅ 외부 Modifier를 적용하고 가로로 가득 채웁니다.
        // 타이틀
        title = {
            // 타이틀이 null이 아닐 때만 Text를 렌더링합니다.
            title?.let {
                Text(
                    text = it,
                    fontFamily = NanumSquareRound,
                )
            }
        },
        // 네비게이션 아이콘
        navigationIcon = {
            // navigationIconImage가 null이 아닐 때만 IconButton을 렌더링합니다.
            navigationIconImage?.let { icon ->
                IconButton(
                    onClick = navigationIconOnClick
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null // 실제 앱에서는 적절한 contentDescription을 제공해야 합니다.
                    )
                }
            }
        },
        // 액션 아이템들 (오른쪽 아이콘들)
        actions = menuItems, // ✅ 기존 actions 람다를 그대로 사용
        // TopAppBar의 색상 설정
        colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            // 텍스트 및 아이콘 색상을 테마에 맞게 설정
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
            actionIconContentColor = MaterialTheme.colorScheme.onSurface
        ),
        // scrollBehavior = scrollBehavior // ✅ 스크롤 동작을 활성화하려면 주석 해제
    )
}