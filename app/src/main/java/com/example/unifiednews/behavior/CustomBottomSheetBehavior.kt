package com.example.unifiednews.behavior

import android.content.Context
import android.util.AttributeSet
import androidx.coordinatorlayout.widget.CoordinatorLayout
import com.google.android.material.bottomsheet.BottomSheetBehavior
import android.view.MotionEvent
import android.view.View

class CustomBottomSheetBehavior<V : View>(context: Context, attrs: AttributeSet) : BottomSheetBehavior<V>(context, attrs) {

    private var draggableHandle: View? = null

    fun setDraggableHandle(handle: View) {
        draggableHandle = handle
    }

    override fun onInterceptTouchEvent(parent: CoordinatorLayout, child: V, event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN && isWithinHandle(event)) {
            return super.onInterceptTouchEvent(parent, child, event)
        }
        return false
    }

    private fun isWithinHandle(event: MotionEvent): Boolean {
        draggableHandle?.let {
            val handleLocation = IntArray(2)
            it.getLocationOnScreen(handleLocation)
            val handleLeft = handleLocation[0]
            val handleRight = handleLeft + it.width
            val handleTop = handleLocation[1]
            val handleBottom = handleTop + it.height

            return event.rawX >= handleLeft && event.rawX <= handleRight
                    && event.rawY >= handleTop && event.rawY <= handleBottom
        }
        return false
    }
}