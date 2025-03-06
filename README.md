# About this project

Project to setup a backend service with Gradle, Springboot and Postgres.

# Quickstart

 - Start docker desktop
 - docker compose up dev-db
 - (test) docker compose up test-db
 - Run bootRun to start app

## Make a clean build

Will run the tests and then build

```aidl
./gradlew clean build
```
## Project

 - Using Gradle 8.7

```aidl
java --version
java 17.0.1 2021-10-19 LTS
Java(TM) SE Runtime Environment (build 17.0.1+12-LTS-39)
Java HotSpot(TM) 64-Bit Server VM (build 17.0.1+12-LTS-39, mixed mode, sharing)
```

## Running the application

You need docker desktop running to start the database. Then:

```
docker compose up dev-db
```

![img.png](images/img.png)

Check the build.gradle file to see dependencies required.

We can run the application by using the IDE action "bootRun" as seen in the image above.

Or we can also do:

```
./gradlew bootRun
```


## Running the tests

First, you need to run the docker test db:

```aidl
docker compose up test-db
```

Check the build.gradle file to see dependencies required.

![img.png](images/imgTest.png)

We can run the application by using the IDE action "verification" -> "test" as seen in the image 
above.

## Migrations

First file:

```aidl
./gradlew generateChangeLog
```

Drop the DB:

```aidl
 ./gradlew dropall
```

Update the DB with migration file
```aidl
 ./gradlew update
```


### Deployment in Railway

First some notes: We can always start the app from the command line using:

./gradlew bootRun

So we can also pass parameters here to make sure we are using the right profile,

 - Railway connects to the repo in github and automatically makes a build of the app.
 - 