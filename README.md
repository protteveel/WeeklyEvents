# WeeklyEvents

## Overview
**WeeklyEvents** is a Java-based application designed to help users manage and schedule their daily tasks efficiently. It operates by checking a predefined schedule from a JSON file and notifies users of their upcoming tasks at specified times throughout the week.

## Features
- **Daily Scheduling**: Allows users to define tasks for each day of the week.
- **Notifications**: Notifies users of tasks at specified times with a popup window.
- **Sound Alerts**: Plays a sound when notifying users of tasks.
- **Editable Schedule**: Users can edit their schedule through a dedicated interface.
- **Autonomous Operation**: Runs continuously and checks for tasks every minute.

## Requirements
- Java 22.0.1 or higher
- Apache Maven 3.9.6 or higher
- JavaFX SDK
- JSON-simple library for JSON parsing
- Media and MediaPlayer libraries for playing sound

## Installation
1. **Clone the repository**:
    ```bash
    git clone https://github.com/protteveel/WeeklyEvents
    cd WeeklyEvents
    ```
2. **Install dependencies**:
    Ensure that the required libraries are specified in the `pom.xml` file.
    ```xml
    <dependencies>
        <dependency>
            <groupId>com.googlecode.json-simple</groupId>
            <artifactId>json-simple</artifactId>
            <version>1.1.1</version>
        </dependency>
        <dependency>
            <groupId>org.openjfx</groupId>
            <artifactId>javafx-media</artifactId>
            <version>20</version>
        </dependency>
    </dependencies>
    ```
3. **Build the project**:
    ```bash
    mvn compile
    ```

## Usage
1. **Running the Application**:
    ```bash
    mvn exec:java
    ```

2. **Main Window**:
    - The main window displays the current day and time.
    - It checks for messages every minute based on the schedule defined in `WeeklyEvents.json`.

3. **Editing the Schedule**:
    - Click the "Edit" button in the main window to open the edit interface.
    - In the edit window, you can modify the tasks for each time slot and day.
    - Save your changes to update the schedule.

## Configuration
- **Schedule File**: `WeeklyEvents.json`
    - Define tasks for each day and time in this JSON file.
    - Example format:
      ```json
      {
        "Monday": {
          "07:00": [""],
          "07:15": [""],
          "07:30": [""],
          "07:45": ["Walk the dogs", "Feed the dogs"],
          ...
        },
        ...
      }
      ```

## Troubleshooting
- **Compilation Errors**: Ensure that all dependencies are correctly specified in the `pom.xml` file.
- **Sound Not Playing**: Check that the `events.mp3` file is located in the same directory as `WeeklyEvents.json`.

## Contributing
- Contributions are welcome. Please submit a pull request or open an issue to discuss your changes.

## License
- This project is licensed under the MIT License.

## Contact
- For any questions or support, please contact Percy Rotteveel at percy@rotteveel.ca
