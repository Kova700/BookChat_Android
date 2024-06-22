package com.example.bookchat.ui.channel.chatting.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.util.LruCache
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.ui.channel.chatting.adapter.ChatItemAdapter
import com.example.bookchat.ui.channel.chatting.adapter.ChatItemViewHolder
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.bitmap.getMergedBitmap
import com.example.bookchat.utils.image.saveImage

fun RecyclerView.captureItems(
	headerIndex: Int,
	bottomIndex: Int,
) {
	val adapter = this.adapter as? ChatItemAdapter ?: throw Exception("Adapter is null")
	if (adapter.itemCount - 1 < headerIndex) throw Exception("Header index is bigger than item count")

	var startIndex = bottomIndex

	var bitmapHeight = 0
	val maxMemoryKB = (Runtime.getRuntime().maxMemory() / 1024).toInt()
	val cacheSize = maxMemoryKB / 4
	val bitmapCache = LruCache<Int, Bitmap>(cacheSize)

	for (index in bottomIndex..headerIndex) {
		val holder = findViewHolderForAdapterPosition(index) as? ChatItemViewHolder
			?: adapter.createViewHolder(this, adapter.getItemViewType(index))
		adapter.onBindViewHolderForCapture(holder, index)

		holder.itemView.measure(
			View.MeasureSpec.makeMeasureSpec(this.width, View.MeasureSpec.EXACTLY),
			View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
		)
		holder.itemView.layout(0, 0, holder.itemView.measuredWidth, holder.itemView.measuredHeight)

		val bitmap = Bitmap.createBitmap(
			holder.itemView.width,
			holder.itemView.height,
			Bitmap.Config.ARGB_8888
		)
		val canvas = Canvas(bitmap)
		holder.itemView.draw(canvas)

		/** 제한된 메모리 사이즈가 초과된다면 이미지 분할 저장 */
		if (bitmapCache.canPutBitmapInCache(bitmap).not()) {
			val mergedBitmap = getMergedBitmap(
				cacheRange = bottomIndex..headerIndex,
				bitmapCache = bitmapCache,
				bigBitmapWidth = this.measuredWidth,
				bigBitmapHeight = bitmapHeight
			)
			context.saveImage(mergedBitmap)
			bitmapCache.evictAll()
			startIndex = index
			bitmapHeight = 0
		}

		bitmapCache.put(index, bitmap)
		bitmapHeight += holder.itemView.measuredHeight
	}

	val mergedBitmap = getMergedBitmap(
		cacheRange = startIndex..headerIndex,
		bitmapCache = bitmapCache,
		bigBitmapWidth = this.measuredWidth,
		bigBitmapHeight = bitmapHeight
	)
	context.saveImage(mergedBitmap)
}

//TODO : 분할 저장 잘 작동하는지 확인
fun LruCache<Int, Bitmap>.canPutBitmapInCache(bitmap: Bitmap): Boolean {
	val bitmapKBSize = bitmap.byteCount / 1024
	val currentCacheSize = this.size()
	val maxCacheSize = this.maxSize()
//	val maxCacheSize = 2 //TODO : 이거 줄이니까 바로 그냥
	//  ChannelActivity: makeCaptureImage() - throwable : java.lang.IllegalArgumentException: width and height must be > 0 터짐
	Log.d(
		TAG, ": canPutBitmapInCache() - currentCacheSize : $currentCacheSize \n" +
						"maxCacheSize(maxMemoryKB / 4) : $maxCacheSize"
	)
	return currentCacheSize + bitmapKBSize <= maxCacheSize
}