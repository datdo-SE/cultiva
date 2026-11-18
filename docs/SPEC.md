# Phù Lục — Product Spec

Status: draft, derived from the design canvas "Phù Lục — Thiết kế màn hình" (https://claude.ai/artifact/EZez3nRWeAtXR4UBZgecc6) as of 2026-11-18. The canvas is the visual source of truth; this file is the behavioral one. When they disagree, raise it before building.

## 1. Summary

Phù Lục is a Vietnamese-language screen-time app for iOS and Android. Users hand-draw talismans (bùa) and stick them on apps they want to use less. Each talisman is one rule on one app: limit minutes, lock during hours, or force a breathing pause before opening. Drawing is deliberate friction: a talisman costs effort to make and is consumed when used, so rules cannot be reused forever.

Release order: Android ships first (native Kotlin, Jetpack Compose). iOS follows. Shared rules must stay achievable on iOS (§7.2), so Android v1 does not add behavior iOS cannot match.

Out of scope for v1: iOS, reading content inside apps, sending usage data off the device, Android Accessibility Service.

## 2. Glossary

| Term | Meaning |
| --- | --- |
| Bùa / lá bùa | Talisman. One drawn talisman = one rule on one app. |
| Mẫu bùa | Talisman template (type): Định Thời, Cấm, Tĩnh Tâm. |
| Bùa phổ | Library of the user's drawn designs, reused as faint guides. |
| Túi bùa | Pouch: inventory of drawn, unused talismans, counted per type. |
| Cõi | Realm: a named group of apps (e.g. Mạng xã hội, Video ngắn, Trò chơi). |
| Động phủ | Home screen. |
| Trận pháp | Formation: a scheduled set of talismans. |
| Trận nhãn | Formation eye: the stroke that arms a formation. |
| Khai trận | Activate a formation for one run. |
| Phong ấn | Seal: the block state and its shield screen. |
| Phá phong | Override a seal. Costs đạo hạnh. |
| Đạo hạnh | Merit points, the only currency. |
| Tu vi | Cultivation level: Luyện Khí → Trúc Cơ → Kết Đan, each with tầng (tiers). |
| Linh mạch | Cap on concurrent schedules (18). |
| Khai quang | Consecrate: confirm a drawn talisman and make it active. |
| Ấn son | Red seal at the talisman's foot. Shows the template sign: 定 Định Thời, 禁 Cấm, 靜 Tĩnh Tâm. Never the app name. |

## 3. Talisman types

| Type | Sign | Rule | Shield | Override |
| --- | --- | --- | --- | --- |
| Định Thời Phù | 定 | Max N minutes per day per app (15–120, step 15; default 30). Expires 00:00 and is consumed. | When minutes run out, until 00:00. | 30 đạo hạnh, hold 3 s. Talisman is torn (consumed). App opens 15 min. Then the next Định Thời in the pouch auto-applies. |
| Cấm Phù | 禁 | Lock the app within a time window (e.g. 08:00–17:00). | During the window. | 50 đạo hạnh, hold 3 s. Talisman is not torn. App opens 15 min, then relocks until window end. |
| Tĩnh Tâm Phù | 靜 | Breathe for X seconds (default 15) before each open. 5 opens per talisman. | Breathing screen on every open (§7). | Complete the breathing; consumes 1 of 5 opens. |

Precedence when several talismans apply to one app: the strictest wins.

1. Cấm Phù inside its window.
2. Định Thời Phù with minutes exhausted.
3. Tĩnh Tâm Phù.

Lower-precedence talismans show "Chờ đến lượt" in the app detail sheet.

## 4. Economy

- Start: Luyện Khí tầng 1, 50 đạo hạnh.
- +2 đạo hạnh each time the user leaves a sealed app ("Rời đi" / "Về động phủ").
- −30 to override Định Thời, −50 to override Cấm.
- Not enough đạo hạnh: the override button is disabled and says exactly how many are missing ("Thiếu 12 đạo hạnh").
- Tu vi advances by consecutive days of keeping rules ("12 ngày giữ luật", "Còn 9 ngày nữa đến Kết Đan").

## 5. Talisman lifecycle

1. **Draw** (Vẽ bùa): pick type, draw, name it, pick target realm, set the parameter.
2. **Khai quang**: confirm. A realm with K apps needs K talismans; the user draws them one by one.
3. **Pouch**: spare talismans wait in the pouch, counted per type.
4. **Apply**: drag from the pouch onto an app on the home screen, or "Dán thêm bùa" in app detail.
5. **Consume**: Định Thời at 00:00 or on override; Tĩnh Tâm after 5 opens.
6. **Remove** ("Gỡ"): destroys the talisman. It never returns to the pouch.

Empty pouch: the app shows "Túi rỗng" and has no rule protecting it. Notify when one talisman is left.

## 6. Screens

### 6.1 Onboarding (6 screens, fixed order, ~2 min)

The canvas draws the iPhone variant. Android v1 differs on screens 2, 3 and 5 as below.

| # | Screen | Android (v1) | iOS (later) |
| --- | --- | --- | --- |
| 1 | Chào | One Định Thời Phù, one sentence on what the app does. Single CTA "Bắt đầu nhập môn". States "Mất khoảng 2 phút." | Same. |
| 2 | Cấp quyền | Explain first, then two rows, each with "Mở Cài đặt" deep-linking to its settings page: Usage access (`PACKAGE_USAGE_STATS`) and Display over other apps (`SYSTEM_ALERT_WINDOW`). Each row shows granted/not granted on return. "Tiếp tục" enabled only when both are granted. Never request Accessibility. Lists what the app can and cannot do (no content, no data off device). | Screen Time authorization via system prompt, confirmed by Face ID or passcode. On denial: inline alert with retry and Settings path. |
| 3 | Cõi đầu tiên | In-app picker of launchable apps. Preselect the Social category (`ApplicationInfo.CATEGORY_SOCIAL`) minus Zalo (`com.zing.zalo`). Selecting a category stores the category, so later installs in it join the realm. "Chọn lại" reopens the picker. | `FamilyActivityPicker`, same preselection. Category selection also avoids the ~50-token limit. |
| 4 | Lá bùa đầu tiên | Draw one Định Thời per app in the realm (3 apps = 3 talismans). Faint template to trace; from the second talisman, the previous drawing is the guide. Progress dots per talisman. Minutes slider. CTA "Khai quang lá 1 trên 3". | Same. |
| 5 | Thông báo | Android 13+: request `POST_NOTIFICATIONS`. Used for: one talisman left in the pouch, formation starting. Not needed to reach rituals. "Để sau" allowed. | Required path from shield to rituals (§7.2). "Để sau" states the user must open Phù Lục manually to override. |
| 6 | Khai quang | Talismans stamp down. "3 lá bùa đã có hiệu lực." Explain expiry at 00:00 and that the pouch is empty: draw more tonight. CTA "Vào động phủ". | Same. |

### 6.2 Động phủ (home)

- Header: title, đạo hạnh balance.
- Today's total usage, delta vs yesterday.
- Tu vi card: level, streak days, progress to next level.
- Next formation card: name, start time, armed state ("Chưa có trận nhãn, chạm để vẽ").
- Pouch: per-type counts (e.g. "4 / 10 lá").
- Managed apps only. Each row: icon, name, usage, talisman count, usage bar, state chip: Phong ấn, Tĩnh tâm n/5, Cấm đến HH:MM, Còn phút, Túi rỗng.
- Drag a talisman from the pouch onto an app row to apply.
- "+ Thêm app cần phong ấn".
- Tab bar: Động phủ, Vẽ bùa, Trận pháp.

### 6.3 Vẽ bùa (draw)

- Type selector: Định Thời, Cấm Phù, Tĩnh Tâm, with one-line explanation.
- Paper with faint guide ("Nét mẫu" toggle). Seal shows the type sign.
- Name field ("Tên lá bùa", e.g. "Cấm Phù học đường").
- Target realm ("Dán lên"): list of realms with app counts, "+ Lập cõi mới".
- Parameter: minutes per day / time window / breathing seconds.
- Summary sentence: "Lá bùa này sẽ giới hạn TikTok và YouTube 30 phút mỗi ngày. Cõi có 2 app nên cần 2 lá. Mỗi lá tàn lúc 00:00."
- Tools: Hoàn tác, Xoá nét, inkstone, brush rack.
- CTA "Khai quang lá đầu tiên".

Brush and ink:

- Finger input: ink comes out at the brush tip, offset up-left from the touch point. Stylus: ink at the contact point.
- One dip ≈ 900 px of stroke. A bar under the inkstone shows ink on the brush. Low ink → dry-brush (phi bạch) white streaks.
- Brushes: Tiểu khải ×0.6, Trung khải ×1, Đại khải ×1.6. The active one lowers and its tip turns red.
- Speed: slow = thick, fast = thin. Pause at pen-down = heavy head. Lift while fast = tapered tail.
- Style: Lỗ Ban. No written characters; brush strokes only. Every design has a 雨 head, a type-specific body, a spiral tail.
  - Định Thời: two stacked rectangles.
  - Cấm: three 口 forming 品 over a barred door.
  - Tĩnh Tâm: chain of linked rings like breaths.

### 6.4 Trận pháp (formations)

- Formation selector (e.g. Dạ Tĩnh Trận, Giờ học), "+ Trận mới".
- Bronze drum face with up to 8 talisman slots. Tap a talisman from Bùa phổ to place; tap one in the formation to remove.
- Schedule: start, end, days (T2–CN).
- Effect summary ("Facebook, TikTok, Instagram phải thở 15 giây trước khi mở", "Liên Quân bị khoá hẳn", "Mỗi lần khai trận lấy 4 lá từ túi").
- Each run draws fresh talismans from the pouch: one per app per talisman.
- Unarmed warning: "Chưa có trận nhãn. Vẽ trước 22:00 để trận vận hành tối nay."
- CTA "Lưu trận".

### 6.5 Trận nhãn (arm a formation)

- Draw one cinnabar line through every talisman in the formation, ending at the sun in the drum center. Connected talismans glow.
- Status list: connected talismans, then the remaining ones by name.
- "Khai trận" enabled only when all are connected.
- Accessible alternatives: "Chạm từng lá thay vì vẽ"; keyboard Tab then Enter.
- "Nghỉ trận lần này" skips this run.

### 6.6 Chi tiết app (app detail sheet)

- App usage today and its realm.
- Talismans on this app with state ("Đã cạn 45/45 phút · đang phong ấn", "Chờ đến lượt: Định Thời Phù đang nghiêm hơn", "còn 5/5 lần mở"), expiry, pouch count.
- "Gỡ" per talisman, with note: removing destroys it.
- "Dán thêm bùa", "Xem màn phong ấn".

### 6.7 Phong ấn (seal) — see §7 for platform mechanics

| Type | Title | Primary | Secondary |
| --- | --- | --- | --- |
| Định Thời | "TikTok đã bị phong ấn" + used/limit, unseals 00:00 | Rời đi · +2 đạo hạnh | Phá phong (30 đạo hạnh) |
| Tĩnh Tâm | "Tĩnh tâm trước khi mở Facebook" + opens left | Thôi, rời đi · +2 đạo hạnh | Bắt đầu tĩnh tâm |
| Cấm | "Liên Quân bị cấm đến 17:00" + window | Rời đi · +2 đạo hạnh | Phá phong (50 đạo hạnh) |

Android: this is one full-screen Phù Lục screen shown over the blocked app. The secondary button opens the ritual directly in place. "Rời đi" sends the user to the launcher.

iOS (later): this is the system shield. The secondary button posts a notification; the subtitle becomes "Đã gửi thông báo. Chạm vào thông báo…" with "Gửi lại thông báo". The ritual opens from the notification.

Rituals:

- **Phá phong**: cost table (balance, fee, remaining or missing), what happens after (opens 15 min; next talisman auto-applies, or relocks until window end). Hold 3 s to confirm. "Giữ phong ấn, về động phủ" as the alternative. Android: on success, dismiss and return to the app. iOS: tell the user to reopen the app from the home screen.
- **Tĩnh tâm**: breathing ring, countdown, "Hít vào / Thở ra" alternating every 4 s. "Mở khoá Facebook" enabled at 0. Shows opens left. Teal-tinted background to distinguish from blocking types.

## 7. Platform mechanics

### 7.1 Android (v1)

Native Kotlin, Jetpack Compose. No Accessibility Service.

- **Detect the foreground app**: `UsageStatsManager.queryEvents` (needs Usage access), polled by a foreground service while any rule is active. Android 14+: foreground service type `specialUse` with a declared subtype.
- **Count minutes**: sum foreground time per package from usage events since 00:00 local.
- **Block**: when a sealed package comes to the foreground, start the full-screen ritual Activity over it. Display over other apps grants the background-activity-start exemption needed for this.
- **Leave**: "Rời đi" sends `Intent.ACTION_MAIN` + `CATEGORY_HOME`, then grants +2 đạo hạnh.
- **Override**: lift the block for that package for 15 min, then re-evaluate rules.
- **Schedules** (Cấm windows, formations, 00:00 expiry): exact alarms or WorkManager; re-register on boot (`RECEIVE_BOOT_COMPLETED`) and time/zone change.
- **Gestures**: hold 3 s via Compose `pointerInput` (`detectTapGestures(onPress)` with timer). Breathing ring via `Canvas` + `animateFloatAsState`.
- **Data**: all state on device. Back up nothing to a server.
- **Permission loss**: if Usage access or overlay is revoked, rules cannot enforce. Show a blocking banner on Động phủ with "Mở Cài đặt".

### 7.2 iOS (later: FamilyControls, ManagedSettings, DeviceActivity)

- Authorization: Screen Time (`AuthorizationCenter`), confirmed by Face ID or passcode.
- App selection: `FamilyActivityPicker`; the app sees opaque tokens, not names.
- Shield: system-drawn via `ShieldConfiguration`. Customizable only: background color and blur style, one icon image, title, subtitle, two button labels and colors. No hold gesture, cannot open the main app.
  - Icon = the user's drawn talisman, read by the extension from the App Group.
  - Primary button returns `.close` and grants +2 đạo hạnh.
  - Secondary button posts a local notification and returns `.defer` so the shield redraws with the "notification sent" subtitle.
- Notifications are the only path from shield to ritual. Without them the user must open Phù Lục manually.
- Rituals: SwiftUI `fullScreenCover`, inset grouped list. Hold via `onLongPressGesture(minimumDuration: 3)`. On success, lift the shield for 15 min via `ManagedSettingsStore`. iOS cannot open the target app; tell the user to return to it.
- Tĩnh Tâm still uses a shield on iOS: the system has no "slow down" mode.
- Breathing ring: `Circle().trim` + animation, timing via `TimelineView`.
- Limits: 20 `DeviceActivity` schedules → cap Cấm Phù + active formations at 18 (linh mạch). Android v1 enforces the same cap so rules port unchanged. Category selection avoids the ~50 app-token limit.

## 8. Visual system

Colors:

| Name | Hex | Use |
| --- | --- | --- |
| Mực chàm | `#1B2341` | Screen background |
| Đêm | `#121830` | Ritual background |
| Hoàng chỉ | `#E8C96F` | Talisman paper, selection. Paper gradient `#EFD47F` → `#E8C96F` → `#CFA647` |
| Chu sa | `#B42C18` | Ink, primary button, seal |
| Chu sa sáng | `#EE6A4F` | Warnings on dark |
| Linh khí | `#8CC6A8` | Remaining time only |

Text `#E6E0D2` / secondary `#A0A6C2` / muted body `#C3C7DA`. Border `#4A5585`. Primary button text `#FFF4DE`.

Type: Alegreya 700 for headings, big numbers, seals. Be Vietnam Pro 400–600 for body and buttons. Native ritual screens use the system font (Roboto on Android, SF Pro on iOS); the canvas draws them iOS-style, so Android adapts them to Material 3 layout with the same content and colors. Seal glyphs (定 禁 靜) need a CJK fallback; Alegreya has none.

Accessibility: touch targets ≥ 44 px; real buttons/links; `aria-label` (or platform equivalent) on icon-only controls; text contrast ≥ 4.5:1; every drawing interaction has a tap or keyboard alternative.

## 9. Open questions

- Confirm the two Android permissions (Usage access + Display over other apps). The canvas shows two rows but does not name them.
- Android minimum SDK.
- Android picker: show all launchable apps, or only categorized ones? Many apps report no category.
- Accept polling latency (blocked app visible briefly before the ritual covers it)? Pick a poll interval.

- Pouch shows "4 / 10 lá": is 10 a per-type cap?
- "2 trận lực" on the formation screen: what does it count?
- Exact tu vi thresholds (days per tier, per realm).
- Does +2 đạo hạnh for leaving a sealed app have a daily cap?
- Cấm Phù override: can it be repeated within one window, and does each cost 50?
