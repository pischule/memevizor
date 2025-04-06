package com.pischule.memestv

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.boot.runApplication

@SpringBootApplication
class MemesTvApplication

fun main(args: Array<String>) {
    runApplication<MemesTvApplication>(*args)
}
