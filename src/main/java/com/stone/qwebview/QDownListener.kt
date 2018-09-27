package com.stone.qwebview

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.webkit.DownloadListener
import com.stone.commonutils.isValid
import com.stone.log.Logs
import com.stone.mdlib.MDAlert
import org.jetbrains.anko.toast

/**
 * Created By: sqq
 * Created Time: 17/7/6 上午11:42.
 */

class QDownListener(private val activity: Activity) : DownloadListener {
    override fun onDownloadStart(url: String, userAgent: String, contentDisposition: String, mimetype: String, contentLength: Long) {
        if (!activity.isValid()) return
        //跳转至手机浏览器去下载
        MDAlert(activity, "是否打开浏览器去下载？").setCancel()
                .setBtnPositive {
                    val uri = Uri.parse(url)
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    try {
                        activity.startActivity(intent)
                    } catch (e: Exception) {
                        Logs.w("onDownloadStart: ${e.message}")
                        activity.toast("请安装浏览器后重试")
                    }
                }.show()
    }
}
