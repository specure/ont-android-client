package at.specure.android.util.network.ip

import timber.log.Timber
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Singleton

@Singleton
class CaptivePortal {
    var captivePortalStatus: CaptivePortalStatus = CaptivePortalStatus.NOT_TESTED
    private var isCaptivePortalTestRunning = false
    val WALLED_GARDEN_URL = "YOUR_CAPTIVE_PORTAL_URL"

    fun resetCaptivePortalStatus() {
        captivePortalStatus = CaptivePortalStatus.NOT_TESTED
    }

    fun checkForCaptivePortal() {
        if (!isCaptivePortalTestRunning) {
            isCaptivePortalTestRunning = true
            captivePortalStatus = CaptivePortalStatus.TESTING
            val status = isWalledGardenConnection(WALLED_GARDEN_URL)
            captivePortalStatus = if (status) CaptivePortalStatus.FOUND else CaptivePortalStatus.NOT_FOUND
            Timber.e("CPS detected: $status")
            isCaptivePortalTestRunning = false
        }
    }

    private fun isWalledGardenConnection(urlString: String): Boolean {
        var urlConnection: HttpURLConnection? = null
        try {
            Timber.i("checking for walled garden...")
            val url = URL(urlString)
            urlConnection = url.openConnection() as HttpURLConnection
            urlConnection.instanceFollowRedirects = false
            urlConnection.connectTimeout = WALLED_GARDEN_SOCKET_TIMEOUT_MS
            urlConnection.readTimeout = WALLED_GARDEN_SOCKET_TIMEOUT_MS
            urlConnection.useCaches = false
            urlConnection.inputStream
            Timber.d("check completed, response: ${urlConnection.responseCode}")
            // We got a valid response, but not from the real google
            return urlConnection.responseCode != 204
        } catch (e: IOException) {
            e.printStackTrace()
            return false
        } finally {
            urlConnection?.disconnect()
        }
    }

    enum class CaptivePortalStatus {
        NOT_TESTED,
        FOUND,
        NOT_FOUND,
        TESTING;
    }

    companion object {
        const val WALLED_GARDEN_SOCKET_TIMEOUT_MS = 10000
    }
}