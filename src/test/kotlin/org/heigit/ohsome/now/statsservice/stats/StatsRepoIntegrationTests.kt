package org.heigit.ohsome.now.statsservice.stats

import com.clickhouse.data.value.UnsignedLong
import org.heigit.ohsome.now.statsservice.SpringTestWithClickhouse
import org.heigit.ohsome.now.statsservice.WithStatsData
import org.heigit.ohsome.now.statsservice.createClickhouseContainer
import org.heigit.ohsome.now.statsservice.utils.CountryHandler
import org.heigit.ohsome.now.statsservice.utils.HashtagHandler
import org.heigit.ohsome.now.statsservice.utils.UserHandler
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
        "edit" to 0,
        "changeset" to 1,
        "contributor" to 1,
        "hashtag" to "&uganda"
    )

    private val emptyListCountryHandler = CountryHandler(emptyList())
    private val statsTopicsHandler = StatsTopicsHandler(listOf("edit", "changeset", "contributor"))
    private val noUserHandler = UserHandler("");

    @Test
    fun `getStatsForTimeSpan should return all data when using no hashtag, no time span and null-list of countries`() {
        val hashtagHandler = HashtagHandler("")
        val result =
            this.repo.getStatsForTimeSpan(
                hashtagHandler,
                null,
                null,
                emptyListCountryHandler,
                statsTopicsHandler,
                noUserHandler
            )
        assertEquals(4, result.size)
        println(result)
        assertEquals(
            "{edit=16, changeset=7, contributor=6, hashtag=}", result.toString()
        )
    }

    @Test
    fun `getStatsForTimeSpan should return all data when using no time span and null-list of countries`() {
        val hashtagHandler = HashtagHandler("&uganda")
        val result =
            this.repo.getStatsForTimeSpan(
                hashtagHandler,
                null,
                null,
                emptyListCountryHandler,
                statsTopicsHandler,
                noUserHandler
            )
        assertEquals(4, result.size)
        println(result)
        assertEquals(expected.toString(), result.toString())
    }


    @Test
    fun `getStatsForTimeSpan should return all data when using no time span and list of 2 countries`() {
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getStatsForTimeSpan(
            hashtagHandler, null, null, CountryHandler(listOf("HUN", "BEL")), statsTopicsHandler, noUserHandler
        )
        assertEquals(4, result.size)
        println(result)
        assertEquals(
            "{edit=9, changeset=4, contributor=2, hashtag=}", result.toString()
        )
    }


    @Test
    fun `getStatsForTimeSpan returns partial data in time span`() {
        val startDate = Instant.ofEpochSecond(1457186410)
        val endDate = Instant.ofEpochSecond(1457186430)
        println(startDate)
        val hashtagHandler = HashtagHandler("&")
        val result = this.repo.getStatsForTimeSpan(
            hashtagHandler, startDate, endDate, emptyListCountryHandler, statsTopicsHandler, noUserHandler
        )
        println(result)

        assertEquals(4, result.size)
        assertEquals("1", result["changeset"].toString())
    }


    @Test
    fun `getStatsForTimeSpan returns partial data in time span for start date only`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val hashtagHandler = HashtagHandler("&group")
        val result =
            this.repo.getStatsForTimeSpan(
                hashtagHandler,
                startDate,
                null,
                emptyListCountryHandler,
                statsTopicsHandler,
                noUserHandler
            )
        println(result)

        assertEquals(4, result.size)
        assertEquals("1", result["changeset"].toString())
    }


    @Test
    fun `getStatsForTimeSpan returns partial data in time span for end date only`() {
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("&group")
        val result =
            this.repo.getStatsForTimeSpan(
                hashtagHandler,
                null,
                endDate,
                emptyListCountryHandler,
                statsTopicsHandler,
                noUserHandler
            )
        println(result)

        assertEquals(4, result.size)
        assertEquals("1", result["changeset"].toString())
    }


    @Test
    fun `getStatsForTimeSpan returns combined data of multiple hashtags with wildcard`() {
        val hashtagHandlerWildcard = HashtagHandler("&group*")
        val resultWildCard = this.repo.getStatsForTimeSpan(
            hashtagHandlerWildcard, null, null, emptyListCountryHandler, statsTopicsHandler, noUserHandler
        )

        val hashtagHandler = HashtagHandler("&group")
        val result =
            this.repo.getStatsForTimeSpan(
                hashtagHandler,
                null,
                null,
                emptyListCountryHandler,
                statsTopicsHandler,
                noUserHandler
            )

        assertTrue(result["changeset"].toString().toInt() < resultWildCard["changeset"].toString().toInt())
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
        // as of schema version 3 startsWith("") counts all contributions which have hashtags
        // querying only "*" is currently prohibited by HashtagValidator
        // if we want this behavior, it could be optimized in HashtagHandler by filtering on has_hashtags
        val hashtagHandlerWildcard = HashtagHandler("*")
        val resultWildCard = this.repo.getStatsForTimeSpan(
            hashtagHandlerWildcard, null, null, emptyListCountryHandler, statsTopicsHandler, noUserHandler
        )

        assertEquals(7, resultWildCard["changeset"].toString().toInt())
    }


    @Test
    fun `getStatsForTimeSpanInterval returns partial data in time span for start and end date with hashtag aggregated by month without countries`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpanInterval(
            hashtagHandler, startDate, endDate, "P1M", this.emptyListCountryHandler, statsTopicsHandler, noUserHandler
        )
        println(result)
        assertEquals(5, result.size)
        assertEquals(84, (result["changeset"] as DoubleArray).size)
        assertEquals("2015-01-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())
        assertEquals("1.0", (result["contributor"] as DoubleArray)[83].toString())
        assertEquals("7.0", (result["edit"] as DoubleArray)[83].toString())
    }


    @Test
    fun `getStatsForTimeSpanInterval returns partial data in time span for start and end date with hashtag aggregated by month with 1 country`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("&uganda")
        val result = this.repo.getStatsForTimeSpanInterval(
            hashtagHandler, startDate, endDate, "P1M", CountryHandler(listOf("XYZ")), statsTopicsHandler, noUserHandler
        )
        println(result)
        assertEquals(5, result.size)
        assertEquals(84, (result["changeset"] as DoubleArray).size)
        assertEquals("2015-01-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())
        assertEquals("1.0", (result["contributor"] as DoubleArray)[35].toString())
        assertEquals("1.0", (result["changeset"] as DoubleArray)[35].toString())
    }


    @Test
    fun `getStatsForTimeSpanInterval fills data between two dates with zeros`() {

        val startDate = Instant.ofEpochSecond(1503644723)

        val endDate = Instant.ofEpochSecond(1640486233)
        val hashtagHandler = HashtagHandler("&gid")
        val result = this.repo.getStatsForTimeSpanInterval(
            hashtagHandler, startDate, endDate, "P1M", this.emptyListCountryHandler, statsTopicsHandler, noUserHandler
        )

        assertEquals(5, result.size)
        assertEquals(53, (result["changeset"] as DoubleArray).size)

        assertEquals("2017-08-01T00:00", (result["startdate"] as Array<LocalDateTime>)[0].toString())
    }


    @Test
    fun `getStatsForTimeSpanInterval returns all data when nothing is supplied as startdate`() {
        val startDate = null
        val endDate = Instant.ofEpochSecond(1639054888)
        val hashtagHandler = HashtagHandler("&group")
        val result = this.repo.getStatsForTimeSpanInterval(
            hashtagHandler, startDate, endDate, "P1M", this.emptyListCountryHandler, statsTopicsHandler, noUserHandler
        )
        println(result)
        assertEquals(5, result.size)
        assertEquals(624, (result["edit"] as DoubleArray).size)
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
            this.emptyListCountryHandler,
            statsTopicsHandler,
            noUserHandler
        )

        // year 2023 has 3 distinct userids with different and without hashtags
        assertEquals(3.0, (result["contributor"] as DoubleArray)[0])
    }


    @Test
    fun `getStatsForTimeSpanCountry returns partial data in time span for start and end date with hashtag aggregated by country`() {
        val startDate = Instant.ofEpochSecond(0)
        val endDate = Instant.ofEpochSecond(1639054890)
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getStatsForTimeSpanCountry(
            hashtagHandler,
            startDate,
            endDate,
            statsTopicsHandler,
            noUserHandler
        )
        println(result)
        assertTrue(result is List)
        assertEquals(4, result[0].size)
    }


    @Test
    fun `getStatsForTimeSpanCountry aggregates data by country from all contributions with and without hashtags`() {
        val startDate = Instant.parse("2023-01-01T00:00:00Z")
        val endDate = Instant.parse("2024-01-01T00:00:00Z")
        val hashtagHandler = HashtagHandler("")
        val result = this.repo.getStatsForTimeSpanCountry(
            hashtagHandler,
            startDate,
            endDate,
            statsTopicsHandler,
            noUserHandler
        )
        println(result)
        assertEquals(4, result[0].size)

        // year 2023 has 3 distinct userids with different and without hashtags
        assertEquals("3", result[0]["contributor"].toString())
    }


    @Test
    fun `getStatsByH3 returns data aggregated by h3`() {
        val hashtagHandler = HashtagHandler("*")
        val result = this.repo.getStatsByH3(
            hashtagHandler,
            null,
            null,
            StatsTopicsHandler(listOf("edit")),
            3,
            CountryHandler(emptyList()),
            UserHandler("")
        )
        println(result)
        assertTrue(result.contains("832830fffffffff"))
        assertTrue(result.contains("result,hex_cell\n"))
    }


    @Test
    fun `getStatsByUserIdAndHashtag returns stats for only one userid`() {
        val result = this.repo.getStatsForTimeSpan(
            HashtagHandler("hotosm-project-*"),
            null,
            null,
            CountryHandler(emptyList()),
            StatsTopicsHandler(listOf("edit", "changeset")),
            UserHandler("2186388"),
        )
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(3, result.size)
    }


    @Test
    fun `getStatsForTimeSpan returns zeros for unavailable user id`() {
        val result = this.repo.getStatsForTimeSpan(
            HashtagHandler("hotosm-project-*"),
            null,
            null,
            CountryHandler(emptyList()),
            StatsTopicsHandler(listOf("edit", "changeset")),
            UserHandler("2186381"),
        )
        println(result)
        assertTrue(result is MutableMap<String, *>)
        assertEquals(UnsignedLong.valueOf(0), result["edit"])
        assertEquals(UnsignedLong.valueOf(0), result["changeset"])
    }


    @Test
    fun `getMostUsedHashtags returns the most used hashtags in given time range for start and enddate`() {
        val startDate = Instant.ofEpochSecond(1420991470)
        val endDate = Instant.ofEpochSecond(1639054890)
        val result = this.repo.getMostUsedHashtags(
            startDate,
            endDate,
            countryHandler = CountryHandler(emptyList()),
        )
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
        val result = this.repo.getMostUsedHashtags(
            startDate,
            endDate,
            countryHandler = CountryHandler(emptyList()),
        )
        println(result)
        assertEquals(emptyList<String>(), result)
    }

    @Test
    fun `getMostUsedHashtags can filter list by countries`() {
        val result = this.repo.getMostUsedHashtags(
            Instant.EPOCH,
            Instant.now(),
            countryHandler = CountryHandler(listOf("HUN")),
        )
        assertEquals(listOf("&group", "&groupExtra"), result.map { it["hashtag"] })
    }

    @Test
    fun `getUniqueHashtags returns all hashtags`() {
        val result = this.repo.getUniqueHashtags()

        assertEquals(
            listOf(mapOf("hashtag" to "&group", "count" to 13)).toString(), result.toString()
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


