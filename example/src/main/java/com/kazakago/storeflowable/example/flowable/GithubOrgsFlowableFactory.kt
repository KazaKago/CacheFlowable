package com.kazakago.storeflowable.example.flowable

import com.kazakago.storeflowable.FetchingResult
import com.kazakago.storeflowable.FlowableDataStateManager
import com.kazakago.storeflowable.example.api.GithubApi
import com.kazakago.storeflowable.example.cache.GithubCache
import com.kazakago.storeflowable.example.cache.GithubOrgsStateManager
import com.kazakago.storeflowable.example.model.GithubOrg
import com.kazakago.storeflowable.pagination.PaginatingStoreFlowableFactory
import java.time.Duration
import java.time.LocalDateTime

class GithubOrgsFlowableFactory : PaginatingStoreFlowableFactory<Unit, List<GithubOrg>> {

    companion object {
        private val EXPIRED_DURATION = Duration.ofMinutes(1)
        private const val PER_PAGE = 20
    }

    private val githubApi = GithubApi()
    private val githubCache = GithubCache

    override val key: Unit = Unit

    override val flowableDataStateManager: FlowableDataStateManager<Unit> = GithubOrgsStateManager

    override suspend fun loadDataFromCache(): List<GithubOrg>? {
        return githubCache.orgsCache
    }

    override suspend fun saveDataToCache(newData: List<GithubOrg>?) {
        githubCache.orgsCache = newData
        githubCache.orgsCacheCreatedAt = LocalDateTime.now()
    }

    override suspend fun saveAdditionalDataToCache(cachedData: List<GithubOrg>?, newData: List<GithubOrg>) {
        githubCache.orgsCache = (cachedData ?: emptyList()) + newData
    }

    override suspend fun fetchDataFromOrigin(): FetchingResult<List<GithubOrg>> {
        val data = githubApi.getOrgs(null, PER_PAGE)
        return FetchingResult(data = data, noMoreAdditionalData = data.isEmpty())
    }

    override suspend fun fetchAdditionalDataFromOrigin(cachedData: List<GithubOrg>?): FetchingResult<List<GithubOrg>> {
        val since = cachedData?.lastOrNull()?.id
        val data = githubApi.getOrgs(since, PER_PAGE)
        return FetchingResult(data = data, noMoreAdditionalData = data.isEmpty())
    }

    override suspend fun needRefresh(cachedData: List<GithubOrg>): Boolean {
        val createdAt = githubCache.orgsCacheCreatedAt
        return if (createdAt != null) {
            val expiredAt = createdAt + EXPIRED_DURATION
            expiredAt < LocalDateTime.now()
        } else {
            true
        }
    }
}
