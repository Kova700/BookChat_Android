package com.example.bookchat.ui.channel.chatting.util

import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import android.util.LruCache
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.example.bookchat.utils.Constants.TAG
import com.example.bookchat.utils.bitmap.getMergedBitmap
import com.example.bookchat.utils.image.saveImage

//TODO :
// 문제점 1 : 채팅 내용이 담겨 있지 않은 채로 이미지 캡처됨 (프로필 마저 담기지 않음)
//    (발송 시간, 방장 유무는 정확하게 나옴)
// 문제점 2 : 공지 메세지 + 날짜 메세지 마저 내용이 들어가있지 않음
// 문제점 3 : 캡처 선택된 배경이 사라지지 않고 남아있음

fun RecyclerView.captureItems(
	headerIndex: Int,
	bottomIndex: Int,
) {
	val adapter = this.adapter ?: throw Exception("Adapter is null")
	if (adapter.itemCount - 1 < headerIndex) throw Exception("Header index is bigger than item count")

	var startIndex = bottomIndex

	var bitmapHeight = 0
	val maxMemoryKB = (Runtime.getRuntime().maxMemory() / 1024).toInt()
	val cacheSize = maxMemoryKB / 4
	val bitmapCache = LruCache<Int, Bitmap>(cacheSize)

	for (index in bottomIndex..headerIndex) {
		val holder = adapter.createViewHolder(this, adapter.getItemViewType(index))
		// TODO : 필요한지 체크해볼 것 (여기서부터)
		adapter.onBindViewHolder(holder, index)

		holder.itemView.measure(
			View.MeasureSpec.makeMeasureSpec(this.width, View.MeasureSpec.EXACTLY),
			View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
		)
		// TODO : 여기까지
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

//TODO : 의문 1. 굳이 ViewHolder를 생성해서 캡쳐하는 이유가 무엇인가?
//        (기존에 있는 ViewHolder가 있다면 그대로 재활용 해도 되는거 아닌가)

//TODO : 분할 저장 잘 작동하는지 확인
fun LruCache<Int, Bitmap>.canPutBitmapInCache(bitmap: Bitmap): Boolean {
	val bitmapKBSize = bitmap.byteCount / 1024
	val currentCacheSize = this.size()
//	val maxCacheSize = this.maxSize()
	val maxCacheSize = 2 //TODO : 이거 줄이니까 바로 그냥
	//  ChannelActivity: makeCaptureImage() - throwable : java.lang.IllegalArgumentException: width and height must be > 0 터짐
	Log.d(
		TAG, ": canPutBitmapInCache() - currentCacheSize : $currentCacheSize \n" +
						"maxCacheSize(maxMemoryKB / 4) : $maxCacheSize"
	)
	return currentCacheSize + bitmapKBSize <= maxCacheSize
}