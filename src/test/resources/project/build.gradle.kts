plugins {
    java
    id("org.cthing.build-greeting")
}

version = "1.2.3"
group = "org.cthing"

tasks.register<DefaultTask>("hello") {
    doLast {
        println("hello world")
    }
}
