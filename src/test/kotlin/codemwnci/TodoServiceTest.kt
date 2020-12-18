package codemwnci

import io.restassured.RestAssured.given
import org.hamcrest.CoreMatchers.containsString
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.BeforeEach
import kotliquery.*
import javax.inject.Inject
import javax.enterprise.inject.Default
import javax.sql.DataSource
import org.flywaydb.core.Flyway
import io.quarkus.test.common.QuarkusTestResource
import io.quarkus.test.h2.H2DatabaseTestResource
import io.quarkus.test.junit.QuarkusTest
import org.hamcrest.CoreMatchers.not


@QuarkusTestResource(H2DatabaseTestResource::class)
open class TodoServiceTest {
    @Inject
    lateinit var flyway: Flyway;
    // Create the DB structure before each test
    @BeforeEach
    fun dropAndRecreate() {
        flyway.clean()
        flyway.migrate()
    }

    @Inject
    @field: Default
    lateinit var ds: DataSource
    fun addTestTodos(vararg todos: String) {
        using(sessionOf(ds)) { session ->
            for(todo in todos) {
                session.run(queryOf("INSERT INTO todo (txt) VALUES (?);", todo).asUpdate)
            }
        }
    }

    @Test
    fun testEmptyTodoList() {

        given().`when`().get("/todos").then().statusCode(200).body(containsString("[]"))
    }

    @Test
    fun testAdd() {
        // TEST THE POST RETURNS A SINGLE JSON
        given().body("test todo").`when`()
            .post("/todos").then().statusCode(200)
            .body(containsString("test todo"), containsString("""{"id":1,"txt":"test todo","completed":false}"""))
        // TEST GET NOW RETURNS AN ARRAY WITH A SINGLE ITEM
        given().`when`().get("/todos").then().statusCode(200)
            .body(containsString("""[{"id":1,"txt":"test todo","completed":false}]"""))
    }

    @Test
    fun testGetOne() {
        addTestTodos("number1", "test 2")
        given().`when`().get("/todos/1").then().statusCode(200)
            .body(containsString("""{"id":1,"txt":"number1","completed":false}"""), not(containsString("test 2")))
    }

    @Test
    fun testDelete() {
        addTestTodos("number1", "test 2")
        // confirm the item is there
        given().`when`().get("/todos").then().statusCode(200).body(containsString("number1"))
// delete the todo, and confirm true is returned
        given().`when`().delete("/todos/1").then().statusCode(200).body(containsString("true"))
// confirm the item is no longer there, and is therefore deleted
        given().`when`().get("/todos").then().statusCode(200).body(not(containsString("number1")))
    }

    @Test
    fun testUpdate() {
        addTestTodos("number1", "test 2")
        // confirm the item is there
        given().`when`().get("/todos").then().statusCode(200).body(containsString("number1"))
        // confirm it returned the updated details
        given().contentType("application/json").body("""{"id":1,"txt":"1_isupdated_to_number2","completed":true}""")
            .`when`().put("/todos/1").then().statusCode(200)
            .body(not(containsString("number1")), containsString("1_isupdated_to_number2"), containsString("true"))
        // confirm it returns from get as expected
        given().`when`().get("/todos").then().statusCode(200).body(containsString("1_isupdated_to_number2"))
        // make sure we cant change the ID and that it returned the updated details
        given().contentType("application/json").body("""{"id":1111,"txt":"1_isupdated_to_number3","completed":true}""")
            .`when`().put("/todos/1").then().statusCode(200)
            .body(not(containsString("number1")), not(containsString("1111")))
        // confirm it returns fron getAll as expected
        given().`when`().get("/todos").then().statusCode(200)
            .body(containsString("""{"id":1,"txt":"1_isupdated_to_number3","completed":true}"""))
    }
}
