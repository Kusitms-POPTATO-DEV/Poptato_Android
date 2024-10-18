package com.poptato.domain.repository

import com.poptato.domain.model.request.todo.ModifyTodoRequestModel
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    suspend fun deleteTodo(todoId: Long): Flow<Result<Unit>>
    suspend fun modifyTodo(request: ModifyTodoRequestModel): Flow<Result<Unit>>
}