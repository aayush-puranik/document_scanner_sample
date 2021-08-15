package com.vault_erp.pdfmergeandconvert

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.zynksoftware.documentscanner.ScanActivity
import com.zynksoftware.documentscanner.model.DocumentScannerErrorModel
import com.zynksoftware.documentscanner.model.ScannerResults
import java.io.File

interface ImageAdapterListener {
    fun onSaveButtonClicked(image: File)
}

class AppScanActivity: ScanActivity(), ImageAdapterListener {

    private var alertDialogBuilder: android.app.AlertDialog.Builder? = null
    private var alertDialog: android.app.AlertDialog? = null

    companion object {
        private val TAG = AppScanActivity::class.simpleName

        fun start(context: Context) {
            val intent = Intent(context, AppScanActivity::class.java)
            context.startActivity(intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_app_scan)

        addFragmentContentLayout()
    }

    override fun onClose() {
        Log.d(TAG, "onClose")
        finish()
    }

    override fun onError(error: DocumentScannerErrorModel) {
        showAlertDialog(getString(R.string.error_label), error.errorMessage?.error, getString(R.string.ok_label))
    }

    override fun onSuccess(scannerResults: ScannerResults) {
        initViewPager(scannerResults)
    }

    override fun onSaveButtonClicked(image: File) {
    }

    private fun initViewPager(scannerResults: ScannerResults) {
        SharedPreferenceHelper(this, "db").save("original", scannerResults.originalImageFile?.path ?: "")
        SharedPreferenceHelper(this, "db").save("cropped",scannerResults.croppedImageFile?.path ?: "")
        SharedPreferenceHelper(this, "db").save("transform", scannerResults.transformedImageFile?.path ?: "")
        finish()
    }

    private fun showAlertDialog(title: String?, message: String?, buttonMessage: String) {
        alertDialogBuilder = android.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(buttonMessage) { dialog, which ->

            }
        alertDialog?.dismiss()
        alertDialog = alertDialogBuilder?.create()
        alertDialog?.setCanceledOnTouchOutside(false)
        alertDialog?.show()
    }
}