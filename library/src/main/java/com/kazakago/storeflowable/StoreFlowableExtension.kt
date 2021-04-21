package com.kazakago.storeflowable

/**
 * Create [StoreFlowable] class from [StoreFlowableCallback].
 */
fun <KEY, DATA> StoreFlowableCallback<KEY, DATA>.create(): StoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(this)
}

@Deprecated("Use StoreFlowableCallback.create")
fun <KEY, DATA> StoreFlowableResponder<KEY, DATA>.create(): StoreFlowable<KEY, DATA> {
    return StoreFlowableImpl(toStoreFlowableCallback())
}

private fun <KEY, DATA> StoreFlowableResponder<KEY, DATA>.toStoreFlowableCallback(): StoreFlowableCallback<KEY, DATA> {
    return object : StoreFlowableCallback<KEY, DATA> {

        override val key = this@toStoreFlowableCallback.key

        override val flowableDataStateManager = this@toStoreFlowableCallback.flowableDataStateManager

        override suspend fun loadDataFromCache(): DATA? {
            return this@toStoreFlowableCallback.loadData()
        }

        override suspend fun saveDataToCache(newData: DATA?) {
            this@toStoreFlowableCallback.saveData(newData)
        }

        override suspend fun fetchDataFromOrigin(): FetchingResult<DATA> {
            return FetchingResult(this@toStoreFlowableCallback.fetchOrigin())
        }

        override suspend fun needRefresh(cachedData: DATA): Boolean {
            return this@toStoreFlowableCallback.needRefresh(cachedData)
        }
    }
}

/**
 * Deprecated, use [StoreFlowable.getData].
 *
 * @see StoreFlowable.getData
 */
@Deprecated("Use getData()", ReplaceWith("getData(type)"))
suspend inline fun <KEY, DATA> StoreFlowable<KEY, DATA>.getOrNull(type: AsDataType = AsDataType.Mix): DATA? {
    return runCatching { get(type) }.getOrNull()
}
