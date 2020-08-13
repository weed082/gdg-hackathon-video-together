package org.personal.videotogether.view.fragments.home.nestonhome

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_friends_list.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.domianmodel.FriendData
import org.personal.videotogether.util.view.DataStateHandler
import org.personal.videotogether.view.adapter.FriendListAdapter
import org.personal.videotogether.view.fragments.home.MainHomeFragmentDirections
import org.personal.videotogether.viewmodel.FriendStateEvent
import org.personal.videotogether.viewmodel.FriendViewModel
import org.personal.videotogether.viewmodel.UserStateEvent
import org.personal.videotogether.viewmodel.UserViewModel

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class FriendsListFragment
constructor(
    private val dataStateHandler: DataStateHandler
) : Fragment(R.layout.fragment_friends_list), FriendListAdapter.ItemClickListener, View.OnClickListener {

    private val TAG = javaClass.name

    // 네비게이션 + 뷰모델
    private lateinit var mainNavController: NavController
    private val friendViewModel: FriendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }

    // 리사이클러 뷰
    private val friendList by lazy { ArrayList<FriendData>() }
    private val friendListAdapter by lazy { FriendListAdapter(requireContext(), friendList, false, this) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val mainFragmentContainer: FragmentContainerView = view.rootView.findViewById(R.id.mainFragmentContainer)
        mainNavController = Navigation.findNavController(mainFragmentContainer)
        setListener()
        subscribeObservers()
        buildRecyclerView()
        userViewModel.setStateEvent(UserStateEvent.GetUserDataFromLocal)
        friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromLocal)
    }

    private fun setListener() {
        myProfileContainerCL.setOnClickListener(this)
    }

    private fun subscribeObservers() {
        // 사용자 정보를 room 으로부터 가져옴
        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            Glide.with(requireContext()).load(userData!!.profileImageUrl).into(profileIV)
            nameTV.text = userData.name
        })

        // 친구 목록 불러오기
        friendViewModel.friendList.observe(viewLifecycleOwner, Observer { dataState ->
            friendList.clear()
            Log.i(TAG, "subscribeObservers: $dataState")
            dataState!!.forEach { friendData ->
                Log.i(TAG, "subscribeObservers: $friendData")
                friendList.add(friendData)
            }
            friendListAdapter.notifyDataSetChanged()
        })
    }

    private fun buildRecyclerView() {
        val layoutManager = LinearLayoutManager(requireContext())

        friendsListRV.setHasFixedSize(true)
        friendsListRV.layoutManager = layoutManager
        friendsListRV.adapter = friendListAdapter
    }

    // ------------------ 클릭 리스너 메소드 모음 ------------------
    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.myProfileContainerCL -> {
                mainNavController.navigate(R.id.action_mainHomeFragment_to_profileMineFragment)
            }
        }
    }

    // ------------------ 리사이클러 뷰 아이템 클릭 리스너 메소드 모음 ------------------
    override fun onItemClick(view: View?, itemPosition: Int) {
        val selectedFriendData = friendList[itemPosition]
        val action =
            MainHomeFragmentDirections.actionMainHomeFragmentToProfileFriendFragment(selectedFriendData)
        mainNavController.navigate(action)
    }
}