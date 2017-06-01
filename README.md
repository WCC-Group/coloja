# coloja
Helper library to improve code coverage and mutation testing when using Lombok, jacoco and pitest.


## Use case

Generated code through lombok doesn't get full coverage from your unit tests most of the time (it's generated after all). This can however interfere with your code coverage analysis, generating a lot of noise you don't want to see. This library helps you with that.

## Known issues

Objects with @Value and @Builder will only be picked up when they are generated with lombok 1.16.16 or later.

## The name?

COverage for LOmbok generated code with JAcoco.
