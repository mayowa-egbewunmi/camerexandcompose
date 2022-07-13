package com.sample.android.shared.utils

import android.content.Context
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.Recorder
import androidx.camera.video.VideoCapture
import androidx.camera.view.PreviewView
import androidx.compose.runtime.compositionLocalOf
import androidx.concurrent.futures.await
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.google.common.util.concurrent.ListenableFuture
import com.sample.android.shared.PreviewState
import java.io.File

class VideoCaptureManager private constructor(private val builder: Builder) :
    LifecycleEventObserver {

    private lateinit var cameraProviderFuture: ListenableFuture<ProcessCameraProvider>
    private lateinit var videoCapture: VideoCapture<Recorder>

    var recordingListener: RecordingListener = object : RecordingListener {
        override fun onInitialised(cameraLensInfo: HashMap<Int, CameraInfo>) {}
        override fun onSuccess(imageResult: ImageCapture.OutputFileResults) {}
        override fun onError(exception: Exception) {}
    }

    init {
        getLifecycle().addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event) {
            Lifecycle.Event.ON_CREATE -> {
                cameraProviderFuture = ProcessCameraProvider.getInstance(getContext())
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    queryCameraInfo(source, cameraProvider)
                }, ContextCompat.getMainExecutor(getContext()))
            }
            else -> Unit
        }
    }

    private fun getCameraPreview() = PreviewView(getContext()).apply {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.MATCH_PARENT
        )
        keepScreenOn = true
    }

    private fun getLifecycle() = builder.lifecycleOwner?.lifecycle!!

    private fun getContext() = builder.context

    private fun getLifeCycleOwner() = builder.lifecycleOwner!!

    /**
     * Queries the capabilities of the FRONT and BACK camera lens
     * The result is stored in an array map.
     *
     * With this, we can determine if a camera lens is available or not,
     * and what capabilities the lens can support e.g flash support
     */
    private fun queryCameraInfo(
        lifecycleOwner: LifecycleOwner,
        cameraProvider: ProcessCameraProvider
    ) {
        val cameraLensInfo = HashMap<Int, CameraInfo>()
        arrayOf(CameraSelector.LENS_FACING_BACK, CameraSelector.LENS_FACING_FRONT).forEach { lens ->
            val cameraSelector = CameraSelector.Builder().requireLensFacing(lens).build()
            if (cameraProvider.hasCamera(cameraSelector)) {
                val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector)
                if (lens == CameraSelector.LENS_FACING_BACK) {
                    cameraLensInfo[CameraSelector.LENS_FACING_BACK] = camera.cameraInfo
                } else if (lens == CameraSelector.LENS_FACING_FRONT) {
                    cameraLensInfo[CameraSelector.LENS_FACING_FRONT] = camera.cameraInfo
                }
            }
        }
        recordingListener.onInitialised(cameraLensInfo)
    }

    /**
     * Takes a [previewState] argument to determine the camera options
     *
     * Create a Preview.
     * Create Video Capture use case
     * Bind the selected camera and any use cases to the lifecycle.
     * Connect the Preview to the PreviewView.
     */
    private fun showPreview(previewState: PreviewState, cameraPreview: PreviewView): View {
        getLifeCycleOwner().lifecycleScope.launchWhenResumed {
            val cameraProvider = cameraProviderFuture.await()
            cameraProvider.unbindAll()

            //Select a camera lens
            val cameraSelector: CameraSelector = CameraSelector.Builder()
                .requireLensFacing(previewState.cameraLens)
                .build()

            //Create Preview use case
            val preview: Preview = Preview.Builder()
                .build()
                .apply { setSurfaceProvider(cameraPreview.surfaceProvider) }

            //Create Video Capture use case
            val recorder = Recorder.Builder().build()
            videoCapture = VideoCapture.withOutput(recorder)

            cameraProvider.bindToLifecycle(
                getLifeCycleOwner(),
                cameraSelector,
                preview,
                videoCapture
            )
        }
        return cameraPreview
    }

    fun showPreview(previewState: PreviewState): View {
        return showPreview(previewState, getCameraPreview())
    }

    fun updatePreview(previewState: PreviewState, previewView: View) {
        showPreview(previewState, previewView as PreviewView)
    }

    fun startRecording(filePath: String) {
        val file = File(filePath)
        val outputFileOptions = ImageCapture.OutputFileOptions.Builder(file).build()
//
//        videoCapture.re(outputFileOptions, ContextCompat.getMainExecutor(getContext()),
//            object : ImageCapture.OnImageSavedCallback {
//                override fun onError(error: ImageCaptureException) {
//                    Timber.e(error)
//                    recordingListener.onError(error)
//                }
//
//                override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
//                    recordingListener.onSuccess(outputFileResults)
//                }
//            })
    }

    class Builder(val context: Context) {

        var lifecycleOwner: LifecycleOwner? = null
            private set

        fun registerLifecycleOwner(source: LifecycleOwner): Builder {
            this.lifecycleOwner = source
            return this
        }

        fun create(): VideoCaptureManager {
            requireNotNull(lifecycleOwner) { "Lifecycle owner is not set" }
            return VideoCaptureManager(this)
        }
    }

    interface RecordingListener {
        fun onInitialised(cameraLensInfo: HashMap<Int, CameraInfo>)
        fun onSuccess(imageResult: ImageCapture.OutputFileResults)
        fun onError(exception: Exception)
    }
}

val LocalVideoCaptureManager = compositionLocalOf<VideoCaptureManager> { error("No capture manager found!") }
