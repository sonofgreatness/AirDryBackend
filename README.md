# Build Commands
## Build Whole Project
`./gradlew build `
## Build Compose Desktop Only
`
./gradlew  composeApp:build`
`
./gradlew composeApp:run `
## Build Ktor Server Only
`./gradlew server:build`
`./gradlew server:run`

--
# Package Commands

## Full Stack Webapp (Ktor + React)
**Important:** Perform a full clean build first.

`./gradlew clean build`

THEN

`./gradlew server:shadowJarForServer`

## ComposeDesktop
`./gradlew composeApp:packageDistributionForCurrentOS`
