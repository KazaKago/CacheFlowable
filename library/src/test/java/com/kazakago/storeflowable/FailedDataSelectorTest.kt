package com.kazakago.storeflowable

import com.kazakago.storeflowable.cache.CacheDataManager
import com.kazakago.storeflowable.datastate.DataState
import com.kazakago.storeflowable.datastate.DataStateManager
import com.kazakago.storeflowable.logic.DataSelector
import com.kazakago.storeflowable.logic.RequestType
import com.kazakago.storeflowable.origin.InternalFetchingResult
import com.kazakago.storeflowable.origin.OriginDataManager
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.amshove.kluent.shouldBeEqualTo
import org.amshove.kluent.shouldBeInstanceOf
import org.junit.Assert.fail
import org.junit.Test
import java.net.UnknownHostException

@ExperimentalCoroutinesApi
class FailedDataSelectorTest {

    companion object {
        const val FORCE_REFRESH = false
        const val CLEAR_CACHE_BEFORE_FETCHING = true
        const val CLEAR_CACHE_WHEN_FETCH_FAILS = true
        const val CONTINUE_WHEN_ERROR = true
        const val AWAIT_FETCHING = true
        internal val REQUEST_TYPE = RequestType.Refresh
    }

    private enum class TestData(val needRefresh: Boolean) {
        ValidData(false),
        InvalidData(true),
        FetchedData(false),
    }

    private val dataSelector = DataSelector(
        key = "key",
        dataStateManager = object : DataStateManager<String> {
            override fun load(key: String): DataState {
                return dataState
            }

            override fun save(key: String, state: DataState) {
                dataState = state
            }
        },
        cacheDataManager = object : CacheDataManager<TestData> {
            override suspend fun load(): TestData? {
                return dataCache
            }

            override suspend fun save(newData: TestData?) {
                dataCache = newData
            }

            override suspend fun saveAppending(cachedData: TestData?, newData: TestData) {
                fail()
            }

            override suspend fun savePrepending(cachedData: TestData?, newData: TestData) {
                fail()
            }
        },
        originDataManager = object : OriginDataManager<TestData> {
            override suspend fun fetch(): InternalFetchingResult<TestData> {
                throw UnknownHostException()
            }

            override suspend fun fetchAppending(cachedData: TestData?): InternalFetchingResult<TestData> {
                fail()
                throw NotImplementedError()
            }

            override suspend fun fetchPrepending(cachedData: TestData?): InternalFetchingResult<TestData> {
                fail()
                throw NotImplementedError()
            }
        },
        needRefresh = { it.needRefresh }
    )

    private var dataState: DataState = DataState.Fixed(mockk(), mockk())
    private var dataCache: TestData? = null

    @Test
    fun doStateAction_Fixed_NoCache() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Fixed_ValidCache() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.ValidData
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun doStateAction_Fixed_InvalidCache() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.InvalidData
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Fixed_NoCache_ForceRefresh() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = null
        dataSelector.doStateAction(forceRefresh = true, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Fixed_ValidCache_ForceRefresh() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.ValidData
        dataSelector.doStateAction(forceRefresh = true, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Fixed_InvalidCache_ForceRefresh() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.InvalidData
        dataSelector.doStateAction(forceRefresh = true, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Fixed_NoCache_NonClearCacheBeforeFetching_NonClearCacheWhenFetchFails() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Fixed_ValidCache_NonClearCacheBeforeFetching_NonClearCacheWhenFetchFails() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.ValidData
        dataSelector.doStateAction(FORCE_REFRESH, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Fixed::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun doStateAction_Fixed_InvalidCache_NonClearCacheBeforeFetching_NonClearCacheWhenFetchFails() = runBlockingTest {
        dataState = DataState.Fixed(mockk(), mockk())
        dataCache = TestData.InvalidData
        dataSelector.doStateAction(FORCE_REFRESH, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo TestData.InvalidData
    }

    @Test
    fun doStateAction_Loading_NoCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Loading_ValidCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = TestData.ValidData
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun doStateAction_Loading_InvalidCache() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = TestData.InvalidData
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.InvalidData
    }

    @Test
    fun doStateAction_Loading_NoCache_ForceRefresh() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = null
        dataSelector.doStateAction(forceRefresh = true, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Loading_ValidCache_ForceRefresh() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = TestData.ValidData
        dataSelector.doStateAction(forceRefresh = true, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun doStateAction_Loading_InvalidCache_ForceRefresh() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = TestData.InvalidData
        dataSelector.doStateAction(forceRefresh = true, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.InvalidData
    }

    @Test
    fun doStateAction_Loading_NoCache_NonClearCacheBeforeFetching_NonClearCacheWhenFetchFails() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Loading_ValidCache_NonClearCacheBeforeFetching_NonClearCacheWhenFetchFails() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = TestData.ValidData
        dataSelector.doStateAction(FORCE_REFRESH, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun doStateAction_Loading_InvalidCache_NonClearCacheBeforeFetching_NonClearCacheWhenFetchFails() = runBlockingTest {
        dataState = DataState.Loading()
        dataCache = TestData.InvalidData
        dataSelector.doStateAction(FORCE_REFRESH, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Loading::class
        dataCache shouldBeEqualTo TestData.InvalidData
    }

    @Test
    fun doStateAction_Error_NoCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Error_ValidCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.ValidData
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Error_InvalidCache() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.InvalidData
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Error_NoCache_ForceRefresh() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = null
        dataSelector.doStateAction(forceRefresh = true, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Error_ValidCache_ForceRefresh() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.ValidData
        dataSelector.doStateAction(forceRefresh = true, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Error_InvalidCache_ForceRefresh() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.InvalidData
        dataSelector.doStateAction(forceRefresh = true, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Error_NoCache_NonContinueWhenError() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, continueWhenError = false, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Error_ValidCache_NonContinueWhenError() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.ValidData
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, continueWhenError = false, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun doStateAction_Error_InvalidCache_NonContinueWhenError() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.InvalidData
        dataSelector.doStateAction(FORCE_REFRESH, CLEAR_CACHE_BEFORE_FETCHING, CLEAR_CACHE_WHEN_FETCH_FAILS, continueWhenError = false, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo TestData.InvalidData
    }

    @Test
    fun doStateAction_Error_NoCache_NonClearCacheBeforeFetching_NonClearCacheWhenFetchFails() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = null
        dataSelector.doStateAction(FORCE_REFRESH, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo null
    }

    @Test
    fun doStateAction_Error_ValidCache_NonClearCacheBeforeFetching_NonClearCacheWhenFetchFails() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.ValidData
        dataSelector.doStateAction(FORCE_REFRESH, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo TestData.ValidData
    }

    @Test
    fun doStateAction_Error_InvalidCache_NonClearCacheBeforeFetching_NonClearCacheWhenFetchFails() = runBlockingTest {
        dataState = DataState.Error(mockk())
        dataCache = TestData.InvalidData
        dataSelector.doStateAction(FORCE_REFRESH, clearCacheBeforeFetching = false, clearCacheWhenFetchFails = false, CONTINUE_WHEN_ERROR, AWAIT_FETCHING, REQUEST_TYPE)
        dataState shouldBeInstanceOf DataState.Error::class
        dataCache shouldBeEqualTo TestData.InvalidData
    }
}
