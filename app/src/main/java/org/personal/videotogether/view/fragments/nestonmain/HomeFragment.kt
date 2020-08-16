package org.personal.videotogether.view.fragments.nestonmain

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.fragment.app.Fragment
import android.view.View
import androidx.activity.addCallback
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_home.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.personal.videotogether.R
import org.personal.videotogether.viewmodel.*

@ExperimentalCoroutinesApi
@AndroidEntryPoint
class HomeFragment : Fragment(R.layout.fragment_home) {

    private val TAG = javaClass.name

    private lateinit var mainNavController: NavController
    private lateinit var homeNavController: NavController
    private lateinit var homeDetailNavController: NavController
    private lateinit var videoNavController: NavController

    private val friendViewModel: FriendViewModel by lazy { ViewModelProvider(requireActivity())[FriendViewModel::class.java] }
    private val userViewModel: UserViewModel by lazy { ViewModelProvider(requireActivity())[UserViewModel::class.java] }
    private val chatViewModel by lazy { ViewModelProvider(requireActivity())[ChatViewModel::class.java] }
    private val socketViewModel by lazy { ViewModelProvider(requireActivity())[SocketViewModel::class.java] }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setNavControl(view)
        subscribeObservers()
        setBottomNav()
        userViewModel.setStateEvent(UserStateEvent.GetUserDataFromLocal)
    }

    @SuppressLint("RestrictedApi")
    private fun setNavControl(view: View) {
        val homeFragmentContainer = childFragmentManager.findFragmentById(R.id.homeFragmentContainer)
        val homeDetailFragment = childFragmentManager.findFragmentById(R.id.homeDetailFragmentContainer)
        val videoFragmentContainer = childFragmentManager.findFragmentById(R.id.videoFragmentContainer)

        mainNavController = Navigation.findNavController(view)
        homeNavController = homeFragmentContainer!!.findNavController()
        homeDetailNavController = homeDetailFragment!!.findNavController()
        videoNavController = videoFragmentContainer!!.findNavController()

        Log.e(TAG, "setNavControl: ${childFragmentManager}")
        Log.e(TAG, "setNavControl: ${homeDetailNavController.currentDestination}")
        // 바텀, 앱 바 관련
        bottomNavBN.setupWithNavController(homeNavController)

        // 뒤로가기 버튼 눌렀을 때
        requireActivity().onBackPressedDispatcher.addCallback(this) {

            when {
                homeDetailNavController.backStack.count() > 2 -> homeDetailNavController.popBackStack()
                videoNavController.backStack.count() > 2 -> videoNavController.popBackStack()
                homeNavController.backStack.count() > 2 -> homeNavController.popBackStack()
                else -> {
                    remove()
                    killProcess()
                }
            }
        }
    }

    private fun killProcess() {
        requireActivity().moveTaskToBack(true)
        requireActivity().finishAndRemoveTask()
        // TODO : 뒤로가기 버튼으로 액티비티 스택을 지우면 SocketViewModel onCleared 에서 tcp disconnect 가 호출 되지 않음 -> 해결방안 찾기
        socketViewModel.setStateEvent(SocketStateEvent.DisconnectFromTCPServer)
    }

    private fun subscribeObservers() {
        // 사용자 정보를 room 으로부터 가져옴
        // 유저 정보를 이용해 친구 데이터 업데이트, 채팅 소켓 등록을 함
        userViewModel.userData.observe(viewLifecycleOwner, Observer { userData ->
            friendViewModel.setStateEvent(FriendStateEvent.GetFriendListFromServer(userData!!.id))
            socketViewModel.setStateEvent(SocketStateEvent.RegisterSocket(userData))
            socketViewModel.setStateEvent(SocketStateEvent.ReceiveFromTCPServer)
            Log.i(TAG, "subscribeObservers: 한번?")
        })
    }

    // 상단 앱 바 아이템 추가
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.app_bar_menu, menu)
        when (homeNavController.currentDestination?.label) {
            "친구 목록" -> {
                menu.removeItem(R.id.addChatRoomFragment)
                menu.removeItem(R.id.youtubeSearchFragment)
            }
            "채팅 목록" -> {
                menu.removeItem(R.id.youtubeSearchFragment)
                menu.removeItem(R.id.addFriendFragment)
            }
        }
        super.onCreateOptionsMenu(menu, inflater)
    }

    // 바텀 네비게이션 관련 세팅
    private fun setBottomNav() {

    }

    // ------------------ 상단 앱 바 아이템 클릭 리스너 모음 ------------------
    @SuppressLint("RestrictedApi")
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.i(TAG, "onOptionsItemSelected: ${homeNavController.getBackStackEntry(R.id.friendsListFragment)}")
        when (item.itemId) {
            R.id.youtubeSearchFragment -> {
                homeNavController.navigate(R.id.action_youtubeFragment_to_youtubeSearchFragment)
            }
            else -> {
                NavigationUI.onNavDestinationSelected(item, mainNavController)
            }
        }

        return super.onOptionsItemSelected(item)
    }

//    // ------------------ homeNavController 리스너 모음 ------------------
//// 네비게이션 아이템을 클릭해서 destination 이 변경 될때마다 상단 앱 바 메뉴 아이템 변경
//    override fun onDestinationChanged(controller: NavController, destination: NavDestination, arguments: Bundle?) {
//        val removeItemList = ArrayList<Int>()
//        val menu = homeToolbarTB.menu
//        menu.clear()
//        searchET.visibility = View.GONE
//        homeToolbarTB.title = destination.label
//
//        when (destination.id) {
//
//            R.id.friendsListFragment -> {
//                removeItemList.apply {
//                    add(R.id.youtubeSearchFragment)
//                    add(R.id.addChatRoomFragment)
//                }
//                refreshMenuItem(menu, removeItemList)
//            }
//
//            R.id.chatListFragment -> {
//                removeItemList.apply {
//                    add(R.id.youtubeSearchFragment)
//                    add(R.id.addFriendFragment)
//                }
//                refreshMenuItem(menu, removeItemList)
//            }
//
//            R.id.youtubeFragment -> {
//                removeItemList.apply {
//                    add(R.id.searchFragment)
//                    add(R.id.addFriendFragment)
//                    add(R.id.addChatRoomFragment)
//                }
//                refreshMenuItem(menu, removeItemList)
//            }
//
//            R.id.youtubeSearchFragment -> {
//                homeToolbarTB.title = null
//                searchET.visibility = View.VISIBLE
//            }
//        }
//    }
//
//    private fun refreshMenuItem(menu: Menu, removeItemList: ArrayList<Int>) {
//        val menuInflater = requireActivity().menuInflater
//        menuInflater.inflate(R.menu.app_bar_menu, menu)
//
//        removeItemList.forEach { item ->
//            menu.removeItem(item)
//        }
//        Log.i(TAG, "onDestinationChanged: refresh $menu")
//    }
}