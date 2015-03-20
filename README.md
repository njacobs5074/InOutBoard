[![Build Status](https://travis-ci.org/njacobs5074/InOutBoard.svg?branch=master)](https://travis-ci.org/njacobs5074/InOutBoard)
[![Codacy Badge](https://www.codacy.com/project/badge/008a969a845f495994c2ea473f786eb7)](https://www.codacy.com/public/nick_3/InOutBoard)
# InOutBoard - Simple In/Out Board App #

This application implements an "in/out board" - a 20th century device that people used in offices to record
their comings and goings. In/out boards usually include the following information, presented in a table:
* Person's name
* Their status, i.e. in or out of the office
* A comment

I wrote this application to teach myself how to use [AngularJS](https://angularjs.org/) a bit better, [SpringBoot](http://projects.spring.io/spring-boot/), and [STOMP/WebSockets](http://jmesnil.net/stomp-websocket/doc/).
This initial version is web-based - the next iteration will provide an Android app.

## Getting Started

Clone/fork this repository.

### Prerequisites

* Java 1.8
* Maven 3
* NPM
* Bower
* MySQL 5.0+ (if desired)

### Build
`mvn package`

*NB*: There's not much point installing it into your local Maven repository.

The Maven script runs `bower install` as part of the build (`generate-sources` phase for the interested).  For the even more interested, I run `bower` and `npm` in Maven profiles because on the [Travis](https://travis-ci.org) CI site, I have to run these commands separately and prior to Maven.  However, they run via Maven when run outside of Travis.

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
For server-side components, I used [JUnit](http://junit.org/) and Spring's related annotations.  For the client-side (JavaScript) components, I used [Karma](http://karma-runner.github.io/0.12/index.html) and [Jasmine](http://jasmine.github.io/2.0/introduction.html).  Note that as of this writing, I have only tested the code running in Chrome 39.0.2171.

The versions of the various test frameworks are managed via Maven, so you shouldn't need to do anything special to get it to work.

## Limitations
* Rudimentary sense of session.  Your login is stored locally and so can be recovered when you relaunch the page.  However, you cannot use that same login from another browser (i.e. you launch Chrome, then launch Safari) and perhaps more importantly, you can't access that login from another device.
