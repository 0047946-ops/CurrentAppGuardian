package com.currentguardian.testing.templates

import com.currentguardian.testing.TestEventTemplate

object GhostMTestTemplate {

    val events =
        listOf(

            TestEventTemplate(
                id = "GHOSTM_01",
                displayName = "進入遊戲",
                description =
                    "開始一次遊戲測試"
            ),

            TestEventTemplate(
                id = "GHOSTM_02",
                displayName = "開啟介面",
                description =
                    "測試介面開啟後的狀態"
            ),

            TestEventTemplate(
                id = "GHOSTM_03",
                displayName = "切換頁面",
                description =
                    "測試頁面切換"
            ),

            TestEventTemplate(
                id = "GHOSTM_04",
                displayName = "過圖",
                description =
                    "測試場景切換"
            ),

            TestEventTemplate(
                id = "GHOSTM_05",
                displayName = "打怪",
                description =
                    "測試戰鬥期間狀態"
            ),

            TestEventTemplate(
                id = "GHOSTM_06",
                displayName = "戰鬥結束",
                description =
                    "測試戰鬥結束後狀態"
            ),

            TestEventTemplate(
                id = "GHOSTM_07",
                displayName = "回村",
                description =
                    "測試返回城鎮後狀態"
            ),

            TestEventTemplate(
                id = "GHOSTM_08",
                displayName = "NPC",
                description =
                    "測試 NPC 互動"
            ),

            TestEventTemplate(
                id = "GHOSTM_09",
                displayName = "任務",
                description =
                    "測試任務操作"
            ),

            TestEventTemplate(
                id = "GHOSTM_10",
                displayName = "縮小／切換",
                description =
                    "測試 App 進入背景"
            )
        )
}
