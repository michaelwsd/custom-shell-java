# Custom Shell

This project is a custom shell implemented in Java. It provides a command-line interface for executing built-in commands, running external programs, and managing input/output redirection. The shell is designed to mimic the behavior of traditional Unix-like shells.

## Features

- **Built-in Commands**: Includes commands like `echo`, `pwd`, `cd`, `ls`, `cat`, `history`, and more.
- **External Program Execution**: Supports running external programs available in the system's `PATH`.
- **Input/Output Redirection**: Handles redirection of output to files using `>` and `>>`.
- **Command History**: Maintains a history of executed commands.
- **Cross-Platform**: Compatible with both Unix-like systems and Windows.

## Getting Started

### Prerequisites

- Java Development Kit (JDK) 25 or higher.
- Maven build tool.

### Building the Project

To compile and package the project, run the following command:

```sh
mvn package
```

### Running the Shell

To start the shell locally, use the provided script:

```sh
./your_program.sh
```

### Usage

Once the shell is running, you can execute commands as you would in a traditional shell. For example:

```sh
$ echo Hello, World!
Hello, World!

$ pwd
/home/user

$ ls
file1.txt  file2.txt
```

## Project Structure

- `src/main/java`: Contains the Java source code for the shell.
- `pom.xml`: Maven configuration file.
- `your_program.sh`: Script to compile and run the shell locally.