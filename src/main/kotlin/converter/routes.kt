package com.example.routes

import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.server.routing.*
import io.ktor.http.*

fun Application.configureRouting() {
    routing {        route("/pets") {
            /**
             * List all pets
             *
             * @operationId listPets
             *
             * @tags pets
             * @param limit (integer, required=false, in=query) How many items to return at one time (max 100)
             * @response 200 -> A paged array of pets (#/components/schemas/Pets)
             * @response default -> unexpected error (#/components/schemas/Error)
             */
            get {
                call.respondText("Handling get at /pets", status = HttpStatusCode.OK)
            }

            /**
             * Create a pet
             *
             * @operationId createPets
             *
             * @tags pets
             * @body application/json -> #/components/schemas/Pet
             * @response 201 -> Null response (unknown)
             * @response default -> unexpected error (#/components/schemas/Error)
             */
            post {
                call.respondText("Handling post at /pets", status = HttpStatusCode.OK)
            }

        }
        route("/pets/{petId}") {
            /**
             * Info for a specific pet
             *
             * @operationId showPetById
             *
             * @tags pets
             * @param petId (string, required=true, in=path) The id of the pet to retrieve
             * @response 200 -> Expected response to a valid request (#/components/schemas/Pet)
             * @response default -> unexpected error (#/components/schemas/Error)
             */
            get {
                call.respondText("Handling get at /pets/{petId}", status = HttpStatusCode.OK)
            }

        }
    }
}
