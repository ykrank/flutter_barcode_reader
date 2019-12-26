package com.apptreesoftware.barcodescan

import android.app.Activity
import android.content.Intent
import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry
import io.flutter.plugin.common.PluginRegistry.Registrar

class BarcodeScanPlugin(): MethodCallHandler, PluginRegistry.ActivityResultListener, FlutterPlugin, ActivityAware {
  
  private var result : Result? = null
  private var channel: MethodChannel? = null
  private var activity: Activity? = null

  constructor(activity: Activity?) : this() {
    this.activity = activity
  }

  companion object {
    @JvmStatic
    fun registerWith(registrar: Registrar) {
      val channel = MethodChannel(registrar.messenger(), "com.apptreesoftware.barcode_scan")
      if (registrar.activity() != null) {
        val plugin = BarcodeScanPlugin(registrar.activity())
        channel.setMethodCallHandler(plugin)
        registrar.addActivityResultListener(plugin)
      }  
    }
  }

  override fun onMethodCall(call: MethodCall, result: Result) {
    if (call.method == "scan") {
      this.result = result
      showBarcodeView()
    } else {
      result.notImplemented()
    }
  }

  private fun showBarcodeView() {
    activity?.let {
      val intent = Intent(it, BarcodeScannerActivity::class.java)
      it.startActivityForResult(intent, 100)
    }
  }

  override fun onActivityResult(code: Int, resultCode: Int, data: Intent?): Boolean {
    if (code == 100) {
      if (resultCode == Activity.RESULT_OK) {
        val barcode = data?.getStringExtra("SCAN_RESULT")
        barcode?.let { this.result?.success(barcode) }
      } else {
        val errorCode = data?.getStringExtra("ERROR_CODE")
        this.result?.error(errorCode, null, null)
      }
      return true
    }
    return false
  }

  override fun onAttachedToEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel = MethodChannel(binding.binaryMessenger, "com.apptreesoftware.barcode_scan")
  }

  override fun onDetachedFromEngine(binding: FlutterPlugin.FlutterPluginBinding) {
    channel?.setMethodCallHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    val plugin = BarcodeScanPlugin(binding.activity)
    channel?.setMethodCallHandler(plugin)
    binding.addActivityResultListener(plugin)
  }

  override fun onDetachedFromActivity() {

  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    onAttachedToActivity(binding)
  }

  override fun onDetachedFromActivityForConfigChanges() {
    onDetachedFromActivity()
  }
}
