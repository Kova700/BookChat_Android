package com.example.bookchat.ui.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.bookchat.R
import com.example.bookchat.adapter.MainChatRoomAdapter
import com.example.bookchat.databinding.FragmentHomeBinding

//이미 살아있는 Fragment 객체가 있으면 그 상태를 불러오는게 Best (스크롤 상태까지 그대로)

class HomeFragment : Fragment() {

    lateinit var binding :FragmentHomeBinding
    private lateinit var chatRoomAdapter: MainChatRoomAdapter

    //프래그먼트가 액티비티에 붙을 때 호출됨
    //인자로 Context가 주어진다.
    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    //프래그먼트가 액티비티의 호출을 받아 생성됨
    //Bundle로 액티비티로부터 데이터가 넘어옴
    //UI초기화는 불가능
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)
        initRecyclerView()


        return binding.root
    }



    //onCreateView()를 통해 반환된 View 객체는 onViewCreated()의 파라미터로 전달 된다.
    //이 때 Lifecycle이 INITIALIZED 상태로 업데이트가 됨
    //때문에 View의 초기값 설정, LiveData 옵저빙, RecyclerView, ViewPager2에 사용될 Adapter 세팅은 이 메소드에서 해주는 것이 적절함
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    //저장해둔 모든 state 값이 Fragment의 View의 계층 구조에 복원되었을 때 호출 ex) 체크박스 위젯이 현재 체크되어있는가
    //View lifecycle owner : INITIALIZED → CREATED 변경
    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    //프래그먼트가 액티비티로부터 해제되어질 때 호출된다.
    override fun onDetach() {
        super.onDetach()
    }

    private fun initRecyclerView(){
        with(binding){
            chatRoomAdapter = MainChatRoomAdapter()
            todayChatRoomListView.adapter = chatRoomAdapter
            todayChatRoomListView.setHasFixedSize(true)
            todayChatRoomListView.layoutManager = LinearLayoutManager(requireContext())
//            val snapHelper = LinearSnapHelper()
//            snapHelper.attachToRecyclerView(chatRoomRecyclerView)
        }
    }

}