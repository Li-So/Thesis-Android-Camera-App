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
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.TopEnd
import androidx.compose.ui.Alignment.Companion.TopStart
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import de.yanneckreiss.cameraxtutorial.core.utils.rotateBitmap
import org.koin.androidx.compose.koinViewModel
import java.util.concurrent.Executor


@Composable
fun CameraScreen(
    viewModel: CameraViewModel = koinViewModel()
) {
    val cameraState: CameraState by viewModel.state.collectAsStateWithLifecycle()
    val context: Context = LocalContext.current
    val cameraController: LifecycleCameraController =
        remember { LifecycleCameraController(context) }


    if(cameraState.capturedImage != null){
        TakenPhotoPreview(
            onPhotoSaved = viewModel::storePhotoInGallery,
            onPhotoDismissed = viewModel::updateCapturedPhotoState ,
            lastCapturedPhoto = cameraState.capturedImage!!
        )
    } else {
        CameraContent(
            onPhotoCaptured = viewModel::updateCapturedPhotoState,
            cameraController = cameraController,
            context = context
        )
    }
}

@Composable
private fun TakenPhotoPreview(
    onPhotoSaved: (Bitmap) -> Unit,
    onPhotoDismissed: (Bitmap?) -> Unit,
    lastCapturedPhoto: Bitmap
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
                contentDescription = "",
                modifier = Modifier
                    .size(50.dp)
                    .padding(top = 10.dp, start = 10.dp)
                    .align(TopStart)
            )
        }

        IconButton(
            onClick = { onPhotoSaved(lastCapturedPhoto) },
            modifier = Modifier
                .size(50.dp)
                .padding(top = 10.dp, end = 10.dp)
                .align(TopEnd)
        ) {
            Icon(imageVector = Icons.Default.Download, contentDescription = "")
        }


    }
}

@Composable
private fun CameraContent(
    onPhotoCaptured: (Bitmap) -> Unit,
    cameraController: LifecycleCameraController,
    context: Context
) {
    val lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            floatingActionButton = {
                ExtendedFloatingActionButton(
                    content = {
                        Icon(
                            imageVector = Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Camera capture icon",
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
                            cameraController.cameraSelector =
                                if (cameraController.cameraSelector == CameraSelector.DEFAULT_BACK_CAMERA) {
                                    CameraSelector.DEFAULT_FRONT_CAMERA
                                } else CameraSelector.DEFAULT_BACK_CAMERA
                        }
                    )
                })
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
            contentDescription = "Last captured photo",
            contentScale = androidx.compose.ui.layout.ContentScale.Crop,
            modifier = modifier
        )
    }
}

@Preview
@Composable
private fun Preview_CameraScreen() {
    CameraScreen()
}
