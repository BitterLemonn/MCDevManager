# ResourceDetailVO 字段说明文档

> 资源详情接口返回的完整字段标注，对应数据类 `ResourceDetailVO` 及其嵌套类型。

---

## 一、基础信息

| 字段               | 类型       | 说明                                                   |
|------------------|----------|------------------------------------------------------|
| `itemId`         | `String` | 资源唯一 ID，如 `"4685709996580401081"`                    |
| `itemName`       | `String` | 资源名称，如 `"【苦柠】简易村民"`                                  |
| `itemVersion`    | `String` | 资源当前版本号，如 `"1.7"`                                    |
| `normalNumber`   | `String` | 资源编号（平台分配的可读编号），如 `"6604469"`                        |
| `status`         | `String` | 资源状态：`"online"` 上线 / `"offline"` 下线 / `"review"` 审核中 |
| `runningStatus`  | `String` | 运行状态，通常为 `"normal"`                                  |
| `category`       | `String` | 平台分类：`"pe"` 手机版 / `"pc"` PC 版                        |
| `subType`        | `Int`    | 资源子类型 ID（如 6 = Addon）                                |
| `modSecondType`  | `Int`    | Mod 二级分类 ID（如 202 = 玩法类）                             |
| `modVersion`     | `String` | 引擎/Mod 框架版本号，如 `"3.7"`                               |
| `mcVersion`      | `String` | 兼容的 MC 版本（主资源，此处为空表示跟随 `syncItemInfo`）               |
| `bodyType`       | `String` | 资源体型分类（一般为空）                                         |
| `itemRealStatus` | `Int`    | 资源真实状态码（1 = 正常）                                      |

---

## 二、作者信息

| 字段           | 类型       | 说明                                       |
|--------------|----------|------------------------------------------|
| `authorInfo` | `String` | 作者简介 HTML，如 `"<p>原Reinhart开发者账号...</p>"` |

---

## 三、描述与展示

| 字段                   | 类型       | 说明                            |
|----------------------|----------|-------------------------------|
| `info`               | `String` | 资源详细介绍，HTML 格式，包含图片和描述文本      |
| `bannerPic`          | `String` | 横幅大图 URL（一般为空则无横幅）            |
| `updateSummary`      | `String` | 最近更新摘要，如 `"[feature]更新村民交易机"` |
| `chargeDesc`         | `String` | 付费备注信息（开发者自己填写）               |
| `activityDesc`       | `String` | 活动描述（参与活动时使用）                 |
| `peGameIntroduction` | `String` | PE 端游戏简介                      |

---

## 四、价格与付费

| 字段                   | 类型       | 说明                                                          |
|----------------------|----------|-------------------------------------------------------------|
| `price`              | `Int`    | 资源售价（钻石/点券），如 `300`                                         |
| `priceType`          | `String` | 价格类型：`"diamond"` 钻石 / `"points"` 点券 / `"free"` 免费           |
| `priceRank`          | `Int`    | 价格排名（0 = 无排名）                                               |
| `priType`            | `Int`    | 付费类型 ID：`0` 免费 / `1` 限时免费 / `2` 付费 / `3` 其他                 |
| `chargeType`         | `String` | 收费模式：`"worlds"` 按世界 / `"once"` 一次性 / `"free"` 免费            |
| `androidPrice`       | `Int`    | Android 端价格（0 表示跟随统一价）                                      |
| `androidPriceType`   | `String` | Android 端价格类型                                               |
| `iosPrice`           | `Int`    | iOS 端价格（0 表示跟随统一价）                                          |
| `iosPriceType`       | `String` | iOS 端价格类型                                                   |
| `iosJellyId`         | `String` | iOS 内购商品 ID（iOS 付费资源使用）                                     |
| `premiumApplyStatus` | `String` | 精品申请状态：`"no_apply"` 未申请 / `"applying"` 审核中 / `"passed"` 已通过 |

---

## 五、统计与评分

| 字段            | 类型       | 说明                   |
|---------------|----------|----------------------|
| `score`       | `Double` | 资源评分（满分 5.0），如 `4.8` |
| `ratingLevel` | `Int`    | 评级等级（0 = 未评级）        |

---

## 六、时间信息

| 字段                  | 类型        | 说明                     |
|---------------------|-----------|------------------------|
| `createTime`        | `String`  | 资源创建时间，ISO 8601 格式     |
| `onlineTime`        | `String`  | 最近一次上线时间               |
| `firstOnlineTime`   | `String`  | 首次上线时间                 |
| `firstAcceptTime`   | `String`  | 首次通过审核时间               |
| `applyReviewTime`   | `String`  | 最近一次提交审核时间             |
| `appointOnlineTime` | `String?` | 预约上线时间（`null` = 未设置预约） |

---

## 七、标签与分类

| 字段              | 类型                        | 说明                      |
|-----------------|---------------------------|-------------------------|
| `tags`          | `List<ResourceDetailTag>` | 资源标签列表，如 `[工业, 村民, 生存]` |
| `labelTypeList` | `List<Int>`               | 标签类型 ID 列表（平台内部分类码）     |

### ResourceDetailTag

| 字段       | 类型       | 说明                        |
|----------|----------|---------------------------|
| `name`   | `String` | 标签名称，如 `"工业"`             |
| `source` | `Int`    | 标签来源：`0` 开发者自填 / `1` 系统推荐 |

---

## 八、资源文件

| 字段              | 类型                              | 说明                   |
|-----------------|---------------------------------|----------------------|
| `res`           | `List<ResourceDetailRes>`       | 资源包文件列表（主包体 + 各平台包体） |
| `channel`       | `List<ResourceDetailChannel>`   | 资源渠道分发列表（不同平台版本）     |
| `videoInfoList` | `List<ResourceDetailVideoInfo>` | 资源宣传视频列表             |

### ResourceDetailRes — 资源包文件

| 字段           | 类型                          | 说明                             |
|--------------|-----------------------------|--------------------------------|
| `addVersion` | `Boolean`                   | 是否为增量更新版本                      |
| `cdnInfo`    | `ResourceDetailResFileInfo` | CDN 分发文件信息（大小/MD5/时间）          |
| `cdnUrl`     | `String`                    | CDN 下载地址                       |
| `mcVersion`  | `List<String>`              | 适配的 MC 版本列表                    |
| `resId`      | `Int`                       | 资源文件 ID                        |
| `resInfo`    | `ResourceDetailResFileInfo` | 原始资源文件信息（上传包体信息）               |
| `resName`    | `String`                    | 资源文件名，如 `"SimpleVillager.zip"` |
| `resUrl`     | `String`                    | 资源原始 URL                       |

### ResourceDetailResFileInfo — 文件信息

| 字段        | 类型       | 说明                    |
|-----------|----------|-----------------------|
| `resMd5`  | `String` | 文件 MD5 校验值            |
| `resSize` | `Int`    | 文件大小（字节）              |
| `resTime` | `String` | 文件上传/更新时间，ISO 8601 格式 |

### ResourceDetailChannel — 渠道分发

| 字段           | 类型       | 说明                                                |
|--------------|----------|---------------------------------------------------|
| `channelId`  | `Int`    | 渠道 ID（3=Android, 5=iOS, 6=PC, 1001/1002/1003=子平台） |
| `channelUrl` | `String` | 渠道资源下载 URL                                        |
| `version`    | `Int`    | 渠道资源版本号                                           |

### ResourceDetailVideoInfo — 宣传视频

| 字段      | 类型       | 说明          |
|---------|----------|-------------|
| `cover` | `String` | 视频封面图 URL   |
| `size`  | `Int`    | 视频文件大小（字节）  |
| `url`   | `String` | 视频播放/下载 URL |

---

## 九、DLC 信息

### ResourceDetailDlcInfo

| 字段          | 类型        | 说明                                               |
|-------------|-----------|--------------------------------------------------|
| `dlcSwitch` | `Boolean` | 是否启用 DLC 模式                                      |
| `dlcType`   | `String`  | DLC 类型：`"off"` 关闭 / `"master"` 主包 / `"slave"` 从包 |
| `master`    | `String?` | 主包信息（作为从包时引用主包 ID）                               |
| `slaveList` | `String?` | 从包列表（作为主包时引用从包 ID 列表）                            |

---

## 十、性能数据

### ResourceDetailPerfData

| 字段           | 类型        | 说明           |
|--------------|-----------|--------------|
| `memSize`    | `Int`     | 资源占用内存大小（MB） |
| `memWarning` | `Boolean` | 内存占用是否触发警告   |

---

## 十一、大厅资源

### ResourceDetailLobbyRes

| 字段                     | 类型             | 说明                |
|------------------------|----------------|-------------------|
| `lobbyManifestVersion` | `String`       | 大厅清单版本号（UUID 格式）  |
| `lobbyResMd5`          | `String`       | 大厅资源 MD5 校验值      |
| `lobbyResSize`         | `Int`          | 大厅资源大小（字节）        |
| `lobbyResUrl`          | `String`       | 大厅资源下载 URL        |
| `mcpSigns`             | `List<String>` | MCP 签名列表（资源完整性校验） |

---

## 十二、同步物品详情（PC/手机同步）

### ResourceDetailSyncItemInfo

当资源同时支持 PC 和手机版时，`syncItemInfo` 存储另一平台（同步端）的资源信息。

| 字段                  | 类型                                | 说明                                               |
|---------------------|-----------------------------------|--------------------------------------------------|
| `availableScope`    | `String`                          | 可用范围：`"client/server"` 客户端和服务端 / `"client"` 仅客户端 |
| `brief`             | `String`                          | 同步端资源简介                                          |
| `category`          | `String`                          | 同步端分类：`"comp"` 组件 / `"play"` 玩法等                 |
| `channel`           | `List<ResourceDetailSyncChannel>` | 同步端的渠道分发列表                                       |
| `gameHost`          | `String?`                         | 游戏主机信息                                           |
| `includeMap`        | `Boolean`                         | 是否包含地图                                           |
| `info`              | `String`                          | 同步端详细介绍 HTML                                     |
| `itemId`            | `String`                          | 同步端资源 ID                                         |
| `itemName`          | `String`                          | 同步端资源名称                                          |
| `mcVersion`         | `List<String>`                    | 同步端适配 MC 版本，如 `["100.0.0"]`                      |
| `priType`           | `Int`                             | 同步端付费类型                                          |
| `rarity`            | `Int`                             | 同步端稀有度                                           |
| `requirement`       | `List<JsonElement>`               | 同步端前置依赖列表                                        |
| `status`            | `String`                          | 同步端资源状态                                          |
| `subType`           | `Int`                             | 同步端子类型                                           |
| `tag`               | `List<Int>`                       | 同步端标签 ID 列表                                      |
| `weakOffline`       | `Boolean`                         | 同步端是否弱联网                                         |
| `weakOfflineReason` | `String`                          | 同步端弱联网原因说明                                       |

### ResourceDetailSyncChannel

| 字段           | 类型       | 说明                         |
|--------------|----------|----------------------------|
| `channelId`  | `Int`    | 同步端渠道 ID（7/8/9/10... 各子平台） |
| `channelUrl` | `String` | 同步端渠道资源 URL                |
| `version`    | `Int?`   | 渠道版本号（部分旧渠道可能无此字段）         |

---

## 十三、大厅配置

| 字段                   | 类型        | 说明              |
|----------------------|-----------|-----------------|
| `lobbyCommercialize` | `Boolean` | 大厅是否启用商业化       |
| `lobbyForceMaxNum`   | `Int`     | 大厅强制最大人数，如 `10` |
| `lobbyMaxNum`        | `Int`     | 大厅最大人数（0 = 不限制） |
| `lobbyMinNum`        | `Int`     | 大厅最小人数          |
| `lobbyNormalMode`    | `Boolean` | 是否为普通模式大厅       |
| `lobbyPlayerNum`     | `Int`     | 大厅当前玩家数         |
| `lobbyReconnectTime` | `Int`     | 大厅断线重连超时时间（秒）   |
| `isLobbyCompetitive` | `Boolean` | 是否为竞技类大厅        |
| `mainCity`           | `Boolean` | 是否为主城资源         |
| `canManageServer`    | `Boolean` | 是否支持管理服务器       |

---

## 十四、布尔功能标记

| 字段                         | 说明                |
|----------------------------|-------------------|
| `activityOnly`             | 是否仅限活动内获取         |
| `canSilentOnline`          | 是否支持静默上线（无推送通知）   |
| `canSynchronizePcOld`      | 是否可同步至旧版 PC 端     |
| `dyeingOrigin`             | 是否为染色原始资源         |
| `firstSellRankTop`         | 首次售卖是否进入排行榜前列     |
| `forceEncrypt`             | 是否强制加密资源包         |
| `isInPromotionApplication` | 是否在推广申请中          |
| `isJointActivity`          | 是否为联合活动资源         |
| `isLotteryReward`          | 是否为抽奖奖励           |
| `isOfficialItem`           | 是否为官方资源           |
| `isOriginal`               | 是否为原创资源           |
| `isPersona`                | 是否为人格/皮肤类资源       |
| `isPremium`                | 是否为精品资源           |
| `isRecommend`              | 是否为推荐资源           |
| `isSeasonMod`              | 是否为赛季 Mod         |
| `isSilentOnline`           | 当前是否处于静默上线状态      |
| `isSpigot`                 | 是否为 Spigot 插件     |
| `isSuitablePc`             | 是否适配 PC 端         |
| `isSync`                   | 是否为同步资源（PC/手机双端）  |
| `isVipBenefit`             | 是否为 VIP 专属福利      |
| `itemUpdatePush`           | 资源更新时是否推送通知给已购买用户 |
| `mountCallEnabled`         | 是否启用骑乘召唤功能        |
| `needBehaviourUuid`        | 是否需要行为包 UUID      |
| `needMethodUuid`           | 是否需要方法 UUID       |
| `oriWeakOffline`           | 原始弱联网标记           |
| `pure`                     | 是否为纯净资源（无附加内容）    |
| `relateItemWeakOffline`    | 关联资源是否弱联网         |
| `searchable`               | 是否可被搜索到           |
| `syncPcFlag`               | 是否标记为 PC 同步资源     |
| `versionCompatibleEnable`  | 是否启用版本兼容模式        |
| `vipOnly`                  | 是否仅限 VIP 用户       |
| `weakOffline`              | 当前是否为弱联网资源        |
| `peIsAddPlayPlan`          | PE 端是否加入游玩计划      |

---

## 十五、整型配置

| 字段                            | 说明                                  |
|-------------------------------|-------------------------------------|
| `achievementEnabled`          | 成就系统开关：`0` 关闭 / `1` 开启              |
| `advObtainNum`                | 通过广告获取次数限制                          |
| `antiCheatEnable`             | 反作弊开关：`0` 关闭 / `1` 开启               |
| `claimItemEnabled`            | 领取物品功能开关                            |
| `decomposeCurrency`           | 分解获得的货币数量                           |
| `exchangeCurrency`            | 兑换所需货币数量                            |
| `exemptPerfReviewNum`         | 豁免性能评审次数                            |
| `isDomainServerItem`          | 是否为域名服务器资源：`0` 否 / `1` 是            |
| `isEa`                        | 是否为抢先体验（Early Access）：`0` 否 / `1` 是 |
| `lotteryId`                   | 关联抽奖活动 ID（0 = 无关联）                  |
| `performanceServiceAvailable` | 性能服务可用状态                            |
| `performanceServiceStatus`    | 性能服务当前状态                            |
| `personaMtypeid`              | 人格主类型 ID                            |
| `personaStypeid`              | 人格子类型 ID                            |
| `playPlanExpireTime`          | 游玩计划过期时间（`-1` = 永不过期）               |
| `queuePosition`               | 审核队列中的位置                            |
| `rarity`                      | 稀有度等级：`0` 普通                        |
| `seasonBegin`                 | 赛季开始时间戳（0 = 非赛季资源）                  |
| `trialDuration`               | 试用时长（分钟，0 = 无试用）                    |
| `urgentStatus`                | 加急审核状态：`0` 未加急                      |

---

## 十六、字符串配置

| 字段                         | 说明                                                 |
|----------------------------|----------------------------------------------------|
| `achievementBackgroundUrl` | 成就背景图 URL                                          |
| `collectionId`             | 所属合集 ID（`"0"` = 不属于任何合集）                           |
| `collectionName`           | 所属合集名称                                             |
| `decomposeCurrencyType`    | 分解货币类型：`"ordinary"` 普通                             |
| `discountActivityStatus`   | 折扣活动状态：`"init"` 初始 / `"active"` 活跃 / `"ended"` 已结束 |
| `dyeing`                   | 染色信息                                               |
| `exchangeCurrencyType`     | 兑换货币类型：`"ordinary"` 普通                             |
| `jointActivityName`        | 联合活动名称                                             |
| `maintain`                 | 维护状态：`"normal"` 正常                                 |
| `multiTags`                | 多标签（`"0"` = 无多标签）                                  |
| `oriWeakOfflineReason`     | 原始弱联网原因                                            |
| `personaMtype`             | 人格主类型名称                                            |
| `personaStype`             | 人格子类型名称                                            |
| `preReviewVideo`           | 预审视频信息（JSON 字符串）                                   |
| `relateItemId`             | 关联资源 ID（同步端的对应资源）                                  |
| `subjectId`                | 主题 ID                                              |
| `suitId`                   | 套装 ID（`"0"` = 不属于任何套装）                             |
| `urgentReason`             | 加急审核原因（开发者填写的说明）                                   |
| `vanityNumber`             | 自定义编号（靓号）                                          |
| `weakOfflineReason`        | 弱联网原因说明                                            |
| `whitelist`                | 白名单信息                                              |

---

## 十七、可空字段

| 字段                    | 说明                            |
|-----------------------|-------------------------------|
| `gameHost`            | 游戏主机地址（大厅类资源使用，`null` = 无）    |
| `appointOnlineTime`   | 预约上线时间（`null` = 无预约）          |
| `levelData`           | 关卡数据（仅地图类资源有值，`null` = 非地图资源） |
| `levelDataSyncError`  | 关卡数据同步错误信息                    |
| `levelDataSyncStatus` | 关卡数据同步状态                      |
| `levelDataVersion`    | 关卡数据版本号                       |
| `priEffectType`       | 付费效果类型                        |
| `subEffectType`       | 子效果类型                         |

---

## 十八、数组字段（平台扩展配置）

> 以下字段为平台内部使用的扩展配置数组，当前返回为空或仅在特定场景有值。统一使用 `List<JsonElement>`
> 接收。

| 字段                   | 说明              |
|----------------------|-----------------|
| `achievementConfigs` | 成就配置列表          |
| `changeLog`          | 更新日志列表          |
| `discount`           | 折扣信息列表          |
| `interceptFields`    | 拦截字段列表（内容审核用）   |
| `jointActivityTag`   | 联合活动标签          |
| `lobbyCamps`         | 大厅阵营配置          |
| `lobbyConfigOpLog`   | 大厅配置操作日志        |
| `lobbyTags`          | 大厅标签            |
| `peActivityCoupon`   | PE 活动优惠券 ID 列表  |
| `peChatBubbleId`     | PE 聊天气泡 ID 列表   |
| `peEmotesId`         | PE 表情 ID 列表     |
| `peFrameId`          | PE 头像框 ID 列表    |
| `peFurnitureId`      | PE 家具 ID 列表     |
| `peHomeCash`         | PE 主页现金道具 ID 列表 |
| `peItemId`           | PE 关联物品 ID 列表   |
| `peLotteryChance`    | PE 抽奖机会 ID 列表   |
| `peMcItemId`         | PE MC 物品 ID 列表  |
| `peOneMonthVipId`    | PE 月度 VIP ID 列表 |
| `pePassportTenId`    | PE 护照十连抽 ID 列表  |
| `peSixMonthVipId`    | PE 半年 VIP ID 列表 |
| `peUserBackgroundId` | PE 用户背景 ID 列表   |
| `prerequisiteItems`  | 前置依赖资源列表        |

---

## 十九、JSON 对象字段

| 字段                    | 说明                    |
|-----------------------|-----------------------|
| `dyeingRelation`      | 染色关系映射（`{}` = 无染色关系）  |
| `jointActivityDetail` | 联合活动详情对象（`{}` = 无活动）  |
| `lobbySortKey`        | 大厅排序权重映射（`{}` = 默认排序） |

---

## 二十、渠道 ID 对照表

根据接口数据分析，常见渠道 ID 含义如下：

| 渠道 ID | 平台              |
|-------|-----------------|
| 3     | Android         |
| 5     | iOS             |
| 6     | PC (Windows)    |
| 7     | Android（同步端）    |
| 8     | Nintendo Switch |
| 9     | PlayStation     |
| 10    | Xbox            |
| 11    | 通用主机            |
| 12    | VR              |
| 13    | edu（教育版）        |
| 14    | Amazon          |
| 16    | ChromeOS        |
| 29    | 其他平台            |
| 1001  | 子渠道 A           |
| 1002  | 子渠道 B           |
| 1003  | 子渠道 C           |
| 1004  | 子渠道 D           |
| 1005  | 子渠道 E           |
