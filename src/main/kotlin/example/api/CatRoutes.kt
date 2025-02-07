package io.offscale.example.api

import io.offscale.example.models.Cat
import io.offscale.example.repository.CatRepository

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*

fun Route.catRoutes(repository: CatRepository) {
    route("/cats") {

        /**
         * Returns a list of all cats.
         *
         * @response 200 A list of cats.
         * @produces application/json
         */
        get {
            call.respond(repository.getAllCats())
        }

        /**
         * Returns a cat with a specific name.
         *
         * @param name The name of the cat to retrieve.
         * @response 200 A cat object.
         * @response 404  message: "Cat not found"
         * @produces application/json
         */
        get("/{name}") {
            val name = call.parameters["name"]
            val cat = repository.getCatByName(name ?: "")
            if (cat != null) {
                call.respond(cat)
            } else {
                call.respond(mapOf("message" to "Cat not found"))
            }
        }

        /**
         * Adds a new cat to the repository.
         *
         * @body A JSON object representing the cat.
         * @response 201  message: "Cat added successfully!"
         * @produces application/json
         */
        post {
            val cat = call.receive<Cat>()
            repository.addCat(cat)
            call.respond(mapOf("message" to "Cat added successfully!"))
        }

        /**
         * Updates an existing cat's information.
         *
         * @param name The name of the cat to update.
         * @body A JSON object with updated cat details.
         * @response 200 message: "Cat updated successfully!"
         * @response 404 message: "Cat not found"
         * @produces application/json
         */
        put("/{name}") {
            val name = call.parameters["name"]
            val updatedCat = call.receive<Cat>()
            if (repository.updateCat(name ?: "", updatedCat)) {
                call.respond(mapOf("message" to "Cat updated successfully!"))
            } else {
                call.respond(mapOf("message" to "Cat not found"))
            }
        }

        /**
         * Partially updates a cat's information.
         *
         * @param name The name of the cat to update.
         * @body A JSON object with partial cat details.
         * @response 200 message: "Cat updated successfully!
         * @response 404  message: "Cat not found"
         * @produces application/json
         */
        patch("/{name}") {
            val name = call.parameters["name"]
            val partialUpdate = call.receive<Map<String, Any>>()
            if (repository.partialUpdateCat(name ?: "", partialUpdate)) {
                call.respond(mapOf("message" to "Cat updated successfully!"))
            } else {
                call.respond(mapOf("message" to "Cat not found"))
            }
        }

        /**
         * Deletes a cat from the repository.
         *
         * @param name The name of the cat to delete.
         * @response 200  message: "Cat deleted successfully!"
         * @response 404  message: "Cat not found"
         * @produces application/json
         */
        delete("/{name}") {
            val name = call.parameters["name"]
            if (repository.deleteCat(name ?: "")) {
                call.respond(mapOf("message" to "Cat deleted successfully!"))
            } else {
                call.respond(mapOf("message" to "Cat not found"))
            }
        }

        /**
         * Returns metadata about the `/cats` endpoint.
         *
         * @response 200 No content (headers only).
         */
        head {
            call.response.headers.append("X-Total-Cats", repository.countCats().toString())
            call.respond("")
        }

        /**
         * Returns allowed HTTP methods for the `/cats` endpoint.
         *
         * @response 200 A list of allowed methods.
         * @produces application/json
         */
        options {
            call.response.headers.append("Allow", "GET, POST, PUT, PATCH, DELETE, HEAD, OPTIONS")
            call.respond(mapOf("allowedMethods" to listOf("GET", "POST", "PUT", "PATCH", "DELETE", "HEAD", "OPTIONS")))
        }
    }
}
