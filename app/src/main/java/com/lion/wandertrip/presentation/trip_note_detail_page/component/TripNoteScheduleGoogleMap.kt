package com.lion.wandertrip.presentation.trip_note_detail_page.component

import android.util.Log
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.lion.wandertrip.component.ScheduleDetailCustomMarkerIcon
import com.lion.wandertrip.model.ScheduleItem
import com.lion.wandertrip.presentation.trip_note_detail_page.TripNoteDetailViewModel

// Google Map을 표시하는 Composable
@Composable
fun TripNoteScheduleGoogleMap(
    scheduleItems: List<ScheduleItem>,
    tripNoteDetailViewModel: TripNoteDetailViewModel = hiltViewModel(),
) {
    Log.d("scheduleItems", "scheduleItems${scheduleItems}")
    val context = LocalContext.current
    val cameraPositionState = rememberCameraPositionState()

    val defaultLocation = LatLng(
        tripNoteDetailViewModel.tripNoteModelValue.value.lat,
        tripNoteDetailViewModel.tripNoteModelValue.value.lng
    )
    val centerLocation = if (scheduleItems.isNotEmpty()) {
        LatLng(scheduleItems.first().itemLatitude, scheduleItems.first().itemLongitude)
    } else defaultLocation

    LaunchedEffect(scheduleItems) {
        cameraPositionState.position = CameraPosition.fromLatLngZoom(centerLocation, 11f)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()  // 부모 영역 가득 채우기
            .pointerInput(Unit) {  // 터치 입력을 감지하기 위한 Modifier
                awaitPointerEventScope {  // 이벤트 대기 및 처리 범위 시작
                    while (true) {  // 무한 루프로 지속적으로 터치 이벤트 감지
                        val event = awaitPointerEvent()  // 다음 터치 이벤트 대기 및 획득

                        if (event.type == PointerEventType.Press) {
                            // 사용자가 화면을 누르기 시작한 시점 (터치 시작)
                            Log.d("MapTouch", "🟢 지도 터치 시작됨")
                            tripNoteDetailViewModel.setMapTouched(true)  // 터치 상태 true로 설정
                        } else if (event.type == PointerEventType.Release || event.type == PointerEventType.Exit) {
                            // 사용자가 화면에서 손을 뗐거나 (Release)
                            // 터치가 화면 영역 밖으로 벗어난 경우 (Exit)
                            Log.d("MapTouch", "⚪️ 지도 터치 끝남")
                            tripNoteDetailViewModel.setMapTouched(false)  // 터치 상태 false로 설정
                        }
                    }
                }
            }
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                zoomGesturesEnabled = true,
                scrollGesturesEnabled = true,
                compassEnabled = true
            )
        ) {
            scheduleItems.forEach { item ->
                Marker(
                    state = MarkerState(position = LatLng(item.itemLatitude, item.itemLongitude)),
                    title = item.itemTitle,
                    icon = ScheduleDetailCustomMarkerIcon(context, item.itemIndex)
                )
            }
        }
    }
}
