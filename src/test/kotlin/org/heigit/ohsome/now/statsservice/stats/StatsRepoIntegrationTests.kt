package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.SpringTestWithClickhouse
import org.heigit.ohsome.now.statsservice.WithStatsData
import org.heigit.ohsome.now.statsservice.createClickhouseContainer
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.testcontainers.junit.jupiter.Container
import java.time.Instant
import java.time.LocalDateTime


// TODO: do we need more tests with contribution data with more than a single hashtag in the list?

@SpringTestWithClickhouse
@WithStatsData
class StatsRepoIntegrationTests {


    @BeforeEach
    fun checkClickhouse() = assertTrue(clickHouse.isRunning)


    companion object {

        @JvmStatic
        @Container
        private val clickHouse = createClickhouseContainer()


        @JvmStatic
        @DynamicPropertySource
        fun clickhouseUrl(registry: DynamicPropertyRegistry) =
            registry.add("spring.datasource.url") { clickHouse.jdbcUrl }
    }

    @Autowired
    lateinit var repo: StatsRepo

    val expected = mapOf(
        "changesets" to 1,
        "users" to 1,
        "edits" to 0,
        "latest" to "2017-12-19T00:52:03Z",
        "hashtag" to "&uganda"
    )

    private val emptyListCountryHandler = CountryHandler(emptyList())

    @Test
    fun `getStatsForTimeSpan should return all data when using no hashtag, no time span and null-list of countries`() {
        val hashtagHandler = HashtagHandler("")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, null, emptyListCountryHandler)
        assertEquals(5, result.size)
        println(result)
        assertEquals(
            "{changesets=7, users=6, edits=16, latest=2023-06-29T12:48:45Z, hashtag=}",
            result.toString()
        )
    }

    @Test
    fun `getStatsForTimeSpan should return all data when using no time span and null-list of countries`() {
        val hashtagHandler = HashtagHandler("&uganda")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, null, emptyListCountryHandler)
        assertEquals(5, result.size)
        println(result)
        assertEquals(expected.toString(), result.toString())
    }


    @Test
    fun `getStatsForTimeSpan should return all data when using no time span and list of 2 countries`() {
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, null, CountryHandler(listOf("HUN", "BEL")))
        assertEquals(5, result.size)
        println(result)
        assertEquals(
            "{changesets=4, users=2, edits=9, latest=2021-12-09T13:01:28Z, hashtag=}",
            result.toString()
        )
    }


    @Test
    fun `getStatsForTimeSpan returns partial data in time span`() {
        val startDate = Instant.ofEpochSecond(1457186410)
        val endDate = Instant.ofEpochSecond(1457186430)
        println(startDate)
        val hashtagHandler = HashtagHandler("&")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, startDate, endDate, emptyListCountryHandler)
        println(result)

        assertEquals(5, result.size)
        assertEquals("1", result["changesets"].toString())
        assertEquals("2016-03-05T14:00:20Z", result["latest"].toString())
    }


    @Test
    fun `getStatsForTimeSpan returns partial data in time span for start date only`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, startDate, null, emptyListCountryHandler)
        println(result)

        assertEquals(5, result.size)
        assertEquals("1", result["changesets"].toString())
        assertEquals("2021-12-09T13:01:28Z", result["latest"].toString())
    }


    @Test
    fun `getStatsForTimeSpan returns partial data in time span for end date only`() {
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, endDate, emptyListCountryHandler)
        println(result)

        assertEquals(5, result.size)
        assertEquals("1", result["changesets"].toString())
        assertEquals("2021-12-09T13:01:28Z", result["latest"].toString())
    }


    @Test
    fun `getStatsForTimeSpan returns combined data of multiple hashtags with wildcard`() {
        val hashtagHandlerWildcard = HashtagHandler("&group*")
        val resultWildCard =
            this.repo.getStatsForTimeSpan(hashtagHandlerWildcard, null, null, emptyListCountryHandler)

        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpan(hashtagHandler, null, null, emptyListCountryHandler)

        assertTrue(result["changesets"].toString().toInt() < resultWildCard["changesets"].toString().toInt())
    }


    @Test
    fun `getStatsForTimeSpanAggregate returns disaggregated data with wildcard`() {
        val hashtagHandlerWildcard = HashtagHandler("&group*")
        val resultsWildCard = this.repo.getStatsForTimeSpanAggregate(hashtagHandlerWildcard, null, null)

        assertEquals(2, resultsWildCard.size)

        val hashtags = mutableListOf<String>()
        resultsWildCard.onEach { hashtags.add(it["hashtag"].toString()) }
        assertIterableEquals(listOf("&groupExtra", "&group").sorted(), hashtags.sorted())
    }


    @Test
    fun `getStatsForTimeSpanAggregate returns data in list without wildcard`() {
        val hashtagHandler = HashtagHandler("&group")
        val resultsWildCard = this.repo.getStatsForTimeSpanAggregate(hashtagHandler, null, null)

        assertEquals(1, resultsWildCard.size)

        val hashtags = mutableListOf<String>()
        resultsWildCard.onEach { hashtags.add(it["hashtag"].toString()) }
        assertIterableEquals(listOf("&group").sorted(), hashtags.sorted())
    }


    @Test
    fun `getStatsForTimeSpan returns all data on everything with only wildcard character`() {
        // wasn't sure about the sql behavior of startWith(""), but it seems that it selects everything like expected
        // as of schema verison 3 startsWith("") counts all contributions which have hashtags
        // querying only "*" in currently prohoboted by HashtagValidator
        // if we want this behaviour, it could be optimized in HashtagHandler by filtering on has_hashtags
        val hashtagHandlerWildcard = HashtagHandler("*")
        val resultWildCard = this.repo.getStatsForTimeSpan(hashtagHandlerWildcard, null, null, emptyListCountryHandler)

        assertEquals(7, resultWildCard["changesets"].toString().toInt())
    }


    @Test
    fun `getStatsForTimeSpanInterval returns partial data in time span for start and end date with hashtag aggregated by month without countries`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            "P1M",
            this.emptyListCountryHandler
        )
        println(result)
        assertEquals(5, result.size)
        assertEquals(84, (result["changesets"] as LongArray).size)
        assertEquals("2015-01-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())
        assertEquals("1", (result["users"] as LongArray)[83].toString())
        assertEquals("7", (result["edits"] as LongArray)[83].toString())
    }


    @Test
    fun `getStatsForTimeSpanInterval returns partial data in time span for start and end date with hashtag aggregated by month with 1 country`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("&uganda")
        val result = this.repo.getStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            "P1M",
            CountryHandler(listOf("XYZ"))
        )
        println(result)
        assertEquals(5, result.size)
        assertEquals(84, (result["changesets"] as LongArray).size)
        assertEquals("2015-01-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())
        assertEquals("1", (result["users"] as LongArray)[35].toString())
        assertEquals("1", (result["changesets"] as LongArray)[35].toString())
    }


    @Test
    fun `getStatsForTimeSpanInterval fills data between two dates with zeros`() {

        val startDate = Instant.ofEpochSecond(1503644723)

        val endDate = Instant.ofEpochSecond(1640486233)
        val hashtagHandler = HashtagHandler("&gid")
        val result = this.repo.getStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            "P1M",
            this.emptyListCountryHandler
        )

        assertEquals(5, result.size)
        assertEquals(53, (result["changesets"] as LongArray).size)

        assertEquals("2017-08-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())
    }


    @Test
    fun `getStatsForTimeSpanInterval returns all data when nothing is supplied as startdate`() {
        val startDate = null
        val endDate = Instant.ofEpochSecond(1639054888)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            "P1M",
            this.emptyListCountryHandler
        )
        println(result)
        assertEquals(5, result.size)
        assertEquals(624, (result["edits"] as LongArray).size)
        assertEquals("1970-01-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())
    }

    @Test
    fun `getStatsForTimeSpanInterval aggregates data by year from all contributions with and without hashtags`() {
        val startDate = Instant.ofEpochSecond(1672531200) // 2023-01-01Z
        val endDate = Instant.ofEpochSecond(1704067200) // 2024-01-01Z
        val hashtagHandler = HashtagHandler("")
        val interval = "P1Y"
        val result = this.repo.getStatsForTimeSpanInterval(
            hashtagHandler,
            startDate,
            endDate,
            interval,
            this.emptyListCountryHandler
        )

        // year 2023 has 3 distinct userids with different and without hashtags
        assertEquals(3, (result["users"] as LongArray)[0])
    }


    @Test
    fun `getStatsForTimeSpanCountry returns partial data in time span for start and end date with hashtag aggregated by country`() {
        val startDate = Instant.ofEpochSecond(0)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getStatsForTimeSpanCountry(hashtagHandler, startDate, endDate)
        println(result)
        assertTrue(result is List)
        assertEquals(5, result[0].size)
    }


    @Test
    fun `getStatsByH3 returns data aggregated by h3`() {
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getStatsByH3(hashtagHandler, null, null)
        println(result)
    }


    @Test
    fun `getStatsForTimeSpanCountry aggregates data by country from all contributions with and without hashtags`() {
        val startDate = Instant.parse("2023-01-01T00:00:00Z")
        val endDate = Instant.parse("2024-01-01T00:00:00Z")
//        val startDate = Instant.ofEpochSecond(0)
//        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("")
        val result = this.repo.getStatsForTimeSpanCountry(hashtagHandler, startDate, endDate)
        println(result)
        assertEquals(5, result[0].size)

        // year 2023 has 3 distinct userids with different and without hashtags
        assertEquals("3", result[0]["users"].toString())
    }


    @Test
    fun `getStatsByUserIdAndHashtag returns stats for only one userid`() {
        val result = this.repo.getStatsByUserId(
            "2186388",
            HashtagHandler("hotosm-project-*"),
            StatsTopicsHandler(listOf("edit", "changeset"))
        )
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(3, result.size)
    }


    @Test
    fun `getStatsByUserIdAndHashtag returns zeros for unavailable user id`() {
        val result = this.repo.getStatsByUserId(
            "2186381",
            HashtagHandler("hotosm-project-*"),
            StatsTopicsHandler(listOf("edit", "user"))
        )
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(2186381, result["user_id"])
        assertEquals(UnsignedLong.valueOf(0), result["edits"])
        assertEquals(UnsignedLong.valueOf(0), result["changesets"])
    }


    @Test
    fun `getMostUsedHashtags returns the most used hashtags in given time range for start and enddate`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1639054890)
        val result = this.repo.getMostUsedHashtags(startDate, endDate, 10, CountryHandler(emptyList()))
        println(result)
        assertTrue(result[0] is Map)
        assertTrue(result[0].containsKey("hashtag"))
        assertTrue(result[0].containsKey("number_of_users"))
        assertTrue(result[0].size == 2)
        assertTrue(result.size == 7)
    }


    @Test
    fun `getMostUsedHashtags returns empty list when out of time bounds`() {
        val startDate = Instant.ofEpochSecond(1020991470)
        val endDate = Instant.ofEpochSecond(1019054890)
        val result = this.repo.getMostUsedHashtags(startDate, endDate, 10, CountryHandler(emptyList()))
        println(result)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `getMostUsedHashtags can filter list by countries`() {
        val result = this.repo.getMostUsedHashtags(Instant.EPOCH, Instant.now(), 10, CountryHandler(listOf("HUN")))
        assertEquals(listOf("&group", "&groupExtra"), result.map { it["hashtag"] })
    }

    @Test
    fun `getUniqueHashtags returns all hashtags`() {
        val result = this.repo.getUniqueHashtags()

        assertEquals(
            listOf(mapOf("hashtag" to "&group", "count" to 13)).toString(),
            result.toString()
        )
        println(result)
    }

    @Test
    fun `getMetadata returns the minimum and maximum timestamp`() {
        val result = this.repo.getMetadata()
        println(result)
        assertEquals("2009-04-22T22:00Z", result["min_timestamp"].toString())

    }
}


