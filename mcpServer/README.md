# MCDev MCP Server

独立的 Kotlin/JVM stdio MCP Server，复用 `shared` 中的网易 MC 开发者平台登录、作品管理和文件上传流程。

## 源码仓库内使用

项目根目录已提供 `.mcp.json`。支持项目级 MCP 配置的 agent 客户端会自动启动 stdio 子进程，无需用户手动运行
MCP Server。首次使用时客户端可能要求批准项目 MCP 配置。

`mcpServer/run-mcp.bat` 会在分发尚未构建时自动执行一次 `:mcpServer:installDist`，并将 Gradle 输出重定向到
`stderr`，避免污染 stdio JSON-RPC。也可以手动构建：

```bash
./gradlew :mcpServer:installDist
```

源码树中的 `.mcp.json` 与 `run-mcp.bat` 仅用于项目开发，不属于正式 Release 分发包。

## GitHub Release 分发

正式发版时 GitHub Actions 会自动生成 `MCDevManagerMP-V<version>-mcp.zip`。该 zip 可跨平台使用，内含完整
JVM 依赖及两种启动脚本：

```text
mcdev-mcp-<version>/
├── bin/
│   ├── mcdev-mcp.bat   # Windows
│   └── mcdev-mcp       # macOS / Linux
└── lib/                # 运行时依赖
```

使用前需安装 JRE/JDK 11 或更高版本。解压后将 MCP 客户端的 `command` 指向对应启动脚本，无需
Gradle，也无需启动主 App。macOS/Linux 若解压工具未保留可执行权限，请执行：

```bash
chmod +x /path/to/mcdev-mcp-<version>/bin/mcdev-mcp
```

## 环境变量

| 名称               | 必需    | 说明                               |
|------------------|-------|----------------------------------|
| `MCDEV_EMAIL`    | 与密码成对 | 网易账号；仅 MCP 进程读取，不会进入 Tool 参数或返回值 |
| `MCDEV_PASSWORD` | 与账号成对 | 网易密码；启用 Cookie 过期后的自动重新登录        |
| `NTES_SESS`      | 二选一   | Cookie 单值或完整 Cookie 字符串          |

至少配置 `NTES_SESS` 或 `MCDEV_EMAIL` + `MCDEV_PASSWORD`。若同时配置，优先验证 Cookie，失效后回退账号密码登录。

Windows Release 配置示例：

```json
{
    "mcpServers": {
        "mcdev": {
            "type": "stdio",
            "command": "cmd.exe",
            "args": [
                "/d",
                "/c",
                "C:\\path\\to\\mcdev-mcp-1.2.2\\bin\\mcdev-mcp.bat"
            ],
            "env": {
                "NTES_SESS": "由本地安全配置注入"
            }
        }
    }
}
```

macOS/Linux Release 配置示例：

```json
{
    "mcpServers": {
        "mcdev": {
            "type": "stdio",
            "command": "/path/to/mcdev-mcp-1.2.2/bin/mcdev-mcp",
            "env": {
                "NTES_SESS": "由本地安全配置注入"
            }
        }
    }
}
```

示例中的版本号和解压路径请替换为实际值。也可以在 `env` 中同时配置 `MCDEV_EMAIL` 与 `MCDEV_PASSWORD`
，用于 Cookie 失效后的自动重新登录。

## 工具列表

| 工具                        | 类型  | 说明                                             |
|---------------------------|-----|------------------------------------------------|
| `list_works`              | 只读  | 按平台、状态和名称查询作品，返回作品状态及当前允许的操作。                  |
| `get_work_detail`         | 只读  | 获取指定作品的完整详情、当前状态和允许的操作。                        |
| `get_work_reference_data` | 只读  | 获取创建和编辑作品所需的分类、渠道及标签参考数据。                      |
| `get_review_feedback`     | 只读  | 获取指定作品的审核反馈。                                   |
| `upload_work_asset`       | 写入  | 上传 agent 可访问的本地资源包、图片或视频，返回当前进程内有效的 `assetId`。 |
| `create_work`             | 写入  | 创建 PE 作品，可通过 `assetId` 引用资源包和宣传图，并可选择创建时提审。    |
| `update_work`             | 写入  | 使用白名单 Merge Patch 编辑作品，可更新基本信息、资源、宣传图、视频及授权图。  |
| `submit_review`           | 写入  | 提交待审核作品。                                       |
| `cancel_review`           | 写入  | 取消正在准备或审核中的作品审核。                               |
| `submit_self_test`        | 写入  | 提交作品自测，可指定是否免机审。                               |
| `cancel_self_test`        | 写入  | 取消正在进行或准备中的自测。                                 |
| `publish_work`            | 写入  | 将已通过审核的作品立即上架。                                 |
| `schedule_work_publish`   | 写入  | 设置定时上架时间；省略时间可取消定时上架。                          |
| `change_work_price`       | 写入  | 调整已上架且非免费的作品价格。                                |
| `delete_work`             | 破坏性 | 永久删除待提交审核的草稿；必须由 MCP 客户端发起原生确认并由用户明确同意。        |

写入工具会在执行前重新查询作品状态，并依据项目现有的 `WorkItemStatusEnum.actions()` 检查操作是否合法。上传得到的
`assetId` 仅在当前 MCP 进程内有效，服务重启后需要重新上传。

## 安全约束

- 凭据只从进程环境读取，不存在设置或查看凭据的 MCP Tool。
- `stdout` 仅用于 MCP 协议；普通输出重定向到 `stderr`。
- `upload_work_asset` 直接接收 agent 提供的本地文件路径；路径必须存在、可读且为普通文件。
- 上传回执只保存在当前进程内，并以 opaque `assetId` 提供给创建/编辑工具。
- `delete_work` 没有模型可填写的确认参数；只有客户端支持 `elicitation/create`、用户接受并勾选确认后才执行。
