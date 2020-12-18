package codemwnci

import javax.ws.rs.*
import javax.ws.rs.core.MediaType
import kotliquery.*
import javax.inject.Inject
import javax.enterprise.inject.Default
import javax.sql.DataSource

@Path("/hello")
class HelloService {
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    fun hello() = "hello21"
}

@Path("/todos")
class TodoService {
    class Todo(var id: Long = 0L, val txt: String = "", val completed: Boolean = false)
    /*
    Původní náhrada db za pole v paměti
    val idSeq: AtomicLong = AtomicLong()
    fun createTodo(txt: String): Todo = Todo(idSeq.incrementAndGet(), txt)
    val todos: ArrayList<Todo> = ArrayList()
                          */
    @Inject
    @field: Default
    lateinit var ds: DataSource

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun getAll() = using(sessionOf(ds)) { session ->
        session.run(queryOf("SELECT id, txt, completed FROM todo").map {
                row -> Todo(row.long("id"), row.string("txt"), row.boolean("completed"))
        }.asList)
    }

    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getOne(@PathParam("id") id: Long) = using(sessionOf(ds)) { session ->
        session.run(queryOf("SELECT id, txt, completed FROM todo WHERE id=?", id).map {
                row -> Todo(row.long("id"), row.string("txt"), row.boolean("completed"))
        }.asSingle)
    }

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    fun addOne(txt: String): Todo? {
        val newId: Long? = using(sessionOf(ds, true)) { session ->
            session.run(queryOf("INSERT INTO todo (txt) VALUES (?)", txt).asUpdateAndReturnGeneratedKey )
        }
        return if (newId != null) getOne(newId) else null
    }

    @DELETE
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun deleteOne(@PathParam("id") id: Long) =
        using(sessionOf(ds)) { session -> session.run(queryOf("DELETE FROM todo WHERE id=?", id).asUpdate)} == 1

    @PUT
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    fun updateOne(@PathParam("id") id: Long, todo: Todo): Todo? {
        using(sessionOf(ds)) { session ->
            session.run(queryOf("UPDATE todo SET txt=?, completed=? WHERE id=?", todo.txt, todo.completed, id).asUpdate) }
        return getOne(id)
    }

}

