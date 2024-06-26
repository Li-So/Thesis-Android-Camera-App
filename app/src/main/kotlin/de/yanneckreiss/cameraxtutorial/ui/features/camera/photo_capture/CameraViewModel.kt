package de.yanneckreiss.cameraxtutorial.ui.features.camera.photo_capture

import android.graphics.Bitmap
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import de.yanneckreiss.cameraxtutorial.data.usecases.SavePhotoToGalleryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.koin.android.annotation.KoinViewModel

@KoinViewModel
class CameraViewModel(
    private val savePhotoToGalleryUseCase: SavePhotoToGalleryUseCase
) : ViewModel() {

    private val _state = MutableStateFlow(CameraState())
    val state = _state.asStateFlow()
    private val _flashState = MutableStateFlow(ImageCapture.FLASH_MODE_OFF)
    val flashState = _flashState.asStateFlow()
    private val _cameraSelector = MutableStateFlow(CameraSelector.DEFAULT_BACK_CAMERA)
    val cameraSelector = _cameraSelector.asStateFlow()
    fun setFlashMode(flashMode: Int){
        _flashState.value = flashMode
    }

    fun setCameraSelector(cameraSelector: CameraSelector){
        _cameraSelector.value = cameraSelector
    }

    fun storePhotoInGallery(bitmap: Bitmap){
        viewModelScope.launch {
            savePhotoToGalleryUseCase.call(bitmap)
            updateCapturedPhotoState(null)
        }
    }

    fun updateCapturedPhotoState(updatedPhoto: Bitmap?) {
        _state.value.capturedImage?.recycle()
        _state.value = _state.value.copy(capturedImage = updatedPhoto)
    }

    override fun onCleared() {
        _state.value.capturedImage?.recycle()
        super.onCleared()
    }
}
