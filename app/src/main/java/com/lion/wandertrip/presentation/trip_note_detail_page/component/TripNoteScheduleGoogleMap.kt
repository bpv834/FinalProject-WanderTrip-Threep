package com.lion.wandertrip.presentation.trip_note_detail_page.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.lion.wandertrip.component.ScheduleDetailCustomMarkerIcon
import com.lion.wandertrip.model.ScheduleItem

// Google Map을 표시하는 Composable
@Composable
fun TripNoteScheduleGoogleMap(
    scheduleItems: List<ScheduleItem>,
    onTouch: (Boolean) -> Unit // 터치 이벤트 콜백
) {
    val context = LocalContext.current

    // 구글 맵 카메라 상태
    val cameraPositionState = rememberCameraPositionState()

    // 기본 위치 (스케줄 아이템이 없을 경우)
    val defaultLocation = LatLng(37.5665, 126.9780)
    // 스케줄 아이템이 있다면 첫 번째 아이템의 위치를 중심으로 사용
    val centerLocation = if (scheduleItems.isNotEmpty()) {
        LatLng(scheduleItems.first().itemLatitude, scheduleItems.first().itemLongitude)
    } else {
        defaultLocation
    }

    // ✅ filteredItems가 바뀔 때마다 카메라 위치를 다시 설정
    LaunchedEffect(scheduleItems) {
        if (scheduleItems.isNotEmpty()) {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(centerLocation, 11f)
        } else {
            cameraPositionState.position = CameraPosition.fromLatLngZoom(defaultLocation, 11f)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    awaitPointerEventScope {
                        while (true) {
                            val event = awaitPointerEvent()
                            when (event.type) {
                                PointerEventType.Press -> onTouch(true)
                                PointerEventType.Release, PointerEventType.Exit -> onTouch(false)
                            }
                        }
                    }
                },
            cameraPositionState = cameraPositionState
        ) {
            // 각 ScheduleItem마다 마커를 추가 (커스텀 아이콘 적용)
            scheduleItems.forEach { item ->
                Marker(
                    state = rememberMarkerState(
                        position = LatLng(item.itemLatitude, item.itemLongitude)
                    ),
                    title = item.itemTitle,
                    icon = ScheduleDetailCustomMarkerIcon(context,item.itemIndex)
                )
            }
        }
    }
}
