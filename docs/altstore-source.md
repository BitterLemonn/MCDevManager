# 使用 AltStore / SideStore 安装与发布

## 为什么增加这个配置

可以把 Source 理解为一份“应用目录和更新清单”：它告诉 AltStore / SideStore 应用名称、版本以及去哪下载 IPA。用户添加一次源后，即可在客户端中查看版本、安装与发现更新，不必每次到 GitHub 下载 IPA 再手动导入。是否自动安装更新仍取决于侧载客户端、签名有效期与系统限制。

对维护者而言，这不是增加一套应用商店服务：文件托管在现有 GitHub Release，无需另购服务器、接入 Apple 开发者后台或收集用户 Apple ID。本改动不修改应用业务代码、登录流程、密码存储或运行时网络请求，不会给应用增加权限，也不会自动替用户签名。

构建侧增加的工作为：为 iOS 包设置真实版本/构建号，读取本次及历史 IPA 并输出 JSON，再随现有草稿 Release 上传。另有独立的整理工作流，在正式发布后更新固定软件源，或由维护者手动运行。权限字段只是对包内已有权限的声明，不授予应用新权限；草稿仍由维护者手动确认公开。整理任务只有仓库读取权限，发布任务使用 `contents: write` 写入独立软件源分支，不需要额外令牌或 Apple 凭据。

影响边界也需注意：整理需要下载历史 IPA，会消耗 Actions 运行时间和网络流量，但不启动 Xcode/Gradle 编译；下载或校验错误会阻止源发布，已发布源保持不变。源和 IPA 为公开内容，仍需真机验证安装与更新。若暂时不提供软件源，可停用独立整理工作流，并移除 Release 中生成/上传源与报告的步骤，原有 IPA 手动下载签名方式不受影响。

## 给使用者：添加源

本功能面向 **AltStore Classic / SideStore**，两者使用同一份 Source JSON，无需分别维护。这里的 AltStore 不包含 PAL 公证版；IPA 仍需要使用你自己的 Apple ID 签名，免费账号的到期、应用数量等限制不会因此消失。

维护者首次成功运行整理工作流后，可在 AltStore Classic 或 SideStore 的添加源入口中粘贴同一地址（具体按钮位置随客户端版本变化）：

```text
https://raw.githubusercontent.com/BitterLemonn/MCDevManager/altstore-source/source.json
```

然后在应用列表 / Browse 中找到 MCDevManager 安装。已有侧载版本请使用原 Apple ID 覆盖安装，不要先删除应用；操作前备份重要数据。添加源并不会自动恢复删除 AltStore / SideStore 后遗失的管理记录。如果 IPA 包含小组件等扩展，安装时保留需要的扩展。

**上述地址不是本 PR 一提交就生效**：它要求上游采纳后，成功运行下方整理工作流，创建 `altstore-source` 分支。固定地址不依赖哪一个 Release 被标记为 Latest，也不需要修改或重新上传已有 IPA。

## 合并后建议先执行一次：仅整理历史软件源

无需等待下一版，也无需重新打包：

1. 将改动合并到默认分支后，打开仓库 **Actions → Sync AltStore / SideStore Source → Run workflow**。
2. 选择默认分支，保留 `Include prereleases` 为未勾选，执行一次。
3. 工作流分页枚举全部 Release 和附件，下载公开正式版本中的 IPA，从真实安装包读取信息，排序、去重并生成源。
4. 成功后从运行摘要取得固定 Source 地址。生成结果和完整 `report.json` 同时保存在该次 Actions 产物及 `altstore-source` 分支中。

这个 Action 只整理已有文件，不编译、不签名、不改 IPA、不修改历史 Release，也不自动公开草稿。通常不需配置 Secret；若仓库规则禁止工作流向该分支写入，会明确失败，不会自动修改仓库权限。代码修改或替换 Release 附件后，也可以重新运行以重建源。

默认只收录正式版；如确需收录公开预发布版本，可勾选 `Include prereleases`。预发布的真实版本号更高时可能排在首位；此选项只影响本次手动运行，后续自动整理仍按正式版执行。草稿永不收录。

## 历史版本如何处理

- 枚举全部分页，不仅是最近 30/100 个 Release；没有 IPA 的历史记录记为跳过。
- 按 IPA 内真实 `version` / `buildVersion` 从新到旧排列，合并保留版本所需的权限声明。
- **Release 名叫 1.2.9，不代表 IPA 内的版本就是 1.2.9。** 若多个旧包使用相同真实版本号和构建号，源中只能保留一个可区分条目，本实现选择发布时间较新的包，在报告中记录其余冲突及所保留的 Release；不会修改旧包或伪造版本号。
- 实包检查示例（2026-10-27）：上游 `v1.2.8` 与 `v1.2.9` 的公开 IPA 均为 `CFBundleShortVersionString=1.0`、`CFBundleVersion=1`，两包 ZIP 校验通过。这样的历史包不能仅靠 JSON 恢复为两个不同的真实版本；后续 Release 已通过构建参数写入正确版本来避免继续产生冲突。
- 不同 Bundle ID 的 IPA 会跳过并报告；下载失败、大小/哈希不匹配、损坏包或镜像内容不一致会阻止发布，避免部分历史悄悄丢失。
- 每次重新扫描当前已公开的记录，因此撤销发布/删除 Release 后，可从源中移除对应版本。没有任何可用 IPA 时不会用空源覆盖现有源。
- 默认模式下，新发布的真实版本 1.2.10 会和所有仍可用、版本标识不冲突的历史 IPA 一起收录；旧版本的下载文件必须继续可访问。

## 给维护者：正常发布

1. 正常运行现有 **Release** 工作流。
2. iOS 任务把本次发布版本与 Actions 运行编号写入实际应用版本/构建号；打包后，合并本次本地 IPA 和仓库中所有已发布历史 IPA，生成含历史版本的 `source.json` 及整理报告。
3. 现有汇总任务会把 IPA、`source.json` 与报告一同上传到草稿 Release。**本改动不自动公开草稿，不改变原 Release 工作流权限，不触发额外编译。**
4. 核对后按原流程发布。网页手动发布、编辑、删除或撤销公开 Release 时，独立工作流会重新整理所有已发布 IPA，并更新固定地址。草稿中的 JSON 是构建时快照，固定分支上的源以发布后的重新扫描结果为准。

独立工作流必须先合并到默认分支；旧 Release 的标签若不包含该工作流，编辑事件可能不会自动触发，直接手动整理即可。GitHub 使用 `GITHUB_TOKEN` 触发的事件通常不会启动另一个工作流；若以后改为完全由其他 Action 自动公开 Release，应显式触发整理，或手动运行一次。仅修改附件不保证触发 Release 编辑事件，也建议手动整理。

默认下载地址由 `altstore/source.template.json` 中的 `downloadURL` 控制：

```json
"downloadURL": "https://github.com/BitterLemonn/MCDevManager/releases/download/{tag}/{filename}"
```

- `{tag}` 是传给工具的 Release 标签，如 `v1.2.9`。
- `{filename}` 是实际 IPA 文件名，工具会进行 URL 编码。
- 也可填写完整 HTTPS 直链，不使用占位符。不支持需要登录、临时签名或解压 ZIP 才能取得 IPA 的链接。
- 模板不是最终软件源：**不要把 `source.template.json` 的地址加进 AltStore / SideStore**。

## Fork / 自建下载站：只改下载地址

若继续分发同一 Bundle ID 的 MCDevManager，只是下载站或仓库不同，直接修改模板中这一项即可。例如：

```json
"downloadURL": "https://github.com/YOUR_NAME/MCDevManager/releases/download/{tag}/{filename}"
```

然后按相同流程生成。Action 自动扫描所在仓库，不需另改仓库名；**不用手工填写版本、构建号、大小、最低系统版本和权限**。Fork 的固定源地址对应为 `https://raw.githubusercontent.com/YOUR_NAME/MCDevManager/altstore-source/source.json`。模板的作者与图标默认保留上游署名，源名、描述等品牌信息可按需要另外定制。

历史整理要求每个版本有独立、长期有效的下载地址，不要把全部版本都指向同一条“最新版 IPA”地址。自建镜像的文件必须与 Release 附件字节一致，整理时会校验；不能只更换 URL 却让它返回另一个版本。

若同时修改了应用 Bundle ID，则模板的 `bundleIdentifier` 也必须一致，工具会阻止错配。不要把完整源 JSON 永久复制后每次只换 URL：旧版本号/包大小可能让 AltStore 判断更新或验证安装失败。

## 不重新打包，给已有 IPA 生成源

使用 macOS 自带 `codesign` 与 Python 3.9+，无需安装第三方 Python 库：

```shell
python3 iosApp/scripts/generate_altstore_source.py \
  --ipa /path/to/MCDevManager.ipa \
  --release-tag v1.2.9 \
  --output build/altstore/source.json
```

把实际 IPA 与生成的 JSON 上传到配置对应的 HTTPS 服务即可。工具本身不签名、不改包、不访问 Apple 账号，也不会上传任何文件。

单包工具的可选参数：`--template` 使用自定义模板；`--date` 指定带时区的 ISO 8601 发布时间；`--previous` 读取已发布源，保留版本历史。正式工作流使用下面的全历史整理工具，不依赖旧源 JSON 是否完整。

```shell
python3 iosApp/scripts/generate_altstore_source.py \
  --ipa /path/to/MCDevManager.ipa --release-tag v1.2.9 \
  --previous old-source.json --output build/altstore/source.json
python3 iosApp/scripts/sync_altstore_source.py --repository BitterLemonn/MCDevManager
python3 -B -m unittest discover -s iosApp/scripts -p 'test_*altstore*.py'
```

## 更新、权限与限制

- AltStore 按 `versions` 数组顺序与真实 `version` / `buildVersion` 判断更新，不按 IPA 文件名或发布日期判断。
- Release 工作流将 `MARKETING_VERSION` 设为本次输入的数字 `X.Y.Z`（允许前缀 `v`），`CURRENT_PROJECT_VERSION` 设为该工作流的运行编号。直接本地打包或使用其他工作流时，也应确保这两个值递增；只改 IPA 文件名不会改变包内版本，本工具不会伪造版本号。重跑同一次 Actions 的编号不变，同版本再次公开新版时应发起新的工作流运行。
- 完全未签名包没有内嵌 entitlement；有临时签名的定制包则读取实际签名权限，包括扩展 App Groups。不要在生成 JSON 后再修改/重签 IPA 却不重新生成。
- `--previous` 会拒绝旧版本置顶和同一版本/构建号指向不同地址或大小的包。历史版本必须保留对应下载文件。
- 发布前仍需在真机验证添加源、安装、覆盖更新和扩展。本工具及 CI 测试不能替代真机验收。
- 软件源不提供托管签名服务，不绕过 iOS 限制，也不会自动接管已安装但不在 AltStore 列表中的应用。

参考：[AltStore 官方源格式](https://faq.altstore.io/developers/make-a-source)、[更新规则](https://faq.altstore.io/developers/updating-apps)、[SideStore 添加 AltSource](https://docs.sidestore.io/docs/advanced/app-sources)、[GitHub Release 事件](https://docs.github.com/en/actions/reference/workflows-and-actions/events-that-trigger-workflows#release)。
