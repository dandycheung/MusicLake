package com.cyl.musiclake.ui.music.search

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.cyl.musiclake.R
import com.cyl.musiclake.bean.HotSearchBean
import com.cyl.musiclake.bean.Music
import com.cyl.musiclake.bean.SearchHistoryBean
import com.cyl.musiclake.common.Constants
import com.cyl.musiclake.player.PlayManager
import com.cyl.musiclake.ui.base.BaseLazyFragment
import com.cyl.musiclake.ui.music.dialog.BottomDialogFragment
import com.cyl.musiclake.ui.music.local.adapter.SongAdapter
import com.cyl.musiclake.utils.LogUtil
import kotlinx.android.synthetic.main.fragment_recyclerview_notoolbar.*

/**
 * 功能：本地歌曲列表
 * 作者：yonglong on 2016/8/10 20:49
 * 邮箱：643872807@qq.com
 * 版本：2.5
 */
class SearchSongsFragment : BaseLazyFragment<SearchPresenter>(), SearchContract.View {

    private var mAdapter: SongAdapter? = null
    /**
     * 歌曲列表
     */
    private val musicList = mutableListOf<Music>()
    /**
     * 分页偏移量
     */
    private var mCurrentCounter = 0
    private val limit = 15
    private var mOffset = 0

    private var searchInfo: String = ""
    private var TAG = "SearchSongsFragment"
    private var type = SearchEngine.Filter.ANY


    //上拉加载更多监听事件
    val listener = BaseQuickAdapter.RequestLoadMoreListener {
        recyclerView.postDelayed({
            LogUtil.d(TAG, "mCurrentCounter=$mCurrentCounter")
            if (mCurrentCounter == 0) {
                //数据全部加载完毕
                mAdapter?.loadMoreEnd()
            } else {
                //成功获取更多数据
                mPresenter?.search(searchInfo, type, limit, mOffset)
            }
        }, 1000)
    }

    companion object {
        fun newInstance(searchInfo: String?, type: SearchEngine.Filter): SearchSongsFragment {
            val args = Bundle()
            val fragment = SearchSongsFragment()
            args.putString("searchInfo", searchInfo)
            args.putString("type", type.toString())
            fragment.arguments = args
            return fragment
        }
    }

    override fun getLayoutId(): Int {
        return R.layout.fragment_recyclerview_notoolbar
    }

    override fun initViews() {
        searchInfo = arguments?.getString("searchInfo") ?: ""
        type = when (arguments?.getString("type")) {
            "QQ" -> SearchEngine.Filter.QQ
            "BAIDU" -> SearchEngine.Filter.BAIDU
            "XIAMI" -> SearchEngine.Filter.XIAMI
            "NETEASE" -> SearchEngine.Filter.NETEASE
            else -> SearchEngine.Filter.ANY
        }
        LogUtil.d(TAG, "初始化")
        musicList.clear()
    }


    override fun initInjector() {
        mFragmentComponent.inject(this)
    }

    override fun listener() {
    }

    override fun onLazyLoad() {
        mPresenter?.search(searchInfo, type, limit, mOffset)
    }


    /**
     * 更新歌曲列表
     */
    fun updateMusicList(songList: MutableList<Music>) {
        musicList.addAll(songList)

        if (mAdapter == null) {
            mAdapter = SongAdapter(musicList)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = mAdapter
            mAdapter?.bindToRecyclerView(recyclerView)
            mAdapter?.setOnLoadMoreListener(listener, recyclerView)

            mAdapter?.setOnItemClickListener { adapter, view, position ->
                if (view.id != R.id.iv_more) {
                    PlayManager.play(position, musicList, Constants.PLAYLIST_LOCAL_ID)
                    mAdapter?.notifyDataSetChanged()
                }
            }
            mAdapter?.setOnItemChildClickListener { adapter, _, position ->
                val music = adapter.getItem(position) as Music?
                BottomDialogFragment.newInstance(music, Constants.PLAYLIST_LOCAL_ID)
                        .apply {
                            removeSuccessListener = {
                                this@SearchSongsFragment.mAdapter?.notifyItemRemoved(position)
                            }
                        }.show(mFragmentComponent.activity as AppCompatActivity)
            }
        } else {
            mAdapter?.setNewData(musicList)
        }
        hideLoading()
    }

    override fun showSearchResult(list: MutableList<Music>) {
        if (list.size != 0) {
            mOffset++
        } else {
            mAdapter?.loadMoreComplete()
            mAdapter?.setEnableLoadMore(false)
        }
        //更新歌曲列表
        updateMusicList(list)

        mCurrentCounter = list.size
        if (musicList.size == 0) {
            mAdapter?.loadMoreComplete()
            mAdapter?.setEnableLoadMore(false)
            showEmptyState()
        }
    }

    override fun showHotSearchInfo(list: MutableList<HotSearchBean>) {
    }


    override fun showSearchHistory(list: MutableList<SearchHistoryBean>) {
    }

    override fun showLoading() {
        super.showLoading()
    }

    override fun hideLoading() {
        super.hideLoading()
    }

}