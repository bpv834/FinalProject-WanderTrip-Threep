package com.lion.wandertrip.presentation.schedule_detail_random_page.components


import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.key
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Timestamp
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.lion.wandertrip.component.ScheduleDetailCustomMarkerIcon
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.presentation.schedule_detail_random_page.ScheduleDetailRandomViewModel

// Google Map을 표시하는 Composable
@Composable
fun ScheduleDetailRandomGoogleMap(
    scheduleItems: List<ScheduleItem>,
    viewModel: ScheduleDetailRandomViewModel,
    onTouch: (Boolean) -> Unit, // 터치 이벤트 콜백
    mapScheduleDate: Timestamp,
    selectedLocation: LatLng?
) {
    val context = LocalContext.current

    // 구글 맵 카메라 상태
    val cameraPositionState = rememberCameraPositionState()

    val defaultLocation = selectedLocation!!
    // 스케줄 아이템이 있다면 첫 번째 아이템의 위치
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

    // 예를 들어, viewModel.selectedLocation이 LatLng? 타입이라고 가정합니다.
    LaunchedEffect(selectedLocation) {
        if (mapScheduleDate == viewModel.selectedDate.value)
            selectedLocation?.let { newLocation ->
                cameraPositionState.animate(
                    update = CameraUpdateFactory.newLatLngZoom(newLocation, 11f)
                )

                viewModel.selectedDate.value = null
                viewModel.selectedLocation.value = null
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
            // 마커들
            scheduleItems.forEach { item ->
                key(item.itemDocId, item.itemIndex) {
                    Marker(
                        state = rememberMarkerState(
                            position = LatLng(item.itemLatitude, item.itemLongitude)
                        ),
                        title = item.itemTitle,
                        icon = ScheduleDetailCustomMarkerIcon(context, item.itemIndex)
                    )
                }
            }
        }
    }
}
