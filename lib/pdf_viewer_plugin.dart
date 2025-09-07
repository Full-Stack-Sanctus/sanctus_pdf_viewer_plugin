import 'package:flutter/foundation.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

class PdfViewer extends StatefulWidget {
  final String url;
  const PdfViewer({super.key, required this.url});

  @override
  State<PdfViewer> createState() => _PdfViewerState();
}

class _PdfViewerState extends State<PdfViewer> {
  MethodChannel? _channel;

  String status = "loading";
  String? errorMessage;

  void _setupChannel(int id) {
    _channel = MethodChannel("pdf_viewer_plugin/errors_$id");
    _channel!.setMethodCallHandler((call) async {
      if (call.method == "onError") {
        setState(() {
          status = "error";
          errorMessage = call.arguments as String?;
        });
      } else if (call.method == "onLoading") {
        setState(() {
          status = "loading";
          errorMessage = null;
        });
      } else if (call.method == "onSuccess") {
        setState(() {
          status = "success";
          errorMessage = null;
        });
      }
    });
  }

  @override
  Widget build(BuildContext context) {
    return Stack(
      children: [
        AndroidView(
          viewType: 'pdf_viewer_plugin/view',
          creationParams: {"url": widget.url},
          creationParamsCodec: const StandardMessageCodec(),
          onPlatformViewCreated: _setupChannel,
        ),
        if (status == "loading")
          const Center(child: CircularProgressIndicator()),
        if (status == "error")
          Center(child: Text(errorMessage ?? "Failed to load PDF")),
      ],
    );
  }
}
