cdd-kotlin
==========

[![License](https://img.shields.io/badge/license-Apache--2.0%20OR%20MIT-blue.svg)](https://opensource.org/licenses/Apache-2.0)

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

## Related work
    
  | Language             | Compiler                                               |
  | -------------------- | ------------------------------------------------------ |
  | Python               | [cdd-python](https://github.com/offscale/cdd-python)   |
  | C                    | [cdd-c](https://github.com/SamuelMarks/cdd-c)          |
  | Java (Android)       | [cdd-java](https://github.com/offscale/cdd-java)       |
  | Swift (iOS)          | [cdd-swift](https://github.com/offscale/cdd-swift-ios) |
  | TypeScript (Angular) | [cdd-ts-ng](https://github.com/offscale/cdd-ts-ng)     |
  | Rust                 | [cdd-rust](https://github.com/offscale/cdd-rust)       |

<hr/>

## License

Licensed under any either of:

- Apache License, Version 2.0 ([LICENSE-APACHE](LICENSE-APACHE) or <https://www.apache.org/licenses/LICENSE-2.0>)
- MIT license ([LICENSE-MIT](LICENSE-MIT) or <https://opensource.org/licenses/MIT>)

at your option.

### Contribution

Unless you explicitly state otherwise, any contribution intentionally submitted for inclusion in the work by you, as defined in the Apache-2.0 license, shall be licensed as above, without any additional terms or conditions.
