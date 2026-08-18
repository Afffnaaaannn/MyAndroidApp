package com.example.mytodoapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.mytodoapplication.ui.theme.MyTodoApplicationTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch


class MainActivity : ComponentActivity() {

    private lateinit var database: TodoDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        database = TodoDatabase.getDatabase(this)

        setContent {
            MyTodoApplicationTheme {
                TodoApp(
                    todoDao = database.todoDao()
                )
            }
        }
    }
}


@Composable
fun TodoApp(
    todoDao: TodoDao
) {

    val todos by todoDao
        .getAllTodos()
        .collectAsState(initial = emptyList())

    val scope = rememberCoroutineScope()

    var todoText by remember {
        mutableStateOf("")
    }

    var editingTodo by remember {
        mutableStateOf<Todo?>(null)
    }


    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Text(
            text = if (editingTodo == null) {
                "My Todo List"
            } else {
                "Edit Todo"
            },
            style = MaterialTheme.typography.headlineMedium
        )


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        OutlinedTextField(
            value = todoText,
            onValueChange = {
                todoText = it
            },
            modifier = Modifier.fillMaxWidth(),
            label = {
                Text("Enter a task")
            },
            singleLine = true
        )


        Spacer(
            modifier = Modifier.height(8.dp)
        )


        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {

            Button(
                modifier = Modifier.weight(1f),
                onClick = {

                    if (todoText.isNotBlank()) {

                        val textToSave = todoText.trim()

                        scope.launch(Dispatchers.IO) {

                            if (editingTodo == null) {

                                val newTodo = Todo(
                                    title = textToSave
                                )

                                todoDao.insertTodo(newTodo)

                            } else {

                                val updatedTodo = editingTodo!!.copy(
                                    title = textToSave
                                )

                                todoDao.updateTodo(updatedTodo)
                            }
                        }

                        todoText = ""
                        editingTodo = null
                    }
                }
            ) {

                Text(
                    text = if (editingTodo == null) {
                        "Add to list"
                    } else {
                        "Save Changes"
                    }
                )
            }


            if (editingTodo != null) {

                TextButton(
                    onClick = {
                        todoText = ""
                        editingTodo = null
                    }
                ) {

                    Text("Cancel")
                }
            }
        }


        Spacer(
            modifier = Modifier.height(16.dp)
        )


        if (todos.isEmpty()) {

            Text(
                text = "No tasks yet.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onBackground
            )

        } else {

            LazyColumn(
                modifier = Modifier.fillMaxWidth()
            ) {

                items(
                    items = todos,
                    key = { todo ->
                        todo.id
                    }
                ) { todo ->

                    TodoCard(
                        todo = todo,

                        onComplete = {

                            scope.launch(Dispatchers.IO) {

                                todoDao.updateTodo(
                                    todo.copy(
                                        isCompleted = !todo.isCompleted
                                    )
                                )
                            }
                        },

                        onDelete = {

                            scope.launch(Dispatchers.IO) {

                                todoDao.deleteTodo(todo)
                            }

                            if (editingTodo?.id == todo.id) {
                                todoText = ""
                                editingTodo = null
                            }
                        },

                        onEdit = {

                            editingTodo = todo
                            todoText = todo.title
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun TodoCard(
    todo: Todo,
    onComplete: () -> Unit,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 8.dp,
                    vertical = 10.dp
                ),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Checkbox(
                checked = todo.isCompleted,
                onCheckedChange = {
                    onComplete()
                }
            )


            Text(
                text = todo.title,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textDecoration = if (todo.isCompleted) {
                    TextDecoration.LineThrough
                } else {
                    TextDecoration.None
                }
            )


            TextButton(
                onClick = {
                    onEdit()
                }
            ) {
                Text("Edit")
            }


            TextButton(
                onClick = {
                    onDelete()
                }
            ) {
                Text("Delete")
            }
        }
    }
}