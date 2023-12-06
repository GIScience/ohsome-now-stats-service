package org.heigit.ohsome.now.statsservice

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class AppProperties {
    @Value("\${ohsome.contribution.stats.service.token}")
    lateinit var token: String
}