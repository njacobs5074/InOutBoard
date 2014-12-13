# InOutBoard - Simple In/Out Board App

This application implementation of an "in/out board" - a 20th century device that people used in offices to record
their comings and goings. They usually include the following information, presented in a table:
* Person's name
* Their status, i.e. in or out of the office
* A comment

I wrote this application to teach myself how to use AngularJS a bit better, SpringBoot, and STOMP/WebSockets.
This initial version is web-based - the next iteration will provide an Android app.

## Getting Started

Clone this repository.

### Prerequisites

* Java 1.8
* Maven 3
* NPM
* Bower
* MySQL 5.0+ (if desired)

### Build
`maven package`

The Maven script runs `bower install` as part of the build (`generate-sources` phase for the interested).

### Run the Application

`java -Dspring.datasource.platform=hsqldb -jar target/in-out-board-app-1.0-SNAPSHOT.war`

or

`java -Dspring.datasource.platform=mysql -jar target/in-out-board-app-1.0-SNAPSHOT.war`

The HSQL database is maintained in memory and will be wiped out on restart.  The MySQL database is, of course,
persisted but requires that you install MySQL 5.0+.  See the `src/main/resource/schema-mysql.sql` and `src/main/resources/application.properties`
as you'll likely want to change these.

Once this is running, navigate to `http://localhost:8080`.  You have 3 buttons on the right hand side of
the screen.  These are:

* Login/Connect - Will prompt you for a "handle" and your name.  Both are free-form text.
* Status - Once you've logged in, you can update your status and comment.
* Logout/Disconnect - Once logged in, you logout.

## Limitations
1. There is basically no sense of session. So, if you login and then reload the page, the app does not know you've previously
logged in.
