package com.autoskip.app

import android.graphics.Rect
import android.view.accessibility.AccessibilityNodeInfo

/** Where to click: a clickable node, or (fallback) the centre of the matched button's bounds. */
class ClickTarget(val node: AccessibilityNodeInfo?, val bounds: Rect)

/**
 * Locates the skip-ad button inside a YouTube window. Never returns anything that
 * isn't matched by the skip button's view ID or by a complete "skip ad" label.
 */
object SkipAdFinder {

    private const val MAX_NODES_VISITED = 1500
    private const val MAX_ANCESTOR_DEPTH = 3

    /**
     * A clickable ancestor larger than this fraction of the screen is almost certainly
     * the video player itself; clicking it would pause the video, so we never do.
     */
    private const val MAX_TARGET_SCREEN_FRACTION = 0.25

    fun find(root: AccessibilityNodeInfo, screenArea: Long): ClickTarget? {
        val maxArea = (screenArea * MAX_TARGET_SCREEN_FRACTION).toLong()

        // 1. Fast path: known view IDs (language independent).
        for (id in SkipAdMatcher.SKIP_VIEW_IDS) {
            val nodes = root.findAccessibilityNodeInfosByViewId("${SkipAdMatcher.YOUTUBE_PACKAGE}:id/$id")
            for (node in nodes) {
                resolve(node, maxArea)?.let { return it }
            }
        }

        // 2. Fallback: full "skip ad" label in text or content description (breadth-first).
        val queue = ArrayDeque<AccessibilityNodeInfo>()
        queue.add(root)
        var visited = 0
        while (queue.isNotEmpty() && visited < MAX_NODES_VISITED) {
            val node = queue.removeFirst()
            visited++
            if (SkipAdMatcher.isSkipText(node.text) || SkipAdMatcher.isSkipText(node.contentDescription)) {
                resolve(node, maxArea)?.let { return it }
            }
            for (i in 0 until node.childCount) {
                node.getChild(i)?.let(queue::add)
            }
        }
        return null
    }

    private fun resolve(matched: AccessibilityNodeInfo, maxArea: Long): ClickTarget? {
        if (!matched.isVisibleToUser || !matched.isEnabled) return null
        val matchedBounds = Rect().also(matched::getBoundsInScreen)
        if (matchedBounds.isEmpty || matchedBounds.area() > maxArea) return null

        // The label is often a TextView inside the clickable button: climb a few levels.
        var current: AccessibilityNodeInfo? = matched
        var depth = 0
        while (current != null && depth <= MAX_ANCESTOR_DEPTH) {
            val bounds = Rect().also(current::getBoundsInScreen)
            if (bounds.area() > maxArea) break
            if (current.isClickable && current.isEnabled && current.isVisibleToUser) {
                return ClickTarget(current, bounds)
            }
            current = current.parent
            depth++
        }
        return ClickTarget(null, matchedBounds)
    }

    private fun Rect.area(): Long = width().toLong() * height().toLong()
}
