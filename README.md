# About this project

Project to setup a backend service with Gradle, Springboot and Postgres.

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
