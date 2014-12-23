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
`mvn package`

The Maven script runs `bower install` as part of the build (`generate-sources` phase for the interested).

### Run the Application

`java -jar target/in-out-board-app-1.0-SNAPSHOT.war --spring.profiles.active=hsqldb`

or

`java -jar target/in-out-board-app-1.0-SNAPSHOT.war --spring.profiles.active=mysql`

The HSQL database is maintained in memory and will be wiped out on restart.  The MySQL database is, of course,
persisted but requires that you install MySQL 5.0+.  See the `src/main/resource` directory as you'll probably
need/want to change the values for your environment.

Once this is running, navigate to `http://localhost:8080`.  You have 3 buttons on the right hand side of
the screen.  These are:

* Login/Connect - Will prompt you for a "handle" and your name.  Both are free-form text.
* Status - Once you've logged in, you can update your status and comment.
* Logout/Disconnect - Once logged in, you can logout should you so desire..

### Testing
For server-side components, I used jUnit and Spring's related annotations.  For the client-side (JavaScript) components, I used
Karma and Jasmine.  Note that as of this writing, I have only tested the code running in Chrome 39.0.2171.

The versions of the various test frameworks  managed via the `pom.xml`, so you shouldn't need to do anything special to get it to work.

## Limitations
* Rudimentary sense of session.  Your login is stored locally and so can be recovered when you relaunch the page.  However, you cannot
use that same login from another browser (i.e. you launch Chrome, then launch Safari) and perhaps more importantly, you can't access
that login from another device.
