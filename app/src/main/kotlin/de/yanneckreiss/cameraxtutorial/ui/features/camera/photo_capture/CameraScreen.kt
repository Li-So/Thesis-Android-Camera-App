package de.yanneckreiss.cameraxtutorial.ui.features.camera.photo_capture

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment.Companion.TopCenter
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.yanneckreiss.cameraxtutorial.R
import de.yanneckreiss.cameraxtutorial.core.utils.rotateBitmap
import kotlinx.coroutines.delay
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.Executor


@Composable
fun CameraScreen(
    viewModel: CameraViewModel = koinViewModel()
) {
    val cameraState: CameraState by viewModel.state.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val cameraController: LifecycleCameraController =
        remember(context) { LifecycleCameraController(context) }
    var isVisible by remember { mutableStateOf(false) }
    val flashMode: Int by viewModel.flashState.collectAsStateWithLifecycle()
    val cameraSelector: CameraSelector by viewModel.cameraSelector.collectAsStateWithLifecycle()

    Box{
        if (cameraState.capturedImage != null) {
            TakenPhotoPreview(
                onPhotoSaved = viewModel::storePhotoInGallery,
                onPhotoDismissed = viewModel::updateCapturedPhotoState,
                lastCapturedPhoto = cameraState.capturedImage!!,
                showTopToast = { isVisible = true }
            )
        } else {
            CameraContent(
                onPhotoCaptured = viewModel::updateCapturedPhotoState,
                cameraController = cameraController,
                context = context,
                flashMode = flashMode,
                setFlashMode = viewModel::setFlashMode,
                cameraSelector = cameraSelector,
                setCameraSelector = viewModel::setCameraSelector
            )
        }
        TopScreenToast(
            isVisible = isVisible,
            toggleVisible = { isVisible = false },
            text = stringResource(R.string.photo_was_saved_to_camera_roll)
        )

    }
}

@Composable
private fun TakenPhotoPreview(
    onPhotoSaved: (Bitmap) -> Unit,
    onPhotoDismissed: (Bitmap?) -> Unit,
    lastCapturedPhoto: Bitmap,
    showTopToast: () -> Unit
){
    Box(){
        LastPhotoPreview(
            modifier = Modifier
                .fillMaxSize(),
            lastCapturedPhoto = lastCapturedPhoto
        )

        IconButton(onClick = { onPhotoDismissed(null) }) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(R.string.dismiss_photo),
                modifier = Modifier
                    .size(50.dp)
                    .padding(top = 10.dp, start = 10.dp)
                    .align(TopStart)
            )
        }

        IconButton(
            onClick = {
                onPhotoSaved(lastCapturedPhoto)
                showTopToast()
                      },
            modifier = Modifier
                .size(50.dp)
                .padding(top = 10.dp, end = 10.dp)
                .align(TopEnd)
        ) {
            Icon(imageVector = Icons.Default.Download, contentDescription = stringResource(R.string.save_photo))
        }


    }
}

@Composable
private fun CameraContent(
    onPhotoCaptured: (Bitmap) -> Unit,
    cameraController: LifecycleCameraController,
    context: Context,
    flashMode: Int,
    setFlashMode: (Int) -> Unit,
    cameraSelector: CameraSelector,
    setCameraSelector: (CameraSelector) -> Unit
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(flashMode) {
        cameraController.imageCaptureFlashMode = flashMode
    }

    LaunchedEffect(cameraSelector) {
        cameraController.cameraSelector = cameraSelector
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        floatingActionButton = {
            ExtendedFloatingActionButton(
                content = {
                    Icon(
                        imageVector = Icons.Default.RadioButtonUnchecked,
                        contentDescription = stringResource(R.string.camera_capture_icon),
                        tint = androidx.compose.ui.graphics.Color.White,
                        modifier = Modifier
                            .size(100.dp)
                        )
                    },
                    onClick = { capturePhoto(context, cameraController, onPhotoCaptured) },
                    containerColor = androidx.compose.ui.graphics.Color.Transparent,
                    shape = CircleShape,
                    elevation = FloatingActionButtonDefaults.elevation(
                        defaultElevation = 0.dp
                    )
                )
            },
            floatingActionButtonPosition = FabPosition.Center
        ) { paddingValues: PaddingValues ->

            AndroidView(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                factory = { context ->
                    PreviewView(context).apply {
                        layoutParams = LinearLayout.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        setBackgroundColor(Color.BLACK)
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_START
                    }.also { previewView ->
                        previewView.controller = cameraController
                        cameraController.bindToLifecycle(lifecycleOwner)
                    }
                }
            )

            Box(modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectTapGestures(
                        onDoubleTap = {
                            when (cameraController.cameraSelector) {
                                CameraSelector.DEFAULT_FRONT_CAMERA -> setCameraSelector(
                                    CameraSelector.DEFAULT_BACK_CAMERA
                                )

                                CameraSelector.DEFAULT_BACK_CAMERA -> setCameraSelector(
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                )
                            }
                        }
                    )
                }){
                if(cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA){
                    if (flashMode == ImageCapture.FLASH_MODE_OFF) {
                        IconButton(onClick = {
                            setFlashMode(ImageCapture.FLASH_MODE_ON)
                        }) {
                            Icon(
                                imageVector = Icons.Default.FlashOn,
                                contentDescription = stringResource(R.string.flash_on)
                            )
                        }
                    } else {
                        IconButton(onClick = {
                            setFlashMode(ImageCapture.FLASH_MODE_OFF)
                        }) {
                            Icon(
                                imageVector = Icons.Default.FlashOff,
                                contentDescription = stringResource(R.string.flash_off)
                            )
                        }
                    }
                }


            }
        }

}


private fun capturePhoto(
    context: Context,
    cameraController: LifecycleCameraController,
    onPhotoCaptured: (Bitmap) -> Unit
) {
    val mainExecutor: Executor = ContextCompat.getMainExecutor(context)

    cameraController.takePicture(mainExecutor, object : ImageCapture.OnImageCapturedCallback() {
        override fun onCaptureSuccess(image: ImageProxy) {
            val correctedBitmap: Bitmap = image
                .toBitmap()
                .rotateBitmap(image.imageInfo.rotationDegrees)

            onPhotoCaptured(correctedBitmap)
            image.close()
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraContent", "Error capturing image", exception)
        }
    })
}

@Composable
private fun LastPhotoPreview(
    modifier: Modifier = Modifier,
    lastCapturedPhoto: Bitmap
) {

    val capturedPhoto: ImageBitmap =
        remember(lastCapturedPhoto.hashCode()) { lastCapturedPhoto.asImageBitmap() }

    Box(
        modifier = modifier
    ) {
        Image(
            bitmap = capturedPhoto,
            contentDescription = stringResource(R.string.last_captured_photo),
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Composable
fun TopScreenToast(
    isVisible: Boolean,
    toggleVisible: () -> Unit,
    text: String
){
    LaunchedEffect(key1 = isVisible) {
        delay(2000)
        toggleVisible()
    }

    AnimatedVisibility(
        visible = isVisible,
        enter = fadeIn(animationSpec = tween(durationMillis = 1000)),
        exit = fadeOut(animationSpec = tween(durationMillis = 1000))
    ){
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Text(
                text = text,
                textAlign = TextAlign.Center,
                lineHeight = 4.em,
                color = androidx.compose.ui.graphics.Color.White,
                modifier = Modifier
                    .align(TopCenter)
                    .fillMaxWidth()
                    .height(70.dp)
                    .padding(start = 70.dp, top = 10.dp, end = 70.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(androidx.compose.ui.graphics.Color.Green)
            )
        }
    }
}

@Preview
@Composable
private fun Preview_CameraScreen() {
    CameraScreen()
}
