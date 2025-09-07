package com.example.pdf_viewer_plugin

import io.flutter.embedding.engine.plugins.FlutterPlugin

class PdfViewerPlugin : FlutterPlugin {
    override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        binding
            .platformViewRegistry
            .registerViewFactory(
                "pdf_viewer_plugin/view",
                PdfViewerFactory(binding.binaryMessenger)
            )
    }

    override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
        // Clean up if necessary
    }
}
