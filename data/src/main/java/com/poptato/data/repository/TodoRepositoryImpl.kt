package com.poptato.data.repository

import com.poptato.data.base.BaseRepository
import com.poptato.data.mapper.UnitResponseMapper
import com.poptato.data.service.TodoService
import com.poptato.domain.model.request.todo.ModifyTodoRequestModel
import com.poptato.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TodoRepositoryImpl @Inject constructor(
    private val todoService: TodoService
) : TodoRepository, BaseRepository() {
    override suspend fun deleteTodo(todoId: Long): Flow<Result<Unit>> {
        return apiLaunch(apiCall = { todoService.deleteTodo(todoId) }, UnitResponseMapper)
    }

    override suspend fun modifyTodo(request: ModifyTodoRequestModel): Flow<Result<Unit>> {
        return apiLaunch(apiCall = { todoService.modifyTodo(todoId = request.todoId, request = request.content) }, UnitResponseMapper)
    }
}