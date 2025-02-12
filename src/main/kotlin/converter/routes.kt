route("/pets") {
    /**
     * List all pets
     * @param limit (integer, required=false) How many items to return at one time (max 100)
     * @response 200 -> A paged array of pets (#/components/schemas/Pets)
     * @response default -> unexpected error (#/components/schemas/Error)
     */
    get {
        // Implementation goes here
    }

    /**
     * Create a pet
     * @body application/json -> #/components/schemas/Pet
     * @response 201 -> Null response (unknown)
     * @response default -> unexpected error (#/components/schemas/Error)
     */
    post {
        // Implementation goes here
    }

}

route("/pets/{petId}") {
    /**
     * Info for a specific pet
     * @param petId (string, required=true) The id of the pet to retrieve
     * @response 200 -> Expected response to a valid request (#/components/schemas/Pet)
     * @response default -> unexpected error (#/components/schemas/Error)
     */
    get {
        // Implementation goes here
    }

}

