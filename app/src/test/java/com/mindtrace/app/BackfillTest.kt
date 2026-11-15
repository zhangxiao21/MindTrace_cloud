package com.mindtrace.app

import com.mindtrace.app.data.DateUtils
import com.mindtrace.app.data.MedicationEntity
import com.mindtrace.app.data.selectDoseMedications
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

/** 补记（历史数据补全）相关纯逻辑测试，不需要 Android Context 或数据库。 */
class BackfillTest {

    private fun med(
        id: Long,
        active: Boolean,
        startDate: String,
        endDate: String? = null,
        asNeeded: Boolean = false
    ) = MedicationEntity(
        id = id,
        name = "Med$id",
        times = "08:00,20:00",
        startDate = startDate,
        endDate = endDate,
        active = active,
        asNeeded = asNeeded,
        createdAt = 0L
    )

    @Test
    fun backfill_lists_stopped_or_late_added_medications() {
        val pastDate = "2026-11-10"
        val meds = listOf(
            med(1, active = true, startDate = "2026-11-01"),
            med(2, active = false, startDate = "2026-11-20"), // 已停用、且晚于补记日才添加
            med(3, active = true, startDate = "2026-10-01", endDate = "2026-11-05"),
            med(4, active = true, startDate = "2026-11-01", asNeeded = true)
        )

        val historical = selectDoseMedications(pastDate, meds, historical = true)
        assertEquals("补记应列出全部定时药物", setOf(1L, 2L, 3L), historical.map { it.id }.toSet())

        val today = selectDoseMedications(pastDate, meds, historical = false)
        assertEquals("常规打卡只显示在服且落在起止日期内的药物", setOf(1L), today.map { it.id }.toSet())
    }

    @Test
    fun invalid_date_produces_no_doses() {
        assertTrue(selectDoseMedications("not-a-date", listOf(med(1, true, "2026-11-01")), true).isEmpty())
    }

    @Test
    fun backdated_scale_timestamp_keeps_the_chosen_date() {
        val timestamp = DateUtils.timestampOf("2026-10-05", "14:30")
        val local = DateUtils.toLocalDateTime(timestamp)

        assertEquals(LocalDate.of(2026, 10, 5), local.toLocalDate())
        assertEquals(14, local.hour)
        assertEquals(30, local.minute)
    }

    @Test
    fun future_date_never_becomes_a_backfill_target() {
        val today = DateUtils.todayIso()
        val future = DateUtils.iso(DateUtils.today().plusDays(3))
        assertTrue(future > today)
        // 仓库层用同样的「字符串比较 + parse」判定未来；这里锁定工具行为。
        assertEquals(3L, java.time.temporal.ChronoUnit.DAYS.between(DateUtils.today(), DateUtils.parse(future)!!))
    }
}
