package com.stone.qwebview

import android.graphics.Bitmap
import android.net.http.SslError
import android.view.View
import android.webkit.*
import com.stone.log.Logs

/**
 * Created By: sqq
 * Created Time: 17/12/7 下午5:26.
 */
class QWebViewClient(
        private val errorView: View? = null,
//        private val mPtrFrame: PtrFrameLayout? = null,
        private val loadingView: View? = null
) : WebViewClient() {

    // TODO: 8/23/18 可以通过多个constructor来 处理多种case
    /**
     * 1. errorView, loadingView
     * 2. LoadFrameLayout
     * 3. UI操作回调，主constructor
     */

    private var isReceivedError: Boolean = false

    override fun shouldOverrideUrlLoading(view: WebView, url: String?): Boolean {
        Logs.i(TAG, "shouldOverrideUrlLoading() called with: url = [$url]")
        return view.overrideUrlLoading(url)
    }

    override fun onReceivedSslError(view: WebView, handler: SslErrorHandler, error: SslError) {
        handler.proceed()
    }

    override fun onPageStarted(view: WebView, url: String?, favicon: Bitmap?) {
        super.onPageStarted(view, url, favicon)
        errorView?.visibility = View.GONE
        loadingView?.visibility = View.VISIBLE
        isReceivedError = false
        Logs.e(TAG, "onPageStarted ")
    }

    override fun onPageFinished(view: WebView, url: String) {
        super.onPageFinished(view, url)
//        mPtrFrame?.refreshComplete()
        loadingView?.visibility = View.GONE
        if (!isReceivedError && view.visibility != View.VISIBLE) {
            view.visibility = View.VISIBLE
        }
        Logs.e(TAG, "onPageFinished ")
    }

    override fun onReceivedError(view: WebView, request: WebResourceRequest?, error: WebResourceError?) {
        super.onReceivedError(view, request, error)

        /*此新版本的错误回调，无需处理逻辑；在实际使用中发现：存在着 页面加载正常，deprecated的方法未被回调，但是此新版本的回调被执行了，导致不应该展示错误页面的时候，却展示错误UI.*/
//        errorView?.visibility = View.VISIBLE
//        loadingView?.visibility = View.GONE
//        view.visibility = View.INVISIBLE
//        isReceivedError = true
        Logs.e(TAG, "onReceivedError new ")
//        view.loadUrl("about:blank")
    }

    //onReceivedError()发生后会继续回调onPageFinish()
    override fun onReceivedError(view: WebView, errorCode: Int, description: String, failingUrl: String) {
        super.onReceivedError(view, errorCode, description, failingUrl)
        errorView?.visibility = View.VISIBLE
        loadingView?.visibility = View.GONE
        view.visibility = View.INVISIBLE
        isReceivedError = true
        Logs.e(TAG, "onReceivedError deprecated")
//        view.loadUrl("about:blank")
    }

    companion object {
        private const val TAG = "MainWebViewClient"
    }
}
