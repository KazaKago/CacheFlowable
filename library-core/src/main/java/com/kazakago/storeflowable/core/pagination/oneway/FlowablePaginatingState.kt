package com.kazakago.storeflowable.core.pagination.oneway

import kotlinx.coroutines.flow.Flow

/**
 * Type alias of `Flow<PaginatingState<T>>`.
 */
typealias FlowablePaginatingState<T> = Flow<PaginatingState<T>>
