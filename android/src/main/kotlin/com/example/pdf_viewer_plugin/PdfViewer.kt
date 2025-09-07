package com.example.pdf_viewer_plugin

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.FrameLayout
import com.github.barteksc.pdfviewer.PDFView
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.BinaryMessenger
import kotlinx.coroutines.*
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKeys
import java.io.File


class PdfViewer(
    context: Context,
    private val url: String?,
    messenger: BinaryMessenger,
    id: Int // 👈 viewId from factory
) : FrameLayout(context), PlatformView {

    private val pdfView: PDFView = PDFView(context, null)
    private val channel = MethodChannel(messenger, "pdf_viewer_plugin/errors_$id")

    private val mainHandler = Handler(Looper.getMainLooper())
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    @Volatile private var finished = false
    
    init { }

    override fun getView(): android.view.View {
        return this
    }

    override fun dispose() {
        scope.cancel() // cancel ongoing downloads
        pdfView.recycle() // free memory
    }
    
    fun start() {
        showLoading("Loading...")
        if (url != null) {
            downloadAndLoadPdf(url)
        } else {
            showError("No URL provided")
        }
    }
            
    
    private fun showError(message: String) {
        if (finished) return
        finished = true
        
        Log.e("PdfViewer", message)
        runOnMain {
          channel.invokeMethod("onError", message)
        }
    }


    private fun showLoading(msg: String) {
        runOnMain { channel.invokeMethod("onLoading", msg) }
    }

    private fun showSuccess() {
        if (finished) return
        finished = true
        
        runOnMain { channel.invokeMethod("onSuccess", null) }
    }

    private fun runOnMain(block: () -> Unit) {
        mainHandler.post { block() }
    }

    private fun downloadAndLoadPdf(fileUrl: String) {
        scope.launch {
            try {
                val cacheFile = File(context.filesDir, "cached_${fileUrl.hashCode()}.pdf")
            
                val bytes: ByteArray = if (cacheFile.exists()) {
                  // 🔐 Load from secure storage
                  Log.d("PdfViewer", "Loading PDF from secure cache")
                  readEncryptedFile(cacheFile)
                } else {
                  // 🌐 Download fresh
                  Log.d("PdfViewer", "Downloading PDF from network")
               
                  val url = URL(fileUrl)
                  val conn = url.openConnection() as HttpURLConnection
                  conn.doInput = true
                  conn.connect()

                  val newBytes = conn.inputStream.use { readAllBytes(it) }

                  // Save securely
                  writeEncryptedFile(cacheFile, newBytes)
                  newBytes
                
                }
                
                withContext(Dispatchers.Main) {
                    try {
                        this@PdfViewer.removeAllViews()
                        this@PdfViewer.addView(pdfView)

                        pdfView.fromBytes(bytes)
                            .enableSwipe(true)
                            .swipeHorizontal(false)
                            .enableDoubletap(true)
                            .onLoad { nbPages ->
                                Log.d("PdfViewer", "PDF loaded successfully, total pages = $nbPages")
                                showSuccess()
                            }
                            .onError { t ->
                                showError("Render error: ${t.message}")
                            }
                            .onPageError { page, t ->
                                showError("Page $page error: ${t.message}")
                            }
                            .load()
                            
                        // 👇 Timeout safeguard (e.g. 5 seconds)
                        mainHandler.postDelayed({
                            if (!finished) {
                              showError("Timeout loading PDF")
                            }
                        }, 20000)

                    } catch (e: Exception) {
                        showError("Unexpected error: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e("PdfViewer", "Error downloading PDF", e)
                showError("Download error: ${e.message}")
            }
        }
    }
    
    
    private fun writeEncryptedFile(file: File, data: ByteArray) {
      val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
      val encryptedFile = EncryptedFile.Builder(
          file,
          context,
          masterKeyAlias,
          EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
      ).build()

      encryptedFile.openFileOutput().use { it.write(data) }
    }
    
    private fun readEncryptedFile(file: File): ByteArray {
      val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
      val encryptedFile = EncryptedFile.Builder(
          file,
          context,
          masterKeyAlias,
          EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
      ).build()

      return encryptedFile.openFileInput().use { it.readBytes() }
    }
    
    
    private fun readAllBytes(input: InputStream): ByteArray {
        val buffer = ByteArrayOutputStream()
        val data = ByteArray(1024)
        var count: Int
        while (input.read(data).also { count = it } != -1) {
            buffer.write(data, 0, count)
        }
        return buffer.toByteArray()
    }
}
