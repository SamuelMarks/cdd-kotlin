cdd-kotlin
==========

OpenAPI ↔ Kotlin: Compiler Driven Development for Kotlin. 

## Usage

    Usage: cdd-kotlin [<options>] <command> [<args>]...
    
    Options:
    --version   Show the version and exit
    -h, --help  Show this message and exit
    
    Commands:
    sync
    emit

### `sync`

    Usage: cdd-kotlin sync [<options>]
    
    Options:
    --truth=<path>  the correct starting point, e.g., the ktor client
    --generate=(ktorClient|ktorClientTests|viewModel)
    what to generate, * to generate everything (default)
    -h, --help      Show this message and exit

### `emit`

    Usage: cdd-kotlin emit [<options>]
    
    Options:
    --replace-existing=true|false  whether to override any existing file
    --filename=<path>              path to OpenAPI file, default: ./openapi.json
    -h, --help                     Show this message and exit

<hr/>

## License

Licensed under any either of:

- Apache License, Version 2.0 ([LICENSE-APACHE](LICENSE-APACHE) or <https://www.apache.org/licenses/LICENSE-2.0>)
- MIT license ([LICENSE-MIT](LICENSE-MIT) or <https://opensource.org/licenses/MIT>)

at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted for inclusion in the work by you, as defined in the Apache-2.0 license, shall be licensed as above, without any additional terms or conditions.
