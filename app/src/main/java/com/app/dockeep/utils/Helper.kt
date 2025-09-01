package com.app.dockeep.utils

import android.content.ClipData
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.core.content.pm.PackageInfoCompat
import com.app.dockeep.model.AppVersion
import java.lang.Exception
import java.text.DecimalFormat
import kotlin.math.log10
import kotlin.math.pow

object Helper {
    fun Context.openDocument(uri: Uri, mimeType: String?) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        try {
            startActivity(intent)
        } catch (_: Exception) {
            Toast.makeText(this, "Can't open document", Toast.LENGTH_SHORT).show()
        }
    }

    fun Context.shareDocument(uris: List<Uri>, mimeTypes: List<String>) {
        val intent = if (uris.size == 1) {
            Intent(Intent.ACTION_SEND).apply {
                putExtra(Intent.EXTRA_STREAM, uris[0])
                type = mimeTypes[0]
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                clipData = ClipData.newRawUri("", uris[0])
            }
        } else {
            val array = arrayListOf<Uri>()
            array.addAll(uris)
            val clipD  = ClipData(
                "",
                mimeTypes.toTypedArray(),
                ClipData.Item(uris[0])
            )
            for (i in 1 until uris.size) {
                clipD.addItem(ClipData.Item(uris[i]))
            }

            Intent(Intent.ACTION_SEND_MULTIPLE).apply {
                setType("*/*")
                putParcelableArrayListExtra(Intent.EXTRA_STREAM, array)
                putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
                clipData = clipData
            }
        }

        try {
            startActivity(Intent.createChooser(intent, null))
        } catch (_: Exception) {
            Toast.makeText(this, "Can't share document", Toast.LENGTH_SHORT).show()
        }
    }

    fun Intent.extractUris(): List<Uri> {
        val uriList = mutableListOf<Uri>()
        clipData?.let { clip ->
            for (i in 0 until clip.itemCount) {
                clip.getItemAt(i).uri?.let { uriList.add(it) }
            }
        }
        data?.let { uriList.add(it) }
        return uriList
    }

    val openDocumentIntent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
        addCategory(Intent.CATEGORY_OPENABLE)
        addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true)
        setType("*/*")
    }

    fun Context.getAppVersion(): AppVersion? {
        return try {
            val packageManager = packageManager
            val packageName = packageName
            val packageInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(0))
            } else {
                packageManager.getPackageInfo(packageName, 0)
            }
            AppVersion(
                versionName = packageInfo.versionName,
                versionNumber = PackageInfoCompat.getLongVersionCode(packageInfo),
            )
        } catch (_: Exception) {

        } as AppVersion?
    }

    fun Context.getAppName(): String = applicationInfo.loadLabel(packageManager).toString()

    fun humanReadableSize(bytes: Long, decimalPlaces: Int = 1): String {
        if (bytes <= 0) return "0 Bytes"
        val units = arrayOf("Bytes", "KB", "MB", "GB", "TB", "PB", "EB")
        val magnitude = (log10(bytes.toDouble()) / log10(1000.0)).toInt()
        val unitIndex = if (magnitude >= units.size) units.size - 1 else magnitude
        val valueInUnit = bytes / 1000.0.pow(unitIndex.toDouble())
        val decimalFormat = DecimalFormat("#,##0.${"#".repeat(decimalPlaces)}")

        return "${decimalFormat.format(valueInUnit)} ${units[unitIndex]}"
    }
}