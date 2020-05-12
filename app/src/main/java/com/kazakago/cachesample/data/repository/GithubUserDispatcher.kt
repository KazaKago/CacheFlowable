package com.kazakago.cachesample.data.repository

import com.kazakago.cachesample.data.api.GithubApi
import com.kazakago.cachesample.data.api.GithubUserResponseMapper
import com.kazakago.cachesample.data.cache.GithubCache
import com.kazakago.cachesample.data.cache.GithubUserEntity
import com.kazakago.cachesample.data.cache.state.DataState
import com.kazakago.cachesample.data.cache.state.getOrCreate
import com.kazakago.cachesample.data.repository.dispatcher.CacheStreamDispatcher
import kotlinx.coroutines.flow.StateFlow
import java.util.*

class GithubUserDispatcher(
    private val githubApi: GithubApi,
    private val githubUserResponseMapper: GithubUserResponseMapper,
    private val githubCache: GithubCache,
    private val userName: String
) : CacheStreamDispatcher<GithubUserEntity>() {

    override fun loadDataStateFlow(): StateFlow<DataState> {
        return githubCache.userState.getOrCreate(userName)
    }

    override suspend fun saveDataState(state: DataState) {
        githubCache.userState.getOrCreate(userName).value = state
    }

    override suspend fun loadEntity(): GithubUserEntity? {
        return githubCache.userCache[userName]
    }

    override suspend fun saveEntity(entity: GithubUserEntity?) {
        githubCache.userCache[userName] = entity
        githubCache.userCreateAdCache[userName] = Calendar.getInstance()
    }

    override suspend fun fetchOrigin(): GithubUserEntity {
        val response = githubApi.getUser(userName)
        return githubUserResponseMapper.map(response)
    }

    override suspend fun needRefresh(entity: GithubUserEntity): Boolean {
        val expiredTime = githubCache.userCreateAdCache.getOrCreate(userName).apply {
            add(Calendar.MINUTE, 3)
        }
        return expiredTime < Calendar.getInstance()
    }

}