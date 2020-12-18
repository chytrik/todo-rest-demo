package codemwnci

import java.util.concurrent.atomic.AtomicLong
import javax.ws.rs.*
import javax.ws.rs.core.MediaType

@Path("/hello")
class HelloService {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello() = "hello21"
}

@Path("/todos")
class TodoService {
    class Todo(var id: Long = 0L, val txt: String = "", val completed: Boolean = false)
    val idSeq: AtomicLong = AtomicLong()
    fun createTodo(txt: String): Todo = Todo(idSeq.incrementAndGet(), txt)
    val todos: ArrayList<Todo> = ArrayList()

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll() = todos

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getOne(@PathParam("id") id: Long) = if (!todos.isEmpty()) todos.first { it.id == id } else null

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun addOne(txt: String): Todo {
        val todo = createTodo(txt)
        todos.add(todo)
        return todo
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteOne(@PathParam("id") id: Long) = todos.remove( todos.first { it.id == id } )

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun updateOne(@PathParam("id") id: Long, todo: Todo): Todo? {
        deleteOne(id)
        todos.add(todo)
        return todo
    }

}

