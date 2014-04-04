BPMN Reference validation
===

Tool to check all references in BPMN 2.0 process files:

- Points the reference to an existing element?
- Has the referenced element an allowed, valid type? (e.g., references a `messageRef` attribute really a `message`)

## Software Requirements
- JDK 1.7.X
  - `JAVA_HOME` should point to the jdk directory
  - `PATH` should include `JAVA_HOME/bin`

## Licensing

LGPL Version 3: http://www.gnu.org/licenses/lgpl-3.0.html

## Project Usage

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
    
## Application Usage

The application supports some variants of validation and different log level. Language support is available for German and English

### API

If you add the jar as dependency to your project you can use the following methods of the Interface `BPMNReferenceValidator` and the corresponding implementation `BPMNReferenceValidatorImpl`:

```
validate(String path)
validateExistenceOnly(String path)
validateSingleFile(String path)
validateSingleFileExistenceOnly(String path)
setLogLevel(Level level)
setLanguage(int language)
```

### GUI

To open the GUI either run `gradlew run` or the jar.

# Authors 

- main contributor: Andreas Vorndran
- advisor: Matthias Geiger


