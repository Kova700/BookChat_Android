package com.example.bookchat.paging

import com.example.bookchat.data.Agony
import com.example.bookchat.data.AgonyRecord
import com.example.bookchat.data.BookShelfItem
import com.example.bookchat.data.ChatRoomListItem
import com.example.bookchat.data.response.*
import com.example.bookchat.utils.AgonyFolderHexColor

object TestPagingDataSource {

    fun getChatRoomListPagingSource() : ResponseGetChatRoomList {
        val testChatRoomDataList = mutableListOf<ChatRoomListItem>()
        val item = ChatRoomListItem(1L,"채팅방 제목","roomSid",3L,2,"https://img1.daumcdn.net/thumb/R1280x0/?scode=mtistory2&fname=https%3A%2F%2Ft1.daumcdn.net%2Fcfile%2Ftistory%2F996333405A8280FC23",
        3L,"2023-02-13T16:03:23.8916109","블라블라")
        val item2 = item.copy(roomId = 2L,lastActiveTime = "2023-02-28T02:55:06")
        val item3 = item.copy(roomId = 3L,lastActiveTime = "2023-02-27T02:55:06")
        val item4 = item.copy(roomId = 4L,lastActiveTime = "2022-12-03T02:55:06")
        val item5 = item.copy(roomId = 5L,lastActiveTime = "2023-03-01T03:29:01")
        val item6 = item.copy(roomId = 5L,lastActiveTime = "2023-03-01T13:29:01")
        val item7 = item.copy(roomId = 5L,lastActiveTime = "2023-03-01T00:29:01")
        val item8 = item.copy(roomId = 5L,lastActiveTime = "2023-03-01T23:29:01")
        testChatRoomDataList.addAll(listOf(item2,item3,item4,item5,item6,item7,item8))
        for (i in 9 until 20){
            testChatRoomDataList.add(item.copy(roomId = i.toLong()))
        }
        val testMeta = CursorMeta(1,6,true,false,true,true,1)
        return ResponseGetChatRoomList(testChatRoomDataList, testMeta)
    }


    fun getTestReadingBookPagingSource() : ResponseGetBookShelfBooks {
        val testReadingBookList = mutableListOf<BookShelfItem>()
        val bookShelfItem = BookShelfItem(1,"test","12345",
            "https://search1.kakaocdn.net/thumb/R120x174.q85/?fname=http%3A%2F%2Ft1.daumcdn.net%2Flbook%2Fimage%2F1590611%3Ftimestamp%3D20221209172054%22,%22isbn%22:%221158883595",
            listOf("joshua"),"북챗출판사","2000-10-02", 151,null
        )
        for (i in 0 until 20){
            testReadingBookList.add(bookShelfItem.copy(bookShelfId = i.toLong(), title = "test$i", pages = 150+i))
        }
        val testPagedMeta = BookShelfMeta(testReadingBookList.size.toLong(),1,1,0,0,true,true,false)
        return ResponseGetBookShelfBooks(testReadingBookList,testPagedMeta)
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