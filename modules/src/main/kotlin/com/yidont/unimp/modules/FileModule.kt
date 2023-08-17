package com.yidont.unimp.modules

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.fragment.app.FragmentActivity
import com.yidont.library.utils.saveFileToDownloadDir
import io.dcloud.feature.uniapp.annotation.UniJSMethod
import io.dcloud.feature.uniapp.bridge.UniJSCallback
import io.dcloud.feature.uniapp.common.UniModule
import io.dcloud.feature.uniapp.utils.UniLogUtils
import java.io.File
import java.util.concurrent.Executors


open class FileModule : UniModule() {

    companion object {
        fun getTempFile(context: Context, tempPath: String, appId: String): File {
            val externalPath = context.getExternalFilesDir(null)?.parentFile?.path ?: ""
            val parentFile = File(externalPath, "apps/$appId/")
            return File(parentFile, tempPath)
        }
    }

    private var success: UniJSCallback? = null

    // type */* 所有文件类型
    @UniJSMethod(uiThread = true)
    fun chooseFile(type: String?, success: UniJSCallback) {
        this.success = success
        val intent = Intent(Intent.ACTION_GET_CONTENT)
            .addCategory(Intent.CATEGORY_OPENABLE)
            .setType(type ?: "*/*")
        val context = mUniSDKInstance.context as Activity
        try {
            context.startActivityForResult(intent, 1)
        } catch (e: Exception) {
            success.invoke("")
        }
    }

    @UniJSMethod(uiThread = true)
    fun downFileToFileApp(filePath: String, callback: UniJSCallback) {
        UniLogUtils.d("filePath=$filePath")
        val activity = mUniSDKInstance.context as FragmentActivity
        val appId =
            mUniSDKInstance.bundleUrl?.substringAfter("/apps/")?.substringBefore('/')
        val file = File(
            activity.getExternalFilesDir(null)?.parentFile,
            "apps/$appId/${filePath.replaceFirst("_", "")}"
        )
        Executors.newSingleThreadExecutor().execute {
            try {
                saveFileToDownloadDir(activity, file)
                callback.invoke(file.name)
            } catch (e: Exception) {
                UniLogUtils.e("保存文件失败", e)
                callback.invoke(null)
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            1 -> {
                val uri = data?.data
                if (resultCode == Activity.RESULT_OK && uri != null) {
                    success?.invoke(uri.toString())
                } else {
                    success?.invoke("")
                }
            }
        }
    }


}