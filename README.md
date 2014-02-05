BPMN Referencevalidation
===

Tool to check all references in BPMN 2.0 process files:

- Points the reference to an existing element?
- Has the referenced element an allowed, valid type? (e.g., references a `messageRef` attibute really a `message`)

## Software Requirements
- JDK 1.7.X
  - `JAVA_HOME` should point to the jdk directory
  - `PATH` should include `JAVA_HOME/bin`

## Licensing
TBD

## Usage

```bash
$ gradlew build # builds the project to folder `/build`

$ gradlew assemble # Builds a jar which will be stored in `/build/libs`

# Generate project files 
$ gradlew idea # Generates Intellij IDEA project files
$ gradlew eclipse # Generates Eclipse project files
```



## Project Structure

    src/ # the main source code
    resources/ # needed resources such as BPMN XSD files and all defined references to check
    test/src # JUnit tests for testing assertions 
    test/tests # BPMN files used in JUnit tests

# Authors 

- main contributor: Andreas Vorndran
- advisor: Matthias Geiger


