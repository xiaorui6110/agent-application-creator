可以，直接给你一套 **Windows 可落地** 的方案。
先说结论：**Codex 目前公开的 `config.toml` / CLI 选项里，没有文档化的“关闭 BOM”配置项**；官方配置参考和 CLI 参考没列出编码开关，社区 issue 里也有人反馈“目前没有支持的配置项能强制改文件编码”。([OpenAI开发者][1])

所以最稳的打法是：

1. 正常跑 Codex
2. **自动清理 `.java` 文件的 BOM**
3. 可选：Git 提交前再兜底一次

下面这套你复制就能用。

---

## 方案 A：Windows 包一层脚本，跑完 Codex 自动去 BOM

新建文件：`codex-safe.ps1`

```powershell
param(
    [Parameter(ValueFromRemainingArguments = $true)]
    [string[]]$CodexArgs
)

$ErrorActionPreference = "Stop"

function Remove-Utf8BomFromFile {
    param(
        [Parameter(Mandatory = $true)]
        [string]$Path
    )

    if (-not (Test-Path -LiteralPath $Path)) {
        return
    }

    $bytes = [System.IO.File]::ReadAllBytes($Path)

    if ($bytes.Length -ge 3 -and
        $bytes[0] -eq 0xEF -and
        $bytes[1] -eq 0xBB -and
        $bytes[2] -eq 0xBF) {

        $newBytes = New-Object byte[] ($bytes.Length - 3)
        [System.Array]::Copy($bytes, 3, $newBytes, 0, $newBytes.Length)
        [System.IO.File]::WriteAllBytes($Path, $newBytes)
        Write-Host "[BOM removed] $Path"
    }
}

function Remove-BomRecursively {
    param(
        [string]$Root = ".",
        [string[]]$Extensions = @("*.java")
    )

    foreach ($pattern in $Extensions) {
        Get-ChildItem -LiteralPath $Root -Recurse -File -Filter $pattern |
            ForEach-Object {
                Remove-Utf8BomFromFile -Path $_.FullName
            }
    }
}

Write-Host "==> Running Codex..."
& codex @CodexArgs
$exitCode = $LASTEXITCODE

Write-Host "==> Cleaning UTF-8 BOM from Java files..."
Remove-BomRecursively -Root "." -Extensions @("*.java")

if ($exitCode -ne 0) {
    Write-Host "Codex exited with code $exitCode"
}

exit $exitCode
```

### 用法

以后别直接敲 `codex`，改成：

```powershell
powershell -ExecutionPolicy Bypass -File .\codex-safe.ps1 "帮我生成一个 Spring Boot Controller 和 service"
```

或者带正常参数也行：

```powershell
powershell -ExecutionPolicy Bypass -File .\codex-safe.ps1 --full-auto "重构这个模块"
```

---

## 方案 B：顺手把常见 Java 工程文件也清掉

如果你不只想处理 `.java`，还想顺手净化这些：

* `.xml`
* `.properties`
* `.yml`
* `.yaml`
* `.kt`
* `.gradle`

把这行：

```powershell
Remove-BomRecursively -Root "." -Extensions @("*.java")
```

改成：

```powershell
Remove-BomRecursively -Root "." -Extensions @("*.java", "*.xml", "*.properties", "*.yml", "*.yaml", "*.kt", "*.gradle")
```

这就属于“宁可多洗一遍，也不让脏东西进门”。

---

## 方案 C：Git 提交前自动去 BOM，彻底兜底

在仓库里新建：`.git/hooks/pre-commit`

```powershell
#!/usr/bin/env pwsh

$ErrorActionPreference = "Stop"

function Remove-Utf8BomFromFile {
    param([string]$Path)

    if (-not (Test-Path -LiteralPath $Path)) {
        return
    }

    $bytes = [System.IO.File]::ReadAllBytes($Path)

    if ($bytes.Length -ge 3 -and
        $bytes[0] -eq 0xEF -and
        $bytes[1] -eq 0xBB -and
        $bytes[2] -eq 0xBF) {

        $newBytes = New-Object byte[] ($bytes.Length - 3)
        [System.Array]::Copy($bytes, 3, $newBytes, 0, $newBytes.Length)
        [System.IO.File]::WriteAllBytes($Path, $newBytes)
        Write-Host "[BOM removed] $Path"
    }
}

$files = git diff --cached --name-only --diff-filter=ACM | Where-Object {
    $_ -match '\.(java|xml|properties|yml|yaml|kt|gradle)$'
}

foreach ($file in $files) {
    Remove-Utf8BomFromFile -Path $file
    git add -- $file
}

exit 0
```

### 注意

如果你机器没有 `pwsh`，把第一行改成：

```powershell
#!/usr/bin/env powershell
```

不过我更推荐 **PowerShell 7 (`pwsh`)**，编码这块更省心。

---

## 方案 D：给你一个最省事的别名

你可以在 PowerShell profile 里加个函数，以后像原生命令一样用。

打开 profile：

```powershell
notepad $PROFILE
```

追加：

```powershell
function codexj {
    powershell -ExecutionPolicy Bypass -File "D:\tools\codex-safe.ps1" @args
}
```

把路径改成你真实脚本路径。

以后直接：

```powershell
codexj "生成一个 MyBatis Plus 的分页查询"
```

丝滑，不用每次手敲一长串。

---

## 再补一刀：IDE 也设成无 BOM

如果你用 IntelliJ IDEA：

* `Settings`
* `Editor`
* `File Encodings`
* 全部设成 `UTF-8`
* 确保不是 `UTF-8 BOM`

这样就算 Codex 手滑，IDE 也不至于继续拱火。

---

## 你现在最推荐的落地组合

我建议你直接上这套：

* **平时用 `codex-safe.ps1` 包装 Codex**
* **仓库里加 `pre-commit` 再兜底**
* **IDE 编码设为 UTF-8 无 BOM**

这基本就是给 BOM 判了个无期徒刑。

要是你愿意，我下一条可以直接给你再补一个 **“一键生成 Spring Boot 代码 + 自动去 BOM + `mvn test`”** 的增强版脚本。

[1]: https://developers.openai.com/codex/config-reference/ "Configuration Reference – Codex | OpenAI Developers"
