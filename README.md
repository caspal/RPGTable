# RPGTable
RPGTable is a virtual table which provides a battle plan to support Pen & Paper Role-playing game players.

## Run it
1. Build it
```
$ ./bin/build.sh
```
2. Go into the the new created *dist* folder
3. Start it
```
$ .start.sh
```

## Folder structure
```
|-- bin
|   |-- build.sh
|   `-- test.sh
|-- browser
|   |-- dist
|   |-- src
|   `-- Gruntfile.js
|-- conf
|-- server
|   |-- src
|   |   |-- main
|   |   `-- test
|   |-- build.gradle
|   |-- dependencies.gradle
|   `-- gradlew
`-- README.md
```

### bin
The bin folder contains scripts to build, start and test the application.

### browser
The browser folder contains all files related to HTML5 client. We choose the name browser instead of client, because this allows us to add an additional client, e. g. an android client, easily.

### server
The server folder contains the back end, which is based on [Vert.x](http://vertx.io/)

## IDE
There is no specific IDE you have to use, but there are already Eclipse projects. But especially for the content in folder *browser* it could make sense to use an IDE which is more HTML5/JavaScript friendly.

## Contribute
We are using Gerrit, so PRs in Github will be ignored. Please use [GerritHub.io](https://review.gerrithub.io)
to contribute changes. The project name is *caspal/RPGTable*

### Code Style
1. Encoding must be in UTF-8.
2. Change must have Commit message.
3. The line endings must be LF (linux).
4. The length of a line should be between 80 and 120 characters.
5. Use spaces instead of tabs.
6. Use 4 spaces for indentation
  * If the project already uses 2, adapt the project rules
8. No trailing whitespaces.
9. Avoid unnecessary empty lines.
10. Adapt your code to the surroundings.
11. Follow the default language style guide.
  * [Java](http://www.oracle.com/technetwork/java/codeconventions-150003.pdf)
  * [JavaScript](http://vertx.io/)http://javascript.crockford.com/code.html
