package com.autoskip.app

import android.accessibilityservice.AccessibilityService
import android.accessibilityservice.GestureDescription
import android.graphics.Path
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import android.util.Log
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import android.view.accessibility.AccessibilityWindowInfo

/**
 * Runs in the background while enabled in Accessibility settings. It only receives
 * events from YouTube (see accessibility_service_config.xml) and only ever clicks
 * the skip-ad button found by [SkipAdFinder].
 */
class AutoSkipService : AccessibilityService() {

    private val handler = Handler(Looper.getMainLooper())
    private var scanScheduled = false
    private var lastClickAt = 0L

    private val scanRunnable = Runnable {
        scanScheduled = false
        scan()
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent) {
        if (event.packageName?.toString() != SkipAdMatcher.YOUTUBE_PACKAGE) return
        if (!Prefs.isAutoSkipEnabled(this)) return
        // YouTube fires many content-change events per second; coalesce them into one scan.
        if (!scanScheduled) {
            scanScheduled = true
            handler.postDelayed(scanRunnable, SCAN_DELAY_MS)
        }
    }

    override fun onInterrupt() = Unit

    override fun onDestroy() {
        handler.removeCallbacks(scanRunnable)
        super.onDestroy()
    }

    private fun scan() {
        if (SystemClock.elapsedRealtime() - lastClickAt < CLICK_COOLDOWN_MS) return

        // Width × height is orientation independent, so portrait and landscape behave the same.
        val metrics = resources.displayMetrics
        val screenArea = metrics.widthPixels.toLong() * metrics.heightPixels.toLong()

        for (root in youTubeRoots()) {
            val target = SkipAdFinder.find(root, screenArea) ?: continue
            val clicked = click(target)
            Log.i(TAG, "skip clicked=$clicked id=${target.node?.viewIdResourceName} " +
                "text=${target.node?.text} desc=${target.node?.contentDescription} bounds=${target.bounds}")
            if (clicked) {
                lastClickAt = SystemClock.elapsedRealtime()
                Prefs.incrementSkippedCount(this)
            }
            return
        }
    }

    private fun youTubeRoots(): List<AccessibilityNodeInfo> {
        val fromWindows = windows
            .filter { it.type == AccessibilityWindowInfo.TYPE_APPLICATION }
            .mapNotNull { it.root }
            .filter { it.packageName?.toString() == SkipAdMatcher.YOUTUBE_PACKAGE }
        if (fromWindows.isNotEmpty()) return fromWindows
        return listOfNotNull(rootInActiveWindow)
            .filter { it.packageName?.toString() == SkipAdMatcher.YOUTUBE_PACKAGE }
    }

    private fun click(target: ClickTarget): Boolean {
        if (target.node?.performAction(AccessibilityNodeInfo.ACTION_CLICK) == true) return true
        // Some YouTube builds render the button without a clickable node: tap its centre instead.
        val path = Path().apply { moveTo(target.bounds.exactCenterX(), target.bounds.exactCenterY()) }
        val gesture = GestureDescription.Builder()
            .addStroke(GestureDescription.StrokeDescription(path, 0, TAP_DURATION_MS))
            .build()
        return dispatchGesture(gesture, null, null)
    }

    private companion object {
        const val TAG = "AutoSkip"
        const val SCAN_DELAY_MS = 250L
        const val CLICK_COOLDOWN_MS = 1500L
        const val TAP_DURATION_MS = 50L
    }
}
