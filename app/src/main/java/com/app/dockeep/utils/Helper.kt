package com.app.dockeep.utils

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

    fun Context.shareDocument(uri: Uri, mimeType: String?) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_STREAM, uri)
            type = mimeType
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
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
        if (bytes <= 0) return "0 Bytes" // Or handle negative values as an error/empty string

        // Array of units
        val units = arrayOf("Bytes", "KB", "MB", "GB", "TB", "PB", "EB")

        // Calculate the magnitude
        val magnitude = (log10(bytes.toDouble()) / log10(1000.0)).toInt()

        // Ensure magnitude is within the bounds of our units array
        val unitIndex = if (magnitude >= units.size) units.size - 1 else magnitude

        // Calculate the value in the chosen unit
        val valueInUnit = bytes / 1000.0.pow(unitIndex.toDouble())

        // Format the number with specified decimal places
        val decimalFormat = DecimalFormat("#,##0.${"#".repeat(decimalPlaces)}")
        // Or for a fixed number of decimal places without grouping separator:
        // val decimalFormat = DecimalFormat("0.${"0".repeat(decimalPlaces)}")


        return "${decimalFormat.format(valueInUnit)} ${units[unitIndex]}"
    }
}