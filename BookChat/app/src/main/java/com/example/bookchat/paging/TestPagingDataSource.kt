package com.example.bookchat.paging

import com.example.bookchat.data.Agony
import com.example.bookchat.data.AgonyRecord
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.BookShelfResult
import com.example.bookchat.response.BookShelfMeta
import com.example.bookchat.response.CursorMeta
import com.example.bookchat.response.ResponseGetAgony
import com.example.bookchat.response.ResponseGetAgonyRecord
import com.example.bookchat.utils.AgonyFolderHexColor

object TestPagingDataSource {

    fun getTestReadingBookPagingSource() :BookShelfResult{
        val testReadingBookList = mutableListOf<BookShelfItem>()
        val bookShelfItem = BookShelfItem(1,"test","12345",
            "https://search1.kakaocdn.net/thumb/R120x174.q85/?fname=http%3A%2F%2Ft1.daumcdn.net%2Flbook%2Fimage%2F1590611%3Ftimestamp%3D20221209172054%22,%22isbn%22:%221158883595",
            listOf("joshua"),"북챗출판사","2000-10-02", 151,null
        )
        for (i in 0 until 20){
            testReadingBookList.add(bookShelfItem.copy(bookShelfId = i.toLong(), title = "test$i", pages = 150+i))
        }
        val testPagedMeta = BookShelfMeta(testReadingBookList.size.toLong(),1,1,0,0,true,true,false)
        return BookShelfResult(testReadingBookList,testPagedMeta)
    }

    fun getTestAgonyPagingSource() : ResponseGetAgony {
        val agonyFolderHexColorList = listOf(
            AgonyFolderHexColor.WHITE, AgonyFolderHexColor.BLACK,
            AgonyFolderHexColor.PURPLE, AgonyFolderHexColor.MINT,
            AgonyFolderHexColor.GREEN, AgonyFolderHexColor.YELLOW,
            AgonyFolderHexColor.ORANGE
        )
        val agony = Agony(1L,"테스트 제목1", AgonyFolderHexColor.WHITE)
        val testPagedAgony = mutableListOf<Agony>()
        for (i in 0 until 30){
            testPagedAgony.add(agony.copy(agonyId = i.toLong(), title = "테스트 제목$i", hexColorCode = agonyFolderHexColorList[i%agonyFolderHexColorList.size]))
        }
        val testMeta = CursorMeta(1,6,true,false,true,true,1)
        return ResponseGetAgony(testPagedAgony,testMeta)
    }

    fun getTestAgonyRecordPagingSource() : ResponseGetAgonyRecord {
        val testPagedAgonyRecord = mutableListOf<AgonyRecord>()
        val agonyRecord = AgonyRecord(1L,"테스트 제목1","테스트 내용1", "2023-01-07")
        for (i in 0 until 20){
            testPagedAgonyRecord.add(agonyRecord.copy(agonyRecordId = i.toLong(), agonyRecordTitle = "테스트 제목$i", agonyRecordContent = "테스트 내용$i", createdAt = "2023-01-07"))
        }
        val testMeta = CursorMeta(1,6,true,false,true,true,1)
        return ResponseGetAgonyRecord(testPagedAgonyRecord,testMeta)
    }

}