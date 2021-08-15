package com.vault_erp.pdfmergeandconvert

import android.database.Cursor
import android.graphics.*
import android.graphics.pdf.PdfDocument
import android.media.ExifInterface
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_first.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileNotFoundException
import java.io.FileOutputStream
import java.io.IOException
import java.util.*
import kotlin.collections.ArrayList


class FirstFragment : Fragment() {

    lateinit var rootView: View
    lateinit var adapter: RVAdapter
    val imageList = arrayListOf<Bitmap>()
    var documentData: ArrayList<Uri> = arrayListOf()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        rootView = inflater.inflate(R.layout.fragment_first, container, false)

        SharedPreferenceHelper(requireContext(), "db").deleteSharedPref()

        adapter = RVAdapter(requireContext(), arrayListOf())

        rootView.rv.layoutManager = LinearLayoutManager(context)
        rootView.rv.adapter = adapter

        rootView.findViewById<Button>(R.id.buttonClick).setOnClickListener {
            (activity as MainActivity).openDocScanner()
        }

        rootView.findViewById<Button>(R.id.mergeButton).setOnClickListener {
            overlay(imageList, {

            })
        }

        return rootView
    }

    override fun onResume() {
        super.onResume()

        val original = SharedPreferenceHelper(requireContext(), "db").getString("original")
        val cropped = SharedPreferenceHelper(requireContext(), "db").getString("cropped")
        val transform = SharedPreferenceHelper(requireContext(), "db").getString("transform")

        if (original.isNotEmpty()) {
            displayFile(original, cropped, transform)
        }
    }

    override fun onStop() {
        super.onStop()

//        rootView.rv.visibility = GONE
        SharedPreferenceHelper(requireContext(), "db").deleteSharedPref()
    }

    fun displayFile(original: String, cropped: String, transform: String) {
        rootView.rv.visibility = View.VISIBLE
        if (transform.isNotEmpty()) {
//            convertButton(BitmapFactory.decodeFile(transform),{})
//            convertButton(BitmapFactory.decodeFile(compressImage(transform)), {})
            compressImage(transform)
        } else {
            if (cropped.isNotEmpty()) {
//                convertButton(BitmapFactory.decodeFile(cropped),{})
//                convertButton(BitmapFactory.decodeFile(compressImage(cropped)), {})
                compressImage(cropped)
            }
        }
    }

    private fun convertButton(bitmap: Bitmap, callback: (() -> Unit)? = null) {
        CoroutineScope(Dispatchers.Default).launch {
            val directory = context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path

            val pdfDocument = PdfDocument()
            val myPageInfo: PdfDocument.PageInfo =
                PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
            val page: PdfDocument.Page = pdfDocument.startPage(myPageInfo);

            page.canvas.drawBitmap(bitmap, 0f, 0f, null)
            pdfDocument.finishPage(page)

            val cal = Calendar.getInstance()
            val year = cal.get(Calendar.YEAR)
            val month = cal.get(Calendar.MONTH) + 1
            val day = cal.get(Calendar.DATE)

            val pdfFile = "$directory/myPdf_${year}${month}${day}${cal.timeInMillis}.pdf"
            val myPDFFile = File(pdfFile)

            try {
                pdfDocument.writeTo(FileOutputStream(myPDFFile))
            } catch (e: IOException) {
                e.printStackTrace()
            } catch (ex: Exception) {
                ex.printStackTrace()
            }
            pdfDocument.close()
            withContext(Dispatchers.Main) {
                val fileName = pdfFile.split("/").last()
                val file = File(directory, fileName)
                val uri = FileProvider.getUriForFile(
                    requireContext(),
                    requireContext().packageName + ".provider",
                    file
                )
                documentData.add(uri)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        SharedPreferenceHelper(requireContext(), "db").deleteSharedPref()
    }

    fun compressImage(imageUri: String): String? {
        val filePath = getRealPathFromURI(imageUri)
        var scaledBitmap: Bitmap? = null
        val options = BitmapFactory.Options()

//      by setting this field as true, the actual bitmap pixels are not loaded in the memory. Just the bounds are loaded. If
//      you try the use the bitmap here, you will get null.
        options.inJustDecodeBounds = true
        var bmp = BitmapFactory.decodeFile(filePath, options)
        var actualHeight = options.outHeight
        var actualWidth = options.outWidth

//      max Height and width values of the compressed image is taken as 816x612
        val maxHeight = 816.0f
        val maxWidth = 612.0f
        var imgRatio = (actualWidth / actualHeight).toFloat()
        val maxRatio = maxWidth / maxHeight

//      width and height values are set maintaining the aspect ratio of the image
        if (actualHeight > maxHeight || actualWidth > maxWidth) {
            if (imgRatio < maxRatio) {
                imgRatio = maxHeight / actualHeight
                actualWidth = (imgRatio * actualWidth).toInt()
                actualHeight = maxHeight.toInt()
            } else if (imgRatio > maxRatio) {
                imgRatio = maxWidth / actualWidth
                actualHeight = (imgRatio * actualHeight).toInt()
                actualWidth = maxWidth.toInt()
            } else {
                actualHeight = maxHeight.toInt()
                actualWidth = maxWidth.toInt()
            }
        }

//      setting inSampleSize value allows to load a scaled down version of the original image
        options.inSampleSize = calculateInSampleSize(options, actualWidth, actualHeight)

//      inJustDecodeBounds set to false to load the actual bitmap
        options.inJustDecodeBounds = false

//      this options allow android to claim the bitmap memory if it runs low on memory
        options.inPurgeable = true
        options.inInputShareable = true
        options.inTempStorage = ByteArray(16 * 1024)
        try {
//          load the bitmap from its path
            bmp = BitmapFactory.decodeFile(filePath, options)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        try {
            scaledBitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888)
        } catch (exception: OutOfMemoryError) {
            exception.printStackTrace()
        }
        val ratioX = actualWidth / options.outWidth.toFloat()
        val ratioY = actualHeight / options.outHeight.toFloat()
        val middleX = actualWidth / 2.0f
        val middleY = actualHeight / 2.0f
        val scaleMatrix = Matrix()
        scaleMatrix.setScale(ratioX, ratioY, middleX, middleY)
        val canvas = scaledBitmap?.let { Canvas(it) }
        canvas?.setMatrix(scaleMatrix)
        canvas?.drawBitmap(
            bmp,
            middleX - bmp.width / 2,
            middleY - bmp.height / 2,
            Paint(Paint.FILTER_BITMAP_FLAG)
        )

//      check the rotation of the image and display it properly
        val exif: ExifInterface
        try {
            exif = ExifInterface(filePath!!)
            val orientation = exif.getAttributeInt(
                ExifInterface.TAG_ORIENTATION, 0
            )
            Log.d("EXIF", "Exif: $orientation")
            val matrix = Matrix()
            if (orientation == 6) {
                matrix.postRotate(90f)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 3) {
                matrix.postRotate(180f)
                Log.d("EXIF", "Exif: $orientation")
            } else if (orientation == 8) {
                matrix.postRotate(270f)
                Log.d("EXIF", "Exif: $orientation")
            }
            scaledBitmap = Bitmap.createBitmap(
                scaledBitmap!!, 0, 0,
                scaledBitmap.width, scaledBitmap.height, matrix,
                true
            )
        } catch (e: IOException) {
            e.printStackTrace()
        }
        var out: FileOutputStream? = null
        val filename = getFilename()
        try {
            out = FileOutputStream(filename)

//          write the compressed bitmap at the destination specified by filename.
            scaledBitmap?.compress(Bitmap.CompressFormat.JPEG, 40, out)
            if (scaledBitmap != null) {
                imageList.add(scaledBitmap)
            }
            adapter.updateList(imageList)
        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        }
        return filename
    }

    fun getFilename(): String {
        val directory = context?.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)?.path

        val file = File(directory)
        if (!file.exists()) {
            file.mkdirs()
        }
        return file.absolutePath + "/" + System.currentTimeMillis() + ".jpg"
    }

    private fun getRealPathFromURI(contentURI: String): String? {
        val contentUri = Uri.parse(contentURI)
        val cursor: Cursor? = context?.contentResolver?.query(contentUri, null, null, null, null)
        return if (cursor == null) {
            contentUri.path
        } else {
            cursor.moveToFirst()
            val index: Int = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA)
            cursor.getString(index)
        }
    }

    fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val heightRatio = Math.round(height.toFloat() / reqHeight.toFloat())
            val widthRatio = Math.round(width.toFloat() / reqWidth.toFloat())
            inSampleSize = if (heightRatio < widthRatio) heightRatio else widthRatio
        }
        val totalPixels = (width * height).toFloat()
        val totalReqPixelsCap = (reqWidth * reqHeight * 2).toFloat()
        while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
            inSampleSize++
        }
        return inSampleSize
    }

    fun overlay(imageList: ArrayList<Bitmap>, callback: (() -> Unit)?) {
        CoroutineScope(Dispatchers.Default).launch {
            if (imageList.isNotEmpty()) {
                val width = imageList.maxOf { it.width }
                val height = imageList.sumBy { it.height }

                val bmOverlay = Bitmap.createBitmap((width), height, imageList.first().config)
                val canvas = Canvas(bmOverlay)
                var topMargin = 0f
                imageList.mapIndexed { index, bitmap ->
                    if (index == 0) {
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                    } else {
                        topMargin += imageList[index - 1].height.toFloat() + 20f
                        canvas.drawBitmap(bitmap, 0f, topMargin.toFloat(), null)
                    }
                    bitmap.recycle()
                }
                convertButton(bitmap = bmOverlay)
            }
        }
    }
}