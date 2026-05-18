package org.sb.sbexercise

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.retry.annotation.EnableRetry

@EnableRetry
@SpringBootApplication
class SbExerciseApplication

fun main(args: Array<String>) {
    runApplication<SbExerciseApplication>(*args)
}
