package com.stone.qwebview

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.view.View
import android.view.ViewGroup
import android.webkit.WebSettings
import android.webkit.WebView
import com.stone.commonutils.isValid
import com.stone.log.Logs
import com.stone.mdlib.MDAlert

/**
 * 关于WebView及其附属类的一些函数扩展
 */

/**
 * 使用此项需要注意的是，WebView依赖的上下文需要是Activity Context，而非Application Context
 */
internal val WebView.activity: Activity
    get() = context as Activity

/**
 * 根据项目需要，配置WebView
 */
@SuppressLint("SetJavaScriptEnabled")
internal fun WebView.customSetting() {
    settings.javaScriptEnabled = true
    settings.defaultTextEncodingName = "UTF-8"
    settings.domStorageEnabled = true
    settings.cacheMode = WebSettings.LOAD_NO_CACHE
    //WebView中启用或禁用文件访问
    settings.allowFileAccess = true
    /* 一般很少会用到这个，用WebView组件显示普通网页时一般会出现横向滚动条，这样会导致页面查看起来非常不方便。LayoutAlgorithm是一个枚举，用来控制html的布局，总共有三种类型：
        NORMAL：正常显示，没有渲染变化。
        SINGLE_COLUMN：把所有内容放到WebView组件等宽的一列中。
        NARROW_COLUMNS：可能的话，使所有列的宽度不超过屏幕宽度。*/
    settings.layoutAlgorithm = WebSettings.LayoutAlgorithm.NARROW_COLUMNS
    //设置是否支持缩放，我这里为false，默认为true。
    settings.setSupportZoom(false)
    //置是否显示缩放工具，默认为false。
    settings.builtInZoomControls = false
    //设置webview推荐使用的窗口 设置加载进来的页面自适应手机屏幕
    settings.useWideViewPort = false
    settings.loadWithOverviewMode = true
    //默认的是false，也就是说WebView默人不支持新窗口，但是这个不是说WebView不能打开多个页面了，只是你点击页面上的连接，当它的target属性是_blank时。它会在当前你所看到的页面继续加载那个连接。而不是重新打开一个窗口。
    //当你设置为true时，就代表你想要你的WebView支持多窗口，但是一旦设置为true，必须要重写WebChromeClient的onCreateWindow方法。
    settings.setSupportMultipleWindows(false)
    // 启用缓存 默认关闭，即，H5的缓存无法使用。
    settings.setAppCacheEnabled(true)

    // 启用地理定位
    settings.setGeolocationEnabled(true)
    // 设置缓存大小,默认Long.MAX_VALUE
    settings.setAppCacheMaxSize(Long.MAX_VALUE)

    settings.allowContentAccess = true
    // 开启数据库缓存
    settings.databaseEnabled = true
    settings.savePassword = false
    settings.saveFormData = false

//    settings.setAppCachePath(this.context.getDir("appcache", 0).path)
//    settings.databasePath = this.context.getDir("databases", 0).path
//    settings.setGeolocationDatabasePath(this.context.getDir("geolocation", 0).path)
    //安卓5.0以后，默认不允许混合模式，https中不能加载http的资源，下面开启混合模式
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
        settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW
    }
    Logs.w("WebActivity", "initWebView: " + settings.userAgentString)

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
        removeJavascriptInterface("searchBoxJavaBridge_")
        removeJavascriptInterface("accessibility")
        removeJavascriptInterface("accessibilityTraversal")
    }

    //设置下载监听 // FIXME: 8/23/18
//    setDownloadListener(MyDownListener(activity))
}

/**
 * loadURL之前，针对URL做特定过滤处理
 */
internal fun WebView.loadUrlWithCheck(url: String?) {
    if (url.isNullOrEmpty()) return
    else loadUrl(url)
}

/**
 * WebViewClient.shouldOverrideUrlLoading自定义逻辑处理
 * @return true: 使用自定义逻辑处理了当前的URL，阻拦WebView默认逻辑的执行；false：可自定义逻辑，但是不阻拦WebView的默认逻辑执行
 */
internal fun WebView.overrideUrlLoading(url: String?): Boolean {
    if (url == null) return true
    if (url.startsWith("tel:")) {//支持网页拨号
        if (activity.isValid()) {
            MDAlert(activity, "是否拨打电话：\n$url").setCancel()
                    .setBtnPositive {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse(url))
                        activity.startActivity(intent)
                    }.show()
            return true
        }
    } else if (url.startsWith("weixin://") || url.startsWith("intent://")) {
        try {
            val resultUrl: String = if (url.startsWith("intent://") && url.contains("com.tencent.mm")) {
                url.replace("intent://", "weixin://")
            } else {
                url
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(resultUrl))
            if (activity.isValid()) {
                activity.startActivity(intent)
                return true
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
    return false
}

/**
 * 将WebActivity设置标题的方法，扩展到WebView中，避免ChromeClient的receiveTitle回调中直接引用WebActivity的方法
 */
internal fun WebView.setTitle(title: String?) {
    if (title.isNullOrEmpty() || title!!.contains("http") || title.contains("daiqianb")) {
        // TODO: 8/23/18 关于默认标题的处理
        activity.setTitle(R.string.app_name)
    } else if (title == "找不到网页" || title == "网页无法打开") {
        activity.title = "网络连接失败"
    } else {
        activity.title = title
    }
    //设置title
    Logs.i("WebActivity", "setTitle() called with: title = [$title]")
    Logs.i("WebActivity", "setTitle: webView.getOriginalUrl() = " + this.originalUrl)
}

/**
 * 销毁WebView
 */
internal fun WebView?.selfDestroy() {
    if (this == null) return
    this.clearCache(true) //清空缓存
    try {
        settings.builtInZoomControls = true
        visibility = View.GONE
    } catch (ignored: Throwable) {
    }
    try {
        stopLoading()
    } catch (ignored: Throwable) {
    }
    try {
        removeAllViews()
    } catch (ignored: Throwable) {
    }
    try {
        webChromeClient = null
    } catch (ignored: Throwable) {
    }
    try {
        webViewClient = null
    } catch (ignored: Throwable) {
    }
    try {
        destroy()
    } catch (ignored: Throwable) {
    }
    try {
        if (null != parent && parent is ViewGroup) {
            (parent as ViewGroup).removeView(this)
        }
    } catch (ignored: Throwable) {
    }
}