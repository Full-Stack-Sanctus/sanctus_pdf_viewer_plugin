package com.example.pdf_viewer_plugin

import android.os.Handler
import android.os.Looper
import android.content.Context
import android.util.Log
import io.flutter.plugin.common.BinaryMessenger
import io.flutter.plugin.common.StandardMessageCodec
import io.flutter.plugin.platform.PlatformView
import io.flutter.plugin.platform.PlatformViewFactory
import org.json.JSONObject

class PdfViewerFactory(private val messenger: BinaryMessenger) : PlatformViewFactory(StandardMessageCodec.INSTANCE) {

    override fun create(context: Context, id: Int, args: Any?): PlatformView {
      var url: String? = null
      try {
        if (args is Map<*, *>) {
          url = args["url"] as? String
        }
      } catch (e: Exception) {
          Log.e("PdfViewerFactory", "Error parsing args", e)
      }
      
      val viewer = PdfViewer(context, url, messenger, id)
      
      // ✅ start after Flutter is ready
      
      Handler(Looper.getMainLooper()).post {
        viewer.start()
      }
      
    return viewer
    
    }

}
