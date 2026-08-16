(() => {
    "use strict";

    const STORAGE_KEY =
        "current_app_guardian_web_blackbox_v1";

    const MAX_EVENTS = 300;

    const state = {
        startedAt: Date.now(),

        fps: 0,

        frameTime: 0,

        lastFrameTime: performance.now(),

        frameCounter: 0,

        lastFpsCalculation:
            performance.now(),

        socketState:
            "UNKNOWN",

        events:
            loadEvents()
    };

    function loadEvents() {

        try {

            const raw =
                localStorage.getItem(
                    STORAGE_KEY
                );

            if (!raw) {
                return [];
            }

            const parsed =
                JSON.parse(raw);

            return Array.isArray(parsed)
                ? parsed.slice(
                    -MAX_EVENTS
                )
                : [];

        } catch {

            return [];
        }
    }

    function persist() {

        try {

            localStorage.setItem(
                STORAGE_KEY,
                JSON.stringify(
                    state.events.slice(
                        -MAX_EVENTS
                    )
                )
            );

        } catch {
            /*
             * Web Storage 失敗不應讓
             * Web App 自己出錯。
             */
        }
    }

    function record(
        type,
        detail = ""
    ) {

        const event = {

            timestamp:
                new Date()
                    .toISOString(),

            type,

            detail

        };

        state.events.push(
            event
        );

        if (
            state.events.length >
            MAX_EVENTS
        ) {

            state.events =
                state.events.slice(
                    -MAX_EVENTS
                );
        }

        persist();

        renderEvent(
            event
        );
    }

    function renderEvent(
        event
    ) {

        const timeline =
            document.getElementById(
                "eventTimeline"
            );

        if (!timeline) {
            return;
        }

        const row =
            document.createElement(
                "div"
            );

        row.className =
            "event";

        const time =
            document.createElement(
                "time"
            );

        time.textContent =
            formatTime(
                event.timestamp
            );

        const text =
            document.createElement(
                "span"
            );

        text.textContent =
            event.type +
            (
                event.detail
                    ? ` — ${event.detail}`
                    : ""
            );

        row.appendChild(time);
        row.appendChild(text);

        timeline.prepend(
            row
        );

        while (
            timeline.children.length >
            100
        ) {

            timeline.removeChild(
                timeline.lastChild
            );
        }
    }

    function renderStoredEvents() {

        for (
            const event of state.events
        ) {

            renderEvent(
                event
            );
        }
    }

    function formatTime(
        iso
    ) {

        try {

            return new Date(
                iso
            ).toLocaleTimeString();

        } catch {

            return "--:--:--";
        }
    }

    function frameLoop(
        now
    ) {

        const delta =
            now -
            state.lastFrameTime;

        state.lastFrameTime =
            now;

        state.frameTime =
            delta;

        state.frameCounter++;

        const elapsed =
            now -
            state.lastFpsCalculation;

        if (
            elapsed >= 1000
        ) {

            state.fps =
                Math.round(
                    (
                        state.frameCounter *
                        1000
                    ) /
                    elapsed
                );

            state.frameCounter =
                0;

            state.lastFpsCalculation =
                now;

            updateMetrics();
        }

        requestAnimationFrame(
            frameLoop
        );
    }

    function updateMetrics() {

        const fps =
            document.getElementById(
                "fpsValue"
            );

        const frameTime =
            document.getElementById(
                "frameTimeValue"
            );

        if (fps) {

            fps.textContent =
                state.fps;
        }

        if (frameTime) {

            frameTime.textContent =
                state.frameTime
                    .toFixed(1) +
                " ms";
        }

        const socket =
            document.getElementById(
                "socketValue"
            );

        if (socket) {

            socket.textContent =
                state.socketState;
        }
    }

    async function measureLatency() {

        const start =
            performance.now();

        try {

            /*
             * 同源小請求；沒有可用端點時，
             * 只記錄 UNKNOWN，不假裝這是
             * 遊戲伺服器 Ping。
             */

            const response =
                await fetch(
                    window.location.href,
                    {
                        method: "HEAD",
                        cache: "no-store"
                    }
                );

            if (!response.ok) {

                throw new Error(
                    "HTTP " +
                    response.status
                );
            }

            const latency =
                Math.round(
                    performance.now() -
                    start
                );

            const latencyElement =
                document.getElementById(
                    "latencyValue"
                );

            if (latencyElement) {

                latencyElement.textContent =
                    latency +
                    " ms";
            }

            return latency;

        } catch {

            const latencyElement =
                document.getElementById(
                    "latencyValue"
                );

            if (latencyElement) {

                latencyElement.textContent =
                    "UNKNOWN";
            }

            return null;
        }
    }

    function markEvent() {

        const label =
            prompt(
                "輸入要標記的事件："
            );

        if (
            !label ||
            !label.trim()
        ) {

            return;
        }

        record(
            "USER_MARK",
            label.trim()
        );
    }

    function exportBlackBox() {

        const payload = {

            app:
                location.href,

            startedAt:
                new Date(
                    state.startedAt
                ).toISOString(),

            exportedAt:
                new Date()
                    .toISOString(),

            fps:
                state.fps,

            frameTime:
                state.frameTime,

            socketState:
                state.socketState,

            events:
                state.events

        };

        const blob =
            new Blob(
                [
                    JSON.stringify(
                        payload,
                        null,
                        2
                    )
                ],
                {
                    type:
                        "application/json"
                }
            );

        const url =
            URL.createObjectURL(
                blob
            );

        const anchor =
            document.createElement(
                "a"
            );

        anchor.href =
            url;

        anchor.download =
            "guardian_web_blackbox_" +
            Date.now() +
            ".json";

        anchor.click();

        URL.revokeObjectURL(
            url
        );

        record(
            "BLACKBOX_EXPORTED"
        );
    }

    function clearBlackBox() {

        const confirmed =
            confirm(
                "確定清除本機 Web 黑盒？"
            );

        if (!confirmed) {
            return;
        }

        localStorage.removeItem(
            STORAGE_KEY
        );

        state.events = [];

        const timeline =
            document.getElementById(
                "eventTimeline"
            );

        if (timeline) {

            timeline.innerHTML =
                "";
        }

        record(
            "BLACKBOX_CLEARED"
        );
    }

    function updateLifecycle(
        type
    ) {

        record(
            type
        );
    }

    document.addEventListener(
        "visibilitychange",
        () => {

            if (
                document.hidden
            ) {

                updateLifecycle(
                    "PAGE_BACKGROUND"
                );

            } else {

                updateLifecycle(
                    "PAGE_FOREGROUND"
                );
            }
        }
    );

    window.addEventListener(
        "error",
        event => {

            record(
                "JAVASCRIPT_ERROR",
                event.message ||
                    "UNKNOWN"
            );
        }
    );

    window.addEventListener(
        "unhandledrejection",
        event => {

            record(
                "UNHANDLED_REJECTION",
                String(
                    event.reason
                )
            );
        }
    );

    document.addEventListener(
        "DOMContentLoaded",
        () => {

            record(
                "WEB_APP_STARTED"
            );

            renderStoredEvents();

            requestAnimationFrame(
                frameLoop
            );

            measureLatency();

            setInterval(
                measureLatency,
                5000
            );

            const markButton =
                document.getElementById(
                    "markButton"
                );

            if (markButton) {

                markButton.onclick =
                    markEvent;
            }

            const exportButton =
                document.getElementById(
                    "exportButton"
                );

            if (exportButton) {

                exportButton.onclick =
                    exportBlackBox;
            }

            const clearButton =
                document.getElementById(
                    "clearButton"
                );

            if (clearButton) {

                clearButton.onclick =
                    clearBlackBox;
            }
        }
    );

    window.CurrentAppGuardianWeb = {

        record,

        exportBlackBox,

        markEvent,

        measureLatency,

        getState() {

            return {
                ...state,

                events:
                    [
                        ...state.events
                    ]
            };
        }
    };
})();
