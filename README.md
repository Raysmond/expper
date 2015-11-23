Expper
=====

> Explore the Internet and Share Something Useful.

This is the source code [https://www.expper.com](https://www.expper.com) website.

## Prerequisites
- JDK 1.8+
- Spring Boot 1.3.0.RELEASE
- PostgreSQL 9.4+
- Jhipster 2.24.0
- Redis 3.0+
- RabbitMQ 3.5.6+
- Node.js 

## Installation in development
1. Install node and bower dependencies
    ```
    npm install -g grunt-cli 
    npm install
    bower install 
    ```
2. Start Postgres and other services
    ```
    # in Mac OS X
    brew install redis
    brew install rabbitmq
    
    redis-server
    rabbitmq-server
    ```
3. Create and modify the configuration files according to your settings
    ```
    cp src/main/resources/config/application.example.yml src/main/resources/config/application.yml 
    cp src/main/resources/config/application-dev.example.yml src/main/resources/config/application-dev.yml 
    cp src/main/resources/config/application-prod.example.yml src/main/resources/config/application-prod.yml 
    ```
4. Start application in development mode
    ```
    gradle bootRun # or ./gradlew bootRun , if you haven't install gradle yet
    
    # open another terminal and run grunt
    grunt 
    ```


## Building for production

To optimize the expper client for production, run:

    ./gradlew -Pprod clean bootRepackage

To ensure everything worked, run:

    java -jar build/libs/*.war --spring.profiles.active=prod

Then navigate to [http://localhost:9000](http://localhost:9000) in your browser.

## Testing

Unit tests are run by [Karma][] and written with [Jasmine][]. They're located in `src/test/javascript` and can be run with:

    grunt test
    
UI end-to-end tests are powered by [Protractor][], which is built on top of WebDriverJS. They're located in `src/test/javascript/e2e` 
and can be run by starting Spring Boot in one terminal (`./gradlew bootRun`) and running the tests (`grunt itest`) in a second one.


## License
This project is licensed under GPLv3 license.
